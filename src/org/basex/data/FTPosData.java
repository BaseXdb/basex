package org.basex.data;

import org.basex.index.FTEntry;
import org.basex.util.Array;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
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
   * Adds position data. Index variant.
   * @param fte full-text entry
   */
  public void add(final FTEntry fte) {
    final int[] ps = fte.pos.finish();
    final byte[] pi = fte.poi.finish();
    int c = find(fte.pre);
    if(c < 0) { 
      c = -c - 1;
      if(size == ftp.length) ftp = Array.extend(ftp);
      Array.move(ftp, c, 1, size++ - c);
      ftp[c] = new FTPos(fte.pre, ps, pi);
    } else {
      ftp[c].union(ps, pi);
    }
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
