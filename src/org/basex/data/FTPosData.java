package org.basex.data;

import org.basex.index.FTEntry;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 * 
 * The data is stored as follows:
 * prepos[i][pre, pos0, ..., posn]
 * poi[i][poiMax, poi0, ..., poin]
 * For each pos values, theres a poi value stored. poiMax is the
 * max value of all pos0, ..., poin
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Pre values. */
  private FTPos[] ftp = new FTPos[1];
  /** Number of pre values. */
  private int size;

  /**
   * Adds position data. Sequential variant.
   * @param p pre value
   * @param ps positions
   * @param tn token number
   */
  public void add(final int p, final IntList[] ps, final byte tn) {
    int[] pp = ps[0].finish();
    byte[] pi = new byte[pp.length];
    for(int i = 0; i < pp.length; i++) pi[i] = tn;

    for(int c = 1; c < ps.length; c++) {
      final int prs = pp.length;
      final int pss = ps[c].size;
      final int[] tp = new int[prs + pss];
      final byte[] ti = new byte[tp.length];
      for(int i = 0, p0 = 0, p1 = 0; i < tp.length; i++) {
        final boolean s = p0 == prs || p1 < pss && ps[c].list[p1] < pp[p0];
        tp[i] = s ? ps[c].list[p1++] : pp[p0];
        ti[i] = s ? (byte) (c + tn) : pi[p0++];
      }
      pp = tp;
      pi = ti;
    }
    add(p, pp, pi);
  }

  /**
   * Adds a full-text entry. Index variant.
   * @param fte full-text entry reference.
   */
  public void add(final FTEntry fte) {
    add(fte.pre, fte.pos.finish(), fte.poi.finish());
  }
  
  /**
   * Removes position data for the specified node.
   * Called by And expression.
   * @param p int pre value
   */
  public void remove(final int p) {
    final int i = find(p);
    if(i < 0) return;
    System.arraycopy(ftp, i, ftp, i, --size - i);
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a pre value, null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param p int pre value
   * @return int[2][n] full-text data or null
   */
  public FTPos get(final int p) {
    final int i = find(p);
    return i < 0 ? null : ftp[i];
  }

  /**
   * Compares full-text data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean same(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; i++) {
      if(ftp[i].pre != ft.ftp[i].pre || !Array.eq(ftp[i].pos, ft.ftp[i].pos) ||
          !Array.eq(ftp[i].poi, ft.ftp[i].poi)) return false;
    }
    return true;
  }

  /**
   * Adds or merges position arrays.
   * pp : [pre, pos0, ..., posn]
   * p : [poiMax, poi0, ..., poin]
   * @param p pre value
   * @param ps int[] positions
   * @param pi int[] pointer
   */
  private void add(final int p, final int[] ps, final byte[] pi) {
    if(size == ftp.length) ftp = Array.extend(ftp);

    int c = find(p);
    if(c < 0) { 
      // new pre value...
      c = -c - 1;
      if(c < size) Array.move(ftp, c, 1, size - c);
      ftp[c] = new FTPos(p, ps, pi);
      size++;
    } else {
      if(Array.eq(ps, ftp[c].pos)) return;

      // merge entries with the same pre value
      final int prs = ps.length;
      final int pss = ftp[c].pos.length;
      final int[] ts = new int[prs + pss];
      final byte[] ti = new byte[ts.length];
      for(int i = 0, p0 = 0, p1 = 0; i < ts.length; i++) {
        final boolean s = p0 == prs || p1 < pss && ftp[c].pos[p1] < ps[p0];
        ts[i] = s ? ftp[c].pos[p1] : ps[p0];
        ti[i] = s ? ftp[c].poi[p1++] : pi[p0++];
      }
      ftp[c] = new FTPos(p, ts, ti);
    }
  }

  /**
   * Returns the position of the specified pre value.
   * @param p int pre value
   * @return position or negative insertion value - 1
   */
  private int find(final int p) {
    // binary search
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = ftp[m].pre - p;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }
}
