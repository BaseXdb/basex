package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.BuildException;
import org.basex.core.Prop;
import org.basex.core.Users;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Util;
import org.basex.util.ft.Language;

/**
 * This class provides meta information on a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Properties. */
  public final Prop prop;

  /** Database name. */
  public String name;
  /** Database users. */
  public Users users;

  /** Encoding of XML document. */
  public String encoding = UTF8;
  /** Original filename of XML document. */
  public IO file;
  /** Original file size of XML document. */
  public long filesize;
  /** Number of XML documents. */
  public int ndocs;
  /** Maximum document height. */
  public int height;
  /** Modification time. */
  public long time;
  /** Flag for whitespace chopping. */
  public boolean chop;
  /** Flag for entity parsing. */
  public boolean entity;
  /** Flag for creating a text index. */
  public boolean txtindex;
  /** Flag for creating a attribute value index. */
  public boolean atvindex;
  /** Flag for creating a full-text index. */
  public boolean ftxindex;
  /** Flag for creating a path summary. */
  public boolean pthindex = true;

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

  /** Flag for removed index structures. */
  public boolean uptodate = true;
  /** Dirty flag. */
  public boolean dirty;
  /** Flag for out-of-dates indexes. */
  public boolean oldindex;
  /** Table size. */
  public int size;
  /** Last (highest) id assigned to a node. */
  public int lastid = -1;

  /** DeepFS mount point. */
  public String mount = "";
  /** Flag for DeepFS instance. */
  public boolean deepfs;

  /**
   * Constructor, specifying the database name.
   * @param db database name
   * @param pr database properties
   */
  public MetaData(final String db, final Prop pr) {
    name = db;
    prop = pr;
    chop = prop.is(Prop.CHOP);
    entity = prop.is(Prop.ENTITY);
    pthindex = prop.is(Prop.PATHINDEX);
    txtindex = prop.is(Prop.TEXTINDEX);
    atvindex = prop.is(Prop.ATTRINDEX);
    ftxindex = prop.is(Prop.FTINDEX);
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
   * @param pr database properties
   * @return result of check
   */
  public static boolean found(final String path, final String db,
      final Prop pr) {

    // true is returned if path and database name are equal and if the db exists
    final boolean exists = pr.dbpath(db).exists();
    if(!exists || path.equals(db)) return exists;

    DataInput in = null;
    try {
      // match filename of database instance
      in = new DataInput(file(db, DATAINFO, pr));
      String str = "", k;
      IO f = null;
      long t = 0;
      while(!(k = string(in.readBytes())).isEmpty()) {
        final String v = string(in.readBytes());
        if(k.equals(DBSTR)) str = v;
        else if(k.equals(DBFNAME)) f = IO.get(v);
        else if(k.equals(DBTIME)) t = toLong(v);
      }
      return f != null && f.eq(IO.get(path)) && STORAGE.equals(str) &&
        f.date() == t;
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
    final File dir = prop.dbpath(name);
    long len = 0;
    if(dir.exists()) for(final File f : dir.listFiles()) len += f.length();
    return len;
  }

  /**
   * Adds the database suffix to the specified filename and creates
   * a file instance.
   * @param fn filename
   * @return database filename
   */
  public File file(final String fn) {
    return file(name, fn, prop);
  }

  /**
   * Creates a database file instance.
   * @param db name of the database
   * @param fn filename
   * @param pr database properties
   * @return database filename
   */
  private static File file(final String db, final String fn, final Prop pr) {
    return new File(pr.get(Prop.DBPATH) + '/' + db + '/' + fn + IO.BASEXSUFFIX);
  }

  /**
   * Notifies the meta structures of an update and invalidates the indexes.
   */
  void update() {
    time = System.currentTimeMillis();
    txtindex = false;
    atvindex = false;
    ftxindex = false;
    uptodate = false;
    dirty = true;
  }

  /**
   * Opens the metadata for the current database and returns the table size.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public void read(final DataInput in) throws IOException {
    String storage = "", istorage = "";
    while(true) {
      final String k = string(in.readBytes());
      if(k.isEmpty()) break;
      if(k.equals(DBPERM)) {
        users = new Users(in);
        continue;
      }
      final String v = string(in.readBytes());
      if(k.equals(DBSTR))         storage    = v;
      else if(k.equals(IDBSTR))   istorage   = v;
      else if(k.equals(DBSIZE))   size       = toInt(v);
      else if(k.equals(DBFNAME))  file       = IO.get(v);
      else if(k.equals(DBFSIZE))  filesize   = toLong(v);
      else if(k.equals(DBNDOCS))  ndocs      = toInt(v);
      else if(k.equals(DBFTDC))   diacritics = toBool(v);
      else if(k.equals(DBENC))    encoding   = v;
      else if(k.equals(DBHGHT))   height     = toInt(v);
      else if(k.equals(DBCHOP))   chop       = toBool(v);
      else if(k.equals(DBENTITY)) entity     = toBool(v);
      else if(k.equals(DBPTHIDX)) pthindex   = toBool(v);
      else if(k.equals(DBTXTIDX)) txtindex   = toBool(v);
      else if(k.equals(DBATVIDX)) atvindex   = toBool(v);
      else if(k.equals(DBFTXIDX)) ftxindex   = toBool(v);
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
      else if(k.equals(DBMNT))    mount      = v;
      else if(k.equals(DBDEEPFS)) deepfs     = toBool(v);
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
    writeInfo(out, DBFNAME,  file.path());
    writeInfo(out, DBFSIZE,  filesize);
    writeInfo(out, DBNDOCS,  ndocs);
    writeInfo(out, DBENC,    encoding);
    writeInfo(out, DBHGHT,   height);
    writeInfo(out, DBSIZE,   size);
    writeInfo(out, DBCHOP,   chop);
    writeInfo(out, DBENTITY, entity);
    writeInfo(out, DBPTHIDX, pthindex);
    writeInfo(out, DBTXTIDX, txtindex);
    writeInfo(out, DBATVIDX, atvindex);
    writeInfo(out, DBFTXIDX, ftxindex);
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
    writeInfo(out, DBMNT,    mount);
    writeInfo(out, DBDEEPFS, deepfs);
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
