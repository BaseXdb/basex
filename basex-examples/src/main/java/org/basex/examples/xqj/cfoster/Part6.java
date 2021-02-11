package org.basex.examples.xqj.cfoster;

import java.io.*;

import javax.xml.stream.*;
import javax.xml.xquery.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 6: Streaming XQuery results.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Part6 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("6: Streaming XQuery results");

    // Create the connection
    XQConnection conn = connect();

    // Stream XQuery Result Sequences with StAX
    info("Stream XQuery Result Sequences with StAX");

    XQExpression xqe = conn.createExpression();

    String path = new File("src/main/resources/xml").getAbsolutePath();
    XQResultSequence rs = xqe.executeQuery(
        "doc('" + path + "/books.xml')//book");

    XMLStreamReader reader = rs.getSequenceAsStream();

    for(; reader.hasNext(); reader.next())
      System.out.println(getEventString(reader));

    // Print tailing END_DOCUMENT event too
    System.out.println(getEventString(reader));

    // Streaming XQuery Result Sequences with SAX
    info("Stream XQuery Result Sequences with SAX");

    rs = xqe.executeQuery("doc('" + path + "/books.xml')//book");
    rs.writeSequenceToSAX(new MySAXHandler());

    // Closing connection to the Database.
    close(conn);
  }

  /**
   * Returns the correspondent event string.
   * @param reader stream reader
   * @return event string
   */
  static String getEventString(final XMLStreamReader reader) {
    switch(reader.getEventType()) {
      case XMLStreamConstants.START_ELEMENT:
        return "START_ELEMENT:\t\"" + reader.getLocalName() + '"';
      case XMLStreamConstants.END_ELEMENT:
        return "END_ELEMENT:\t\"" + reader.getLocalName() + '"';
      case XMLStreamConstants.START_DOCUMENT:
        return "START_DOCUMENT";
      case XMLStreamConstants.END_DOCUMENT:
        return "END_DOCUMENT";
      case XMLStreamConstants.CHARACTERS:
        return "CHARACTERS:\t\"" + reader.getText() + '"';
    }
    return "";
  }

  /**
   * SAX Handler.
   */
  static class MySAXHandler extends DefaultHandler {
    @Override
    public void startDocument() {
      System.out.println("Start Document");
    }

    @Override
    public void startElement(final String u, final String name, final String qn,
        final Attributes a) {
      System.out.println("Start Element:\t\"" + name + '"');
    }

    @Override
    public void endElement(final String u, final String name, final String qn) {
      System.out.println("End Element:\t\"" + name + '"');
    }

    @Override
    public void characters(final char[] ch, final int start, final int len) {
      System.out.println("Characters:\t\"" + new String(ch, start, len) + '"');
    }

    @Override
    public void endDocument() {
      System.out.println("End Document");
    }
  }
}
