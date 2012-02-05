package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Treat as expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
    return checkUp(expr, ctx).isValue() ? optPre(value(ctx), ctx) : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr);
    final Item it = iter.next();
    if(it == null) {
      if(type.mayBeZero()) return Empty.ITER;
      throw XPEMPTY.thrw(input, description());
    }
    if(type.zeroOrOne()) {
      if(iter.next() != null) NOTREATS.thrw(input, description(), type);
      if(!it.type.instanceOf(type.type))
        NOTREAT.thrw(input, description(), it.type, type);
      return it.iter();
    }

    return new Iter() {
      Item i = it;

      @Override
      public Item next() throws QueryException {
        if(i == null) return null;
        if(!i.type.instanceOf(type.type))
          NOTREAT.thrw(input, description(), i.type, type);
        final Item ii = i;
        i = iter.next();
        return ii;
      }
    };
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final Value val = ctx.value(expr);

    final long len = val.size();
    if(len == 0) {
      if(type.mayBeZero()) return val;
      throw XPEMPTY.thrw(input, description());
    }
    if(type.zeroOrOne()) {
      if(len > 1) throw NOTREATS.thrw(input, description(), type);
      final Item it = val.itemAt(0);
      if(!it.type.instanceOf(type.type))
        NOTREAT.thrw(input, description(), it.type, type);
      return it;
    }

    for(long i = 0; i < len; i++) {
      final Item it = val.itemAt(i);
      if(!it.type.instanceOf(type.type))
        NOTREAT.thrw(input, description(), it.type, type);
    }

    return val;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYP, Token.token(type.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + TREAT + ' ' + AS + ' ' + type;
  }
}
