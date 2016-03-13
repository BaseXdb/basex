package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.Text;

/**
 * Text node fragment.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FTxt extends FNode {
  /**
   * Constructor.
   * @param t text value
   */
  public FTxt(final String t) {
    this(Token.token(t));
  }

  /**
   * Constructor.
   * @param value text value
   */
  public FTxt(final byte[] value) {
    super(NodeType.TXT);
    this.value = value;
  }

  /**
   * Constructor for creating a text from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param txt DOM node
   */
  public FTxt(final Text txt) {
    this(txt.getData());
  }

  @Override
  public FNode deepCopy(final MainOptions options) {
    return new FTxt(value).parent(parent);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, value));
  }

  @Override
  public String toString() {
    return Atm.toString(value);
  }
}
