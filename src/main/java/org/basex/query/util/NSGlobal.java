package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.util.Atts;

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
    NS.add(OPT, OPTIONURI);
    NS.add(MAP, MAPURI);
    RESERVED = NS.size();

    // implementation-defined pre-declarations
    NS.add(LOCAL, LOCALURI);
    NS.add(OUTPUT, OUTPUTURI);
    NS.add(ERR, ERRORURI);
    // BaseX namespaces
    NS.add(BASEX, BASEXURI);
    NS.add(DB, DBURI);
    NS.add(FT, FTURI);
    NS.add(HOF, HOFURI);
    NS.add(INDEX, INDEXURI);
    NS.add(JSON, JSONURI);
    NS.add(SQL, SQLURI);
    NS.add(UTIL, UTILURI);
    NS.add(XSLT, XSLTURI);
    NS.add(REPO, REPOURI);
    // EXPath namespaces
    NS.add(CRYPTO, CRYPTOURI);
    NS.add(FILE, FILEURI);
    NS.add(HTTP, HTTPURI);
    NS.add(ZIP, ZIPURI);
    NS.add(PKG, PKGURI);
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
      if(eq(NS.name(s), pref)) return NS.string(s);
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
      if(eq(NS.string(s), uri)) return NS.name(s);
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
      if(eq(NS.string(s), uri)) return true;
    }
    return false;
  }
}
