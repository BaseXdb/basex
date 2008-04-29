package org.basex.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.xml.sax.InputSource;

/**
 * BaseX file representation, pointing to a local or remote file or
 * byte array contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IO {
  /** XML Suffix. */
  public static final String XMLSUFFIX = ".xml";
  /** BlockSize Power. */
  public static final int BLOCKPOWER = 12;
  /** Table NodeSize Power. */
  public static final int NODEPOWER = 4;
  /** Fill Factor (greater than 0.0, maximum 1.0). */
  public static final double BLOCKFILL = 1;
  
  /** File reference. */
  private File file;
  /** File name. */
  private String name;
  /** URL flag. */
  private boolean url;
  /** Content flag. */
  private boolean content;
  /** File contents. */
  private byte[] cont;
  
  /**
   * Constructor.
   * @param f file reference
   */
  public IO(final File f) {
    file = f;
    name = f.getAbsolutePath().replace('\\', '/');
  }
  
  /**
   * Constructor.
   * @param f file reference
   */
  public IO(final String f) {
    content = f.startsWith("<");
    if(content) cont = Token.token(f);
    url = !content && f.startsWith("http://");
    name = content ? "tmp" : url ? f :
      new File(f).getAbsolutePath().replace('\\', '/');
  }
  
  /**
   * Constructor.
   * @param f file contents
   */
  public IO(final byte[] f) {
    content = true;
    cont = f;
    name = "tmp";
  }
  
  /**
   * Returns the file contents.
   * @return contents
   * @throws IOException I/O exception
   */
  public byte[] content() throws IOException {
    if(cont != null) return cont;
    if(url) {
      final URL u = new URL(name);
      final BufferedInputStream bis = new BufferedInputStream(u.openStream());
      final TokenBuilder tb = new TokenBuilder();
      int b = 0;
      while((b = bis.read()) != -1) tb.add((byte) b);
      bis.close();
      cont = tb.finish();
    } else {
      file();
      cont = new byte[(int) file.length()];
      final FileInputStream in = new FileInputStream(file);
      in.read(cont);
      in.close();
    }
    return cont;
  }
  
  /**
   * Returns a buffered reader for the file.
   * @return buffered reader
   */
  public BufferInput buffer() {
    try {
      if(content || url) return new CachedInput(content());
      return new BufferInput(name);
    } catch(final IOException ex) {
      BaseX.notexpected();
      return null;
    }
  }
  
  /**
   * Verifies if the file exists.
   * @return result of check
   */
  public boolean exists() {
    if(content) return true;
    if(!url) return file().exists();
    try {
      // enough?...
      new URL(name).openConnection();
      //new URL(file).openStream();
      return true;
    } catch(IOException ex) {
      BaseX.debug(ex);
      return false;
    }
  }
  
  /**
   * Returns if this is a directory instance.
   * @return result of check
   */
  public boolean isDir() {
    if(content || url) BaseX.notexpected();
    return file().isDirectory();
  }
  
  /**
   * Returns the modification date of this file.
   * @return modification date
   */
  public long date() {
    if(content) return System.currentTimeMillis();
    if(url) BaseX.notexpected();
    return file().lastModified();
  }
  
  /**
   * Returns the file length.
   * @return file length
   */
  public long length() {
    if(cont != null) return cont.length;
    if(url) BaseX.notexpected();
    return file().length();
  }
  
  /**
   * Returns a buffered reader for the file.
   * @return buffered reader
   */
  public InputSource source() {
    return content ? new InputSource(new ByteArrayInputStream(cont)) :
      new InputSource(url ? name : "file:///" + name);
  }
  
  /**
   * Merges two filenames.
   * @param f filename of the file
   * @return contents
   */
  public IO merge(final IO f) {
    return url ? this : new IO(file().getParent() + "/" + f.name());
  }
  
  /**
   * Sets the specified suffix if none exists.
   * @param suf suffix
   */
  public void suffix(final String suf) {
    if(name.indexOf(".") == -1) name += suf;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String dbname() {
    final String n = name();
    return n.endsWith(IO.XMLSUFFIX) ?
        n.substring(0, n.length() - IO.XMLSUFFIX.length()) : n;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String name() {
    return name.substring(name.lastIndexOf('/') + 1);
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String path() {
    return name;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public File file() {
    if(file == null) file = new File(name);
    return file;
  }

  /**
   * Writes the specified file contents.
   * @param c contents
   * @throws IOException I/O exception
   */
  public void write(final byte[] c) throws IOException {
    if(content || url) BaseX.notexpected();
    final FileOutputStream out = new FileOutputStream(name);
    out.write(c);
    out.close();
    cont = c;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public boolean delete() {
    if(content || url) BaseX.notexpected();
    return file().delete();
  }
  
  /**
   * Compares the filename of the specified IO reference.
   * @param io io reference
   * @return result of check
   */
  public boolean eq(final IO io) {
    return name.equals(io.name);
  }

  @Override
  public String toString() {
    return name;
  }

  // STATIC METHODS ===========================================================
  
  /**
   * Adds the database suffix to the specified filename and creates
   * a file instance.
   * @param db name of the database
   * @param file filename
   * @return database filename
   */
  public static File dbfile(final String db, final String file) {
    return new File(Prop.dbpath + '/' + db + '/' + file + ".basex");
  }
  
  /**
   * Returns a file instance for the current database path.
   * @param db name of the database
   * @return database filename
   */
  public static File dbpath(final String db) {
    return new File(Prop.dbpath + '/' + db);
  }
  
  /**
   * Delete a directory recursively.
   * @param db database to delete recursively
   * @param pat file pattern
   * @return success of operation
   */
  public static boolean dbdelete(final String db, final String pat) {
    final File path = dbpath(db);
    if(!path.exists()) return false;
    for(final File sub : path.listFiles()) {
      if((pat == null || sub.getName().matches(pat)) && !sub.delete()) {
        return false;
      }
    }
    return pat == null ? path.delete() : true;
  }
}
