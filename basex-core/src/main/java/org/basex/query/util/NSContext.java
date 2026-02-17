package org.basex.query.util;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This class references all statically known namespaces.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NSContext {
  /** Static namespaces, containing prefixes and URIs. */
  public final Atts list = new Atts();

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
    list.add(prefix, uri);
  }

  /**
   * Deletes the specified namespace at parsing time.
   * @param prefix namespace prefix
   */
  public void delete(final byte[] prefix) {
    for(int s = list.size() - 1; s >= 0; s--) {
      if(eq(prefix, list.name(s))) {
        list.remove(s);
        break;
      }
    }
  }

  /**
   * Finds the namespace URI for the specified prefix if it is found
   * in statically declared namespaces.
   * @param prefix prefix of the namespace
   * @return URI or {@code null}
   */
  public byte[] resolveDeclared(final byte[] prefix) {
    for(int s = list.size() - 1; s >= 0; s--) {
      if(eq(list.name(s), prefix)) return list.value(s);
    }
    return null;
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
