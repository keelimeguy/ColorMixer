package ColorMixer.ai;

import java.util.ArrayList;
import java.util.Random;

public class GeneticLearner {
	private EvolverView evolver;
	private int cur;

	private ArrayList<Creature> population;

	public GeneticLearner(int numWeights, int maxWeight, int numCreatures, EvolverView evolver) {
		this.evolver = evolver;                   // Keeps track of statistics and view of network
		cur = -1;                                 // Index of the recent network added to the population
		population = new ArrayList<Creature>();   // List of all networks in our population
		// Populate with random networks
		for (int i = 0; i < numCreatures; i++) {
			population.add(new Creature(numWeights, maxWeight, i, evolver));
		}
	}

	public ArrayList<Creature> getPopulation() {
		return population;
	}

	public int populationSize() {
		return population.size();
	}

	public double getAvgValue() {
		double ret = 0;
		// Find the average value of all networks
		for (Creature creature : population)
			ret += creature.getValue();
		ret /= population.size();
		return ret;
	}

	public int nextChild() {
		Random rand = new Random();
		int m = 0;
		// If 2 or less in population, reproduce by mutation only
		if (population.size() < 3) {
			Creature c = population.remove(rand.nextInt(population.size()));
			double[] weights = c.getWeights();
			for (int i = 0; i < weights.length; i++) {
				// Mutate according to mutation rate
				if (rand.nextDouble() < evolver.getMutationRate()) {
					weights[i] = (double)rand.nextInt(evolver.getMaxWeight());
					m++;
					continue;
				}
			}
			population.add(c);
			cur = c.getId(); // Set current view to network we just added
			return m;
		}
		Creature[] c = new Creature[3];
		// Randomly select 3 networks from population to reproduce
		for (int i = 0; i < 3; i++)
			c[i] = population.remove(rand.nextInt(population.size()));
		int max = 0;
		// Choose the network with the maximum value (the worst network)
		for (int i = 1; i < 3; i++) {
			if (c[max].compareTo(c[i]) < 0) max = i;
		}
		// Add other networks back to population
		for (int i = 0; i < 3; i++)
			if (max != i) population.add(c[i]);

		// Randomly create child from chosen networks
		double[] weights = new double[population.get(0).getWeights().length];
		for (int i = 0; i < weights.length; i++) {
			// Mutate according to mutation rate
			if (rand.nextDouble() < evolver.getMutationRate()) {
				weights[i] = (double)rand.nextInt(evolver.getMaxWeight());
				m++;
				continue;
			}
			// Randomly choose one of other networks to replace value for max network
			int gene = rand.nextInt(2);
			if (max == 0) weights[i] = c[gene + 1].getWeights()[i];
			if (max == 1) weights[i] = (gene == 0) ? c[0].getWeights()[i] : c[2].getWeights()[i];
			if (max == 2) weights[i] = c[gene].getWeights()[i];
		}
		population.add(new Creature(weights, c[max].getId(), evolver));
		cur = c[max].getId(); // Set current view to network we just added
		if (c[max].getValue() == 1.0) evolver.stop();
		return m;
	}

	public int getCur() {
		if (cur != -1) return cur;
		return 0;
	}

	public int getMidColor(int i) {
		if (population.isEmpty()) return 0;
		// Get color of i-th hidden node of cur network
		// If cur not set, just choose last member
		if (cur == -1)
			return population.get(population.size() - 1).getMidColor(i);
		else
			return population.get(cur).getMidColor(i);
	}

	public int getEndColor(int j) {
		if (population.isEmpty()) return 0;
		// Get color of j-th output node of cur network
		// If cur not set, just choose last member
		if (cur == -1)
			return population.get(population.size() - 1).getEndColor(j);
		else
			return population.get(cur).getEndColor(j);
	}

	public int[] getColorWeights() {
		// Convert synapses weights of cur network into grayscale color for visual feedback
		if (population.isEmpty()) return null;
		double[] weightsSrc = null;
		int[] weights = null;
		// Take average weights of population if cur not set
		if (cur == -1) {
			weights = getAvgWeights();
		} else {
			weightsSrc = population.get(cur).getWeights();
			weights = new int[weightsSrc.length];
			for (int i = 0; i < weightsSrc.length; i++)
				weights[i] = (int)weightsSrc[i];
		}
		// Convert to grayscale color (all (r g b) components equal to weight value)
		for (int i = 0; i < weights.length; i++)
			weights[i] = 0xff000000 | ((weights[i] << 16)) | ((weights[i] << 8)) | ((weights[i]));
		return weights;
	}

	public int[] getAvgWeights() {
		// Find average weights (component wise) of population
		if (population.isEmpty()) return null;
		int[] weights = new int[population.get(0).getWeights().length];
		for (Creature creature : population)
			for (int i = 0; i < weights.length; i++)
				weights[i] += (int)creature.getWeights()[i];
		for (int i = 0; i < weights.length; i++)
			weights[i] /= population.size();
		return weights;
	}
}
