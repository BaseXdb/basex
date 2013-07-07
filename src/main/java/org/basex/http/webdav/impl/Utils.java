package org.basex.http.webdav.impl;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * WebDAV utility methods.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class Utils {
  /** Time string. */
  public static final String TIME = Text.TIMESTAMP.replaceAll(" |-",
    "").toLowerCase(Locale.ENGLISH);
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
    final int idx = path.lastIndexOf(SEP);
    return idx < 0 ? path : path.substring(idx + 1, path.length());
  }

  /**
   * Gets a valid database name from a general file name.
   * @param db name of database
   * @return valid database name
   */
  public static String dbname(final String db) {
    Util.stack(5);
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
