package org.basex.test.examples;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.basex.api.dom.NodeImpl;
import org.basex.api.jaxp.XPathImpl;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.util.Performance;
import org.xml.sax.InputSource;

/**
 * This class serves an example for executing XPath requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class JAXPExample {
  /** Input XML file. */
  private static final String XMLFILE = "g:/media/xml/xmark/11MB.xml";
  /** Sample query. */
  private static final String QUERY = "//item[@id != 'item0']";

  /** Private constructor. */
  private JAXPExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    Prop.read();
    eval(new XPathImpl());
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
      Data d = Check.check(XMLFILE);
      Object o = xpath.evaluate(QUERY, NodeImpl.get(d, 0), type);
      System.out.println(o + (o != null ? ": " + o.getClass() : ""));
      System.out.println(p.getTimer());
    } catch(Exception e) {
      e.printStackTrace();
    }
    
  }
}

