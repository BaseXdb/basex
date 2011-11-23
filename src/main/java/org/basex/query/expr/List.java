package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.AtomType;
import org.basex.query.item.Item;
import org.basex.query.item.IntSeq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.item.SeqType.Occ;
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
    for(int e = expr.length; --e >= 0;) expr[e] = expr[e].comp(ctx);
    checkUp(ctx, expr);

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

    // evaluate sequence type
    type = SeqType.EMP;
    boolean v = true;
    for(final Expr e : expr) {
      // check if all expressions are values
      v &= e.isValue();
      // skip expression that will not add any results
      if(e.size() == 0) continue;
      // evaluate sequence type
      final SeqType et = e.type();
      type = type == SeqType.EMP ? et :
        SeqType.get(et.type == type.type ? et.type : AtomType.ITEM,
            et.mayBeZero() && type.mayBeZero() ? Occ.ZM : Occ.OM);
    }

    // return cached integer sequence, cached values or self reference
    return v ? type.type.instanceOf(AtomType.ITR) ?
        optPre(IntSeq.get(expr, size, type.type), ctx) :
        optPre(value(ctx), ctx) : this;
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
  public Value value(final QueryContext ctx) throws QueryException {
    final ItemCache ic = new ItemCache();
    for(final Expr e : expr) ic.add(ctx.value(e));
    return ic.value();
  }

  @Override
  public boolean isVacuous() {
    for(final Expr e : expr) if(!e.isVacuous()) return false;
    return true;
  }

  @Override
  public String toString() {
    return new TokenBuilder(PAR1).addSep(expr, SEP).add(PAR2).toString();
  }
}
