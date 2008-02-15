package org.basex.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.basex.core.Prop;
import org.basex.util.TokenBuilder;

/**
 * Input/Output Constants.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IOConstants {
  /** BlockSize Power. */
  public static final int BLOCKPOWER = 12;
  /** Table NodeSize Power. */
  public static final int NODEPOWER = 4;
  /** Fill Factor (greater than 0.0, maximum 1.0). */
  public static final double BLOCKFILL = 1;

  /** Private Constructor. */
  private IOConstants() { }
  
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
    return new File(Prop.dbpath + '/' + db + '/');
  }
  
  /**
   * Opens a file, dissolving the parent reference, and returns its contents.
   * If the file references a URL, the parent reference is ignored.
   * @param par parent reference
   * @param file name of the file
   * @return contents
   * @throws IOException I/O exception
   */
  public static byte[] read(final String par, final String file)
      throws IOException {

    if(file.startsWith("http://")) return read(new URL(file));
    return read(new File(par).getParent() + "/" + file);
  }
  
  /**
   * Opens a file and returns its contents.
   * @param file name of the file
   * @return contents
   * @throws IOException I/O exception
   */
  public static byte[] read(final String file) throws IOException {
    return file.startsWith("http://") ? read(new URL(file)) :
      read(new File(file));
  }
  
  /**
   * Opens a file and returns its contents.
   * @param file file reference
   * @return contents
   * @throws IOException I/O exception
   */
  public static byte[] read(final File file) throws IOException {
    final byte[] cont = new byte[(int) file.length()];
    final FileInputStream in = new FileInputStream(file);
    in.read(cont);
    in.close();
    return cont;
  }
  
  /**
   * Opens a URL and returns its contents.
   * @param url url reference
   * @return contents
   * @throws IOException I/O exception
   */
  public static byte[] read(final URL url) throws IOException {
    final BufferedInputStream bis = new BufferedInputStream(url.openStream());
    final TokenBuilder tb = new TokenBuilder();
    int b = 0;
    while((b = bis.read()) != -1) tb.add((byte) b);
    bis.close();
    return tb.finish();
  }
  
  /**
   * Writes the specified file.
   * @param file file reference
   * @param cont contents
   * @throws IOException I/O exception
   */
  public static void write(final String file, final byte[] cont)
      throws IOException {
    write(new File(file), cont);
  }
  
  /**
   * Writes the specified file.
   * @param f file reference
   * @param cont contents
   * @throws IOException I/O exception
   */
  public static void write(final File f, final byte[] cont) throws IOException {
    final FileOutputStream out = new FileOutputStream(f);
    out.write(cont);
    out.close();
  }
}
