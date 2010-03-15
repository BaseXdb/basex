package org.basex.test.query;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.basex.core.Context;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Nod;
import org.w3c.dom.Node;

/**
 * DOM Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DOMTest {
  /** Database Context. */
  private final Context context = new Context();

  /**
   * Main method of the test class.
   * @param args command-line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new DOMTest();
  }

  /**
   * Constructor.
   * @throws Exception exception
   */
  private DOMTest() throws Exception {
    compare("<c xmlns:c='x' c:a='x'/>");
  }

  /**
   * Compares the DOM implementation results from BaseX and Java.
   * @param in input
   * @throws Exception exception
   */
  private void compare(final String in) throws Exception {
    Node bx = query(in).java().getAttributes().item(0);
    Node jv = dom(in).getChildNodes().item(0).getAttributes().item(0);

    System.out.println(bx.getNamespaceURI());
    System.out.println(jv.getNamespaceURI());
  }

  /**
   * Performs the specified query and returns the first result node.
   * @param qu query
   * @return first node
   * @throws Exception exception
   */
  private Nod query(final String qu) throws Exception {
    QueryProcessor proc = new QueryProcessor(qu, context);
    return (Nod) proc.iter().next();
  }

  /**
   * Converts the specified string and returns a DOM structure.
   * @param in input
   * @return DOM structure
   * @throws Exception exception
   */
  private Node dom(final String in) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes());
    return builder.parse(bais);
  }
}
