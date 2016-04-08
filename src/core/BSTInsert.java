package core;

public class BSTInsert {
    private final BST T;
    private final int K;
    private final BSTNode v;

    public BSTInsert(BST T, BSTNode v) {
        this.T = T;
        this.v = v;
        K = v.getKey();
    }

    
    public void insert() {
        if(this.K != -1)
        {
        if (T.getRoot() == null) {
            T.setRoot(v);
        } else {
            BSTNode w = T.getRoot();
            while (true) {
                if (w.getKey() == K) {
                    return;
                } else if (w.getKey() < K) {
                    if (w.getRight() != null) {
                        w = w.getRight();
                    } else {
                        w.linkRight(v);
                        break;
                    }
                } else {
                    if (w.getLeft() != null) {
                        w = w.getLeft();
                    } else {
                        w.linkLeft(v);
                        break;
                    }
                }
            }
        }
        }
    }
    


}
