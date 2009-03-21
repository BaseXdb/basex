package org.basex.data;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.core.Prop;
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
  /** Database name. */
  public String dbname;
  /** FS mount point. */
  public String mount = Prop.mountpoint;
  /** FS backing path. */
  public String backing = Prop.backingpath;

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
  public boolean chop = Prop.chop;
  /** Flag for entity parsing. */
  public boolean entity = Prop.entity;
  /** Flag for creating a text index. */
  public boolean txtindex = Prop.textindex;
  /** Flag for creating a attribute value index. */
  public boolean atvindex = Prop.attrindex;
  /** Flag for creating a fulltext index. */
  public boolean ftxindex = Prop.ftindex;
  
  /** Flag for iterator optimized storage within ftindex. */
  public boolean ftittr = Prop.ftittr;
  /** Flag for fuzzy indexing. */
  public boolean ftfz = Prop.ftfuzzy;
  /** Flag for fulltext stemming. */
  public boolean ftst = Prop.ftst;
  /** Flag for fulltext case sensitivity. */
  public boolean ftcs = Prop.ftcs;
  /** Flag for fulltext diacritics removal. */
  public boolean ftdc = Prop.ftdc;

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

  /**
   * Constructor, specifying the database name.
   * @param db database name
   */
  public MetaData(final String db) {
    dbname = db;
  }

  /**
   * Checks if the specified file path refers to the specified database.
   * @param path file path
   * @param db database name
   * @return result of check
   */
  public static boolean found(final String path, final String db) {
    // true is returned if path and database name are equal and if the db exists
    if(path.equals(db) && IO.dbpath(db).exists()) return true;

    DataInput in = null;
    try {
      // match filename of database instance
      in = new DataInput(db, DATAINFO);
      String str = "", k;
      IO f = null;
      long t = 0;
      while((k = in.readString()).length() != 0) {
        final String v = in.readString();
        if(k.equals(DBSTORAGE)) str = v;
        else if(k.equals(DBFNAME)) f = IO.get(v);
        else if(k.equals(DBTIME)) t = Token.toLong(v);
      }
      return f != null && f.eq(IO.get(path)) && f.date() == t &&
        STORAGE.equals(str);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return false;
    } finally {
      try { if(in != null) in.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Notifies the meta structures of an update.
   * Deletes/resets the indexes.
   */
  public void update() {
    txtindex = false;
    atvindex = false;
    ftxindex = false;
    uptodate = false;
    dirty = true;
  }

  /**
   * Opens the metadata for the current database and returns the table size.
   * @param in input stream
   * @throws IOException I/O Exception
   */
  public void read(final DataInput in) throws IOException {
    String storage = "", istorage = "";
    while(true) {
      final String k = in.readString();
      if(k.length() == 0) break;
      final String v = in.readString();

      if(k.equals(DBSTORAGE))       storage  = v;
      else if(k.equals(IDBSTORAGE)) istorage = v;
      else if(k.equals(DBSIZE))     size     = Token.toInt(v);
      else if(k.equals(DBFNAME))    file     = IO.get(v);
      else if(k.equals(DBFSIZE))    filesize = Token.toLong(v);
      else if(k.equals(DBNDOCS))    ndocs    = Token.toInt(v);
      else if(k.equals(DBFTDC))     ftdc     = toBool(v);
      else if(k.equals(DBENCODING)) encoding = v;
      else if(k.equals(DBHEIGHT))   height   = Token.toInt(v);
      else if(k.equals(DBCHOPPED))  chop     = toBool(v);
      else if(k.equals(DBENTITY))   entity   = toBool(v);
      else if(k.equals(DBTXTINDEX)) txtindex = toBool(v);
      else if(k.equals(DBATVINDEX)) atvindex = toBool(v);
      else if(k.equals(DBFTXINDEX)) ftxindex = toBool(v);
      else if(k.equals(DBFZINDEX))  ftfz     = toBool(v);
      else if(k.equals(DBFTSTEM))   ftst     = toBool(v);
      else if(k.equals(DBFTCS))     ftcs     = toBool(v);
      else if(k.equals(DBFTDC))     ftdc     = toBool(v);
      else if(k.equals(DBTIME))     time     = Token.toLong(v);
      else if(k.equals(DBUPTODATE)) uptodate = toBool(v);
      else if(k.equals(DBLASTID))   lastid   = Token.toInt(v);
      else if(k.equals(MOUNT))      mount    = v;
      else if(k.equals(BACKING))    backing  = v;
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
   * Writes the database to the specified path.
   * @param out output stream
   * @throws IOException IO Exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
    writeInfo(out, DBSTORAGE,  STORAGE);
    writeInfo(out, IDBSTORAGE, ISTORAGE);
    writeInfo(out, DBFNAME,    file.path());
    writeInfo(out, DBFSIZE,    filesize);
    writeInfo(out, DBNDOCS,    ndocs);
    writeInfo(out, DBENCODING, encoding);
    writeInfo(out, DBHEIGHT,   height);
    writeInfo(out, DBSIZE,     size);
    writeInfo(out, DBCHOPPED,  chop);
    writeInfo(out, DBENTITY,   entity);
    writeInfo(out, DBTXTINDEX, txtindex);
    writeInfo(out, DBATVINDEX, atvindex);
    writeInfo(out, DBFTXINDEX, ftxindex);
    writeInfo(out, DBFZINDEX,  ftfz);
    writeInfo(out, DBFTSTEM,   ftst);
    writeInfo(out, DBFTCS,     ftcs);
    writeInfo(out, DBFTDC,     ftdc);
    writeInfo(out, DBTIME,     time);
    writeInfo(out, DBUPTODATE, uptodate);
    writeInfo(out, DBLASTID,   lastid);
//    if(Prop.fuse) {
//      writeInfo(out, MOUNT,    mount);
//      writeInfo(out, BACKING,  backing);
//    }
    out.writeString("");
  }

  /**
   * Writes a boolean property to the specified output.
   * @param out output stream
   * @param k key
   * @param prop property to write
   * @throws IOException in case the info could not be written
   */
  private void writeInfo(final DataOutput out, final String k,
      final boolean prop) throws IOException {
    writeInfo(out, k, prop ? "1" : "0");
  }

  /**
   * Writes a numeric property to the specified output.
   * @param out output stream
   * @param k key
   * @param v value
   * @throws IOException in case the info could not be written
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
   * @throws IOException in case the info could not be written
   */
  private void writeInfo(final DataOutput out, final String k,
      final String v) throws IOException {
    out.writeString(k);
    out.writeString(v);
  }
}
