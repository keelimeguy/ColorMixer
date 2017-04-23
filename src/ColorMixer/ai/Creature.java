package ColorMixer.ai;

import java.util.Random;

public class Creature implements Comparable<Creature> {

	private double[] weights;
	private int id;
	private View view;

	public Creature(int numWeights, int maxWeight, int id, View view) {
		// Create random network
		Random rand = new Random();
		this.id = id;                   // Unique id of network
		this.view = view;         // View associated with network
		weights = new double[numWeights];
		// Randomly determine weights of synapses in network
		for (int i = 0; i < weights.length; i++)
			weights[i] = (double)rand.nextInt(maxWeight);
	}

	public Creature(double[] weights, int id, View view) {
		// Create network from given synapses weight
		this.weights = weights;
		this.id = id;
		this.view = view;
	}

	public int getId() {
		return id;
	}

	public double[] getWeights() {
		return weights;
	}

	public double getValue() {
		int colr = 0, colg = 0, colb = 0;
		int col = 0;
		double score = 0;
		// Determine output colors of network
		for (int j = 1; j < view.getGoal().length + 1; j++) {
			col = getEndColor(j);
			// Take value as squared sum of errors (errors are differences in output colors and expected colors)
			score += Math.pow((((col & 0xff0000)) >> 16) - (((view.getGoal()[j - 1] & 0xff0000)) >> 16), 2);
			score += Math.pow((((col & 0xff00)) >> 8) - (((view.getGoal()[j - 1] & 0xff00)) >> 8), 2);
			score += Math.pow(((col & 0xff)) - ((view.getGoal()[j - 1] & 0xff)), 2);
		}
		// return score / 2.0;
		// Score calculated as percentage of maximum difference
		return 1.0 - (score / view.getGoal().length) / 65025.0;
	}

	public int compareTo(Creature k) {
		return Double.compare(k.getValue(), getValue());
	}

	public int getEndColor(int j) {
		// Get color of j-th output node of network
		double colr = 0, colg = 0, colb = 0;
		// Color chosen as average of the connecting hidden node colors multiplied by weight (as perentage)
		// Take average of each color component (r g b) separately
		for (int i = 1; i < view.getNumMid() + 1; i++) {
			colr += weights[3 * (i - 1)]*(weights[view.getNumMid() * 3 + (j - 1) + (i - 1) * view.getGoal().length]/(view.getMaxWeight()-1));
			colg += weights[3 * (i - 1) + 1]*(weights[view.getNumMid() * 3 + (j - 1) + (i - 1) * view.getGoal().length]/(view.getMaxWeight()-1));
			colb += weights[3 * (i - 1) + 2]*(weights[view.getNumMid() * 3 + (j - 1) + (i - 1) * view.getGoal().length]/(view.getMaxWeight()-1));
		}
		colr /= view.getNumMid();
		colg /= view.getNumMid();
		colb /= view.getNumMid();
		return (((int)colr&0xff)<<16) | (((int)colg&0xff)<<8) | ((int)colb&0xff);
	}

	public int getMidColor(int i) {
		// Get color of i-th hidden node of network
		// Color of middle node determined by weights of 3 input synapses (r, g, b)
		return ((int)weights[3 * (i - 1)] << 16) | ((int)weights[3 * (i - 1) + 1] << 8) | ((int)weights[3 * (i - 1) + 2]);
	}
}
