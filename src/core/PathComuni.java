package core;

public class PathComuni {
    
    public String path =" ";
    public int target = 0;
   
    
    public PathComuni()
    {
        this.target = 0;
        this.path = "";
    }
    
    public PathComuni(int t, String p)
    {
        this.target = t;
        this.path = p;
    }

    public void setTarget(int t)
    {
        this.target = t;
    }
    
    public void setPath(String p)
    {
        this.path = p;
    }
}
