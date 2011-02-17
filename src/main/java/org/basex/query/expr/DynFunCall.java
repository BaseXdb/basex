package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DynFunCall extends Arr {

  /** Function reference. */
  private Expr func;

  /**
   * Function constructor.
   * @param ii input info
   * @param fun function expression
   * @param arg arguments
   */
  public DynFunCall(final InputInfo ii, final Expr fun, final Expr[] arg) {
    super(ii, arg);
    func = fun;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    type = func.type();
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(toString()));
    func.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(func.toString()).add('(').add(
        toString(", ")).add(')').toString();
  }
}
