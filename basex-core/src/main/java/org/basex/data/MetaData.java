package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Strings.*;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * This class provides meta information on a database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Database path. Set to {@code null} if database is in main memory. */
  public final IOFile path;

  /** Database name. */
  public volatile String name;

  /** Encoding of original document. */
  public volatile String encoding = UTF8;
  /** Path to original document. */
  public volatile String original = "";
  /** Size of original document. */
  public volatile long filesize;
  /** Timestamp of original document. */
  public volatile long time;
  /** Number of stored documents. */
  public AtomicInteger ndocs = new AtomicInteger();

  /** Flag for whitespace chopping. */
  public volatile boolean chop;
  /** Flag for activated automatic index update. */
  public volatile boolean updindex;
  /** Flag for automatic index updating. */
  public volatile boolean autoopt;
  /** Indicates if a text index exists. */
  public volatile boolean textindex;
  /** Indicates if an attribute index exists. */
  public volatile boolean attrindex;
  /** Indicates if a full-text index exists. */
  public volatile boolean ftxtindex;
  /** Indicates if text index is to be recreated. */
  public volatile boolean createtext;
  /** Indicates if attribute index is to be recreated. */
  public volatile boolean createattr;
  /** Indicates if full-text index is to be recreated. */
  public volatile boolean createftxt;

  /** Flag for full-text stemming. */
  public volatile boolean stemming;
  /** Flag for full-text case sensitivity. */
  public volatile boolean casesens;
  /** Flag for full-text diacritics removal. */
  public volatile boolean diacritics;
  /** Full-text stopword file. */
  public volatile String stopwords = "";

  /** Maximum number of categories. */
  public volatile int maxcats;
  /** Maximum token length. */
  public volatile int maxlen;

  /** Language of full-text search index. */
  public volatile Language language;

  /** Indicates if index structures are out-dated. */
  public volatile boolean uptodate = true;
  /** Indicate if the database may be corrupt. */
  public volatile boolean corrupt;
  /** Dirty flag. */
  public volatile boolean dirty;

  /** Number of nodes. */
  public volatile int size;
  /** Last (highest) id assigned to a node. */
  public volatile int lastid = -1;

  /** Flag for out-of-date indexes. */
  private volatile boolean oldindex;
  /** Flag for out-of-date wildcard index (legacy, deprecated). */
  private volatile boolean wcindex;
  /** Scoring mode (legacy, deprecated). */
  private volatile int scoring;

  /**
   * Constructor for a main-memory database instance.
   * @param options database options
   */
  MetaData(final MainOptions options) {
    this("", options, null);
  }

  /**
   * Constructor.
   * @param name name of the database
   * @param options database options
   * @param sopts static options
   */
  public MetaData(final String name, final MainOptions options, final StaticOptions sopts) {
    this.name = name;
    path = sopts != null ? sopts.dbpath(name) : null;
    chop = options.get(MainOptions.CHOP);
    createtext = options.get(MainOptions.TEXTINDEX);
    createattr = options.get(MainOptions.ATTRINDEX);
    createftxt = options.get(MainOptions.FTINDEX);
    diacritics = options.get(MainOptions.DIACRITICS);
    stemming = options.get(MainOptions.STEMMING);
    casesens = options.get(MainOptions.CASESENS);
    updindex = options.get(MainOptions.UPDINDEX);
    autoopt = options.get(MainOptions.AUTOOPTIMIZE);
    maxlen = options.get(MainOptions.MAXLEN);
    maxcats = options.get(MainOptions.MAXCATS);
    stopwords = options.get(MainOptions.STOPWORDS);
    language = Language.get(options);
  }

  // STATIC METHODS ==========================================================

  /**
   * Normalizes a database path. Converts backslashes and
   * removes duplicate and leading slashes.
   * Returns {@code null} if the path contains invalid characters.
   * @param path input path
   * @return normalized path or {@code null}
   */
  public static String normPath(final String path) {
    final StringBuilder sb = new StringBuilder();
    boolean slash = false;
    final int pl = path.length();
    for(int p = 0; p < pl; p++) {
      final char c = path.charAt(p);
      if(c == '\\' || c == '/') {
        if(!slash && p != 0) sb.append('/');
        slash = true;
      } else {
        if(Prop.WIN && ":*?\"<>\\|".indexOf(c) != -1) return null;
        if(slash) slash = false;
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Calculates the database size.
   * @param file current file
   * @return file length
   */
  private static long dbsize(final IOFile file) {
    long s = 0;
    if(file.isDir()) {
      for(final IOFile f : file.children()) s += dbsize(f);
    } else {
      s += file.length();
    }
    return s;
  }

  /**
   * Creates a database file.
   * @param path database path
   * @param name filename
   * @return database filename
   */
  public static IOFile file(final IOFile path, final String name) {
    return new IOFile(path, name + IO.BASEXSUFFIX);
  }

  // PUBLIC METHODS ===========================================================

  /**
   * Returns true if the indexes need to be updated.
   * @return result of check
   */
  public boolean oldindex() {
    return oldindex || wcindex || scoring != 0;
  }

  /**
   * Returns the disk size of the database.
   * @return database size
   */
  public long dbsize() {
    return path != null ? dbsize(path) : 0;
  }

  /**
   * Returns the disk timestamp of the database.
   * @return database size
   */
  public long dbtime() {
    return path != null ? path.timeStamp() : 0;
  }

  /**
   * Returns a file instance for the specified database file.
   * Should only be called if database is disk-based.
   * @param filename filename
   * @return database filename
   */
  public IOFile dbfile(final String filename) {
    return file(path, filename);
  }

  /**
   * Returns the binary directory.
   * @return binary directory
   */
  public IOFile binaries() {
    return new IOFile(path, IO.RAW);
  }

  /**
   * Returns a file that indicates ongoing updates.
   * @return updating file
   */
  public IOFile updateFile() {
    return dbfile(DATAUPD);
  }

  /**
   * Returns the specified binary file or {@code null} if the resource
   * path cannot be resolved (e.g. if it points to a parent directory).
   * @param pth internal file path
   * @return binary directory
   */
  public IOFile binary(final String pth) {
    if(path == null) return null;
    final IOFile dir = binaries();
    final IOFile file = new IOFile(dir, pth);
    return file.path().startsWith(dir.path()) ? file : null;
  }

  /**
   * Drops the specified database files.
   * Should only be called if database is disk-based.
   * @param pattern file pattern or {@code null} if all files are to be deleted
   * @return result of check
   */
  public synchronized boolean drop(final String pattern) {
    return path != null && DropDB.drop(path, pattern + IO.BASEXSUFFIX);
  }

  /**
   * Reads in all meta data.
   * @throws IOException exception
   */
  public void read() throws IOException {
    try(final DataInput di = new DataInput(dbfile(DATAINF))) {
      read(di);
    }
  }

  /**
   * Reads in meta data from the specified stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  void read(final DataInput in) throws IOException {
    String storage = "", istorage = "";
    while(true) {
      final String k = Token.string(in.readToken());
      if(k.isEmpty()) break;
      if(k.equals(DBPERM)) {
        // legacy (Version < 8)
        for(int u = in.readNum(); u > 0; --u) { in.readToken(); in.readToken(); in.readNum(); }
      } else {
        final String v = Token.string(in.readToken());
        if(k.equals(DBSTR))           storage    = v;
        else if(k.equals(IDBSTR))     istorage   = v;
        else if(k.equals(DBFNAME))    original   = v;
        else if(k.equals(DBENC))      encoding   = v;
        else if(k.equals(DBFTSW))     stopwords  = v;
        else if(k.equals(DBFTLN))     language   = Language.get(v);
        else if(k.equals(DBSIZE))     size       = toInt(v);
        else if(k.equals(DBNDOCS))    ndocs      = new AtomicInteger(toInt(v));
        else if(k.equals(DBSCTYPE))   scoring    = toInt(v);
        else if(k.equals(DBMAXLEN))   maxlen     = toInt(v);
        else if(k.equals(DBMAXCATS))  maxcats    = toInt(v);
        else if(k.equals(DBLASTID))   lastid     = toInt(v);
        else if(k.equals(DBTIME))     time       = toLong(v);
        else if(k.equals(DBFSIZE))    filesize   = toLong(v);
        else if(k.equals(DBFTDC))     diacritics = toBool(v);
        else if(k.equals(DBCHOP))     chop       = toBool(v);
        else if(k.equals(DBUPDIDX))   updindex   = toBool(v);
        else if(k.equals(DBAUTOOPT))  autoopt    = toBool(v);
        else if(k.equals(DBTXTIDX))   textindex  = toBool(v);
        else if(k.equals(DBATVIDX))   attrindex  = toBool(v);
        else if(k.equals(DBFTXIDX))   ftxtindex  = toBool(v);
        else if(k.equals(DBCRTTXT))   createtext = toBool(v);
        else if(k.equals(DBCRTATV))   createattr = toBool(v);
        else if(k.equals(DBCRTFTX))   createftxt = toBool(v);
        else if(k.equals(DBWCIDX))    wcindex    = toBool(v);
        else if(k.equals(DBFTST))     stemming   = toBool(v);
        else if(k.equals(DBFTCS))     casesens   = toBool(v);
        else if(k.equals(DBUPTODATE)) uptodate   = toBool(v);
        // legacy: set up-to-date flag to false if path index does not exist
        else if(k.equals(DBPTHIDX) && !toBool(v)) uptodate = false;
      }
    }

    // check version of database storage
    if(!storage.equals(STORAGE) && new Version(storage).compareTo(new Version(
        STORAGE)) > 0) throw new BuildException(H_DB_FORMAT, storage);
    // check version of database indexes
    oldindex = !istorage.equals(ISTORAGE) &&
        new Version(istorage).compareTo(new Version(ISTORAGE)) > 0;
    corrupt = dbfile(DATAUPD).exists();
    // deactivate full-text index if obsolete trie structure was used
    if(wcindex) ftxtindex = false;
  }

  /**
   * Writes the meta data to the specified output stream.
   * @param out output stream
   * @throws IOException I/O Exception
   */
  void write(final DataOutput out) throws IOException {
    writeInfo(out, DBSTR,      STORAGE);
    writeInfo(out, DBFNAME,    original);
    writeInfo(out, DBTIME,     time);
    writeInfo(out, IDBSTR,     ISTORAGE);
    writeInfo(out, DBFSIZE,    filesize);
    writeInfo(out, DBNDOCS,    ndocs.intValue());
    writeInfo(out, DBENC,      encoding);
    writeInfo(out, DBSIZE,     size);
    writeInfo(out, DBCHOP,     chop);
    writeInfo(out, DBUPDIDX,   updindex);
    writeInfo(out, DBAUTOOPT,  autoopt);
    writeInfo(out, DBTXTIDX,   textindex);
    writeInfo(out, DBATVIDX,   attrindex);
    writeInfo(out, DBFTXIDX,   ftxtindex);
    writeInfo(out, DBCRTTXT,   createtext);
    writeInfo(out, DBCRTATV,   createattr);
    writeInfo(out, DBCRTFTX,   createftxt);
    writeInfo(out, DBFTST,     stemming);
    writeInfo(out, DBFTCS,     casesens);
    writeInfo(out, DBFTDC,     diacritics);
    writeInfo(out, DBFTSW,     stopwords);
    writeInfo(out, DBMAXLEN,   maxlen);
    writeInfo(out, DBMAXCATS,  maxcats);
    writeInfo(out, DBUPTODATE, uptodate);
    writeInfo(out, DBLASTID,   lastid);
    if(language != null) writeInfo(out, DBFTLN, language.toString());
    out.write(0);
  }

  /**
   * Notifies the meta structures of an update and invalidates the indexes.
   */
  public void update() {
    // update database timestamp
    time = System.currentTimeMillis();
    uptodate = false;
    dirty = true;
    if(!updindex) {
      textindex = false;
      attrindex = false;
    }
    ftxtindex = false;
  }

  /**
   * Assigns parser information.
   * @param parser parser
   */
  public void assign(final Parser parser) {
    final IO file = parser.source;
    original = file != null ? file.path() : "";
    filesize = file != null ? file.length() : 0;
    time = file != null ? file.timeStamp() : System.currentTimeMillis();
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Converts the specified string to a boolean value.
   * @param value value
   * @return result
   */
  private static boolean toBool(final String value) {
    return "1".equals(value);
  }

  /**
   * Writes a boolean option to the specified output.
   * @param out output stream
   * @param name key
   * @param value value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String name, final boolean value)
      throws IOException {
    writeInfo(out, name, value ? "1" : "0");
  }

  /**
   * Writes a numeric option to the specified output.
   * @param out output stream
   * @param name key
   * @param value value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String name, final long value)
      throws IOException {
    writeInfo(out, name, Long.toString(value));
  }

  /**
   * Writes a string option to the specified output.
   * @param out output stream
   * @param name key
   * @param value value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String name, final String value)
      throws IOException {
    out.writeToken(Token.token(name));
    out.writeToken(Token.token(value));
  }

}
