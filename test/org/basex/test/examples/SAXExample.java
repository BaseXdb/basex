package org.basex.test.examples;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.data.Result;
import org.basex.data.SAXSerializer;
import org.basex.query.QueryProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class serves as an example for evaluating XQuery requests
 * and serializing the results via the SAX API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class SAXExample extends DefaultHandler {
  /** Sample query. */
  private static final String QUERY = "doc('input.xml')//title";

  /**
   * Main method of the example class.
   * @param args an optional query can be specified on the command line
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
    // create database context
    Context ctx = new Context();

    // create query instance
    QueryProcessor query = new QueryProcessor(QUERY, ctx);
    // execute query
    Result result = query.query();

    // create XML reader
    XMLReader reader = new SAXSerializer(result);
    // set this class as content handler
    reader.setContentHandler(this);
    // start parser
    reader.parse("");
  }

  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) {
    Main.outln("Start Element: " + qn);
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn) {
    Main.outln("End Element: " + qn);
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    Main.outln("Characters: " + new String(ch, s, l));
  }
}
