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
import org.basex.data.BuilderSerializer;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * Evaluates the 'optimize all' command and rebuilds all data structures of
 * the currently opened database. This effectively eliminates all fragmentation
 * and can lead to significant space savings after updates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class OptimizeAll extends ACreate {
  /** Data reference to optimize. */
  DiskData old;
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

    old = (DiskData) context.data;
    final MetaData m = old.meta;
    size = m.size;

    // check if database is also pinned by other users
    if(context.datas.pins(m.name) > 1) return error(DBLOCKED, m.name);

    // find unique temporary database name
    final String tname = m.random();

    // build database and index structures
    final DiskBuilder builder = new DiskBuilder(new DBParser(), m.prop);
    try {
      final DiskData d = builder.build(tname);
      if(m.textindex || prop.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      d);
      if(m.attrindex || prop.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, d);
      if(m.ftindex   || prop.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  d);
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
   * Parser for rebuilding existing databases.
   *
   * @author BaseX Team 2005-11, BSD License
   * @author Leo Woerteler
   */
  public final class DBParser extends Parser {
    /** Constructor. */
    protected DBParser() {
      super(old.meta.path.isEmpty() ? null : IO.get(old.meta.path), "");
    }

    @Override
    public void parse(final Builder build) throws IOException {
      final Serializer ser = new BuilderSerializer(build) {
        @Override
        protected void start(final byte[] t) throws IOException {
          super.start(t);
          pre++;
        }

        @Override
        protected void openDoc(final byte[] name) throws IOException {
          super.openDoc(name);
          pre++;
        }
      };
      for(final int root : old.doc()) {
        ser.node(old, root);
      }
    }
  }
}
