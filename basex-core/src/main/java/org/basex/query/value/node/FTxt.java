package org.basex.query.value.node;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Text node fragment.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FTxt extends FNode {
  /** Text value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param value text value
   */
  public FTxt(final byte[] value) {
    super(NodeType.TEXT);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value text value
   */
  public FTxt(final String value) {
    this(Token.token(value));
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
  public byte[] string() {
    return value;
  }

  @Override
  public FTxt materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc) {
    return materialized(test, ii) ? this : new FTxt(value);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTxt && Token.eq(value, ((FTxt) obj).value) &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(value);
  }
}
