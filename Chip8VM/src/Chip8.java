import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class Chip8 implements ActionListener
{
	final short MEMORY_SIZE = 4096; // Amount of memory the Chip-8 contains (in bytes)
	final byte NUM_REGISTERS = 16; // The amount of registers in the Chip-8's CPU
	int[] memory; // Array of memory addresses
	int[] v; // Array of registers
	int index; // Index register
	int pc; // Program counter register, acts as a pointer to the line of code being executed
	int[] stack; // The program stack
	int sp; // Stack pointer
	int delayTimer; // Timer for animations used in games
	int soundTimer; // Timer for audio
	boolean paused; // Check to see if the system is paused
	int speed; // Speed of the CPU cycles
	InputStream rom;
	Monitor monitor;
	Keyboard keyboard;
	Timer timer;
	
	Chip8(Monitor monitor, Keyboard keyboard)
	{
		this.memory = new int[MEMORY_SIZE];
		this.v = new int[NUM_REGISTERS];
		this.index = 0;
		this.pc = 0x200;
		this.stack = new int[16];
		this.sp = 0;
		this.delayTimer = 0;
		this.soundTimer = 0;
		
		// Keyboard
		this.keyboard = keyboard;
		
		this.monitor = monitor;
		this.paused = false;
		this.speed = 10;
		
		try
		{
			int[] romCode = loadRom();
			loadSpritesIntoMemory();
			loadProgramIntoMemory(romCode);
			timer = new Timer(10, this);
			timer.start();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int[] loadRom() throws IOException
	{
		rom = getClass().getResourceAsStream("BRICK");
		int[] romCode = new int[rom.available()];
		for(int i = 0; i < romCode.length; i++)
		{
			romCode[i] = rom.read();
		}
		
		return romCode;
	}
	
	public void loadSpritesIntoMemory()
	{
		int[] sprites = {
				0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
	            0x20, 0x60, 0x20, 0x20, 0x70, // 1
	            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
	            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
	            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
	            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
	            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
	            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
	            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
	            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
	            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
	            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
	            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
	            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
	            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
	            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
		};
		for(int i = 0; i < sprites.length; i++)
		{
			memory[i] = sprites[i];
		}
		
	}
	
	public void loadProgramIntoMemory(int[] program)
	{
		for(int i = 0; i < program.length; i++)
		{
			memory[0x200 + i] = program[i];
		}
	}
	
	public void cycle()
	{
		for(int i = 0; i < speed; i++)
		{
			if(!paused)
			{
				int opcode = memory[pc] << 8 | memory[pc + 1];
				//System.out.print("PC " + Integer.toHexString(pc) + ": ");
				//System.out.println(Integer.toHexString(opcode));
				interpretInstructions(opcode);
			}
		}
		
		if(!paused)
		{
			updateTimers();
		}
		//sound();
		monitor.repaint();
	}
	
	public void updateTimers()
	{
		if(delayTimer > 0)
		{
			delayTimer -= 1;
		}
		
		if(soundTimer > 0)
		{
			soundTimer -= 1;
		}
	}
	
	public void interpretInstructions(int instruction)
	{
		pc += 2;
		instruction &= 0xFFFF;
		for(int i = 0; i < v.length; i++)
		{
			v[i] &= 0xFF;
		}
		
		final int x = (instruction & 0x0F00) >> 8;
		final int y = (instruction & 0x00F0) >> 4;
		
		switch(instruction & 0xF000)
		{
			case 0x0000:
				switch(instruction)
				{
					case 0x00E0:
						// CLR
						monitor.clear();
						break;
					case 0x00EE:
						// RET
						pc = stack[--sp];
						break;
					default:
						System.out.println("ERROR: 0x0 BAD INSTRUCTION");
						break;
				}
				break;
			case 0x1000:
				// JP addr
				pc = instruction & 0x0FFF;
				break;
			case 0x2000:
				// CALL addr
				stack[sp] = pc;
				sp++;
				pc = (instruction & 0x0FFF);
				break;
			case 0x3000:
				// SE Vx, byte
				if(v[x] == (instruction & 0xFF))
				{
					pc += 2;
				}
				break;
			case 0x4000:
				// SNE Vx, byte
				if(v[x] != (instruction & 0xFF))
				{
					pc += 2;
				}
				break;
			case 0x5000:
				// SE Vx, Vy
				if(v[x] == v[y])
				{
					pc += 2;
				}
				break;
			case 0x6000:
				// LD Vx, byte
				v[x] = (instruction & 0xFF);
				break;
			case 0x7000:
				// ADD Vx, byte
				v[x] += (instruction & 0xFF);
				break;
			case 0x8000:
				switch(instruction & 0xF)
				{
					case 0x0:
						// LD Vx, Vy
						v[x] = v[y];
						break;
					case 0x1:
						// OR Vx, Vy
						v[x] |= v[y];
						break;
					case 0x2:
						// AND Vx, Vy
						v[x] &= v[y];
						break;
					case 0x3:
						// XOR Vx, Vy
						v[x] ^= v[y];
						break;
					case 0x4:
						// ADD Vx, Vy
						int sum = (v[x] += v[y]);
						v[0xF] = 0;
						if(sum > 0xFF) v[0xF] = 1;
						v[x] = (sum/* & 0xFF*/);						
						break;
					case 0x5:
						// SUB Vx, Vy
						v[0xF] = 0;
						if(v[x] > v[y]) v[0xF] = 1;
						
						v[x] -= v[y];
						break;
					case 0x6:
						// SHR Vx, Vy
						v[0xF] = v[x] & 0x1;
						v[x] >>= 1;
						break;
					case 0x7:
						// SUBN Vx, Vy
						v[0xF] = 0;
						if(v[y] > v[x])
						{
							v[0xF] = 1;
						}
						v[x] = v[y] - v[x];
						break;
					case 0xE:
						// SHL Vx {, Vy}
						v[0xF] = v[x] & 0x80;
						v[0xF] >>= 7;
						v[x] <<= 1;
						break;
					default:
						System.out.print("ERROR: 0x8 BAD INSTRUCTION");
						break;
				}
				break;
			case 0x9000:
				// SNE Vx, Vy
				if(v[x] != v[y])
				{
					pc += 2;
				}
				break;
			case 0xA000:
				// LD I, addr
				index = instruction & 0x0FFF;
				break;
			case 0xB000:
				// JP V0, addr
				pc = (instruction & 0x0FFF) + v[0];
				break;
			case 0xC000:
				// RND Vx, byte
				int rand = (int) Math.floor(Math.random() * 0xFF);
				v[x] = rand & (instruction & 0xFF);
				break;
			case 0xD000:
				// DRW Vx, Vy, nibble
				int width = 8;
				int height = (instruction & 0xF);
				
				v[0xF] = 0;
				
				for(int row = 0; row < height; row++)
				{
					int sprite = memory[index + row];
					
					for(int col = 0; col < width; col++)
					{
						
						if((sprite & 0x80) > 0)
						{
							if(monitor.setPixel(v[x] + col, v[y] + row))
							{
								v[0xF] = 1;
							}
						}
						sprite <<= 1;
					}
				}
				break;
			case 0xE000:
				switch(instruction & 0xFF)
				{
					case 0x9E:
						// SKP Vx
						if(keyboard.isKeyPressed(v[x]));
						{
							pc += 2;
						}
						break;
					case 0xA1:
						// SKNP Vx
						if(!keyboard.isKeyPressed(v[x]))
						{
							pc += 2;
						}
						break;
					default:
						System.out.println("ERROR: BAD INSTRUCTION");
						break;
				}
				break;
			case 0xF000:
				switch(instruction & 0xFF)
				{
					case 0x07:
						// LD Vx, DT
						v[x] = delayTimer;
						break;
					case 0x0A:
						// LD Vx, k
						paused = true;
						if(keyboard.onNextKeyPressed())
						{
							for(int i = 0; i < keyboard.keysPressed.length; i++)
							{
								if(keyboard.keysPressed[i])
								{
									v[x] = keyboard.keymapVal[i];
									paused = false;
								}
							}
						}
						break;
					case 0x15:
						// LD Dt, Vx
						delayTimer = v[x];
						break;
					case 0x18:
						// LD ST, Vx
						soundTimer = v[x];
						break;
					case 0x1E:
						// ADD I, Vx
						index += v[x];
						break;
					case 0x29:
						// LD F, Vx
						index = v[x] * 5;
						break;
					case 0x33:
						// LD B, Vx
						memory[index] = v[x] / 100;
						memory[index + 1] = (v[x] % 100) / 10;
						memory[index + 2] = v[x] % 10;
						break;
					case 0x55:
						// LD [I], Vx
						for(int ri = 0; ri <= x; ri++)
						{
							memory[index + ri] = v[ri];							
						}
						break;
					case 0x65:
						// LD Vx, [I]
						for(int ri = 0; ri <= x; ri++)
						{
							v[ri] = memory[index + ri];
						}
						break;
					default:
						System.out.println("ERROR: 0xF BAD INSTRUCTION");
				}
			System.out.println(Integer.toHexString(pc)+"\t"+Integer.toHexString(instruction)+"\t\t"+
					Integer.toHexString(v[0])+"\t"+
					Integer.toHexString(v[1])+"\t"+
					Integer.toHexString(v[2])+"\t"+
					Integer.toHexString(v[3])+"\t"+
					Integer.toHexString(v[4])+"\t"+
					Integer.toHexString(v[5])+"\t"+
					Integer.toHexString(v[6])+"\t"+
					Integer.toHexString(v[7])+"\t"+
					Integer.toHexString(v[8])+"\t"+
					Integer.toHexString(v[9])+"\t"+
					Integer.toHexString(v[10])+"\t"+
					Integer.toHexString(v[11])+"\t"+
					Integer.toHexString(v[12])+"\t"+
					Integer.toHexString(v[13])+"\t"+
					Integer.toHexString(v[14])+"\t"+
					Integer.toHexString(v[15]));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		cycle();		
	}
	
}
