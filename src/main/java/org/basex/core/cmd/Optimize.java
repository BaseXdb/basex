package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * Evaluates the 'optimize' command and optimizes the data structures of
 * the currently opened database. Indexes and statistics are refreshed,
 * which is especially helpful after updates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Optimize extends ACreate {
  /** Current pre value. */
  private int pre;
  /** Data size. */
  private int size;

  /**
   * Default constructor.
   */
  public Optimize() {
    super(DATAREF | User.WRITE);
  }

  @Override
  protected boolean run() {
    final Data d = context.data;

    // refresh indexes
    d.pthindex.init();
    d.tagindex.init();
    d.atnindex.init();
    final MetaData m = d.meta;
    m.dirty = true;

    final int[] parStack = new int[IO.MAXHEIGHT];
    final int[] tagStack = new int[IO.MAXHEIGHT];
    final boolean path = prop.is(Prop.PATHINDEX);
    int level = 0;
    int h = 0, n = 0;

    size = m.size;
    for(pre = 0; pre < size; ++pre) {
      final byte kind = (byte) d.kind(pre);
      final int par = d.parent(pre, kind);
      while(level > 0 && parStack[level - 1] > par) --level;

      if(kind == Data.DOC) {
        parStack[level++] = pre;
        if(path) d.pthindex.index(0, kind, level);
        ++n;
      } else if(kind == Data.ELEM) {
        final int id = d.name(pre);
        d.tagindex.index(d.tagindex.key(id), null, true);
        if(path) d.pthindex.index(id, kind, level);
        tagStack[level] = id;
        parStack[level++] = pre;
      } else if(kind == Data.ATTR) {
        final int id = d.name(pre);
        d.atnindex.index(d.atnindex.key(id), d.text(pre, false), true);
        if(path) d.pthindex.index(id, kind, level);
      } else {
        final byte[] txt = d.text(pre, true);
        if(kind == Data.TEXT) d.tagindex.index(tagStack[level - 1], txt);
        if(path) d.pthindex.index(0, kind, level);
      }
      if(h < level) h = level;
    }
    m.ndocs = n;
    m.pathindex = path;
    m.uptodate = true;

    try {
      // global property check can be skipped as soon as id/pre mapping exists
      if(m.textindex || prop.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      d);
      if(m.attrindex || prop.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, d);
      if(m.ftindex   || prop.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  d);
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    d.flush();

    return info(DBOPTIMIZED, m.name, perf);
  }

  @Override
  public double prog() {
    return (double) pre / size;
  }

  @Override
  public boolean stoppable() {
    return false;
  }

  @Override
  public String det() {
    return INFOSTATS;
  }
}
