package org.basex.query.func;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Functions on arrays.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FNArray extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNArray(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _ARRAY_HEAD:       return head(qc).iter();
      case _ARRAY_TAIL:       return tail(qc).iter();
      case _ARRAY_FOLD_LEFT:  return foldLeft(qc).iter();
      case _ARRAY_FOLD_RIGHT: return foldRight(qc).iter();
      default:                return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _ARRAY_HEAD:       return head(qc);
      case _ARRAY_TAIL:       return tail(qc);
      case _ARRAY_FOLD_LEFT:  return foldLeft(qc);
      case _ARRAY_FOLD_RIGHT: return foldRight(qc);
      default:                return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _ARRAY_SIZE:            return Int.get(array(0, qc).arraySize());
      case _ARRAY_APPEND:          return append(qc);
      case _ARRAY_SERIALIZE:       return Str.get(array(0, qc).serialize(info));
      case _ARRAY_SUBARRAY:        return subarray(qc);
      case _ARRAY_REMOVE:          return remove(qc);
      case _ARRAY_INSERT_BEFORE:   return insertBefore(qc);
      case _ARRAY_JOIN:            return join(qc);
      case _ARRAY_REVERSE:         return reverse(qc);
      case _ARRAY_FOR_EACH_MEMBER: return forEachMember(qc);
      case _ARRAY_FILTER:          return filter(qc);
      case _ARRAY_FOR_EACH_PAIR:   return forEachPair(qc);
      default:                     return super.item(qc, ii);
    }
  }

  /**
   * Appends a value to an array.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array append(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int as = array.arraySize();
    final ValueList vl = new ValueList(as + 1);
    for(final Value v : array.members()) vl.add(v);
    return vl.add(qc.value(exprs[1])).array();
  }

  /**
   * Returns a sub-array.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array subarray(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int s = checkIndex(array, checkItr(exprs[1], qc), true);
    final int l = exprs.length > 2 ? (int) checkItr(exprs[2], qc) : array.arraySize() - s;
    if(l < 0) throw ARRAYNEG.get(info, l);
    checkIndex(array, s + 1 + l, true);
    return Array.get(array, s, l);
  }

  /**
   * Removes an array entry.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array remove(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int i = checkIndex(array, checkItr(exprs[1], qc));
    final int as = array.arraySize();
    if(i == 0) return Array.get(array, 1, as - 1);
    if(i + 1 == as) return Array.get(array, 0, as - 1);

    final ValueList vl = new ValueList(as - 1);
    for(int a = 0; a < as; a++) if(a != i) vl.add(array.get(a));
    return vl.array();
  }

  /**
   * Inserts an array entry before the specified position.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array insertBefore(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int s = checkIndex(array, checkItr(exprs[1], qc), true);
    final Value ins = qc.value(exprs[2]);

    final int as = array.arraySize();
    final ValueList vl = new ValueList(as + 1);
    for(int a = 0; a < as; a++) {
      if(a == s) vl.add(ins);
      vl.add(array.get(a));
    }
    if(s == as) vl.add(ins);
    return vl.array();
  }

  /**
   * Returns the first array member.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value head(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    return array.get(checkIndex(array, 1));
  }

  /**
   * Returns the first array member.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value tail(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int s = checkIndex(array, 1) + 1;
    return Array.get(array, s, array.arraySize() - 1);
  }

  /**
   * Reverses the members of an array.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array reverse(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final int as = array.arraySize();
    final ValueList vl = new ValueList(as);
    for(int a = as - 1; a >= 0; a--) vl.add(array.get(a));
    return vl.array();
  }

  /**
   * Applies a function to all members of an array.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array forEachMember(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final FItem fun = checkArity(1, 1, qc);
    final ValueList vl = new ValueList(array.arraySize());
    for(final Value v : array.members()) vl.add(fun.invokeValue(qc, info, v));
    return vl.array();
  }

  /**
   * Applies a function to all members of two arrays.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array forEachPair(final QueryContext qc) throws QueryException {
    final Array array1 = array(0, qc);
    final Array array2 = array(1, qc);
    final FItem fun = checkArity(2, 2, qc);
    final int as = Math.min(array1.arraySize(), array2.arraySize());
    final ValueList vl = new ValueList(as);
    for(int a = 0; a < as; a++) {
      vl.add(fun.invokeValue(qc, info, array1.get(a), array2.get(a)));
    }
    return vl.array();
  }

  /**
   * Applies a filter function to all members of an array.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array filter(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    final FItem fun = checkArity(1, 1, qc);
    final ValueList vl = new ValueList();
    for(final Value v : array.members()) {
      if(checkBln(fun.invokeItem(qc, info, v))) vl.add(v);
    }
    return vl.array();
  }

  /**
   * Folds a sequence into a return value, starting from the left.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Value foldLeft(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    Value r = qc.value(exprs[1]);
    final FItem fun = checkArity(2, 2, qc);
    for(final Value v : array.members()) r = fun.invokeValue(qc, info, r, v);
    return r;
  }

  /**
   * Folds a sequence into a return value, starting from the right.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Value foldRight(final QueryContext qc) throws QueryException {
    final Array array = array(0, qc);
    Value r = qc.value(exprs[1]);
    final FItem fun = checkArity(2, 2, qc);
    for(int i = array.arraySize(); --i >= 0;) r = fun.invokeValue(qc, info, array.get(i), r);
    return r;
  }

  /**
   * Joins arrays.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array join(final QueryContext qc) throws QueryException {
    final ValueList vl = new ValueList();
    final Iter ir = qc.iter(exprs[0]);
    for(Item it; (it = ir.next()) != null;) {
      for(final Value v : checkArray(it).members()) vl.add(v);
    }
    return vl.array();
  }

  /**
   * Gets the array at the first argument position.
   * @param index index
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array array(final int index, final QueryContext qc) throws QueryException {
    return checkArray(checkItem(exprs[index], qc, SeqType.ANY_ARRAY));
  }

  /**
   * Checks if an index is within the range of an array.
   * @param array array
   * @param index index
   * @return specified index as integer -1
   * @throws QueryException query exception
   */
  private int checkIndex(final Array array, final long index) throws QueryException {
    return checkIndex(array, index, false);
  }

  /**
   * Checks if an index is within the range of an array.
   * @param array array
   * @param index index
   * @param incl include last entry
   * @return specified index as integer -1
   * @throws QueryException query exception
   */
  private int checkIndex(final Array array, final long index, final boolean incl)
      throws QueryException {
    final int as = array.arraySize() + (incl ? 1 : 0);
    if(index < 1 || index > as) throw ARRAYBOUNDS.get(info, index, as);
    return (int) index - 1;
  }
}
