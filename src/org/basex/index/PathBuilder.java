package org.basex.index;

import org.basex.data.Data;
import org.basex.data.PathSummary;
import org.basex.io.IO;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class PathBuilder extends IndexBuilder {
  /**
   * Constructor.
   * @param d data reference
   */
  public PathBuilder(final Data d) {
    super(d);
  }

  @Override
  public Index build() {
    final int[] parStack = new int[IO.MAXHEIGHT];
    final PathSummary path = new PathSummary();
    int h = 0, l = 0;
    for(pre = 0; pre < size; pre++) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(l > 0 && parStack[l - 1] > par) --l;

      if(kind == Data.DOC) {
        parStack[l++] = pre;
        path.add(0, l, kind);
      } else if(kind == Data.ELEM) {
        path.add(data.name(pre), l, kind);
        parStack[l++] = pre;
      } else if(kind == Data.ATTR) {
        path.add(data.name(pre), l, kind);
      } else {
        path.add(0, l, kind);
      }
      if(h < l) h = l;
    }
    data.meta.pthindex = true;
    data.flush();
    return path;
  }

  @Override
  public void abort() {
    data.meta.pthindex = false;
  }
}
