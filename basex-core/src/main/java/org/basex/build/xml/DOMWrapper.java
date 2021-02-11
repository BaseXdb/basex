package org.basex.build.xml;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.util.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * This class converts an DOM document instance to a database representation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DOMWrapper extends Parser {
  /** Strip namespaces. */
  private final boolean stripNS;
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
   * @param opts database options
   */
  public DOMWrapper(final Document doc, final String fn, final MainOptions opts) {
    super(fn, opts);
    root = doc;
    filename = fn;
    stripNS = opts.get(MainOptions.STRIPNS);
  }

  @Override
  public void parse(final Builder builder) throws IOException {
    builder.openDoc(token(filename));

    final Stack<NodeIterator> stack = new Stack<>();
    stack.push(new NodeIterator(root));

    while(!stack.empty()) {
      final NodeIterator ni = stack.peek();
      if(ni.more()) {
        final Node n = ni.curr();
        if(n instanceof Element) {
          stack.push(new NodeIterator(n));

          atts.reset();
          nsp.reset();
          final NamedNodeMap at = n.getAttributes();
          final int as = at.getLength();
          for(int a = 0; a < as; ++a) {
            final Attr att = (Attr) at.item(a);
            final byte[] an = token(att.getName());
            final byte[] av = token(att.getValue());
            if(eq(an, XMLNS)) {
              if(!stripNS) nsp.add(EMPTY, av);
            } else if(startsWith(an, XMLNS_COLON)) {
              if(!stripNS) nsp.add(local(an), av);
            } else {
              atts.add(stripNS ? local(an) : an, av);
            }
          }
          final byte[] en = token(n.getNodeName());
          builder.openElem(stripNS ? local(en) : en, atts, nsp);
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
        builder.closeElem();
      }
    }
    builder.closeDoc();
  }

  @Override
  public String detailedInfo() {
    return Util.info(NODES_PARSED_X_X, filename, nodes);
  }

  @Override
  public double progressInfo() {
    return nodes / 1000000.0d % 1;
  }

  /** Node iterator. */
  private static final class NodeIterator {
    /** Node list. */
    private final NodeList nl;
    /** Position. */
    private int i = -1;

    /**
     * Constructor.
     * @param node input node
     */
    NodeIterator(final Node node) {
      nl = node.getChildNodes();
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
