package org.basex.query.util;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class references all statically known namespaces.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NSContext {
  /** Static namespaces, containing prefixes and URIs. */
  public final Atts list = new Atts();
  /** URIs of the static namespaces, indexed by their prefixes. */
  private final TokenObjectMap<byte[]> uris = new TokenObjectMap<>();

  /**
   * Validates and adds the specified namespace at parsing time.
   * @param prefix namespace prefix
   * @param uri namespace URI
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void add(final byte[] prefix, final byte[] uri, final InputInfo info)
      throws QueryException {
    if(eq(prefix, XML, XMLNS)) throw BINDXML_X.get(info, prefix);
    if(eq(uri, XML_URI)) throw BINDXMLURI_X_X.get(info, uri, XML);
    if(eq(uri, XMLNS_URI)) throw BINDXMLURI_X_X.get(info, uri, XMLNS);
    if(uris.put(prefix, uri) == null) list.add(prefix, uri);
    else list.value(list.get(prefix), uri);
  }

  /**
   * Deletes the specified namespace at parsing time.
   * @param prefix namespace prefix
   */
  public void delete(final byte[] prefix) {
    if(uris.remove(prefix) != 0) list.remove(list.get(prefix));
  }

  /**
   * Finds the namespace URI for the specified prefix if it is found
   * in statically declared namespaces.
   * @param prefix prefix of the namespace
   * @return URI or {@code null}
   */
  public byte[] resolveDeclared(final byte[] prefix) {
    return uris.get(prefix);
  }

  /**
   * Returns the namespace URI for the specified prefix if it is either found in the static or
   * predefined namespaces.
   * @param prefix prefix of the namespace
   * @return namespace URI or {@code null}
   */
  public byte[] resolveStatic(final byte[] prefix) {
    final byte[] u = resolveDeclared(prefix);
    return u == null ? prefix.length == 0 ? null : NSGlobal.uri(prefix) : u.length == 0 ? null : u;
  }
}
