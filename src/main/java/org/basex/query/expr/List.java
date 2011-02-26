package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Expression list.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  public List(final InputInfo ii, final Expr... l) {
    super(ii, l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].comp(ctx);
    checkUp(ctx, expr);

    if(values()) {
      // return simple sequence if all values are items or empty sequences
      final ItemCache ir = new ItemCache(expr.length);
      for(final Expr e : expr) ir.add(ctx.iter(e));
      return ir.finish();
    }

    // evaluate sequence type
    type = expr[0].type();
    for(int i = 1; i < expr.length; ++i) type = type.intersect(expr[i].type());
    final SeqType.Occ o = type.mayBeZero() ? SeqType.Occ.ZM : SeqType.Occ.OM;
    type = SeqType.get(type.type, o);

    // compute number of results
    size = 0;
    for(final Expr e : expr) {
      final long c = e.size();
      if(c == -1) {
        size = c;
        break;
      }
      size += c;
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      Iter ir;
      int e;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir == null) {
            if(e == expr.length) return null;
            ir = ctx.iter(expr[e++]);
          }
          final Item it = ir.next();
          if(it != null) return it;
          ir = null;
        }
      }
    };
  }

  @Override
  public boolean vacuous() {
    for(final Expr e : expr) if(!e.vacuous()) return false;
    return true;
  }

  @Override
  public String toString() {
    return new TokenBuilder(PAR1).addSep(expr, SEP).add(PAR2).toString();
  }
}
