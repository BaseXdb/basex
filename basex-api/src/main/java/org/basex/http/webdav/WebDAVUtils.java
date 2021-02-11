package org.basex.http.webdav;

import java.io.*;
import java.net.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * WebDAV utility methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
final class WebDAVUtils {
  /** File path separator. */
  static final char SEP = '/';
  /** Dummy file for empty folder. */
  static final String DUMMY = ".empty";

  /** Private constructor. */
  private WebDAVUtils() { }

  /**
   * Strips leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  static String stripLeadingSlash(final String s) {
    return s == null || s.isEmpty() || s.charAt(0) != SEP ? s : s.substring(1);
  }

  /**
   * Gets the name from the given path.
   * @param path path
   * @return name of the resource identified by the path
   */
  static String name(final String path) {
    return IO.get(path).name();
  }

  /**
   * Gets a valid database name from a general file name.
   * @param db name of database
   * @return valid database name
   */
  static String dbName(final String db) {
    return IO.get(db).dbName();
  }

  /**
   * Decodes a url. Character set must be guessed, because it cannot be derived from the request.
   * @param url url to be decoded
   * @return decoded url
   */
  static String decode(final String url) {
    if(url.indexOf('%') != -1) {
      try {
        final String ud = URLDecoder.decode(url, Strings.UTF8);
        return ud.contains("\uFFFD") ? URLDecoder.decode(url, Strings.ISO88591) : ud;
      } catch(final Exception ex) {
        Util.stack(ex);
      }
    }
    return url;
  }

  /**
   * Peeks the next byte in the given buffer.
   * @param bi buffer
   * @return the next byte in the buffer
   * @throws IOException I/O exception
   */
  static int peek(final BufferInput bi) throws IOException {
    // input stream will be closed outside this function
    final TextInput ti = new TextInput(bi);
    final int c = ti.read();
    try {
      bi.reset();
    } catch(final IOException ignore) { }
    return c;
  }
}
