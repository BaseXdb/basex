package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.data.Result;
import org.basex.query.item.Item;
import org.basex.util.Util;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class serializes data via SAX.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SAXSerializer extends Serializer implements XMLReader {
  /** Content handler reference. */
  private ContentHandler content;
  /** Lexical handler reference. */
  private LexicalHandler lexical;
  /** Content handler reference. */
  private final Result result;
  /** Caches attributes. */
  private AttributesImpl atts;

  /**
   * Constructor.
   * @param res result
   */
  public SAXSerializer(final Result res) {
    result = res;
  }

  @Override
  public ContentHandler getContentHandler() {
    return content;
  }

  @Override
  public DTDHandler getDTDHandler() {
    return null;
  }

  @Override
  public EntityResolver getEntityResolver() {
    return null;
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return null;
  }

  @Override
  public boolean getFeature(final String name) {
    return false;
  }

  @Override
  public Object getProperty(final String name) {
    return null;
  }

  @Override
  public void parse(final InputSource input) throws SAXException {
    parse("");
  }

  @Override
  public void parse(final String id) throws SAXException {
    try {
      // execute query
      content.startDocument();
      result.serialize(this);
      content.endDocument();
    } catch(final Exception ex) {
      throw new SAXException(ex);
    }
  }

  @Override
  public void setContentHandler(final ContentHandler c) {
    content = c;
  }

  /**
   * Sets the lexical handler for reacting on comments.
   * @param l handler
   */
  public void setLexicalHandler(final LexicalHandler l) {
    lexical = l;
  }

  @Override
  public void setDTDHandler(final DTDHandler h) {
    throw Util.notimplemented();
  }

  @Override
  public void setEntityResolver(final EntityResolver resolver) {
    throw Util.notimplemented();
  }

  @Override
  public void setErrorHandler(final ErrorHandler h) {
    throw Util.notimplemented();
  }

  @Override
  public void setFeature(final String name, final boolean value) {
    throw Util.notimplemented();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    throw Util.notimplemented();
  }

  @Override
  public void openResult() throws IOException {
    openElement(T_RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    closeElement();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) {
    final String an = string(n);
    atts.addAttribute("", an, an, "", string(v));
  }

  @Override
  protected void startOpen(final byte[] t) {
    atts = new AttributesImpl();
  }

  @Override
  protected void finishOpen() throws IOException {
    try {
      content.startElement("", string(tag), string(tag), atts);
    } catch(final SAXException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    try {
      content.endElement("", string(tag), string(tag));
    } catch(final SAXException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void finishText(final byte[] b) throws IOException {
    final char[] c = string(b).toCharArray();
    try {
      content.characters(c, 0, c.length);
    } catch(final SAXException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void finishComment(final byte[] t) throws IOException {
    try {
      final char[] c = string(t).toCharArray();
      if(lexical != null) lexical.comment(c, 0, t.length);
    } catch(final SAXException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void finishPi(final byte[] n, final byte[] v) throws IOException {
    try {
      content.processingInstruction(string(n), string(v));
    } catch(final SAXException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void finishAtomic(final Item b) throws IOException {
    throw new BaseXException("Atomic values cannot be serialized");
  }
}
