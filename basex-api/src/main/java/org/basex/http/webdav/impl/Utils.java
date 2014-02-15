package org.basex.http.webdav.impl;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;

/**
 * WebDAV utility methods.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class Utils {
  /** File path separator. */
  public static final char SEP = '/';
  /** Dummy file for empty folder.*/
  static final String DUMMY = ".empty";

  /** Private constructor. */
  private Utils() { }

  /**
   * Strips leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  public static String stripLeadingSlash(final String s) {
    return s == null || s.isEmpty() || s.charAt(0) != SEP ? s : s.substring(1);
  }

  /**
   * Gets the name from the given path.
   * @param path path
   * @return name of the resource identified by the path
   */
  public static String name(final String path) {
    return IO.get(path).name();
  }

  /**
   * Gets a valid database name from a general file name.
   * @param db name of database
   * @return valid database name
   */
  public static String dbname(final String db) {
    return IO.get(db).dbname();
  }

  /**
   * Peeks the next byte in the given buffer.
   * @param bi buffer
   * @return the next byte in the buffer
   * @throws IOException I/O exception
   */
  public static int peek(final BufferInput bi) throws IOException {
    final TextInput ti = new TextInput(bi);
    final int c = ti.read();
    try {
      bi.reset();
    } catch(final IOException ignore) {
    }
    return c;
  }
}
