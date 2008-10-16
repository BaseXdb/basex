package org.basex.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.xml.sax.InputSource;

/**
 * BaseX file representation, pointing to a local or remote file or byte array
 * contents.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class IO {
  /** Return IO dummy instance. */
  public static final IO DUMMY = new IOContent(Token.EMPTY);

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

  /** Block size. */
  public static final int BLOCKSIZE = 1 << 12;
  /** Table NodeSize Power. */
  public static final int NODEPOWER = 4;
  /** Fill Factor (greater than 0.0, maximum 1.0). */
  public static final double BLOCKFILL = 1;

  /** File path and name. */
  protected String path = "";
  /** File contents. */
  protected byte[] cont;

  /** First call. */
  protected boolean more = false;

  /** Empty Constructor. */
  protected IO() { }

  /**
   * Constructor.
   * @param s source
   * @return IO reference
   */
  public static IO get(final String s) {
    if(s == null) return new IOFile("tmp");
    if(s.startsWith("<")) return new IOContent(Token.token(s));
    if(s.startsWith("http://")) return new IOUrl(s);
    return new IOFile(s);
  }

  /**
   * Returns the contents.
   * @return contents
   * @throws IOException I/O exception
   */
  public final byte[] content() throws IOException {
    if(cont == null) cache();
    return cont;
  }

  /**
   * Caches the contents.
   * @throws IOException I/O exception
   */
  public abstract void cache() throws IOException;

  /**
   * Verifies if the file exists.
   * @return result of check
   */
  public boolean exists() {
    return true;
  }

  /**
   * Returns if this is a directory instance.
   * @return result of check
   */
  public boolean isDir() {
    return false;
  }

  /**
   * Returns if this is a directory instance.
   * @return result of check
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public boolean isSymLink() throws IOException {
    return false;
  }

  /**
   * Returns the directory of this path.
   * @return result of check
   */
  public final String getDir() {
    return isDir() ? path() : path.substring(0, path.lastIndexOf('/') + 1);
  }

  /**
   * Returns the modification date of this file.
   * @return modification date
   */
  public long date() {
    return System.currentTimeMillis();
  }

  /**
   * Returns the file length.
   * @return file length
   */
  public long length() {
    return cont != null ? cont.length : 0;
  }

  /**
   * Checks if more input streams are found.
   * @return result of check
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public boolean more() throws IOException {
    return more ^= true;
  }

  /**
   * Returns the next input source.
   * @return input source
   */
  public abstract InputSource inputSource();

  /**
   * Returns a buffered reader for the file.
   * @return buffered reader
   * @throws IOException I/O exception
   */
  public abstract BufferInput buffer() throws IOException;

  /**
   * Merges two filenames.
   * @param f filename of the file
   * @return contents
   */
  @SuppressWarnings("unused")
  public IO merge(final IO f) {
    BaseX.notexpected();
    return null;
  }

  /**
   * Sets the specified suffix if none exists.
   * @param suf suffix
   */
  public final void suffix(final String suf) {
    if(path.indexOf(".") == -1) path += suf;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * Returns "tmp" if name is empty.
   * @return chopped filename
   */
  public final String dbname() {
    final String n = name();
    final int i = n.lastIndexOf(".");
    return i != -1 ? n.substring(0, i) : n.length() == 0 ? "tmp" : n;
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public final String name() {
    return path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public final String path() {
    return path;
  }

  /**
   * Returns the children of a document.
   * @return chopped filename
   */
  public IO[] children() {
    BaseX.notexpected();
    return null;
  }

  /**
   * Writes the specified file contents.
   * @param c contents
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void write(final byte[] c) throws IOException {
    BaseX.notexpected();
  }

  /**
   * Chops the path and the XML suffix of the specified filename.
   * @return chopped filename
   */
  public boolean delete() {
    BaseX.notexpected();
    return false;
  }

  /**
   * Compares the filename of the specified IO reference.
   * @param io io reference
   * @return result of check
   */
  public final boolean eq(final IO io) {
    return path.equals(io.path);
  }

  @Override
  public final String toString() {
    return path;
  }

  /**
   * Caches the contents of the specified input stream.
   * @param i input stream
   * @return cached contents
   * @throws IOException exception
   */
  protected final byte[] cache(final InputStream i) throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    final InputStream bis = i instanceof BufferedInputStream ? i
        : new BufferedInputStream(i);
    int b;
    while((b = bis.read()) != -1)
      tb.add((byte) b);
    bis.close();
    cont = tb.finish();
    return cont;
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
      if(!Token.letterOrDigit(c)
          && (c < ' ' || c > '@' || INVALID.indexOf(c) != -1)) return false;
    }
    return true;
  }

  /**
   * Creates a URL from the specified path.
   * @param path path to be converted
   * @return URL
   */
  public static final String url(final String path) {
    String pre = "file://";
    if(!path.startsWith("/")) {
      pre += "/";
      if(path.length() < 2 || path.charAt(1) != ':') {
        pre += "/" + Prop.WORK.replace('\\', '/');
        if(!pre.endsWith("/")) pre += "/";
      }
    }
    return pre + path.replace('\\', '/');
  }

  /**
   * Determines if the specified file is a symbolic link.
   * @param f file to be tested.
   * @return true for a symbolic link
   * @throws IOException I/O exception
   */
  public static boolean isSymlink(final File f) throws IOException {
    return !f.getPath().equals(f.getCanonicalPath());
  }
}
