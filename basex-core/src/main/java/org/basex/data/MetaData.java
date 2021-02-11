package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Strings.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * This class provides meta information on a database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Database directory. Set to {@code null} if database is in main memory. */
  public final IOFile dir;

  /** Database name. */
  public String name;

  /** Path to initially imported resources. */
  public String original = "";
  /** Size of initially imported resources. */
  public long inputsize;
  /** Database timestamp. */
  public long time;
  /** Number of stored XML documents. */
  public int ndocs;

  /** Indicates if a text index exists. */
  public boolean textindex;
  /** Indicates if an attribute index exists. */
  public boolean attrindex;
  /** Indicates if a token index exists. */
  public boolean tokenindex;
  /** Indicates if a full-text index exists. */
  public boolean ftindex;

  /** Flag for activated automatic index update. */
  public boolean updindex;
  /** Flag for automatic index updating. */
  public boolean autooptimize;

  /** Indicates if the text index is to be recreated. */
  public boolean createtext;
  /** Indicates if the attribute index is to be recreated. */
  public boolean createattr;
  /** Indicates if the token index is to be recreated. */
  public boolean createtoken;
  /** Indicates if the full-text index is to be recreated. */
  public boolean createft;
  /** Text index: names to include. */
  public String textinclude;
  /** Attribute index: names to include. */
  public String attrinclude;
  /** Token index: names to tokenize. */
  public String tokeninclude;
  /** Full-text index: names to include. */
  public String ftinclude;

  /** Flag for full-text stemming. */
  public boolean stemming;
  /** Flag for full-text case sensitivity. */
  public boolean casesens;
  /** Flag for full-text diacritics removal. */
  public boolean diacritics;
  /** Full-text stopword file. */
  public String stopwords;

  /** Maximum number of categories. */
  public int maxcats;
  /** Maximum length of index entries. */
  public int maxlen;
  /** Split size for creating indexes. */
  public int splitsize;

  /** Language of full-text search index. */
  public Language language;

  /** Indicates if index structures are out-dated. */
  public boolean uptodate = true;
  /** Indicate if the database may be corrupt. */
  public boolean corrupt;
  /** Dirty flag. */
  public boolean dirty;

  /** Number of nodes. */
  public int size;
  /** Last (highest) id assigned to a node. Can be {@code -1} if database is empty. */
  public int lastid = -1;

  /** Flag for out-of-date indexes. */
  private boolean oldindex;

  /**
   * Constructor for a main-memory database instance.
   * @param options database options
   */
  public MetaData(final MainOptions options) {
    this("", null, options);
  }

  /**
   * Constructor.
   * @param name name of the database
   * @param options database options
   * @param sopts static options
   */
  public MetaData(final String name, final MainOptions options, final StaticOptions sopts) {
    this(name, sopts.dbPath(name), options);
  }

  /**
   * Constructor.
   * @param name name of the database
   * @param dir database directory ({@code null} if database is in main memory)
   * @param options database options
   */
  private MetaData(final String name, final IOFile dir, final MainOptions options) {
    this.name = name;
    this.dir = dir;
    createtext = options.get(MainOptions.TEXTINDEX);
    createattr = options.get(MainOptions.ATTRINDEX);
    createtoken = options.get(MainOptions.TOKENINDEX);
    createft = options.get(MainOptions.FTINDEX);
    diacritics = options.get(MainOptions.DIACRITICS);
    stemming = options.get(MainOptions.STEMMING);
    casesens = options.get(MainOptions.CASESENS);
    updindex = options.get(MainOptions.UPDINDEX);
    autooptimize = options.get(MainOptions.AUTOOPTIMIZE);
    maxlen = options.get(MainOptions.MAXLEN);
    maxcats = options.get(MainOptions.MAXCATS);
    stopwords = options.get(MainOptions.STOPWORDS);
    language = Language.get(options);
    textinclude = options.get(MainOptions.TEXTINCLUDE);
    attrinclude = options.get(MainOptions.ATTRINCLUDE);
    tokeninclude = options.get(MainOptions.TOKENINCLUDE);
    ftinclude = options.get(MainOptions.FTINCLUDE);
    splitsize = options.get(MainOptions.SPLITSIZE);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Normalizes a database path. Converts backslashes and removes duplicate and leading slashes.
   * Returns {@code null} if the path contains invalid characters.
   * @param path input path
   * @return normalized path or {@code null}
   */
  public static String normPath(final String path) {
    // scan path segments
    final StringList list = new StringList();
    final StringBuilder sb = new StringBuilder();
    final int pl = path.length();
    for(int p = 0; p < pl; p++) {
      final char ch = path.charAt(p);
      if(ch == '\\' || ch == '/') {
        if(!addToPath(sb, list)) return null;
      } else {
        if(Prop.WIN && ":*?\"<>\\|".indexOf(ch) != -1) return null;
        sb.append(ch);
      }
    }
    if(!addToPath(sb, list)) return null;
    sb.append(String.join("/", list.finish()));

    // add trailing slash
    if(pl > 0 && sb.length() > 0) {
      final char ch = path.charAt(pl - 1);
      if(ch == '\\' || ch == '/') sb.append('/');
    }
    return sb.toString();
  }

  /**
   * Adds a path segment.
   * @param sb string builder
   * @param list list of segments
   * @return result flag
   */
  private static boolean addToPath(final StringBuilder sb, final StringList list) {
    if(sb.length() != 0) {
      final String seg = sb.toString();
      if(seg.equals("..")) {
        if(list.isEmpty()) return false;
        list.remove(list.size() - 1);
      } else if(!seg.equals(".")) {
        list.add(seg);
      }
      sb.setLength(0);
    }
    return true;
  }

  /**
   * Calculates the database size.
   * @param file current file
   * @return file length
   */
  private static long dbSize(final IOFile file) {
    long s = 0;
    if(file.isDir()) {
      for(final IOFile f : file.children()) s += dbSize(f);
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

  // PUBLIC METHODS ===============================================================================

  /**
   * Returns true if the indexes need to be updated.
   * @return result of check
   */
  public boolean oldindex() {
    return oldindex;
  }

  /**
   * Returns the disk size of the database.
   * @return database size
   */
  public long dbSize() {
    return dir != null ? dbSize(dir) : 0;
  }

  /**
   * Returns the disk timestamp of the database.
   * @return database size
   */
  public long dbTime() {
    return dir != null ? dir.timeStamp() : 0;
  }

  /**
   * Returns a file instance for the specified database file.
   * Should only be called if database is disk-based.
   * @param filename filename
   * @return database filename
   */
  public IOFile dbFile(final String filename) {
    return file(dir, filename);
  }

  /**
   * Returns the binary directory.
   * @return binary directory, or {@code null} if this is a main-memory database
   */
  public IOFile binaryDir() {
    return dir == null ? null : new IOFile(dir, IO.RAW);
  }

  /**
   * Returns a file that indicates ongoing updates.
   * @return updating file
   */
  public IOFile updateFile() {
    return dbFile(DATAUPD);
  }

  /**
   * Returns a reference to the specified binary file.
   * @param path internal file path
   * @return path, or {@code null} if this is a main-memory database of
   *   if the resource path cannot be resolved (e.g. because it points to a parent directory).
   */
  public IOFile binary(final String path) {
    if(dir == null) return null;
    final IOFile bin = binaryDir();
    final IOFile file = new IOFile(bin, path);
    return file.path().startsWith(bin.path()) ? file : null;
  }

  /**
   * Drops the specified database files.
   * Should only be called if database is disk-based.
   * @param pattern file pattern or {@code null} if all files are to be deleted
   * @return result of check
   */
  public synchronized boolean drop(final String pattern) {
    return dir != null && DropDB.drop(dir, pattern + IO.BASEXSUFFIX);
  }

  /**
   * Reads in all meta data.
   * @throws IOException exception
   */
  public void read() throws IOException {
    try(DataInput di = new DataInput(dbFile(DATAINF))) {
      read(di);
    }
  }

  /**
   * Returns if the specified index exists.
   * @param type index type
   * @return result of check
   */
  public boolean index(final IndexType type) {
    switch(type) {
      case TEXT:      return textindex;
      case ATTRIBUTE: return attrindex;
      case TOKEN:     return tokenindex;
      case FULLTEXT:  return ftindex;
      default:        throw Util.notExpected();
    }
  }

  /**
   * Sets availability of the specified index.
   * @param type index type
   * @param exists indicates if the index exists
   */
  public void index(final IndexType type, final boolean exists) {
    switch(type) {
      case TEXT:      textindex = exists; break;
      case ATTRIBUTE: attrindex = exists; break;
      case TOKEN:     tokenindex = exists; break;
      case FULLTEXT:  ftindex = exists; break;
      default:        throw Util.notExpected();
    }
  }

  /**
   * Returns the included names for the specified index type.
   * @param type index type
   * @return index
   */
  public String names(final IndexType type) {
    switch(type) {
      case TEXT:      return textinclude;
      case ATTRIBUTE: return attrinclude;
      case TOKEN:     return tokeninclude;
      case FULLTEXT:  return ftinclude;
      default:        throw Util.notExpected();
    }
  }

  /**
   * Assigns include names options to the specified index type.
   * @param type index type
   * @param options main options
   */
  public void names(final IndexType type, final MainOptions options) {
    switch(type) {
      case TEXT:      textinclude = options.get(MainOptions.TEXTINCLUDE); break;
      case ATTRIBUTE: attrinclude = options.get(MainOptions.ATTRINCLUDE); break;
      case TOKEN:     tokeninclude = options.get(MainOptions.TOKENINCLUDE); break;
      case FULLTEXT:  ftinclude = options.get(MainOptions.FTINCLUDE); break;
      default:        throw Util.notExpected();
    }
  }

  // CLASS METHODS ================================================================================

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
      final String v = Token.string(in.readToken());
      switch(k) {
        case DBSTR:      storage = v; break;
        case IDBSTR:     istorage = v; break;
        case DBFNAME:    original = v; break;
        case DBFTSW:     stopwords = v; break;
        case DBFTLN:     language = Language.get(v); break;
        case DBSIZE:     size = toInt(v); break;
        case DBNDOCS:    ndocs = toInt(v); break;
        case DBMAXLEN:   maxlen = toInt(v); break;
        case DBMAXCATS:  maxcats = toInt(v); break;
        case DBLASTID:   lastid = toInt(v); break;
        case DBTIME:     time = toLong(v); break;
        case DBFSIZE:    inputsize = toLong(v); break;
        case DBFTDC:     diacritics = toBoolean(v); break;
        case DBUPDIDX:   updindex = toBoolean(v); break;
        case DBAUTOOPT:  autooptimize = toBoolean(v); break;
        case DBTXTIDX:   textindex = toBoolean(v); break;
        case DBATVIDX:   attrindex = toBoolean(v); break;
        case DBTOKIDX:   tokenindex = toBoolean(v); break;
        case DBFTXIDX:   ftindex = toBoolean(v); break;
        case DBTXTINC:   textinclude = v; break;
        case DBATVINC:   attrinclude = v; break;
        case DBTOKINC:   tokeninclude = v; break;
        case DBFTXINC:   ftinclude = v; break;
        case DBSPLITS:   splitsize = toInt(v); break;
        case DBCRTTXT:   createtext = toBoolean(v); break;
        case DBCRTATV:   createattr = toBoolean(v); break;
        case DBCRTTOK:   createtoken = toBoolean(v); break;
        case DBCRTFTX:   createft = toBoolean(v); break;
        case DBFTST:     stemming = toBoolean(v); break;
        case DBFTCS:     casesens = toBoolean(v); break;
        case DBUPTODATE: uptodate = toBoolean(v); break;
      }
    }

    // check version of database storage
    if(!storage.equals(STORAGE) && new Version(storage).compareTo(new Version(
        STORAGE)) > 0) throw new BuildException(H_DB_FORMAT, storage);
    // check version of database indexes
    oldindex = !istorage.equals(ISTORAGE) &&
        new Version(istorage).compareTo(new Version(ISTORAGE)) > 0;
    corrupt = dbFile(DATAUPD).exists();
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
    writeInfo(out, DBFSIZE,    inputsize);
    writeInfo(out, DBNDOCS,    ndocs);
    writeInfo(out, DBSIZE,     size);
    writeInfo(out, DBUPDIDX,   updindex);
    writeInfo(out, DBAUTOOPT,  autooptimize);
    writeInfo(out, DBTXTIDX,   textindex);
    writeInfo(out, DBATVIDX,   attrindex);
    writeInfo(out, DBTOKIDX,   tokenindex);
    writeInfo(out, DBFTXIDX,   ftindex);
    writeInfo(out, DBTXTINC,   textinclude);
    writeInfo(out, DBATVINC,   attrinclude);
    writeInfo(out, DBTOKINC,   tokeninclude);
    writeInfo(out, DBFTXINC,   ftinclude);
    writeInfo(out, DBSPLITS,   splitsize);
    writeInfo(out, DBCRTTXT,   createtext);
    writeInfo(out, DBCRTATV,   createattr);
    writeInfo(out, DBCRTTOK,   createtoken);
    writeInfo(out, DBCRTFTX,   createft);
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
      tokenindex = false;
    }
    ftindex = false;
  }

  /**
   * Assigns parser information.
   * @param parser parser
   */
  public void assign(final Parser parser) {
    final IO source = parser.source;
    original = source != null ? source.path() : "";
    inputsize = source != null ? source.length() : 0;
    time = source != null ? source.timeStamp() : System.currentTimeMillis();
  }

  // PRIVATE METHODS ==============================================================================

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
