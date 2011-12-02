package org.basex.build.xml;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Stack;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.util.Util;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * This class converts an DOM document instance to a database representation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DOMWrapper extends Parser {
  /** Name of the document. */
  private final String filename;
  /** Root document. */
  private final Node root;
  /** Element counter. */
  private int nodes;

  /**
   * Constructor.
   * @param doc document instance
   * @param fn filename
   */
  public DOMWrapper(final Document doc, final String fn) {
    super(fn);
    root = doc;
    filename = fn;
  }

  @Override
  public void parse(final Builder builder) throws IOException {
    builder.startDoc(token(filename));

    final Stack<NodeIterator> stack = new Stack<NodeIterator>();
    stack.push(new NodeIterator(root));

    while(!stack.empty()) {
      final NodeIterator ni = stack.peek();
      if(ni.more()) {
        final Node n = ni.curr();
        if(n instanceof Element) {
          stack.push(new NodeIterator(n));

          atts.reset();
          final NamedNodeMap at = n.getAttributes();
          for(int a = 0, as = at.getLength(); a < as; ++a) {
            final Attr att = (Attr) at.item(a);
            final byte[] k = token(att.getName()), v = token(att.getValue());
            if(eq(k, XMLNS)) {
              builder.startNS(EMPTY, v);
            } else if(startsWith(k, XMLNSC)) {
              builder.startNS(local(k), v);
            } else {
              atts.add(k, v);
            }
          }
          builder.startElem(token(n.getNodeName()), atts);
        } else if(n instanceof Text) {
          builder.text(token(n.getNodeValue()));
        } else if(n instanceof Comment) {
          builder.comment(token(n.getNodeValue()));
        } else if(n instanceof ProcessingInstruction) {
          builder.pi(token(n.getNodeName() + ' ' + n.getNodeValue()));
        }
        ++nodes;
      } else {
        stack.pop();
        if(stack.empty()) break;
        builder.endElem();
      }
    }
    builder.endDoc();
  }

  @Override
  public String det() {
    return Util.info(NODESPARSED, filename, nodes);
  }

  @Override
  public double prog() {
    return nodes / 1000000d % 1;
  }

  /** Node iterator. */
  private static final class NodeIterator {
    /** Node list. */
    private final NodeList nl;
    /** Position. */
    private int i = -1;

    /**
     * Constructor.
     * @param n input node
     */
    NodeIterator(final Node n) {
      nl = n.getChildNodes();
    }

    /**
     * Checks if more nodes are found.
     * @return result of check
     */
    boolean more() {
      return ++i < nl.getLength();
    }
    /**
     * Returns the current node.
     * @return current node
     */
    Node curr() {
      return nl.item(i);
    }
  }
}
