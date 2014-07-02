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
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNUnit(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
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
    final byte[] str = exprs.length < 2 ? null : checkStr(exprs[1], ctx);
    if(exprs[0].ebv(ctx, info).bool(info)) return null;
    throw str == null ? UNIT_ASSERT.get(info) : UNIT_MESSAGE.get(info, str);
  }

  /**
   * Performs the assert-equals function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assertEquals(final QueryContext ctx) throws QueryException {
    final byte[] str = exprs.length < 3 ? null : checkStr(exprs[2], ctx);
    final Iter iter1 = ctx.iter(exprs[0]), iter2 = ctx.iter(exprs[1]);
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
    throw UNIT_MESSAGE.get(info, checkStr(exprs[0], ctx));
  }
}
