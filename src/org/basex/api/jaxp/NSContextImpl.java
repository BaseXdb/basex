package org.basex.api.jaxp;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.util.Namespaces;
import org.basex.util.Token;

/**
 * This class provides access to namespace context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class NSContextImpl implements NamespaceContext {
  /** Namespaces references. */
  private final Namespaces ns;

  /**
   * Constructor.
   * @param n namespace references.
   */
  public NSContextImpl(final Namespaces n) {
    ns = n;
  }
  
  public String getNamespaceURI(final String pre) {
    final Uri uri = ns.find(Token.token(pre));
    return uri == null ? null : Token.string(uri.str());
  }

  public String getPrefix(final String uri) {
    return Token.string(ns.prefix(Uri.uri(Token.token(uri))));
  }

  public Iterator getPrefixes(final String uri) {
    final ArrayList<String> list = new ArrayList<String>();
    final String pre = Token.string(ns.prefix(Uri.uri(Token.token(uri))));
    if(pre.length() != 0) list.add(pre);
    return list.iterator();
  }
}
