package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.io.IO;

/**
 * Evaluates the 'optimize' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Optimize extends ACreate {
  /** Current pre value. */
  private int pre;
  /** Data size. */
  private int size;

  /**
   * Constructor.
   */
  public Optimize() {
    super(DATAREF | UPDATING);
  }

  @Override
  protected boolean exec() {
    // rebuild statistics
    final Data data = context.data();
    if(!stats(data)) return error(DBOPTERR1);
    info(DBOPT1, perf.getTimer());
    return true;
  }

  /**
   * Creates new statistics.
   * @param data data reference
   * @return true if operation was successful
   */
  private boolean stats(final Data data) {
    // refresh statistics
    data.skel.init();
    data.tags.init();
    data.atts.init();

    final int[] parStack = new int[IO.MAXHEIGHT];
    final int[] tagStack = new int[IO.MAXHEIGHT];
    int h = 0;
    int l = 0;

    size = data.meta.size;
    for(pre = 0; pre < size; pre++) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(l > 0 && parStack[l - 1] > par) --l;

      if(kind == Data.ELEM) {
        final int id = data.tagID(pre);
        data.tags.index(data.tags.key(id), null, true);
        tagStack[l] = id;
        parStack[l] = pre;
        if(h < ++l) h = l;
        data.skel.add(id, l, kind);
      } else if(kind == Data.ATTR) {
        final int id = data.attNameID(pre);
        data.atts.index(data.atts.key(id), data.attValue(pre), true);
        data.skel.add(id, l + 1, kind);
      } else if(kind == Data.TEXT || kind == Data.DOC) {
        if(l > 0) data.tags.index(tagStack[l - 1], data.text(pre));
        data.skel.add(0, l, kind);
      }
    }
    data.meta.height = h;
    data.meta.uptodate = true;

    try {
      data.meta.txtindex = true;
      data.meta.atvindex = true;
      index(data);
    } catch(final IOException e) {
      BaseX.debug(e);
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
