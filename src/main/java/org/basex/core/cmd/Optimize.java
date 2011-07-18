package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.Util;
import org.basex.util.list.IntList;

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
    final MetaData m = d.meta;
    size = m.size;

    try {
      optimize(d, prop, this);
    } catch(final IOException ex) {
      Util.debug(ex);
    }

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

  /**
   * Optimize data structures.
   * @param d data
   * @param p database properties
   * @throws IOException IO exception during index rebuild
   */
  public static void optimize(final Data d, final Prop p) throws IOException {
    optimize(d, p, null);
  }

  /**
   * Optimize data structures.
   * @param d data
   * @param p database properties
   * @param c calling command (can be null)
   * @throws IOException IO exception during index rebuild
   */
  private static void optimize(final Data d, final Prop p, final Optimize c)
      throws IOException {
    // refresh indexes
    d.pthindex.init();
    d.tagindex.init();
    d.atnindex.init();
    final MetaData m = d.meta;
    m.dirty = true;

    final IntList pars = new IntList();
    final IntList tags = new IntList();
    final boolean path = p.is(Prop.PATHINDEX);
    int n = 0;

    for(int pre = 0; pre < m.size; ++pre) {
      final byte kind = (byte) d.kind(pre);
      final int par = d.parent(pre, kind);
      while(!pars.empty() && pars.peek() > par) {
        pars.pop();
        tags.pop();
      }
      if(kind == Data.DOC) {
        pars.push(pre);
        tags.push(0);
        if(path) d.pthindex.index(0, kind, pars.size());
        ++n;
      } else if(kind == Data.ELEM) {
        final int id = d.name(pre);
        d.tagindex.index(d.tagindex.key(id), null, true);
        if(path) d.pthindex.index(id, kind, pars.size());
        pars.push(pre);
        tags.push(id);
      } else if(kind == Data.ATTR) {
        final int id = d.name(pre);
        d.atnindex.index(d.atnindex.key(id), d.text(pre, false), true);
        if(path) d.pthindex.index(id, kind, pars.size());
      } else {
        final byte[] txt = d.text(pre, true);
        if(kind == Data.TEXT) d.tagindex.index(tags.peek(), txt);
        if(path) d.pthindex.index(0, kind, pars.size());
      }
      if(c != null) c.pre = pre;
    }
    m.ndocs = n;
    m.pathindex = path;
    m.uptodate = true;

    try {
      // global property check can be skipped as soon as id/pre mapping exists
      if(m.textindex || p.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      d, c);
      if(m.attrindex || p.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, d, c);
      if(m.ftindex   || p.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  d, c);
    } finally {
      d.flush();
    }
  }
}
