package core;

import java.util.Random;

public class AccessSequence {
    
    private int num;
    private int[] seq;
    private int[] seq2;
    private int max;
    private int min;
    private int numNode;
    
    public AccessSequence(int numNode, int num){       
        this.num = num;
        this.seq = new int[num];
        this.seq2 = new int[numNode];
        this.max = numNode - 1;
        this.min = 0;
        this.numNode = numNode;
    }
    

    public int[] generateSequence(int type)
    {
        SelfSimilarDistribution d = new SelfSimilarDistribution();
        
        switch(type){
        case 0: 
        {
            for (int i = 0; i < num; ++i)
                seq[i] = min + (int)(Math.random() * ((max - min) + 1));           
            break;
        }
        case 1: 
        {
           for (int i = 0; i < numNode; i++)
                seq2[i] = (int) i;
            
            seq2 = shuffleArray(seq2);
            
            for (int i = 0; i < num; ++i)
                seq[i] = seq2[(int) d.SelfSimilarDistr(max, 0.1)];      
                   
            break;
        }  
        case 2: 
        {            
            for (int i = 0; i < numNode; i++)
                seq2[i] = (int) i;
            
            seq2 = shuffleArray(seq2);
            
            for (int i = 0; i < num; ++i)
                seq[i] = seq2[(int) d.SelfSimilarDistr(max, 0.2)];      
                   
            break;
        }     
        
        }
        
        return seq;
        
    }
    
    // ++++KNUTH-SHUFFLING
    public int[] shuffleArray(int[] ar)
    {
      Random rnd = new Random();
      for (int i = ar.length - 1; i > 0; i--)
      {
        int index = rnd.nextInt(i + 1);
        int a = ar[index];
        ar[index] = ar[i];
        ar[i] = a;
      }
      
      return ar;
    }  

}
