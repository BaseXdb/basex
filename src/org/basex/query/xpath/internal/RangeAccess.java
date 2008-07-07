package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.index.RangeToken;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * This index class retrieves range values from the index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class RangeAccess extends InternalExpr {
  /** Index type. */
  private final RangeToken index;

  /**
   * Constructor.
   * @param ind index terms
   */
  public RangeAccess(final RangeToken ind) {
    index = ind;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    ctx.local = new NodeSet(ctx.local.data.ids(index)[0], ctx);
    return ctx.local;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token(TYPE), Token.token(
        index.type.toString()), Token.token(MIN), Token.token(index.min),
        Token.token(MAX), Token.token(index.max));
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, %-%)", name(), index.type, index.min, index.max);
  }
}
