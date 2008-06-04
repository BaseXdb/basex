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
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Progress;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.FTBuilder;
import org.basex.index.FZBuilder;
import org.basex.index.Index;
import org.basex.index.IndexBuilder;
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
      if(type.equals(XML)) return Prop.intparse ? xml() : sax();
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
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();

    final IO file = new IO(cmd.arg(1));
    if(!file.exists()) return error(FILEWHICH, file);
    final String db = file.dbname();
    
    try {
      return build(new XMLParser(file), db);
    } catch(final IOException e) {
      return error(e.getMessage(), db);
    }
  }

  /**
   * Create a database for the specified XML file
   * with a conventional SAX parser.
   * @return success of operation
   */
  private boolean sax() {
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();

    // determine file path and create database
    final IO file = new IO(cmd.arg(1));
    if(!file.exists()) return error(FILEWHICH, file);
    final String db = file.dbname();

    return build(new SAXWrapper(file), db);
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
    if(cmd.nrArgs() != 3) throw new IllegalArgumentException();
    final String db = cmd.arg(1);
    final IO path = new IO(cmd.arg(2));

    Prop.chop = true;
    Prop.entity = true;
    Prop.mainmem = false;

    return build(new FSParser(path), db);
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

      if(data.meta.txtindex) buildIndex(Index.TYPE.TXT, data);
      if(data.meta.atvindex) buildIndex(Index.TYPE.ATV, data);
      if(data.meta.ftxindex) buildIndex(
          data.meta.fzindex ? Index.TYPE.FUY : Index.TYPE.FTX, data);
      context.data(data);
      
      return Prop.info ? timer(DBCREATED) : true;
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
      if(!fn.exists()) return null;

      final Parser p = Prop.intparse ? new XMLParser(fn) : new SAXWrapper(fn);
      if(Prop.onthefly) return new MemBuilder().build(p, db);

      final Data data = new DiskBuilder().build(p, db);
      if(data.meta.txtindex)
        data.openIndex(Index.TYPE.TXT, new ValueBuilder(true).build(data));
      if(data.meta.atvindex)
        data.openIndex(Index.TYPE.ATV, new ValueBuilder(false).build(data));
      if(data.meta.ftxindex) {
        if(data.meta.fzindex)
          data.openIndex(Index.TYPE.FUY, new FZBuilder().build(data));
        else data.openIndex(Index.TYPE.FTX, new FTBuilder().build(data));
      }
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
      Index.TYPE index = null;
      if(type.equals(TXT)) {
        data.meta.txtindex = true;
        index = Index.TYPE.TXT;
      } else if(type.equals(ATV)) {
        data.meta.atvindex = true;
        index = Index.TYPE.ATV;
      } else if(type.equals(FTX)) {
        data.meta.ftxindex = true;
        index = data.meta.fzindex ? Index.TYPE.FUY : Index.TYPE.FTX;
      } else {
        throw new IllegalArgumentException();
      }

      data.meta.finish(data.size);
      buildIndex(index, data);
      return inf ? timer(DBINDEXED) : true;
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
  private void buildIndex(final Index.TYPE i, final Data d) throws IOException {
    switch(i) {
      case TXT: buildIndex(i,
          new ValueBuilder(true), d, Prop.allInfo ? CREATETEXT : null);  break;
      case ATV: buildIndex(i,
          new ValueBuilder(false), d, Prop.allInfo ? CREATEATTR : null); break;
      case FUY: buildIndex(i,
          new FZBuilder(), d, Prop.allInfo ? CREATEFT : null); break;
          //new FuzzyBuilder(), d, Prop.allInfo ? CREATEFT : null); break;
      case FTX: buildIndex(i,
            new FTBuilder(), d, Prop.allInfo ? CREATEFT : null); 
    }
  }

  /**
   * Builds a new index.
   * @param index index to be built.
   * @param builder builder instance
   * @param data data reference
   * @param inf info string
   * @throws IOException I/O exception
   */
  private void buildIndex(final Index.TYPE index, final IndexBuilder builder,
      final Data data, final String inf) throws IOException {

    final Performance pp = new Performance();
    progress((Progress) builder);
    data.closeIndex(index);
    data.openIndex(index, builder.build(data));
    if(inf != null) info(inf + NL, pp.getTimer());
  }
}
