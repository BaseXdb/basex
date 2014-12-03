package org.basex.util;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.util.list.*;

/**
 * Simple XML string builder.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XMLTokenBuilder {
  /** XML string. */
  private final TokenBuilder cache = new TokenBuilder();
  /** Element stack. */
  private final TokenList open = new TokenList();
  /** Indents the output. */
  private boolean indent;
  /** Current indentation flag. */
  private boolean ind;

  /**
   * Activates indentation.
   * @return self reference
   */
  public XMLTokenBuilder indent() {
    indent = true;
    return this;
  }

  /**
   * Opens an element.
   * @param name name of element
   * @param atts attribute names and values
   */
  public void open(final byte[] name, final byte[]... atts) {
    ws();
    final TokenBuilder cch = cache;
    cch.add('<').add(name);
    final int al = atts.length;
    for(int a = 0; a < al; a += 2) {
      cch.add(' ').add(atts[a]).add('=').add('"');
      attribute(atts[a + 1]);
      cch.add('"');
    }
    cch.add('>');
    open.add(name);
    ind = indent;
  }

  /**
   * Closes an element.
   */
  public void close() {
    final byte[] name = open.pop();
    ws();
    cache.add('<').add('/').add(name).add('>');
    ind = indent;
  }

  /**
   * Encodes the specified text.
   * @param value value to be encoded
   */
  public void text(final byte[] value) {
    final int tl = value.length;
    for(int k = 0; k < tl; k += cl(value, k)) add(cp(value, k));
    ind = false;
  }

  /**
   * Returns the XML document as byte array, and invalidates the internal array.
   * @return XML document
   */
  public byte[] finish() {
    while(!open.isEmpty()) close();
    return cache.finish();
  }

  /**
   * Encodes the specified attribute value.
   * @param value value to be encoded
   */
  private void attribute(final byte[] value) {
    final int vl = value.length;
    for(int k = 0; k < vl; k += cl(value, k)) {
      final int ch = cp(value, k);
      if(ch == '"') {
        cache.add(E_QUOT);
      } else if(ch == 0x9 || ch == 0xA) {
        addHex(ch);
      } else {
        add(ch);
      }
    }
  }

  /**
   * Encodes the specified codepoint.
   * @param cp codepoint to be encoded
   */
  private void add(final int cp) {
    if(cp < ' ' && cp != '\n' && cp != '\t' || cp >= 0x7F && cp < 0xA0) {
      addHex(cp);
    } else if(cp == '&') {
      cache.add(E_AMP);
    } else if(cp == '>') {
      cache.add(E_GT);
    } else if(cp == '<') {
      cache.add(E_LT);
    } else if(cp == 0x2028) {
      cache.add(E_2028);
    } else {
      cache.add(cp);
    }
  }

  /**
   * Returns a hex entity for the specified codepoint.
   * @param cp codepoint
   */
  private void addHex(final int cp) {
    cache.add("&#x");
    if(cp > 0xF) cache.add(HEX[cp >> 4]);
    cache.add(HEX[cp & 0xF]).add(';');
  }

  /**
   * Adds some indentation.
   */
  private void ws() {
    if(ind) {
      final int os = open.size();
      if(os >= 0) {
        final TokenBuilder cch = cache;
        cch.add('\n');
        for(int o = 0; o < os; o++) cch.add(' ').add(' ');
      }
    }
  }
}
