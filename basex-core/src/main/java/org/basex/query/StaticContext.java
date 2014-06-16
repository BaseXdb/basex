package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains the static context of an expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class StaticContext {
  /** Decimal formats. */
  public final TokenObjMap<DecFormatter> decFormats = new TokenObjMap<>();
  /** Static and dynamic namespaces. */
  public final NSContext ns = new NSContext();
  /** Mix updates flag. */
  public final boolean mixUpdates;

  /** Default collation (default collection ({@link QueryText#COLLATIONURI}): {@code null}). */
  public Collation collation;
  /** Default element/type namespace. */
  public byte[] elemNS;
  /** Default function namespace. */
  public byte[] funcNS = FNURI;
  /** Context item static type. */
  public SeqType initType;

  /** Construction mode. */
  public boolean strip;
  /** Ordering mode. */
  public boolean ordered;
  /** Default order for empty sequences. */
  public boolean orderGreatest;
  /** Boundary-space policy. */
  public boolean spaces;
  /** Copy-namespaces mode: (no-)preserve. */
  public boolean preserveNS = true;
  /** Copy-namespaces mode: (no-)inherit. */
  public boolean inheritNS = true;

  /** XQuery version flag. */
  private boolean xquery3;
  /** Static Base URI. */
  private Uri baseURI = Uri.EMPTY;

  /**
   * Constructor setting the XQuery version.
   * @param ctx database context
   */
  public StaticContext(final Context ctx) {
    final MainOptions opts = ctx.options;
    mixUpdates = opts.get(MainOptions.MIXUPDATES);
    xquery3 = opts.get(MainOptions.XQUERY3);
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the specified URI is an empty string.
   * The default element namespaces is set if the specified prefix is empty.
   * @param prefix namespace prefix
   * @param uri namespace URI
   * @throws QueryException query exception
   */
  public void namespace(final String prefix, final String uri) throws QueryException {
    if(prefix.isEmpty()) {
      elemNS = uri.isEmpty() ? null : token(uri);
    } else if(uri.isEmpty()) {
      ns.delete(token(prefix));
    } else {
      ns.add(token(prefix), token(uri), null);
    }
  }

  /**
   * Returns an IO representation of the static base URI, or {@code null}.
   * @return IO reference
   */
  public IO baseIO() {
    return baseURI == Uri.EMPTY ? null : IO.get(baseURI.toJava());
  }

  /**
   * Returns an IO reference for the specified filename.
   * If a base URI exists, it is merged with the specified filename.
   * Otherwise, a plain reference is returned.
   * @param path file path
   * @return io reference
   */
  public IO io(final String path) {
    final IO base = baseIO();
    return base != null ? base.merge(path) : IO.get(path);
  }

  /**
   * Returns the static base URI.
   * @return base URI
   */
  public Uri baseURI() {
    return baseURI;
  }

  /**
   * Sets the static base URI.
   * @param uri uri to be set
   */
  public void baseURI(final String uri) {
    final IO base = IO.get(uri);
    if(uri.isEmpty()) {
      baseURI = Uri.EMPTY;
    } else if(base instanceof IOContent) {
      baseURI = Uri.uri(uri);
    } else if(baseURI == Uri.EMPTY) {
      baseURI = Uri.uri(base.url());
    } else {
      baseURI = Uri.uri(baseIO().merge(uri).url());
    }
  }

  /**
   * Assigns the XQuery 3.0 flag.
   * @param xq30 XQuery 3.0 flag
   */
  public void xquery3(final boolean xq30) {
    xquery3 = xq30;
  }

  /**
   * Checks if XQuery 3.0 features are allowed.
   * @return {@code true} if XQuery 3.0 is allowed, {@code false} otherwise
   */
  public boolean xquery3() {
    return xquery3;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + baseIO() + ']';
  }
}
