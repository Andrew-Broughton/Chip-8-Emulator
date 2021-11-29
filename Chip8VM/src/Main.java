import javax.swing.JFrame;

public class Main extends JFrame
{
	Keyboard keyboard;
	Monitor monitor;
	Main()
	{
		keyboard = new Keyboard();
		monitor = new Monitor(keyboard);
		this.add(monitor);
		this.setTitle("Chip-8 Emulator");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}

	public static void main(String[] args)
	{
		//System.out.println("PC\tOPCODE\t\tV Registers");
		new Main();
	}

}
