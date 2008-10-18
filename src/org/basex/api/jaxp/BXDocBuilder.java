package org.basex.api.jaxp;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.basex.BaseX;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXDomImpl;
import org.basex.build.MemBuilder;
import org.basex.build.xml.SAXWrapper;
import org.basex.data.Data;
import org.basex.query.xquery.item.DNode;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class provides a document builder.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXDocBuilder extends DocumentBuilder {
  /** Parser instance. */
  private SAXParserFactory factory;
  /** Parser instance. */
  private XMLReader parser;

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
    BaseX.notimplemented();
    return null;
  }

  @Override
  public Document parse(InputSource is) throws IOException {
    final SAXSource source = new SAXSource(parser, is);
    final String id = is.getSystemId();;
    final Data data = new MemBuilder().build(
        new SAXWrapper(source), id == null ? "tmp" : id);
    return new BXDoc(new DNode(data, 0));
  }

  @Override
  public void setEntityResolver(EntityResolver er) {
    parser.setEntityResolver(er);
  }

  @Override
  public void setErrorHandler(ErrorHandler eh) {
    parser.setErrorHandler(eh);
  }
}
