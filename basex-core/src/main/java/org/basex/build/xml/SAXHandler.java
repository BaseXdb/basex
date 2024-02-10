package org.basex.build.xml;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 * SAX Parser wrapper.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class SAXHandler extends DefaultHandler implements LexicalHandler {
  /** Builder reference. */
  protected final Builder builder;

  /** Strip namespaces. */
  private final boolean stripNS;
  /** Strip whitespace. */
  private final boolean stripWS;
  /** Whitespace handling. */
  private final BoolList strips = new BoolList();
  /** Temporary attribute array. */
  private final Atts atts = new Atts();
  /** Temporary string builder for high surrogates. */
  private final StringBuilder sb = new StringBuilder();
  /** Temporary namespace array. */
  private final Atts nsp = new Atts();
  /** DTD flag. */
  private boolean dtd;
  /** Element counter. */
  int nodes;

  static {
    // needed for XMLEntityManager: increase entity limit
    System.setProperty("entityExpansionLimit", "536870912");
    // needed for frequently visited sites: modify user agent
    System.setProperty("http.agent", "sax");
  }

  /**
   * Constructor.
   * @param builder builder reference
   */
  public SAXHandler(final Builder builder) {
    this(builder, false, false);
  }

  /**
   * Constructor.
   * @param builder builder reference
   * @param stripWS strip whitespace
   * @param stripNS strip namespaces
   */
  public SAXHandler(final Builder builder, final boolean stripWS, final boolean stripNS) {
    this.builder = builder;
    this.stripNS = stripNS;
    this.stripWS = stripWS;
    strips.push(stripWS);
  }

  @Override
  public void startElement(final String uri, final String local, final String name,
      final Attributes attr) throws SAXException {

    try {
      finishText();

      final int al = attr.getLength();
      for(int a = 0; a < al; a++) {
        atts.add(token(attr.getQName(a)), token(attr.getValue(a)), stripNS);
      }
      final byte[] en = token(name);
      builder.openElem(stripNS ? local(en) : en, atts, nsp);

      boolean strip = strips.peek();
      if(stripWS) {
        final int a = atts.get(DataText.XML_SPACE);
        if(a != -1) {
          final byte[] s = atts.value(a);
          if(eq(s, DataText.DEFAULT)) strip = true;
          else if(eq(s, DataText.PRESERVE)) strip = false;
        }
      }
      strips.push(strip);

      atts.reset();
      nsp.reset();
      ++nodes;
    } catch(final IOException ex) {
      throw error(ex);
    }
  }

  @Override
  public void endElement(final String uri, final String local, final String name)
      throws SAXException {
    try {
      finishText();
      builder.closeElem();
      strips.pop();
    } catch(final IOException ex) {
      throw error(ex);
    }
  }

  @Override
  public void characters(final char[] chars, final int start, final int length) {
    sb.append(chars, start, length);
  }

  @Override
  public void processingInstruction(final String name, final String content) throws SAXException {
    if(dtd) return;
    try {
      finishText();
      builder.pi(token(name + ' ' + content));
    } catch(final IOException ex) {
      throw error(ex);
    }
  }

  @Override
  public void comment(final char[] chars, final int start, final int length) throws SAXException {
    if(dtd) return;
    try {
      finishText();
      builder.comment(token(new String(chars, start, length)));
    } catch(final IOException ex) {
      throw error(ex);
    }
  }

  /**
   * Checks if a text node has to be written.
   * @throws IOException I/O exception
   */
  private void finishText() throws IOException {
    if(sb.length() != 0) {
      final String s = sb.toString();
      builder.text(token(strips.peek() ? s.trim() : s));
      sb.setLength(0);
    }
  }

  /**
   * Creates and throws a SAX exception for the specified exception.
   * @param ex exception
   * @return SAX exception
   */
  protected static SAXException error(final IOException ex) {
    final SAXException ioe = new SAXException(Util.message(ex));
    ioe.setStackTrace(ex.getStackTrace());
    return ioe;
  }

  // EntityResolver

  /* public InputSource resolveEntity(String pub, String sys) { } */

  // DTDHandler

  /* public void notationDecl(String name, String pub, String sys) { } */
  /* public void unparsedEntityDecl(String name, String pub, String sys, String not) { } */

  // ContentHandler

  /*public void setDocumentLocator(Locator locator) { } */

  @Override
  public void startPrefixMapping(final String prefix, final String uri) {
    if(!stripNS) nsp.add(token(prefix), token(uri));
  }

  /*public void endPrefixMapping(String prefix) { } */
  /*public void ignorableWhitespace(char[] ch, int s, int l) { } */
  /*public void skippedEntity(String name) { } */

  // ErrorHandler

  /* public void warning(SAXParseException ex) { } */
  /* public void fatalError(SAXParseException ex) { } */

  // LexicalHandler
  @Override
  public void startDTD(final String name, final String pid, final String sid) {
    dtd = true;
  }

  @Override
  public void endDTD() {
    dtd = false;
  }

  @Override
  public void endCDATA() { /* ignored. */ }

  @Override
  public void endEntity(final String entity) { /* ignored. */ }

  @Override
  public void startCDATA() { /* ignored. */ }

  @Override
  public void startEntity(final String entity) { /* ignored. */ }
}
