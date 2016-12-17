package misc;

import java.util.Map.Entry;

/**
 * Implements common discrete Mutual Information functions.
 * Provides: Mutual Information I(X;Y),
 *           Conditional Mutual Information I(X,Y|Z).
 * Defaults to log_2, and so the entropy is calculated in bits.
 * @author apocock
 */
public abstract class MutualInformation
{
  private MutualInformation() {}

  /**
   * Calculates the Mutual Information I(X;Y) between two random variables.
   * Uses histograms to estimate the probability distributions, and thus the information.
   * The mutual information is bounded 0 &#8804; I(X;Y) &#8804; min(H(X),H(Y)). It is also symmetric,
   * so I(X;Y) = I(Y;X).
   *
   * @param  firstVector  Input vector (X). It is discretised to the floor of each value before calculation.
   * @param  secondVector  Input vector (Y). It is discretised to the floor of each value before calculation.
   * @return The Mutual Information I(X;Y).
   */
  public strictfp static double calculateMutualInformation(double[] firstVector, double[] secondVector)
  {
    JointProbabilityState state = new JointProbabilityState(firstVector,secondVector);

    double jointValue, firstValue, secondValue;

    double mutualInformation = 0.0;
    for (Entry<Pair<Integer,Integer>,Double> e : state.jointProbMap.entrySet())
    {
      jointValue = e.getValue();
      firstValue = state.firstProbMap.get(e.getKey().a);
      secondValue = state.secondProbMap.get(e.getKey().b);

      if ((jointValue > 0) && (firstValue > 0) && (secondValue > 0))
      {
        mutualInformation += jointValue * Math.log((jointValue / firstValue) / secondValue);
      }
    }

    mutualInformation /= Math.log(Entropy.LOG_BASE);
    
    return mutualInformation; 
  }//calculateMutualInformation(double [], double [])
  
  /**
   * Calculates the conditional Mutual Information I(X;Y|Z) between two random variables, conditioned on
   * a third.
   * Uses histograms to estimate the probability distributions, and thus the information.
   * The conditional mutual information is bounded 0 &#8804; I(X;Y) &#8804; min(H(X|Z),H(Y|Z)). 
   * It is also symmetric, so I(X;Y|Z) = I(Y;X|Z).
   *
   * @param  firstVector  Input vector (X). It is discretised to the floor of each value before calculation.
   * @param  secondVector  Input vector (Y). It is discretised to the floor of each value before calculation.
   * @param  conditionVector  Input vector (Z). It is discretised to the floor of each value before calculation.
   * @return The conditional Mutual Information I(X;Y|Z).
   */
  public static double calculateConditionalMutualInformation
      (double[] firstVector, double[] secondVector, double[] conditionVector)
  {
    //first create the vector to hold *outputVector
    double[] mergedVector = new double[firstVector.length];
    
    ProbabilityState.mergeArrays(firstVector,conditionVector,mergedVector);
    
    double firstCondEnt = Entropy.calculateConditionalEntropy(secondVector, conditionVector);
    double secondCondEnt = Entropy.calculateConditionalEntropy(secondVector, mergedVector);
    
    double answer = firstCondEnt - secondCondEnt;
    
    return answer; 
  }//calculateConditionalMutualInformation(double [], double [], double [])
  
  public static void main(String[] args) {
	  double[] vector1 = {0.1, 0.2, 0.3};
	  double[] vector2 = {0.1, 0.1, 0.1};
	  
	  //currently the score is 0.0 if the vectors are similar
	  System.out.println("The MI is " + (1-calculateMutualInformation(vector1, vector2)));
  }
  
}//class MutualInformation
