package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
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
  private final RangeToken ind;

  /**
   * Constructor.
   * @param i index terms
   */
  public RangeAccess(final RangeToken i) {
    ind = i;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    final IndexIterator it = ctx.local.data.ids(ind);
    final int[] ids = new int[it.size()];
    for(int i = 0; i < ids.length; i++) ids[i] = it.next();
    ctx.local = new NodeSet(ids, ctx);
    return ctx.local;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token(TYPE), Token.token(ind.type.toString()),
        Token.token(MIN), Token.token(ind.min),
        Token.token(MAX), Token.token(ind.max));
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, %-%)", name(), ind.type, ind.min, ind.max);
  }
}
