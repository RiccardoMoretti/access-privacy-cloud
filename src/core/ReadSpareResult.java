package core;

public class ReadSpareResult {
    
    BSTNode a = null;
    BSTNode b = null;
    BSTNode c = null;
    BSTNode d = null;
    
    
    public ReadSpareResult(BSTNode a, BSTNode b, BSTNode c, BSTNode d )
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d; 
    }
    
    public ReadSpareResult(BSTNode a, BSTNode b, BSTNode c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    public ReadSpareResult(BSTNode a, BSTNode b)
    {
        this.a = a;
        this.b = b;
    }
    
    public ReadSpareResult(BSTNode a)
    {
        this.a = a;      
    }
    

}
