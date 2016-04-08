package core;

import java.util.Arrays;
import java.util.Random;

public class BSTAccess{

private BST T;

private BSTNode w;
private BSTNode target;

private double numBlock;

private int localNodeCont;
private int pathTargetCont;
private int spareNodeCont;
private int plusNodeCont;
private int numberOfNode;
private int nodeToTarget;
private int level;
private int K;

private String path;
private String pathT;

public String BSTAccessRun(BST T, int numberOfNode, int key, int level) {
    this.T = T;
    this.K = key;
    this.numberOfNode = numberOfNode;
    this.numBlock = (2* Math.ceil(Math.log(numberOfNode) / Math.log(2)));
    this.level = level;  
    this.path = ""; 
    this.pathT = "";
    this.nodeToTarget = 0;
    
    //inizio accesso, parto leggendo la root

    //w contiene il nodo del cammino verso il target attualmente in esame/considerato
        w = T.getRoot();
                    
    //inizio la ricerca, mi sposto a destra o a sinistra nell'albero fino a quando non trovo il nodo con key = k (nodo target)
    while (true) {
                    
            if (w.getKey() == K) { //trovato il target, w è il mio target
                
                target = w;
                     
                //pathT contiene il part percorso per raggiungere il target
                pathT = path;
                
                //se il nodo target non è una foglia, percorro un cammino casuale fino ad arrivare ad una foglia
                if(!w.isLeaf())  
                    path = continuePath(w, path);
                                
                //ruoto fino a quando il target non diventa la root
                moveUpTarget(target,level);
                                
                //una volta trovato il target e spostato fino a root, vengono eseguite alcune rotazioni possibili con i nodi che si hanno in casa
                //al fine di ribilanciare (un pochino, non troppo) l'albero..impedendogli cosi di superare l'altezza massima prefissata
                //T = balanceTree(T, localNode, localNodeCont);
                                
                break; 
                              
            //il target è a destra del nodo w che sto considerando ora, cammino proseguendo verso destra
            } else if (w.getKey() < K) {

                                      
                //proseguo camminando verso destra 
                if (w.getRight() != null) {                     
                    w = w.getRight(); 
                    path = path.concat("1"); 
                    nodeToTarget++;
                } 
                
            } 
                        
            //il target è a sinistra del nodo w che sto considerando ora, cammino proseguendo verso sinistra
            else {        
                                                        
             //proseguo camminando verso sinistra 
               if (w.getLeft() != null) {                   
                   w = w.getLeft();
                   path = path.concat("0");
                   nodeToTarget++;
               } 

            }                     
      }  
            
    //System.out.println("Target:"+K+"\tPathTarget"+pathT+"\tPATH:"+path);
    
    //ritorno il cammino percorso che è una sequenza di 0 e 1 (o sinistra, 1 destra)
    return path;
          
    } 
     

//il nodo target in questo caso è un nodo interno, dopo averlo letto viene percorso un cammino casuale fino a raggiungere  un nodo senza figli
private String continuePath(BSTNode w, String path)
{
  //w è il nodo target
  //se il target non è una foglia, procedo casualmente fino a raggiungerne una ed aggiungo la direzione alla stringa path
  while(!w.isLeaf())
  {
      //il target ha un solo figlio (sinistro), scelta obbligata! Cammino verso sinistra
      if(w.getRight() == null)
      {           
          w = w.getLeft();
          path = path.concat("0");
          }
    //il target ha un solo figlio (destro), scelta obbligata! Cammino verso destra
      else if (w.getLeft() == null)
          {    
              w = w.getRight();
              path = path.concat("1");
          }
      //il target ha due figli, scelgo casualmente da che parte andare
      else if(Math.random() < 0.5)
              {   
                  w = w.getRight();
                  path = path.concat("1"); 
               }        
            else
              {
                  w = w.getLeft();
                  path = path.concat("0"); 
              }     
           
  }//fine ciclo while che procede random fino alle foglie dopo aver trovato il target (nel caso lui stesso non sia una foglia)

  return path;
  
}


//esegue rotazioni fino a quando il nodo target passato in input diventa la radice dell'albero
private void moveUpTarget(BSTNode target, int level)
{
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

}


//++++++++++++++++++++++++++++++++++++ CODICE IN FASE DI IMPLEMENTAZIONE ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//dopo aver letto il target esso viene spostato in alto nell'albero in maniera forzata, questo può causare un'eccessivo sbilanciamento
//dell'albero che viene "ristemato" utilizzando questa funzione
private BST balanceTree(BST T, BSTNode[] local, int localNodeCont)
{
    BSTNode[] ableRotate = new BSTNode[localNodeCont];
    int contAble = 0;
    
    //visto che ho ruotato, prima di tutto devo aggiornare i dati (h) dei nodi letti che ho nell'array localNode (basta ricercarli tutti e risalvarli)
    for ( int i = 0; i < localNodeCont; i++ )
        local[i] = findNode(T, local[i]);
        
        //riordino i nodi nell'array facendo si che man mano che procedo nell'array salgo lungo l'albero  (primi blocchi sono le foglie ecc) 
        //reorder(T, local, localNodeCont);
        
        //individuo quali nodi sono in grado di ruotare basandomi solo su quelli che ho in casa (solo quelli che mi migliorano la situazione)                
        //for (int i = 0; i < localNodeCont; i++ )    
        //   ableRotate[i] = canRotate(local[i]);
        
        //ableRotate mi serve per sapere quali sono i nodi che posso ruotare e che mi migliorano l'altezza
        //prendo questi nodi uno per uno e scelgo casualmente se ruotarli o no
       
    
        //ricalcolo statistiche albero
        T.getRoot().calcTree();
    
    //ritorno l'albero risistemato    
    return T;
}

//trova un nodo in un BST data la sua chiave e lo restituisce
private BSTNode findNode(BST T, BSTNode tofind)
{
    BSTNode w = T.getRoot();
    
    while (true) 
    {                    
            if (w.getKey() == tofind.getKey())                
                   break;            
            else if (w.getKey() < tofind.getKey())                     
                   w = w.getRight(); 
            else           
                   w = w.getLeft();
    }
    return w;   
}




}