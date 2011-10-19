package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;

import org.basex.build.BuildException;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.Users;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.Util;
import org.basex.util.ft.Language;

/**
 * This class provides meta information on a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Properties. */
  public final Prop prop;

  /** Database name. */
  public String name;
  /** Database users. */
  public Users users;

  /** Encoding of original document. */
  public String encoding = UTF8;
  /** Path to original document. */
  public String original = "";
  /** Size of original document. */
  public long filesize;
  /** Number of stored documents. */
  public int ndocs;
  /** Timestamp of original document. */
  public long time;
  /** Flag for whitespace chopping. */
  public boolean chop;

  /** Flag for activated text index. */
  public boolean textindex;
  /** Flag for activated attribute value index. */
  public boolean attrindex;
  /** Flag for activated full-text index. */
  public boolean ftindex;
  /** Flag for activated path summary. */
  public boolean pathindex = true;

  /** Flag for wildcard indexing. */
  public boolean wildcards;
  /** Flag for full-text stemming. */
  public boolean stemming;
  /** Flag for full-text case sensitivity. */
  public boolean casesens;
  /** Flag for full-text diacritics removal. */
  public boolean diacritics;
  /** Language of full-text search index. */
  public Language language;
  /** Maximal indexed full-text score. */
  public int maxscore;
  /** Minimal indexed full-text score. */
  public int minscore;
  /** Scoring mode: see {@link Prop#SCORING}. */
  public int scoring;

  /** Flag for out-of-date index structures.
   *  Will be removed as soon as all indexes support updates. */
  public boolean uptodate = true;
  /** Flag for out-of-date indexes. */
  public boolean oldindex;
  /** Dirty flag. */
  public boolean dirty;
  /** Table size. */
  public int size;
  /** Last (highest) id assigned to a node. */
  public int lastid = -1;

  /** Database path. Set to {@code null} for main memory instances. */
  private final File path;

  /**
   * Constructor, specifying the database properties.
   * @param pr database properties
   */
  public MetaData(final Prop pr) {
    this("", pr, null);
  }

  /**
   * Constructor, specifying the database name and context.
   * @param db database name
   * @param ctx database context
   */
  public MetaData(final String db, final Context ctx) {
    this(db, ctx.prop, ctx.mprop);
  }

  /**
   * Constructor, specifying the database name.
   * @param db database name
   * @param pr database properties
   * @param mprop main properties
   */
  public MetaData(final String db, final Prop pr, final MainProp mprop) {
    path = mprop != null ? mprop.dbpath(db) : null;
    prop = pr;
    name = db;
    chop = prop.is(Prop.CHOP);
    pathindex = prop.is(Prop.PATHINDEX);
    wildcards = prop.is(Prop.WILDCARDS);
    stemming = prop.is(Prop.STEMMING);
    diacritics = prop.is(Prop.DIACRITICS);
    casesens = prop.is(Prop.CASESENS);
    scoring = prop.num(Prop.SCORING);
    language = Language.get(prop);
    users = new Users(false);
  }

  // STATIC METHODS ==========================================================

  /**
   * Checks if the specified file path refers to the specified database.
   * @param path file path
   * @param db database name
   * @param mprop main properties
   * @return result of check
   */
  public static boolean found(final String path, final String db,
      final MainProp mprop) {

    // return true if the database exists and if the
    // specified path and database name equal each other
    final File file = mprop.dbpath(db);
    final boolean exists = file.exists();
    if(!exists || path.equals(db)) return exists;

    final IO io = IO.get(path);
    DataInput in = null;
    try {
      // return true if the storage version is up-to-date and
      // if the original and the specified file path and date are equal
      in = new DataInput(file(file, DATAINF));
      boolean ok = true;
      int i = 3;
      String k;
      while(i != 0 && !(k = string(in.readToken())).isEmpty()) {
        final String v = string(in.readToken());
        if(k.equals(DBSTR)) {
          ok &= STORAGE.equals(v);
          i--;
        } else if(k.equals(DBFNAME)) {
          ok &= io.eq(IO.get(v));
          i--;
        } else if(k.equals(DBTIME)) {
          ok &= io.date() == toLong(v);
          i--;
        }
      }
      return i == 0 && ok;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    } finally {
      if(in != null) try { in.close(); } catch(final IOException ex) { }
    }
  }

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
   * Checks if the specified database name is valid, matching the pattern
   * {@code [-\w]+}, or {@code [-\w*?,]+} if the glob flag is activated.
   * @param name name to be checked
   * @param glob allow glob syntax
   * @return result of check
   */
  public static boolean validName(final String name, final boolean glob) {
    if(name == null) return false;
    // faster than a regular expression..
    final int nl = name.length();
    for(int n = 0; n < nl; n++) {
      final char ch = name.charAt(n);
      if((!glob || ch != '?' && ch != '*' && ch != ',') &&
          !letterOrDigit(ch) && ch != '-') return false;
    }
    return nl != 0;
  }

  // PUBLIC METHODS ===========================================================

  /**
   * Returns the disk size of the database.
   * @return database size
   */
  public long dbsize() {
    return path != null ? dbsize(new IOFile(path)) : 0;
  }

  /**
   * Returns the disk timestamp of the database.
   * @return database size
   */
  public long dbtime() {
    return path != null ? new IOFile(path).date() : 0;
  }

  /**
   * Calculates the database size.
   * @param io current file
   * @return file length
   */
  private long dbsize(final IOFile io) {
    long s = 0;
    if(io.isDir()) {
      for(final IOFile f : io.children()) s += dbsize(f);
    } else {
      s += io.length();
    }
    return s;
  }

  /**
   * Returns a file instance for the specified database file.
   * Should only be called if database is disk-based.
   * @param fn filename
   * @return database filename
   */
  public File dbfile(final String fn) {
    return file(path, fn);
  }

  /**
   * Returns the binary directory.
   * @return binary directory
   */
  public IOFile binaries() {
    return new IOFile(path, M_RAW);
  }

  /**
   * Returns the specified binary file, or {@code null} if the resource
   * path cannot be resolved (e.g. points to a parent directory).
   * @param pth internal file path
   * @return binary directory
   */
  public IOFile binary(final String pth) {
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
        users.read(in);
      } else {
        final String v = string(in.readToken());
        if(k.equals(DBSTR))         storage    = v;
        else if(k.equals(DBFNAME))  original   = v;
        else if(k.equals(DBTIME))   time       = toLong(v);
        else if(k.equals(IDBSTR))   istorage   = v;
        else if(k.equals(DBSIZE))   size       = toInt(v);
        else if(k.equals(DBFSIZE))  filesize   = toLong(v);
        else if(k.equals(DBNDOCS))  ndocs      = toInt(v);
        else if(k.equals(DBFTDC))   diacritics = toBool(v);
        else if(k.equals(DBENC))    encoding   = v;
        else if(k.equals(DBCHOP))   chop       = toBool(v);
        else if(k.equals(DBPTHIDX)) pathindex  = toBool(v);
        else if(k.equals(DBTXTIDX)) textindex  = toBool(v);
        else if(k.equals(DBATVIDX)) attrindex  = toBool(v);
        else if(k.equals(DBFTXIDX)) ftindex    = toBool(v);
        else if(k.equals(DBWCIDX))  wildcards  = toBool(v);
        else if(k.equals(DBFTST))   stemming   = toBool(v);
        else if(k.equals(DBFTCS))   casesens   = toBool(v);
        else if(k.equals(DBFTDC))   diacritics = toBool(v);
        else if(k.equals(DBFTLN))   language   = Language.get(v);
        else if(k.equals(DBSCMAX))  maxscore   = toInt(v);
        else if(k.equals(DBSCMIN))  minscore   = toInt(v);
        else if(k.equals(DBSCTYPE)) scoring    = toInt(v);
        else if(k.equals(DBUTD))    uptodate   = toBool(v);
        else if(k.equals(DBLID))    lastid     = toInt(v);
      }
    }
    if(!storage.equals(STORAGE)) throw new BuildException(DBUPDATE, storage);
    if(!istorage.equals(ISTORAGE)) {
      oldindex = true;
      update();
    }
  }

  /**
   * Writes the meta data to the specified output stream.
   * @param out output stream
   * @throws IOException I/O Exception
   */
  void write(final DataOutput out) throws IOException {
    writeInfo(out, DBSTR,    STORAGE);
    writeInfo(out, DBFNAME,  original);
    writeInfo(out, DBTIME,   time);
    writeInfo(out, IDBSTR,   ISTORAGE);
    writeInfo(out, DBFSIZE,  filesize);
    writeInfo(out, DBNDOCS,  ndocs);
    writeInfo(out, DBENC,    encoding);
    writeInfo(out, DBSIZE,   size);
    writeInfo(out, DBCHOP,   chop);
    writeInfo(out, DBPTHIDX, pathindex);
    writeInfo(out, DBTXTIDX, textindex);
    writeInfo(out, DBATVIDX, attrindex);
    writeInfo(out, DBFTXIDX, ftindex);
    writeInfo(out, DBWCIDX,  wildcards);
    writeInfo(out, DBFTST,   stemming);
    writeInfo(out, DBFTCS,   casesens);
    writeInfo(out, DBFTDC,   diacritics);
    writeInfo(out, DBSCMAX,  maxscore);
    writeInfo(out, DBSCMIN,  minscore);
    writeInfo(out, DBSCTYPE, scoring);
    writeInfo(out, DBUTD,    uptodate);
    writeInfo(out, DBLID,    lastid);
    if(language != null) writeInfo(out, DBFTLN, language.toString());
    out.writeString(DBPERM);
    users.write(out);
    out.write(0);
  }

  /**
   * Notifies the meta structures of an update and invalidates the indexes.
   */
  void update() {
    // update database timestamp
    time = System.currentTimeMillis();
    uptodate = false;
    dirty = true;
    // reset of flags might be skipped if id/pre mapping is supported
    textindex = false;
    attrindex = false;
    ftindex = false;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Converts the specified string to a boolean value.
   * @param v value
   * @return result
   */
  private boolean toBool(final String v) {
    return v.equals("1");
  }

  /**
   * Writes a boolean property to the specified output.
   * @param out output stream
   * @param k key
   * @param pr property to write
   * @throws IOException I/O exception
   */
  private void writeInfo(final DataOutput out, final String k,
      final boolean pr) throws IOException {
    writeInfo(out, k, pr ? "1" : "0");
  }

  /**
   * Writes a numeric property to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException I/O exception
   */
  private void writeInfo(final DataOutput out, final String k,
      final long v) throws IOException {
    writeInfo(out, k, Long.toString(v));
  }

  /**
   * Writes a string property to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException I/O exception
   */
  private void writeInfo(final DataOutput out, final String k,
      final String v) throws IOException {
    out.writeString(k);
    out.writeString(v);
  }

  /**
   * Creates a database file instance.
   * @param path database path
   * @param fn filename
   * @return database filename
   */
  private static File file(final File path, final String fn) {
    return new File(path, fn + IO.BASEXSUFFIX);
  }
}
