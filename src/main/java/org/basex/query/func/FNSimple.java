package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Simple functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNSimple extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNSimple(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ONEORMORE:
        final Iter ir = SeqIter.get(ctx.iter(expr[0]));
        if(ir.size() < 1) Err.or(input, EXP1M);
        return ir;
      case UNORDER:
        return ctx.iter(expr[0]);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Expr e = expr.length == 1 ? expr[0] : null;
    switch(func) {
      case FALSE:   return Bln.FALSE;
      case TRUE:    return Bln.TRUE;
      case EMPTY:   return Bln.get(!e.item() && e.iter(ctx).next() == null);
      case EXISTS:  return Bln.get(e.item() || e.iter(ctx).next() != null);
      case BOOLEAN: return Bln.get(e.ebv(ctx, input).bool(input));
      case NOT:     return Bln.get(!e.ebv(ctx, input).bool(input));
      case DEEPEQUAL:
        return Bln.get(deep(ctx));
      case ZEROORONE:
        Iter iter = e.iter(ctx);
        Item it = iter.next();
        if(it == null) return null;
        if(iter.next() != null) Err.or(input, EXP01);
        return it;
      case EXACTLYONE:
        iter = e.iter(ctx);
        it = iter.next();
        if(it == null || iter.next() != null) Err.or(input, EXP1);
        return it;
      default:
        return super.atomic(ctx, ii);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    final SeqType s = expr.length == 1 ? expr[0].returned(ctx) : null;

    switch(func) {
      case FALSE:
      case TRUE:
        return atomic(ctx, input);
      case EMPTY:
      case EXISTS:
      case BOOLEAN:
        return expr[0].empty() || expr[0].item() ? atomic(ctx, input) : this;
      case NOT:
        if(expr[0].item()) return atomic(ctx, input);
        if(expr[0] instanceof Fun) {
          final Fun fs = (Fun) expr[0];
          if(fs.func == FunDef.EMPTY) {
            expr = fs.expr;
            func = FunDef.EXISTS;
          } else if(fs.func == FunDef.EXISTS) {
            expr = fs.expr;
            func = FunDef.EMPTY;
          }
        }
        return this;
      case ZEROORONE:
        return s.zeroOrOne() ? expr[0] : this;
      case EXACTLYONE:
        return s.one() ? expr[0] : this;
      case ONEORMORE:
        return !s.mayBeZero() ? expr[0] : this;
      case UNORDER:
        return expr[0];
      default:
        return this;
    }
  }

  /**
   * Checks items for deep equality.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deep(final QueryContext ctx) throws QueryException {
    if(expr.length == 3) checkColl(expr[2], ctx);
    return deep(input, ctx.iter(expr[0]), ctx.iter(expr[1]));
  }

  /**
   * Checks items for deep equality.
   * @param ii input info
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final InputInfo ii, final Iter iter1,
      final Iter iter2) throws QueryException {

    // [CG] check if namespaces references are correctly compared
    Item it1 = null;
    Item it2 = null;
    // explicit non-short-circuit..
    while((it1 = iter1.next()) != null & (it2 = iter2.next()) != null) {
      if(!it1.equiv(ii, it2)) return false;
      if(!it1.node() && !it2.node()) continue;

      // comparing nodes
      if(!(it1.node() && it2.node())) return false;
      final NodeIter niter1 = ((Nod) it1).descOrSelf();
      final NodeIter niter2 = ((Nod) it2).descOrSelf();

      Nod n1 = null, n2 = null;
      while(true) {
        n1 = niter1.next();
        n2 = niter2.next();
        if(n1 == null && n2 == null || n1 == null ^ n2 == null) break;
        if(n1.type != n2.type) return false;

        final QNm qn1 = n1.qname();
        if(qn1 != null && !qn1.eq(n2.qname())) return false;

        if(n1.type == Type.ATT || n1.type == Type.PI || n1.type == Type.COM ||
            n1.type == Type.TXT) {
          if(!Token.eq(n1.atom(), n2.atom())) return false;
          continue;
        }

        NodeIter att1 = n1.attr();
        int s1 = 0;
        while(att1.next() != null) s1++;
        NodeIter att2 = n2.attr();
        int s2 = 0;
        while(att2.next() != null) s2++;
        if(s1 != s2) return false;

        Nod a1 = null, a2 = null;
        att1 = n1.attr();
        while((a1 = att1.next()) != null) {
          att2 = n2.attr();
          boolean found = false;
          while((a2 = att2.next()) != null) {
            if(a1.qname().eq(a2.qname())) {
              found = Token.eq(a1.atom(), a2.atom());
              break;
            }
          }
          if(!found) return false;
        }
      }
      if(n1 != n2) return false;
    }
    return it1 == it2;
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    final Type t = expr.length == 1 ? expr[0].returned(ctx).type : null;
    if(func == FunDef.ZEROORONE)  return new SeqType(t, SeqType.Occ.ZO);
    if(func == FunDef.EXACTLYONE) return new SeqType(t, SeqType.Occ.O);
    if(func == FunDef.ONEORMORE)  return new SeqType(t, SeqType.Occ.OM);
    return super.returned(ctx);
  }
}
