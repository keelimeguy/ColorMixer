## ColorMixer
Testing two ways to train a neural network, using the color mixing problem.  
Notice that we are trying to train the neural network to find a solution for a specific input and output combination,  
no further sets of input and output data is used in training for these networks (just the single input/output data point).

### Running code:
run `ant` then `java -jar build/ColorMixer.jar`

### Methods:
#### Genetic Algorithm:
- Can sometimes get stuck in a local minimum, but can get out with lucky random mutation
- Usually gets very close to solution
- Better suited for more multiple output networks
- Fitness of networks always improves
- Can take a while to find a solution
- Generally the better method for this problem

#### Back Propogation:
- Easy to fall into a local minimum
- Gets close to a solution faster than GA
- Worse at solving for multiple output networks, especially very different colors
- Fitness of networks may decrease
- May never find a solution if reaches local minimum
- Quicker at solving single output problems or problems where colors are related by constant (0x222222 vs 0xbbbbbb)
