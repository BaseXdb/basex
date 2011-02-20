package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.AtomType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Treat as expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Treat(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    type = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).value() ? optPre(value(ctx), ctx) : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr);
    final Item it = iter.next();
    if(it == null) {
      if(!type.mayBeZero() || type.type == AtomType.EMP) return Empty.ITER;
      XPEMPTY.thrw(input, desc());
    }
    if(type.zeroOrOne()) {
      if(iter.next() != null) NOTREATS.thrw(input, desc(), type);
      if(!it.type.instance(type.type))
        NOTREAT.thrw(input, desc(), type, it.type);
      return it.iter();
    }

    return new Iter() {
      Item i = it;

      @Override
      public Item next() throws QueryException {
        if(i == null) return null;
        if(!i.type.instance(type.type))
          NOTREAT.thrw(input, desc(), type, i.type);
        final Item ii = i;
        i = iter.next();
        return ii;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(type.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + TREAT + " " + AS + " " + type;
  }
}
