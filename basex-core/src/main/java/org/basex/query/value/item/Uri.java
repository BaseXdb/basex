package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.net.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.UriParser.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * URI item ({@code xs:anyURI}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Uri extends AStr {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);
  /** Parsed URI (lazy instantiation). */
  private ParsedUri pUri;

  /**
   * Constructor.
   * @param value value
   */
  private Uri(final byte[] value) {
    super(AtomType.ANY_URI, value);
  }

  /**
   * Creates a new uri instance.
   * @param value value
   * @return uri instance
   */
  public static Uri uri(final byte[] value) {
    return uri(value, true);
  }

  /**
   * Creates a new uri instance.
   * @param value string value
   * @return uri instance
   */
  public static Uri uri(final String value) {
    return uri(Token.token(value), true);
  }

  /**
   * Creates a new uri instance.
   * @param value value
   * @param normalize chop leading and trailing whitespaces
   * @return uri instance
   */
  public static Uri uri(final byte[] value, final boolean normalize) {
    final byte[] u = normalize ? Token.normalize(value) : value;
    return u.length == 0 ? EMPTY : new Uri(u);
  }

  /**
   * Checks the URIs for equality.
   * @param uri to be compared
   * @return result of check
   */
  public boolean eq(final Uri uri) {
    return Token.eq(string(), uri.string());
  }

  /**
   * Appends the specified address. If one of the URIs is invalid,
   * the original uri is returned.
   * @param add address to be appended
   * @param ii input info
   * @return new uri
   * @throws QueryException query exception
   */
  public Uri resolve(final Uri add, final InputInfo ii) throws QueryException {
    if(add.value.length == 0) return this;
    try {
      final URI base = new URI(Token.string(value)), res = new URI(Token.string(add.value));
      String uri = base.resolve(res).toString();
      if(uri.startsWith(IO.FILEPREF))
        uri = uri.replaceAll('^' + IO.FILEPREF + "([^/])", IO.FILEPREF + "//$1");
      return uri(Token.token(uri), false);
    } catch(final URISyntaxException ex) {
      throw URIARG_X.get(ii, ex.getMessage());
    }
  }

  /**
   * Tests if this is an absolute URI.
   * @return result of check
   */
  public boolean isAbsolute() {
    return isValid() && parsed().scheme() != null;
  }

  /**
   * Checks the validity of this URI.
   * @return result of check
   */
  public boolean isValid() {
    return parsed().valid();
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return value;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public byte[] string() {
    return value;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    return (mode == Simplify.EBV || mode == Simplify.PREDICATE) ?
      cc.simplify(this, Bln.get(value.length != 0)) : this;
  }

  /**
   * Caches and returns a parsed URI representation.
   * @return parsed uri
   */
  private ParsedUri parsed() {
    if(pUri == null) pUri = UriParser.parse(Token.string(Token.encodeUri(value, true)));
    return pUri;
  }

  @Override
  public URI toJava() throws QueryException {
    try {
      return new URI(Token.string(value));
    } catch(final URISyntaxException ex) {
      throw new QueryException(ex);
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Uri)) return false;
    final Uri u = (Uri) obj;
    return type == u.type && Token.eq(value, u.value);
  }

}
