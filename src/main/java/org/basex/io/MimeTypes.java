package org.basex.io;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.basex.io.in.*;
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
  /** Content-Type. */
  public static final String CONTENT_TYPE = "Content-Type";
  /** Charset. */
  public static final String CHARSET = "; charset=";

  /** Media type: application/html+xml. */
  public static final String APP_HTML_XML = "application/html+xml";
  /** Media type: application/json. */
  public static final String APP_JSON = "application/json";
  /** Media type: application/jsonml+json. */
  public static final String APP_JSONML = "application/jsonml+json";
  /** Media type: text/plain. */
  public static final String APP_OCTET = "application/octet-stream";
  /** Media type: application/xml. */
  public static final String APP_XML = "application/xml";
  /** Media type: application/xml-external-parsed-entity. */
  public static final String APP_EXT_XML = "application/xml-external-parsed-entity";
  /** Media type: application/x-www-form-urlencoded. */
  public static final String APP_FORM = "application/x-www-form-urlencoded";
  /** Media type: multipart/form-data. */
  public static final String MULTIPART_FORM = "multipart/form-data";

  /** Media type: text/comma-separated-values. */
  public static final String TEXT_CSV = "text/comma-separated-values";
  /** Media type: text/html. */
  public static final String TEXT_HTML = "text/html";
  /** Media type: text/plain. */
  public static final String TEXT_PLAIN = "text/plain";
  /** Media type: text/xml. */
  public static final String TEXT_XML = "text/xml";

  /** XML media types' suffix. */
  public static final String MIME_XML_SUFFIX = "+xml";
  /** Text media types' prefix. */
  public static final String MIME_TEXT_PREFIX = "text/";
  /** XML media type. */
  public static final String TEXT_XML_EXT = "text/xml-external-parsed-entity";

  /** Private constructor. */
  private MimeTypes() { }

  /**
   * Returns the mime type for the suffix of the specified file path.
   * {@code application/octet-stream} is returned if no type is found.
   * @param path path to be checked
   * @return mime-type
   */
  public static String get(final String path) {
    final String ct = TYPES.get(IO.suffix(path));
    return ct != null ? ct : APP_OCTET;
  }

  /**
   * Checks if the content type is an XML content type.
   * @param type content type
   * @return result of check
   */
  public static boolean isXML(final String type) {
    return eq(type, TEXT_XML, TEXT_XML_EXT, APP_XML, APP_EXT_XML) ||
        type.endsWith(MIME_XML_SUFFIX);
  }

  /**
   * Checks if the main part of the content type is {@code "text"}.
   * @param type content type
   * @return result of check
   */
  public static boolean isText(final String type) {
    return type.startsWith(MIME_TEXT_PREFIX);
  }

  /**
   * Checks if a content type is accepted by the specified pattern.
   * @param type content type
   * @param pattern pattern
   * @return result of check
   */
  public static boolean matches(final String type, final String pattern) {
    final String[] t = type.split("/", 2);
    final String[] p = pattern.split("/", 2);
    return t.length == 2 && p.length == 2 &&
        (p[0].equals("*") || p[0].equals(t[0])) &&
        (p[1].equals("*") || p[1].equals(t[1]));
  }

  /** Hash map containing all assignments. */
  private static final HashMap<String, String> TYPES = new HashMap<String, String>();

  /** Reads in the mime-types. */
  static {
    NewlineInput nli = null;
    try {
      final String file = "/mime.txt";
      final InputStream is = MimeTypes.class.getResourceAsStream(file);
      if(is == null) {
        Util.errln(file + " not found.");
      } else {
        nli = new NewlineInput(is);
        for(String line; (line = nli.readLine()) != null;) {
          final int i = line.indexOf('\t');
          if(i == -1 || line.startsWith("#")) continue;
          TYPES.put(line.substring(0, i), line.substring(i + 1));
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    } finally {
      if(nli != null) try { nli.close(); } catch(final IOException ex) { }
    }
  }
}
