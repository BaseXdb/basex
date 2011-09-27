package org.basex.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.basex.util.Util;

/**
 * This class returns the mime types of a file, which is either dynamically
 * determined by Java, or statically resolved by requesting the mappings in
 * the {@code mime.txt} project file.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class MimeTypes {
  /** Media type: text/html. */
  public static final String TEXT_HTML = "text/html";
  /** Media type: text/plain. */
  public static final String TEXT_PLAIN = "text/plain";
  /** Media type: text/plain. */
  public static final String APP_OCTET = "application/octet-stream";
  /** Media type: application/xml. */
  public static final String APP_XML = "application/xml";
  /** Media type: application/json. */
  public static final String APP_JSON = "application/json";

  /** Private constructor. */
  private MimeTypes() { }

  /**
   * Returns the mime type for the specified file path.
   * application/octet-stream is returned if no type is found.
   * @param path path to be checked
   * @return mime-type
   */
  public static String get(final String path) {
    // check if file suffix exists
    final int i = path.lastIndexOf('.');
    if(i != -1) {
      final String ct = MimeTypes.TYPES.get(path.substring(i + 1));
      // return found type
      if(ct != null) return ct;
    }
    // return default type
    return APP_OCTET;
  }


  /** Hash map containing all assignments. */
  private static final HashMap<String, String> TYPES =
      new HashMap<String, String>();

  /** Reads in the mime-types. */
  static {
    BufferedReader br = null;
    try {
      final String file = "/mime.txt";
      final InputStream is = MimeTypes.class.getResourceAsStream(file);
      if(is == null) {
        Util.errln(file + " not found.");
      } else {
        br = new BufferedReader(new InputStreamReader(is));
        for(String line; (line = br.readLine()) != null;) {
          final int i = line.indexOf('\t');
          if(i == -1 || line.startsWith("#")) continue;
          TYPES.put(line.substring(0, i), line.substring(i + 1));
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    } finally {
      if(br != null) try { br.close(); } catch(final IOException ex) { }
    }
  }
}
