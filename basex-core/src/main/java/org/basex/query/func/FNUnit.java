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
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNUnit(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _UNIT_ASSERT:        return assrt(qc);
      case _UNIT_ASSERT_EQUALS: return assertEquals(qc);
      case _UNIT_FAIL:          return fail(qc);
      default:                  return super.item(qc, ii);
    }
  }

  /**
   * Performs the assert function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assrt(final QueryContext qc) throws QueryException {
    final Item it = exprs.length < 2 ? null : checkItem(exprs[1], qc);
    if(exprs[0].ebv(qc, info).bool(info)) return null;
    throw error(it);
  }

  /**
   * Performs the assert-equals function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assertEquals(final QueryContext qc) throws QueryException {
    final Item it = exprs.length < 3 ? null : checkItem(exprs[2], qc);
    final Iter iter1 = qc.iter(exprs[0]), iter2 = qc.iter(exprs[1]);
    final DeepCompare comp = new DeepCompare(info);
    Item it1, it2;
    int c = 1;
    while(true) {
      it1 = iter1.next();
      it2 = iter2.next();
      final boolean empty1 = it1 == null, empty2 = it2 == null;
      if(empty1 && empty2) return null;
      if(empty1 || empty2 || !comp.equal(it1.iter(), it2.iter())) break;
      c++;
    }
    throw new UnitException(info, UNIT_ASSERT_EQUALS, it1, it2, c).value(it);
  }

  /**
   * Performs the fail function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item fail(final QueryContext qc) throws QueryException {
    throw error(exprs.length < 1 ? null : checkItem(exprs[0], qc));
  }

  /**
   * Returns an error with the specified item as value.
   * @param it item (may be {@code null})
   * @return error
   * @throws QueryException query exception
   */
  private QueryException error(final Item it) throws QueryException {
    return (it == null ? UNIT_ASSERT.get(info) : UNIT_MESSAGE.get(info, it.string(info))).value(it);
  }
}
