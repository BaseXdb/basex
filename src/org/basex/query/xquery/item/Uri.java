package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import java.net.URI;
import org.basex.util.Token;

/**
 * URI item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Uri extends Str {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);
  /** Default namespace. */
  public static final Uri LOCAL = new Uri(LOCALURI);
  /** Default namespace. */
  public static final Uri XMLNS = new Uri(XMLNSURI);
  /** Default namespace. */
  public static final Uri XML = new Uri(XMLURI);
  /** Default namespace. */
  public static final Uri FN = new Uri(FNURI);
  /** Default namespace. */
  public static final Uri XS = new Uri(XSURI);
  /** Default namespace. */
  public static final Uri XSI = new Uri(XSIURI);
  /** Default namespace. */
  public static final Uri BX = new Uri(BXURI);

  /**
   * Constructor.
   * @param v value
   */
  private Uri(final byte[] v) {
    super(Token.norm(v), Type.URI);
  }

  /**
   * Constructor.
   * @param v value
   * @param t type (URI)
   */
  private Uri(final byte[] v, final Type t) {
    super(Token.norm(v), t);
  }

  /**
   * Creates a new uri instance.
   * @param v value
   * @return uri instance
   */
  public static Uri uri(final byte[] v) {
    return v.length == 0 ? Uri.EMPTY : new Uri(v, Type.URI);
  }

  /**
   * Compares the specified uri.
   * @param uri uri to be compared
   * @return result of check
   */
  public boolean eq(final Uri uri) {
    return uri != null && (val == uri.val || Token.eq(val, uri.val));
  }

  /**
   * Appends the specified address. If one of the URIs is invalid,
   * the original uri is returned.
   * @param add address to be appended
   * @return new uri
   */
  public Uri resolve(final Uri add) {
    if(add.val.length == 0) return this;

    try {
      final java.net.URI base = new URI(Token.string(val));
      final URI uri = base.resolve(Token.string(add.val));
      return uri(Token.token(uri.toString()));
    } catch(final Exception e) {
      return this;
    }
  }

  /**
   * Returns if this is an absolute URI.
   * @return result of check
   */
  public boolean absolute() {
    return Token.contains(val, ':');
  }

  /**
   * Checks the validity of this URI.
   * @return result of check
   */
  public boolean valid() {
    try {
      new URI(Token.string(val));
      return true;
    } catch(final Exception e) {
      return false;
    }
  }
}
