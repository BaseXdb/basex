package org.basex.test.examples;

import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
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
    // create database context
    final Context ctx = new Context();

    // create a database
    new CreateDB(XMLFILE).execute(ctx, null);

    // create query instance
    final QueryProcessor query = new QueryProcessor(QUERY, ctx.current());
    // execute query
    final Result result = query.query();

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
    System.out.print("Start Element: " + qn);
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn) {
    System.out.print("End Element: " + qn);
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    System.out.print("Characters: " + new String(ch, s, l));
  }
}
