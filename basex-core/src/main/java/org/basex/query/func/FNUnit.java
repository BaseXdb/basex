package org.basex.query.func;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.unit.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Unit functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNUnit extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNUnit(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _UNIT_ASSERT:        return assrt(ctx);
      case _UNIT_ASSERT_EQUALS: return assertEquals(ctx);
      case _UNIT_FAIL:          return fail(ctx);
      default:                  return super.item(ctx, ii);
    }
  }

  /**
   * Performs the assert function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assrt(final QueryContext ctx) throws QueryException {
    final byte[] str = expr.length < 2 ? null : checkStr(expr[1], ctx);
    if(expr[0].ebv(ctx, info).bool(info)) return null;
    throw str == null ? UNIT_ASSERT.get(info) : UNIT_MESSAGE.get(info, str);
  }

  /**
   * Performs the assert-equals function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assertEquals(final QueryContext ctx) throws QueryException {
    final byte[] str = expr.length < 3 ? null : checkStr(expr[2], ctx);
    final Iter iter1 = ctx.iter(expr[0]), iter2 = ctx.iter(expr[1]);
    final Compare comp = new Compare(info);
    Item it1, it2;
    int c = 1;
    while(true) {
      it1 = iter1.next();
      it2 = iter2.next();
      final boolean empty1 = it1 == null, empty2 = it2 == null;
      if(empty1 && empty2) return null;
      if(empty1 || empty2 || !comp.deep(it1.iter(), it2.iter())) break;
      c++;
    }
    if(str != null) throw UNIT_MESSAGE.get(info, str);
    throw new UnitException(info, UNIT_ASSERT_EQUALS, it1, it2, c);
  }

  /**
   * Performs the fail function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item fail(final QueryContext ctx) throws QueryException {
    throw UNIT_MESSAGE.get(info, checkStr(expr[0], ctx));
  }
}
