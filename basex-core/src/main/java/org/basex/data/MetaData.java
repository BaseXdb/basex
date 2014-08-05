package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Database path. Set to {@code null} if database is in main memory. */
  public final IOFile path;
  /** Database options. */
  public final MainOptions options;

  /** Database name. */
  public volatile String name;
  /** Database users. */
  public volatile Users users;

  /** Encoding of original document. */
  public volatile String encoding = UTF8;
  /** Path to original document. */
  public volatile String original = "";
  /** Size of original document. */
  public volatile long filesize;
  /** Number of stored documents. */
  public volatile int ndocs;
  /** Timestamp of original document. */
  public volatile long time;

  /** Flag for whitespace chopping. */
  public volatile boolean chop;
  /** Flag for activated automatic index update. */
  public volatile boolean updindex;
  /** Indicates if a text index exists. */
  public volatile boolean textindex;
  /** Indicates if a attribute index exists. */
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

  /** Flag for out-of-date index structures.
   *  Will be removed as soon as all indexes support updates. */
  public volatile boolean uptodate = true;
  /** Flag to indicate possible corruption. */
  public volatile boolean corrupt;
  /** Dirty flag. */
  public volatile boolean dirty;

  /** Table size. */
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
   * Constructor, specifying the database options.
   * @param opts database options
   */
  public MetaData(final MainOptions opts) {
    this("", opts, null);
  }

  /**
   * Constructor, specifying the database name and context.
   * @param db name of the database
   * @param ctx database context
   */
  public MetaData(final String db, final Context ctx) {
    this(db, ctx.options, ctx.globalopts);
  }

  /**
   * Constructor, specifying the database name.
   * @param db name of the database
   * @param opts database options
   * @param gopts global options
   */
  private MetaData(final String db, final MainOptions opts, final GlobalOptions gopts) {
    path = gopts != null ? gopts.dbpath(db) : null;
    options = opts;
    name = db;
    chop = options.get(MainOptions.CHOP);
    createtext = options.get(MainOptions.TEXTINDEX);
    createattr = options.get(MainOptions.ATTRINDEX);
    createftxt = options.get(MainOptions.FTINDEX);
    diacritics = options.get(MainOptions.DIACRITICS);
    stemming = options.get(MainOptions.STEMMING);
    casesens = options.get(MainOptions.CASESENS);
    updindex = options.get(MainOptions.UPDINDEX);
    maxlen = options.get(MainOptions.MAXLEN);
    maxcats = options.get(MainOptions.MAXCATS);
    stopwords = options.get(MainOptions.STOPWORDS);
    language = Language.get(options);
    users = new Users(null);
  }

  // STATIC METHODS ==========================================================

  /**
   * Normalizes a database path. Converts backslashes and
   * removes duplicate and leading slashes.
   * Returns {@code null} if the path contains invalid characters.
   * @param path input path
   * @return normalized path, or {@code null}
   */
  public static String normPath(final String path) {
    final StringBuilder sb = new StringBuilder();
    boolean slash = false;
    for(int p = 0; p < path.length(); p++) {
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
   * @param io current file
   * @return file length
   */
  private static long dbsize(final IOFile io) {
    long s = 0;
    if(io.isDir()) {
      for(final IOFile f : io.children()) s += dbsize(f);
    } else {
      s += io.length();
    }
    return s;
  }

  /**
   * Creates a database file.
   * @param path database path
   * @param fn filename
   * @return database filename
   */
  public static IOFile file(final IOFile path, final String fn) {
    return new IOFile(path, fn + IO.BASEXSUFFIX);
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
   * @param fn filename
   * @return database filename
   */
  public IOFile dbfile(final String fn) {
    return file(path, fn);
  }

  /**
   * Returns the binary directory.
   * @return binary directory
   */
  public IOFile binaries() {
    return new IOFile(path, IO.RAW);
  }

  /**
   * Returns the specified binary file, or {@code null} if the resource
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
   * @param pat file pattern, or {@code null} if all files are to be deleted
   * @return result of check
   */
  public synchronized boolean drop(final String pat) {
    return path != null && DropDB.drop(path, pat + IO.BASEXSUFFIX);
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
  public void read(final DataInput in) throws IOException {
    String storage = "", istorage = "";
    while(true) {
      final String k = string(in.readToken());
      if(k.isEmpty()) break;
      if(k.equals(DBPERM)) {
        //users.read(in);
      } else {
        final String v = string(in.readToken());
        if(k.equals(DBSTR))           storage    = v;
        else if(k.equals(IDBSTR))     istorage   = v;
        else if(k.equals(DBFNAME))    original   = v;
        else if(k.equals(DBENC))      encoding   = v;
        else if(k.equals(DBFTSW))     stopwords  = v;
        else if(k.equals(DBFTLN))     language   = Language.get(v);
        else if(k.equals(DBSIZE))     size       = toInt(v);
        else if(k.equals(DBNDOCS))    ndocs      = toInt(v);
        else if(k.equals(DBSCTYPE))   scoring    = toInt(v);
        else if(k.equals(DBMAXLEN))   maxlen     = toInt(v);
        else if(k.equals(DBMAXCATS))  maxcats    = toInt(v);
        else if(k.equals(DBLASTID))   lastid     = toInt(v);
        else if(k.equals(DBTIME))     time       = toLong(v);
        else if(k.equals(DBFSIZE))    filesize   = toLong(v);
        else if(k.equals(DBFTDC))     diacritics = toBool(v);
        else if(k.equals(DBCHOP))     chop       = toBool(v);
        else if(k.equals(DBUPDIDX))   updindex   = toBool(v);
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
    writeInfo(out, DBNDOCS,    ndocs);
    writeInfo(out, DBENC,      encoding);
    writeInfo(out, DBSIZE,     size);
    writeInfo(out, DBCHOP,     chop);
    writeInfo(out, DBUPDIDX,   updindex);
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
    out.writeToken(token(DBPERM));
    users.write(out);
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

  // PRIVATE METHODS ==========================================================

  /**
   * Converts the specified string to a boolean value.
   * @param v value
   * @return result
   */
  private static boolean toBool(final String v) {
    return "1".equals(v);
  }

  /**
   * Writes a boolean option to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String k, final boolean v)
      throws IOException {
    writeInfo(out, k, v ? "1" : "0");
  }

  /**
   * Writes a numeric option to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String k, final long v)
      throws IOException {
    writeInfo(out, k, Long.toString(v));
  }

  /**
   * Writes a string option to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException I/O exception
   */
  private static void writeInfo(final DataOutput out, final String k, final String v)
      throws IOException {
    out.writeToken(token(k));
    out.writeToken(token(v));
  }

}
