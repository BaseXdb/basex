package org.basex.query.func.array;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on arrays.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNArray extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _ARRAY_HEAD:       return head(qc).iter();
      case _ARRAY_FOLD_LEFT:  return foldLeft(qc).iter();
      case _ARRAY_FOLD_RIGHT: return foldRight(qc).iter();
      default:                return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _ARRAY_HEAD:       return head(qc);
      case _ARRAY_FOLD_LEFT:  return foldLeft(qc);
      case _ARRAY_FOLD_RIGHT: return foldRight(qc);
      default:                return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _ARRAY_SIZE:            return Int.get(toArray(exprs[0], qc).arraySize());
      case _ARRAY_TAIL:            return tail(qc);
      case _ARRAY_APPEND:          return append(qc);
      case _ARRAY_SERIALIZE:       return Str.get(toArray(exprs[0], qc).serialize(info));
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
    final Array array = toArray(exprs[0], qc);
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
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc), true);
    final int l = exprs.length > 2 ? (int) toLong(exprs[2], qc) : array.arraySize() - p;
    if(l < 0) throw ARRAYNEG_X.get(info, l);
    checkPos(array, p + 1 + l, true);
    return Array.get(array, p, l);
  }

  /**
   * Removes an array entry.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array remove(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc));
    final int as = array.arraySize();
    if(p == 0) return Array.get(array, 1, as - 1);
    if(p + 1 == as) return Array.get(array, 0, as - 1);
    final ValueList vl = new ValueList(as - 1);
    for(int a = 0; a < as; a++) if(a != p) vl.add(array.get(a));
    return vl.array();
  }

  /**
   * Inserts an array entry before the specified position.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array insertBefore(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final int p = checkPos(array, toLong(exprs[1], qc), true);
    final Value ins = qc.value(exprs[2]);
    final int as = array.arraySize();
    final ValueList vl = new ValueList(as + 1);
    for(int a = 0; a < as; a++) {
      if(a == p) vl.add(ins);
      vl.add(array.get(a));
    }
    if(p == as) vl.add(ins);
    return vl.array();
  }

  /**
   * Returns the first array member.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value head(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    return array.get(checkPos(array, 1));
  }

  /**
   * Returns all array members after the first.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Array tail(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    return Array.get(array, checkPos(array, 1) + 1, array.arraySize() - 1);
  }

  /**
   * Reverses the members of an array.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array reverse(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
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
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
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
    final Array array1 = toArray(exprs[0], qc), array2 = toArray(exprs[1], qc);
    final FItem fun = checkArity(exprs[2], 2, qc);
    final int as = Math.min(array1.arraySize(), array2.arraySize());
    final ValueList vl = new ValueList(as);
    for(int a = 0; a < as; a++) vl.add(fun.invokeValue(qc, info, array1.get(a), array2.get(a)));
    return vl.array();
  }

  /**
   * Applies a filter function to all members of an array.
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private Array filter(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    final ValueList vl = new ValueList();
    for(final Value v : array.members()) {
      if(toBoolean(fun.invokeItem(qc, info, v))) vl.add(v);
    }
    return vl.array();
  }

  /**
   * Folds a sequence into a return value, starting from the left.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value foldLeft(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    Value res = qc.value(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);
    for(final Value v : array.members()) res = fun.invokeValue(qc, info, res, v);
    return res;
  }

  /**
   * Folds a sequence into a return value, starting from the right.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private Value foldRight(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    Value res = qc.value(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);
    for(int a = array.arraySize(); --a >= 0;) res = fun.invokeValue(qc, info, array.get(a), res);
    return res;
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
      for(final Value v : toArray(it).members()) vl.add(v);
    }
    return vl.array();
  }

  /**
   * Checks if a position is within the range of an array.
   * @param array array
   * @param pos position
   * @return specified position -1
   * @throws QueryException query exception
   */
  private int checkPos(final Array array, final long pos) throws QueryException {
    return checkPos(array, pos, false);
  }

  /**
   * Checks if a position is within the range of an array.
   * @param array array
   * @param pos position
   * @param incl include last entry
   * @return specified position -1
   * @throws QueryException query exception
   */
  private int checkPos(final Array array, final long pos, final boolean incl)
      throws QueryException {
    final int as = array.arraySize() + (incl ? 1 : 0);
    if(pos < 1 || pos > as) throw ARRAYBOUNDS_X_X.get(info, pos, as);
    return (int) pos - 1;
  }
}
