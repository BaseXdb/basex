package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.IndexType;
import org.basex.index.FTBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.PathBuilder;
import org.basex.index.ValueBuilder;

/**
 * Abstract class for database creation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class ACreate extends Proc {
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
      final boolean mem = prop.is(Prop.MAINMEM);
      if(!mem && context.pinned(db)) return error(DBLOCKED, db);

      builder = mem ? new MemBuilder(p, prop) : new DiskBuilder(p, prop);
      progress(builder);

      final Data d = builder.build(db);
      if(mem) {
        context.openDB(d);
      } else {
        d.close();
        final Open pr = new Open(db);
        if(!pr.run(context)) return error(pr.info());
        index(context.data);
      }
      return info(DBCREATED, db, perf);
    } catch(final Exception ex) {
      // Known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Main.debug(ex);
      abort();
      final String msg = ex instanceof IOException ?
          ex.getMessage() : Main.info(PARSEERR, p.file);
      return error(msg != null ? msg : args[0]);
    }
  }

  /**
   * Builds the indexes.
   * @param data data reference
   * @throws IOException I/O exception
   */
  protected final void index(final Data data) throws IOException {
    if(data.meta.txtindex) index(IndexType.TXT, data);
    if(data.meta.atvindex) index(IndexType.ATV, data);
    if(data.meta.ftxindex) index(IndexType.FTX, data);
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
      case TXT: b = new ValueBuilder(d, true); break;
      case ATV: b = new ValueBuilder(d, false); break;
      case FTX: b = FTBuilder.get(d, d.meta.wildcards); break;
      case PTH: b = new PathBuilder(d); break;
      default: break;
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
  protected final String path(final String p) {
    return p.replaceAll("[\\\\//]+", "/").replaceAll("^/|/$", "");
  }
}
