package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Command;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.index.FTBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.PathBuilder;
import org.basex.index.ValueBuilder;
import org.basex.util.Util;

/**
 * Abstract class for database creation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Builder instance. */
  private Builder builder;

  /**
   * Protected constructor, specifying command arguments.
   * @param a arguments
   */
  protected ACreate(final String... a) {
    this(User.CREATE, a);
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param p command properties
   * @param a arguments
   */
  protected ACreate(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Builds and creates a new database instance.
   * @param p parser instance
   * @param db name of database
   * @return success of operation
   */
  protected final boolean build(final Parser p, final String db) {
    // close open database
    new Close().run(context);

    try {
      if(context.pinned(db)) return error(DBLOCKED, db);

      final boolean mem = prop.is(Prop.MAINMEM);
      builder = mem ? new MemBuilder(p, prop) : new DiskBuilder(p, prop);
      progress(builder);

      final Data d = builder.build(db);
      if(mem) {
        context.openDB(d);
        context.pin(d);
      } else {
        d.close();
        final Open open = new Open(db);
        if(!open.run(context)) return error(open.info());
        index(context.data);
      }
      return info(DBCREATED, db, perf);
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final Exception ex) {
      // Known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      final String msg = ex instanceof IOException ?
          ex.getMessage() : Util.info(PARSEERR, p.file);
      return error(msg != null ? msg : args[0]);
    }
  }

  /**
   * Builds the indexes.
   * @param data data reference
   * @throws IOException I/O exception
   */
  protected final void index(final Data data) throws IOException {
    if(data.meta.textindex) index(IndexType.TEXT, data);
    if(data.meta.attrindex) index(IndexType.ATTRIBUTE, data);
    if(data.meta.ftindex)   index(IndexType.FULLTEXT, data);
  }

  /**
   * Builds the specified index.
   * @param i index to be built
   * @param d data reference
   * @throws IOException I/O exception
   */
  protected final void index(final IndexType i, final Data d)
      throws IOException {

    if(d instanceof MemData) return;
    IndexBuilder b = null;
    switch(i) {
      case TEXT: b = new ValueBuilder(d, true); break;
      case ATTRIBUTE: b = new ValueBuilder(d, false); break;
      case FULLTEXT: b = FTBuilder.get(d); break;
      case PATH: b = new PathBuilder(d); break;
      default: Util.notexpected();
    }
    d.closeIndex(i);
    d.meta.dirty = true;
    progress(b);
    d.setIndex(i, b.build());
  }

  /**
   * Normalizes the database path.
   * Removes duplicate, leading and trailing slashes
   * @param p input path
   * @return normalized path
   */
  protected static final String path(final String p) {
    return p.replaceAll("[\\\\//]+", "/").replaceAll("^/|/$", "");
  }
}
