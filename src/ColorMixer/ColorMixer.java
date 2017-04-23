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

public class ColorMixer extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static int width = 600;
	public static int height = width / 16 * 9;
	public static int scale = 2; // The view will be scaled up by this factor, so the actual window width and height will be the above values times this value
	public static String title = "ColorMixer";

	public static int MODE = 0;
	public static int NUM_MID = 3;
	public final int NUM_CREATURES = 40;
	public static int NUM_GOALS = 3;
	public final double MUTATION_RATE = 0.01;
	public final double LEARNING_RATE = 0.01;
	public static int[] GOAL = {0xff0000, 0xff00, 0xff};
	public final int CIRCLE_SIZE_PARAM = 12;

	public int anim = 0, speed = 1, step = 0;

	private Thread thread;
	private JFrame frame;
	private View view;
	private boolean running = false;

	// The image which will be drawn in the window
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

	/**
	 * Initiates the necessary variables of the ColorMixer
	 */
	public ColorMixer() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		frame = new JFrame();
		if (MODE == 0)
			view = new EvolverView(NUM_MID, 256, NUM_CREATURES, GOAL, MUTATION_RATE);
		else
			view = new View(NUM_MID, 256, GOAL, LEARNING_RATE);

		define();
	}

	private void define() {

	}

	/**
	 * Returns the height of the window with scaling.
	 * @return The width as an int value
	 */
	public int getWindowWidth() {
		return frame.getContentPane().getWidth();
	}

	/**
	 * Returns the height of the window with scaling.
	 * @return The height as an int value
	 */
	public int getWindowHeight() {
		return frame.getContentPane().getHeight();
	}

	/**
	 * Starts the main thread
	 */
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}

	/**
	 * Stops the main thread
	 */
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Runs the program loop
	 */
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0, updates = 0;
		requestFocus();

		// The program loop
		while (running) {
			// long now = System.nanoTime();
			// delta += (now - lastTime) / ns;
			// lastTime = now;
			update();
			// // Update 60 times a second
			// while (delta >= 1) {
			// 	updates++;
			// 	delta--;
			// }
			Graphics g = getGraphics();
			paint(g);
			frames++;

			// // Keep track of and display the program's ups and fps every second
			// if (System.currentTimeMillis() - timer >= 1000) {
			// 	timer += 1000;
			// 	frame.setTitle(title + " | ups: " + updates + ", fps: " + frames);
			// 	updates = 0;
			// 	frames = 0;
			// }
		}

		// If we get out of the program loop stop the program
		stop();
	}

	/**
	 * Update the view
	 */
	public void update() {
		// // Optional: limit speed
		// if (anim < 750 * speed)
		// 	anim++;
		// else
		// 	anim = 0;
		// if (anim % speed == speed - 1)
			view.update();
	}

	/**
	 * Render the screen
	 */

	public void update(Graphics g) {
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		gi.clearRect(0, 0, getWidth(), getHeight());
		view.render(gi, CIRCLE_SIZE_PARAM);
		gi.dispose();

		// Draw the image
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
	}

	/**
	 * Start of the program
	 * @param args : Unused default arguments
	 */
	public static void main(String[] args) {
		System.setProperty("sun.awt.noerasebackground", "true");

		String[] choices = { "Genetic Algorithm", "Back Propogation" };
		String input = (String) JOptionPane.showInputDialog(null, "How will the network learn?",
			"Learning Setup", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		if (input.equals(choices[0]))
			MODE = 0;
		else
			MODE = 1;
		NUM_MID = Integer.parseInt(JOptionPane.showInputDialog("How many linking nodes?", "3"));
		NUM_GOALS = Integer.parseInt(JOptionPane.showInputDialog("How many goals?", "3"));
		String[] inputs = JOptionPane.showInputDialog("What are the goals? (" + NUM_GOALS + " total)", "0xff0000, 0xff00, 0xff").split(", ");
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for (String s : inputs){
			colors.add(Integer.decode(s));
		}
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
