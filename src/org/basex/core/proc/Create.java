package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.File;
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
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.FTBuilder;
import org.basex.index.Index;
import org.basex.index.IndexBuilder;
import org.basex.index.ValueBuilder;
import org.basex.index.WordBuilder;
import org.basex.io.CachedInput;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * Evaluates the 'create' command. Creates a new database instance for the
 * specified arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Create extends Proc {
  /** XML Suffix. */
  public static final String XMLSUFFIX = ".xml";
  /** XQuery Suffix. */
  public static final String XQSUFFIX = ".xq";

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
  public static final String WRD = "word";
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
    } catch(final RuntimeException e) {
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

    final String file = cmd.arg(1);
    String path = file;
    try {
      // interpret input as XML document...
      if(file.startsWith("<")) {
        return build(new XMLParser(new CachedInput(Token.token(file))), "temp");
      }
      // determine file path and create database
      path = filePath(path, true);
      return build(new XMLParser(path), path);
    } catch(final IOException e) {
      return error(e.getMessage(), path);
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
    String path = cmd.arg(1);
    try {
      path = filePath(path, true);
      return build(new SAXWrapper(path), path);
    } catch(final IOException e) {
      return error(e.getMessage(), path);
    }
  }

  /**
   * Create a database for the specified MAB2 file.
   * @return success of operation
   */
  private boolean mab2() {
    if(cmd.nrArgs() != 2) throw new IllegalArgumentException();

    try {
      // check if file exists
      final String file = filePath(cmd.arg(1), false);
      String db = file.replace('\\', '/');
      if(db.contains("/")) db = db.replaceAll("^.*/(.*)\\..*", "$1");
      Prop.chop  = true;
      Prop.entity   = true;
      Prop.textindex = true;
      Prop.attrindex = true;
      Prop.wordindex = true;
      Prop.ftindex = false;
      return build(new MAB2Parser(file), db + XMLSUFFIX);
    } catch(final IOException e) {
      return error(e.getMessage());
    }
  }

  /**
   * Create database instance for a file hierarchy starting from
   * a specified path.
   * @return success of operation
   */
  private boolean fs() {
    if(cmd.nrArgs() != 3) throw new IllegalArgumentException();
    final String db = cmd.arg(1);
    final String path = cmd.arg(2);

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
   * @param file file to be parsed
   * @return success of operation
   */
  private boolean build(final Parser p, final String file) {
    String err = "";
    final String db = chopPath(file);
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
      if(data.meta.wrdindex) buildIndex(Index.TYPE.WRD, data);
      if(data.meta.ftxindex) buildIndex(Index.TYPE.FTX, data);
      context.data(data);
      
      return Prop.info ? timer(DBCREATED) : true;
    } catch(final RuntimeException ex) {
      throw ex;
    } catch(final FileNotFoundException ex) {
      BaseX.debug(ex);
      err = BaseX.info(FILEWHICH, file);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      err = ex.getMessage();
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
  public static Data xml(final String db, final String fn) {
    try {
      final String f = Create.filePath(fn, true);
      final XMLParser p = new XMLParser(f);
      if(Prop.onthefly) return new MemBuilder().build(p, db);

      Drop.drop(db);
      final Data data = new DiskBuilder().build(p, db);
      if(data.meta.txtindex)
        data.openIndex(Index.TYPE.TXT, new ValueBuilder(true).build(data));
      if(data.meta.atvindex)
        data.openIndex(Index.TYPE.ATV, new ValueBuilder(false).build(data));
      if(data.meta.wrdindex)
        data.openIndex(Index.TYPE.WRD, new WordBuilder().build(data));
      if(data.meta.ftxindex)
        data.openIndex(Index.TYPE.FTX, new FTBuilder().build(data));

      return data;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return null;
    }
  }

  /**
   * Creates an empty database without indexes.
   * @param db name of the database to be created
   * @return database instance
   */
  public static Data empty(final String db) {
    try {
      Prop.textindex = false;
      Prop.attrindex = false;
      Prop.chop = true;

      /** Empty Parser */
      final Parser parser = new Parser("") {
        @Override
        public void parse(final Builder build) { }
        @Override
        public String head() { return ""; }
        @Override
        public double percent() { return 0; }
        @Override
        public String det() { return ""; }
      };
      return new DiskBuilder().build(parser, db);
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
      } else if(type.equals(WRD)) {
        data.meta.wrdindex = true;
        index = Index.TYPE.WRD;
      } else if(type.equals(FTX)) {
        data.meta.ftxindex = true;
        index = Index.TYPE.FTX;
      } else {
        throw new IllegalArgumentException();
      }

      data.meta.write(data.size);
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
      case WRD: buildIndex(i,
          new WordBuilder(), d, Prop.allInfo ? CREATEWORD : null); break;
      case FTX: buildIndex(i,
          new FTBuilder(), d, Prop.allInfo ? CREATEFT : null);
      //case FZY: buildIndex(i,
      //buildIndex(i, new FuzzyBuilder(), d, Prop.allInfo ? CREATEFT : null);}
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

  /**
   * Returns the filename if the specified file exists, or throws an exception.
   * @param file file to be checked
   * @param addXML add xml suffix if file was not found
   * @return null or error string
   * @throws FileNotFoundException file not found exception
   */
  public static String filePath(final String file, final boolean addXML)
      throws FileNotFoundException {

    File f = new File(file.replace('\\', '/'));
    boolean found = f.exists();
    
    if(!found && addXML) {
      final File f2 = new File(file + XMLSUFFIX);
      found = f2.exists();
      if(found) f = f2;
    }
    if(!found) throw new FileNotFoundException(
        BaseX.info(FILEWHICH, f.getAbsoluteFile()));

    return f.getAbsolutePath();
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @param name filename
   * @return chopped filename
   */
  public static String chopPath(final String name) {
    String n = name.replace('\\', '/');
    final int c = n.lastIndexOf('/');
    if(c > -1) n = n.substring(c + 1);
    return n.endsWith(XMLSUFFIX) ? n.substring(0, n.length() -
        XMLSUFFIX.length()) : n;
  }
}
