package org.basex.io;

import static org.basex.util.Token.*;

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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MimeTypes {
  /** Media type: text/xml. */
  private static final String TEXT_XML = "text/xml";
  /** Media type: text/html. */
  public static final String TEXT_HTML = "text/html";
  /** Media type: text/plain. */
  public static final String TEXT_PLAIN = "text/plain";
  /** Media type: text/comma-separated-values. */
  public static final String TEXT_CSV = "text/comma-separated-values";
  /** Media type: text/plain. */
  public static final String APP_OCTET = "application/octet-stream";
  /** Media type: application/xml. */
  public static final String APP_XML = "application/xml";
  /** Media type: application/xquery. */
  public static final String APP_XQUERY = "application/xquery";
  /** Media type: application/json. */
  public static final String APP_JSON = "application/json";
  /** Media type: application/jsonml+json. */
  public static final String APP_JSONML = "application/jsonml+json";

  /** XML media type. */
  private static final String APPL_EXT_XML =
      "application/xml-external-parsed-entity";
  /** XML media type. */
  private static final String TXT_EXT_XML =
      "text/xml-external-parsed-entity";
  /** XML media types' suffix. */
  private static final String MIME_XML_SUFFIX = "+xml";
  /** Text media types' prefix. */
  public static final String MIME_TEXT_PREFIX = "text/";

  /** Private constructor. */
  private MimeTypes() { }

  /**
   * Returns the mime type for the suffix of the specified file path.
   * {@code application/octet-stream} is returned if no type is found.
   * @param path path to be checked
   * @return mime-type
   */
  public static String get(final String path) {
    final int i = path.lastIndexOf('.');
    if(i != -1) {
      final String ct = TYPES.get(path.substring(i + 1));
      if(ct != null) return ct;
    }
    return APP_OCTET;
  }

  /**
   * Checks if the content type is an XML content type.
   * @param c content type
   * @return result
   */
  public static boolean isXML(final String c) {
    return eq(c, TEXT_XML, TXT_EXT_XML, APP_XML, APPL_EXT_XML) ||
        c.endsWith(MIME_XML_SUFFIX);
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
