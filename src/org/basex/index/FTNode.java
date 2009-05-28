package org.basex.index;

import org.basex.ft.Tokenizer;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * Full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTNode {
  /** Full-text data for a node (pre, pos1, pos2, ...). */
  public IntList ip;
  /** Pointer for idpos - each idpos has a pointer at
   * its search string position in the query.
   * poi[0] = max. pointer value in poi */
  public IntList p;
  /** Flag for negative node. */
  public boolean not;

  /** Pre value of the current node. */
  private int pre;
  /** Counter for pos values. */
  private int c;
  /** List for tokens from query. */
  private Tokenizer[] tok;

  /**
   * Constructor.
   */
  public FTNode() {
    ip = new IntList();
  }

  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   */
  public FTNode(final IntList idpos) {
    ip = idpos;
  }

  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer pointer on query tokens
   */
  public FTNode(final int[] idpos, final int[] pointer) {
    ip = new IntList(idpos);
    p = new IntList(pointer);
  }

  /**
   * Returns true if no values are stored for this node.
   * @return result of check
   */
  public boolean empty() {
    return ip.size == 0;
  }
  
  /**
   * Generates pointer with value v.
   * @param v value
   */
  void genPointer(final int v) {
    if(p != null && p.size > 0 && p.list[0] == v) return;
    final int[] t = new int[ip.size];
    for (int i = 0; i < t.length; i++) t[i] = v;
    p = new IntList(t);
  }

  /**
   * Getter for the pre value.
   * @return pre value
   */
  public int pre() {
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
   * Sets the current tokenizer.
   * @param token tokenizer
   */
  public void setToken(final Tokenizer[] token) {
    tok = token;
  }

  /**
   * Returns the current tokenizer.
   * @return tokenizer
   */
  public Tokenizer[] getToken() {
    return tok;
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
  public boolean union(final FTNode n, final int w) {
    if(not != n.not) return false;

    boolean mp = morePos();
    boolean nmp = n.morePos();
    if(!(mp && nmp) || pre() != n.pre()) return false;

    final IntList il = new IntList();
    final IntList pn = p != null ? initNewPointer(n.p) : null;
    il.add(pre());
    while(mp && nmp) {
      final int d = nextPos() - n.nextPos() + w;
      if(d == 0) {
        add(il, pn);
        if(w > 0) n.add(il, pn);
        mp = morePos();
        nmp = n.morePos();
      } else if(d < 0) {
        if(w == 0) add(il, pn);
        mp = morePos();
      } else {
        if(w == 0) n.add(il, pn);
        nmp = n.morePos();
      }
    }
    if(w == 0) {
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

    // [SG] when is tok/n.tok null?
    if(tok != null) {
      final Tokenizer[] tmp = new Tokenizer[tok.length + n.tok.length];
      Array.copy(tok, tmp, 0);
      Array.copy(n.tok, tmp, tok.length);
      tok = tmp;
    }

    if(tok != null && n.tok != null) {
      // [SG] ntok is not stored here..
      final Tokenizer[] ntok = new Tokenizer[tok.length + n.tok.length];
      Array.copy(tok, ntok, 0);
      Array.copy(n.tok, ntok, tok.length);
    } else {
      tok = null;
    }

    c = 0;
    if(p != null) p = new IntList(pn.finish());
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
    if(p == null) return new IntList[0];
    final IntList[] il = new IntList[p.list[0]];
    for(int k = 0; k < il.length; k++) il[k] = new IntList();
    c = 0;
    while(morePos()) il[nextPoi() - 1].add(nextPos());
    return il;
  }

  /**
   * Get next pointer.
   * @return next pointer
   */
  public int nextPoi() {
    return p.list[c];
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + "[");
    if(tok != null) for(final Tokenizer t : tok) sb.append(t + " ");
    else sb.append("-");
    return sb.append("]").toString();
  }
}
