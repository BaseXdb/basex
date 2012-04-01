package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTIter;
import org.basex.util.InputInfo;

/**
 * FTExtensionSelection expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FTExtensionSelection extends FTExpr {
  /** Pragmas. */
  private final Expr[] pragmas;

  /**
   * Constructor.
   * @param ii input info
   * @param prag pragmas
   * @param e enclosed FTSelection
   */
  public FTExtensionSelection(final InputInfo ii, final Expr[] prag,
      final FTExpr e) {
    super(ii, e);
    pragmas = prag;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return expr[0].item(ctx, info);
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return expr[0].iter(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : pragmas) e.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr p : pragmas) sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ' + expr[0] + ' ' + BRACE2).toString();
  }
}
