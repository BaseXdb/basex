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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StaticContext {
  /** Decimal formats. */
  public final TokenObjMap<DecFormatter> decFormats = new TokenObjMap<>();
  /** Static and dynamic namespaces. */
  public final NSContext ns = new NSContext();
  /** Mix updates flag. */
  public final boolean mixUpdates;
  /** Look up documents in databases. */
  public final boolean withdb;

  /** Default collation (default collection ({@link QueryText#COLLATION_URI}): {@code null}). */
  public Collation collation;
  /** Default element/type namespace. */
  public byte[] elemNS;
  /** Default function namespace. */
  public byte[] funcNS = FN_URI;
  /** Name of module (not assigned for main module). */
  public QNm module;

  /** Construction mode. */
  public boolean strip;
  /** Ordering mode. */
  public boolean ordered = true;
  /** Default order for empty sequences. */
  public boolean orderGreatest;
  /** Boundary-space policy. */
  public boolean spaces;
  /** Copy-namespaces mode: (no-)preserve. */
  public boolean preserveNS = true;
  /** Copy-namespaces mode: (no-)inherit. */
  public boolean inheritNS = true;

  /** Static type of context value. */
  SeqType contextType;
  /** Sets a module URI resolver. */
  UriResolver resolver;

  /** Static Base URI. */
  private Uri baseURI = Uri.EMPTY;

  /**
   * Constructor.
   * @param qc query context
   */
  public StaticContext(final QueryContext qc) {
    mixUpdates = qc.context.options.get(MainOptions.MIXUPDATES);
    withdb = qc.context.options.get(MainOptions.WITHDB);
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
   * Returns the static base URI.
   * @return base URI
   */
  public Uri baseURI() {
    return baseURI;
  }

  /**
   * Sets the static base URI.
   * @param uri uri to be set: an empty URI will be ignored, {@code null} invalidates the URI
   */
  public void baseURI(final String uri) {
    String string = "";
    if(uri != null) {
      // ignore empty URIs
      if(uri.isEmpty()) return;
      // adopt original URIs that do not adhere to a known IO schema
      string = IO.get(uri) instanceof IOContent ? uri : resolve(uri).url();
      // #1062: check if specified URI points to a directory. if yes, add trailing slash
      if(!Strings.endsWith(string, '/') &&
        (Strings.endsWith(uri, '.') || Strings.endsWith(uri, '/'))) string += '/';
    }
    baseURI = Uri.uri(string);
  }

  /**
   * Returns an IO representation of the static base URI or {@code null}.
   * @return IO reference (can be {@code null})
   */
  public IO baseIO() {
    return baseURI == Uri.EMPTY ? null : IO.get(string(baseURI.string()));
  }

  /**
   * Resolves the specified path against the base URI.
   * @param path to be resolved
   * @return resulting path
   */
  public IO resolve(final String path) {
    final IO baseIO = baseIO();
    return baseIO == null ? IO.get(path) : baseIO.merge(path);
  }

  /**
   * Returns an IO reference for the specified path.
   * If a base URI exists, it is merged with the path.
   * @param path file path
   * @param uri module namespace (can be {@code null}, only relevant for custom resolver)
   * @return io reference
   */
  public IO resolve(final String path, final String uri) {
    return resolver == null ? resolve(path) : resolver.resolve(path, uri, baseURI);
  }

  /**
   * Returns a decimal format.
   * @param id format id
   * @return decimal format or {@code null}
   * @throws QueryException query exception
   */
  public DecFormatter decFormat(final byte[] id) throws QueryException {
    DecFormatter df = decFormats.get(id);
    if(df == null && eq(id, EMPTY)) {
      // lazy instantiation of default decimal format
      df = new DecFormatter(null, null);
      decFormats.put(id, df);
    }
    return df;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + baseIO() + ']';
  }
}
