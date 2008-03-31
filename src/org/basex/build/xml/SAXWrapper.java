package org.basex.build.xml;

import static org.basex.Text.*;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.util.Token;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class parses an XML document via a conventional SAX parser.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SAXWrapper extends Parser implements ContentHandler,
  ErrorHandler, LexicalHandler {

  /** Temporary string builder. */
  private final StringBuilder sb = new StringBuilder();
  /** Builder reference. */
  private Builder builder;
  /** Element counter. */
  private int nodes;

  /**
   * Constructor.
   * @param in input file
   */
  public SAXWrapper(final String in) {
    super(in);
  }

  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;
    try {
      final SAXParserFactory f = SAXParserFactory.newInstance();
      f.setValidating(true);
      final XMLReader r = f.newSAXParser().getXMLReader();
      r.setContentHandler(this);
      r.setProperty("http://xml.org/sax/properties/lexical-handler", this);
      r.setErrorHandler(this);
      r.parse(file);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      throw new IOException(ex.getMessage());
    }
  }

  // ContentHandler implementations
  
  /** {@inheritDoc} */
  public void setDocumentLocator(final Locator loc) { }

  /** {@inheritDoc} */
  public void startDocument() { }

  /** {@inheritDoc} */
  public void endDocument() { }

  /** {@inheritDoc} */
  public void startPrefixMapping(final String pre, final String uri) { }

  /** {@inheritDoc} */
  public void endPrefixMapping(final String pre) { }

  /** {@inheritDoc} */
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) {
    
    try {
      checkText();
      final int as = at.getLength();
      final byte[][] atts = new byte[as << 1][];
      for(int a = 0; a < as; a++) {
        atts[a << 1] = Token.token(at.getQName(a));
        atts[(a << 1) + 1] = Token.token(at.getValue(a));
      }
      builder.startNode(Token.token(qn), atts);
      nodes++;
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  /** {@inheritDoc} */
  public void endElement(final String uri, final String ln, final String qn) {
    try {
      checkText();
      builder.endNode(Token.token(qn));
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }
  
  /** {@inheritDoc} */
  public void characters(final char[] ch, final int s, final int l) {
    sb.append(ch, s, l);
  }

  /** {@inheritDoc} */
  public void ignorableWhitespace(final char[] ch, final int s, final int l) { }

  /** {@inheritDoc} */
  public void processingInstruction(final String name, final String val) {
    try {
      checkText();
      builder.pi(Token.token(name + ' ' + val));
      nodes++;
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  /** {@inheritDoc} */
  public void skippedEntity(final String name) { }

  // LexicalHandler implementations

  /** {@inheritDoc} */
  public void comment(final char[] ch, final int s, final int l) { 
    try {
      checkText();
      builder.comment(Token.token(new String(ch, s, l)));
      nodes++;
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  /** {@inheritDoc} */
  public void startEntity(final String name) { }

  /** {@inheritDoc} */
  public void endEntity(final String name) { }

  /** {@inheritDoc} */
  public void startCDATA() { }

  /** {@inheritDoc} */
  public void endCDATA() { }

  /** {@inheritDoc} */
  public void startDTD(final String n, final String pid, final String sid) { }

  /** {@inheritDoc} */
  public void endDTD() { }

  // ErrorHandler implementations

  /** {@inheritDoc} */
  public void warning(final SAXParseException ex) throws SAXParseException {
    throw ex;
  }

  /** {@inheritDoc} */
  public void error(final SAXParseException ex) throws SAXParseException {
    throw ex;
  }

  /** {@inheritDoc} */
  public void fatalError(final SAXParseException ex) throws SAXParseException {
    throw ex;
  }

  /**
   * Checks if a text node has to be written.
   * @throws IOException I/O exception
   */
  private void checkText() throws IOException {
    if(sb.length() == 0) return;
    final byte[] txt = Token.token(sb.toString());
    builder.text(txt, Token.ws(txt));
    sb.setLength(0);
    nodes++;
  }

  @Override
  public String head() {
    return PROGCREATE;
  }
  
  @Override
  public String det() {
    return nodes + NODESPARSED;
  }

  @Override
  public double percent() {
    return 0;
  }
}
