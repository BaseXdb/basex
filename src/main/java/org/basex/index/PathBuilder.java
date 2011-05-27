package org.basex.index;

import org.basex.data.Data;
import org.basex.data.PathSummary;
import org.basex.io.IO;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-11, BSD License
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
    abort();

    final int[] stack = new int[IO.MAXHEIGHT];
    final PathSummary path = new PathSummary();
    int h = 0, l = 0;
    for(pre = 0; pre < size; ++pre) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(l > 0 && stack[l - 1] > par) --l;

      if(kind == Data.DOC) {
        stack[l++] = pre;
        path.index(0, kind, l);
      } else if(kind == Data.ELEM) {
        path.index(data.name(pre), kind, l);
        stack[l++] = pre;
      } else if(kind == Data.ATTR) {
        path.index(data.name(pre), kind, l);
      } else {
        path.index(0, kind, l);
      }
      if(h < l) h = l;
    }
    data.meta.pathindex = true;
    return path;
  }

  @Override
  public void abort() {
    data.meta.pathindex = false;
  }
}
