package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.xquery.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.CharacterData;

/**
 * DOM - Character Data Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public String getNodeValue() {
    return Token.string(node.str());
  }

  public final int getLength() {
    return node.str().length;
  }

  public final String substringData(final int off, final int count) {
    final String val = getNodeValue();
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
