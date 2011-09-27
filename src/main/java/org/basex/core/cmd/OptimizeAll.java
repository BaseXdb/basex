package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.serial.BuilderSerializer;
import org.basex.io.serial.Serializer;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * Evaluates the 'optimize all' command and rebuilds all data structures of
 * the currently opened database. This effectively eliminates all fragmentation
 * and can lead to significant space savings after updates.
 *
 * @author BaseX Team 2005-11, BSD License
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
    super(DATAREF | User.WRITE);
  }

  @Override
  protected boolean run() {
    try {
      final Data data = context.data();
      optimizeAll(data, context, this);

      final Open open = new Open(data.meta.name);
      return open.run(context) ? info(DBOPTIMIZED, data.meta.name, perf) :
        error(open.info());
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(Util.message(ex));
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
    return INFOSTATS;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.OPTIMIZE + " " + ALL);
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

    if(!(data instanceof DiskData)) throw new BaseXException(PROCMM);

    final DiskData old = (DiskData) data;
    final MetaData m = old.meta;
    if(cmd != null) cmd.size = m.size;

    // check if database is also pinned by other users
    if(ctx.datas.pins(m.name) > 1) throw new BaseXException(DBLOCKED, m.name);

    // find unique temporary database name
    final String tname = ctx.mprop.random(m.name);

    // build database and index structures
    final DiskBuilder builder = new DiskBuilder(tname,
        new DBParser(old, cmd), ctx);
    try {
      final DiskData d = builder.build();
      if(m.textindex || ctx.prop.is(Prop.TEXTINDEX))
        index(IndexType.TEXT, d, cmd);
      if(m.attrindex || ctx.prop.is(Prop.ATTRINDEX))
        index(IndexType.ATTRIBUTE, d, cmd);
      if(m.ftindex || ctx.prop.is(Prop.FTINDEX))
        index(IndexType.FULLTEXT, d, cmd);
      d.meta.filesize = m.filesize;
      d.meta.users    = m.users;
      d.meta.dirty    = true;
      // move binary files
      final IOFile bin = data.meta.binaries();
      if(bin.exists()) bin.rename(d.meta.binaries());
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
    // usually, no exceptions should be thrown here anymore
    if(!DropDB.drop(m.name, ctx.mprop))
      throw new BaseXException(DBDROPERROR, m.name);
    if(!AlterDB.alter(tname, m.name, ctx.mprop))
      throw new BaseXException(DBNOTALTERED, tname);
  }

  /**
   * Parser for rebuilding existing databases.
   *
   * @author BaseX Team 2005-11, BSD License
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
    protected DBParser(final DiskData d, final OptimizeAll c) {
      super(d.meta.original.isEmpty() ? null : IO.get(d.meta.original));
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
        protected void openDoc(final byte[] name) throws IOException {
          super.openDoc(name);
          if(cmd != null) cmd.pre++;
        }
      };
      final IntList il = data.docs();
      for(int i = 0, is = il.size(); i < is; i++) ser.node(data, il.get(i));
    }
  }
}
