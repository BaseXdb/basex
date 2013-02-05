package org.basex.io.serial;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 * Bridge to translate XQuery items to SAX events.
 * The {@link #parse(Item)} method does the following:
 * <ol>
 *   <li>notify startDocument()</li>
 *   <li>serialize the item</li>
 *   <li>notify endDocument()</li>
 * </ol>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Hedenus
 */
public final class SAXSerializer extends Serializer implements XMLReader {
  /** Content handler. */
  private ContentHandler contentHandler;
  /** Entity resolver. */
  private EntityResolver entityResolver;
  /** DTD handler. */
  private DTDHandler dtdHandler;
  /** Error handler. */
  private ErrorHandler errorHandler;
  /** Lexical handler. */
  private LexicalHandler lexicalHandler;

  /**
   * Constructor.
   */
  public SAXSerializer() { }

  // XMLReader ==========================================================================

  @Override
  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  @Override
  public boolean getFeature(final String name) {
    return false;
  }

  @Override
  public Object getProperty(final String name) {
    return null;
  }

  /**
   * Parses the specified item.
   * @param item item to be parsed
   * @throws SAXException SAX exception
   */
  public void parse(final Item item) throws SAXException {
    try {
      contentHandler.startDocument();
      serialize(item);
      contentHandler.endDocument();
    } catch(final Exception ex) {
      throw new SAXException(ex);
    }
  }

  @Override
  public void parse(final InputSource input) throws SAXException {
    Util.notimplemented("Call parse(Item) instead.");
  }

  @Override
  public void parse(final String id) throws SAXException {
    Util.notimplemented("Call parse(Item) instead.");
  }

  @Override
  public void setContentHandler(final ContentHandler ch) {
    contentHandler = ch;
  }

  /**
   * Sets a lexical handler.
   * @param lh handler reference
   */
  public void setLexicalHandler(final LexicalHandler lh) {
    lexicalHandler = lh;
  }

  @Override
  public void setEntityResolver(final EntityResolver er) {
    entityResolver = er;
  }

  @Override
  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  @Override
  public void setDTDHandler(final DTDHandler dtd) {
    dtdHandler = dtd;
  }

  @Override
  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  @Override
  public void setErrorHandler(final ErrorHandler eh) {
    errorHandler = eh;
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  @Override
  public void setFeature(final String name, final boolean value)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException();
  }

  @Override
  public void setProperty(final String name, final Object value)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException();
  }

  // Serializer =========================================================================

  /** Map containing all attributes. */
  private final Atts attributes = new Atts();
  /** Map containing all attributes. */
  private NSDecl namespaces;

  @Override
  protected void startOpen(final byte[] n) throws IOException {
    namespaces = new NSDecl(namespaces);
    attributes.reset();
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) throws IOException {
    byte[] prefix = null;
    if(startsWith(n, XMLNS)) {
      if(n.length == 5) {
        prefix = EMPTY;
      } else if(n[5] == ':') {
        prefix = substring(n, 6);
      }
    }

    if(prefix != null) {
      namespaces.put(prefix, v);
    } else {
      attributes.add(n, v);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    try {
      final AttributesImpl attrs = new AttributesImpl();
      final int as = attributes.size();
      for(int a = 0; a < as; a++) {
        final byte[] name = attributes.name(a);
        final String ns = string(namespaces.get(prefix(name)));
        final String lname = string(local(name));
        final String rname = string(name);
        final String value = string(attributes.string(a));
        attrs.addAttribute(ns, lname, rname, null, value);
      }

      final String ns = string(namespaces.get(prefix(elem)));
      final String lname = string(local(elem));
      final String rname = string(elem);
      contentHandler.startElement(ns, lname, rname, attrs);

    } catch(final SAXException ex) {
      throw new IOException(ex);
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
      final String name = string(elem);
      contentHandler.endElement("", name, name);
      namespaces = namespaces.getParent();
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void finishText(final byte[] text) throws IOException {
    try {
      final String s = string(text);
      final char[] c = s.toCharArray();
      contentHandler.characters(c, 0, c.length);
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void finishComment(final byte[] comment) throws IOException {
    if(lexicalHandler != null) {
      try {
        final String s = string(comment);
        final char[] c = s.toCharArray();
        lexicalHandler.comment(c, 0, c.length);
      } catch(final SAXException ex) {
        throw new IOException(ex);
      }
    }
  }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) throws IOException {
    try {
      contentHandler.processingInstruction(string(n), string(v));
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void atomic(final Item i) throws IOException {
    // ignored
  }

  /**
   * Namespace declaration.
   */
  static class NSDecl {
    /** Parent namespace declarations. */
    private final NSDecl parent;
    /** Namespace declarations. */
    private Atts decls;

    /**
     * Constructor.
     * @param par parent declarations
     */
    NSDecl(final NSDecl par) {
      parent = par;
    }

    /**
     * Returns the parent declarations.
     * @return parent declarations
     */
    NSDecl getParent() {
      return parent;
    }

    /**
     * Stores a new prefix and namespace.
     * @param prefix prefix
     * @param uri namespace uri
     */
    void put(final byte[] prefix, final byte[] uri) {
      decls.add(prefix, uri);
    }

    /**
     * Retrieves the namespace uri for the given prefix.
     * @param prefix prefix to be found
     * @return namespace uri
     */
    byte[] get(final byte[] prefix) {
      for(NSDecl c = this; c != null; c = c.parent) {
        if(c.decls != null) {
          final byte[] ns = c.decls.string(prefix);
          if(ns != null) return ns;
        }
      }
      return EMPTY;
    }
  }
}
