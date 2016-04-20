package core;

public class ContinuePathResult {
    
    public String path;
    public int numSpare;
    public BSTNode[] localNode;
    public BSTNode target;
    public BSTNode s;
    public int localCont;
    public int camT;
    public int nBlock;
    public BSTNode b;
    public BSTNode c;
    
    public ContinuePathResult(BSTNode w, BSTNode s, BSTNode s2, BSTNode s3, String path, int numSpare, BSTNode[] localNode, int localCont, int camT, int nBlock)
    {
        this.target = w;
        this.s = s;
        this.path = path;
        this.numSpare = numSpare;
        this.localNode = localNode;
        this.localCont = localCont;
        this.camT = camT;
        this.nBlock = nBlock;
        this.b = s2;
        this.c = s3;
    }

}
