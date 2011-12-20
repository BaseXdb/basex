package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

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
    final Data d = context.data();
    final MetaData m = d.meta;
    size = m.size;

    try {
      optimize(d, this);
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
   * @throws IOException I/O Exception during index rebuild
   */
  public static void optimize(final Data d) throws IOException {
    optimize(d, null);
  }

  /**
   * Optimize data structures.
   * @param d data
   * @param c calling command (can be null)
   * @throws IOException I/O Exception during index rebuild
   */
  private static void optimize(final Data d, final Optimize c)
      throws IOException {

    // refresh indexes
    d.pthindex.close();
    d.tagindex.init();
    d.atnindex.init();
    final MetaData m = d.meta;
    m.dirty = true;

    final IntList pars = new IntList();
    final IntList tags = new IntList();
    int n = 0;

    for(int pre = 0; pre < m.size; ++pre) {
      final byte kind = (byte) d.kind(pre);
      final int par = d.parent(pre, kind);
      while(!pars.empty() && pars.peek() > par) {
        pars.pop();
        tags.pop();
      }
      final int level = pars.size();
      if(kind == Data.DOC) {
        if(m.createpath) d.pthindex.index(0, kind, level);
        pars.push(pre);
        tags.push(0);
        ++n;
      } else if(kind == Data.ELEM) {
        final int id = d.name(pre);
        d.tagindex.index(d.tagindex.key(id), null, true);
        if(m.createpath) d.pthindex.index(id, kind, level);
        pars.push(pre);
        tags.push(id);
      } else if(kind == Data.ATTR) {
        final int id = d.name(pre);
        final byte[] val = d.text(pre, false);
        d.atnindex.index(d.atnindex.key(id), val, true);
        if(m.createpath) d.pthindex.index(id, kind, level, val, m);
      } else {
        final byte[] val = d.text(pre, true);
        if(kind == Data.TEXT && level > 1) {
          d.tagindex.index(tags.peek(), val);
        }
        if(m.createpath) d.pthindex.index(0, kind, level, val, m);
      }
      if(c != null) c.pre = pre;
    }
    m.ndocs = n;
    m.pathindex = m.createpath;
    m.uptodate = true;

    try {
      optimize(IndexType.ATTRIBUTE, d, m.createattr, c);
      optimize(IndexType.TEXT,      d, m.createtext, c);
      optimize(IndexType.FULLTEXT,  d, m.createftxt, c);
    } finally {
      d.flush();
    }
  }

  /**
   * Optimizes the specified index.
   * @param type index type
   * @param d data reference
   * @param create create flag
   * @param c calling command
   * @throws IOException I/O exception
   *
   */
  private static void optimize(final IndexType type, final Data d,
      final boolean create, final Optimize c) throws IOException {
    if(create) create(type, d, c);
    else drop(type, d);
  }
}
