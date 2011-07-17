package org.basex.index.path;

import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexBuilder;
import org.basex.util.list.IntList;

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

    final IntList st = new IntList();
    final PathSummary path = new PathSummary();
    for(pre = 0; pre < size; ++pre) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(st.size() > 0 && st.peek() > par) st.pop();

      if(kind == Data.DOC) {
        st.push(pre);
        path.index(0, kind, st.size());
      } else if(kind == Data.ELEM) {
        path.index(data.name(pre), kind, st.size());
        st.push(pre);
      } else if(kind == Data.ATTR) {
        path.index(data.name(pre), kind, st.size());
      } else {
        path.index(0, kind, st.size());
      }
    }
    data.meta.pathindex = true;
    return path;
  }

  @Override
  public void abort() {
    data.meta.pathindex = false;
  }
}
