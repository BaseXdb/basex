package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'optimize' command and optimizes the data structures of
 * the currently opened database. Indexes and statistics are refreshed,
 * which is especially helpful after updates.
 *
 * @author BaseX Team 2005-14, BSD License
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
    super(Perm.WRITE, true);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    final MetaData meta = data.meta;
    size = meta.size;

    if(!startUpdate()) return false;
    try {
      optimize(data, options, this);
      return info(DB_OPTIMIZED_X, meta.name, perf);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    } finally {
      if(!finishUpdate()) return false;
    }
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
    return CREATE_STATS_D;
  }

  /**
   * Optimizes the structures of a database.
   * @param data data
   * @param options main options
   * @param cmd calling command instance (may be {@code null})
   * @throws IOException I/O Exception during index rebuild
   */
  public static void optimize(final Data data, final MainOptions options, final Optimize cmd)
      throws IOException {
    optimize(data, options, false, false, cmd);
  }

  /**
   * Optimizes the structures of a database.
   * @param data data
   * @param options main options
   * @param enforce enforce index operation
   * @param enforceFT enforce full-text index operation
   * @param cmd calling command instance (may be {@code null})
   * @throws IOException I/O Exception during index rebuild
   */
  public static void optimize(final Data data, final MainOptions options, final boolean enforce,
      final boolean enforceFT, final Optimize cmd) throws IOException {

    // initialize structural indexes
    final MetaData md = data.meta;
    if(!md.uptodate) {
      data.paths.init();
      data.elemNames.init();
      data.attrNames.init();
      md.dirty = true;

      final IntList pars = new IntList();
      final IntList elms = new IntList();
      int n = 0;

      for(int pre = 0; pre < md.size; ++pre) {
        final byte kind = (byte) data.kind(pre);
        final int par = data.parent(pre, kind);
        while(!pars.isEmpty() && pars.peek() > par) {
          pars.pop();
          elms.pop();
        }
        final int level = pars.size();
        if(kind == Data.DOC) {
          data.paths.put(0, Data.DOC, level);
          pars.push(pre);
          elms.push(0);
          ++n;
        } else if(kind == Data.ELEM) {
          final int id = data.name(pre);
          data.elemNames.index(data.elemNames.key(id), null, true);
          data.paths.put(id, Data.ELEM, level);
          pars.push(pre);
          elms.push(id);
        } else if(kind == Data.ATTR) {
          final int id = data.name(pre);
          final byte[] val = data.text(pre, false);
          data.attrNames.index(data.attrNames.key(id), val, true);
          data.paths.put(id, Data.ATTR, level, val, md);
        } else {
          final byte[] val = data.text(pre, true);
          if(kind == Data.TEXT && level > 1) data.elemNames.index(elms.peek(), val);
          data.paths.put(0, kind, level, val, md);
        }
        if(cmd != null) cmd.pre = pre;
      }
      md.ndocs.set(n);
      md.uptodate = true;
    }

    // reassign autooptimize flag
    final boolean autoopt = options.get(MainOptions.AUTOOPTIMIZE);
    if(autoopt != md.autoopt) {
      md.autoopt = autoopt;
      md.dirty = true;
    }

    // rebuild value indexes
    optimize(IndexType.ATTRIBUTE, data, options, md.createattr, md.attrindex, enforce, cmd);
    optimize(IndexType.TEXT,      data, options, md.createtext, md.textindex, enforce, cmd);
    optimize(IndexType.FULLTEXT,  data, options, md.createftxt, md.ftxtindex, enforceFT, cmd);
  }

  /**
   * Optimizes the specified index if the old and new state is different.
   * @param type index type
   * @param data data reference
   * @param options main options
   * @param create new flag
   * @param old old flag
   * @param force enforce operation
   * @param cmd calling command instance
   * @throws IOException I/O exception
   */
  private static void optimize(final IndexType type, final Data data, final MainOptions options,
      final boolean create, final boolean old, final boolean force, final Optimize cmd)
      throws IOException {

    // check if flags have changed
    if(create == old && !force) return;

    // create or drop index
    if(create) create(type, data, options, cmd);
    else drop(type, data);
  }
}
