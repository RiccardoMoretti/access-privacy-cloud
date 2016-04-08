package core;

import java.util.Random;

public class AccessSequence {
    
    private int num;
    private int[] seq;
    private int max;
    private int min;
    
    public AccessSequence(int numNode, int num){       
        this.num = num;
        this.seq = new int[num];
        this.max = numNode - 1;
        this.min = 0;
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
            for (int i = 0; i < num; ++i)
                seq[i] = (int) d.SelfSimilarDistr(max, 0.2);           
            break;
        }  
        case 2: 
        {            
            for (int i = 0; i < num; ++i)
                seq[i] = (int) d.SelfSimilarDistr(max, 0.1);           
            break;
        }     
        case 3: 
        {                        
            for (int i = 0; i < num; ++i)
                seq[i] = (int) d.SelfSimilarDistr(max, 0.3);           
            break;
        }     
        case 4: 
        {                        
            for (int i = 0; i < num; ++i)
                seq[i] = (int) d.SelfSimilarDistr(max, 0.4);           
            break;
        }   
        case 5: 
        {                        
            for (int i = 0; i < num; ++i)
                seq[i] = (int) d.SelfSimilarDistr(max, 0.5);           
            break;
        }     
        
 
        }
        
        return seq;
        
    }

}
