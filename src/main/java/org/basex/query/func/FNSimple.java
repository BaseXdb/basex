package org.basex.query.func;

import static org.basex.query.util.Err.*;
import java.util.Stack;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ItemIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Simple functions.
 *
 * @author BaseX Team 2005-11, ISC License
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
    Iter ir = ctx.iter(expr[0]);
    switch(def) {
      case ONEORMORE:
        if(expr[0].type().mayBeZero()) ir = ItemIter.get(ir);
        if(ir.size() < 1) EXPECTOM.thrw(input);
        return ir;
      case UNORDER:
        return ir;
      default:
        return super.iter(ctx);
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
      case DEEPEQUAL:
        return Bln.get(deep(ctx));
      case ZEROORONE:
        Iter ir = e.iter(ctx);
        Item it = ir.next();
        if(it != null && ir.next() != null) EXPECTZ0.thrw(input);
        return it;
      case EXACTLYONE:
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
      case BOOLEAN:
        expr[0] = e.compEbv(ctx);
        return expr[0].type().eq(SeqType.BLN) ? e : this;
      case NOT:
        if(e.isFun(FunDef.EMPTY)) {
          // simplify: not(empty(A)) -> exists(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((Fun) e).expr;
          def = FunDef.EXISTS;
        } else if(e.isFun(FunDef.EXISTS)) {
          // simplify: not(exists(A)) -> empty(A)
          ctx.compInfo(QueryText.OPTWRITE, this);
          expr = ((Fun) e).expr;
          def = FunDef.EMPTY;
        } else {
          // simplify: not(boolean(A)) -> not(A)
          expr[0] = e.compEbv(ctx);
        }
        return this;
      case ZEROORONE:
        type = SeqType.get(e.type().type, SeqType.Occ.ZO);
        return e.type().zeroOrOne() ? e : this;
      case EXACTLYONE:
        type = SeqType.get(e.type().type, SeqType.Occ.O);
        return e.type().one() ? e : this;
      case ONEORMORE:
        type = SeqType.get(e.type().type, SeqType.Occ.OM);
        return !e.type().mayBeZero() ? e : this;
      case UNORDER:
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
    if(def == FunDef.BOOLEAN) {
      // (test)[boolean(A)] -> (test)[A]
      if(!e.type().mayBeNum()) ex = e;
    } else if(def == FunDef.EXISTS) {
      // if(exists(node*)) -> if(node*)
      if(e.type().type.node()) ex = e;
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

    Item it1 = null;
    Item it2 = null;
    // explicit non-short-circuit..
    while((it1 = iter1.next()) != null & (it2 = iter2.next()) != null) {
      // check atomic values
      if(!it1.node() && !it2.node()) {
        if(!it1.equiv(ii, it2)) return false;
        continue;
      }

      // node types must be equal
      if(it1.type != it2.type) return false;

      Nod s1 = (Nod) it1, s2 = (Nod) it2;
      final Stack<NodeIter[]> chld = new Stack<NodeIter[]>();
      NodeIter[] ch = { s1.child(), s2.child() };
      chld.push(ch);
      boolean desc = false;
      do {
        final Type t1 = s1 != null ? s1.type : null;
        final Type t2 = s2 != null ? s2.type : null;

        if(desc) {
          // skip descendant comments and processing instructions
          if(t1 == Type.PI || t1 == Type.COM) {
            s1 = ch[0].next();
            continue;
          }
          if(t2 == Type.PI || t2 == Type.COM) {
            s2 = ch[1].next();
            continue;
          }
        }

        if(s1 == null || s2 == null) {
          if(s1 != s2) return false;
          ch = chld.pop();
        } else {
          // compare names
          final QNm n1 = s1.qname(), n2 = s2.qname();
          if(n1 != null && n2 != null && !n1.eq(n2))
            return false;

          // compare string values
          if((t1 == Type.TXT || t1 == Type.ATT || t1 == Type.COM ||
              t1 == Type.PI) && !Token.eq(s1.atom(), s2.atom())) return false;

          // compare elements
          if(t1 == Type.ELM) {
            // compare number of attributes
            if(s1.attr().finish().size() != s2.attr().finish().size())
              return false;

            // compare attributes names and values
            Nod a1 = null, a2 = null;
            final NodeIter att1 = s1.attr();
            while((a1 = att1.next()) != null) {
              final NodeIter att2 = s2.attr();
              boolean f = false;
              while((a2 = att2.next()) != null) {
                if(a1.qname().eq(a2.qname())) {
                  f = Token.eq(a1.atom(), a2.atom());
                  break;
                }
              }
              if(!f) return false;
            }

            // check children
            chld.push(ch);
            ch = new NodeIter[] { s1.child(), s2.child() };
          }
        }

        // check next child
        s1 = ch[0].next();
        s2 = ch[1].next();
        desc = true;
      } while(!chld.isEmpty());
    }
    return it1 == it2;
  }
}
