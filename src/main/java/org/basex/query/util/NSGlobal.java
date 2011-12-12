package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.util.Atts;

/**
 * Global namespaces.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Namespaces, containing prefixes and uris. */
  private static final Atts NS = new Atts();

  static {
    // pre-declared namespaces (the order is important here)
    NS.add(LOCAL, LOCALURI);
    NS.add(XML, XMLURI);
    NS.add(XS, XSURI);
    NS.add(XSI, XSIURI);
    NS.add(FN, FNURI);
    NS.add(OPT, OPTIONURI);
    // implementation-defined pre-declarations
    NS.add(MAP, MAPURI);
    NS.add(MATH, MATHURI);
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
    // EXPath namespaces
    NS.add(CRYPTO, CRYPTOURI);
    NS.add(FILE, FILEURI);
    NS.add(HTTP, HTTPURI);
    NS.add(PKG, PKGURI);
    NS.add(ZIP, ZIPURI);
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
   * Checks if the specified uri is a standard uri.
   * @param uri uri to be checked
   * @return result of check
   */
  public static boolean standard(final byte[] uri) {
    // first ('local') namespace is skipped
    for(int s = NS.size() - 1; s > 0; s--) {
      if(eq(NS.string(s), uri)) return true;
    }
    return false;
  }
}
