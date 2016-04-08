package core;

import core.BSTNode;

public class BST {
        
    protected BSTNode root;
 
    public BSTNode getRoot() {
        return root;
    }

    public void setRoot(BSTNode root) {
        this.root = root;
    }
        
    public void insert(int x) {
        new BSTInsert(this, new BSTNode(x));
    }

    public String access(BST t, int numnode, int key, int level) {
        BSTAccess acc = new BSTAccess();
        return acc.BSTAccessRun(t, numnode, key, level);
    }
        
    /*
     * Rotation is specified by a single vertex v; if v is a left child of its
     * parent, rotate right, if it is a right child, rotate lef This method also
     * recalculates positions of all nodes and their statistics.
     */
    
    public void rotate(BSTNode v) {
        if (v.isLeft()) {
            rightrot(v);
        } else {
            leftrot(v);
        }
        if (v.getLeft() != null) {
            v.getLeft().calc();
                    }
        if (v.getRight() != null) {
            v.getRight().calc();
        }
        v.calc();
    }
    
    protected void leftrot(BSTNode v) {
        final BSTNode u = v.getParent();
        if (v.getLeft() == null) {
            u.unlinkRight();
        } else {
            u.linkRight(v.getLeft());
        }
        if (u.isRoot()) {
            setRoot(v);
        } else {
            if (u.isLeft()) {
                u.getParent().linkLeft(v);
            } else {
                u.getParent().linkRight(v);
            }
        }
        v.linkLeft(u);
    }

    protected void rightrot(BSTNode v) {
        final BSTNode u = v.getParent();
        if (v.getRight() == null) {
            u.unlinkLeft();
        } else {
            u.linkLeft(v.getRight());
        }
        if (u.isRoot()) {
            setRoot(v);
        } else {
            if (u.isLeft()) {
                u.getParent().linkLeft(v);
            } else {
                u.getParent().linkRight(v);
            }
        }
        v.linkRight(u);
    }
    
    
}
