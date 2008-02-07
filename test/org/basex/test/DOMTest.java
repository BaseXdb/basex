package org.basex.test;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.basex.api.jaxp.XPathImpl;
import org.basex.core.Prop;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * This class serves an example for executing XPath requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class DOMTest {
  /** Input XML file. */
  private static final String XMLFILE = "input.xml";
  /** Sample query. */
  private static final String QUERY = "descendant::text()[4]";

  /** Private constructor. */
  private DOMTest() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    Prop.read();
    System.out.println("=== BaseX DOM ===========================");
    test(eval(new XPathImpl()));
    System.out.println("\n=== Java DOM ============================");
    test(eval(XPathFactory.newInstance().newXPath()));
  }
  
  /**
   * Evaluates a query with the specified XPath implementation.
   * @param xpath xpath implementation
   * @return node
   * @throws Exception exception
   */
  private static Node eval(final XPath xpath) throws Exception {
    InputSource is = new InputSource(XMLFILE);
    return (Node) xpath.evaluate(QUERY, is, XPathConstants.NODE);
  }
  
  /**
   * Performs some DOM tests.
   * @param node node reference
   */
  private static void test(final Node node) {
    try {
      System.out.println("! " + ((Text) node).substringData(0, 8));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}

