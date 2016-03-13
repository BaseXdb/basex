package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'optimize' command and optimizes the data structures of
 * the currently opened database. Indexes and statistics are refreshed,
 * which is especially helpful after updates.
 *
 * @author BaseX Team 2005-16, BSD License
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
    boolean ok = true;
    try {
      // reassign autooptimize flag
      final boolean autoopt = options.get(MainOptions.AUTOOPTIMIZE);
      if(autoopt != data.meta.autooptimize) {
        data.meta.autooptimize = autoopt;
        data.meta.dirty = true;
      }
      optimize(data, this);
      ok = info(DB_OPTIMIZED_X, meta.name, perf);
    } catch(final IOException ex) {
      ok = error(Util.message(ex));
    } finally {
      ok &= finishUpdate();
    }
    return ok;
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
   * Optimizes a database after updates.
   * @param data data
   * @throws IOException I/O Exception during index rebuild
   */
  public static void finish(final Data data) throws IOException {
    // GH-676: optimize database and rebuild index structures if ID has turned negative
    if(data.meta.lastid < data.meta.size - 1) optimizeIds(data);
    // GH-1035: auto-optimize database
    if(data.meta.autooptimize) optimize(data, null);
  }

  /**
   * Optimizes the structures of a database.
   * @param data data
   * @param cmd calling command instance (may be {@code null})
   * @throws IOException I/O Exception during index rebuild
   */
  public static void optimize(final Data data, final Optimize cmd) throws IOException {
    optimize(data, false, false, false, false, cmd);
  }

  /**
   * Optimizes the structures of a database.
   * @param data data
   * @param enforceText enforce creation or deletion of text index
   * @param enforceAttr enforce creation or deletion of attribute index
   * @param enforceToken enforce creation or deletion of token index
   * @param enforceFt enforce creation or deletion of full-text index
   * @param cmd calling command instance (may be {@code null})
   * @throws IOException I/O Exception during index rebuild
   */
  public static void optimize(final Data data, final boolean enforceText, final boolean enforceAttr,
      final boolean enforceToken, final boolean enforceFt, final Optimize cmd)
      throws IOException {

    // initialize structural indexes
    final MetaData md = data.meta;
    if(!md.uptodate) {
      data.paths.init();
      data.elemNames.init();
      data.attrNames.init();
      md.dirty = true;

      final IntList pars = new IntList(), elms = new IntList();
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
          final int id = data.nameId(pre);
          data.elemNames.index(data.elemNames.key(id), null, true);
          data.paths.put(id, Data.ELEM, level);
          pars.push(pre);
          elms.push(id);
        } else if(kind == Data.ATTR) {
          final int id = data.nameId(pre);
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
      md.ndocs = n;
      md.uptodate = true;
    }

    // rebuild value indexes
    optimize(IndexType.TEXT, data, md.createtext, enforceText, cmd);
    optimize(IndexType.ATTRIBUTE, data, md.createattr, enforceAttr, cmd);
    optimize(IndexType.TOKEN, data, md.createtoken, enforceToken, cmd);
    optimize(IndexType.FULLTEXT, data, md.createft, enforceFt, cmd);
  }

  /**
   * Creates or deletes the specified index if the old and new state is different.
   * @param type index type
   * @param data data reference
   * @param create new flag
   * @param enforce enforce operation
   * @param cmd calling command instance
   * @throws IOException I/O exception
   */
  private static void optimize(final IndexType type, final Data data, final boolean create,
      final boolean enforce, final Optimize cmd) throws IOException {

    // check if flags have changed
    if(create == data.meta.index(type) && !enforce) return;
    // create or drop index
    if(create) CreateIndex.create(type, data, cmd);
    else DropIndex.drop(type, data);
  }

  /**
   * Creates new node ids and recreates updatable index structures.
   * @param data data
   * @throws IOException I/O Exception during index rebuild
   */
  private static void optimizeIds(final Data data) throws IOException {
    final MetaData md = data.meta;
    final int size = md.size;
    for(int pre = 0; pre < size; ++pre) data.id(pre, pre);
    md.lastid = size - 1;
    md.dirty = true;

    if(data.meta.updindex) {
      data.idmap = new IdPreMap(md.lastid);
      if(data.meta.textindex) optimize(IndexType.TEXT, data, true, true, null);
      if(data.meta.attrindex) optimize(IndexType.ATTRIBUTE, data, true, true, null);
      if(data.meta.tokenindex) optimize(IndexType.TOKEN, data, true, true, null);
    }
  }
}
