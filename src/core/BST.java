package core;

import java.io.IOException;

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

    public AccessResult access(BST t, int numnode, int key, int level) throws IOException {
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
    
    public void rotateBIS(BSTNode v) {

      //  System.out.println(v.phiAdd+"\t"+v.getParent().phiAdd);
        int temp = v.phiAdd;
        v.phiAdd = v.getParent().phiAdd;
        v.getParent().phiAdd = temp;
       // System.out.println(v.phiAdd+"\t"+v.getParent().phiAdd);
       // System.out.println();
        
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
        }
         else {
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
