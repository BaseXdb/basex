package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This class references all statically known namespaces.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NSContext {
  /** Static namespaces, containing prefixes and URIs. */
  private final Atts ns = new Atts();
  /** Dynamically added namespaces. */
  private Atts stack;

  /**
   * Validates and adds the specified namespace at parsing time.
   * @param pref namespace prefix
   * @param uri namespace URI
   * @param ii input info
   * @throws QueryException query exception
   */
  public void add(final byte[] pref, final byte[] uri, final InputInfo ii) throws QueryException {
    if(eq(pref, XML, XMLNS)) throw BINDXML_X.get(ii, pref);
    if(eq(uri, XML_URI)) throw BINDXMLURI_X_X.get(ii, uri, XML);
    if(eq(uri, XMLNS_URI)) throw BINDXMLURI_X_X.get(ii, uri, XMLNS);
    ns.add(pref, uri);
  }

  /**
   * Deletes the specified namespace at parsing time.
   * @param pref namespace prefix
   */
  public void delete(final byte[] pref) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(pref, ns.name(s))) ns.delete(s);
    }
  }

  /**
   * Finds the namespace URI for the specified prefix, if it is found
   * in statically declared namespaces.
   * @param pref prefix of the namespace
   * @return uri or {@code null}
   */
  public byte[] staticURI(final byte[] pref) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ns.name(s), pref)) return ns.value(s);
    }
    return null;
  }

  /**
   * Returns the namespace URI for the specified prefix, if it is either
   * found in the dynamic, static or predefined namespaces.
   * @param pref prefix of the namespace
   * @return namespace URI or {@code null}
   */
  public byte[] uri(final byte[] pref) {
    if(stack != null) {
      for(int s = stack.size() - 1; s >= 0; s--) {
        if(eq(stack.name(s), pref)) return stack.value(s);
      }
    }
    final byte[] u = staticURI(pref);
    return u == null ? pref.length == 0 ? null : NSGlobal.uri(pref) : u.length == 0 ? null : u;
  }

  /**
   * Returns the number of dynamic namespaces.
   * @return namespaces
   */
  public int size() {
    return stack().size();
  }

  /**
   * Sets the number of dynamic namespaces.
   * @param s namespaces
   */
  public void size(final int s) {
    stack().size(s);
  }

  /**
   * Adds a namespace to the namespace stack.
   * @param pref namespace prefix
   * @param uri namespace URI
   */
  public void add(final byte[] pref, final byte[] uri) {
    stack().add(pref, uri);
  }

  /**
   * Returns the namespace stack.
   * @return stack
   */
  private Atts stack() {
    if(stack == null) stack = new Atts();
    return stack;
  }
}
