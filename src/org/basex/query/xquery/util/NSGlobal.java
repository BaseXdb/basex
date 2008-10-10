package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Token;

/**
 * Global namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NSGlobal {
  /** Default namespaces. */
  private static final QNm[] NAMES = {
      new QNm(LOCAL, Uri.LOCAL), new QNm(XS, Uri.XS), new QNm(XSI, Uri.XSI), 
      new QNm(FN, Uri.FN), new QNm(XMLNS, Uri.XMLNS), new QNm(XML, Uri.XML),
      new QNm(BASEX, Uri.BX)
  };

  /** Private constructor. */
  private NSGlobal() { }

  /**
   * Finds the specified namespace uri.
   * @param pre prefix of the namespace
   * @return uri
   */
  public static Uri uri(final byte[] pre) {
    for(int s = NAMES.length - 1; s >= 0; s--) {
      if(eq(NAMES[s].str(), pre)) return NAMES[s].uri;
    }
    return Uri.EMPTY;
  }

  /**
   * Checks if the specified uri is a standard uri.
   * @param uri uri to be checked
   * @return result of check
   */
  public static boolean standard(final Uri uri) {
    for(int s = NAMES.length - 1; s > 0; s--) {
      if(NAMES[s].uri.eq(uri)) return true;
    }
    return false;
  }

  /**
   * Finds the specified URI prefix.
   * @param uri URI
   * @return prefix
   */
  public static byte[] prefix(final Uri uri) {
    for(int s = NAMES.length - 1; s >= 0; s--) {
      if(NAMES[s].uri.eq(uri)) return NAMES[s].str();
    }
    return Token.EMPTY;
  }
}
