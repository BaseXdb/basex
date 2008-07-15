package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.fs.FSParser;
import org.basex.build.mediovis.MAB2Parser;
import org.basex.build.xml.DirParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Progress;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.FTBuilder;
import org.basex.index.FZBuilder;
import org.basex.index.IndexBuilder;
import org.basex.index.IndexToken;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;
import org.basex.util.Performance;

/**
 * Evaluates the 'create' command. Creates a new database instance for the
 * specified arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Create extends Proc {
  /** Create option. */
  public static final String DB = "database";
  /** Create option. */
  public static final String DBS = "db";
  /** Create option. */
  public static final String XML = "xml";
  /** Create option. */
  public static final String MAB2 = "mab2";
  /** Create option. */
  public static final String FS = "fs";
  /** Create option. */
  public static final String INDEX = "index";

  /** Create index option. */
  public static final String TXT = "text";
  /** Create index option. */
  public static final String ATV = "attribute";
  /** Create index option. */
  public static final String FTX = "fulltext";

  @Override
  protected boolean exec() {
    final String type = cmd.arg(0).toLowerCase();

    try {
      if(type.equals(DB) || type.equals(DBS) || type.equals(XML)) return xml();
      if(type.equals(MAB2)) return mab2();
      if(type.equals(FS)) return fs();
      if(type.equals(INDEX)) return index();
    } catch(final IllegalArgumentException ex) {
      throw ex;
    } catch(final ProgressException ex) {
      return error(CANCELCREATE);
    }
    throw new IllegalArgumentException();
  }

  /**
   * Create a database for the specified XML file.
   * @return success of operation
   */
  private boolean xml() {
    int n = cmd.nrArgs();
    if(n < 2 || n > 3) throw new IllegalArgumentException();

    final IO f = new IO(cmd.arg(1));
    if(!f.exists()) return error(FILEWHICH, f);
    final String db = n != 3 ? f.dbname() : cmd.arg(2);
    return build(new DirParser(f), db);
  }

  /**
   * Create a database for the specified MAB2 file.
   * @return success of operation
   */
  private boolean mab2() {
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();

    // check if file exists
    final IO file = new IO(cmd.arg(1));
    if(!file.exists()) return error(FILEWHICH, cmd.arg(1));

    Prop.chop  = true;
    Prop.entity   = true;
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.ftindex = false;
    
    return build(new MAB2Parser(file), file.dbname().replaceAll("\\..*", ""));
  }

  /**
   * Create database instance for a file hierarchy starting from
   * a specified path.
   * @return success of operation
   */
  private boolean fs() {
    int n = cmd.nrArgs();
    if(n < 2 || n > 3) throw new IllegalArgumentException();

    final IO f = new IO(cmd.arg(1));
    final String db = n != 3 ? f.dbname() : cmd.arg(2);

    Prop.chop = true;
    Prop.entity = true;
    Prop.mainmem = false;

    return build(new FSParser(f), db);
  }

  /**
   * Creates index structures.
   * @return success of operation
   */
  private boolean index() {
    if(context.data() == null) return error(PROCNODB);
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();
    return index(cmd.arg(1), true);
  }

  /**
   * Builds and creates a new database instance.
   * @param p parser instance
   * @param db name of database
   * @return success of operation
   */
  private boolean build(final Parser p, final String db) {
    String err = "";
    Builder builder = null;
    try {
      if(Prop.onthefly) {
        context.data(new MemBuilder().build(p, db));
        return true;
      }

      context.close();
      final Performance pp = new Performance();
      builder = new DiskBuilder();
      progress(builder);
      final Data data = builder.build(p, db);
      if(Prop.allInfo) info(CREATETABLE + NL, pp.getTimer());
      builder = null;

      if(data.meta.txtindex) buildIndex(IndexToken.TYPE.TXT, data);
      if(data.meta.atvindex) buildIndex(IndexToken.TYPE.ATV, data);
      if(data.meta.ftxindex) buildIndex(IndexToken.TYPE.FTX, data);
      context.data(data);
      
      return Prop.info ? info(DBCREATED, db, perf.getTimer()) : true;
    } catch(final FileNotFoundException ex) {
      BaseX.debug(ex);
      err = BaseX.info(FILEWHICH, p.file);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      err = ex.getMessage();
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final Exception ex) {
      String msg = ex.getMessage();
      if(msg == null) msg = ex.toString();
      BaseX.debug(ex);
      err = BaseX.info(CREATEERR, cmd.args(), msg.length() != 0 ? msg : "");
    }
    
    try {
      if(builder != null) builder.close();
    } catch(final IOException ex) {
      BaseX.debug(ex);
    }
    Drop.drop(db);
    return error(err);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * No warnings are thrown; instead, an empty reference is returned.
   * @param db name of the database to be created
   * @param fn database name
   * @return database instance
   */
  public static Data xml(final IO fn, final String db) {
    try {
      final Parser p = Prop.intparse ? new XMLParser(fn) : new SAXWrapper(fn);
      if(Prop.onthefly) return new MemBuilder().build(p, db);

      final Data data = new DiskBuilder().build(p, db);
      if(data.meta.txtindex) data.openIndex(
          IndexToken.TYPE.TXT, new ValueBuilder(true).build(data));
      if(data.meta.atvindex) data.openIndex(
          IndexToken.TYPE.ATV, new ValueBuilder(false).build(data));
      if(data.meta.ftxindex) data.openIndex(
          IndexToken.TYPE.FTX, data.meta.ftfuzzy ?
              new FZBuilder().build(data) : new FTBuilder().build(data));
      return data;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return null;
    }
  }

  /**
   * Creates the specified index.
   * @param type index type
   * @param inf show info
   * @return success of operation
   */
  private boolean index(final String type, final boolean inf) {
    if(context.data() == null) return error(PROCNODB);

    try {
      final Data data = context.data();
      IndexToken.TYPE index = null;
      if(type.equals(TXT)) {
        data.meta.txtindex = true;
        index = IndexToken.TYPE.TXT;
      } else if(type.equals(ATV)) {
        data.meta.atvindex = true;
        index = IndexToken.TYPE.ATV;
      } else if(type.equals(FTX)) {
        data.meta.ftxindex = true;
        index = IndexToken.TYPE.FTX;
      } else {
        throw new IllegalArgumentException();
      }

      data.meta.finish(data.size);
      buildIndex(index, data);
      return inf ? info(DBINDEXED, perf.getTimer()) : true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Builds the specified index.
   * @param i index to be built.
   * @param d data reference
   * @throws IOException I/O exception
   */
  private void buildIndex(final IndexToken.TYPE i, final Data d)
      throws IOException {

    switch(i) {
      case TXT: buildIndex(i, new ValueBuilder(true), d);  break;
      case ATV: buildIndex(i, new ValueBuilder(false), d); break;
      case FTX: buildIndex(i, d.meta.ftfuzzy ?
          new FZBuilder() : new FTBuilder(), d); break;
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
    data.openIndex(index, builder.build(data));

    if(Prop.debug) {
      BaseX.err("% Index: % (%)\n", index, pp.getTimer(), Performance.getMem());
    }
  }
}
