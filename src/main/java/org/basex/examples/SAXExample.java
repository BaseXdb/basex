package org.basex.examples;

import org.basex.core.Context;
import org.basex.data.Result;
import org.basex.io.serial.SAXSerializer;
import org.basex.query.QueryProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class serves as an example for evaluating XQuery requests
 * and serializing the results via the SAX API.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class SAXExample extends DefaultHandler {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception passes on any exception
   */
  public static void main(final String[] args) throws Exception {
    new SAXExample().run();
  }

  /**
   * Runs the example code.
   * @throws Exception exception
   */
  private void run() throws Exception {

    System.out.println("=== SAXExample ===");

    // Create database context
    final Context ctx = new Context();

    // ----------------------------------------------------------------------
    // Execute the query
    final QueryProcessor proc =
      new QueryProcessor("doc('src/main/resources/xml/input.xml')//title", ctx);
    final Result result = proc.execute();

    // ----------------------------------------------------------------------
    // Create an XML reader, set the content handler and start parsing
    final XMLReader reader = new SAXSerializer(result);
    reader.setContentHandler(this);
    reader.parse("");
  }

  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) {
    // Print opening tags
    System.out.println("\n* Start Element: " + qn);
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn) {
    // Print closing tags
    System.out.println("\n* End Element: " + qn);
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    // Print text nodes
    System.out.println("\n* Characters: " + new String(ch, s, l));
  }
}
