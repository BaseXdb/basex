package org.basex.api.dom;

import org.basex.core.Main;
import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * DOM - Character data implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class BXChar extends BXNode implements CharacterData {
  /**
   * Constructor.
   * @param n node reference
   */
  protected BXChar(final Nod n) {
    super(n);
  }

  @Override
  public final String getData() {
    return getNodeValue();
  }

  @Override
  public final String getNodeValue() {
    return Token.string(node.atom());
  }

  @Override
  public final int getLength() {
    return node.atom().length;
  }

  @Override
  public final String substringData(final int off, final int count) {
    final String val = getNodeValue();
    if(count < 0 || off < 0 || off >= val.length()) throw new DOMException(
        DOMException.INDEX_SIZE_ERR, "Invalid size values.");
    return val.substring(off, Math.min(val.length(), off + count));
  }

  @Override
  public final void appendData(final String arg) {
    Main.notimplemented();
  }

  @Override
  public final void deleteData(final int off, final int count) {
    Main.notimplemented();
  }

  @Override
  public final void insertData(final int off, final String arg) {
    Main.notimplemented();
  }

  @Override
  public final void replaceData(final int off, final int c, final String arg) {
    Main.notimplemented();
  }

  @Override
  public final void setData(final String dat) {
    Main.notimplemented();
  }
}
