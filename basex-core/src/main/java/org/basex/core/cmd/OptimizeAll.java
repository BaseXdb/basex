package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class OptimizeAll extends ACreate {
  /** Current pre value. */
  private int pre;
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
      return error(Util.message(ex));
    } finally {
      context.closeDB();
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
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.CTX);
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
    cb.init(Cmd.OPTIMIZE + " " + S_ALL);
  }

  /**
   * Optimizes all data structures and closes the database.
   * Recreates the database, drops the old instance and renames the recreated instance.
   * @param data disk data
   * @param ctx database context
   * @param cmd command reference or {@code null}
   * @throws IOException I/O Exception during index rebuild
   * @throws BaseXException database exception
   */
  public static void optimizeAll(final Data data, final Context ctx, final OptimizeAll cmd)
      throws IOException {

    if(data.inMemory()) throw new BaseXException(NO_MAINMEM);

    final DiskData odata = (DiskData) data;
    final MetaData ometa = odata.meta;
    final String name = ometa.name;

    // check if database is also pinned by other users
    if(ctx.dbs.pins(ometa.name) > 1) throw new BaseXException(DB_PINNED_X, name);

    // find unique temporary database name
    final StaticOptions sopts = ctx.soptions;
    final MainOptions options = ctx.options;

    // adopt original meta information
    options.set(MainOptions.CHOP, ometa.chop);
    // adopt original index options
    options.set(MainOptions.UPDINDEX, ometa.updindex);
    options.set(MainOptions.MAXCATS,  ometa.maxcats);
    options.set(MainOptions.MAXLEN,   ometa.maxlen);
    // adopt original full-text index options
    options.set(MainOptions.STEMMING,   ometa.stemming);
    options.set(MainOptions.CASESENS,   ometa.casesens);
    options.set(MainOptions.DIACRITICS, ometa.diacritics);
    options.set(MainOptions.LANGUAGE,   ometa.language.toString());
    options.set(MainOptions.STOPWORDS,  ometa.stopwords);

    // build database and index structures
    if(cmd != null) cmd.size = ometa.size;
    final String tname = sopts.random(name);
    final DBParser parser = new DBParser(odata, options, cmd);
    try(final DiskBuilder builder = new DiskBuilder(tname, parser, ctx)) {
      final DiskData dt = builder.build();
      try {
        if(ometa.createtext) create(IndexType.TEXT, dt, options, cmd);
        if(ometa.createattr) create(IndexType.ATTRIBUTE, dt, options, cmd);
        if(ometa.createftxt) create(IndexType.FULLTEXT, dt, options, cmd);
        // adopt original meta data
        dt.meta.createtext = ometa.createtext;
        dt.meta.createattr = ometa.createattr;
        dt.meta.createftxt = ometa.createftxt;
        dt.meta.filesize   = ometa.filesize;
        dt.meta.users      = ometa.users;
        dt.meta.dirty      = true;

        // move binary files
        final IOFile bin = data.meta.binaries();
        if(bin.exists()) bin.rename(dt.meta.binaries());
        final IOFile upd = odata.updateFile();
        if(upd.exists()) upd.copyTo(dt.updateFile());
      } finally {
        dt.close();
      }
    }
    // return database instance
    Close.close(data, ctx);

    // drop old database and rename temporary to final name
    if(!DropDB.drop(name, sopts)) throw new BaseXException(DB_NOT_DROPPED_X, name);
    if(!AlterDB.alter(tname, name, sopts)) throw new BaseXException(DB_NOT_RENAMED_X, tname);
  }

  /**
   * Parser for rebuilding existing databases.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  private static final class DBParser extends Parser {
    /** Disk data. */
    private final DiskData data;
    /** Calling command (may be {@code null}). */
    final OptimizeAll cmd;

    /**
     * Constructor.
     * @param data disk data
     * @param options main options
     * @param cmd calling command (may be {@code null})
     */
    DBParser(final DiskData data, final MainOptions options, final OptimizeAll cmd) {
      super(data.meta.original.isEmpty() ? null : IO.get(data.meta.original), options);
      this.data = data;
      this.cmd = cmd;
    }

    @Override
    public void parse(final Builder build) throws IOException {
      final Serializer ser = new BuilderSerializer(build) {
        @Override
        protected void startOpen(final byte[] name) throws IOException {
          super.startOpen(name);
          if(cmd != null) cmd.pre++;
        }

        @Override
        protected void openDoc(final byte[] name) throws IOException {
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
