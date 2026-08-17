package org.basex.util;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Contains helper functions for retrieving XML contents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLAccess {
  /** Private constructor. */
  private XMLAccess() { }

  /**
   * Returns the root element of an XML file.
   * @param file file
   * @param name name of root element
   * @return root element, or {@code null} if the file does not exist or could not be parsed
   */
  public static XNode root(final IOFile file, final QNm name) {
    if(!file.exists()) return null;
    try {
      final MainOptions options = new MainOptions(false);
      options.set(MainOptions.INTPARSE, true);
      options.set(MainOptions.STRIPWS, true);
      final XNode doc = new DBNode(Parser.singleParser(file, options, ""));
      if(children(doc, name).next() instanceof final XNode root) return root;
      Util.errln("%: No <%/> root element.", file, name);
    } catch(final IOException ex) {
      Util.errln("%: %", file, ex);
    }
    return null;
  }

  /**
   * Writes an XML file.
   * @param file file
   * @param node root element
   * @throws IOException I/O exception
   */
  public static void write(final IOFile file, final FNode node) throws IOException {
    file.parent().md();
    file.write(node.serialize(SerializerMode.INDENT.get()).finish());
  }

  /**
   * Returns child elements.
   * @param node root node
   * @return iterator
   */
  public static BasicNodeIter children(final XNode node) {
    return children(node, null);
  }

  /**
   * Returns child elements.
   * @param node root node
   * @param name element name (can be {@code null})
   * @return iterator
   */
  public static BasicNodeIter children(final XNode node, final QNm name) {
    final BasicNodeIter children = node.childIter();
    return new BasicNodeIter() {
      @Override
      public GNode next() {
        for(GNode child; (child = children.next()) != null;) {
          if(child.kind() == Kind.ELEMENT && (name == null || name.eq(child.qname())))
            return child;
        }
        return null;
      }
    };
  }

  /**
   * Returns the value of the requested attribute, or an error.
   * @param node node
   * @param name attribute name
   * @param info element info
   * @return value
   * @throws BaseXException database exception
   */
  public static byte[] attribute(final XNode node, final QNm name, final String info)
      throws BaseXException {

    final byte[] value = node.attribute(name);
    if(value != null) return value;
    throw new BaseXException("%: Missing \"%\" attribute.", info, name);
  }

  /**
   * Returns an enum instance for the requested attribute.
   * @param prefix error prefix
   * @param node node
   * @param name attribute name
   * @param values expected names
   * @param <E> token type
   * @return enum
   * @throws BaseXException database exception
   */
  public static <E extends Enum<E>> E attribute(final String prefix, final XNode node,
      final QNm name, final E[] values) throws BaseXException {
    return value(prefix, attribute(node, name, prefix), values);
  }

  /**
   * Returns an enum instance for the requested value.
   * @param prefix error prefix
   * @param name name
   * @param names allowed names
   * @param <E> token type
   * @return enum
   * @throws BaseXException database exception
   */
  public static <E extends Enum<E>> E value(final String prefix, final byte[] name, final E[] names)
      throws BaseXException {

    final String n = string(name);
    for(final E nm : names) {
      if(nm.toString().equals(n)) return nm;
    }
    throw new BaseXException("%: Unexpected element: \"%\".", prefix, name);
  }
}
