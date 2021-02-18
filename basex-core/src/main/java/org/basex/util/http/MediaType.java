package org.basex.util.http;

import java.io.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.util.*;

/**
 * Single Internet media type.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MediaType implements Comparable<MediaType> {
  /** Multipart type. */
  private static final String MULTIPART = "multipart";
  /** Text type. */
  private static final String TEXT = "text";
  /** XQuery sub type. */
  private static final String XQUERY = "xquery";
  /** CSV sub type. */
  private static final String CSV = "csv";
  /** CSV sub type. */
  private static final String COMMA_SEPARATED_VALUES = "comma-separated-values";
  /** XML media type suffix. */
  private static final String XML_SUFFIX = "+xml";
  /** JSON media type suffix. */
  private static final String JSON_SUFFIX = "+json";

  /** Media type: wildcards. */
  public static final MediaType ALL_ALL = new MediaType("*/*");

  /** Media type: application/x-www-form-urlencoded. */
  public static final MediaType APPLICATION_X_WWW_FORM_URLENCODED =
      new MediaType("application/x-www-form-urlencoded");
  /** Media type: application/html+xml. */
  public static final MediaType APPLICATION_HTML_XML = new MediaType("application/html+xml");
  /** Text/plain. */
  public static final MediaType APPLICATION_JSON = new MediaType("application/json");
  /** Media type: text/plain. */
  public static final MediaType APPLICATION_OCTET_STREAM =
      new MediaType("application/octet-stream");
  /** Media type: application/xml. */
  public static final MediaType APPLICATION_XML = new MediaType("application/xml");
  /** Media type: application/xml-external-parsed-entity. */
  public static final MediaType APPLICATION_XML_EPE =
      new MediaType("application/xml-external-parsed-entity");

  /** Media type: multipart/form-data. */
  public static final MediaType MULTIPART_FORM_DATA = new MediaType("multipart/form-data");

  /** Media type: text/comma-separated-values. */
  public static final MediaType TEXT_CSV = new MediaType("text/csv");
  /** Media type: text/html. */
  public static final MediaType TEXT_HTML = new MediaType("text/html");
  /** Media type: text/plain. */
  public static final MediaType TEXT_PLAIN = new MediaType("text/plain");
  /** Media type: text/xml. */
  public static final MediaType TEXT_XML = new MediaType("text/xml");
  /** XML media type. */
  public static final MediaType TEXT_XML_EPE = new MediaType("text/xml-external-parsed-entity");

  /** Main type. */
  private final String main;
  /** Sub type. */
  private final String sub;
  /** Parameters. */
  private final HashMap<String, String> params = new HashMap<>();

  /**
   * Constructor.
   * @param string media type string
   */
  public MediaType(final String string) {
    final int p = string.indexOf(';');
    final String type = p == -1 ? string : string.substring(0, p);

    // set main and sub type
    final int s = type.indexOf('/');
    main = s == -1 ? type : type.substring(0, s);
    sub  = s == -1 ? "" : type.substring(s + 1);

    // parse parameters (simplified version of RFC 2045; no support for comments, etc.)
    if(p != -1) {
      for(final String param : Strings.split(string.substring(p + 1), ';')) {
        final String[] kv = Strings.split(param, '=', 2);
        // attribute: trim whitespaces, convert to lower case
        final String k = kv[0].trim().toLowerCase(Locale.ENGLISH);
        // value: trim whitespaces, remove quotes and backslashed characters
        String v = kv.length < 2 ? "" : kv[1].trim();
        if(Strings.startsWith(v, '"')) v = v.replaceAll("^\"|\"$", "").replaceAll("\\\\(.)", "$1");
        params.put(k, v);
      }
    }
  }

  /**
   * Returns the main type.
   * @return type
   */
  public String main() {
    return main;
  }

  /**
   * Returns the sub type.
   * @return type
   */
  public String sub() {
    return sub;
  }

  /**
   * Returns the media type, composed from the main and sub type.
   * @return type without parameters
   */
  public String type() {
    return sub.isEmpty() ? main : (main + '/' + sub);
  }

  /**
   * Returns the parameters.
   * @return parameters
   */
  public HashMap<String, String> parameters() {
    return params;
  }

  /**
   * Checks if this is a multipart type.
   * @return result of check
   */
  public boolean isMultipart() {
    return main.equals(MULTIPART);
  }

  /**
   * Checks if this is a text type.
   * @return result of check
   */
  public boolean isText() {
    return main.equals(TEXT);
  }

  /**
   * Checks if this is an XQuery type.
   * @return result of check
   */
  public boolean isXQuery() {
    return sub.equals(XQUERY);
  }

  /**
   * Checks if this is a CSV type.
   * @return result of check
   */
  public boolean isCSV() {
    return sub.equals(CSV) || sub.equals(COMMA_SEPARATED_VALUES);
  }

  /**
   * Checks if this is an XML type.
   * @return result of check
   */
  public boolean isXML() {
    return is(TEXT_XML) || is(TEXT_XML_EPE) || is(APPLICATION_XML) || is(APPLICATION_XML_EPE) ||
        sub.endsWith(XML_SUFFIX);
  }

  /**
   * Checks if this is a JSON type.
   * @return result of check
   */
  public boolean isJSON() {
    return is(APPLICATION_JSON) || sub.endsWith(JSON_SUFFIX);
  }

  /**
   * Checks if the specified media type is contained in this media type.
   * @param pattern pattern
   * @return result of check
   */
  public boolean matches(final MediaType pattern) {
    return Strings.eq(pattern.main(), main, "*") && Strings.eq(pattern.sub(), sub, "*");
  }

  /**
   * Checks if the main and sub type of this and the specified type are equal.
   * @param type type
   * @return result of check
   */
  public boolean is(final MediaType type) {
    return main.equals(type.main) && sub.equals(type.sub);
  }

  @Override
  public int compareTo(final MediaType type) {
    final int cmp = compareTo(main, type.main);
    return cmp == 0 ? compareTo(sub, type.sub) : cmp;
  }

  /**
   * Compares the specified main or sub types.
   * @param type1 first type
   * @param type2 second type
   * @return result of check
   */
  private static int compareTo(final String type1, final String type2) {
    return type1.equals(type2) ? 0 :
           type1.equals("*") ? 1 :
           type2.equals("*") ? -1 :
           type1.compareTo(type2);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(type());
    params.forEach((key, value) -> sb.append("; ").append(key).append('=').append(value));
    return sb.toString();
  }

  /**
   * Returns the media type for the suffix of the specified file path.
   * {@code application/octet-stream} is returned if no type is found.
   * @param path path to be checked
   * @return media type
   */
  public static MediaType get(final String path) {
    final int s = path.lastIndexOf('/'), d = path.lastIndexOf('.');
    final String suffix = d <= s ? "" : path.substring(d + 1).toLowerCase(Locale.ENGLISH);
    return TYPES.getOrDefault(suffix, APPLICATION_OCTET_STREAM);
  }

  /** Hash map containing all assignments. */
  private static final HashMap<String, MediaType> TYPES = new HashMap<>();

  /* Reads in the media-types. */
  static {
    final HashMap<String, MediaType> cache = new HashMap<>();
    try {
      final String file = "/media-types.properties";
      final InputStream is = MediaType.class.getResourceAsStream(file);
      if(is == null) {
        Util.errln(file + " not found.");
      } else {
        try(NewlineInput nli = new NewlineInput(is)) {
          for(String line; (line = nli.readLine()) != null;) {
            final int i = line.indexOf('=');
            if(i == -1 || Strings.startsWith(line, '#')) continue;
            final String suffix = line.substring(0, i), type = line.substring(i + 1);
            final MediaType mt = cache.computeIfAbsent(type, MediaType::new);
            TYPES.put(suffix, mt);
          }
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    } catch(final Throwable ex) {
      Util.stack(ex);
    }
  }
}
