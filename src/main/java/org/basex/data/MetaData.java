package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;

import org.basex.build.BuildException;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.Users;
import org.basex.io.IO;
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
  /** Path to database. Set to {@code null} for main memory instances. */
  public final File path;

  /** Database name. */
  public String name;
  /** Database users. */
  public Users users;

  /** Encoding of XML document. */
  public String encoding = UTF8;
  /** Path to original input. */
  public String original = "";
  /** Size of original documents. */
  public long filesize;
  /** Number of XML documents. */
  public int ndocs;
  /** Database timestamp. */
  public long time;
  /** Flag for whitespace chopping. */
  public boolean chop;
  /** Flag for entity parsing. */
  public boolean entity;
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
    entity = prop.is(Prop.ENTITY);
    pathindex = prop.is(Prop.PATHINDEX);
    wildcards = prop.is(Prop.WILDCARDS);
    stemming = prop.is(Prop.STEMMING);
    diacritics = prop.is(Prop.DIACRITICS);
    casesens = prop.is(Prop.CASESENS);
    scoring = prop.num(Prop.SCORING);
    language = Language.get(prop.get(Prop.LANGUAGE));
    users = new Users(false);
  }

  /**
   * Checks if the specified file path refers to the specified database.
   * @param path file path
   * @param db database name
   * @param mprop main properties
   * @return result of check
   */
  public static boolean found(final String path, final String db,
      final MainProp mprop) {

    // true is returned if path and database name are equal and if the db exists
    final boolean exists = mprop.dbpath(db).exists();
    if(!exists || path.equals(db)) return exists;

    DataInput in = null;
    try {
      // match filename of database instance
      in = new DataInput(file(mprop.dbpath(db), DATAINFO));
      String str = "", k;
      IO p = null;
      long t = 0;
      while(!(k = string(in.readBytes())).isEmpty()) {
        final String v = string(in.readBytes());
        if(k.equals(DBSTR)) str = v;
        else if(k.equals(DBFNAME)) p = IO.get(v);
        else if(k.equals(DBTIME)) t = toLong(v);
      }
      return p != null && p.eq(IO.get(path)) && STORAGE.equals(str) &&
        p.date() == t;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    } finally {
      if(in != null) try { in.close(); } catch(final IOException ex) { }
    }
  }

  /**
   * Returns the size of the database.
   * @return database size
   */
  public long dbsize() {
    long len = 0;
    for(final File io : path.listFiles()) len += io.length();
    return len;
  }

  /**
   * Returns a file instance for the specified database file.
   * @param fn filename
   * @return database filename
   */
  public File file(final String fn) {
    return file(path, fn);
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

  /**
   * Reads in meta data from the specified stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public void read(final DataInput in) throws IOException {
    String storage = "", istorage = "";
    while(true) {
      final String k = string(in.readBytes());
      if(k.isEmpty()) break;
      if(k.equals(DBPERM)) {
        users.read(in);
      } else {
        final String v = string(in.readBytes());
        if(k.equals(DBSTR))         storage    = v;
        else if(k.equals(IDBSTR))   istorage   = v;
        else if(k.equals(DBSIZE))   size       = toInt(v);
        else if(k.equals(DBFNAME))  original       = v;
        else if(k.equals(DBFSIZE))  filesize   = toLong(v);
        else if(k.equals(DBNDOCS))  ndocs      = toInt(v);
        else if(k.equals(DBFTDC))   diacritics = toBool(v);
        else if(k.equals(DBENC))    encoding   = v;
        else if(k.equals(DBCHOP))   chop       = toBool(v);
        else if(k.equals(DBENTITY)) entity     = toBool(v);
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
        else if(k.equals(DBTIME))   time       = toLong(v);
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
   * Converts the specified string to a boolean value.
   * @param v value
   * @return result
   */
  private boolean toBool(final String v) {
    return v.equals("1");
  }

  /**
   * Writes the meta data to the specified output stream.
   * @param out output stream
   * @throws IOException IO Exception
   */
  void write(final DataOutput out) throws IOException {
    writeInfo(out, DBSTR,    STORAGE);
    writeInfo(out, IDBSTR,   ISTORAGE);
    writeInfo(out, DBFNAME,  original);
    writeInfo(out, DBFSIZE,  filesize);
    writeInfo(out, DBNDOCS,  ndocs);
    writeInfo(out, DBENC,    encoding);
    writeInfo(out, DBSIZE,   size);
    writeInfo(out, DBCHOP,   chop);
    writeInfo(out, DBENTITY, entity);
    writeInfo(out, DBPTHIDX, pathindex);
    writeInfo(out, DBTXTIDX, textindex);
    writeInfo(out, DBATVIDX, attrindex);
    writeInfo(out, DBFTXIDX, ftindex);
    writeInfo(out, DBWCIDX,  wildcards);
    writeInfo(out, DBFTST,   stemming);
    writeInfo(out, DBFTCS,   casesens);
    writeInfo(out, DBFTDC,   diacritics);
    if(language != null) writeInfo(out, DBFTLN, language.name());
    writeInfo(out, DBSCMAX,  maxscore);
    writeInfo(out, DBSCMIN,  minscore);
    writeInfo(out, DBSCTYPE, scoring);
    writeInfo(out, DBTIME,   time);
    writeInfo(out, DBUTD,    uptodate);
    writeInfo(out, DBLID,    lastid);
    out.writeString(DBPERM);
    users.write(out);
    out.write(0);
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
}
