package org.basex.test.examples;

import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.SAXSerializer;
import org.basex.query.QueryProcessor;
import org.basex.query.xquery.XQueryProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class serves as an example for evaluating XQuery requests
 * and receiving the results via the SAX API. Note that the SAX output
 * doesn't support namespaces yet.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SAXExample extends DefaultHandler {
  /** Input XML file. */
  private static final String XMLFILE = "input.xml";
  /** Sample query. */
  private static final String QUERY = "<xml>This is a test</xml>/text()";

  /**
   * Main method of the example class.
   * @param args an optional query can be specified on the command line.
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // evaluate query
    new SAXExample();
  }

  /**
   * Constructor.
   * @throws Exception exception
   */
  public SAXExample() throws Exception {
    // read properties (database path, language, ...)
    Prop.read();

    // create a database or open an existing one
    final Data data = Check.check(XMLFILE);

    // create query instance
    final QueryProcessor xpath = new XQueryProcessor(QUERY);
    // create context set, referring to the root node (0)
    final Nodes nodes = new Nodes(0, data);
    // execute query
    final Result result = xpath.query(nodes);

    // create XML reader
    final XMLReader reader = new SAXSerializer(result);
    // set this class as content handler
    reader.setContentHandler(this);
    // start parser
    reader.parse("");
  }

  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) {
    System.out.print("<" + qn);
    for(int a = 0; a < at.getLength(); a++) {
      System.out.print(" " + at.getQName(a) + "=\"" + at.getValue(a) + "\"");
    }
    System.out.print(">");
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn) {
    System.out.print("</" + qn + ">");
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    System.out.print(new String(ch, s, l));
  }
}
