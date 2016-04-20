package test;

import java.io.*;
import java.util.Random;

import core.AccessResult;
import core.BSTInsert;
import core.BSTNode;
import core.BST;
import core.AccessSequence;

public class TestAltezza{

    private final static int NUMACCESS  = 2500;
    private final static int NUMNODE  = 256;
    
    //diverse modalità di esecuzione (random, self80-20, ecc)
    private final static int NUMVARIOUSSEQUENCE = 2;
    
    //primi k livelli dell'albero in cui può essere spostato il target
    //private final static int KLIVELLI =  (int) Math.ceil(Math.log(NUMNODE) / Math.log(2))/2;
    private final static int KLIVELLI = 2;
       
    public static void main(String[] args) throws Exception {
                
        //valori di chiave presenti nell'albero
        int[] key;
   
        //sequenza dei valori di chiave acceduti 
        int[] seqAccess = new int[NUMACCESS]; 
                               
        //utile per generare sequenze di accessi, casuali, self-similari, ecc
        AccessSequence access = new AccessSequence(NUMNODE, NUMACCESS);
        
        //utili per grafici e per plottaggio risultati    
        String[] height = new String[NUMACCESS];
     
      //per diversi posizionamenti root    
        for(int j = 0; j < NUMVARIOUSSEQUENCE; ++j)
         {  
    //per diversi posizionamenti root    
    for(int kliv = 0; kliv < KLIVELLI; kliv++)
     {            
            //creazione dell'albero binario di ricerca contenente i dati 
            BST T = new BST();
            
            key = balancedTree(NUMNODE);
            
            for (int i = 0; i < NUMNODE; i++)
                  new BSTInsert(T, new BSTNode(key[i])).insert();
                                
            T.getRoot().calcTree(); 
                                                                      
            //generazione sequenza d'accesso in base al parametro (rand, 80-20, ecc)            
            seqAccess = access.generateSequence(j);
            
            for (int i = 0; i < NUMACCESS; i++) { 
                                             
                AccessResult accres = T.access(T, NUMNODE, seqAccess[i], kliv);
                
                //memorizzo altezza 
                height[i] = Integer.toString(T.getRoot().height);
                       
            }
            
            for ( int z = 0 ; z < NUMACCESS ; z++ ) {                     
                String filenameh= "C:/Users/Riccardo Moretti/Desktop/test/Altezze"+"Parameto0"+j+"K"+kliv+".txt";
                FileWriter fwh = new FileWriter(filenameh,true); 
                fwh.write(System.lineSeparator()+height[z]);
                fwh.close();                        
            }            
         }    
      }
  }       
 

    public static void shuffleArray(int[] ar)
    {
      Random rnd = new Random();
      for (int i = ar.length - 1; i > 0; i--)
      {
        int index = rnd.nextInt(i + 1);
        int a = ar[index];
        ar[index] = ar[i];
        ar[i] = a;
      }
    }     
    
    public static int[] balancedTree(int numnode)
    {
        if(numnode == 16)
            {   return new int[]{7, 3, 1, 0, 2, 5, 4, 6, 11, 9, 8, 10, 13, 12, 14, 15};  }
        else if(numnode == 32)
            {   return new int[]{15, 7, 3, 1, 0, 2, 5, 4, 6, 11, 9, 8, 10, 13, 12, 14, 23, 19, 17, 16, 18, 21, 20, 22, 27, 25, 24, 26, 29, 28, 30, 31};}
        else if(numnode == 64)
            {   return new int[]{31, 15, 7, 3, 1, 0, 2, 5, 4, 6, 11, 9, 8, 10, 13, 12, 14, 23, 19, 17, 16, 18, 21, 20, 22, 27, 25, 24, 26, 29, 28, 30, 47, 39, 35, 33, 32, 34, 37, 36, 38, 43, 41, 40, 42, 45, 44, 46, 55, 51, 49, 48, 50, 53, 52, 54, 59, 57, 56, 58, 61, 60, 62, 63}; }
        else if(numnode == 128)
            {   return new int[]{63, 31, 15, 7, 3, 1, 0, 2, 5, 4, 6, 11, 9, 8, 10, 13, 12, 14, 23, 19, 17, 16, 18, 21, 20, 22, 27, 25, 24, 26, 29, 28, 30, 47, 39, 35, 33, 32, 34, 37, 36, 38, 43, 41, 40, 42, 45, 44, 46, 55, 51, 49, 48, 50, 53, 52, 54, 59, 57, 56, 58, 61, 60, 62, 95, 79, 71, 67, 65, 64, 66, 69, 68, 70, 75, 73, 72, 74, 77, 76, 78, 87, 83, 81, 80, 82, 85, 84, 86, 91, 89, 88, 90, 93, 92, 94, 111, 103, 99, 97, 96, 98, 101, 100, 102, 107, 105, 104, 106, 109, 108, 110, 119, 115, 113, 112, 114, 117, 116, 118, 123, 121, 120, 122, 125, 124, 126, 127}; }
        else if(numnode == 256)
            {   return new int[]{127, 63, 31, 15, 7, 3, 1, 0, 2, 5, 4, 6, 11, 9, 8, 10, 13, 12, 14, 23, 19, 17, 16, 18, 21, 20, 22, 27, 25, 24, 26, 29, 28, 30, 47, 39, 35, 33, 32, 34, 37, 36, 38, 43, 41, 40, 42, 45, 44, 46, 55, 51, 49, 48, 50, 53, 52, 54, 59, 57, 56, 58, 61, 60, 62, 95, 79, 71, 67, 65, 64, 66, 69, 68, 70, 75, 73, 72, 74, 77, 76, 78, 87, 83, 81, 80, 82, 85, 84, 86, 91, 89, 88, 90, 93, 92, 94, 111, 103, 99, 97, 96, 98, 101, 100, 102, 107, 105, 104, 106, 109, 108, 110, 119, 115, 113, 112, 114, 117, 116, 118, 123, 121, 120, 122, 125, 124, 126, 191, 159, 143, 135, 131, 129, 128, 130, 133, 132, 134, 139, 137, 136, 138, 141, 140, 142, 151, 147, 145, 144, 146, 149, 148, 150, 155, 153, 152, 154, 157, 156, 158, 175, 167, 163, 161, 160, 162, 165, 164, 166, 171, 169, 168, 170, 173, 172, 174, 183, 179, 177, 176, 178, 181, 180, 182, 187, 185, 184, 186, 189, 188, 190, 223, 207, 199, 195, 193, 192, 194, 197, 196, 198, 203, 201, 200, 202, 205, 204, 206, 215, 211, 209, 208, 210, 213, 212, 214, 219, 217, 216, 218, 221, 220, 222, 239, 231, 227, 225, 224, 226, 229, 228, 230, 235, 233, 232, 234, 237, 236, 238, 247, 243, 241, 240, 242, 245, 244, 246, 251, 249, 248, 250, 253, 252, 254, 255};  }
        
        int[] rand = new int[numnode];        
        for (int i = 0; i < numnode; ++i)
            rand[i] = i;      
        shuffleArray(rand);
        
        return rand;
    }
}
