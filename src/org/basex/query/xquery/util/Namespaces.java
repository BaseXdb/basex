package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Array;

/**
 * Namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Default namespaces. */
  public static final NSIndex DEFAULT = NSIndex.get();
  /** Namespaces. */
  public QNm[] names = new QNm[1];
  /** Number of stored namespaces. */
  public int size;

  /**
   * Indexes the specified namespace.
   * @param name namespace
   * @return true if namespaces was added
   * @throws XQException evaluation exception
   */
  public boolean index(final QNm name) throws XQException {
    final byte[] ln = name.ln();
    final boolean del = name.uri.str().length == 0;
    if(eq(ln, XML) || eq(ln, XMLNS)) Err.or(NSDEF, name);
    if(name.uri.eq(Uri.XML)) Err.or(NOXMLNS, name);

    for(int s = 0; s < size; s++) {
      if(eq(ln, names[s].ln())) {
        if(del) Array.move(names, s + 1, -1, --size - s);
        return del;
      }
    }

    if(size == names.length) names = Array.extend(names);
    names[size++] = new QNm(ln, name.uri);
    return true;
  }

  /**
   * Assigns a uri to the specified QName.
   * @param qname qname
   */
  public void uri(final QNm qname) {
    if(!qname.ns()) return;
    final byte[] pre = qname.pre();
    final Uri uri = find(pre);
    qname.uri = uri != null ? uri : DEFAULT.uri(pre);
  }

  /**
   * Finds the specified namespace uri.
   * @param pre prefix of the namespace
   * @return uri
   * @throws XQException evaluation exception
   */
  public Uri uri(final byte[] pre) throws XQException {
    // [CG] XQuery/uri; add recursive namespace handling
    Uri uri = find(pre);
    if(uri == null) uri = DEFAULT.uri(pre);
    if(uri == Uri.EMPTY) Err.or(PREUNKNOWN, pre);
    return uri;
  }

  /**
   * Finds the uri for the specified prefix.
   * @param pre prefix of the namespace
   * @return uri
   */
  public Uri find(final byte[] pre) {
    for(int s = size - 1; s >= 0; s--) {
      if(eq(pre, names[s].str())) return names[s].uri;
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
      if(uri.eq(names[s].uri)) return names[s].str();
    }
    return DEFAULT.prefix(uri);
  }

  /**
   * Checks if the specified uri is predefined.
   * @param uri uri to be checked
   * @return result of check
   */
  public boolean standard(final Uri uri) {
    return DEFAULT.standard(uri);
  }
}
