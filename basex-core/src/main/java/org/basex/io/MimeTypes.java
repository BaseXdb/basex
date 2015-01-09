package org.basex.io;

import java.io.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.util.*;

/**
 * This class returns the mime types of a file, which is either dynamically
 * determined by Java, or statically resolved by requesting the mappings in
 * the {@code mime.txt} project file.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MimeTypes {
  /** Content-Type. */
  public static final String CONTENT_TYPE = "Content-Type";

  /** Text type. */
  private static final String TEXT = "text/";
  /** Multipart type. */
  private static final String MULTIPART = "multipart/";

  /** Media type: multipart/form-data. */
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";

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
  private static final String APP_XML_EXTERNAL = "application/xml-external-parsed-entity";
  /** Media type: application/x-www-form-urlencoded. */
  public static final String APP_FORM_URLENCODED = "application/x-www-form-urlencoded";

  /** Media type: text/comma-separated-values. */
  public static final String TEXT_CSV = "text/comma-separated-values";
  /** Media type: text/html. */
  public static final String TEXT_HTML = "text/html";
  /** Media type: text/plain. */
  public static final String TEXT_PLAIN = "text/plain";
  /** Media type: text/xml. */
  private static final String TEXT_XML = "text/xml";
  /** XML media type. */
  private static final String TEXT_XML_EXT = "text/xml-external-parsed-entity";

  /** XML media types' suffix. */
  private static final String XML_SUFFIX = "+xml";
  /** XQuery media types' suffix. */
  private static final String XQUERY_SUFFIX = "/xquery";

  /** Private constructor. */
  private MimeTypes() { }

  /**
   * Returns the mime type for the suffix of the specified file path.
   * {@code application/octet-stream} is returned if no type is found.
   * @param path path to be checked
   * @return mime-type
   */
  public static String get(final String path) {
    final int s = path.lastIndexOf('/');
    final int d = path.lastIndexOf('.');
    final String suffix = d <= s ? "" : path.substring(d + 1).toLowerCase(Locale.ENGLISH);
    final String ct = TYPES.get(suffix);
    return ct != null ? ct : APP_OCTET;
  }

  /**
   * Checks if the mime type is an XQuery mime type.
   * @param type mime type
   * @return result of check
   */
  public static boolean isXQuery(final String type) {
    return type.endsWith(XQUERY_SUFFIX);
  }

  /**
   * Checks if the mime type is an XML mime type.
   * @param type mime type
   * @return result of check
   */
  public static boolean isXML(final String type) {
    return Strings.eq(type, TEXT_XML, TEXT_XML_EXT, APP_XML, APP_XML_EXTERNAL) ||
        type.endsWith(XML_SUFFIX);
  }

  /**
   * Checks if the mime type is a JSON mime type.
   * @param type mime type
   * @return result of check
   */
  public static boolean isJSON(final String type) {
    return Strings.eq(type, APP_JSON, APP_JSONML);
  }

  /**
   * Checks if the main part of the mime type is {@code "text"}.
   * @param type mime type
   * @return result of check
   */
  public static boolean isText(final String type) {
    return type.startsWith(TEXT);
  }

  /**
   * Checks if the mime type is a multipart mime type.
   * @param type mime type
   * @return result of check
   */
  public static boolean isMultipart(final String type) {
    return type.startsWith(MULTIPART);
  }

  /**
   * Checks if a mime type is accepted by the specified pattern.
   * @param type mime type
   * @param pattern pattern
   * @return result of check
   */
  public static boolean matches(final String type, final String pattern) {
    final String[] t = prepareType(type), p = prepareType(pattern);
    return Strings.eq(p[0], t[0], "*") && Strings.eq(p[1], t[1], "*");
  }

  /**
   * Prepares the specified mime type for comparison.
   * @param type mime type
   * @return mime type array
   */
  private static String[] prepareType(final String type) {
    final String[] t = { type, "" };
    if(type.equals("*")) {
      t[1] = type;
    } else {
      final int i = type.indexOf('/');
      if(i != -1) {
        t[0] = type.substring(i);
        t[1] = type.substring(i + 1);
      }
    }
    return t;
  }

  /** Hash map containing all assignments. */
  private static final HashMap<String, String> TYPES = new HashMap<>();

  /** Reads in the mime-types. */
  static {
    try {
      final String file = "/mime.properties";
      final InputStream is = MimeTypes.class.getResourceAsStream(file);
      if(is == null) {
        Util.errln(file + " not found.");
      } else {
        try(final NewlineInput nli = new NewlineInput(is)) {
          for(String line; (line = nli.readLine()) != null;) {
            final int i = line.indexOf('=');
            if(i == -1 || line.startsWith("#")) continue;
            TYPES.put(line.substring(0, i), line.substring(i + 1));
          }
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    }
  }
}
