package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Character data implementation.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public final String substringData(final int off, final int count) {
    final String val = getNodeValue();
    if(count < 0 || off < 0 || off >= val.length()) throw new DOMException(
        DOMException.INDEX_SIZE_ERR, "Invalid size values.");
    return val.substring(off, Math.min(val.length(), off + count));
  }

  @Override
  public final void appendData(final String value) {
    throw notImplemented();
  }

  @Override
  public final void deleteData(final int off, final int count) {
    throw notImplemented();
  }

  @Override
  public final void insertData(final int off, final String value) {
    throw notImplemented();
  }

  @Override
  public final void replaceData(final int off, final int count, final String value) {
    throw notImplemented();
  }

  @Override
  public final void setData(final String value) {
    throw notImplemented();
  }
}
