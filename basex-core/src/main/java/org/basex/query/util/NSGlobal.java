package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Global namespaces.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Namespaces: prefixes and namespace URIs. */
  public static final Atts NS = new Atts();
  /** Mapping: prefix to URI. */
  private static final TokenObjectMap<byte[]> URIS = new TokenObjectMap<>();
  /** Mapping: URI to prefix. */
  private static final TokenObjectMap<byte[]> PREFIXES = new TokenObjectMap<>();
  /** URIs of reserved namespaces. */
  private static final TokenSet RESERVED = new TokenSet();

  static {
    // reserved namespaces
    NS.add(XML, XML_URI);
    NS.add(XS_PREFIX, XS_URI);
    NS.add(XSI_PREFIX, XSI_URI);
    NS.add(FN_PREFIX, FN_URI);
    NS.add(MATH_PREFIX, MATH_URI);
    NS.add(MAP_PREFIX, MAP_URI);
    NS.add(ARRAY_PREFIX, ARRAY_URI);
    NS.add(XQ_PREFIX, XQ_URI);
    for(int s = NS.size() - 1; s >= 0; s--) RESERVED.add(NS.value(s));

    // additional XQuery namespaces
    NS.add(LOCAL_PREFIX, LOCAL_URI);
    NS.add(OUTPUT_PREFIX, OUTPUT_URI);
    NS.add(ERR_PREFIX, ERROR_URI);

    // EXPath namespaces
    NS.add(EXPERR_PREFIX, EXPERROR_URI);
    NS.add(BIN_PREFIX, BIN_URI);
    NS.add(CRYPTO_PREFIX, CRYPTO_URI);
    NS.add(FILE_PREFIX, FILE_URI);
    NS.add(HTTP_PREFIX, HTTP_URI);
    NS.add(PKG_PREFIX, PKG_URI);
    // EXQuery namespaces
    NS.add(REQUEST_PREFIX, REQUEST_URI);
    NS.add(REST_PREFIX, REST_URI);
    // BaseX namespaces
    NS.add(BASEX_PREFIX, BASEX_URI);
    NS.add(JAVA_PREFIX, JAVA_URI);
    // namespaces of built-in modules
    NS.add(ADMIN_PREFIX, ADMIN_URI);
    NS.add(ARCHIVE_PREFIX, ARCHIVE_URI);
    NS.add(CLIENT_PREFIX, CLIENT_URI);
    NS.add(CONVERT_PREFIX, CONVERT_URI);
    NS.add(CSV_PREFIX, CSV_URI);
    NS.add(DB_PREFIX, DB_URI);
    NS.add(FETCH_PREFIX, FETCH_URI);
    NS.add(FT_PREFIX, FT_URI);
    NS.add(HOF_PREFIX, HOF_URI);
    NS.add(HTML_PREFIX, HTML_URI);
    NS.add(INDEX_PREFIX, INDEX_URI);
    NS.add(INPUT_PREFIX, INPUT_URI);
    NS.add(INSPECT_PREFIX, INSPECT_URI);
    NS.add(JOB_PREFIX, JOB_URI);
    NS.add(JSON_PREFIX, JSON_URI);
    NS.add(LAZY_PREFIX, LAZY_URI);
    NS.add(PERM_PREFIX, PERM_URI);
    NS.add(PROC_PREFIX, PROC_URI);
    NS.add(PROF_PREFIX, PROF_URI);
    NS.add(RANDOM_PREFIX, RANDOM_URI);
    NS.add(REPO_PREFIX, REPO_URI);
    NS.add(SESSION_PREFIX, SESSION_URI);
    NS.add(SESSIONS_PREFIX, SESSIONS_URI);
    NS.add(SQL_PREFIX, SQL_URI);
    NS.add(STORE_PREFIX, STORE_URI);
    NS.add(STRING_PREFIX, STRING_URI);
    NS.add(UNIT_PREFIX, UNIT_URI);
    NS.add(UPDATE_PREFIX, UPDATE_URI);
    NS.add(USER_PREFIX, USER_URI);
    NS.add(UTIL_PREFIX, UTIL_URI);
    NS.add(VALIDATE_PREFIX, VALIDATE_URI);
    NS.add(WEB_PREFIX, WEB_URI);
    NS.add(WS_PREFIX, WS_URI);
    NS.add(XSLT_PREFIX, XSLT_URI);
    NS.add(XQUERY_PREFIX, XQUERY_URI);

    // obsolete prefixes
    NS.add(JOBS_PREFIX, BASEX_URI);
    NS.add(OUT_PREFIX, BASEX_URI);
    NS.add(STRINGS_PREFIX, BASEX_URI);

    for(int s = NS.size() - 1; s >= 0; s--) {
      final byte[] prefix = NS.name(s), uri = NS.value(s);
      URIS.put(prefix, uri);
      PREFIXES.put(uri, prefix);
    }
  }

  /** Private constructor. */
  private NSGlobal() { }

  /**
   * Finds the specified namespace URI.
   * @param prefix prefix of the namespace
   * @return URI or {@code null}
   */
  public static byte[] uri(final byte[] prefix) {
    return URIS.get(prefix);
  }

  /**
   * Finds the prefix for the specified URI.
   * @param uri namespace URI
   * @return prefix, or empty string
   */
  public static byte[] prefix(final byte[] uri) {
    final byte[] prefix = PREFIXES.get(uri);
    return prefix != null ? prefix : EMPTY;
  }

  /**
   * Checks if the specified URI is a reserved namespace.
   * @param uri URI to be checked
   * @return result of check
   */
  public static boolean reserved(final byte[] uri) {
    return RESERVED.contains(uri);
  }
}
