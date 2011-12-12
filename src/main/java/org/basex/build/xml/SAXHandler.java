package org.basex.build.xml;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.util.Atts;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Parser wrapper.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class SAXHandler extends DefaultHandler implements LexicalHandler {
  /** Temporary attribute array. */
  private final Atts atts = new Atts();
  /** Builder reference. */
  private final Builder builder;
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
   * @param build builder reference
   */
  SAXHandler(final Builder build) {
    builder = build;
  }

  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) throws SAXException {

    try {
      finishText();
      final int as = at.getLength();
      atts.reset();
      for(int a = 0; a < as; ++a) {
        atts.add(token(at.getQName(a)), token(at.getValue(a)));
      }
      builder.startElem(token(qn), atts);
      ++nodes;
    } catch(final IOException ex) {
      error(ex);
    }
  }

  @Override
  public void endElement(final String uri, final String ln, final String qn)
      throws SAXException {

    try {
      finishText();
      builder.endElem();
    } catch(final IOException ex) {
      error(ex);
    }
  }

  @Override
  public void characters(final char[] ch, final int s, final int l) {
    sb.append(ch, s, l);
  }

  @Override
  public void processingInstruction(final String nm, final String cont)
      throws SAXException {

    if(dtd) return;
    try {
      finishText();
      builder.pi(token(nm + ' ' + cont));
    } catch(final IOException ex) {
      error(ex);
    }
  }

  @Override
  public void comment(final char[] ch, final int s, final int l)
      throws SAXException {

    if(dtd) return;
    try {
      finishText();
      builder.comment(token(new String(ch, s, l)));
    } catch(final IOException ex) {
      error(ex);
    }
  }

  /** Temporary token builder.
  private final TokenBuilder tb = new TokenBuilder();
   */
  /** Temporary string builder for high surrogates. */
  private final StringBuilder sb = new StringBuilder();
  /** Temporary namespaces. */
  private final Atts ns = new Atts();

  /**
   * Checks if a text node has to be written.
   * @throws IOException I/O exception
   */
  private void finishText() throws IOException {
    final boolean sur = sb.length() != 0;
    if(sb.length() != 0 || sur) {
      builder.text(token(sb.toString()));
      sb.setLength(0);
    }
    for(int i = 0; i < ns.size(); ++i) {
      builder.startNS(ns.name(i), ns.string(i));
    }
    ns.reset();
  }

  /**
   * Creates and throws a SAX exception for the specified exception.
   * @param ex exception
   * @throws SAXException SAX exception
   */
  private void error(final IOException ex) throws SAXException {
    final SAXException ioe = new SAXException(ex.getMessage());
    ioe.setStackTrace(ex.getStackTrace());
    throw ioe;
  }

  // EntityResolver
  /* public InputSource resolveEntity(String pub, String sys) { } */

  // DTDHandler
  /* public void notationDecl(String name, String pub, String sys) { } */
  /* public void unparsedEntityDecl(final String name, final String pub,
      final String sys, final String not) { } */

  // ContentHandler
  /*public void setDocumentLocator(final Locator locator) { } */

  @Override
  public void startPrefixMapping(final String prefix, final String uri) {
    ns.add(token(prefix), token(uri));
  }

  /*public void endPrefixMapping(final String prefix) { } */
  /*public void ignorableWhitespace(char[] ch, int s, int l) { } */
  /*public void skippedEntity(final String name) { } */

  // ErrorHandler
  /* public void warning(final SAXParseException ex) { } */
  /* public void fatalError(final SAXParseException ex) { } */

  // LexicalHandler
  @Override
  public void startDTD(final String n, final String pid, final String sid) {
    dtd = true;
  }

  @Override
  public void endDTD() {
    dtd = false;
  }

  @Override
  public void endCDATA() { /* ignored. */ }
  @Override
  public void endEntity(final String n) { /* ignored. */ }
  @Override
  public void startCDATA() { /* ignored. */ }
  @Override
  public void startEntity(final String n) { /* ignored. */ }
}
