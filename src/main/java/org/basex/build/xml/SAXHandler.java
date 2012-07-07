package org.basex.build.xml;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 * SAX Parser wrapper.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class SAXHandler extends DefaultHandler implements LexicalHandler {
  /** Builder reference. */
  protected final Builder builder;

  /** Strip namespaces. */
  private final boolean stripNS;
  /** Temporary attribute array. */
  private final Atts atts = new Atts();
  /** DTD flag. */
  private boolean dtd;
  /** Whitespace chopping. */
  private final boolean chop;
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
   * @param ch chopping flag
   * @param sn strip namespaces
   */
  public SAXHandler(final Builder build, final boolean ch, final boolean sn) {
    builder = build;
    chop = ch;
    stripNS = sn;
  }

  @Override
  public void startElement(final String uri, final String ln, final String qn,
      final Attributes at) throws SAXException {

    try {
      finishText();
      final int as = at.getLength();
      atts.reset();
      for(int a = 0; a < as; ++a) {
        final byte[] an = token(at.getQName(a));
        final byte[] av = token(at.getValue(a));
        atts.add(stripNS ? local(an) : an, av);
      }
      final byte[] en = token(qn);
      builder.startElem(stripNS ? local(en) : en, atts);
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
  public void comment(final char[] ch, final int s, final int l) throws SAXException {
    if(dtd) return;
    try {
      finishText();
      builder.comment(token(new String(ch, s, l)));
    } catch(final IOException ex) {
      error(ex);
    }
  }

  /** Temporary string builder for high surrogates. */
  private final StringBuilder sb = new StringBuilder();
  /** Temporary namespaces. */
  private final Atts ns = new Atts();

  /**
   * Checks if a text node has to be written.
   * @throws IOException I/O exception
   */
  private void finishText() throws IOException {
    if(sb.length() != 0) {
      final String s = sb.toString();
      builder.text(token(chop ? s.trim() : s));
      sb.setLength(0);
    }
    if(!stripNS) {
      for(int i = 0; i < ns.size(); ++i) {
        builder.startNS(ns.name(i), ns.string(i));
      }
    }
    ns.reset();
  }

  /**
   * Creates and throws a SAX exception for the specified exception.
   * @param ex exception
   * @throws SAXException SAX exception
   */
  protected static void error(final IOException ex) throws SAXException {
    final SAXException ioe = new SAXException(Util.message(ex));
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
