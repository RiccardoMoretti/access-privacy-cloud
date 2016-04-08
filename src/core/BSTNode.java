package core;

import java.util.Vector;

public class BSTNode{
    private BSTNode left = null, right = null, parent = null;
    private int level; 
    protected int key;  
 
 // statistics
    public int size = 1, height = 1, sumh = 1;   
    public int hsx = 0, hdx = 0, delta = 0;

    public BSTNode(BSTNode x) {
        this.key = x.getKey();
    }
    
    public BSTNode(int k) {
        this.key = k;
    }
        
    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }


    public BSTNode getLeft() {
            return left;
    }

    public void setLeft(BSTNode left) {
        this.left = left;
    }

    public BSTNode getRight() {
            return right;
    }

    public BSTNode setRight(BSTNode right) {
        this.right = right;
        return right;
    }

    public BSTNode getParent() {
        return parent;
    }

    public BSTNode setParent(BSTNode parent) {
        return this.parent = parent;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isLeaf() {
        return getLeft() == null && getRight() == null;
    }

    public boolean isLeft() {
        return getParent() != null && getParent().getLeft() == this;
    }

    /**
     * removes edge between this and left; removes edge between newLeft and its
     * parent; creates new edge between this and newLeft
     */
    public void linkLeft(BSTNode newLeft) {
        if (getLeft() != newLeft) {
            if (getLeft() != null) {
                // remove edge between this and left
                unlinkLeft();
            }
            if (newLeft != null) {
                if (newLeft.getParent() != null) {
                    // remove edge between newLeft and its parent
                    newLeft.unlinkParent();
                }
                // create new edge between this and newLeft
                newLeft.setParent(this);
            }
            setLeft(newLeft);
        }
    }

    /**
     * removes edge between this and left
     */
    public void unlinkLeft() {
        getLeft().setParent(null);
        setLeft(null);
    }

    /**
     * removes edge between this and right; removes edge between newRight and
     * its parent; creates new edge between this and newRight
     */
    public void linkRight(BSTNode newRight) {
        if (getRight() != newRight) {
            if (getRight() != null) {
                // remove edge between this and right
                unlinkRight();
            }
            if (newRight != null) {
                if (newRight.getParent() != null) {
                    // remove edge between newRight and its parent
                    newRight.unlinkParent();
                }
                // create new edge between this and newRight
                newRight.setParent(this);
            }
            setRight(newRight);
        }
    }

    /**
     * removes edge between this and right
     */
    public void unlinkRight() {
        getRight().setParent(null);
        setRight(null);
    }

    private void unlinkParent() {
        if (isLeft()) {
            getParent().unlinkLeft();
        } else {
            getParent().unlinkRight();
        }
    }

    /**
     * Calculate the height, size, and sum of heights of this node, assuming
     * that this was already calculated for its children.
     */
    public void calc() {
        int ls = 0, rs = 0, lh = 0, rh = 0, lsh = 0, rsh = 0;
        if (getLeft() != null) {
            ls = getLeft().size;
            lh = getLeft().height;
            lsh = getLeft().sumh;
            hsx = Math.max(getLeft().hsx, getLeft().hdx) + 1;
        }
        if (getRight() != null) {
            rs = getRight().size;
            rh = getRight().height;
            rsh = getRight().sumh;
            hdx = Math.max(getRight().hsx, getRight().hdx) + 1;
                  
        }
        size = ls + rs + 1;
        height = Math.max(lh, rh) + 1;
        sumh = lsh + rsh + size;
        if (hdx > hsx)
            delta = hdx - hsx;
        if (hsx > hdx)
            delta = hsx - hdx;
        if (hsx == hdx)
            delta = 0;
            
    }

    /**
     * Calculate the height for all the nodes in this subtree (recursively bottom-up).
     */
    public void calcTree() {
        for (BSTNode v : postorder()) {
            v.calc();
        }
    }


    private void _postorder(Vector<BSTNode> acc) {
        if (getLeft() != null) {
            getLeft()._postorder(acc);
        }
        if (getRight() != null) {
            getRight()._postorder(acc);
        }
        acc.add(this);
    }


    public Vector<BSTNode> postorder() {
        Vector<BSTNode> acc = new Vector<BSTNode>();
        this._postorder(acc);
        return acc;
    }


}
    


