package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.util.Token;
import org.w3c.dom.CharacterData;

/**
 * DOM - Character Data Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class CharImpl extends NodeImpl implements CharacterData {
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param k node kind
   */
  protected CharImpl(final Data d, final int p, final int k) {
    super(d, p, k);
  }

  public final String getData() {
    return getNodeValue();
  }

  @Override
  public String getNodeValue() {
    return Token.string(data.text(pre));
  }

  public final int getLength() {
    return data.textLen(pre);
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
