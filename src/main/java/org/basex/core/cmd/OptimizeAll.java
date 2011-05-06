package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.Parser;
import org.basex.core.CommandBuilder;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.data.DiskData;
import org.basex.data.FTPos;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.util.Atts;
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
      final Serializer ser = new Serializer() {
        /** Current tag's name. */
        private byte[] tag;
        /** True while being in an open tag. */
        private boolean open;
        /** Attribute cache. */
        private final Atts att = new Atts();

        @Override
        public void text(final byte[] b, final FTPos ftp) throws IOException {
          text(b);
        }

        @Override
        public void text(final byte[] b) throws IOException {
          finish();
          build.text(b);
        }

        @Override
        protected void start(final byte[] t) throws IOException {
          pre++;
          tag = t;
          open = true;
        }

        @Override
        public void pi(final byte[] n, final byte[] v) throws IOException {
          build.pi(concat(n, new byte[]{ ' ' }, v));
        }

        @Override
        public void item(final byte[] b) throws IOException {
          text(b);
        }

        @Override
        protected void finish() throws IOException {
          if(open) {
            build.startElem(tag, att);
            att.reset();
            open = false;
          }
        }

        @Override
        protected void empty() throws IOException {
          if(open) {
            build.emptyElem(tag, att);
            open = false;
          } else {
            close(tag);
          }
          tag = null;
          att.reset();
        }

        @Override
        public void comment(final byte[] b) throws IOException {
          build.comment(b);
        }

        @Override
        protected void close(final byte[] t) throws IOException {
          build.endElem(t);
          tag = null;
        }

        @Override
        public void attribute(final byte[] n, final byte[] v)
            throws IOException {
          if(startsWith(n, XMLNS)) {
            if(n.length == 5) {
              build.startNS(EMPTY, v);
            } else if(n[5] == ':') {
              build.startNS(substring(n, 6), v);
            } else att.add(n, v);
          } else {
            att.add(n, v);
          }
        }

        @Override
        protected void openDoc(final byte[] name) throws IOException {
          pre++;
          build.startDoc(name);
        }

        @Override
        protected void closeDoc() throws IOException {
          build.endDoc();
        }

        @Override
        public void openResult() throws IOException {
          // ignore this
        }

        @Override
        public void closeResult() throws IOException {
          // ignore this
        }

        @Override
        protected void cls() throws IOException {
          // ignore this
        }
      };
      for(final int root : old.doc()) {
        ser.node(old, root);
      }
    }
  }
}
