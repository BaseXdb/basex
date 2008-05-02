package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Token;

/**
 * This class provides meta information on a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MetaData {
  /** Database name. */
  public String dbname;
  /** Encoding of XML document. */
  public String encoding = Token.UTF8;
  /** Original filename of XML document. */
  public IO file;
  /** Original file size of XML document. */
  public long filesize;
  /** Maximum document height. */
  public int height;
  /** Modification time. */
  public long time;
  /** Flag for whitespace chopping. */
  public boolean chop = Prop.chop;
  /** Flag for entity parsing. */
  public boolean entity = Prop.entity;
  /** Flag for creating a fulltext index. */
  public boolean ftxindex = Prop.ftindex;
  /** Flag for creating a text index. */
  public boolean txtindex = Prop.textindex;
  /** Flag for creating a attribute value index. */
  public boolean atvindex = Prop.attrindex;
  /** Flag for removing the index structures. */
  public boolean newindex = false;
  /** Last (highest) id assigned to a node. */
  public long lastid = -1;

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
    
    try {
      // match filename of database instance
      final DataInput in = new DataInput(db, DATAINFO);
      String key;
      IO f = null;
      long t = 0;
      while((key = in.readString()).length() != 0) {
        final String val = in.readString();
        if(key.equals(DBFNAME)) f = new IO(val);
        if(key.equals(DBTIME)) t = Token.toLong(val);
      }
      in.close();
      return f != null && f.path().equals(path) && f.date() == t;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return false;
    }
  }

  /**
   * Deletes/resets the indexes.
   */
  public void noIndex() {
    txtindex = false;
    atvindex = false;
    ftxindex = false;
    newindex = true;
  }

  /**
   * Opens the metadata for the current database and returns the table size.
   * @throws IOException IO Exception
   * @return table size
   */
  public int read() throws IOException {
    final DataInput in = new DataInput(dbname, DATAINFO);
    String storage = "";
    String istorage = "";
    int size = 0;
    while(true) {
      final String k = in.readString();
      if(k.length() == 0) break;
      final String v = in.readString();

      if(k.equals(DBSTORAGE)) storage = v;
      else if(k.equals(IDBSTORAGE)) istorage = v;
      else if(k.equals(DBFNAME)) file = new IO(v);
      else if(k.equals(DBFSIZE)) filesize = Token.toLong(v);
      else if(k.equals(DBENCODING)) encoding = v;
      else if(k.equals(DBHEIGHT)) height = Token.toInt(v);
      else if(k.equals(DBSIZE)) size = Token.toInt(v);
      else if(k.equals(DBCHOPPED)) chop = v.equals(ON) || v.equals("1");
      else if(k.equals(DBENTITY)) entity = v.equals(ON) || v.equals("1");
      else if(k.equals(DBTXTINDEX)) txtindex = v.equals(ON) || v.equals("1");
      else if(k.equals(DBATVINDEX)) atvindex = v.equals(ON) || v.equals("1");
      else if(k.equals(DBFTXINDEX)) ftxindex = v.equals(ON) || v.equals("1");
      else if(k.equals(DBTIME)) time = Token.toLong(v);
      else if(k.equals(DBLASTID)) lastid = Token.toLong(v);
    }
    in.close();

    if(!storage.equals(STORAGE)) throw new BuildException(DBUPDATE, storage);
    if(!istorage.equals(ISTORAGE)) noIndex();
    return size;
  }

  /**
   * Writes the database to the specified path.
   * @param siz current database size
   * @throws IOException IO Exception
   */
  public synchronized void finish(final int siz) throws IOException {
    final DataOutput inf = new DataOutput(dbname, DATAINFO);
    writeInfo(inf, DBSTORAGE, STORAGE);
    writeInfo(inf, IDBSTORAGE, ISTORAGE);
    writeInfo(inf, DBFNAME, file.path());
    writeInfo(inf, DBFSIZE, Long.toString(filesize));
    writeInfo(inf, DBENCODING, encoding);
    writeInfo(inf, DBHEIGHT, Integer.toString(height));
    writeInfo(inf, DBSIZE, Integer.toString(siz));
    writeInfo(inf, DBCHOPPED, chop);
    writeInfo(inf, DBENTITY, entity);
    writeInfo(inf, DBTXTINDEX, txtindex);
    writeInfo(inf, DBATVINDEX, atvindex);
    writeInfo(inf, DBFTXINDEX, ftxindex);
    writeInfo(inf, DBTIME, Long.toString(time));
    writeInfo(inf, DBLASTID, Long.toString(lastid));
    inf.writeString("");
    inf.close();
  }

  /**
   * Writes a single property.
   * @param out output stream
   * @param k key
   * @param prop property to write
   * @throws IOException in case the info could not be written
   */
  private void writeInfo(final DataOutput out, final String k,
      final boolean prop) throws IOException {
    out.writeString(k);
    out.writeString(prop ? "1" : "0");
  }

  /**
   * Writes a single property.
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
