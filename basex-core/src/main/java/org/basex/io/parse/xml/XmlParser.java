package org.basex.io.parse.xml;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Standard XML parser.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XmlParser {
  /** Reader. */
  private final XMLReader reader;

  /**
   * Constructor.
   * @throws ParserConfigurationException parser configuration
   * @throws SAXException SAX exception
   */
  public XmlParser() throws SAXException, ParserConfigurationException {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);
    reader = factory.newSAXParser().getXMLReader();
  }

  /**
   * Sets a content handler.
   * @param handler content handler
   * @return self reference
   */
  public XmlParser contentHandler(final ContentHandler handler) {
    reader.setContentHandler(handler);
    return this;
  }

  /**
   * Sets a content handler.
   * @param stream input stream
   * @throws IOException I/O exception
   * @throws SAXException SAX exception
   */
  public void parse(final InputStream stream) throws IOException, SAXException {
    reader.setErrorHandler(new ErrorHandler());
    reader.parse(new InputSource(stream));
  }

  /** Error handler (causing no STDERR output). */
  private static class ErrorHandler extends DefaultHandler {
    @Override
    public void fatalError(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void error(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void warning(final SAXParseException ex) throws SAXParseException { throw ex; }
  }
}
