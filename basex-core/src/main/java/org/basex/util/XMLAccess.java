package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Contains helper functions for retrieving XML contents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XMLAccess {
  /** Private constructor. */
  private XMLAccess() { }

  /**
   * Returns child elements.
   * @param node root node
   * @return iterator
   */
  public static BasicNodeIter children(final ANode node) {
    return children(node, null);
  }

  /**
   * Returns child elements.
   * @param node root node
   * @param name element name (can be {@code null})
   * @return iterator
   */
  public static BasicNodeIter children(final ANode node, final byte[] name) {
    final BasicNodeIter children = node.childIter();
    return new BasicNodeIter() {
      @Override
      public ANode next() {
        for(ANode child; (child = children.next()) != null;) {
          if(child.type == NodeType.ELEMENT && (name == null || eq(child.qname().id(), name)))
            return child;
        }
        return null;
      }
    };
  }

  /**
   * Returns the value of the requested attribute, or an error.
   * @param pref error prefix
   * @param node node
   * @param name attribute name
   * @return value
   * @throws BaseXException database exception
   */
  public static byte[] attribute(final String pref, final ANode node, final byte[] name)
      throws BaseXException {

    final byte[] value = node.attribute(name);
    if(value != null) return value;
    throw new BaseXException("%: Missing \"%\" attribute.", pref, name);
  }

  /**
   * Returns an enum instance for the requested attribute.
   * @param pref error prefix
   * @param node node
   * @param name attribute name
   * @param values expected names
   * @param <E> token type
   * @return enum
   * @throws BaseXException database exception
   */
  public static <E extends Enum<E>> E attribute(final String pref, final ANode node,
      final byte[] name, final E[] values) throws BaseXException {
    return value(pref, attribute(pref, node, name), values);
  }

  /**
   * Returns an enum instance for the requested value.
   * @param pref error prefix
   * @param name name
   * @param names allowed names
   * @param <E> token type
   * @return enum
   * @throws BaseXException database exception
   */
  public static <E extends Enum<E>> E value(final String pref, final byte[] name, final E[] names)
      throws BaseXException {

    final String n = string(name);
    for(final E nm : names) {
      if(n.equals(nm.toString())) return nm;
    }
    throw new BaseXException("%: Unexpected element: \"%\".", pref, name);
  }
}
