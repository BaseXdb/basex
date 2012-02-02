package org.basex.api.dom;

import org.basex.query.item.ANode;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * DOM - Character data implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class BXChar extends BXNode implements CharacterData {
  /**
   * Constructor.
   * @param n node reference
   */
  BXChar(final ANode n) {
    super(n);
  }

  @Override
  public final String getData() {
    return getNodeValue();
  }

  @Override
  public final String getNodeValue() {
    return Token.string(node.string());
  }

  @Override
  public final int getLength() {
    return node.string().length;
  }

  @Override
  public final String substringData(final int off, final int count) {
    final String val = getNodeValue();
    if(count < 0 || off < 0 || off >= val.length()) throw new DOMException(
        DOMException.INDEX_SIZE_ERR, "Invalid size values.");
    return val.substring(off, Math.min(val.length(), off + count));
  }

  @Override
  public final void appendData(final String value) {
    Util.notimplemented();
  }

  @Override
  public final void deleteData(final int off, final int count) {
    Util.notimplemented();
  }

  @Override
  public final void insertData(final int off, final String value) {
    Util.notimplemented();
  }

  @Override
  public final void replaceData(final int off, final int count,
      final String value) {
    Util.notimplemented();
  }

  @Override
  public final void setData(final String dat) {
    Util.notimplemented();
  }
}
