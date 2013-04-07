package org.basex.http.webdav.impl;

import org.basex.core.Text;
import org.basex.io.in.BufferInput;
import org.basex.io.in.TextInput;

import java.io.IOException;
import java.util.Locale;

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

  /**
   * Strip leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  public static String stripLeadingSlash(final String s) {
    return s == null || s.isEmpty() || s.charAt(0) != SEP ? s : s.substring(1);
  }

  /**
   * Get the name from the given path.
   * @param path path
   * @return name of the resource identified by the path
   */
  public static String name(String path) {
    final int idx = path.lastIndexOf(SEP);
    return idx < 0 ? path : path.substring(idx + 1, path.length());
  }

  /**
   * Gets a valid database name from a general file name.
   * @param db name of database
   * @return valid database name
   */
  public static String dbname(final String db) {
    final int i = db.lastIndexOf('.');
    return (i < 0 ? db : db.substring(0, i)).replaceAll("[^\\w-]", "");
  }

  public static long now() {
    return System.currentTimeMillis();
  }

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
