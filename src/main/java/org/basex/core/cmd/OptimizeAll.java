package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'optimize all' command and rebuilds all data structures of
 * the currently opened database. This effectively eliminates all fragmentation
 * and can lead to significant space savings after updates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class OptimizeAll extends ACreate {
  /** Current pre value. */
  int pre;
  /** Data size. */
  private int size;

  /**
   * Default constructor.
   */
  public OptimizeAll() {
    super(Perm.WRITE, true);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    try {
      optimizeAll(data, context, this);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(Util.message(ex));
    }

    final Open open = new Open(data.meta.name);
    return open.run(context) ? info(DB_OPTIMIZED_X, data.meta.name, perf) :
      error(open.info());
  }

  @Override
  public boolean newData(final Context ctx) {
    return true;
  }

  @Override
  protected boolean databases(final StringList db) {
    db.add("");
    return true;
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

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.OPTIMIZE + " " + C_ALL);
  }

  /**
   * Optimizes all data structures. Recreates the database, drops the
   * old instance and renames the recreated instance.
   * @param data disk data
   * @param ctx database context
   * @param cmd command reference, or {@code null}
   * @throws IOException I/O Exception during index rebuild
   * @throws BaseXException database exception
   */
  public static void optimizeAll(final Data data, final Context ctx,
      final OptimizeAll cmd) throws IOException {

    if(data.inMemory()) throw new BaseXException(NO_MAINMEM);

    final DiskData old = (DiskData) data;
    final MetaData m = old.meta;
    if(cmd != null) cmd.size = m.size;

    // check if database is also pinned by other users
    if(ctx.dbs.pins(m.name) > 1) throw new BaseXException(DB_PINNED_X, m.name);

    // find unique temporary database name
    final String tname = ctx.mprop.random(m.name);

    // adopt original meta data
    ctx.prop.set(Prop.CHOP, m.chop);
    ctx.prop.set(Prop.UPDINDEX, m.updindex);
    ctx.prop.set(Prop.STEMMING, m.stemming);
    ctx.prop.set(Prop.CASESENS, m.casesens);
    ctx.prop.set(Prop.DIACRITICS, m.diacritics);
    ctx.prop.set(Prop.MAXCATS, m.maxcats);
    ctx.prop.set(Prop.MAXLEN, m.maxlen);
    ctx.prop.set(Prop.LANGUAGE, m.language.toString());

    // build database and index structures
    final DiskBuilder builder = new DiskBuilder(tname, new DBParser(old, cmd), ctx);
    try {
      final DiskData d = builder.build();
      if(m.createtext) create(IndexType.TEXT, d, cmd);
      if(m.createattr) create(IndexType.ATTRIBUTE, d, cmd);
      if(m.createftxt) create(IndexType.FULLTEXT, d, cmd);
      // adopt original meta data
      d.meta.createtext = m.createtext;
      d.meta.createattr =  m.createattr;
      d.meta.createftxt = m.createftxt;
      d.meta.filesize = m.filesize;
      d.meta.users    = m.users;
      d.meta.dirty    = true;

      // move binary files
      final IOFile bin = data.meta.binaries();
      if(bin.exists()) bin.rename(d.meta.binaries());
      final IOFile upd = old.updateFile();
      if(upd.exists()) upd.copyTo(d.updateFile());
      d.close();
    } finally {
      try {
        builder.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
    Close.close(data, ctx);

    // drop old database and rename temporary to final name
    if(!DropDB.drop(m.name, ctx))
      throw new BaseXException(DB_NOT_DROPPED_X, m.name);
    if(!AlterDB.alter(tname, m.name, ctx))
      throw new BaseXException(DB_NOT_RENAMED_X, tname);
  }

  /**
   * Parser for rebuilding existing databases.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  private static final class DBParser extends Parser {
    /** Disk data. */
    private final DiskData data;
    /** Calling command (can be {@code null}). */
    final OptimizeAll cmd;

    /**
     * Constructor.
     * @param d disk data
     * @param c calling command (can be {@code null})
     */
    DBParser(final DiskData d, final OptimizeAll c) {
      super(d.meta.original.isEmpty() ? null : IO.get(d.meta.original), d.meta.prop);
      data = d;
      cmd = c;
    }

    @Override
    public void parse(final Builder build) throws IOException {
      final Serializer ser = new BuilderSerializer(build) {
        @Override
        protected void startOpen(final byte[] t) throws IOException {
          super.startOpen(t);
          if(cmd != null) cmd.pre++;
        }

        @Override
        public void openDoc(final byte[] name) throws IOException {
          super.openDoc(name);
          if(cmd != null) cmd.pre++;
        }
      };

      final IntList il = data.resources.docs();
      final int is = il.size();
      for(int i = 0; i < is; i++) ser.serialize(new DBNode(data, il.get(i)));
    }
  }
}
