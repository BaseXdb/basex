package org.basex.core.proc;

import static org.basex.core.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.Type;
import org.basex.index.FTFuzzyBuilder;
import org.basex.index.FTTrieBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.ValueBuilder;

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
      final Data data = builder.build(db);
      if(mem) {
        context.openDB(data);
      } else {
        index(data);
        data.close();
        final Proc pr = new Open(db);
        if(!pr.execute(context)) return error(pr.info());
      }
      return info(DBCREATED, db, perf);
    } catch(final FileNotFoundException ex) {
      Main.debug(ex);
      return error(FILEWHICH, p.io);
    } catch(final ProgressException ex) {
      return error(PROGERR);
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    } catch(final Exception ex) {
      Main.debug(ex);
      return error(CREATEERR, args[0]);
    }
  }

  @Override
  protected void abort() {
    try {
      if(builder != null) builder.close();
    } catch(final IOException ex) {
      Main.debug(ex);
    }
  }

  
  /**
   * Builds the indexes.
   * @param data data reference
   * @throws IOException I/O exception
   */
  protected void index(final Data data) throws IOException {
    if(data instanceof MemData) return;
    if(data.meta.txtindex) buildIndex(Type.TXT, data);
    if(data.meta.atvindex) buildIndex(Type.ATV, data);
    if(data.meta.ftxindex) buildIndex(Type.FTX, data);
  }

  /**
   * Builds the specified index.
   * @param i index to be built.
   * @param d data reference
   * @throws IOException I/O exception
   */
  protected void buildIndex(final Type i, final Data d) throws IOException {
    final Prop pr = d.meta.prop;
    IndexBuilder b = null;
    switch(i) {
      case TXT: b = new ValueBuilder(d, true); break;
      case ATV: b = new ValueBuilder(d, false); break;
      case FTX: b = d.meta.ftfz ?
          new FTFuzzyBuilder(d, pr) : new FTTrieBuilder(d, pr); break;
      default: break;
    }
    d.closeIndex(i);
    progress(b);
    d.setIndex(i, b.build());
  }
}
