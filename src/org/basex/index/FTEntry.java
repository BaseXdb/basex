package org.basex.index;

import org.basex.ft.Tokenizer;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * This class contains position data for single full-text index entries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTEntry {
  /** Full-text data for a node (pre, pos1, pos2, ...). */
  public IntList pos;
  /** Pointer for idpos - each idpos has a pointer at
   * its search string position in the query.
   * poi[0] = max. pointer value in poi */
  public IntList poi;
  /** Flag for negative node. */
  public boolean not;

  /** Counter for pos values. */
  private int c;
  /** List for tokens from query. */
  private Tokenizer[] tok;

  /**
   * Constructor.
   */
  public FTEntry() {
    pos = new IntList(1);
  }

  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer pointer on query tokens
   * @param token tokenizers
   */
  public FTEntry(final IntList idpos, final IntList pointer,
      final Tokenizer[] token) {
    pos = idpos;
    poi = pointer;
    tok = token;
  }

  /**
   * Getter for the pre value.
   * @return pre value
   */
  public int pre() {
    return pos.list[0];
  }
  
  /**
   * Reset position iterator.
   */
  public void reset() {
    c = 0;
  }
  
  /**
   * Test is any pos value is remaining.
   * @return boolean
   */
  public boolean morePos() {
    return ++c < pos.size;
  }

  /**
   * Get next pos value.
   * @return pos value
   */
  public int nextPos() {
    return pos.list[c];
  }

  /**
   * Get next pointer.
   * @return next pointer
   */
  public int nextPoi() {
    return poi.list[c];
  }
  
  /**
   * Get number of tokens from query for this node.
   * @return number of tokens
   */
  public int getNumTokens() {
    return poi.list[0];
  }

  /**
   * Merges n to the current FTNode.
   * Pointer are node updated.
   * @param n ftnode to be merged
   * @param w distance between the pos values
   * @return boolean
   */
  public boolean union(final FTEntry n, final int w) {
    if(not != n.not) return false;

    boolean mp = morePos();
    boolean nmp = n.morePos();
    if(!(mp && nmp) || pre() != n.pre()) return false;

    final IntList il = new IntList();
    final IntList pn = poi != null ? initNewPointer(n.poi) : null;
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

    pos = new IntList(il.finish());

    final Tokenizer[] tmp = new Tokenizer[tok.length + n.tok.length];
    Array.copy(tok, tmp, 0);
    Array.copy(n.tok, tmp, tok.length);
    tok = tmp;

    c = 0;
    if(poi != null) poi = new IntList(pn.finish());
    return pos.size > 1;
  }

  /**
   * Initialize pointer list.
   * @param n IntList
   * @return IntList
   */
  private IntList initNewPointer(final IntList n) {
    final IntList il = new IntList();
    il.add(poi.list[0] > n.list[0] ? poi.list[0] : n.list[0]);
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
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + "[");
    if(tok != null) for(final Tokenizer t : tok) sb.append(t + " ");
    else sb.append("-");
    return sb.append("]").toString();
  }
}
