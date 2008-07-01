package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.data.Data;

/**
 * Evaluates the 'optimize' command. Optimizes the current database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Optimize extends Proc {
  /** Current pre value. */
  int pre;
  /** Data size. */
  int size;
  
  
  @Override
  protected boolean exec() {
    // rebuild statistics
    final Data data = context.data();
    if(!stats(data)) return error(DBOPTERR1);
    timer(DBOPT1 + NL);

    // minimize and rewrite textual database contents... not quite finished
    return true;
  }

  /**
   * Creates new statistics.
   * @param data data reference
   * @return true if operation was successful
   */
  public boolean stats(final Data data) {
    data.noIndex();

    final int[] parStack = new int[256];
    final int[] tagStack = new int[256];
    int h = 0;
    int l = 0;

    size = data.size;
    for(pre = 0; pre < size; pre++) {
      final int kind = data.kind(pre);
      final int par = data.parent(pre, kind);
      while(l > 0 && parStack[l - 1] > par) --l;

      if(kind == Data.ELEM || kind == Data.DOC) {
        final int id = data.tagID(pre);
        final byte[] tag = data.tags.key(id);
        data.tags.index(tag, null);
        tagStack[l] = id;
        parStack[l] = pre;
        if(h < ++l) h = l;
      } else if(kind == Data.ATTR) {
        data.atts.index(data.attName(pre), data.attValue(pre));
      } else if(kind == Data.TEXT) {
        if(l > 0) data.tags.index(tagStack[l - 1], data.text(pre));
      }
    }
    data.meta.height = h;
    data.meta.newindex = false;
    data.tags.stats = true;
    data.atts.stats = true;

    try {
      data.tags.finish(data.meta.dbname);
      data.atts.finish(data.meta.dbname);
      data.meta.finish(data.size);
    } catch(final IOException ex) {
      return false;
    }
    return true;
  }

  @Override
  public double prog() {
    return pre / (double) size;
  }

  @Override
  public String det() {
    return INFOSTATS;
  }
}
