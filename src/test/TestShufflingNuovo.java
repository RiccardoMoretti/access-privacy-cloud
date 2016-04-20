package test;

import java.io.*;
import java.util.Random;

import core.AccessResult;
import core.BSTInsert;
import core.BSTNode;
import core.BST;
import core.AccessSequence;

public class TestShufflingNuovo{

    private final static int NUMACCESS  = 500000;
    private final static int NUMNODE  = 256;
    
  //diverse modalità di esecuzione (random, self80-20, ecc)
    private final static int NUMVARIOUSSEQUENCE = 3;
    
    //primi k livelli dell'albero in cui può essere spostato il target
    private final static int KLIVELLI = 2;
    
    public static void main(String[] args) throws Exception {
                  
        //sequenza dei valori di chiave acceduti 
        int[][] seqAccessMem = new int[NUMVARIOUSSEQUENCE][NUMACCESS]; 
                                       
        //utile per generare sequenze di accessi, casuali, self-similari, ecc
        AccessSequence access = new AccessSequence(NUMNODE, NUMACCESS);
        
        for(int j = 0; j < NUMVARIOUSSEQUENCE; ++j)        
        { 
            seqAccessMem[1] = access.generateSequence(1);
            
            for ( int z = 0 ; z < NUMACCESS ; z++ ) {                     
                String filenameh= "C:/Users/Riccardo Moretti/Desktop/test/SequenzaAccessi"+j+".txt";
                FileWriter fwh = new FileWriter(filenameh,true); 
                fwh.write(System.lineSeparator()+seqAccessMem[1][z]);
                fwh.close();                        
            }     
        }

    }
}
