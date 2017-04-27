package ColorMixer;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ColorMixer.ai.EvolverView;
import ColorMixer.ai.View;

// Runs the entire program and handles drawing the window
public class ColorMixer extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static int width = 600;
	public static int height = width / 16 * 9; // 16:9 aspect ratio
	public static int scale = 2; // The view will be scaled up by this factor, so the actual window width and height will be the above values times this value
	public static String title = "ColorMixer";

	// Setup variables which should have been changed by user in startup of program
	public static int MODE = 0;
	public static int NUM_MID = 3;
	public final int NUM_CREATURES = 40;
	public static int NUM_GOALS = 3;
	public final double MUTATION_RATE = 0.01;
	public final double LEARNING_RATE = 0.01;
	public static int[] GOAL = {0xff0000, 0xff00, 0xff};
	public final int CIRCLE_SIZE_PARAM = 12;

	// We will run the display within a new thread
	private Thread thread;
	private JFrame frame;
	private View view;
	private boolean running = false;

	// The image which will be drawn in the window
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

	// Initiates the necessary variables of the ColorMixer
	public ColorMixer() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		frame = new JFrame();
		// Setup a genetic algorithm or back propagation search depending on user input
		if (MODE == 0)
			view = new EvolverView(NUM_MID, 256, NUM_CREATURES, GOAL, MUTATION_RATE);
		else
			view = new View(NUM_MID, 256, GOAL, LEARNING_RATE);
	}

	// Returns the height of the window with scaling.
	public int getWindowWidth() {
		return frame.getContentPane().getWidth();
	}

	// Returns the height of the window with scaling.
	public int getWindowHeight() {
		return frame.getContentPane().getHeight();
	}

	// Starts the main thread
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}

	// Stops the main thread
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Runs the program loop continuously
	public void run() {
		requestFocus();

		// The program loop
		while (running) {
			update();
			Graphics g = getGraphics();
			paint(g);
		}

		// If we get out of the program loop stop the program
		stop();
	}

	// Update the view
	public void update() {
		view.update();
	}

	// We overwite the default screen update function
	public void update(Graphics g) {
	}

	// Render the screen
	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		// Clear screen before rendering
		gi.clearRect(0, 0, getWidth(), getHeight());
		// Render the screen
		view.render(gi, CIRCLE_SIZE_PARAM);
		gi.dispose();

		// Copy the image to the screen
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
	}

	// Start of the program
	public static void main(String[] args) {
		System.setProperty("sun.awt.noerasebackground", "true");

		// Setup
		// User chooses what method to train network
		String[] choices = { "Genetic Algorithm", "Back Propagation" };
		String input = (String) JOptionPane.showInputDialog(null, "How will the network learn?",
			"Learning Setup", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		if (input.equals(choices[0]))
			MODE = 0;
		else
			MODE = 1;
		// User chooses number of nodes in hidden layer and output layer
		NUM_MID = Integer.parseInt(JOptionPane.showInputDialog("How many linking nodes?", "3"));
		NUM_GOALS = Integer.parseInt(JOptionPane.showInputDialog("How many goals?", "3"));
		// User defines the goals of the nueral network
		String[] inputs = JOptionPane.showInputDialog("What are the goals? (" + NUM_GOALS + " total)", "0xff0000, 0xff00, 0xff").split(", ");
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (String s : inputs){
			colors.add(Integer.decode(s));
		}
		// Add the colors to the list of goals
		GOAL = new int[colors.size()];
		for (int i=0; i<colors.size(); i++)
			GOAL[i] = colors.get(i).intValue();

		// Create the ColorMixer
		ColorMixer mixer = new ColorMixer();
		mixer.frame.setResizable(true);
		mixer.frame.setTitle(ColorMixer.title);
		mixer.frame.add(mixer);
		mixer.frame.pack();
		mixer.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mixer.frame.setLocationRelativeTo(null);
		mixer.frame.setVisible(true);

		// Start the ColorMixer
		mixer.start();
	}
}
