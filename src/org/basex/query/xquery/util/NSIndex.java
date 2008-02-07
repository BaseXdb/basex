package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Token;

/**
 * Global namespace index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NSIndex {
  /** Default namespaces. */
  private QNm[] names = {
      new QNm(LOCAL, Uri.LOCAL), new QNm(XS, Uri.XS), new QNm(XSI, Uri.XSI), 
      new QNm(FN, Uri.FN), new QNm(XMLNS, Uri.XMLNS), new QNm(XML, Uri.XML),
      new QNm(BASEX, Uri.BX)
  };
  /** Singleton instance. */
  private static NSIndex instance;

  /**
   * Gets the function instance.
   * @return instance
   */
  public static NSIndex get() {
    if(instance == null) instance = new NSIndex();
    return instance;
  }

  /**
   * Finds the specified namespace uri.
   * @param pre prefix of the namespace
   * @return uri
   */
  public Uri uri(final byte[] pre) {
    for(int s = names.length - 1; s >= 0; s--) {
      if(eq(pre, names[s].str())) return names[s].uri;
    }
    return Uri.EMPTY;
  }

  /**
   * Checks if the specified uri is a default uri.
   * @param uri uri to be checked
   * @return result of check
   */
  public boolean standard(final Uri uri) {
    for(int s = names.length - 1; s >= 1; s--) {
      if(uri.eq(names[s].uri)) return true;
    }
    return false;
  }

  /**
   * Finds the specified URI prefix.
   * @param uri URI
   * @return prefix
   */
  public byte[] prefix(final Uri uri) {
    for(int s = names.length - 1; s >= 0; s--) {
      if(uri.eq(names[s].uri)) return names[s].str();
    }
    return Token.EMPTY;
  }
}
