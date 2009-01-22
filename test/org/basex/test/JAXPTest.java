package org.basex.test;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.basex.api.dom.BXDoc;
import org.basex.api.jaxp.BXPath;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.util.Performance;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This class serves an example for executing XPath requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class JAXPTest {
  /** Input XML file. */
  private static final String XMLFILE = "input.xml";
  /** Sample query. */
  private static final String QUERY = "//li";

  /** Private constructor. */
  private JAXPTest() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   */
  public static void main(final String[] args) {
    eval(new BXPath());
    eval(XPathFactory.newInstance().newXPath());
  }
  
  /**
   * Evaluates a query with the specified XPath implementation.
   * @param xpath xpath implementation
   */
  private static void eval(final XPath xpath) {
    Performance p = new Performance();
    QName type = XPathConstants.NODESET;

    try {
      InputSource is = new InputSource(XMLFILE);
      Object o = xpath.evaluate(QUERY, is, type);
      System.out.println(o + (o != null ? ": " + o.getClass() : ""));
      System.out.println(p.getTimer());
    } catch(Exception e) {
      e.printStackTrace();
    }
    try {
      final Data d = Check.check(XMLFILE);
      final Node n = new BXDoc(new DBNode(d, 0));
      final Object o = xpath.evaluate(QUERY, n, type);
      System.out.println(o + (o != null ? ": " + o.getClass() : ""));
      System.out.println(p.getTimer());
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}

