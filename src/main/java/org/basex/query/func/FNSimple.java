package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.Compare.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Simple functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNSimple extends StandardFunc {
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
    switch(sig) {
      case ONE_OR_MORE:
        final Iter ir = expr[0].iter(ctx);
        final long len = ir.size();
        if(len == 0) throw EXPECTOM.thrw(info);
        if(len > 0) return ir;
        return new Iter() {
          private boolean first = true;
          @Override
          public Item next() throws QueryException {
            final Item it = ir.next();
            if(first) {
              if(it == null) throw EXPECTOM.thrw(info);
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
    switch(sig) {
      case ONE_OR_MORE:
        final Value val = ctx.value(expr[0]);
        if(val.isEmpty()) throw EXPECTOM.thrw(info);
        return val;
      case UNORDERED:
        return ctx.value(expr[0]);
      default:
        return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Expr e = expr.length == 1 ? expr[0] : null;
    switch(sig) {
      case FALSE:
        return Bln.FALSE;
      case TRUE:
        return Bln.TRUE;
      case EMPTY:
        return Bln.get(e.iter(ctx).next() == null);
      case EXISTS:
        return Bln.get(e.iter(ctx).next() != null);
      case BOOLEAN:
        return Bln.get(e.ebv(ctx, info).bool(info));
      case NOT:
        return Bln.get(!e.ebv(ctx, info).bool(info));
      case DEEP_EQUAL:
        return Bln.get(deep(ctx));
      case DEEP_EQUAL_OPT:
        return Bln.get(deepOpt(ctx));
      case ZERO_OR_ONE:
        Iter ir = e.iter(ctx);
        Item it = ir.next();
        if(it != null && ir.next() != null) EXPECTZ0.thrw(info);
        return it;
      case EXACTLY_ONE:
        ir = e.iter(ctx);
        it = ir.next();
        if(it == null || ir.next() != null) EXPECTO.thrw(info);
        return it;
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  Expr opt(final QueryContext ctx) throws QueryException {
    if(expr.length == 0) return this;
    final Expr e = expr[0];

    switch(sig) {
      case EMPTY:
      case EXISTS:
        // ignore non-deterministic expressions (e.g.: error())
        return e.size() == -1 || e.uses(Use.NDT) || e.uses(Use.CNS) ? this :
          Bln.get(sig == Function.EMPTY ^ e.size() != 0);
      case BOOLEAN:
        // simplify, e.g.: if(boolean(A)) -> if(A)
        return e.type().eq(SeqType.BLN) ? e : this;
      case NOT:
        if(e.isFunction(Function.EMPTY)) {
          // simplify: not(empty(A)) -> exists(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((StandardFunc) e).expr;
          sig = Function.EXISTS;
        } else if(e.isFunction(Function.EXISTS)) {
          // simplify: not(exists(A)) -> empty(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((StandardFunc) e).expr;
          sig = Function.EMPTY;
        } else if(e instanceof CmpV || e instanceof CmpG) {
          // simplify: not('a' = 'b') -> 'a' != 'b'
          final Cmp c = ((Cmp) e).invert();
          return c == e ? this : c;
        } else if(e.isFunction(Function.NOT)) {
          // simplify: not(not(A)) -> boolean(A)
          return compBln(((StandardFunc) e).expr[0], info);
        } else {
          // simplify, e.g.: not(boolean(A)) -> not(A)
          expr[0] = e.compEbv(ctx);
        }
        return this;
      case ZERO_OR_ONE:
        type = SeqType.get(e.type().type, Occ.ZERO_ONE);
        return e.type().zeroOrOne() ? e : this;
      case EXACTLY_ONE:
        type = SeqType.get(e.type().type, Occ.ONE);
        return e.type().one() ? e : this;
      case ONE_OR_MORE:
        type = SeqType.get(e.type().type, Occ.ONE_MORE);
        return !e.type().mayBeZero() ? e : this;
      case UNORDERED:
        return e;
      default:
        return this;
    }
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    if(expr.length == 0) return this;
    final Expr e = expr[0];

    Expr ex = this;
    if(sig == Function.BOOLEAN) {
      // (test)[boolean(A)] -> (test)[A]
      if(!e.type().mayBeNumber()) ex = e;
    } else if(sig == Function.EXISTS) {
      // if(exists(node*)) -> if(node*)
      if(e.type().type.isNode()) ex = e;
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
    return Compare.deep(ctx.iter(expr[0]), ctx.iter(expr[1]), info);
  }

  /**
   * Checks items for deep equality.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deepOpt(final QueryContext ctx) throws QueryException {
    final Compare cmp = new Compare(info);
    final Flag[] flags = Flag.values();
    if(expr.length == 3) {
      final Iter ir = expr[2].iter(ctx);
      for(Item it; (it = ir.next()) != null;) {
        final byte[] key = uc(checkEStr(it));
        boolean found = false;
        for(final Flag f : flags) {
          found = eq(key, token(f.name()));
          if(found) {
            cmp.set(f);
            break;
          }
        }
        if(!found) ELMOPTION.thrw(info, key);
      }
    }
    return cmp.deep(ctx.iter(expr[0]), ctx.iter(expr[1]));
  }
}
