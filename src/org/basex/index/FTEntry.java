package org.basex.index;

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
   */
  public FTEntry(final IntList idpos, final IntList pointer) {
    pos = idpos;
    poi = pointer;
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
   * Get token number.
   * @return token number
   */
  public int getTokenNum() {
    return poi.list[0];
  }

  /**
   * Merges n to the current node.
   * Pointer are node updated.
   * @param n node to be merged
   * @param w distance between the pos values
   * @return boolean
   */
  public boolean union(final FTEntry n, final int w) {
    if(not != n.not || pre() != n.pre()) return false;

    boolean mp = morePos();
    boolean nmp = n.morePos();
    if(!mp || !nmp) {
      reset();
      n.reset();
      return false;
    }

    final IntList il = new IntList();
    final IntList pn = new IntList();
    pn.add(Math.max(poi.list[0], n.poi.list[0]));

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

    pos = il;
    poi = pn;
    reset();
    n.reset();
    return pos.size > 1;
  }

  /**
   * Adds the current position lists.
   * @param il IntList takes the new node
   * @param pn IntList with pointers
   */
  private void add(final IntList il, final IntList pn) {
    il.add(nextPos());
    if(pn != null) pn.add(nextPoi());
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + pos.list[0] + "]";
  }
}
