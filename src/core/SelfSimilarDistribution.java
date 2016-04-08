package core;

import java.util.Random;

/* Self-similar distribution 
 * Integers between l ...N.
 * The first h*N integers get 1-h of the distribution.
 * For example: if N = 25 and h= .10, then 80% of the weight goes to the first 5 integers 
 * and 64% of the weight goes to the first integer.     
 */
    
    public class SelfSimilarDistribution {
        
    public long SelfSimilarDistr (long n, double h) {        
        return (1 + (long)(n * Math.pow(randf(),(Math.log(h)/Math.log(1-h)))));
    }
 
    public double randf()
    {
        Random r = new Random();
        return (double) 0 + (1 - 0) * r.nextDouble();
    }

 }