package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * DOM - Character Data Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class BXChar extends BXNode implements CharacterData {
  /**
   * Constructor.
   * @param n node reference
   */
  protected BXChar(final Nod n) {
    super(n);
  }

  public final String getData() {
    return getNodeValue();
  }

  @Override
  public final String getNodeValue() {
    return Token.string(node.str());
  }

  public final int getLength() {
    return node.str().length;
  }

  public final String substringData(final int off, final int count) {
    final String val = getNodeValue();
    if(count < 0 || off < 0 || off >= val.length()) throw new DOMException(
        DOMException.INDEX_SIZE_ERR, "Invalid size values.");
    return val.substring(off, Math.min(val.length(), off + count));
  }

  public final void appendData(final String arg) {
    BaseX.notimplemented();
  }

  public final void deleteData(final int off, final int count) {
    BaseX.notimplemented();
  }

  public final void insertData(final int off, final String arg) {
    BaseX.notimplemented();
  }

  public final void replaceData(final int off, final int c, final String arg) {
    BaseX.notimplemented();
  }

  public final void setData(final String dat) {
    BaseX.notimplemented();
  }
}
