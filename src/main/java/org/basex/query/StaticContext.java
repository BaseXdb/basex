package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.io.IO;
import org.basex.query.item.SeqType;
import org.basex.query.item.Uri;
import org.basex.query.util.NSContext;
import org.basex.query.util.format.DecFormatter;
import org.basex.util.Util;
import org.basex.util.hash.TokenObjMap;

/**
 * This class contains the static context of an expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StaticContext {
  /** Static and dynamic namespaces. */
  public NSContext ns = new NSContext();
  /** Default element/type namespace. */
  public byte[] nsElem;
  /** Default function namespace. */
  public byte[] nsFunc = FNURI;
  /** Context item static type. */
  public SeqType initType;

  /** Construction mode. */
  public boolean construct;
  /** Ordering mode. */
  public boolean ordered;
  /** Default order for empty sequences. */
  public boolean orderGreatest;
  /** Boundary-space policy. */
  public boolean spaces;
  /** Copy-namespaces mode: (no-)preserve. */
  public boolean nsPreserve = true;
  /** Copy-namespaces mode: (no-)inherit. */
  public boolean nsInherit = true;

  /** Static Base URI. */
  private byte[] baseURI = EMPTY;
  /** Default collation. */
  private Uri collation = Uri.uri(URLCOLL, false);

  /** Decimal formats. */
  public final TokenObjMap<DecFormatter> decFormats =
    new TokenObjMap<DecFormatter>();

  /**
   * Adopts values of the specified static context.
   * @param sc static context
   */
  public void copy(final StaticContext sc) {
    ns = sc.ns;
    nsElem = sc.nsElem;
    nsFunc = sc.nsFunc;
    // vars, ctxItem, funcs
    collation = sc.collation;
    construct = sc.construct;
    ordered = sc.ordered;
    orderGreatest = sc.orderGreatest;
    spaces = sc.spaces;
    nsPreserve = sc.nsPreserve;
    nsInherit = sc.nsInherit;
    baseURI = sc.baseURI;
    // decFormats
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the {@code uri} is an empty string.
   * The default element namespaces is set if the {@code prefix} is empty.
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @throws QueryException query exception
   */
  public void namespace(final String prefix, final String uri)
      throws QueryException {

    if(prefix.isEmpty()) {
      nsElem = uri.isEmpty() ? null : token(uri);
    } else if(uri.isEmpty()) {
      ns.delete(token(prefix));
    } else {
      ns.add(token(prefix), token(uri), null);
    }
  }

  /**
   * Returns an IO representation of the base URI, or {@code null}.
   * @return IO reference
   */
  public IO baseIO() {
    return baseURI.length != 0 ? IO.get(string(baseURI)) : null;
  }

  /**
   * Returns a URI representation of the base URI.
   * @return IO reference
   */
  public Uri baseURI() {
    return Uri.uri(baseURI, false);
  }

  /**
   * Sets the base URI.
   * @param uri uri to be set
   */
  public void baseURI(final String uri) {
    baseURI = norm(token(uri));
  }

  /**
   * Returns a URI representation of the base URI.
   * @return IO reference
   */
  public Uri collation() {
    return collation;
  }

  /**
   * Sets the collation.
   * @param uri uri to be set
   */
  public void collation(final String uri) {
    collation = Uri.uri(token(uri));
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + baseIO() + ']';
  }
}
