import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener
{
	int[] keymap;
	int[] keymapVal;
	boolean[] keysPressed = new boolean[16];
	int onNextKeyPress = -1;
	int[][] keys;
	boolean reset = false;
	
	Keyboard()
	{
		keymap = new int[]
			{
				49, 50, 51, 52,
				81, 87, 69, 82,
				65, 83, 68, 70,
				90, 88, 67, 86
			};
		
		keymapVal = new int[]
			{
				0x1, 0x2, 0x3, 0xC,
				0x4, 0x5, 0x6, 0xD,
				0x7, 0x8, 0x9, 0xE,
				0xA, 0xB, 0xC, 0xF
			};
		
	}
	
	public boolean resetPressed()
	{
		return reset;
	}
	
	public boolean isKeyPressed(int keyCode)
	{
		return keysPressed[keyCode];
	}
	
	public boolean onNextKeyPressed()
	{
		for(int i = 0; i < keysPressed.length; i++)
		{
			if(keysPressed[i])
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}


	@Override
	public void keyPressed(KeyEvent e)
	{
		for(int i = 0; i < keymap.length; i++)
		{
			if(e.getKeyCode() == keymap[i])
			{
				keysPressed[i] = true;
			}
		}
	}


	@Override
	public void keyReleased(KeyEvent e)
	{
		for(int i = 0; i < keymap.length; i++)
		{
			if(e.getKeyCode() == keymap[i])
			{
				keysPressed[i] = false;
			}
		}
	}

}
