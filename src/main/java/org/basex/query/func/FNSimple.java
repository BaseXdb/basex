package org.basex.query.func;

import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Cmp;
import org.basex.query.expr.CmpG;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Compare;
import org.basex.util.InputInfo;

/**
 * Simple functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNSimple extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNSimple(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case ONE_OR_MORE:
        final Iter ir = expr[0].iter(ctx);
        final long len = ir.size();
        if(len == 0) throw EXPECTOM.thrw(input);
        if(len > 0) return ir;
        return new Iter() {
          private boolean first = true;
          @Override
          public Item next() throws QueryException {
            final Item it = ir.next();
            if(first) {
              if(it == null) throw EXPECTOM.thrw(input);
              first = false;
            }
            return it;
          }
        };
      case UNORDERED:
        return ctx.iter(expr[0]);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case ONE_OR_MORE:
        final Value val = ctx.value(expr[0]);
        if(val.isEmpty()) throw EXPECTOM.thrw(input);
        return val;
      case UNORDERED:
        return ctx.value(expr[0]);
      default:
        return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Expr e = expr.length == 1 ? expr[0] : null;
    switch(def) {
      case FALSE:
        return Bln.FALSE;
      case TRUE:
        return Bln.TRUE;
      case EMPTY:
        return Bln.get(e.iter(ctx).next() == null);
      case EXISTS:
        return Bln.get(e.iter(ctx).next() != null);
      case BOOLEAN:
        return Bln.get(e.ebv(ctx, input).bool(input));
      case NOT:
        return Bln.get(!e.ebv(ctx, input).bool(input));
      case DEEP_EQUAL:
        return Bln.get(deep(ctx));
      case ZERO_OR_ONE:
        Iter ir = e.iter(ctx);
        Item it = ir.next();
        if(it != null && ir.next() != null) EXPECTZ0.thrw(input);
        return it;
      case EXACTLY_ONE:
        ir = e.iter(ctx);
        it = ir.next();
        if(it == null || ir.next() != null) EXPECTO.thrw(input);
        return it;
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public Expr cmp(final QueryContext ctx) {
    // all functions have at least 1 argument
    final Expr e = expr[0];

    switch(def) {
      case EMPTY:
      case EXISTS:
        // ignore non-deterministic expressions (e.g.: error())
        return e.size() == -1 || e.uses(Use.NDT) || e.uses(Use.CNS) ? this :
          Bln.get(def == Function.EMPTY ^ e.size() != 0);
      case BOOLEAN:
        // simplify, e.g.: if(boolean(A)) -> if(A)
        return e.type().eq(SeqType.BLN) ? e : this;
      case NOT:
        if(e.isFunction(Function.EMPTY)) {
          // simplify: not(empty(A)) -> exists(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((FuncCall) e).expr;
          def = Function.EXISTS;
        } else if(e.isFunction(Function.EXISTS)) {
          // simplify: not(exists(A)) -> empty(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((FuncCall) e).expr;
          def = Function.EMPTY;
        } else if(e instanceof CmpV || e instanceof CmpG) {
          // simplify: not('a' = 'b') -> 'a' != 'b'
          final Cmp c = ((Cmp) e).invert();
          return c == e ? this : c;
        } else if(e.isFunction(Function.NOT)) {
          // simplify: not(not(A)) -> boolean(A)
          return compBln(((FuncCall) e).expr[0]);
        } else {
          // simplify, e.g.: not(boolean(A)) -> not(A)
          expr[0] = e.compEbv(ctx);
        }
        return this;
      case ZERO_OR_ONE:
        type = SeqType.get(e.type().type, SeqType.Occ.ZO);
        return e.type().zeroOrOne() ? e : this;
      case EXACTLY_ONE:
        type = SeqType.get(e.type().type, SeqType.Occ.O);
        return e.type().one() ? e : this;
      case ONE_OR_MORE:
        type = SeqType.get(e.type().type, SeqType.Occ.OM);
        return !e.type().mayBeZero() ? e : this;
      case UNORDERED:
        return e;
      default:
        return this;
    }
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // all functions have at least 1 argument
    final Expr e = expr[0];

    Expr ex = this;
    if(def == Function.BOOLEAN) {
      // (test)[boolean(A)] -> (test)[A]
      if(!e.type().mayBeNum()) ex = e;
    } else if(def == Function.EXISTS) {
      // if(exists(node*)) -> if(node*)
      if(e.type().type.isNode() || e.size() > 0) ex = e;
    }
    if(ex != this) ctx.compInfo(QueryText.OPTWRITE, this);
    return ex;
  }

  /**
   * Checks items for deep equality.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deep(final QueryContext ctx) throws QueryException {
    if(expr.length == 3) checkColl(expr[2], ctx);
    return Compare.deep(ctx.iter(expr[0]), ctx.iter(expr[1]), input);
  }
}
