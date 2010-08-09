package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Treat as expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /** Instance. */
  final SeqType seq;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Treat(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    seq = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).value() ? preEval(ctx) : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr);
    final Item it = iter.next();
    if(it == null) {
      if(!seq.mayBeZero() || seq.type == Type.EMP) return Iter.EMPTY;
      Err.or(input, XPEMPTY, desc());
    }
    if(seq.zeroOrOne()) {
      if(iter.next() != null) Err.or(input, NOTREATS, desc(), seq);
      if(!it.type.instance(seq.type))
        Err.or(input, NOTREAT, desc(), seq, it.type);
      return it.iter(ctx);
    }

    return new Iter() {
      Item i = it;

      @Override
      public Item next() throws QueryException {
        if(i == null) return null;
        if(!i.type.instance(seq.type))
          Err.or(input, NOTREAT, desc(), seq, i.type);
        final Item ii = i;
        i = iter.next();
        return ii;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + TREAT + " " + AS + " " + seq;
  }
}
