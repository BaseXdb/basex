package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.util.Token;
import org.w3c.dom.Text;

/**
 * Text node fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FTxt extends FNode {
  /**
   * Constructor.
   * @param t text value
   * @param p parent
   */
  public FTxt(final byte[] t, final ANode p) {
    super(NodeType.TXT);
    val = t;
    par = p;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param txt DOM node
   * @param parent parent reference
   */
  FTxt(final Text txt, final ANode parent) {
    this(Token.token(txt.getData()), parent);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.text(val);
  }

  @Override
  public FTxt copy() {
    return new FTxt(val, par);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, VAL, val);
  }

  @Override
  public String toString() {
    return Token.string(val);
  }
}
