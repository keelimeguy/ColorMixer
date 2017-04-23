package ColorMixer.ai;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import ColorMixer.ColorMixer;

public class EvolverView extends View {

	private GeneticLearner learner;
	protected int generation, mutations, newChildren;
	protected double mutationRate;

	public EvolverView(int numMid, int maxWeight, int numCreatures, int[] goal, double mutationRate) {
		super(numMid, maxWeight, goal, 0);
		this.learner = new GeneticLearner(numWeights, maxWeight, numCreatures, this);
		this.mutationRate = mutationRate; // Mutation rate of genetic algorithm
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void update() {
		if (!done) {
			// Add child
			newChildren++;
			// New genertaion when we've had as many children as there is population
			if (newChildren >= learner.populationSize()) {
				generation++;
				newChildren = 0;
			}
			// Create the new child and update mutations if needed
			mutations += learner.nextChild();
		}
	}

	public void render(Graphics g, int s) {
		int w = ColorMixer.width, h = ColorMixer.height;
		// Draw input nodes
		g.setColor(new Color(0xff0000));
		g.fillOval(6 * w / 25, 1 * h / 9, h / s, h / s);
		g.setColor(new Color(0xff00));
		g.fillOval(6 * w / 25, 4 * h / 9, h / s, h / s);
		g.setColor(new Color(0xff));
		g.fillOval(6 * w / 25, 7 * h / 9, h / s, h / s);

		// Draw hidden nodes
		for (int i = 1; i < numMid + 1; i++) {
			// Draw node
			g.setColor(new Color(learner.getMidColor(i)));
			g.fillOval(11 * w / 25, ((i - 1) * numMid + 1) * h / (numMid == 1 ? 2 : (numMid * numMid)), h / s, h / s);
			// Draw first layer synapses
			g.setColor(new Color(learner.getColorWeights()[3 * (i - 1)]));
			g.drawLine(6 * w / 25 + h / (2 * s), 1 * h / 9 + h / (2 * s), 11 * w / 25 + h / (2 * s), ((i - 1) * numMid + 1) * h / (numMid == 1 ? 2 : (numMid * numMid)) + h / (2 * s));
			g.setColor(new Color(learner.getColorWeights()[3 * (i - 1) + 1]));
			g.drawLine(6 * w / 25 + h / (2 * s), 4 * h / 9 + h / (2 * s), 11 * w / 25 + h / (2 * s), ((i - 1) * numMid + 1) * h / (numMid == 1 ? 2 : (numMid * numMid)) + h / (2 * s));
			g.setColor(new Color(learner.getColorWeights()[3 * (i - 1) + 2]));
			g.drawLine(6 * w / 25 + h / (2 * s), 7 * h / 9 + h / (2 * s), 11 * w / 25 + h / (2 * s), ((i - 1) * numMid + 1) * h / (numMid == 1 ? 2 : (numMid * numMid)) + h / (2 * s));
		}

		// Draw output nodes
		for (int i = 1; i < goal.length + 1; i++) {
			// Draw goal reference around node
			g.setColor(new Color(goal[i - 1]));
			g.fillOval(16 * w / 25 + h / (2 * s) - ((h / s + 8) / 2), ((i - 1) * goal.length + 1) * h / (goal.length == 1 ? 2 : (goal.length * goal.length)) + h / (2 * s) - ((h / s + 8) / 2), h / s + 8, h / s + 8);
			// Draw node
			g.setColor(new Color(learner.getEndColor(i)));
			g.fillOval(16 * w / 25, ((i - 1) * goal.length + 1) * h / (goal.length == 1 ? 2 : (goal.length * goal.length)), h / s, h / s);
		}

		// Draw second layer synapses
		for (int i = 1; i < numMid + 1; i++) {
			for (int j = 1; j < goal.length + 1; j++) {
				g.setColor(new Color(learner.getColorWeights()[numMid * 3 + (j - 1) + (i - 1) * goal.length]));
				g.drawLine(16 * w / 25 + h / (2 * s), ((j - 1) * goal.length + 1) * h / (goal.length == 1 ? 2 : (goal.length * goal.length)) + h / (2 * s), 11 * w / 25 + h / (2 * s), ((i - 1) * numMid + 1) * h / (numMid == 1 ? 2 : (numMid * numMid)) + h / (2 * s));
			}
		}

		// Draw statistics
		g.setColor(new Color(0xffffff));
		g.setFont(new Font(Font.SERIF, 12, 12));
		g.drawString("Total population: " + learner.populationSize(), 5, h / 18);
		g.drawString("New creature: " + learner.getCur(), 5, 2 * h / 18);
		g.drawString("Fitness: " + learner.getPopulation().get(learner.getCur()).getValue(), 5, 3 * h / 18);
		g.drawString("Avg fitness: " + learner.getAvgValue(), 5, 4 * h / 18);
		g.drawString("Total reproductions: " + (newChildren + generation * learner.populationSize()), 5, 5 * h / 18);
		g.drawString("Generation: " + generation, 5, 6 * h / 18);
		g.drawString("Total mutations: " + mutations, 5, 7 * h / 18);

		if (done) {
			Creature solution = learner.getPopulation().get(learner.getCur());
			String weights = "";
			for (int i = 0; i < solution.getWeights().length; i++) {
				weights += ((i!=0)?", ":"") + (int)solution.getWeights()[i];
			}
			g.drawString("Weights: " + weights, 5, 17 * h / 18);
		}
	}
}
