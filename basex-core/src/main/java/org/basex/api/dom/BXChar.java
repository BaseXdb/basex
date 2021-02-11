package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

/**
 * DOM - Character data implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class BXChar extends BXNode implements CharacterData {
  /**
   * Constructor.
   * @param node node reference
   */
  BXChar(final ANode node) {
    super(node);
  }

  @Override
  public final String getData() {
    return getNodeValue();
  }

  @Override
  public final String getNodeValue() {
    return Token.string(nd.string());
  }

  @Override
  public final int getLength() {
    return nd.string().length;
  }

  @Override
  public final String substringData(final int offset, final int count) {
    final String val = getNodeValue();
    if(count < 0 || offset < 0 || offset >= val.length()) throw new DOMException(
        DOMException.INDEX_SIZE_ERR, "Invalid size values.");
    return val.substring(offset, Math.min(val.length(), offset + count));
  }

  @Override
  public final void appendData(final String arg) {
    throw notImplemented();
  }

  @Override
  public final void deleteData(final int offset, final int count) {
    throw notImplemented();
  }

  @Override
  public final void insertData(final int offset, final String arg) {
    throw notImplemented();
  }

  @Override
  public final void replaceData(final int offset, final int count, final String arg) {
    throw notImplemented();
  }

  @Override
  public final void setData(final String value) {
    throw notImplemented();
  }
}
