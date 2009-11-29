package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.item.Uri;
import org.basex.util.Atts;

/**
 * Local namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class NSLocal {
  /** Namespaces. */
  public Atts ns = new Atts();
  /** Number of default namespaces. */
  private int def;

  /**
   * Adds the specified namespace.
   * @param name namespace
   * @throws QueryException query exception
   */
  public void add(final QNm name) throws QueryException {
    final byte[] ln = name.ln();
    if(eq(ln, XML) || eq(ln, XMLNS)) Err.or(NSDEF, name);
    final byte[] uri = name.uri.str();
    if(eq(XMLURI, uri)) Err.or(NOXMLNS, name);
    ns.add(ln, uri);
  }

  /**
   * Deletes the specified namespace.
   * @param name namespace
   */
  public void delete(final QNm name) {
    final byte[] ln = name.ln();
    for(int s = ns.size - 1; s >= 0; s--) {
      if(eq(ln, ns.key[s])) ns.delete(s);
    }
  }

  /**
   * Assigns a uri to the specified QName.
   * @param qname qname
   */
  public void uri(final QNm qname) {
    final byte[] pre = qname.pref();
    if(pre.length == 0) return;
    final byte[] uri = find(pre);
    qname.uri = Uri.uri(uri != null ? uri : NSGlobal.uri(pre));
  }

  /**
   * Finds the uri for the specified prefix in the local and global namespaces.
   * @param pre prefix of the namespace
   * @param dn dynamic error
   * @return uri
   * @throws QueryException query exception
   */
  public byte[] uri(final byte[] pre, final boolean dn) throws QueryException {
    byte[] uri = find(pre);
    if(uri == null) uri = NSGlobal.uri(pre);
    if(uri.length == 0 && pre.length != 0) {
      Err.or(dn ? INVAL : PREUNKNOWN, pre);
    }
    return uri;
  }

  /**
   * Finds the URI for the specified prefix.
   * @param pre prefix of the namespace
   * @return uri or null value
   */
  public byte[] find(final byte[] pre) {
    for(int s = ns.size - 1; s >= 0; s--) {
      if(eq(ns.key[s], pre)) return ns.val[s];
    }
    return null;
  }

  /**
   * Finds the specified URI and returns the prefix.
   * @param uri URI
   * @return prefix
   */
  public byte[] prefix(final byte[] uri) {
    for(int s = ns.size - 1; s >= 0; s--) {
      if(eq(ns.val[s], uri)) return ns.key[s];
    }
    return NSGlobal.prefix(uri);
  }

  /**
   * Finishes the creation of default namespaces.
   * @param elem default element namespace
   */
  public void finish(final byte[] elem) {
    if(elem.length != 0) ns.add(EMPTY, elem);
    def = ns.size;
  }

  /**
   * Creates a copy with the default namespaces.
   * @return copy
   */
  public NSLocal copy() {
    final NSLocal local = new NSLocal();
    for(int i = 0; i < def; i++) local.ns.add(ns.key[i], ns.val[i]);
    return local;
  }

  /**
   * Returns the number of namespaces.
   * @return namespaces
   */
  public int size() {
    return ns.size;
  }

  /**
   * Sets the number of namespaces.
   * @param s namespaces
   */
  public void size(final int s) {
    ns.size = s;
  }
}
