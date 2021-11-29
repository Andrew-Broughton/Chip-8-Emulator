import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

public class Monitor extends JPanel implements ActionListener
{
	final int COLS = 64; // Screen width in pixels
	final int ROWS = 32; // Screen height in pixels
	final int SCALE = 15; // Size of each pixel
	final int WIDTH = COLS * SCALE; // Total width of screen
	final int HEIGHT = ROWS * SCALE; // Total height of screen
	final Color BGCOLOR = new Color(0, 0, 0); // Color of background
	final Color FGCOLOR = new Color(255, 255, 255); // Color of pixels
	byte[] display = new byte[COLS * ROWS]; // Array of each pixel on screen
	Chip8 chip8;

	Monitor(Keyboard keyboard)
	{	
		this.setPreferredSize(new Dimension(COLS * SCALE, ROWS * SCALE));
		this.setBackground(BGCOLOR);
		this.setFocusable(true);
		this.addKeyListener(keyboard);
		chip8 = new Chip8(this, keyboard);
	}
	
	// Method that toggles pixels on or off at a specific coordinate 
	public boolean setPixel(int x, int y)
	{
		// These if statements wrap the value of x and y if they are over the width
		// or height of the screen
		if(x >= COLS)
		{
			x -= COLS;
		}
		else if(x < 0)
		{
			x += COLS;
		}
		
		if(y >= ROWS)
		{
			y -= ROWS;
		}
		else if(y < 0)
		{
			y += ROWS;
		}
		
		// This statement gets the pixel at the x and y postion and then does a bitwise XOR
		// assignment with 1 to flip the bit 
		display[x + (y * COLS)] ^= 1;
		
		// The method then returns true if the pixel was turned off, used for collision detection 
		return display[x + (y * COLS)] != 1;
	}
	
	// This method turns off all pixels on the screen
	public void clear()
	{
		for(int i = 0; i < display.length; i++)
		{
			display[i] = 0;
		}
	}
	
	// Paint method for the Graphics class
	public void paintComponenet(Graphics g)
	{
		super.paintComponent(g);
		paint(g);
	}
	
	// Child paint method for the graphics
	public void paint(Graphics g)
	{
		g.setColor(BGCOLOR); // Sets the color to black
		g.fillRect(0, 0, WIDTH, HEIGHT); // Fills the whole screen with black
		
		// For loop to iterate through every pixel in the display
		for(int i = 0; i < display.length; i++)
		{
			// Creates x and y variables and scales them up to the pixel size
			int x = (i % COLS) * SCALE;
			int y = (int) (Math.floor(i / COLS) * SCALE);
			
			// Checks if each pixel is turned on
			if(display[i] == 1)
			{
				g.setColor(FGCOLOR); // Changes color to white
				g.fillRect(x, y, SCALE, SCALE); // Draws a pixel based on the scale
			}
		}
	}
	
	public void testRender()
	{
		setPixel(0, 0);
		setPixel(5, 2);
		setPixel(5, 2);
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
}
