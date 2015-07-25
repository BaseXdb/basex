package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains the static context of an expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class StaticContext {
  /** Decimal formats. */
  public final TokenObjMap<DecFormatter> decFormats = new TokenObjMap<>();
  /** Static and dynamic namespaces. */
  public final NSContext ns = new NSContext();
  /** Mix updates flag. */
  public final boolean mixUpdates;

  /** Default collation (default collection ({@link QueryText#COLLATION_URI}): {@code null}). */
  public Collation collation;
  /** Default element/type namespace. */
  public byte[] elemNS;
  /** Default function namespace. */
  public byte[] funcNS = FN_URI;
  /** Static type of context value. */
  SeqType contextType;

  /** Construction mode. */
  boolean strip;
  /** Ordering mode. */
  boolean ordered;
  /** Default order for empty sequences. */
  boolean orderGreatest;
  /** Boundary-space policy. */
  boolean spaces;
  /** Copy-namespaces mode: (no-)preserve. */
  boolean preserveNS = true;
  /** Copy-namespaces mode: (no-)inherit. */
  boolean inheritNS = true;

  /** Static Base URI. */
  private Uri baseURI = Uri.EMPTY;
  /** Sets a module URI resolver. */
  UriResolver resolver;

  /**
   * Constructor setting the XQuery version.
   * @param qc query context
   */
  public StaticContext(final QueryContext qc) {
    mixUpdates = qc.context.options.get(MainOptions.MIXUPDATES);
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the specified URI is an empty string.
   * The default element namespaces is set if the specified prefix is empty.
   * @param prefix namespace prefix
   * @param uri namespace URI
   * @throws QueryException query exception
   */
  void namespace(final String prefix, final String uri) throws QueryException {
    if(prefix.isEmpty()) {
      elemNS = uri.isEmpty() ? null : token(uri);
    } else if(uri.isEmpty()) {
      ns.delete(token(prefix));
    } else {
      ns.add(token(prefix), token(uri), null);
    }
  }

  /**
   * Returns an IO representation of the static base URI or {@code null}.
   * @return IO reference
   */
  public IO baseIO() {
    return baseURI == Uri.EMPTY ? null : IO.get(string(baseURI.string()));
  }

  /**
   * Returns an IO reference for the specified path.
   * If a base URI exists, it is merged with the path.
   * @param path file path
   * @param uri module namespace (can be {@code null})
   * @return io reference
   */
  IO resolve(final String path, final String uri) {
    if(resolver != null) return resolver.resolve(path, uri, baseURI);
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
    String url;
    if(uri.isEmpty()) {
      url = "";
    } else if(base instanceof IOContent) {
      url = uri;
    } else if(baseURI == Uri.EMPTY) {
      url = base.url();
    } else {
      url = baseIO().merge(uri).url();
    }
    // #1062: check if specified URI points to a directory. if yes, add trailing slash
    if(!url.endsWith("/") && (uri.endsWith(".") || uri.endsWith("/"))) url += '/';
    baseURI = Uri.uri(url);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + baseIO() + ']';
  }
}
