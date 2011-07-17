package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.Parser;
import org.basex.core.CommandBuilder;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
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
  protected boolean run() throws IOException {
    if(!(context.data instanceof DiskData)) return error(PROCMM);

    final DiskData old = (DiskData) context.data;
    final MetaData m = old.meta;
    size = m.size;

    // check if database is also pinned by other users
    if(context.datas.pins(m.name) > 1) return error(DBLOCKED, m.name);

    final String tname = recreate(old, prop, this);

    // delete the old database, move the new one into place and reopen it
    if(!run(new DropDB(m.name)) || !run(new AlterDB(tname, m.name)) ||
       !run(new Open(m.name))) return false;
    error("");
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

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.OPTIMIZE + " " + ALL);
  }

  /**
   * Optimize all data structures.
   * @param old disk data
   * @param p database properties
   * @return {@code true} if operation is successful
   * @throws IOException IO exception during index rebuild
   */
  public static boolean optimizeAll(final DiskData old, final Prop p)
      throws IOException {
    final String tname = recreate(old, p, null);
    final String oldname = old.meta.name;
    return DropDB.drop(oldname, p) && AlterDB.alter(tname, oldname, p);
  }

  /**
   * Creates a copy of the specified disk-based database.
   * @param old disk data to copy
   * @param p database properties
   * @param c calling command (can be {@code null})
   * @return the name of the copy database
   * @throws IOException IO exception during index rebuild
   */
  private static String recreate(final DiskData old, final Prop p,
      final OptimizeAll c) throws IOException {

    final MetaData m = old.meta;
    // find unique temporary database name
    final String tname = m.random();

    // build database and index structures
    final DiskBuilder builder = new DiskBuilder(new DBParser(old, c), m.prop);
    try {
      final DiskData d = builder.build(tname);
      if(m.textindex || p.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      d, c);
      if(m.attrindex || p.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, d, c);
      if(m.ftindex   || p.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  d, c);
      d.meta.filesize = m.filesize;
      d.meta.users    = m.users;
      d.meta.dirty    = true;
      d.close();
    } finally {
      try {
        builder.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
    return tname;
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
      super(d.meta.path.isEmpty() ? null : IO.get(d.meta.path));
      data = d;
      cmd = c;
    }

    @Override
    public void parse(final Builder build) throws IOException {
      final Serializer ser = new BuilderSerializer(build) {
        @Override
        protected void start(final byte[] t) throws IOException {
          super.start(t);
          if(cmd != null) cmd.pre++;
        }

        @Override
        protected void openDoc(final byte[] name) throws IOException {
          super.openDoc(name);
          if(cmd != null) cmd.pre++;
        }
      };
      final IntList il = data.doc();
      for(int i = 0, is = il.size(); i < is; i++) ser.node(data, il.get(i));
    }
  }
}
