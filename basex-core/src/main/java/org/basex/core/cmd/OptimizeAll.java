package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'optimize all' command and rebuilds all data structures of
 * the currently opened database. This effectively eliminates all fragmentation
 * and can lead to significant space savings after updates.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class OptimizeAll extends ACreate {
  /**
   * Default constructor.
   */
  public OptimizeAll() {
    super(Perm.WRITE, true);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(!update(data, new Code() {
      @Override
      boolean run() throws IOException {
        try {
          optimizeAll(data, context, options, OptimizeAll.this);
          return true;
        } finally {
          context.closeDB();
        }
      }
    })) return false;

    final Open open = new Open(data.meta.name);
    return open.run(context) ? info(DB_OPTIMIZED_X, data.meta.name, jc().performance) :
      error(open.info());
  }

  @Override
  public boolean newData(final Context ctx) {
    return true;
  }

  @Override
  public boolean stoppable() {
    // database will be closed after optimize call
    return false;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.OPTIMIZE + " " + S_ALL);
  }

  /**
   * Optimizes all data structures and closes the database.
   * Recreates the database, drops the old instance and renames the recreated instance.
   * @param data disk data
   * @param context database context
   * @param options main options
   * @param cmd command reference or {@code null}
   * @throws IOException I/O Exception during index rebuild
   * @throws BaseXException database exception
   */
  public static void optimizeAll(final Data data, final Context context, final MainOptions options,
      final OptimizeAll cmd) throws IOException {

    if(data.inMemory()) throw new BaseXException(NO_MAINMEM);

    final DiskData odata = (DiskData) data;
    final MetaData ometa = odata.meta;

    // check if database is also pinned by other users
    final String name = ometa.name;
    if(context.datas.pins(name) > 1) throw new BaseXException(DB_PINNED_X, name);

    // adopt original index options
    options.set(MainOptions.TEXTINDEX, ometa.textindex);
    options.set(MainOptions.ATTRINDEX, ometa.attrindex);
    options.set(MainOptions.TOKENINDEX, ometa.tokenindex);
    options.set(MainOptions.FTINDEX, ometa.ftindex);
    options.set(MainOptions.TEXTINCLUDE, ometa.textinclude);
    options.set(MainOptions.ATTRINCLUDE, ometa.attrinclude);
    options.set(MainOptions.TOKENINCLUDE, ometa.tokeninclude);
    options.set(MainOptions.FTINCLUDE, ometa.ftinclude);
    // adopt original full-text index options
    options.set(MainOptions.STEMMING, ometa.stemming);
    options.set(MainOptions.CASESENS, ometa.casesens);
    options.set(MainOptions.DIACRITICS, ometa.diacritics);
    options.set(MainOptions.LANGUAGE, ometa.language.toString());
    options.set(MainOptions.STOPWORDS, ometa.stopwords);
    // adopt original index options
    options.set(MainOptions.MAXLEN, ometa.maxlen);
    options.set(MainOptions.MAXCATS, ometa.maxcats);

    // build database and index structures
    final StaticOptions sopts = context.soptions;
    final String tmpName = sopts.createTempDb(name);
    final DBParser parser = new DBParser(odata, options);
    final DiskBuilder builder = new DiskBuilder(tmpName, parser, sopts, options);
    if(cmd != null) cmd.pushJob(builder);

    // create new database with identical contents
    final DiskData ndata;
    try {
      ndata = builder.build();
    } finally {
      if(cmd != null) cmd.popJob();
    }
    Close.close(odata, context);

    // adopt original meta data, create new index structures
    final MetaData nmeta = ndata.meta;
    nmeta.createtext = ometa.createtext;
    nmeta.createattr = ometa.createattr;
    nmeta.createtoken = ometa.createtoken;
    nmeta.createft = ometa.createft;
    nmeta.original = ometa.original;
    nmeta.inputsize = ometa.inputsize;
    nmeta.time = ometa.time;
    nmeta.dirty = true;
    try {
      CreateIndex.create(ndata, cmd);
    } catch(final Throwable th) {
      // index creation failed: delete temporary database
      DropDB.drop(tmpName, sopts);
      throw th;
    } finally {
      ndata.close();
    }

    // move binary files
    final IOFile bin = ometa.binaryDir();
    if(bin.exists()) bin.rename(nmeta.binaryDir());

    // drop old database, rename temporary database
    if(!DropDB.drop(name, sopts)) throw new BaseXException(DB_NOT_DROPPED_X, name);
    if(!AlterDB.alter(tmpName, name, sopts)) throw new BaseXException(DB_NOT_RENAMED_X, tmpName);
  }

  /**
   * Parser for rebuilding existing databases.
   *
   * @author BaseX Team 2005-21, BSD License
   * @author Leo Woerteler
   */
  private static final class DBParser extends Parser {
    /** Disk data. */
    private final DiskData data;
    /** Data size. */
    private final int size;
    /** Current pre value. */
    private int pre;

    /**
     * Constructor.
     * @param data disk data
     * @param options main options
     */
    DBParser(final DiskData data, final MainOptions options) {
      super(data.meta.original.isEmpty() ? null : IO.get(data.meta.original), options);
      this.data = data;
      size = data.meta.size;
    }

    @Override
    public void parse(final Builder build) throws IOException {
      final Serializer ser = new BuilderSerializer(build) {
        @Override
        protected void startOpen(final QNm name) throws IOException {
          super.startOpen(name);
          pre++;
        }

        @Override
        protected void openDoc(final byte[] name) throws IOException {
          super.openDoc(name);
          pre++;
        }
      };

      final IntList il = data.resources.docs();
      final int is = il.size();
      for(int i = 0; i < is; i++) ser.serialize(new DBNode(data, il.get(i)));
    }

    @Override
    public double progressInfo() {
      return (double) pre / size;
    }

    @Override
    public String detailedInfo() {
      return CREATE_STATS_D;
    }
  }
}
