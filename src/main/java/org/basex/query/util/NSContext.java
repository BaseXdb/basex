package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * This class references all statically known namespaces.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public void add(final byte[] pref, final byte[] uri, final InputInfo ii)
      throws QueryException {

    if(eq(pref, XML) || eq(pref, XMLNS)) BINDXML.thrw(ii, pref);
    if(eq(XMLURI, uri)) BINDXMLURI.thrw(ii, uri, XML);
    if(eq(XMLNSURI, uri)) BINDXMLURI.thrw(ii, uri, XMLNS);
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
   * Finds the namespace URI for the specified prefix.
   * @param pref prefix of the namespace
   * @return uri or {@code null}
   */
  public byte[] staticURI(final byte[] pref) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ns.name(s), pref)) return ns.string(s);
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
        if(eq(stack.name(s), pref)) return stack.string(s);
      }
    }
    final byte[] uri = staticURI(pref);
    return uri == null ? NSGlobal.uri(pref) : uri.length == 0 ? null : uri;
  }

  /**
   * Returns the prefix for the specified namespace URI.
   * Required by the JAXP API.
   * @param uri namespace URI
   * @return prefix
   */
  public byte[] prefix(final byte[] uri) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ns.string(s), uri)) return ns.name(s);
    }
    return NSGlobal.prefix(uri);
  }

  /**
   * Returns all prefixes. Required by the XQJ API.
   * @return prefixes
   */
  public byte[][] prefixes() {
    final byte[][] prefs = new byte[ns.size()][];
    for(int p = 0; p < prefs.length; ++p) prefs[p] = ns.name(p);
    return prefs;
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
   * Sets the specified stack.
   * @param s stack to be set
   */
  public void stack(final Atts s) {
    stack = s;
  }

  /**
   * Resets the stack and returns the old instance.
   * @return old instance
   */
  public Atts reset() {
    final Atts s = stack;
    stack = null;
    return s;
  }

  /**
   * Returns the namespace stack.
   * @return stack
   */
  public Atts stack() {
    if(stack == null) stack = new Atts();
    return stack;
  }
}
