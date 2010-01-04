package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.util.Atts;

/**
 * Global namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Namespaces. */
  private static Atts atts = new Atts();

  static {
    atts.add(LOCAL, LOCALURI);
    atts.add(XS, XSURI);
    atts.add(XSI, XSIURI);
    atts.add(FN, FNURI);
    atts.add(XMLNS, XMLNSURI);
    atts.add(XML, XMLURI);
    atts.add(BASEX, BXURI);
  }

  /** Private constructor. */
  private NSGlobal() { }

  /**
   * Finds the specified namespace uri.
   * @param pre prefix of the namespace
   * @return uri
   */
  public static byte[] uri(final byte[] pre) {
    for(int s = atts.size - 1; s >= 0; s--) {
      if(eq(atts.key[s], pre)) return atts.val[s];
    }
    return EMPTY;
  }

  /**
   * Finds the specified URI prefix.
   * @param uri URI
   * @return prefix
   */
  static byte[] prefix(final byte[] uri) {
    for(int s = atts.size - 1; s >= 0; s--) {
      if(eq(atts.val[s], uri)) return atts.key[s];
    }
    return EMPTY;
  }

  /**
   * Checks if the specified uri is a standard uri.
   * @param uri uri to be checked
   * @return result of check
   */
  public static boolean standard(final byte[] uri) {
    // 'local' namespace is skipped
    for(int s = atts.size - 1; s > 0; s--) {
      if(eq(atts.val[s], uri)) return true;
    }
    return false;
  }
}
