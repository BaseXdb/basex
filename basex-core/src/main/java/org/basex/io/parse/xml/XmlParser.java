package org.basex.io.parse.xml;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Standard XML parser.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XmlParser {
  /** Reader. */
  private final XMLReader reader;

  /**
   * Constructor.
   * @throws SAXException SAX exception
   * @throws ParserConfigurationException parser configuration exception
   */
  public XmlParser() throws SAXException, ParserConfigurationException {
    reader = reader(false, false);
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
    reader.setErrorHandler(new XmlHandler());
    reader.parse(new InputSource(stream));
  }

  /**
   * Returns an XML reader.
   * @param dtd parse DTDs
   * @param xinclude enable XInclude
   * @throws SAXException SAX exception
   * @throws ParserConfigurationException parser configuration exception
   * @return reader
   */
  public static XMLReader reader(final boolean dtd, final boolean xinclude)
      throws SAXException, ParserConfigurationException {

    final SAXParserFactory f = SAXParserFactory.newInstance();
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", dtd);
    f.setFeature("http://xml.org/sax/features/external-parameter-entities", dtd);
    f.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);
    f.setNamespaceAware(true);
    f.setValidating(false);
    f.setXIncludeAware(xinclude);
    return f.newSAXParser().getXMLReader();
  }

  /** Error handler (causing no STDERR output). */
  private static class XmlHandler extends DefaultHandler {
    @Override
    public void fatalError(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void error(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void warning(final SAXParseException ex) throws SAXParseException { throw ex; }
  }
}
