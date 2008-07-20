package org.basex.data;

import static org.basex.data.DataText.*;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.util.Token;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class allows to output XML results via SAX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SAXSerializer extends Serializer implements XMLReader {
  /** Content handler reference. */
  private ContentHandler handler;
  /** Content handler reference. */
  private final Result result;

  /**
   * Constructor.
   * @param res result
   */
  public SAXSerializer(final Result res) {
    result = res;
    xml = true;
  }

  /* Implements XMLReader method. */
  public ContentHandler getContentHandler() {
    return handler;
  }

  /* Implements XMLReader method. */
  public DTDHandler getDTDHandler() {
    return null;
  }

  /* Implements XMLReader method. */
  public EntityResolver getEntityResolver() {
    return null;
  }

  /* Implements XMLReader method. */
  public ErrorHandler getErrorHandler() {
    return null;
  }

  /* Implements XMLReader method. */
  public boolean getFeature(final String name) {
    return false;
  }

  /* Implements XMLReader method. */
  public Object getProperty(final String name) {
    return null;
  }

  /* Implements XMLReader method. */
  public void parse(final InputSource input) throws SAXException {
    parse("");
  }

  /* Implements XMLReader method. */
  public void parse(final String id) throws SAXException {
    try {
      // execute query
      handler.startDocument();
      result.serialize(this);
      handler.endDocument();
    } catch(final Exception ex) {
      throw new SAXException(ex);
    }
  }

  /* Implements XMLReader method. */
  public void setContentHandler(final ContentHandler h) {
    handler = h;
  }

  /* Implements XMLReader method. */
  public void setDTDHandler(final DTDHandler h) {
    BaseX.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setEntityResolver(final EntityResolver resolver) {
    BaseX.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setErrorHandler(final ErrorHandler h) {
    BaseX.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setFeature(final String name, final boolean value) {
    BaseX.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setProperty(final String name, final Object value) {
    BaseX.notimplemented();
  }
  
  /** Caches a tag name. */
  private String tag;
  /** Caches attributes. */
  private AttributesImpl atts;

  @Override
  public void open(final int s) throws IOException {
    startElement(RESULTS);
    if(s == 0) emptyElement();
    else finishElement();
  }

  @Override
  public void close(final int s) throws IOException {
    if(s != 0) closeElement(RESULTS);
  }

  @Override
  public void openResult() throws IOException {
    openElement(RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    closeElement(RESULT);
  }
  
  @Override
  public void startElement(final byte[] t) {
    tag = Token.string(t);
    atts = new AttributesImpl();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) {
    atts.addAttribute("", "", Token.string(n), "", Token.string(v));
  }

  @Override
  public void emptyElement() throws IOException {
    finishElement();
    try {
      handler.endElement("", "", tag);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void finishElement() throws IOException {
    try {
      handler.startElement("", "", tag, atts);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void closeElement(final byte[] t) throws IOException {
    try {
      handler.endElement("", "", Token.string(t));
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void text(final byte[] b) throws IOException {
    final char[] c = Token.string(b).toCharArray();
    try {
      handler.characters(c, 0, c.length);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void comment(final byte[] t) throws IOException {
    throw new IOException("Can't serialize comments.");
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    try {
      handler.processingInstruction(Token.string(n), Token.string(v));
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void item(final byte[] b) throws IOException {
    throw new IOException("Can't serialize atomic items.");
  }
}
