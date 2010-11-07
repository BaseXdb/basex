package org.basex.api.jaxp;

import static org.basex.util.Token.*;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.basex.query.util.Namespaces;
import org.basex.util.StringList;

/**
 * This class provides access to namespace context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXNamespaceContext implements NamespaceContext {
  /** Namespaces references. */
  private final Namespaces ns;

  /**
   * Constructor.
   * @param n namespace references
   */
  public BXNamespaceContext(final Namespaces n) {
    ns = n;
  }

  @Override
  public String getNamespaceURI(final String pre) {
    final byte[] uri = ns.find(token(pre));
    return uri == null ? null : string(uri);
  }

  @Override
  public String getPrefix(final String uri) {
    return string(ns.prefix(token(uri)));
  }

  @Override
  public Iterator<String> getPrefixes(final String uri) {
    final StringList list = new StringList();
    final String pre = getPrefix(uri);
    if(!pre.isEmpty()) list.add(pre);
    return list.iterator();
  }
}
