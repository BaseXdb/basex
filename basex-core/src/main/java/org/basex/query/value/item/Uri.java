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
import org.basex.util.Token.*;

/**
 * URI item ({@code xs:anyURI}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Uri extends AStr {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);
  /** Parsed URI (lazy instantiation). */
  private ParsedUri parsed;

  /**
   * Constructor.
   * @param value value
   */
  private Uri(final byte[] value) {
    super(value, AtomType.ANY_URI);
  }

  /**
   * Creates a new URI instance.
   * @param value value
   * @return URI instance
   */
  public static Uri get(final byte[] value) {
    return get(value, true);
  }

  /**
   * Creates a new URI instance.
   * @param value string value
   * @return URI instance
   */
  public static Uri get(final String value) {
    return get(Token.token(value), true);
  }

  /**
   * Creates a new URI instance.
   * @param value value
   * @param normalize remove leading and trailing whitespace
   * @return URI instance
   */
  public static Uri get(final byte[] value, final boolean normalize) {
    final byte[] uri = normalize ? Token.normalize(value) : value;
    return uri.length == 0 ? EMPTY : new Uri(uri);
  }

  /**
   * Checks the URIs for equality.
   * @param uri to be compared
   * @return result of check
   */
  public boolean eq(final Uri uri) {
    return Token.eq(value, uri.value);
  }

  /**
   * Appends the specified address. If one of the URIs is invalid,
   * the original URI is returned.
   * @param add address to be appended
   * @param info input info (can be {@code null})
   * @return new URI
   * @throws QueryException query exception
   */
  public Uri resolve(final Uri add, final InputInfo info) throws QueryException {
    if(add.value.length == 0) return this;
    try {
      final URI res = new URI(Token.string(add.value));
      URI base = new URI(Token.string(value));
      if("".equals(base.getPath()) && !"".equals(base.getHost())) {
        // work around JDK-8272702, https://bugs.java.com/bugdatabase/view_bug?bug_id=8272702
        base = new URI(Token.string(value) + "/");
      }
      String uri = base.resolve(res).toString();
      if(uri.startsWith(IO.FILEPREF))
        uri = uri.replaceAll('^' + IO.FILEPREF + "([^/])", IO.FILEPREF + "//$1");
      return get(Token.token(uri), false);
    } catch(final URISyntaxException ex) {
      throw URIARG_X.get(info, ex.getMessage());
    }
  }

  /**
   * Tests if the URI is absolute.
   * @return result of check
   */
  public boolean isAbsolute() {
    return parsed().absolute();
  }

  /**
   * Tests if the URI is valid.
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
  public int hashCode() {
    return Token.hashCode(value);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E[xs:anyURI('x')]  ->  E[true()]
      expr = Bln.get(value.length != 0);
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * Caches and returns a parsed URI representation.
   * @return parsed URI
   */
  private ParsedUri parsed() {
    if(parsed == null) {
      parsed = UriParser.parse(Token.string(Token.encodeUri(value, UriEncoder.IRI)));
    }
    return parsed;
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
    return this == obj || obj instanceof final Uri uri && Token.eq(value, uri.value);
  }
}
