package org.basex.api.jaxp;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXDomImpl;
import org.basex.build.MemBuilder;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.util.Util;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class provides a document builder.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXDocBuilder extends DocumentBuilder {
  /** Database context. */
  private final Context ctx = new Context();
  /** Parser instance. */
  private final SAXParserFactory factory;
  /** Parser instance. */
  private final XMLReader parser;

  /**
   * Constructor.
   * @throws SAXException exception
   * @throws ParserConfigurationException exception
   */
  BXDocBuilder() throws SAXException, ParserConfigurationException {
    factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    parser = factory.newSAXParser().getXMLReader();
  }

  @Override
  public DOMImplementation getDOMImplementation() {
    return BXDomImpl.get();
  }

  @Override
  public boolean isNamespaceAware() {
    return factory.isNamespaceAware();
  }

  @Override
  public boolean isValidating() {
    return factory.isValidating();
  }

  @Override
  public Document newDocument() {
    Util.notimplemented();
    return null;
  }

  @Override
  public Document parse(final InputSource is) throws IOException {
    final String id = is.getSystemId();
    final SAXSource ss = new SAXSource(parser, is);
    final SAXWrapper sw = new SAXWrapper(ss, ctx.prop);
    final Data data = MemBuilder.build(id == null ? "" : id, sw, ctx.prop);
    return new BXDoc(new DBNode(data));
  }

  @Override
  public void setEntityResolver(final EntityResolver er) {
    parser.setEntityResolver(er);
  }

  @Override
  public void setErrorHandler(final ErrorHandler eh) {
    parser.setErrorHandler(eh);
  }
}
