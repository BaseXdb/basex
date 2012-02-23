package org.basex.index.path;

import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexBuilder;
import org.basex.util.list.IntList;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final IntList pars = new IntList();
    final PathSummary path = new PathSummary(data);
    for(pre = 0; pre < size; ++pre) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(!pars.isEmpty() && pars.peek() > par) pars.pop();

      if(kind == Data.DOC) {
        path.index(0, kind, pars.size());
        pars.push(pre);
      } else if(kind == Data.ELEM) {
        path.index(data.name(pre), kind, pars.size());
        pars.push(pre);
      } else if(kind == Data.ATTR) {
        path.index(data.name(pre), kind, pars.size(),
            data.text(pre, false), data.meta);
      } else {
        path.index(0, kind, pars.size(), data.text(pre, true), data.meta);
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
