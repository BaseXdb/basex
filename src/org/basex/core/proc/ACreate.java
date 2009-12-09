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
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.Type;
import org.basex.index.FTBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.ValueBuilder;
import org.basex.util.Token;

/**
 * Abstract class for database creation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class ACreate extends Proc {
  /** Builder instance. */
  private Builder builder;

  /**
   * Protected constructor.
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
    new Close().execute(context);

    final boolean mem = prop.is(Prop.MAINMEM);
    if(!mem && context.pinned(db)) return error(DBLOCKED, db);

    builder = mem ? new MemBuilder(p) : new DiskBuilder(p);
    progress(builder);

    try {
      final Data d = builder.build(db);
      if(mem) {
        context.openDB(d);
      } else {
        d.close();
        final Proc pr = new Open(db);
        if(!pr.execute(context)) return error(pr.info());
        index(context.data);
      }
      return info(DBCREATED, db, perf);
    } catch(final IOException ex) {
      Main.debug(ex);
      abort();
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    }
  }

  /**
   * Builds the indexes.
   * @param data data reference
   * @throws IOException I/O exception
   */
  protected void index(final Data data) throws IOException {
    if(data.meta.txtindex) index(Type.TXT, data);
    if(data.meta.atvindex) index(Type.ATV, data);
    if(data.meta.ftxindex) index(Type.FTX, data);
  }

  /**
   * Builds the specified index.
   * @param i index to be built
   * @param d data reference
   * @throws IOException I/O exception
   */
  protected void index(final Type i, final Data d) throws IOException {
    if(d instanceof MemData) return;
    IndexBuilder b = null;
    switch(i) {
      case TXT: b = new ValueBuilder(d, true); break;
      case ATV: b = new ValueBuilder(d, false); break;
      case FTX: b = FTBuilder.get(d, d.meta.wildcards); break;
      default: break;
    }
    d.closeIndex(i);
    d.meta.dirty = true;
    progress(b);
    d.setIndex(i, b.build());
  }

  /**
   * Finds the specified document in the current database.
   * @param nm document name
   * @return pre value or -1
   */
  protected int findDoc(final byte[] nm) {
    for(final int p : context.doc()) {
      if(Token.eq(nm, context.data.text(p, true))) return p;
    }
    return -1;
  }
}
