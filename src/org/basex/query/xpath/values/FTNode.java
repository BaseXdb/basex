package org.basex.query.xpath.values;

import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * XPath Value representing a full-text Node.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNode extends NodeSet {
  /** Fulltext data for a node. */
  private IntList ip = null;
  /** Pre value of the current node. */
  private int pre;
  /** Pointer for idpos - each idpos has a pointer at 
   * its search string position in the xpath query. 
   * poi[0] = max. max pointer value in poi*/
  public IntList p;
  /** Counter for pos values. */
  public int c = 0;
  /** Flag for negative node. */
  public boolean not = false;
  /** List for tokens from query. */
  private FTTokenizer[] tok;
  
  /**
   * Constructur.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer pointer on query tokens
   */
  public FTNode(final int[] idpos, final int[] pointer) {
    ip = new IntList(idpos);
    p = new IntList(pointer);
    size = idpos.length;
  }

  /**
   * Constructur.
   * @param idpos ftdata, pre, pos1, ..., posn
   */
  public FTNode(final int[] idpos) {
    ip = new IntList(idpos);
    size = 1;
  }

  
  /**
   * Constructur.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer initial pointer on query token
   */
  public FTNode(final int[] idpos, final int pointer) {
    ip = new IntList(idpos);
    genPointer(pointer); 
    size = 1;
  }

  /**
   * Constructor.
   * @param prevalue pre value of the current node
   */
  public FTNode(final int prevalue) {
    pre = prevalue;
    ip = new IntList(1);
    ip.add(pre);
    size = 1;
  }

  /**
   * Constructor.
   */
  public FTNode() {
    ip = new IntList(0);
    size = 0;
  }

  
  
  /**
   * Generatates pointer with value v.
   * @param v value
   */
  public void genPointer(final int v) {
    int[] t = new int[ip.size];
    for (int i = 0; i < t.length; i++) t[i] = v;
    p = new IntList(t);
  }
  
  /**
   * Getter for the prevalue.
   * @return pre value
   */
  public int getPre() {
    if (ip != null) return ip.get(0);
    return pre;
  }
  
  /**
   * Test is any pos value is remaining.
   * @return boolean
   */
  public boolean morePos() {
    ++c;
    return c < ip.size;
  }
  
  /**
   * Setter for FTTokenizer.
   * @param token FTTokenizer
   */
  public void setToken(final FTTokenizer[] token) {
    tok = token;
  }
  
  /**
   * Getter for the FTTokenizer.
   * @return FTTokenizer
   */
  public FTTokenizer[] getToken() {
    return tok;
  }
  
  /**
   * Get next pos value.
   * @return pos value
   */
  public int nextPos() {
    return ip.get(c);
  }
  
  /**
   * Get number of tokens from query for this node.
   * @return number of tokens
   */
  public int getNumTokens() {
    return p.get(0);
  }
  
  /**
   * Merges n to the current FTNode.
   * Pointer are node updated.
   * @param n ftnode to be merged
   * @param w distance between the pos values 
   * @return boolean 
   */
  public boolean merge(final FTNode n, final int w) {
    // merge only equal ftnodes
    if (not != n.not) return false;
    
    //not = not || n.not;
    
    boolean mp = morePos();
    boolean nmp = n.morePos();
    if (getPre() != n.getPre() || 
        !(mp && nmp)) return false;
    IntList il = new IntList(size() + n.size());
    IntList pn = (p != null) ? 
        initNewPointer(size() + n.size() + 1, n.p) : null;
    il.add(getPre());
    int d;
    //while(c < size() && n.c < n.size()) {
    while(mp && nmp) {
      d = nextPos() - n.nextPos() + w;
      if (d == 0) {
        add(this, il, pn); //il.add(nextPos());
        if (w > 0) add(n, il, pn); //il.add(n.nextPos());
        mp = morePos();
        nmp = n.morePos();
      } else if (d < 0) {
        if (w == 0) add(this, il, pn); //il.add(nextPos());
        mp = morePos();
      } else {
        if (w == 0) add(n, il, pn); //il.add(n.nextPos());
        nmp = n.morePos();
      }
    }
    //if (w == 0) while(morePos()) add(this, il, pn); //il.add(nextPos());
    //if (w == 0) while(n.morePos()) add(n, il, pn); //il.add(n.nextPos());
    if (w == 0) while(mp) {
      add(this, il, pn); //il.add(nextPos());
      mp = morePos();
    }
    if (w == 0) while(nmp) {
      add(n, il, pn); //il.add(n.nextPos());
      nmp = n.morePos();
    }
    
    ip = new IntList(il.finish());
    if (tok != null) {
      FTTokenizer[] tmp = new FTTokenizer[tok.length + n.tok.length];
      System.arraycopy(tok, 0, tmp, 0, tok.length);
      System.arraycopy(n.tok, 0, tmp, tok.length, n.tok.length);
      tok = tmp;
    }
    p = (p != null) ? new IntList(pn.finish()) : null;
    return ip.size > 1; 
  }
 
  /**
   * Checks if current node and n are a phrase with distance w.
   * @param n second node
   * @param w distance to first node
   * @return boolean phrase
   */
  public boolean phrase(final FTNode n, final int w) {
    boolean mp = morePos();
    boolean nmp = n.morePos();
    if (getPre() != n.getPre() || 
        !(mp && nmp)) return false;
    IntList il = new IntList(size());
    IntList pn = (p != null) ? 
        initNewPointer(size() + 1, n.p) : null;
    il.add(getPre());
    int d;
    while(mp && nmp) {
      d = nextPos() - n.nextPos() + w;
      if (d == 0) {
        add(this, il, pn); 
        mp = morePos();
        nmp = n.morePos();
      } else if (d < 0) {
        mp = morePos();
      } else {
        nmp = n.morePos();
      }
    }
    
    ip = new IntList(il.finish());
    if (tok != null) {
      FTTokenizer[] tmp = new FTTokenizer[tok.length + n.tok.length];
      System.arraycopy(tok, 0, tmp, 0, tok.length);
      System.arraycopy(n.tok, 0, tmp, tok.length, n.tok.length);
      tok = tmp;
    }
    p = (p != null) ? new IntList(pn.finish()) : null;
    return ip.size > 1; 
  }
  
  /**
   * Init pointer list.
   * @param s size of pointer to add
   * @param n IntList
   * @return IntList
   */
  private IntList initNewPointer(final int s, final IntList n) {
    IntList i = new IntList(s);
    i.add((p.get(0) > n.get(0)) ? p.get(0) : n.get(0));
    return i;
  }
  
  /**
   * Add node n to il and ns pointer to il.
   * @param n node to add
   * @param il IntList takes the new node
   * @param pn IntList with pointers
   */
  private void add(final FTNode n, final IntList il, final IntList pn) {
    il.add(n.nextPos());
    if (pn != null) {
      pn.add(n.nextPoi());
    }
  }
  
  /**
   * Converts the pos values in the following style.
   * poi0, pos0, ..., posk
   * poi1, pos0, ..., posj
   * 
   * @return IntList[]
   */
  public IntList[] convertPos() {
    IntList[] il = new IntList[p.get(0)];
    for (int k = 0; k < il.length; k++) il[k] = new IntList();
    c = 0;
    while(morePos()) {
      il[nextPoi() - 1].add(nextPos());
      //il[p.get(i++) - 1].add(nextPos());
    }
    return il;
  }
  
  /**
   * Get next pointer.
   * @return next pointer
   */
  public int nextPoi() {
    return p.get(c);
  }
  
  @Override
  public int size() {
    return ip.size;
  }

  /**
   * Returns the complete ftnode.
   * 
   * @return current ftnode
   */
  public int[] getFTNode() {
    return ip.finish();
  }

  @Override
  public FTNode eval(final XPContext ctx) {
    return this;
  }

  @Override
  public String toString() {
    return "FTNode [Pre=" + getPre() + "; Pos=" 
    + Array.intArrayToString(1, ip.finish(), ip.size) 
    + "; Poi=" + Array.intArrayToString(0, p.finish(), p.size) + "]";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token("size"), Token.token(size));
  }
}
