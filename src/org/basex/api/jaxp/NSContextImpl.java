package org.basex.api.jaxp;

import static org.basex.util.Token.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.basex.query.xquery.util.NSLocal;

/**
 * This class provides access to namespace context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NSContextImpl implements NamespaceContext {
  /** Namespaces references. */
  private final NSLocal ns;

  /**
   * Constructor.
   * @param n namespace references.
   */
  public NSContextImpl(final NSLocal n) {
    ns = n;
  }

  public String getNamespaceURI(final String pre) {
    final byte[] uri = ns.find(token(pre));
    return uri == null ? null : string(uri);
  }

  public String getPrefix(final String uri) {
    return string(ns.prefix(token(uri)));
  }

  public Iterator<String> getPrefixes(final String uri) {
    final ArrayList<String> list = new ArrayList<String>();
    final String pre = getPrefix(uri);
    if(pre.length() != 0) list.add(pre);
    return list.iterator();
  }
}
