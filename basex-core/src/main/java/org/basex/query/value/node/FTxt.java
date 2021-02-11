package org.basex.query.value.node;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Text node fragment.
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(NodeType.TEXT);
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
  public FTxt materialize(final QueryContext qc, final boolean copy) {
    return copy ? new FTxt(value) : this;
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), value);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.quoted(value);
  }
}
