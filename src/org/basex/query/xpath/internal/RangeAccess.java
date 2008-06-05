package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.data.StatsKey;
import org.basex.index.Index;
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
  private final Index.TYPE type;
  /** Range Key. */
  private final StatsKey key;
  /** Minimum Value. */
  final double min;
  /** Maximum Value. */
  final double max;

  /**
   * Constructor.
   * @param typ index type
   * @param ky statistics key
   * @param mn minimum value
   * @param mx maximum value
   */
  public RangeAccess(final Index.TYPE typ, final StatsKey ky,
      final double mn, final double mx) {
    type = typ;
    key = ky;
    min = mn;
    max = mx;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    // out of range - return empty node set (could be moved to compile method)
    ctx.local = max < key.min || min > key.max ? new NodeSet(ctx) :
      new NodeSet(ctx.local.data.idRange(type, min, true, max, true), ctx);
    return ctx.local;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token(TYPE), Token.token(type.toString()),
        Token.token(MIN), Token.token(min), Token.token(MAX), Token.token(max));
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, %-%)", name(), type, min, max);
  }
}
