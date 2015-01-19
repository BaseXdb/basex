package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * Global namespaces.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Namespace: prefixes and namespace URIs. */
  private static final Atts NS = new Atts();
  /** Reserved namespaces. */
  private static final int RESERVED;

  static {
    // reserved namespaces
    NS.add(XML, XML_URI);
    NS.add(XS_PREFIX, XS_URI);
    NS.add(XSI_PREFIX, XSI_URI);
    NS.add(FN_PREFIX, FN_URI);
    NS.add(MATH_PREFIX, MATH_URI);
    NS.add(MAP_PREFIX, MAP_URI);
    NS.add(ARRAY_PREFIX, ARRAY_URI);
    NS.add(ANN_PREFIX, XQ_URI);
    RESERVED = NS.size();

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
    NS.add(ZIP_PREFIX, ZIP_URI);
    // EXQuery namespaces
    NS.add(REST_PREFIX, REST_URI);
    NS.add(RESTXQ_PREFIX, REST_URI);
    // BaseX namespaces
    NS.add(BXERR_PREFIX, BXERRORS_URI);
    NS.add(BASEX_PREFIX, BASEX_URI);
    // namespaces of built-in modules
    NS.add(ADMIN_PREFIX, ADMIN_URI);
    NS.add(ARCHIVE_PREFIX, ARCHIVE_URI);
    NS.add(CLIENT_PREFIX, CLIENT_URI);
    NS.add(CONVERT_PREFIX, CONVERT_URI);
    NS.add(CSV_PREFIX, CSV_URI);
    NS.add(DB_PREFIX, DB_URI);
    NS.add(FETCH_PREFIX, FETCH_URI);
    NS.add(FT_PREFIX, FT_URI);
    NS.add(HASH_PREFIX, HASH_URI);
    NS.add(HOF_PREFIX, HOF_URI);
    NS.add(HTML_PREFIX, HTML_URI);
    NS.add(INDEX_PREFIX, INDEX_URI);
    NS.add(INSPECT_PREFIX, INSPECT_URI);
    NS.add(JSON_PREFIX, JSON_URI);
    NS.add(OUT_PREFIX, OUT_URI);
    NS.add(PROC_PREFIX, PROC_URI);
    NS.add(PROF_PREFIX, PROF_URI);
    NS.add(QUERY_PREFIX, QUERY_URI);
    NS.add(RANDOM_PREFIX, RANDOM_URI);
    NS.add(REPO_PREFIX, REPO_URI);
    NS.add(SQL_PREFIX, SQL_URI);
    NS.add(STREAM_PREFIX, STREAM_URI);
    NS.add(UNIT_PREFIX, UNIT_URI);
    NS.add(USER_PREFIX, USER_URI);
    NS.add(VLDT_PREFIX, VALIDATE_URI);
    NS.add(XSLT_PREFIX, XSLT_URI);
    NS.add(XQUERY_PREFIX, XQUERY_URI);
  }

  /** Private constructor. */
  private NSGlobal() { }

  /**
   * Finds the specified namespace uri.
   * @param pref prefix of the namespace
   * @return uri or {@code null}
   */
  public static byte[] uri(final byte[] pref) {
    for(int s = NS.size() - 1; s >= 0; s--) {
      if(eq(NS.name(s), pref)) return NS.value(s);
    }
    return null;
  }

  /**
   * Finds the prefix for the specified uri.
   * @param uri namespace uri
   * @return prefix, or empty string
   */
  public static byte[] prefix(final byte[] uri) {
    for(int s = NS.size() - 1; s >= 0; s--) {
      if(eq(NS.value(s), uri)) return NS.name(s);
    }
    return EMPTY;
  }

  /**
   * Checks if the specified uri is a reserved namespace.
   * @param uri uri to be checked
   * @return result of check
   */
  public static boolean reserved(final byte[] uri) {
    for(int s = RESERVED - 1; s >= 0; s--) {
      if(eq(NS.value(s), uri)) return true;
    }
    return false;
  }
}
