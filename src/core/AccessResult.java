package core;

public class AccessResult {
    
    public BST T;
    public int[] logical;
    public int[] phisical;
    public String path;
    
    public AccessResult(BST t, int[] l, int[] p, String path)
    {
        this.T = t;
        this.logical = l;
        this.phisical = p;
        this.path = path;
    }

}
