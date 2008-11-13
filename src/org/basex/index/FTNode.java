package org.basex.index;

import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * XPath Value representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNode {
  /** Fulltext data for a node. */
  private IntList ip;
  /** Pre value of the current node. */
  private int pre;
  /** Pointer for idpos - each idpos has a pointer at
   * its search string position in the xpath query.
   * poi[0] = max. max pointer value in poi */
  public IntList p;
  /** Counter for pos values. */
  private int c = 0;
  /** Flag for negative node. */
  public boolean not;
  /** List for tokens from query. */
  private FTTokenizer[] tok;
  /** Number of stored values.. */
  public int size;

  /**
   * Constructor.
   */
  public FTNode() {
    ip = new IntList();
  }

  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer pointer on query tokens
   */
  public FTNode(final int[] idpos, final int[] pointer) {
    ip = new IntList(idpos);
    p = new IntList(pointer);
    size = idpos.length;
  }

  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer initial pointer on query token
   */
  FTNode(final int[] idpos, final int pointer) {
    ip = new IntList(idpos);
    genPointer(pointer);
    size = 1;
  }

  /**
   * Constructor.
   * @param prevalue pre value of the current node
   */
  FTNode(final int prevalue) {
    pre = prevalue;
    ip = new IntList();
    ip.add(prevalue);
    size = 1;
  }

  /**
   * Generates pointer with value v.
   * @param v value
   */
  private void genPointer(final int v) {
    final int[] t = new int[ip.size];
    for (int i = 0; i < t.length; i++) t[i] = v;
    p = new IntList(t);
  }

  /**
   * Getter for the prevalue.
   * @return pre value
   */
  public int getPre() {
    return ip != null ? ip.list[0] : pre;
  }

  /**
   * Test is any pos value is remaining.
   * @return boolean
   */
  public boolean morePos() {
    return ++c < ip.size;
  }

  /**
   * Reset position iterator.
   */
  public void reset() {
    c = 0;
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
   * Convert all tokens to one byte[].
   *
   * @return byte[] tokens
   * 
   */
  public byte[] getTokens() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < tok.length; i++) {
      sb.append(tok[i].text);
      if (i < tok.length - 1) sb.append(" ");
    }
    return sb.toString().getBytes();
  }
  
  /**
   * Get next pos value.
   * @return pos value
   */
  public int nextPos() {
    return ip.list[c];
  }

  /**
   * Get number of tokens from query for this node.
   * @return number of tokens
   */
  public int getNumTokens() {
    return p.list[0];
  }

  /**
   * Merges n to the current FTNode.
   * Pointer are node updated.
   * @param n ftnode to be merged
   * @param w distance between the pos values
   * @return boolean
   */
  public boolean merge(final FTNode n, final int w) {
    if (not != n.not) return false;

    boolean mp = morePos();
    boolean nmp = n.morePos();
    if (!(mp && nmp) || getPre() != n.getPre()) return false;

    final IntList il = new IntList();
    final IntList pn = p != null ? initNewPointer(n.p) : null;
    il.add(getPre());
    while(mp && nmp) {
      final int d = nextPos() - n.nextPos() + w;
      if (d == 0) {
        add(il, pn);
        if (w > 0) n.add(il, pn);
        mp = morePos();
        nmp = n.morePos();
      } else if (d < 0) {
        if (w == 0) add(il, pn);
        mp = morePos();
      } else {
        if (w == 0) n.add(il, pn);
        nmp = n.morePos();
      }
    }
    if (w == 0) {
      while(mp) {
        add(il, pn);
        mp = morePos();
      }
      while(nmp) {
        n.add(il, pn);
        nmp = n.morePos();
      }
    }

    ip = new IntList(il.finish());
    if (tok != null) {
      final FTTokenizer[] tmp = new FTTokenizer[tok.length + n.tok.length];
      System.arraycopy(tok, 0, tmp, 0, tok.length);
      System.arraycopy(n.tok, 0, tmp, tok.length, n.tok.length);
      tok = tmp;
    }
    
    if (tok != null && n.tok != null) {
        FTTokenizer[] ntok = new FTTokenizer[tok.length + n.tok.length];
        Array.copy(tok, ntok, 0);
        Array.copy(n.tok, ntok, tok.length);
    } else {
      tok = null;
    }   
    
    p = (p != null) ? new IntList(pn.finish()) : null;
    return ip.size > 1;
  }

  /**
   * Initialize pointer list.
   * @param n IntList
   * @return IntList
   */
  private IntList initNewPointer(final IntList n) {
    final IntList il = new IntList();
    il.add(p.list[0] > n.list[0] ? p.list[0] : n.list[0]);
    return il;
  }

  /**
   * Adds node to il and ns pointer to il.
   * @param il IntList takes the new node
   * @param pn IntList with pointers
   */
  private void add(final IntList il, final IntList pn) {
    il.add(nextPos());
    if(pn != null) pn.add(nextPoi());
  }

  /**
   * Converts the pos values in the following style.
   * [poi0]: pos0, ..., posk
   * [poi1]: pos0, ..., posj
   * @return IntList[]
   */
  public IntList[] convertPos() {
    final IntList[] il = new IntList[p.list[0]];
    for (int k = 0; k < il.length; k++) il[k] = new IntList();
    c = 0;
    while(morePos()) {
      il[nextPoi() - 1].add(nextPos());
    }
    return il;
  }

  /**
   * Get next pointer.
   * @return next pointer
   */
  public int nextPoi() {
    return p.list[c];
  }

  /**
   * Returns the complete ftnode.
   * @return current ftnode
   */
  public int[] getFTNode() {
    return ip.finish();
  }

  @Override
  public String toString() {
    return "FTNode [Pre=" + getPre() + "; Pos="
    + Array.toString(ip.finish(), 0, ip.size)
    + "; Poi=" + Array.toString(p.finish(), 0, p.size) + "]";
  }
}
