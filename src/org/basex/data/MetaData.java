package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.core.Prop;
import org.basex.core.proc.Create;
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
  public String filename = "";
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
   * Checks if the specified database refers to the specified file.
   * @param path file path (incl. file name)
   * @param db database name
   * @return result of check
   */
  public static boolean found(final String path, final String db) {
    try {
      // no .xml suffix and database specified - check existence of database
      if(!path.endsWith(Create.XMLSUFFIX) && !path.contains("/"))
        return new File(Prop.dbpath + '/' + db).exists();

      // match filename of database instance
      final DataInput in = new DataInput(db, DATAINFO);
      String key;
      File f = null;
      long t = 0;
      while((key = in.readString()).length() != 0) {
        final String val = in.readString();
        if(key.equals(DBFNAME)) f = new File(val);
        if(key.equals(DBTIME)) t = Token.toLong(val);
      }
      in.close();
      return f != null && f.equals(new File(path)) && f.lastModified() == t;
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
  }

  /**
   * Opens the metadata for the current database and returns the table size.
   * @throws IOException IO Exception
   * @return table size
   */
  public int read() throws IOException {
    final DataInput in = new DataInput(dbname, DATAINFO);
    String storage = "";
    int size = 0;
    while(true) {
      final String key = in.readString();
      if(key.length() == 0) break;
      final String val = in.readString();

      if(key.equals(DBSTORAGE)) storage = val;
      else if(key.equals(DBFNAME)) filename = val;
      else if(key.equals(DBFSIZE)) filesize = Token.toLong(val);
      else if(key.equals(DBENCODING)) encoding = val;
      else if(key.equals(DBHEIGHT)) height = Token.toInt(val);
      else if(key.equals(DBSIZE)) size = Token.toInt(val);
      else if(key.equals(DBCHOPPED)) chop = val.equals("ON");
      else if(key.equals(DBENTITY)) entity = val.equals("ON");
      else if(key.equals(DBTXTINDEX)) txtindex = val.equals("ON");
      else if(key.equals(DBATVINDEX)) atvindex = val.equals("ON");
      else if(key.equals(DBFTXINDEX)) ftxindex = val.equals("ON");
      else if(key.equals(DBTIME)) time = Token.toLong(val);
      else if(key.equals(DBLASTID)) lastid = Token.toLong(val);
    }
    in.close();

    if(!storage.equals(STORAGE)) {
      throw new BuildException(BaseX.info(DBUPDATE, storage));
    }
    return size;
  }

  /**
   * Writes the database to the specified path.
   * @param siz current database size
   * @throws IOException IO Exception
   */
  public synchronized void write(final int siz) throws IOException {
    final DataOutput inf = new DataOutput(dbname, DATAINFO);
    writeInfo(inf, DBSTORAGE, STORAGE);
    writeInfo(inf, DBFNAME, filename);
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
    out.writeString(prop ? "ON" : "OFF");
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
