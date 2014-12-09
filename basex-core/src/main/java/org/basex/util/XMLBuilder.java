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
public final class XMLBuilder {
  /** XML string. */
  private final TokenBuilder cache = new TokenBuilder();
  /** Element stack. */
  private final TokenList open = new TokenList();
  /** Indents the output. */
  private boolean indent;

  /** Current indentation flag. */
  private boolean indenting;
  /** Current opening flag. */
  private boolean opening;

  /**
   * Activates indentation.
   * @return self reference
   */
  public XMLBuilder indent() {
    indent = true;
    return this;
  }

  /**
   * Opens an element.
   * @param name name of element
   * @param attributes name and value pairs
   * @return self reference
   */
  public XMLBuilder open(final Object name, final Object... attributes) {
    opening();
    ws();
    final TokenBuilder cch = cache;
    cch.add('<').add(TokenBuilder.token(name));
    final int al = attributes.length;
    for(int a = 0; a < al; a += 2) {
      cch.add(' ').add(TokenBuilder.token(attributes[a])).add('=').add('"');
      attribute(TokenBuilder.token(attributes[a + 1]));
      cch.add('"');
    }
    open.add(TokenBuilder.token(name));
    indenting = indent;
    opening = true;
    return this;
  }

  /**
   * Encodes the specified text.
   * @param value value to be encoded
   * @return self reference
   */
  public XMLBuilder text(final Object value) {
    final byte[] token = TokenBuilder.token(value);
    final int tl = token.length;
    if(tl != 0) {
      opening();
      for(int k = 0; k < tl; k += cl(token, k)) add(cp(token, k));
      indenting = false;
    }
    return this;
  }

  /**
   * Closes an element.
   * @return self reference
   */
  public XMLBuilder close() {
    final TokenBuilder cch = cache;
    final byte[] name = open.pop();
    if(opening) {
      cch.add('/');
      opening = false;
    } else {
      ws();
      cch.add('<').add('/').add(name);
    }
    cch.add('>');
    indenting = indent;
    return this;
  }

  /**
   * Returns the XML document as byte array, and invalidates the internal array.
   * @return XML document
   */
  public byte[] finish() {
    while(!open.isEmpty()) close();
    return cache.finish();
  }

  @Override
  public String toString() {
    return cache.toString();
  }

  /**
   * Finishes an opening element.
   */
  private void opening() {
    if(opening) {
      cache.add('>');
      opening = false;
    }
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
    if(indenting) {
      final int os = open.size();
      if(os >= 0) {
        final TokenBuilder cch = cache;
        cch.add('\n');
        for(int o = 0; o < os; o++) cch.add(' ').add(' ');
      }
    }
  }
}
