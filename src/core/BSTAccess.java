package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import core.ContinuePathResult;
import core.AccessResult;

public class BSTAccess{

private BST T;
private BST B;

private BSTNode[] localNode;

private BSTNode target;
private BSTNode w;
private BSTNode s;
private BSTNode s2;
private BSTNode s3;

private double numBlock;

private int root;
private int localCont;
private int nBlock;
private int nodeToTarget;
private int K;
private int dir;
private int numSpare;
private int camT;
private int level;
private int altezza;

private int[] phisical;
private int[] logical;
private int[] tempPhi; 

private String path;
private String pathT;

private boolean done;
private boolean skip;

private ReadSpareResult ris;

public AccessResult BSTAccessRun(BST T, int numberOfNode, int key, int level) throws IOException {
    //albero binario di ricerca contenente i dati
    this.T = T;
    this.B = T;
    
    //chiave del nodo target
    this.K = key;
    
    //numero di blocchi acceduti per accesso (hradice + radice stessa + secondo figlio radice)
    this.numBlock = (2* Math.ceil(Math.log(numberOfNode) / Math.log(2)))+2;    
    this.nBlock = (int) numBlock;
    
    //numero di nodi memorizzati localamente dal client (sarebbero numBlock ma siccome ogni tanto l'albero si sbilancia può essere necessario leggerne di piu)
    this.localNode = new BSTNode[(int)(numBlock*2)];
    
    //inizializzazione (poi puo cambiare) del numero degli spare (almeno)
    this.numSpare = nBlock - (T.getRoot().height)-1;
    
    //contatori accesso dati logici(chiavi) e indirizzi fisici
    this.logical = new int[numberOfNode];
    this.phisical = new int[numberOfNode];
     
    //memorizzazione percorso verso il target
    this.path = ""; 
    this.pathT = "";
 
    //altre variabili utili per l'applicazione della strategia
    this.camT = T.getRoot().height;
    this.dir = 0;
    this.nodeToTarget = 0;
    this.localCont = 0;
    this.level = level;
    this.done = false;
    this.skip = false;
    this.altezza = T.getRoot().height;
         
    //inizio accesso, parto leggendo la root

        //w contiene il nodo del cammino verso il target attualmente in esame/considerato
        w = T.getRoot();
        s = T.getRoot();
        s2 = T.getRoot(); 
        s3 = T.getRoot();
       
        //aggiungo la root ai nodi locali e decremento il numero di blocchi in quanto ho già letto un blocco (la root)
        localNode[localCont] = w;
        localCont++;
        nBlock--;
                
        //porto sempre a casa i due figli della radice               
        //la radice ha due figli
     if (w.getLeft() != null && w.getRight() != null)
      {    
         
        //il target è nella radice stessa
        if(w.getKey() == K)
        { 
            done = true;
            target = w;
            pathT = path;
            camT = w.height;
            
            if(Math.random() < 0.5)//vado a destra
            {             
                
                if(w.getRight().height != (camT - 1))                    
                    numSpare =  numSpare + (camT - (w.getRight().height))-1;
                
                camT = w.getRight().height;
                               
                s = w.getLeft(); s2 = s; s3 = s;
                localNode[localCont] = s;
                localCont++;
                numSpare--;
                nBlock--;
                
                w = w.getRight();
                localNode[localCont] = w;
                localCont++;
                nBlock--;
                path = path.concat("1");
              
            }
            
            else //vado a sinistra
            {
                if(w.getLeft().height != (camT - 1))                    
                    numSpare =  numSpare + (camT - (w.getLeft().height))-1;
                
                camT = w.getLeft().height;
                
                s = w.getRight(); s2 = s; s3 = s; 
                localNode[localCont] = s;
                localCont++;
                numSpare--;
                nBlock--; 
                                 
                w = w.getLeft();
                localNode[localCont] = w;
                localCont++;
                nBlock--;
                path = path.concat("0");
                
            }
            
            //continuo il cammino fino a quando  non arrivo ad un nodo foglia 
            ContinuePathResult res = continuePath(w, s, s2, s3, path, numSpare, localNode, localCont, camT, nBlock, dir);
            
            w = res.target; s = res.s; path = res.path; numSpare = res.numSpare; localNode = res.localNode;localCont = res.localCont; 
            camT = res.camT; nBlock = res.nBlock; s2 = res.b; s3 = res.c;
           
            //sono arrivato in fondo al cammino del target ma ho ancora dei nodi spare da leggere
            while(numSpare > 0)
            {
               
                ris = readSpareAfterT(s, s2, s3, 0, nBlock);
                
                //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                    s = ris.a; s2 = s; s3 = s;
                    numSpare--;
                    nBlock--;
                    localNode[localCont] = s;
                    localCont++;
                   
                    //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                    if(ris.b != null)
                    {   
                        s2 = ris.b;
                        numSpare--;
                        nBlock--;
                        localNode[localCont] = s2;
                        localCont++;                                
                    }
                    
                    if(ris.c != null)
                    {   
                        s3 = ris.c;
                        numSpare--;
                        nBlock--;
                        localNode[localCont] = s3;
                        localCont++;                                
                    }
                    
            }             
                
                 
            T = moveUpTarget(T,target,level);

           
            //+++++parte nuova
          /*  
            T.getRoot().calcTree();
            
            //controllo se il sw si è comportato bene 
            for (int i = 0; i < numBlock; ++i)
                if(localNode[i].key < 0)        
                    skip= true;        
               
            for (int i = 0; i < numBlock; ++i)
                for (int j = i+1; j < numBlock; ++j)           
                    if(localNode[i].key == localNode[j].key)        
                        skip= true;        
                     
            if(skip)                  
                T = B;       
             
            //se il se si è comportato bene
            if(!skip && localCont == numBlock)
            {               
                while(T.getRoot().height > (2* Math.ceil(Math.log(numberOfNode) / Math.log(2)))) 
                {
                   T = balanceTree(T, localNode,(int) numBlock, K);
                   T.getRoot().calcTree();
                }                
            }*/
            
            //+++++++fine parte nuova 
           
           T = balanceTree(T, localNode,(int) numBlock, K); 
            
        }
        
        // la root ha due figli ed il target è a destra
        else if(w.getKey() < K)
        {
            
            if(w.getRight().height != (camT - 1))                    
                numSpare =  numSpare + (camT - (w.getRight().height))-1;
            
            camT = w.getRight().height;
 
            s = w.getLeft(); s2 = s; s3 = s;
            localNode[localCont] = s;
            localCont++;
            numSpare--;
            nBlock--;  
            
            w = w.getRight();
            localNode[localCont] = w;
            localCont++;
            nBlock--;
            path = path.concat("1");
            nodeToTarget++;
 
        }
                
        //la root ha due figli ed il target è a sinistra
        else if(w.getKey() > K)
        {
            
            if(w.getLeft().height != (camT - 1))                    
                numSpare =  numSpare + (camT - (w.getLeft().height))-1;
            
            camT = w.getLeft().height;
            
            s = w.getRight(); s2 = s; s3 = s;
            localNode[localCont] = s;
            localCont++;
            numSpare--;
            nBlock--; 
            
            w = w.getLeft();
            localNode[localCont] = w;
            localCont++;
            nBlock--;
            path = path.concat("0");
            nodeToTarget++;
        }
        
        } //la radice ha un solo figlio 
        else if(w.getKey() == K) //il target è nella radice (che ha un solo figlio)
        { 
            done = true;
            target = w;
            pathT = path;
            
            if(w.getLeft() != null)//solo figlio sinistro
            { 
                
                camT = w.getLeft().height;
                
                s = w.getLeft(); s2 = s; s3 = s;
                w = w.getLeft();
                localNode[localCont] = w;
                localCont++;
                nBlock--;
                path = path.concat("0");
                nodeToTarget++;
                
            }
            else //la radice ha solo il figlio destro
            {
                
                camT = w.getRight().height;
               
                s = w.getRight(); s2 = s; s3 = s;
                w = w.getRight();
                localNode[localCont] = w;
                localCont++;
                nBlock--;
                path = path.concat("1");
                nodeToTarget++;
                      
            }
            
            //continuo a camminare fino a quando non ho raggiunto un nodo foglia
            ContinuePathResult res = continuePath(w, s, s2, s3, path, numSpare, localNode, localCont, camT, nBlock, dir);
           
            w = res.target; s = res.s; path = res.path; numSpare = res.numSpare; localNode = res.localNode;localCont = res.localCont; 
            camT = res.camT; nBlock = res.nBlock; s2 = res.b; s3 = res.c;

            //ho finito il cammino del target ma ho ancora dei nodi spare da leggere
            while(numSpare > 0)
            {
               
                ris = readSpareAfterT(s, s2, s3, 0, nBlock);
                
                //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                    s = ris.a; s2 = s; s3 = s;
                    numSpare--;
                    nBlock--;
                    localNode[localCont] = s;
                    localCont++;
                    
                    //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                    if(ris.b != null)
                    {   
                        s2 = ris.b;
                        numSpare--;
                        nBlock--;
                        localNode[localCont] = s2;
                        localCont++;                                
                    }
                    
                    if(ris.c != null)
                    {   
                        s3 = ris.c;
                        numSpare--;
                        nBlock--;
                        localNode[localCont] = s3;
                        localCont++;                                
                    }
                    
            }           
                
                 
            T = moveUpTarget(T,target,level);

            //+++++parte nuova
            /*
            T.getRoot().calcTree();
            
            //controllo se il sw si è comportato bene 
            for (int i = 0; i < numBlock; ++i)
                if(localNode[i].key < 0)        
                    skip= true;        
               
            for (int i = 0; i < numBlock; ++i)
                for (int j = i+1; j < numBlock; ++j)           
                    if(localNode[i].key == localNode[j].key)        
                        skip= true;        
                     
            if(skip)                  
                T = B;       
             
            //se il se si è comportato bene
            if(!skip && localCont == numBlock)
            {               
                while(T.getRoot().height > (2* Math.ceil(Math.log(numberOfNode) / Math.log(2)))) 
                {
                   T = balanceTree(T, localNode,(int) numBlock, K);
                   T.getRoot().calcTree();
                }                
            }
            */
            //+++++++fine parte nuova
            
            T = balanceTree(T, localNode,(int) numBlock, K);
            
        }
        
    //ora inizio la ricerca normale, mi sposto a destra o a sinistra nell'albero fino a quando non trovo il nodo con key = k (nodo target)
    while (!done) {
                    
            if (w.getKey() == K) { //trovato il target, w è il mio target
                            
                done = true;                                                     
                pathT = path;
                target = w;
                                                               
                //se il nodo target non è una foglia, percorro un cammino casuale fino ad arrivare ad una foglia
                ContinuePathResult res = continuePath(w, s, s2, s3, path, numSpare, localNode, localCont, camT, nBlock, dir);
                     
                w = res.target;s = res.s;path = res.path; numSpare = res.numSpare; localNode = res.localNode;localCont = res.localCont; 
                camT = res.camT; nBlock = res.nBlock; s2 = res.b; s3 = res.c;

                //ho finito di percorrere il cammino del target(sono arrivato ad una foglia) ma ho ancora dei nodi spare da leggere
              
                while(numSpare > 0)
                {
                   
                    ris = readSpareAfterT(s, s2, s3, 0, nBlock);
                    
                    //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                        s = ris.a; s2 = s; s3 = s;
                        numSpare--;
                        nBlock--;
                        localNode[localCont] = s;
                        localCont++;
                        
                        //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                        if(ris.b != null)
                        {   
                            s2 = ris.b;
                            numSpare--;
                            nBlock--;
                            localNode[localCont] = s2;
                            localCont++;                                
                        }
                        
                        if(ris.c != null)
                        {   
                            s3 = ris.c;
                            numSpare--;
                            nBlock--;
                            localNode[localCont] = s3;
                            localCont++;                                
                        }
                        
                }           
                
                                  
                //ruoto e porto in alto il target
               T = moveUpTarget(T,target,level);
                                
                //+++++parte nuova
                /*
                T.getRoot().calcTree();
                
                //controllo se il sw si è comportato bene 
                for (int i = 0; i < numBlock; ++i)
                    if(localNode[i].key < 0)        
                        skip= true;        
                  
                for (int i = 0; i < numBlock; ++i)
                    for (int j = i+1; j < numBlock; ++j)           
                        if(localNode[i].key == localNode[j].key)        
                            skip= true;        
                         
                if(skip)                  
                    T = B;       
                 
                //se il se si è comportato bene
                if(!skip && localCont == numBlock)
                {               
                    while(T.getRoot().height > (2* Math.ceil(Math.log(numberOfNode) / Math.log(2)))) 
                    {
                       T = balanceTree(T, localNode,(int) numBlock, K);
                       T.getRoot().calcTree();
                    }                
                }*/
                
                T = balanceTree(T, localNode,(int) numBlock, K);
                
                //+++++++fine parte nuova
                                                
            //il target è a destra del nodo w che sto considerando ora, cammino proseguendo verso destra
            } else if (w.getKey() < K) {
                                
                //proseguo camminando verso destra 
                    w = w.getRight(); 
                    path = path.concat("1");
                    nodeToTarget++;
                    localNode[localCont] = w;
                    localCont++;
                    nBlock--;
                    
                    dir = 1;
                                        
                    if(w.height != (camT - 1))                    
                        numSpare =  numSpare + (camT - (w.height))-1;
                                                           
                    camT = w.height;

                    
                    //se posso leggo uno spare
                    if(numSpare > 0)
                    {   
                        ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
                        
                        //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                        if(ris.a.key != -1) //
                        {
                            s = ris.a; s2 = s; s3 = s;
                            numSpare--;
                            nBlock--;
                            localNode[localCont] = s;
                            localCont++;
                            
                            //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                            if(ris.b != null)
                            {   
                                s2 = ris.b;
                                numSpare--;
                                nBlock--;
                                localNode[localCont] = s2;
                                localCont++;                                
                            }
                            
                            if(ris.c != null)
                            {   
                                s3 = ris.c;
                                numSpare--;
                                nBlock--;
                                localNode[localCont] = s3;
                                localCont++;                                
                            }
                                                                             
                        }
                        
                        else //se c'è solo un nodo al livello successivo    
                            {s = w; s2 = s; s3 = s;}    
                    }             
            }                  
                        
            //il target è a sinistra del nodo w che sto considerando ora, cammino proseguendo verso sinistra
            else {        
               
             //proseguo camminando verso sinistra 
                   w = w.getLeft();
                   path = path.concat("0");
                   nodeToTarget++;
                   localNode[localCont] = w;
                   localCont++;
                   nBlock--;
                   dir = -1;
     
                    if(w.height != (camT - 1))                    
                        numSpare =  numSpare + (camT - (w.height))-1;
                                                           
                     camT = w.height;
                       
                     //se posso leggo uno spare
                     if(numSpare > 0)
                     {   
                         ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
                         
                         //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                         if(ris.a.key != -1) //
                         {
                             s = ris.a; s2 = s; s3 = s;
                             numSpare--;
                             nBlock--;
                             localNode[localCont] = s;
                             localCont++;
                             
                             //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                             if(ris.b != null)
                             {   
                                 s2 = ris.b;
                                 numSpare--;
                                 nBlock--;
                                 localNode[localCont] = s2;
                                 localCont++;                                
                             }
                             
                             if(ris.c != null)
                             {   
                                 s3 = ris.c;
                                 numSpare--;
                                 nBlock--;
                                 localNode[localCont] = s3;
                                 localCont++;                                
                             }
                                                  
                         }
                         
                         else //se c'è solo un nodo al livello successivo    
                             {s = w; s2 = s; s3 = s;}    
                     }
            }                     
      } //fine while 
   
    
  /*  
    for (int i = 0; i < localCont; i++)
    {  if(localNode[i].key < 0)        
    {
        String filenameh= "C:/Users/Riccardo Moretti/Desktop/test/StampaKeyNonOK.txt";
        FileWriter fwh = new FileWriter(filenameh,true); 
        fwh.write(System.lineSeparator()+localNode[i].key);
        fwh.close();    
    }
    }*/
            
            
    //SHUFFLING LOGICO FISICO
    boolean skip = false;
    
    
    //controllo se il sw si è comportato bene 
    for (int i = 0; i < numBlock; ++i)
        if(localNode[i].key < 0)        
            skip= true;        
       
    for (int i = 0; i < numBlock; ++i)
        for (int j = i+1; j < numBlock; ++j)           
            if(localNode[i].key == localNode[j].key)        
                skip= true;        
    
    
    if(skip)
    {   
        //se mi sono comportato male, ripristino l'albero come prima dell'accesso e lo ruoto (per far cambiare un po le cose)
        T = B;       
        T = balanceTree(T, localNode,(int) numBlock, K);
     /*   
        String filenameh= "C:/Users/Riccardo Moretti/Desktop/test/ConteggioCasiNonOK.txt";
        FileWriter fwh = new FileWriter(filenameh,true); 
        fwh.write(System.lineSeparator()+1);
        fwh.close(); */
    }
     
    //se il se si è comportato bene
    if(!skip && localCont == numBlock)
    {     
        this.tempPhi = new int[(int)numBlock];
        
        //conto i nodi logici e i blocchi fisici che ho letto
        for (int i = 0; i < numBlock; ++i)
            { 
                logical[localNode[i].key]++;
                phisical[localNode[i].phiAdd]++;
                tempPhi[i] = localNode[i].phiAdd;
             }
   
        //permutazione casuale dei blocchi fisici (KNUTH-SHUFFLING)
        tempPhi = shuffleArray(tempPhi);
    
        for (BSTNode node : T.getRoot().postorder()) {
            for (int i = 0; i < numBlock; ++i)
                if(node.key == localNode[i].key)
                    node.phiAdd = tempPhi[i];
        }
   
   }
        
    T.getRoot().calcTree(); 
    
    //fine esecuzione, ritorno i dati di interesse dell'accesso
    return new AccessResult(T, logical, phisical, path);
          
    } 
     


private ContinuePathResult continuePath(BSTNode w, BSTNode s, BSTNode s2, BSTNode s3, String path, int numSpare, BSTNode[] localNode, int localCont, int camT, int nBlock, int dir)
{    
    
  //il nodo target in questo caso è un nodo interno, dopo averlo letto viene percorso un cammino casuale fino a raggiungere  un nodo foglia  
    
  //w è il nodo target
  while(!w.isLeaf())
  {
      //il target ha un solo figlio (sinistro), scelta obbligata! Cammino verso sinistra
      if(w.getRight() == null)
      {                                
           w = w.getLeft();
           localNode[localCont] = w;
           localCont++; 
           path = path.concat("0");
           nBlock--;
           
           if(w.height != (camT - 1))                    
               numSpare =  numSpare + (camT - (w.height))-1;
                                                  
           camT = w.height;
            
           dir = -1;
                              
         
           //se posso leggo uno spare
           if(numSpare > 0)
           {   
               ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
               
               //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
               if(ris.a.key != -1) //
               {
                   s = ris.a; s2 = s; s3 = s;
                   numSpare--;
                   nBlock--;
                   localNode[localCont] = s;
                   localCont++;
                   
                   //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                   if(ris.b != null)
                   {   
                       s2 = ris.b;
                       numSpare--;
                       nBlock--;
                       localNode[localCont] = s2;
                       localCont++;                                
                   }
                   
                   if(ris.c != null)
                   {   
                       s3 = ris.c;
                       numSpare--;
                       nBlock--;
                       localNode[localCont] = s3;
                       localCont++;                                
                   }
                                   
               }
               
               else //se c'è solo un nodo al livello successivo    
                   {s = w; s2 = s; s3 = s;}    
           }
         
       }
    
      //il target ha un solo figlio (destro), scelta obbligata! Cammino verso destra
      else if (w.getLeft() == null)
          {                           
              w = w.getRight();
              localNode[localCont] = w;
              localCont++;
              path = path.concat("1");              
              nBlock--;
 
              if(w.height != (camT - 1))                    
                 numSpare =  numSpare + (camT - (w.height))-1;
                                                    
              camT = w.height;
              
              dir = 1;

              //se posso leggo uno spare
              if(numSpare > 0)
              {   
                  ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
                  
                  //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                  if(ris.a.key != -1) //
                  {
                      s = ris.a; s2 = s; s3 = s;
                      numSpare--;
                      nBlock--;
                      localNode[localCont] = s;
                      localCont++;
                      
                      //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                      if(ris.b != null)
                      {   
                          s2 = ris.b;
                          numSpare--;
                          nBlock--;
                          localNode[localCont] = s2;
                          localCont++;                                
                      }
                      
                      if(ris.c != null)
                      {   
                          s3 = ris.c;
                          numSpare--;
                          nBlock--;
                          localNode[localCont] = s3;
                          localCont++;                                
                      }                                          
                  }
                  
                  else //se c'è solo un nodo al livello successivo    
                      {s = w; s2 = s; s3 = s;}    
              }
          }
      //il target ha due figli, scelgo casualmente da che parte andare
      else if(Math.random() < 0.5)
              {            
                      w = w.getRight(); 
                      localNode[localCont] = w;
                      localCont++;
                      path = path.concat("1");
                      nBlock--;
           
                      dir = 1;
                  
                      if(w.height != (camT - 1))                    
                          numSpare =  numSpare + (camT - (w.height))-1;
                                                         
                      camT = w.height;
                   
                      //se posso leggo uno spare
                      if(numSpare > 0)
                      {   
                          ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
                          
                          //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                          if(ris.a.key != -1) //
                          {
                              s = ris.a; s2 = s; s3 = s;
                              numSpare--;
                              nBlock--;
                              localNode[localCont] = s;
                              localCont++;
                              
                              //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                              if(ris.b != null)
                              {   
                                  s2 = ris.b;
                                  numSpare--;
                                  nBlock--;
                                  localNode[localCont] = s2;
                                  localCont++;                                
                              }
                              
                              if(ris.c != null)
                              {   
                                  s3 = ris.c;
                                  numSpare--;
                                  nBlock--;
                                  localNode[localCont] = s3;
                                  localCont++;                                
                              }                     
                          }
                          
                          else //se c'è solo un nodo al livello successivo    
                              {s = w; s2 = s; s3 = s;}    
                      }
               }   
      
            else
              { 
                 w = w.getLeft();
                 localNode[localCont] = w;
                 localCont++;
                 path = path.concat("0");
                 nBlock--;
                 
                 dir = -1;
                                  
                 if(w.height != (camT - 1))                    
                     numSpare =  numSpare + (camT - (w.height))-1;
                                                        
                 camT = w.height;

                 //se posso leggo uno spare
                 if(numSpare > 0)
                 {   
                     ris = readSpare(w.getParent(), s, s2, s3, dir, camT, nBlock);
                     
                     //se al livello successivo c'è almeno un nodo da leggere oltre a quello verso il target
                     if(ris.a.key != -1) //
                     {
                         s = ris.a; s2 = s; s3 = s;
                         numSpare--;
                         nBlock--;
                         localNode[localCont] = s;
                         localCont++;
                         
                         //se devo leggere due nodi di questo livello (il cammino verso il target si è accorciatoe i nodi rimanenti non mi bastano)
                         if(ris.b != null)
                         {   
                             s2 = ris.b;
                             numSpare--;
                             nBlock--;
                             localNode[localCont] = s2;
                             localCont++;                                
                         }
                         
                         if(ris.c != null)
                         {   
                             s3 = ris.c;
                             numSpare--;
                             nBlock--;
                             localNode[localCont] = s3;
                             localCont++;                                
                         }                      
                     }
                     
                     else //se c'è solo un nodo al livello successivo    
                         {s = w; s2 = s; s3 = s;}    
                 }
              }     
           
  }//fine ciclo while che procede random fino alle foglie dopo aver trovato il target (nel caso lui stesso non sia una foglia)
  
  return new ContinuePathResult(w, s, s2, s3, path, numSpare, localNode, localCont, camT, nBlock);
  
}

private BST moveUpTarget(BST T, BSTNode target, int level)
{
  //esegue rotazioni fino a quando il nodo target raggiungere il livello level passato in input 
    
    
    //se level è = 0 scelgo casualmente di posizionare il target nei primi k livelli
    if(level == 0)
    {
        Random rand = new Random();
        int min = 1;
        int max = (int) (this.numBlock)/4;    
        
        //genero casualmente un intero tra 1 e i primi  2logn/4 livelli dell'albero
        int randomNum = rand.nextInt((max - min) + 1) + min;
        level = randomNum;
        
    }    
    
    //se level è = 1 ruoto il target fino a quando non raggiunge la posizione di root    
    if(level == 1)
        while(!(target.isRoot()))
                 T.rotate(target); 
    
    //altrimenti ruoto il target fino a quando non raggiunge il level-esimo livello
    else 
      for( int i = 0; i < (nodeToTarget-level); i++) 
         T.rotate(target);       
    
    return T;
    
}

private BST balanceTree(BST T, BSTNode[] local, int numBlock, int k)
{    
    
    //dopo aver letto il target esso viene spostato in alto nell'albero in maniera forzata, questo può causare un'eccessivo sbilanciamento
    //dell'albero che viene "ristemato" utilizzando questa funzione   
    
   int indt = 0;
   BSTNode target = new BSTNode(-123);
    
  //visto che ho ruotato, prima di tutto devo aggiornare i dati (h) dei nodi letti che ho nell'array localNode (basta ricercarli tutti e risalvarli)
  for ( int i = 0; i < numBlock; i++)
         {
              local[i] = findNode(T, local[i].key);
                  if(local[i].key == k)
                  { 
                      indt = i;
                      target = local[i];                  
                  }
         }
  
  //ora devo cercare il target e metterlo all'inizio dell'array (perchè ruoto scorrendo l'array dal fondo all'inizio, dalle foglie verso la root)   
  for ( int i = 0; i < numBlock; i++ )
           if(local[i].key == k)
                    indt = i-1;
    
  for (int i = indt; i >= 0; i--)              
      local[i+1] = local[i];
  
  local[0] = target;
  
  BSTNode papa;
  BSTNode node;
  int hold;
  int hnew;
      
  for ( int i = numBlock-1; i > 0; i-- )
  {   
      T.getRoot().calcTree();
      node = findNode(T, local[i].key);
      
      if(node.key >= 0 && node != T.getRoot())
      {     
          papa = node.getParent();
          hold = papa.height;
            
          T.rotate(node);
          T.getRoot().calcTree();
     
          hnew = node.height;
     
          if(hnew > hold)
          {
             T.rotate(papa);
             T.getRoot().calcTree();
          }                     

      }
      
      else if(node == T.getRoot())
      {
          if(node.hdx > node.hsx)
              T.rotate(node.getRight());
          else if(node.hsx > node.hdx)
              T.rotate(node.getLeft()); 
          
          T.getRoot().calcTree();
      }
   }
  
      T.getRoot().calcTree();
  
  //ritorno l'albero risistemato    
  return T;
}

private BSTNode findNode(BST T, int tofind)
{
  BSTNode w = T.getRoot();
  boolean find = false;
  
  if(tofind >= 0)
      while (!find) 
          {                    
              if (w.getKey() == tofind) 
                  return w;           
              else if (w.getKey() < tofind)                     
                 w = w.getRight(); 
              else           
                 w = w.getLeft();
       }       
  else
      w = new BSTNode(-666);
  
  return w;
}

public int[] shuffleArray(int[] ar)
{
    //++++KNUTH-SHUFFLING
    
        for (int i = 0; i < ar.length; i++) {
            int r = i + (int) (Math.random() * (ar.length - i));
            int swap = ar[r];
            ar[r] = ar[i];
            ar[i] = swap;
        }
   return ar;
}

public ReadSpareResult readSpare(BSTNode t, BSTNode s1, BSTNode s2, BSTNode s3, int dir, int h, int n)
{
    
    if(s1 == s2 && s2 == s3) //caso in cui ho letto solo due nodi del livello prima (t, s1)
    {
    
    //il target è verso destra del nodo t
    if(dir == 1)
    {
        if ( w == s1 && w.getLeft() != null)
            return new ReadSpareResult(w.getLeft());
            
        else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null)
            return chooseSpare3(t.getLeft(),s1.getLeft(),s1.getRight(), h, n);
        
        else if (t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null)
            return chooseSpare2(t.getLeft(),s1.getRight(), h, n);
        else if (t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null)
            return chooseSpare2(t.getLeft(),s1.getLeft(), h, n);
        else if (t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null)
            return chooseSpare2(s1.getLeft(), s1.getRight(), h, n);
        
        else if (t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null)
            return chooseSpare(s1.getRight(), h, n);
        else if (t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null)
            return chooseSpare(s1.getLeft(), h, n);
        else if (t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null)
            return chooseSpare(t.getLeft(), h, n);
               
        else if (t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null)
            return new ReadSpareResult(new BSTNode(-1));
                
    }//il target è a s1inis1tra del nodo t
    else if (dir == -1)
    {
        if ( w == s1 && w.getRight() != null)
            return new ReadSpareResult(w.getRight());
        
        else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null)
            return chooseSpare3(t.getRight(),s1.getLeft(),s1.getRight(), h, n);
        
        else if (t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null)
            return chooseSpare2(t.getRight(),s1.getRight(), h, n);
        else if (t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null)
            return chooseSpare2(t.getRight(),s1.getLeft(), h, n);
        else if (t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null)
            return chooseSpare2(s1.getLeft(), s1.getRight(), h, n);
        
        else if (t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null)
            return chooseSpare(s1.getRight(), h, n);
        else if (t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null)
            return chooseSpare(s1.getLeft(), h, n);
        else if (t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null)
            return chooseSpare(t.getRight(), h, n);
        
        else if (t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null)
            return new ReadSpareResult(new BSTNode(-1));
    }
    
    } //fine if letto due nodi livello precedente
    
    else if( s1 != s2 && s2 == s3) //livello precedente ho letto tre nodi (t, s1, s2)
    {       
      //il target è verso destra del nodo t
        if(dir == 1)
        {
            
           if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
                        
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare4(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare4(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getRight(), h, n);
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(t.getLeft(),s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(t.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
            
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare3(t.getLeft(),s1.getLeft(),s1.getRight(), h, n);            
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(t.getLeft(),s1.getLeft(),s2.getLeft(), h, n);
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(t.getLeft(),s1.getLeft(),s2.getRight(), h, n);
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(t.getLeft(),s1.getRight(),s2.getLeft(), h, n);            
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(t.getLeft(),s1.getRight(),s2.getRight(), h, n);     
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);            
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getRight(), h, n);          
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare3(s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare3(s1.getRight(), s2.getLeft(),s2.getRight(), h, n);
            
            else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(t.getLeft(),s1.getLeft(), h, n); 
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(t.getLeft(),s1.getRight(), h, n); 
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(t.getLeft(),s2.getLeft(), h, n); 
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(t.getLeft(),s2.getRight(), h, n);           
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(s1.getLeft(),s1.getRight(), h, n); 
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(s1.getLeft(),s2.getLeft(), h, n); 
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(s1.getLeft(),s2.getRight(), h, n);           
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(s1.getRight(),s2.getLeft(), h, n); 
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(s1.getRight(),s2.getRight(), h, n);            
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare2(s2.getLeft(),s2.getRight(), h, n); 
            
            else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(t.getLeft(),h, n); 
            else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(s1.getLeft(), h, n); 
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(s1.getRight(), h, n); 
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare(s2.getLeft(), h, n);
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare(s2.getRight(), h, n); 
            
            else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return new ReadSpareResult(new BSTNode(-1)); 
                          
        }//il target è a sinistra del nodo t
        else if (dir == -1)
        {
            if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
            
            
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare4(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare4(t.getRight(),s1.getLeft(),s1.getRight(),s2.getRight(), h, n);
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(t.getRight(),s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(t.getRight(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare4(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
            
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare3(t.getRight(),s1.getLeft(),s1.getRight(), h, n);            
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(t.getRight(),s1.getLeft(),s2.getLeft(), h, n);
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(t.getRight(),s1.getLeft(),s2.getRight(), h, n);
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(t.getRight(),s1.getRight(),s2.getLeft(), h, n);            
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(t.getRight(),s1.getRight(),s2.getRight(), h, n);     
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);            
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getRight(), h, n);          
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare3(s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare3(s1.getRight(), s2.getLeft(),s2.getRight(), h, n);
            
            else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(t.getRight(),s1.getLeft(), h, n); 
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(t.getRight(),s1.getRight(), h, n); 
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(t.getRight(),s2.getLeft(), h, n); 
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(t.getRight(),s2.getRight(), h, n);           
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare2(s1.getLeft(),s1.getRight(), h, n); 
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(s1.getLeft(),s2.getLeft(), h, n); 
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(s1.getLeft(),s2.getRight(), h, n);           
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare2(s1.getRight(),s2.getLeft(), h, n); 
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare2(s1.getRight(),s2.getRight(), h, n);            
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null)
                return chooseSpare2(s2.getLeft(),s2.getRight(), h, n); 
            
            else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(t.getRight(),h, n); 
            else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(s1.getLeft(), h, n); 
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null)
                return chooseSpare(s1.getRight(), h, n); 
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null)
                return chooseSpare(s2.getLeft(), h, n);
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null)
                return chooseSpare(s2.getRight(), h, n); 
            
            else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null)
                return new ReadSpareResult(new BSTNode(-1));
        }  
    }//fine if al livello precedente ho letto 3 nodi
    
    else if( s1 != s2 && s2 != s3) //livello precedente ho letto tre nodi (t, s1, s2)
    {       
        //il target è verso destra del nodo t
          if(dir == 1)
          {
              //parte dei 7
             if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare7(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
                          
             //parte dei 6
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare6(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare6(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare6(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare6(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare6(t.getLeft(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare6(t.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare6(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
                          
             //parte dei 5
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getRight() == null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s2.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s2.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s3.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                 return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s3.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                 return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
             else if(s1.getLeft() == null && s1.getRight() == null && t.getLeft() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(s1.getLeft() == null && s2.getLeft() == null && t.getLeft() != null &&  s1.getRight() != null  &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(s1.getLeft() == null && s2.getRight() == null && t.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(s1.getLeft() == null && s3.getLeft() == null && t.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
             else if(s1.getLeft() == null && s3.getRight() == null && t.getLeft() != null  &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                 return chooseSpare5(t.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
             else if(s1.getRight() == null && s2.getLeft() == null && t.getLeft() != null && s1.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(s1.getRight() == null && s2.getRight() == null && t.getLeft() != null && s1.getLeft() != null &&  s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(s1.getRight() == null && s3.getLeft() == null && t.getLeft() != null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
             else if(s1.getRight() == null && s3.getRight() == null && t.getLeft() != null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
             else if(s2.getLeft() == null && s2.getRight() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(s2.getLeft() == null && s3.getLeft() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getRight(), s3.getRight(), h, n);
             else if(s2.getLeft() == null && s3.getRight() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null &&  s2.getRight() != null && s3.getLeft() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
             else if(s2.getRight() == null && s3.getLeft() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getRight(), h, n);
             else if(s2.getRight() == null && s3.getRight() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), h, n);
             else if(s3.getLeft() == null && s3.getRight() == null && t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                 return chooseSpare5(t.getLeft(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(),h, n);
             
             //parte dei 4
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s2.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getRight(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getRight(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getRight(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(s1.getRight(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
                     
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s3.getLeft(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
             

           
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s2.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s2.getLeft(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
             
             
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s3.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s2.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s2.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s2.getLeft(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s2.getLeft(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s2.getLeft(), s2.getRight(), h, n);
             
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s1.getRight(), s3.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s1.getRight(), s3.getLeft(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s1.getRight(), s2.getRight(), h, n);
             
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare4(t.getLeft(), s1.getLeft(), s1.getRight(), s2.getLeft(), h, n);
             
             //parte dei tre
             
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getLeft(),s1.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getLeft(),s2.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getLeft(),s2.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getLeft(), s3.getLeft(),h, n);
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(t.getLeft(),s1.getLeft(),s3.getRight(), h, n);
                     
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getRight(),s2.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getRight(),s2.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s1.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(t.getLeft(),s1.getRight(),s3.getRight(),h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s2.getLeft(),s2.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s2.getLeft(),s3.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(t.getLeft(),s2.getLeft(),s3.getRight(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(t.getLeft(),s2.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(t.getLeft(),s2.getRight(),s3.getRight(), h, n);
             
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare3(t.getLeft(),s3.getLeft(),s3.getRight(), h, n);
             

             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s2.getLeft(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getLeft(),s2.getLeft(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getLeft(),s2.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getLeft(),s2.getRight(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getLeft(),s3.getLeft(),s3.getRight(), h, n);
             
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s1.getRight(),s2.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getRight(),s2.getRight(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare3(s1.getRight(),s3.getLeft(),s3.getRight(), h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getRight(), h, n);
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare3(s2.getLeft(),s3.getLeft(),s3.getRight(), h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare3(s2.getRight(),s3.getLeft(),s3.getRight(), h, n);
             
             
             //parte dei due
             else if(t.getLeft() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(t.getLeft(), s1.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(t.getLeft(), s1.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(t.getLeft(), s2.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(t.getLeft(), s2.getRight(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare2(t.getLeft(), s3.getLeft(), h, n);
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare2(t.getLeft(), s3.getRight(), h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getLeft(), s1.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getLeft(), s2.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getLeft(), s2.getRight(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getLeft(), s3.getLeft(), h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare2(s1.getLeft(), s3.getRight(), h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getRight(), s2.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getRight(), s2.getRight(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare2(s1.getRight(), s3.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare2(s1.getRight(), s3.getRight(),  h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare2(s2.getLeft(), s2.getRight(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare2(s2.getLeft(), s3.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare2(s2.getLeft(), s3.getRight(),  h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare2(s2.getRight(), s3.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare2(s2.getRight(), s3.getRight(),  h, n);
             
             
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                 return chooseSpare2(s3.getLeft(), s3.getRight(),  h, n);
             
             
            //parte dell'uno 
             else if(t.getLeft() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare(t.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare(s1.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare(s1.getRight(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare(s2.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                 return chooseSpare(s2.getRight(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                 return chooseSpare(s3.getLeft(),  h, n);
             else if(t.getLeft() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                 return chooseSpare(s3.getRight(),  h, n);
                            
          }//il target è a sinistra del nodo t
          else if (dir == -1)
          {
            //parte dei 7
              if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                   return chooseSpare7(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
                           
              //parte dei 6
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare6(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare6(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare6(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare6(t.getRight(),s1.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare6(t.getRight(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare6(t.getRight(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare6(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
                           
              //parte dei 5
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getRight() == null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s2.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s2.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s3.getLeft() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                  return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() == null && s3.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                  return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
              else if(s1.getLeft() == null && s1.getRight() == null && t.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(s1.getLeft() == null && s2.getLeft() == null && t.getRight() != null &&  s1.getRight() != null  &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(s1.getLeft() == null && s2.getRight() == null && t.getRight() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(s1.getLeft() == null && s3.getLeft() == null && t.getRight() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
              else if(s1.getLeft() == null && s3.getRight() == null && t.getRight() != null  &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                  return chooseSpare5(t.getRight(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
              else if(s1.getRight() == null && s2.getLeft() == null && t.getRight() != null && s1.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(s1.getRight() == null && s2.getRight() == null && t.getRight() != null && s1.getLeft() != null &&  s2.getLeft() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(s1.getRight() == null && s3.getLeft() == null && t.getRight() != null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
              else if(s1.getRight() == null && s3.getRight() == null && t.getRight() != null && s1.getLeft() != null && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
              else if(s2.getLeft() == null && s2.getRight() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(s2.getLeft() == null && s3.getLeft() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null &&  s2.getRight() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(),s2.getRight(), s3.getRight(), h, n);
              else if(s2.getLeft() == null && s3.getRight() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null &&  s2.getRight() != null && s3.getLeft() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
              else if(s2.getRight() == null && s3.getLeft() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getRight(), h, n);
              else if(s2.getRight() == null && s3.getRight() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null && s3.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), h, n);
              else if(s3.getLeft() == null && s3.getRight() == null && t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null)
                  return chooseSpare5(t.getRight(),s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(),h, n);
              
              //parte dei 4
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s2.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getRight(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getRight(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getRight(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(s1.getRight(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
                      
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s3.getLeft(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
              

            
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s2.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s2.getLeft(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
              
              
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s3.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s2.getRight(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s2.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s2.getLeft(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s2.getLeft(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s2.getLeft(), s2.getRight(), h, n);
              
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s1.getRight(), s3.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s1.getRight(), s3.getLeft(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s1.getRight(), s2.getRight(), h, n);
              
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare4(t.getRight(), s1.getLeft(), s1.getRight(), s2.getLeft(), h, n);
              
              //parte dei tre
              
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getLeft(),s1.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getLeft(),s2.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getLeft(),s2.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getLeft(), s3.getLeft(),h, n);
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(t.getRight(),s1.getLeft(),s3.getRight(), h, n);
                      
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getRight(),s2.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getRight(),s2.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s1.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(t.getRight(),s1.getRight(),s3.getRight(),h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s2.getLeft(),s2.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s2.getLeft(),s3.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(t.getRight(),s2.getLeft(),s3.getRight(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(t.getRight(),s2.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(t.getRight(),s2.getRight(),s3.getRight(), h, n);
              
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare3(t.getRight(),s3.getLeft(),s3.getRight(), h, n);
              

              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s2.getLeft(),s2.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s2.getLeft(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getLeft(),s2.getLeft(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getLeft(),s2.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getLeft(),s2.getRight(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getLeft(),s3.getLeft(),s3.getRight(), h, n);
              
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s1.getRight(),s2.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getRight(),s2.getRight(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare3(s1.getRight(),s3.getLeft(),s3.getRight(), h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getRight(), h, n);
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare3(s2.getLeft(),s3.getLeft(),s3.getRight(), h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare3(s2.getRight(),s3.getLeft(),s3.getRight(), h, n);
              
              
              //parte dei due
              else if(t.getRight() != null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(t.getRight(), s1.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(t.getRight(), s1.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(t.getRight(), s2.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(t.getRight(), s2.getRight(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare2(t.getRight(), s3.getLeft(), h, n);
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare2(t.getRight(), s3.getRight(), h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getLeft(), s1.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getLeft(), s2.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getLeft(), s2.getRight(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getLeft(), s3.getLeft(), h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare2(s1.getLeft(), s3.getRight(), h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getRight(), s2.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getRight(), s2.getRight(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare2(s1.getRight(), s3.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare2(s1.getRight(), s3.getRight(),  h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare2(s2.getLeft(), s2.getRight(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare2(s2.getLeft(), s3.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare2(s2.getLeft(), s3.getRight(),  h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare2(s2.getRight(), s3.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare2(s2.getRight(), s3.getRight(),  h, n);
              
              
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
                  return chooseSpare2(s3.getLeft(), s3.getRight(),  h, n);
              
              
             //parte dell'uno 
              else if(t.getRight() != null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare(t.getRight(),  h, n);
              else if(t.getRight() == null && s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare(s1.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare(s1.getRight(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare(s2.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
                  return chooseSpare(s2.getRight(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
                  return chooseSpare(s3.getLeft(),  h, n);
              else if(t.getRight() == null && s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
                  return chooseSpare(s3.getRight(),  h, n);
              
          }  
      }
  
    return new ReadSpareResult(new BSTNode(-3));
}

public ReadSpareResult readSpareAfterT(BSTNode s1, BSTNode s2, BSTNode s3, int h, int n)
{    
    int k1 = s1.key;
    int k2 = s2.key;
    int k3 = s3.key;
    
     
    if( k1 == k2 && k2 == k3)
    {
        if(s1.getLeft() != null && s1.getRight() != null)
            return chooseSpare2(s1.getLeft(), s1.getRight(), h, n);
        
        else if (s1.getLeft() != null && s1.getRight() == null)
            return chooseSpare(s1.getLeft(), h, n);
        else if (s1.getLeft() == null && s1.getRight() != null)
            return chooseSpare(s1.getRight(), h, n);
            
        else if (s1.getLeft() == null && s1.getRight() == null)
            return new ReadSpareResult(new BSTNode(-212));
    }
    
    else if (k1 == k3 && k1 != k2)
    {  
                
        if(s1.getLeft() != null && s1.getRight() != null && s2.getLeft() != null && s2.getRight() != null)
            return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
            
        else if (s1.getLeft() != null && s1.getRight() != null && s2.getLeft() != null && s2.getRight() == null)
            return chooseSpare3(s1.getLeft(), s1.getRight(), s2.getLeft(), h,n);
        else if (s1.getLeft() != null && s1.getRight() != null && s2.getLeft() == null && s2.getRight() != null)
            return chooseSpare3(s1.getLeft(), s1.getRight(), s2.getRight(), h,n);
        else if (s1.getLeft() != null && s1.getRight() == null && s2.getLeft() != null && s2.getRight() != null)
            return chooseSpare3(s1.getLeft(), s2.getLeft(), s2.getRight(), h,n);
        else if (s1.getLeft() == null && s1.getRight() != null && s2.getLeft() != null && s2.getRight() != null)
            return chooseSpare3(s1.getRight(), s2.getLeft(), s2.getRight(), h,n);
               
        else if (s1.getLeft() != null && s1.getRight() != null && s2.getLeft() == null && s2.getRight() == null)
            return chooseSpare2(s1.getLeft(), s1.getRight(), h,n);
        else if (s1.getLeft() != null && s1.getRight() == null && s2.getLeft() != null && s2.getRight() == null)
            return chooseSpare2(s1.getLeft(), s2.getLeft(), h,n);
        else if (s1.getLeft() != null && s1.getRight() == null && s2.getLeft() == null && s2.getRight() != null)
            return chooseSpare2(s1.getLeft(), s2.getRight(), h,n);
        else if (s1.getLeft() == null && s1.getRight() != null && s2.getLeft() != null && s2.getRight() == null)
            return chooseSpare2(s1.getRight(), s2.getLeft(), h,n);
        else if (s1.getLeft() == null && s1.getRight() != null && s2.getLeft() == null && s2.getRight() != null)
            return chooseSpare2(s1.getRight(), s2.getRight(), h,n);
        else if (s1.getLeft() == null && s1.getRight() == null && s2.getLeft() != null && s2.getRight() != null)
            return chooseSpare2(s2.getLeft(), s2.getRight(), h,n);
        
        else if (s1.getLeft() != null && s1.getRight() == null && s2.getLeft() == null && s2.getRight() == null)
            return chooseSpare(s1.getLeft(), h,n);
        else if (s1.getLeft() == null && s1.getRight() != null && s2.getLeft() == null && s2.getRight() == null)
            return chooseSpare(s1.getRight(), h,n);
        else if (s1.getLeft() == null && s1.getRight() == null && s2.getLeft() != null && s2.getRight() == null)
            return chooseSpare(s2.getLeft(), h,n);
        else if (s1.getLeft() == null && s1.getRight() == null && s2.getLeft() == null && s2.getRight() != null)
            return chooseSpare(s2.getRight(), h,n);   
        
        else if (s1.getLeft() == null && s1.getRight() == null && s2.getLeft() == null && s2.getRight() == null)
            return new ReadSpareResult(new BSTNode(-99));             
    }
    
    else if (k1 != k2 && k1 != k3)
    {  
        if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare6(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
     
        //parte dei 5
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(),s2.getRight(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare5(s1.getLeft(),s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare5(s1.getLeft(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare5(s1.getRight(),s2.getLeft(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
        
        
        
        //parte dei 4
        
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s2.getLeft(),s2.getRight(),s3.getLeft(),s3.getRight(),h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getRight(), s2.getRight(),s3.getLeft(),s3.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getRight(),s2.getRight(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getRight(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare4(s1.getRight(), s2.getLeft(), s2.getRight(), s3.getRight(), h, n);
        
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getLeft(), s2.getRight(), s3.getLeft(), s3.getRight(), h, n);        
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getLeft(), s2.getLeft(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getRight(),h , n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare4(s1.getLeft(), s2.getLeft(), s2.getRight(), s3.getLeft(), h, n);
        
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare4(s1.getLeft(), s1.getRight(), s3.getLeft(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getRight(), s3.getLeft(), h, n);
       
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare4(s1.getLeft(), s1.getRight(), s2.getLeft(), s2.getRight(), h, n);
        
        
        
        
        //parte dei 3
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s1.getRight(),s2.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s1.getLeft(),s1.getRight(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s2.getLeft(), s2.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s2.getLeft(), s3.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s1.getLeft(),s2.getLeft(), s3.getRight(), h, n);
        
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s1.getLeft(),s2.getRight(), s3.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s1.getLeft(),s2.getRight(), s3.getRight(), h, n);
        
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare3(s1.getLeft(),s3.getLeft(), s3.getRight(), h, n);
        
        
        
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare3(s1.getRight(),s2.getLeft(),s2.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getLeft(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s1.getRight(),s2.getLeft(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s1.getRight(),s2.getRight(),s3.getLeft(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s1.getRight(),s2.getRight(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare3(s1.getRight(),s3.getLeft(),s3.getRight(), h, n);
        
        
        
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getLeft(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare3(s2.getLeft(),s2.getRight(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare3(s2.getLeft(),s3.getLeft(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare3(s2.getRight(),s3.getLeft(),s3.getRight(), h, n);
        
        
        //parte dei 2
        else  if(  s1.getLeft() != null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare2(s1.getLeft(),s1.getRight(),h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare2(s1.getLeft(),s2.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare2(s1.getLeft(),s2.getRight(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare2(s1.getLeft(),s3.getLeft(), h, n);
        else  if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare2(s1.getLeft(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare2(s1.getRight(),s2.getLeft(), h, n);        
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare2(s1.getRight(),s2.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare2(s1.getRight(),s3.getLeft(),h , n);
        else  if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare2(s1.getRight(),s3.getRight(), h, n);
        
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare2(s2.getLeft(),s2.getRight(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare2(s2.getLeft(), s3.getLeft(), h, n);
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare2(s2.getLeft(), s3.getRight(), h, n);
       
        else  if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() != null)
            return chooseSpare2(s3.getLeft(), s3.getRight(), h, n);
          
        //parte dell'uno 
        else if(  s1.getLeft() != null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare(s1.getLeft(),  h, n);
        else if(  s1.getLeft() == null &&  s1.getRight() != null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare(s1.getRight(),  h, n);
        else if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() != null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare(s2.getLeft(),  h, n);
        else if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() != null && s3.getLeft() == null &&  s3.getRight() == null)
            return chooseSpare(s2.getRight(),  h, n);
        else if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() != null &&  s3.getRight() == null)
            return chooseSpare(s3.getLeft(),  h, n);
        else if(  s1.getLeft() == null &&  s1.getRight() == null  && s2.getLeft() == null &&  s2.getRight() == null && s3.getLeft() == null &&  s3.getRight() != null)
            return chooseSpare(s3.getRight(),  h, n);
        
        else return new ReadSpareResult(new BSTNode(-44));       
    }
 
    else if (k1 == k2 && k1 != k3)
    {
      //non può mai verificarsi in teoria 
       return new ReadSpareResult(new BSTNode(-22));       
    }
    
    else if (k2 == k3 && k2 != k1)
    {
       //non può mai verificarsi in teoria
       return new ReadSpareResult(new BSTNode(-33));       
    }
    
    
    return new ReadSpareResult(new BSTNode(-80));
}

public ReadSpareResult chooseSpare7(BSTNode a,BSTNode b,BSTNode c,BSTNode d,BSTNode e, BSTNode f, BSTNode g, int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
    int hf = f.height+1;
    int hg = g.height+1;
         
    //skippo tutti i casi delle combinazioni con i rand
    if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
    else if( hc+h >= n )
        return new ReadSpareResult(c);
    else if( hd+h >= n )
        return new ReadSpareResult(d);
    else if( he+h >= n )
        return new ReadSpareResult(e);
    else if( hf+h >= n )
        return new ReadSpareResult(f);
    else if( hg+h >= n )
        return new ReadSpareResult(g);
    
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n && hc+h < n && hd+h < n && he+h < n)
        return chooseBrother(a,b,c,d,e,f,g,h,n);

    return new ReadSpareResult(new BSTNode(-4));
}

public ReadSpareResult chooseSpare6(BSTNode a,BSTNode b,BSTNode c,BSTNode d,BSTNode e, BSTNode f, int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
    int hf = f.height+1;
          
    //skippo tutti i casi delle combinazioni con i rand
    if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
    else if( hc+h >= n )
        return new ReadSpareResult(c);
    else if( hd+h >= n )
        return new ReadSpareResult(d);
    else if( he+h >= n )
        return new ReadSpareResult(e);
    else if( hf+h >= n )
        return new ReadSpareResult(f);
    
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n && hc+h < n && hd+h < n && he+h < n)
        return chooseBrother(a,b,c,d,e,f,h,n);

    return new ReadSpareResult(new BSTNode(-4));
}

public ReadSpareResult chooseSpare5(BSTNode a,BSTNode b,BSTNode c,BSTNode d,BSTNode e,int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
          
    if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h >=n && he+h >=n )
        return randSpare5(a,b,c,d,e);
   
    else if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h >= n && he+h < n)
        return randSpare4(a,b,c,d);
    else if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h < n && he+h >= n)
        return randSpare4(a,b,c,e);
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h >= n && he+h >= n)
        return randSpare4(a,b,d,e);
    else if( ha+h >= n && hb+h < n && hc+h >= n && hd+h >= n && he+h >= n)
        return randSpare4(a,c,d,e);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h >= n && he+h >= n)
        return randSpare4(b,c,d,e);
    
    else if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h < n && he+h < n)
        return randSpare3(a,b,c);
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h >= n && he+h < n)
        return randSpare3(a,b,d);
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h < n && he+h >= n)
        return randSpare3(a,b,e);
    else if( ha+h >= n && hb+h < n && hc+h >= n && hd+h >= n && he+h < n)
        return randSpare3(a,c,d);
    else if( ha+h < n && hb+h < n && hc+h >= n && hd+h < n && he+h >= n)
        return randSpare3(b,c,e);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h >= n && he+h < n)
        return randSpare3(b,c,d);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h < n && he+h >= n)
        return randSpare3(b,c,e);
    else if( ha+h < n && hb+h >= n && hc+h < n && hd+h >= n && he+h >= n)
        return randSpare3(b,d,e);
    else if( ha+h < n && hb+h < n && hc+h >= n && hd+h >= n && he+h >= n)
        return randSpare3(c,d,e);
    
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h < n && he+h < n)
        return randSpare2(a,b);
    else if( ha+h >= n && hb+h < n && hc+h >= n && hd+h < n && he+h < n)
        return randSpare2(a,c);
    else if( ha+h >= n && hb+h < n && hc+h < n && hd+h >= n && he+h < n)
        return randSpare2(a,d);
    else if( ha+h >= n && hb+h < n && hc+h < n && hd+h < n && he+h >= n)
        return randSpare2(a,e);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h < n && he+h < n)
        return randSpare2(b,c);
    else if( ha+h < n && hb+h >= n && hc+h < n && hd+h >= n && he+h < n)
        return randSpare2(b,d);
    else if( ha+h < n && hb+h >= n && hc+h < n && hd+h < n && he+h >= n)
        return randSpare2(b,e);
    else if( ha+h < n && hb+h < n && hc+h >= n && hd+h >= n && he+h < n)
        return randSpare2(c,d);
    else if( ha+h < n && hb+h < n && hc+h >= n && hd+h < n && he+h >= n)
        return randSpare2(c,e);
    else if( ha+h < n && hb+h < n && hc+h < n && hd+h >= n && he+h >= n)
        return randSpare2(d,e);
       
    else if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
    else if( hc+h >= n )
        return new ReadSpareResult(c);
    else if( hd+h >= n )
        return new ReadSpareResult(d);
    else if( he+h >= n )
        return new ReadSpareResult(e);
        
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n && hc+h < n && hd+h < n && he+h < n)
        return chooseBrother(a,b,c,d,e,h,n);

    return new ReadSpareResult(new BSTNode(-4));
}

public ReadSpareResult chooseSpare4(BSTNode a,BSTNode b,BSTNode c,BSTNode d, int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
          
    if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h >=n)
        return randSpare4(a,b,c,d);
    
    else if( ha+h >= n && hb+h >= n && hc+h >= n && hd+h < n)
        return randSpare3(a,b,c);
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h >= n)
        return randSpare3(a,b,d);
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h >= n)
        return randSpare3(a,c,d);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h >= n)
        return randSpare3(b,c,d);

    
    else if( ha+h >= n && hb+h >= n && hc+h < n && hd+h < n)
        return randSpare2(a,b);
    else if( ha+h >= n && hb+h < n && hc+h >= n && hd+h < n)
        return randSpare2(a,c);
    else if( ha+h >= n && hb+h < n && hc+h < n && hd+h >= n)
        return randSpare2(a,d);
    else if( ha+h < n && hb+h >= n && hc+h >= n && hd+h < n)
        return randSpare2(b,c);
    else if( ha+h < n && hb+h >= n && hc+h < n && hd+h >= n)
        return randSpare2(b,d);
    else if( ha+h < n && hb+h < n && hc+h >= n && hd+h >= n)
        return randSpare2(c,d);

       
    else if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
    else if( hc+h >= n )
        return new ReadSpareResult(c);
    else if( hd+h >= n )
        return new ReadSpareResult(d);
      
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n && hc+h < n && hd+h < n)
        return chooseBrother(a,b,c,d,h,n);

    return new ReadSpareResult(new BSTNode(-5));
}

public ReadSpareResult chooseSpare3(BSTNode a,BSTNode b,BSTNode c, int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
          
    if( ha+h >= n && hb+h >= n && hc+h >= n)
        return randSpare3(a,b,c);
        
    else if( ha+h >= n && hb+h >= n && hc+h < n)
        return randSpare2(a,b);
    else if( ha+h >= n && hb+h < n && hc+h >= n)
        return randSpare2(a,c);
    else if( ha+h < n && hb+h >= n && hc+h >= n)
        return randSpare2(b,c);

    else if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
    else if( hc+h >= n )
        return new ReadSpareResult(c);
      
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n && hc+h < n)
        return chooseBrother(a,b,c,h,n);

    return new ReadSpareResult(new BSTNode(-6));
}

public ReadSpareResult chooseSpare2(BSTNode a,BSTNode b, int h, int n)
{    
    int ha = a.height+1;
    int hb = b.height+1;
          
    if( ha+h >= n && hb+h >= n)
        return randSpare2(a,b);
        
    else if( ha+h >= n )
        return new ReadSpareResult(a);
    else if( hb+h >= n )
        return new ReadSpareResult(b);
      
  //i nodi successivi dei livelli solo sotto non mi bastano, devo prendere un nodo spare in più 
    else if( ha+h < n && hb+h < n)
        return chooseBrother(a,b,h,n);

    return new ReadSpareResult(new BSTNode(-7));
}

public ReadSpareResult chooseSpare(BSTNode a, int h, int n)
{    
    int ha = a.height+1;
        
    if( ha+h >= n )
        return new ReadSpareResult(a);

    return new ReadSpareResult(new BSTNode(-8));
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, BSTNode c, BSTNode d,BSTNode e, BSTNode f, BSTNode g, int h, int n)
{ 
    int ha = a.height+1;
    int hb= b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
    int hf = f.height+1;
    int hg = e.height+1;
     
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);
    else if ( ha + hc + h >= n)
        return new ReadSpareResult(a,c);
    else if ( ha + hd + h >= n)
        return new ReadSpareResult(a,d);
    else if ( ha + he + h >= n)
        return new ReadSpareResult(a,e);
    else if ( hb + hc + h >= n)
        return new ReadSpareResult(b,c);
    else if ( hb + hd + h >= n)
        return new ReadSpareResult(b,d);
    else if ( hb + he + h >= n)
        return new ReadSpareResult(b,e);
    else if ( hc + hd + h >= n)
        return new ReadSpareResult(c,d);
    else if ( hc + he + h >= n)
        return new ReadSpareResult(c,e);
    else if ( hd + he + h >= n)
        return new ReadSpareResult(d,e);
    else if ( ha + hf + h >= n)
        return new ReadSpareResult(a,f);
    else if ( ha + hg + h >= n)
        return new ReadSpareResult(a,g);
    else if ( hb + hf + h >= n)
        return new ReadSpareResult(b,f);
    else if ( hb + hg + h >= n)
        return new ReadSpareResult(b,g);
    else if ( hc + hf + h >= n)
        return new ReadSpareResult(c,f);
    else if ( hc + hg + h >= n)
        return new ReadSpareResult(c,g);
    else if ( hd + hf + h >= n)
        return new ReadSpareResult(d,f);
    else if ( hd + hg + h >= n)
        return new ReadSpareResult(d,g);
    else if ( he + hf + h >= n)
        return new ReadSpareResult(e,f);
    else if ( he + hg + h >= n)
        return new ReadSpareResult(e,g);
    else if ( hf + hg + h >= n)
        return new ReadSpareResult(f,g);
    
    
    //devo prendere ancora dei nodi in più
    else if( ha + hb + hc + h >= n)
        return new ReadSpareResult(a,b,c);
    else if( ha + hb + hd + h >= n)
        return new ReadSpareResult(a,b,d);
    else if( ha + hb + he + h >= n)
        return new ReadSpareResult(a,b,e);
    else if( ha + hb + hf + h >= n)
        return new ReadSpareResult(a,b,f);
    else if( ha + hb + hg + h >= n)
        return new ReadSpareResult(a,b,g);
    
    else if( ha + hc + hd + h >= n)
        return new ReadSpareResult(a,c,d);
    else if( ha + hc + he + h >= n)
        return new ReadSpareResult(a,c,e);
    else if( ha + hc + hf + h >= n)
        return new ReadSpareResult(a,c,f);
    else if( ha + hc + hg + h >= n)
        return new ReadSpareResult(a,c,g);
    
    else if( ha + hd + he + h >= n)
        return new ReadSpareResult(a,d,e);
    else if( ha + hd + hf + h >= n)
        return new ReadSpareResult(a,d,f);
    else if( ha + hd + hg + h >= n)
        return new ReadSpareResult(a,d,g);
    
    else if( ha + he + hf + h >= n)
        return new ReadSpareResult(a,e,f);
    else if( ha + he + hg + h >= n)
        return new ReadSpareResult(a,e,g);
    
    else if( ha + hf + hg + h >= n)
        return new ReadSpareResult(a,f,g);


    
    else if( hb + hc + hd + h >= n)
        return new ReadSpareResult(b,c,d);
    else if( hb + hc + he + h >= n)
        return new ReadSpareResult(b,c,e);
    else if( hb + hc + hf + h >= n)
        return new ReadSpareResult(b,c,f);
    else if( hb + hc + hg + h >= n)
        return new ReadSpareResult(b,c,g);

    else if( hb + hd + he + h >= n)
        return new ReadSpareResult(b,d,e);
    else if( hb + hd + hf + h >= n)
        return new ReadSpareResult(b,d,f);
    else if( hb + hd + hg + h >= n)
        return new ReadSpareResult(b,d,g);
    
    else if( hb + he + hf + h >= n)
        return new ReadSpareResult(b,e,f);
    else if( hb + he + hg + h >= n)
        return new ReadSpareResult(b,e,g);
    
    else if( hb + hf + hg + h >= n)
        return new ReadSpareResult(b,f,g);

    
    
    else if( hc + hd + he + h >= n)
        return new ReadSpareResult(c,d,e);
    else if( hc + hd + hf + h >= n)
        return new ReadSpareResult(c,d,f);
    else if( hc + hd + hg + h >= n)
        return new ReadSpareResult(c,d,g);
    
    
    else if( hc + he + hf + h >= n)
        return new ReadSpareResult(c,e,f);
    else if( hc + he + hg + h >= n)
        return new ReadSpareResult(c,e,g);
    
    else if( hc + hf + hg + h >= n)
        return new ReadSpareResult(c,f,g);
    
    
    
    else if( hd + he + hf + h >= n)
        return new ReadSpareResult(d,e,f);
    else if( hd + he + hg + h >= n)
        return new ReadSpareResult(d,e,g);
    
    else if( hd + hf + hg + h >= n)
        return new ReadSpareResult(d,f,g);
    
    
    else if( he + hf + hg + h >= n)
        return new ReadSpareResult(e,f,g);
    
        
    return new ReadSpareResult(new BSTNode(-9));
    
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, BSTNode c, BSTNode d,BSTNode e, BSTNode f, int h, int n)
{ 
    int ha = a.height+1;
    int hb= b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
    int hf = f.height+1;
      
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);
    else if ( ha + hc + h >= n)
        return new ReadSpareResult(a,c);
    else if ( ha + hd + h >= n)
        return new ReadSpareResult(a,d);
    else if ( ha + he + h >= n)
        return new ReadSpareResult(a,e);
    else if ( hb + hc + h >= n)
        return new ReadSpareResult(b,c);
    else if ( hb + hd + h >= n)
        return new ReadSpareResult(b,d);
    else if ( hb + he + h >= n)
        return new ReadSpareResult(b,e);
    else if ( hc + hd + h >= n)
        return new ReadSpareResult(c,d);
    else if ( hc + he + h >= n)
        return new ReadSpareResult(c,e);
    else if ( hd + he + h >= n)
        return new ReadSpareResult(d,e);
    else if ( ha + hf + h >= n)
        return new ReadSpareResult(a,f);
    else if ( hb + hf + h >= n)
        return new ReadSpareResult(b,f);
    else if ( hc + hf + h >= n)
        return new ReadSpareResult(c,f);
    else if ( hd + hf + h >= n)
        return new ReadSpareResult(d,f);
    else if ( he + hf + h >= n)
        return new ReadSpareResult(e,f);

    
    
    //devo prendere ancora dei nodi in più
    else if( ha + hb + hc + h >= n)
        return new ReadSpareResult(a,b,c);
    else if( ha + hb + hd + h >= n)
        return new ReadSpareResult(a,b,d);
    else if( ha + hb + he + h >= n)
        return new ReadSpareResult(a,b,e);
    else if( ha + hb + hf + h >= n)
        return new ReadSpareResult(a,b,f);

    
    else if( ha + hc + hd + h >= n)
        return new ReadSpareResult(a,c,d);
    else if( ha + hc + he + h >= n)
        return new ReadSpareResult(a,c,e);
    else if( ha + hc + hf + h >= n)
        return new ReadSpareResult(a,c,f);

    
    else if( ha + hd + he + h >= n)
        return new ReadSpareResult(a,d,e);
    else if( ha + hd + hf + h >= n)
        return new ReadSpareResult(a,d,f);

    
    else if( ha + he + hf + h >= n)
        return new ReadSpareResult(a,e,f);

    
    else if( hb + hc + hd + h >= n)
        return new ReadSpareResult(b,c,d);
    else if( hb + hc + he + h >= n)
        return new ReadSpareResult(b,c,e);
    else if( hb + hc + hf + h >= n)
        return new ReadSpareResult(b,c,f);

    else if( hb + hd + he + h >= n)
        return new ReadSpareResult(b,d,e);
    else if( hb + hd + hf + h >= n)
        return new ReadSpareResult(b,d,f);

    
    else if( hb + he + hf + h >= n)
        return new ReadSpareResult(b,e,f);


    
    
    else if( hc + hd + he + h >= n)
        return new ReadSpareResult(c,d,e);
    else if( hc + hd + hf + h >= n)
        return new ReadSpareResult(c,d,f);
    
    else if( hc + he + hf + h >= n)
        return new ReadSpareResult(c,e,f);

       
    
    else if( hd + he + hf + h >= n)
        return new ReadSpareResult(d,e,f);
    
    
    return new ReadSpareResult(new BSTNode(-10));
    
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, BSTNode c, BSTNode d,BSTNode e, int h, int n)
{ 
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
    int he = e.height+1;
     
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);
    else if ( ha + hc + h >= n)
        return new ReadSpareResult(a,c);
    else if ( ha + hd + h >= n)
        return new ReadSpareResult(a,d);
    else if ( ha + he + h >= n)
        return new ReadSpareResult(a,e);
    else if ( hb + hc + h >= n)
        return new ReadSpareResult(b,c);
    else if ( hb + hd + h >= n)
        return new ReadSpareResult(b,d);
    else if ( hb + he + h >= n)
        return new ReadSpareResult(b,e);
    else if ( hc + hd + h >= n)
        return new ReadSpareResult(c,d);
    else if ( hc + he + h >= n)
        return new ReadSpareResult(c,e);
    else if ( hd + he + h >= n)
        return new ReadSpareResult(d,e);
    
    //devo prendere ancora dei nodi in più
    else if( ha + hb + hc + h >= n)
        return new ReadSpareResult(a,b,c);
    else if ( ha + hb + hd + h >= n)
        return new ReadSpareResult(a,b,d);
    else if ( ha + hb +  he + h >= n)
        return new ReadSpareResult(a,b,e);
    else if ( ha + hc + hd + h >= n)
        return new ReadSpareResult(a,c,d);
    else if ( ha + hc + he + h >= n)
        return new ReadSpareResult(a,c,e);
    else if ( hb + hc + hd + h >= n)
        return new ReadSpareResult(b,c,d);
    else if ( hb + hc + he + h >= n)
        return new ReadSpareResult(b,c,e);
    else if ( hb + hd + he + h >= n)
        return new ReadSpareResult(b,d,e);
    else if ( hc + hd + he + h >= n)
        return new ReadSpareResult(c,d,e);

    return new ReadSpareResult(new BSTNode(-9));
    
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, BSTNode c, BSTNode d, int h, int n)
{ 
    int ha = a.height+1;
    int hb = b.height+1;
    int hc = c.height+1;
    int hd = d.height+1;
     
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);
    else if ( ha + hc + h >= n)
        return new ReadSpareResult(a,c);
    else if ( ha + hd + h >= n)
        return new ReadSpareResult(a,d);
    else if ( hb + hc + h >= n)
        return new ReadSpareResult(b,c);
    else if ( hb + hd + h >= n)
        return new ReadSpareResult(b,d);
    else if ( hc + hd + h >= n)
        return new ReadSpareResult(c,d);
    
    //devo prendere ancora dei nodi in più
    else if ( ha + hb + hc + h >= n)
        return new ReadSpareResult(a,b,c);
    else if ( ha + hc + hd + h >= n)
        return new ReadSpareResult(a,c,d);
    else if ( hb + hc + hd + h >= n)
        return new ReadSpareResult(b,c,d);

    return new ReadSpareResult(new BSTNode(-10));
        
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, BSTNode c, int h, int n)
{ 
    int ha = a.height+1;
    int hb= b.height+1;
    int hc = c.height+1;
     
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);
    else if ( ha + hc + h >= n)
        return new ReadSpareResult(a,c);
    else if ( hb + hc + h >= n)
        return new ReadSpareResult(b,c);
    
    //devo prendere ancora dei nodi in più
    if( ha + hb + hc + h >= n)
        return new ReadSpareResult(a,b,c);

    return new ReadSpareResult(new BSTNode(-11));
        
}

public ReadSpareResult chooseBrother(BSTNode a, BSTNode b, int h, int n)
{ 
    int ha = a.height+1;
    int hb= b.height+1;
     
    //skippo un po di casi per ragioni di tempo (ora è deterministico nel caso funziona devo aggiungere casualità)
    if( ha + hb + h >= n)
        return new ReadSpareResult(a,b);

    return new ReadSpareResult(new BSTNode(-12));
        
}

public ReadSpareResult randSpare5(BSTNode x, BSTNode y, BSTNode z, BSTNode w, BSTNode q)
{ 
    Float f = (float) 1/5;
    Float rand = (float) Math.random();
 
    if(rand <= f)
        return new ReadSpareResult(x);
    else if (rand > f && rand <= 2*f)
        return new ReadSpareResult(y);
    else if (rand > 2*f && rand <= 3*f)
        return new ReadSpareResult(z);
    else if (rand > 3*f && rand <= 4*f)
        return new ReadSpareResult(w);
    else return new ReadSpareResult(q);    
}

public ReadSpareResult randSpare4(BSTNode x, BSTNode y, BSTNode z, BSTNode w)
{ 
    Float f = (float) 1/4;
    Float rand = (float) Math.random();
 
    if(rand <= f)
        return new ReadSpareResult(x);
    else if (rand > f && rand <= 2*f)
        return new ReadSpareResult(y);
    else if (rand > 2*f && rand <= 3*f)
        return new ReadSpareResult(z);
    else return new ReadSpareResult(w);    
}

public ReadSpareResult randSpare3(BSTNode x, BSTNode y, BSTNode z)
{ 
    Float f = (float) 1/3;
    Float rand = (float) Math.random();
 
    if(rand <= f)
        return new ReadSpareResult(x);
    else if (rand > f && rand <= 2*f)
        return new ReadSpareResult(y);
    else return new ReadSpareResult(z);
    
}

public ReadSpareResult randSpare2(BSTNode x, BSTNode y)
{
    Float f = (float) 1/2;
    Float rand = (float) Math.random();
 
    if(rand <= f)
        return new ReadSpareResult(x);
    else 
        return new ReadSpareResult(y);
    
}

}