package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.net.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * URI item ({@code xs:anyURI}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Uri extends Str {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);
  /** URI validation regex as specified by RFC 3986: Appendix B. */
  private static final Pattern VALID_URI = Pattern.compile(
      "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");

  /**
   * Constructor.
   * @param v value
   */
  private Uri(final byte[] v) {
    super(v, AtomType.URI);
  }

  /**
   * Creates a new uri instance.
   * @param uri value
   * @return uri instance
   */
  public static Uri uri(final byte[] uri) {
    return uri(uri, true);
  }

  /**
   * Creates a new uri instance.
   * @param uri value
   * @return uri instance
   */
  public static Uri uri(final String uri) {
    return uri(Token.token(uri), true);
  }

  /**
   * Creates a new uri instance.
   * @param uri value
   * @param normalize chop leading and trailing whitespaces
   * @return uri instance
   */
  public static Uri uri(final byte[] uri, final boolean normalize) {
    final byte[] u = normalize ? Token.norm(uri) : uri;
    return u.length == 0 ? EMPTY : new Uri(u);
  }

  /**
   * Appends the specified address. If one of the URIs is invalid,
   * the original uri is returned.
   * @param add address to be appended
   * @param info input info
   * @return new uri
   * @throws QueryException query exception
   */
  public Uri resolve(final Uri add, final InputInfo info) throws QueryException {
    if(add.val.length == 0) return this;
    try {
      final URI base = new URI(Token.string(val));
      final URI res = new URI(Token.string(add.val));
      final URI uri = base.resolve(res);
      return uri(Token.token(uri.toString()), false);
    } catch(final URISyntaxException ex) {
      throw URIINVRES.get(info, ex.getMessage());
    }
  }

  /**
   * Tests if this is an absolute URI.
   * @return result of check
   */
  public boolean isAbsolute() {
    final Matcher m = matcher();
    return m.matches() && m.group(2) != null && m.group(9) == null;
  }

  /**
   * Checks the validity of this URI.
   * @return result of check
   */
  public boolean isValid() {
    return matcher().matches();
  }

  /**
   * Creates a regex matcher for the current URI.
   * @return matcher
   */
  private Matcher matcher() {
    return VALID_URI.matcher(Token.string(Token.uri(val, true)));
  }
}
