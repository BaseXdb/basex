package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * Local namespaces.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class NSLocal {
  /** Namespaces. */
  private final Atts ns = new Atts();
  /** Number of default namespaces. */
  private int def;

  /**
   * Validates and adds the specified namespace.
   * @param name namespace
   * @param ii input info
   * @throws QueryException query exception
   */
  public void add(final QNm name, final InputInfo ii) throws QueryException {
    final byte[] ln = name.ln();
    if(eq(ln, XML) || eq(ln, XMLNS)) NSDEF.thrw(ii, name);
    final byte[] uri = name.uri().string();
    if(eq(XMLURI, uri)) NOXMLNS.thrw(ii, XML, name);
    if(eq(XMLNSURI, uri)) NOXMLNS.thrw(ii, XMLNS, name);
    ns.add(ln, uri);
  }

  /**
   * Adds the specified namespace.
   * @param name namespace
   */
  public void add(final QNm name) {
    ns.add(name.ln(), name.uri().string());
  }

  /**
   * Deletes the specified namespace.
   * @param name namespace
   */
  public void delete(final QNm name) {
    final byte[] ln = name.ln();
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ln, ns.key(s))) ns.delete(s);
    }
  }

  /**
   * Finds the URI for the specified prefix in the local and global namespaces.
   * Throws an exception if no namespace is defined.
   * @param pref prefix of the namespace
   * @param dn dynamic error
   * @param ii input info
   * @return uri
   * @throws QueryException query exception
   */
  public byte[] uri(final byte[] pref, final boolean dn, final InputInfo ii)
      throws QueryException {

    final byte[] uri = uri(pref);
    if(uri.length == 0 && pref.length != 0) {
      (dn ? INVPREF : PREFUNKNOWN).thrw(ii, pref);
    }
    return uri;
  }

  /**
   * Finds the local URI for the specified prefix.
   * @param pref prefix of the namespace
   * @return uri or {@code null}
   */
  public byte[] localURI(final byte[] pref) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ns.key(s), pref)) return ns.val(s);
    }
    return null;
  }

  /**
   * Finds the URI for the specified prefix.
   * @param pref prefix of the namespace
   * @return uri or {@code null}
   */
  public byte[] uri(final byte[] pref) {
    final byte[] uri = localURI(pref);
    return uri == null ? NSGlobal.uri(pref) : uri;
  }


  /**
   * Returns the prefix for the specified URI.
   * @param uri namespace URI
   * @return prefix
   */
  public byte[] prefix(final byte[] uri) {
    for(int s = ns.size() - 1; s >= 0; s--) {
      if(eq(ns.val(s), uri)) return ns.key(s);
    }
    return NSGlobal.prefix(uri);
  }

  /**
   * Returns all prefixes.
   * @return prefixes
   */
  public byte[][] prefixes() {
    final byte[][] prefs = new byte[ns.size()][];
    for(int p = 0; p < prefs.length; ++p) prefs[p] = ns.key(p);
    return prefs;
  }

  /**
   * Finishes the creation of default namespaces.
   * @param elem default element namespace
   */
  public void finish(final byte[] elem) {
    if(elem.length != 0) ns.add(EMPTY, elem);
    def = ns.size();
  }

  /**
   * Creates a copy with the default namespaces.
   * @return copy
   */
  public NSLocal copy() {
    final NSLocal local = new NSLocal();
    for(int i = 0; i < def; ++i) local.ns.add(ns.key(i), ns.val(i));
    return local;
  }

  /**
   * Returns the number of namespaces.
   * @return namespaces
   */
  public int size() {
    return ns.size();
  }

  /**
   * Sets the number of namespaces.
   * @param s namespaces
   */
  public void size(final int s) {
    ns.size(s);
  }
}
