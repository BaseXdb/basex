package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Process;
import org.basex.core.Progress;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.index.FTBuilder;
import org.basex.index.FTFuzzyBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.IndexToken;
import org.basex.index.ValueBuilder;
import org.basex.util.Performance;

/**
 * Abstract class for database creation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class ACreate extends Process {
  /**
   * Constructor.
   * @param p command properties
   * @param a arguments
   */
  public ACreate(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Builds and creates a new database instance.
   * @param p parser instance
   * @param db name of database
   * @return success of operation
   */
  protected final boolean build(final Parser p, final String db) {
    String err = null;
    Builder builder = null;
    try {
      if(Prop.onthefly) {
        builder = new MemBuilder();
        progress(builder);
        context.data(builder.build(p, db));
      } else {
        if(db.equals(DataText.DEEPFS)) return error(CREATENODEEPDB);
  
        context.close();
        final Performance pp = new Performance();
        builder = new DiskBuilder();
        progress(builder);
        final Data data = builder.build(p, db);
        if(Prop.allInfo) info(CREATETABLE + NL, pp.getTimer());
        builder = null;
        index(data);
        context.data(data);
      }
      return Prop.info ? info(DBCREATED, db, perf.getTimer()) : true;
    } catch(final FileNotFoundException ex) {
      BaseX.debug(ex);
      err = BaseX.info(FILEWHICH, p.io);
    } catch(final ProgressException ex) {
      err = CANCELCREATE;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      final String msg = ex.getMessage();
      err = BaseX.info(msg != null ? msg : args[0]);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      err = BaseX.info(CREATEERR, args[0]);
    }

    try {
      if(builder != null) builder.close();
    } catch(final IOException ex) {
      BaseX.debug(ex);
    }
    DropDB.drop(db);
    return error(err);
  }

  /**
   * Builds the indexes.
   * @param data data reference
   * @throws IOException I/O exception
   */
  void index(final Data data) throws IOException {
    if(data.meta.txtindex) buildIndex(IndexToken.TYPE.TXT, data);
    if(data.meta.atvindex) buildIndex(IndexToken.TYPE.ATV, data);
    if(data.meta.ftxindex) buildIndex(IndexToken.TYPE.FTX, data);
  }

  /**
   * Builds the specified index.
   * @param i index to be built.
   * @param d data reference
   * @throws IOException I/O exception
   */
  void buildIndex(final IndexToken.TYPE i, final Data d)
      throws IOException {

    switch(i) {
      case TXT: buildIndex(i, new ValueBuilder(true), d);  break;
      case ATV: buildIndex(i, new ValueBuilder(false), d); break;
      case FTX: buildIndex(i, d.meta.ftfz ?
          new FTFuzzyBuilder() : new FTBuilder(), d); break;
      default: break;
    }
  }

  /**
   * Builds a new index.
   * @param index index to be built.
   * @param builder builder instance
   * @param data data reference
   * @throws IOException I/O exception
   */
  private void buildIndex(final IndexToken.TYPE index, final IndexBuilder
      builder, final Data data) throws IOException {

    final Performance pp = new Performance();
    progress((Progress) builder);
    data.closeIndex(index);
    data.setIndex(index, builder.build(data));

    if(Prop.debug) BaseX.err("- % Index: % (%)\n", index, pp.getTimer(),
        Performance.getMem());
  }
}
