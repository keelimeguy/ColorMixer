package ColorMixer.ai;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;

// Handles representing the back learning algorithm
public class BackLearner {
	private View view;
	private Creature network;
	private double learningRate;

	public BackLearner(int numWeights, int maxWeight, View view, double learningRate) {
		this.view = view;                   // Keeps track of statistics and view of network
		this.learningRate = learningRate;
		network = new Creature(numWeights, maxWeight, 0, view);   // Create random network
	}

	public Creature getNetwork() {
		return network;
	}

	public double getValue() {
		if (network == null) return -1;
		return network.getValue();
	}

	public void backPropagate() {
		// Copy weights from current weights
		double[] weights_src = network.getWeights();
		double[] weights = new double[weights_src.length];
		for (int i = 0; i < weights_src.length; i++)
			weights[i] = weights_src[i];

		for (int j = 0; j < view.getGoal().length; j++) {
			for (int i = 0; i < view.getNumMid(); i++) {
				// First update values of all synapses entering the output node
				int index = j+3*view.getNumMid() + view.getGoal().length*i;
				weights[index] -= learningRate*backPropagateHiddenError(index, j);
				if (weights[index] >= view.getMaxWeight()-1) weights[index] = view.getMaxWeight()-1;
				if (weights[index] < 0.1) weights[index] = 0.1;
			}
			// Update creature so next steps take these updates into account
			network = new Creature(weights, network.getId(), view);
			// Now update values of all synapses entering the hidden nodes
			// We divide the hidden nodes into section which have greater focus on a particular end node,
			// this helps to find a better solution while avoiding some local minima states
			int divided = (view.getNumMid()/view.getGoal().length);
			for (int i = j*divided; i < (j+1)*divided; i++)
				for (int k = 0; k < 3; k++) {
					weights[k+3*i] -= learningRate*backPropagateInputError(k+3*i, i);
					if ((int)weights[k+3*i] >= view.getMaxWeight()) weights[k+3*i] = view.getMaxWeight()-1;
					if (weights[k+3*i] < 0.1) weights[k+3*i] = 0.1;
				}
		}
		// Update the network with values from above
		network = new Creature(weights, network.getId()+1, view);
		// Finally update all synapses entering the hidden nodes, with no focus on specific outputs
		for (int i = 0; i < view.getNumMid(); i++)
			for (int k = 0; k < 3; k++) {
				weights[k+3*i] -= learningRate*backPropagateInputError(k+3*i, i);
				if ((int)weights[k+3*i] >= view.getMaxWeight()-1) weights[k+3*i] = view.getMaxWeight()-1;
				if (weights[k+3*i] < 0.1) weights[k+3*i] = 0.1;
			}

		// Update the network with final values
		network = new Creature(weights, network.getId()+1, view);
		// When we find a solution, stop the algorithm
		if(network.getValue()==1.0) view.stop();
	}

	public double backPropagateHiddenError(int i, int j) {
		// Find the error in synapses weights leaving hidden nodes using back propagation technique
		int out_j = getEndColor(j+1);
		int col_i = getMidColor((i - view.getNumMid()*3) % view.getGoal().length + 1);
		int target_j = view.getGoal()[j];
		int r_i = ((col_i & 0xff0000)>>16);
		int g_i = ((col_i & 0xff00)>>8);
		int b_i = (col_i & 0xff);
		double err = (double)((((out_j & 0xff0000) >> 16)-((target_j & 0xff0000) >> 16))*r_i*r_i + (((out_j & 0xff00) >> 8)-((target_j & 0xff00) >> 8))*g_i*g_i + ((out_j & 0xff)-(target_j & 0xff))*b_i*b_i);
		err = err/(255.0*(double)view.getNumMid());
		return err;
	}

	public double backPropagateInputError(int i, int k) {
		// Find the error in synapses weights leaving input nodes using back propagation technique
		double err = 0;
		for (int j = 0; j < view.getGoal().length; j++) {
			int out_j = getEndColor(j+1);
			int target_j = view.getGoal()[j];
			double errj = 0;
			if (i == (3 * k)) {
				errj = (double)(((out_j & 0xff0000) >> 16) - ((target_j & 0xff0000) >> 16));
			} else if (i == (3 * k + 1)) {
				errj = (((out_j & 0xff00) >> 8) - ((target_j & 0xff00) >> 8));
			} else {
				errj = ((out_j & 0xff) - (target_j & 0xff));
			}
			double w_jk = network.getWeights()[view.getNumMid() * 3 + j + (k - 1) * view.getGoal().length];
			errj = errj*w_jk/((double)view.getNumMid());
			err+=errj;
		}
		return err;
	}

	public int getMidColor(int i) {
		// Get color of i-th hidden node of network
		return network.getMidColor(i);
	}

	public int getEndColor(int j) {
		// Get color of j-th output node of network
		return network.getEndColor(j);
	}

	public int[] getColorWeights() {
		// Convert synapses weights of cur network into grayscale color for visual feedback
		double[] weightsSrc = null;
		int[] weights = null;
		weightsSrc = network.getWeights();
		weights = new int[weightsSrc.length];
		for (int i = 0; i < weightsSrc.length; i++)
			weights[i] = (int)weightsSrc[i]&0xff;
		// Convert to grayscale color (all (r g b) components equal to weight value)
		for (int i = 0; i < weights.length; i++)
			weights[i] = (0xff000000 | (weights[i] << 16) | (weights[i] << 8) | weights[i]);
		return weights;
	}
}
