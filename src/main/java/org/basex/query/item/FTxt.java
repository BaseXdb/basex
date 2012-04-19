package org.basex.query.item;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Text node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTxt extends FNode {
  /**
   * Constructor.
   * @param t text value
   */
  public FTxt(final byte[] t) {
    super(NodeType.TXT);
    val = t;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param txt DOM node
   */
  FTxt(final Text txt) {
    this(Token.token(txt.getData()));
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.text(val);
  }

  @Override
  public FNode copy() {
    return new FTxt(val).parent(par);
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
