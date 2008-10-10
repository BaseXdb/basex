package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQText.*;
import static org.basex.util.Token.*;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Array;

/**
 * Local Namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NSLocal {
  /** Namespaces. */
  private QNm[] names = new QNm[1];
  /** Number of stored namespaces. */
  public int size;

  /**
   * Adds the specified namespace.
   * @param name namespace
   * @return true if namespace is already known
   * @throws XQException evaluation exception
   */
  public boolean add(final QNm name) throws XQException {
    final byte[] ln = name.ln();
    if(eq(ln, XML) || eq(ln, XMLNS)) Err.or(NSDEF, name);
    if(Uri.XML.eq(name.uri)) Err.or(NOXMLNS, name);

    // check if namespace exists already..
    boolean n = true;
    for(int s = size - 1; s >= 0; s--) n &= !eq(ln, names[s].ln());

    names = Array.check(names, size);
    names[size++] = new QNm(ln, name.uri);
    return n;
  }

  /**
   * Deletes the specified namespace.
   * @param name namespace
   */
  public void delete(final QNm name) {
    final byte[] ln = name.ln();
    for(int s = size - 1; s >= 0; s--) {
      if(eq(ln, names[s].ln())) Array.move(names, s + 1, -1, --size - s);
    }
  }
  
  /**
   * Assigns a uri to the specified QName.
   * @param qname qname
   */
  public void uri(final QNm qname) {
    if(!qname.ns()) return;
    final byte[] pre = qname.pre();
    final Uri uri = find(pre);
    qname.uri = uri != null ? uri : NSGlobal.uri(pre);
  }

  /**
   * Finds the uri for the specified prefix in the local and global namespaces.
   * @param pre prefix of the namespace
   * @return uri
   * @throws XQException evaluation exception
   */
  public Uri uri(final byte[] pre) throws XQException {
    Uri uri = find(pre);
    if(uri == null) uri = NSGlobal.uri(pre);
    if(uri == Uri.EMPTY) Err.or(PREUNKNOWN, pre);
    return uri;
  }

  /**
   * Finds the uri for the specified prefix.
   * @param pre prefix of the namespace
   * @return uri or null value
   */
  public Uri find(final byte[] pre) {
    for(int s = size - 1; s >= 0; s--) {
      if(eq(names[s].str(), pre)) return names[s].uri;
    }
    return null;
  }

  /**
   * Finds the specified URI and returns the prefix.
   * @param uri URI
   * @return prefix
   */
  public byte[] prefix(final Uri uri) {
    for(int s = size - 1; s >= 0; s--) {
      if(names[s].uri.eq(uri)) return names[s].str();
    }
    return NSGlobal.prefix(uri);
  }

  /**
   * Returns the currently available namespaces.
   * @return namespaces
   */
  public QNm[] ns() {
    return Array.finish(names, size);
  }
}
