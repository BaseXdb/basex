package org.basex.index;

import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class contains position data for single full-text index entries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTEntry {
  /** Full-text data for a node (pos1, pos2, ...). */
  public IntList pos;
  /** Pointer for idpos - each idpos has a pointer at
   * its search string position in the query. */
  public TokenBuilder poi;
  /** Pre value. */
  public int pre;
  /** Flag for negative node. */
  public boolean not;

  /** Counter for pos values. */
  private int c = -1;

  /**
   * Constructor.
   */
  public FTEntry() {
    pos = new IntList(1);
  }

  /**
   * Constructor.
   * @param p pre value
   * @param idpos ftdata, pos1, ..., posn
   * @param tn token number
   */
  public FTEntry(final int p, final IntList idpos, final byte tn) {
    pre = p;
    pos = idpos;
    final byte[] pi = new byte[idpos.size];
    for(int i = 0; i < pi.length; i++) pi[i] = tn;
    poi = new TokenBuilder(pi);
  }

  /**
   * Adds position data. Sequential variant.
   * @param p pre value
   * @param ps positions
   * @param tn token number
   */
  public FTEntry(final int p, final IntList[] ps, final byte tn) {
    int[] pp = ps[0].finish();
    byte[] pi = new byte[pp.length];
    for(int i = 0; i < pp.length; i++) pi[i] = tn;

    for(int i = 1; i < ps.length; i++) {
      final int prs = pp.length;
      final int pss = ps[i].size;
      final int[] tp = new int[prs + pss];
      final byte[] ti = new byte[tp.length];
      for(int j = 0, j0 = 0, j1 = 0; j < tp.length; j++) {
        final boolean s = j0 == prs || j1 < pss && ps[i].list[j1] < pp[j0];
        tp[j] = s ? ps[i].list[j1++] : pp[j0];
        ti[j] = s ? (byte) (i + tn) : pi[j0++];
      }
      pp = tp;
      pi = ti;
    }
    pre = p;
    pos = new IntList(pp);
    poi = new TokenBuilder(pi);
  }

  /**
   * Getter for the pre value.
   * @return pre value
   */
  public int pre() {
    return pre;
  }
  
  /**
   * Reset position iterator.
   */
  public void reset() {
    c = -1;
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
  public byte nextPoi() {
    return poi.chars[c];
  }
  
  /**
   * Get token numbers.
   * @return token number
   */
  public int getTokenNum() {
    int m = 0;
    for(int i = 0; i < poi.size; i++) m = Math.max(m, poi.chars[i]);
    return m;
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

    final IntList ps = new IntList();
    final TokenBuilder pi = new TokenBuilder();
    boolean mp = morePos();
    boolean np = n.morePos();
    while(mp || np) {
      final int d = mp && np ? nextPos() - n.nextPos() + w : mp ? -1 : 1;

      if(d < 0) {
        if(w == 0) add(ps, pi);
        mp = morePos();
      } else if(d > 0) {
        if(w == 0) n.add(ps, pi);
        np = n.morePos();
      } else {
        add(ps, pi);
        if(w > 0) n.add(ps, pi);
        mp = morePos();
        np = n.morePos();
      }
    }
    pos = ps;
    poi = pi;
    reset();
    n.reset();
    return pos.size > 0;
  }

  /**
   * Adds the current position lists.
   * @param ps IntList takes the new node
   * @param pi IntList with pointers
   */
  private void add(final IntList ps, final TokenBuilder pi) {
    ps.add(nextPos());
    pi.add(nextPoi());
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + pos.list[0] + "]";
  }
}
