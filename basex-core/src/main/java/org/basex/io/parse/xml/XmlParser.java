package org.basex.io.parse.xml;

import java.io.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.validation.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Standard XML parser.
 *
 * @author BaseX Team, BSD License
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
    reader = reader(new MainOptions());
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
   * @param options XML parsing options
   * @throws SAXException SAX exception
   * @throws ParserConfigurationException parser configuration exception
   * @return reader
   */
  public static XMLReader reader(final MainOptions options)
      throws SAXException, ParserConfigurationException {

    final boolean allowExternalEntities = options.get(MainOptions.ALLOWEXTERNALENTITIES);
    final boolean dtd = options.get(MainOptions.DTD);
    final boolean dtdValidation = options.get(MainOptions.DTDVALIDATION);
    final boolean xinclude = options.get(MainOptions.XINCLUDE);
    final boolean xsdValidation = MainOptions.STRICT.equals(options.get(MainOptions.XSDVALIDATION));
    final boolean xsiSchemaLocation = options.get(MainOptions.XSISCHEMALOCATION);
    final Integer entityExpansionLimit = entityExpansionLimit(options);
    final SAXParserFactory f = SAXParserFactory.newInstance();
    if(allowExternalEntities) {
      // setting these options to false will ignore external entities, rather than rejecting them
      f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", dtd);
      f.setFeature("http://xml.org/sax/features/external-general-entities", dtd);
      f.setFeature("http://xml.org/sax/features/external-parameter-entities", dtd);
    }
    f.setFeature("http://xml.org/sax/features/validation", dtdValidation);
    f.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);
    f.setNamespaceAware(true);
    f.setValidating(dtdValidation);
    f.setXIncludeAware(xinclude);
    if(xsdValidation) {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(Resolver.resources(options));
      f.setSchema(schemaFactory.newSchema());
    }
    XMLReader xmlReader = f.newSAXParser().getXMLReader();
    if(entityExpansionLimit != null) {
      xmlReader.setProperty("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit",
          entityExpansionLimit == 0 ? -1 : entityExpansionLimit  < 0 ? 0 : entityExpansionLimit);
    }
    if(xsdValidation && !xsiSchemaLocation || !allowExternalEntities) {
      xmlReader.setEntityResolver((pubId, sysId) -> {
        throw new SAXException("External access not allowed: " + sysId);
      });
    }
    return xmlReader;
  }

  /**
   * Returns the entity expansion limit.
   * @param options main options
   * @return the entity expansion limit, or null if set to empty value
   */
  private static Integer entityExpansionLimit(final MainOptions options) {
    final Value limit = options.get(MainOptions.ENTITYEXPANSIONLIMIT);
    if(limit.isEmpty()) return null;
    try {
      return (int) limit.itemAt(0).itr(null);
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  /** Error handler (causing no STDERR output). */
  private static final class XmlHandler extends DefaultHandler {
    @Override
    public void fatalError(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void error(final SAXParseException ex) throws SAXParseException { throw ex; }
    @Override
    public void warning(final SAXParseException ex) throws SAXParseException { throw ex; }
  }
}
