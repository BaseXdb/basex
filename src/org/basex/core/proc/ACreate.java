package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.NativeBuilder;
import org.basex.build.Parser;
import org.basex.core.Process;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
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
abstract class ACreate extends Process {
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
   * @param db name of database; if set to null,
   * a main memory database instance is created
   * @return success of operation
   */
  protected final boolean build(final Parser p, final String db) {
    new Close().execute(context);

    final boolean mem = db == null || prop.is(Prop.MAINMEM);
    if(!mem && context.pinned(db)) return error(DBINUSE);

    final Builder builder = mem ? new MemBuilder(p) : prop.is(Prop.NATIVEDATA) ?
        new NativeBuilder(p) : new DiskBuilder(p);
    progress(builder);

    String err = null;
    try {
      final Data data = builder.build(db == null ? "" : db + ".tmp");
      if(mem) {
        context.openDB(data);
      } else {
        index(data);
        data.close();
        move(db, p.prop);
        new Open(db).execute(context);
      }
      return info(DBCREATED, db, perf.getTimer());
    } catch(final FileNotFoundException ex) {
      BaseX.debug(ex);
      err = BaseX.info(FILEWHICH, p.io);
    } catch(final ProgressException ex) {
      err = PROGERR;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      final String msg = ex.getMessage();
      err = BaseX.info(msg != null ? msg : args[0]);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      err = BaseX.info(CREATEERR, args[0]);
    }
    try {
      builder.close();
    } catch(final IOException ex) {
      BaseX.debug(ex);
    }
    if(db != null) DropDB.drop(db + ".tmp", prop);
    return error(err);
  }

  /**
   * Moves a temporary database to the final destination.
   * @param db name of database
   * @param pr database properties
   */
  protected static void move(final String db, final Prop pr) {
    DropDB.drop(db, pr);
    pr.dbpath(db + ".tmp").renameTo(pr.dbpath(db));
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
    IndexBuilder builder = null;
    switch(i) {
      case TXT: builder = new ValueBuilder(d, true); break;
      case ATV: builder = new ValueBuilder(d, false); break;
      case FTX: builder = d.meta.ftfz ?
          new FTFuzzyBuilder(d, pr) : new FTTrieBuilder(d, pr); break;
      default: break;
    }
    d.closeIndex(i);
    progress(builder);
    d.setIndex(i, builder.build());
  }

  /**
   * Returns the index type or creates an error message and returns null.
   * @param type index string
   * @return index type.
   */
  protected CmdIndex getType(final String type) {
    try {
      return CmdIndex.valueOf(type.toUpperCase());
    } catch(final Exception ex) {
      error(CMDWHICH, type);
      return null;
    }
  }
}
