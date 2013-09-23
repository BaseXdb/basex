package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * Global namespaces.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Namespace: prefixes and namespace URIs. */
  private static final Atts NS = new Atts();
  /** Reserved namespaces. */
  private static final int RESERVED;

  static {
    // reserved namespaces
    NS.add(XML, XMLURI);
    NS.add(XS, XSURI);
    NS.add(XSI, XSIURI);
    NS.add(FN, FNURI);
    NS.add(MATH, MATHURI);
    NS.add(MAP, MAPURI);
    NS.add(ANN, XQURI);
    RESERVED = NS.size();

    // implementation-defined pre-declarations
    NS.add(LOCAL, LOCALURI);
    NS.add(OUTPUT, OUTPUTURI);
    NS.add(ERR, ERRORURI);
    // EXPath namespaces
    NS.add(EXPERR, EXPERROR);
    NS.add(CRYPTO, CRYPTOURI);
    NS.add(FILE, FILEURI);
    NS.add(HTTP, HTTPURI);
    NS.add(PKG, PKGURI);
    NS.add(ZIP, ZIPURI);
    // EXQuery namespaces
    NS.add(REST, RESTURI);
    NS.add(RESTXQ, RESTURI);
    // BaseX namespaces
    NS.add(BXERR, BXERRORS);
    NS.add(BASEX, BASEXURI);

    NS.add(ADMIN, ADMINURI);
    NS.add(ARCHIVE, ARCHIVEURI);
    NS.add(CLIENT, CLIENTURI);
    NS.add(CONVERT, CONVERTURI);
    NS.add(CSV, CSVURI);
    NS.add(DB, DBURI);
    NS.add(FETCH, FETCHURI);
    NS.add(FT, FTURI);
    NS.add(HASH, HASHURI);
    NS.add(HOF, HOFURI);
    NS.add(HTML, HTMLURI);
    NS.add(INDEX, INDEXURI);
    NS.add(INSPECT, INSPECTURI);
    NS.add(JSON, JSONURI);
    NS.add(OUT, OUTURI);
    NS.add(PROC, PROCURI);
    NS.add(PROF, PROFURI);
    NS.add(QUERY, QUERYURI);
    NS.add(RANDOM, RANDOMURI);
    NS.add(REPO, REPOURI);
    NS.add(SQL, SQLURI);
    NS.add(STREAM, STREAMURI);
    NS.add(UNIT, UNITURI);
    NS.add(VLDT, VALIDATEURI);
    NS.add(XSLT, XSLTURI);
    NS.add(XQRY, XQUERYURI);
  }

  /** Private constructor. */
  private NSGlobal() { }

  /**
   * Finds the specified namespace uri.
   * @param pref prefix of the namespace
   * @return uri, or {@code null}
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
