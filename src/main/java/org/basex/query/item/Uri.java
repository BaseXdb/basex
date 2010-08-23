package org.basex.query.item;

import java.net.URI;
import org.basex.util.Token;

/**
 * URI item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Uri extends Str {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);

  /**
   * Constructor.
   * @param v value
   */
  Uri(final byte[] v) {
    super(v, Type.URI);
  }

  /**
   * Creates a new uri instance.
   * @param v value
   * @return uri instance
   */
  public static Uri uri(final byte[] v) {
    return v.length == 0 ? Uri.EMPTY : new Uri(Token.norm(v));
  }

  /**
   * Compares the specified uri.
   * @param uri uri to be compared
   * @return result of check
   */
  public boolean eq(final Uri uri) {
    return val == uri.val || Token.eq(val, uri.val);
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
      final URI base = new URI(Token.string(val));
      final URI uri = base.resolve(Token.string(add.val));
      return uri(Token.token(uri.toString()));
    } catch(final Exception ex) {
      return this;
    }
  }

  /**
   * Tests if this is an absolute URI.
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
    } catch(final Exception ex) {
      return false;
    }
  }

  @Override
  public SeqType type() {
    return SeqType.URI;
  }
}
