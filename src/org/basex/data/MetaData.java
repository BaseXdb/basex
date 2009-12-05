package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.BuildException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Users;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * This class provides meta information on a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public String encoding = Token.UTF8;
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
  public boolean pathindex = true;

  /** Flag for fuzzy indexing. */
  public boolean ftfz;
  /** Flag for full-text stemming. */
  public boolean ftst;
  /** Flag for full-text case sensitivity. */
  public boolean ftcs;
  /** Flag for full-text diacritics removal. */
  public boolean ftdc;
  /** Maximal indexed full-text score. */
  public int ftscmax;
  /** Minimal indexed full-text score. */
  public int ftscmin;
  /** Scoring mode: see {@link Prop#FTSCTYPE}. */
  public int ftsctype;

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
  public String  mount = "";
  /** DeepFS backing path. */
  public String backing = "";
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
    pathindex = prop.is(Prop.PATHINDEX);
    txtindex = prop.is(Prop.TEXTINDEX);
    atvindex = prop.is(Prop.ATTRINDEX);
    ftxindex = prop.is(Prop.FTINDEX);
    ftfz = prop.is(Prop.FTFUZZY);
    ftst = prop.is(Prop.FTST);
    ftdc = prop.is(Prop.FTDC);
    ftcs = prop.is(Prop.FTCS);
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
    if(path.equals(db) && pr.dbpath(db).exists()) return true;

    DataInput in = null;
    try {
      // match filename of database instance
      in = new DataInput(file(db, DATAINFO, pr));
      String str = "", k;
      IO f = null;
      long t = 0;
      while(!(k = Token.string(in.readBytes())).isEmpty()) {
        final String v = Token.string(in.readBytes());
        if(k.equals(DBSTR)) str = v;
        else if(k.equals(DBFNAME)) f = IO.get(v);
        else if(k.equals(DBTIME)) t = Token.toLong(v);
      }
      return f != null && f.eq(IO.get(path)) && STORAGE.equals(str) &&
        f.date() == t;
    } catch(final IOException ex) {
      Main.debug(ex);
      return false;
    } finally {
      try { if(in != null) in.close(); } catch(final IOException ex) { }
    }
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
   * Adds the database suffix to the specified filename and creates
   * a file instance.
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
      final String k = Token.string(in.readBytes());
      if(k.isEmpty()) break;
      if(k.equals(DBPERM)) {
        users = new Users(in);
        continue;
      }
      final String v = Token.string(in.readBytes());
      if(k.equals(DBSTR))         storage   = v;
      else if(k.equals(IDBSTR))   istorage  = v;
      else if(k.equals(DBSIZE))   size      = Token.toInt(v);
      else if(k.equals(DBFNAME))  file      = IO.get(v);
      else if(k.equals(DBFSIZE))  filesize  = Token.toLong(v);
      else if(k.equals(DBNDOCS))  ndocs     = Token.toInt(v);
      else if(k.equals(DBFTDC))   ftdc      = toBool(v);
      else if(k.equals(DBENC))    encoding  = v;
      else if(k.equals(DBHGHT))   height    = Token.toInt(v);
      else if(k.equals(DBCHOP))   chop      = toBool(v);
      else if(k.equals(DBENTITY)) entity    = toBool(v);
      else if(k.equals(DBPTHIDX)) pathindex = toBool(v);
      else if(k.equals(DBTXTIDX)) txtindex  = toBool(v);
      else if(k.equals(DBATVIDX)) atvindex  = toBool(v);
      else if(k.equals(DBFTXIDX)) ftxindex  = toBool(v);
      else if(k.equals(DBFZIDX))  ftfz      = toBool(v);
      else if(k.equals(DBFTST))   ftst      = toBool(v);
      else if(k.equals(DBFTCS))   ftcs      = toBool(v);
      else if(k.equals(DBFTDC))   ftdc      = toBool(v);
      else if(k.equals(DBSCMAX))  ftscmax   = Token.toInt(v);
      else if(k.equals(DBSCMIN))  ftscmin   = Token.toInt(v);
      else if(k.equals(DBSCTYPE)) ftsctype  = Token.toInt(v);
      else if(k.equals(DBTIME))   time      = Token.toLong(v);
      else if(k.equals(DBUTD))    uptodate  = toBool(v);
      else if(k.equals(DBLID))    lastid    = Token.toInt(v);
      else if(k.equals(DBMNT))    mount     = v;
      else if(k.equals(DBBCK))    backing   = v;
      else if(k.equals(DBDEEPFS)) deepfs    = toBool(v);
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
  synchronized void write(final DataOutput out) throws IOException {
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
    writeInfo(out, DBPTHIDX, pathindex);
    writeInfo(out, DBTXTIDX, txtindex);
    writeInfo(out, DBATVIDX, atvindex);
    writeInfo(out, DBFTXIDX, ftxindex);
    writeInfo(out, DBFZIDX,  ftfz);
    writeInfo(out, DBFTST,   ftst);
    writeInfo(out, DBFTCS,   ftcs);
    writeInfo(out, DBFTDC,   ftdc);
    writeInfo(out, DBSCMAX,  ftscmax);
    writeInfo(out, DBSCMIN,  ftscmin);
    writeInfo(out, DBSCTYPE, ftsctype);
    writeInfo(out, DBTIME,   time);
    writeInfo(out, DBUTD,    uptodate);
    writeInfo(out, DBLID,    lastid);
    writeInfo(out, DBBCK,    backing);
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
