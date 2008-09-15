package org.basex.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
public final class IO {
  /** Invalid file characters. */
  private static final String INVALID = " \"*./:<>?";
  /** BaseX Suffix. */
  public static final String BASEXSUFFIX = ".basex";
  /** XQuery Suffix. */
  public static final String XQSUFFIX = ".xq";
  /** XML Suffix. */
  public static final String XMLSUFFIX = ".xml";
  /** ZIP Suffix. */
  public static final String ZIPSUFFIX = ".zip";
  /** GZIP Suffix. */
  public static final String GZSUFFIX = ".gz";

  /** BlockSize Power. */
  public static final int BLOCKPOWER = 12;
  /** Table NodeSize Power. */
  public static final int NODEPOWER = 4;
  /** Fill Factor (greater than 0.0, maximum 1.0). */
  public static final double BLOCKFILL = 1;
  
  /** File reference. */
  private File file;
  /** File path and name. */
  private String path;
  /** URL flag. */
  private boolean url;
  /** Content flag. */
  private boolean content;
  /** File contents. */
  private byte[] cont;
  /** File length. */
  private long len;
  
  /**
   * Constructor.
   * @param f file reference
   */
  public IO(final File f) {
    file = f;
    path = f.getAbsolutePath().replace('\\', '/');
  }
  
  /**
   * Constructor.
   * @param f file reference
   */
  public IO(final String f) {
    content = f.startsWith("<");
    if(content) cont = Token.token(f);
    url = !content && f.startsWith("http://");
    path = content ? "tmp.xml" : url ? f :
      new File(f).getAbsolutePath().replace('\\', '/');
  }
  
  /**
   * Constructor.
   * @param f buffer contents
   * @param fn file name
   */
  public IO(final byte[] f, final String fn) {
    content = true;
    cont = f;
    path = fn;
  }
  
  /**
   * Returns the file contents.
   * @return contents
   * @throws IOException I/O exception
   */
  public byte[] content() throws IOException {
    if(cont != null) return cont;
    if(url) {
      final URL u = new URL(path);
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
   * Verifies if the file exists.
   * @return result of check
   */
  public boolean exists() {
    if(content) return true;
    if(!url) return file().exists();
    try {
      // enough?...
      //new URL(path).openConnection();
      new URL(path).openStream();
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
    if(url) BaseX.notexpected();
    return !content && file().isDirectory();
  }
  
  /**
   * Returns the directory of this path.
   * @return result of check
   */
  public String getDir() {
    return isDir() ? path() : file().getParent();
  }
  
  /**
   * Returns if this is a symbolic link.
   * @return result of check
   */
  public boolean isLink() {
    try {
      return !path().equals(file().getCanonicalPath());
    } catch(IOException ex) {
      BaseX.debug(ex);
      return false;
    }
  }


  /**
   * Returns the modification date of this file.
   * @return modification date
   */
  public long date() {
    if(content || url) return System.currentTimeMillis();
    return file().lastModified();
  }
  
  /**
   * Returns the file length.
   * @return file length
   */
  public long length() {
    if(len != 0) return len;
    if(cont != null) return cont.length;
    return url ? 0 : file().length();
  }

  /**
   * Checks if more input streams are found.
   * @return result of check
   * @throws IOException I/O exception
   */
  public boolean more() throws IOException {
    // return cached content
    if(content) {
      is = is == null ? new ByteArrayInputStream(cont) : null;
    } else if(is instanceof ZipInputStream || path.endsWith(ZIPSUFFIX)) {
      if(is == null) is = new ZipInputStream(new FileInputStream(file));
      // doesn't work yet with several zip entries
      else return false;
      while(is != null) {
        ZipEntry e = ((ZipInputStream) is).getNextEntry();
        if(e == null) {
          is = null;
        } else {
          len = e.getSize();
          path = e.getName();
          if(!e.isDirectory()) break;
        }
      }
    } else if(path.endsWith(GZSUFFIX)) {
      is = is == null ? new GZIPInputStream(new FileInputStream(file)) : null;
    } else {
      simple ^= true;
    }
    return is != null || simple;
  }

  /** Input stream reference. */
  InputStream is;
  /** Simple stream reference. */
  boolean simple;
  
  /**
   * Returns the next input source.
   * @return input source
   */
  public InputSource next() {
    return is == null ? new InputSource(url ? path : "file:///" + path) :
      new InputSource(is);
  }
  
  /**
   * Returns a buffered reader for the file.
   * @return buffered reader
   * @throws IOException I/O exception
   */
  public BufferInput buffer() throws IOException {
    // return cached content
    if(content || url) return new CachedInput(content());

    // support for zipped files; the first file will be chosen
    if(path.endsWith(ZIPSUFFIX)) {
      ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
      ZipEntry entry = zip.getNextEntry();
      final BufferInput in = new BufferInput(zip);
      in.length(entry.getSize());
      return in;
    }
    // support for gzipped files
    if(path.endsWith(GZSUFFIX)) {
      GZIPInputStream zip = new GZIPInputStream(new FileInputStream(file));
      return new BufferInput(zip);
    }
    
    // return file content
    return new BufferInput(path);
  }
  
  
  /**
   * Merges two filenames.
   * @param f filename of the file
   * @return contents
   */
  public IO merge(final IO f) {
    return f.url ? f : url ? this : new IO(file().getParent() + "/" + f.name());
  }
  
  /**
   * Sets the specified suffix if none exists.
   * @param suf suffix
   */
  public void suffix(final String suf) {
    if(path.indexOf(".") == -1) path += suf;
  }

  /**
   * Returns the file suffix in lower case.
   * @return suffix
   */
  public String suffix() {
    int i = path.lastIndexOf(".");
    return i == -1 ? "" : path.substring(i + 1).toLowerCase();
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String dbname() {
    final String n = name();
    return n.contains(".") ? n.substring(0, n.lastIndexOf(".")) : n;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String name() {
    return path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public String path() {
    return path;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public File file() {
    if(file == null) file = new File(path);
    return file;
  }

  /**
   * Returns the children of a document.
   * @return chopped filename
   */
  public IO[] children() {
    final File[] ch = file().listFiles();
    final int l = ch != null ? ch.length : 0;
    final IO[] io = new IO[l];
    for(int i = 0; i < l; i++) io[i] = new IO(ch[i]);
    return io;
  }

  /**
   * Writes the specified file contents.
   * @param c contents
   * @throws IOException I/O exception
   */
  public void write(final byte[] c) throws IOException {
    if(content || url) BaseX.notexpected();
    final FileOutputStream out = new FileOutputStream(path);
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
    return path.equals(io.path);
  }

  @Override
  public String toString() {
    return path;
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
    return new File(Prop.dbpath + '/' + db + '/' + file + BASEXSUFFIX);
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
   * Recursively deletes a directory.
   * @param db database to delete
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

  /**
   * Checks if the specified filename is valid; allows only letters,
   * digits and some special characters.
   * @param fn filename
   * @return result of check
   */
  public static boolean valid(final String fn) {
    // . / * "   : < > ?
    for(int i = 0; i < fn.length(); i++) {
      final char c = fn.charAt(i);
      if(!Token.letterOrDigit(c) && (c < ' ' || c > '@' ||
          INVALID.indexOf(c) != -1)) return false;
    }
    return true;
  }
}
