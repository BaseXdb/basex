package org.basex.query.value.array;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An array storing {@link Value}s.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public abstract class Array extends FItem {
  /** Minimum size of a leaf. */
  static final int MIN_LEAF = 8;
  /** Maximum size of a leaf. */
  static final int MAX_LEAF = 2 * MIN_LEAF - 1;
  /** Minimum number of elements in a digit. */
  static final int MIN_DIGIT = MIN_LEAF / 2;
  /** Maximum number of elements in a digit. */
  static final int MAX_DIGIT = MAX_LEAF + MIN_DIGIT;
  /** Maximum size of a small array. */
  static final int MAX_SMALL = 2 * MIN_DIGIT - 1;

  /**
   * Default constructor.
   */
  Array() {
    super(SeqType.ANY_ARRAY, new AnnList());
  }

  /**
   * The empty array.
   * Running time: <i>O(1)</i> and no allocation
   * @return (unique) instance of an empty array
   */
  public static Array empty() {
    return EmptyArray.INSTANCE;
  }

  /**
   * Creates a singleton array containing the given element.
   * @param elem the contained element
   * @return the singleton array
   */
  public static Array singleton(final Value elem) {
    return new SmallArray(new Value[] { elem });
  }

  /**
   * Creates an array containing the given elements.
   * @param values elements
   * @return the resulting array
   */
  @SafeVarargs
  public static Array from(final Value... values) {
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value value : values) builder.append(value);
    return builder.freeze();
  }

  /**
   * Prepends an element to the front of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to prepend
   * @return resulting array
   */
  public abstract Array cons(Value elem);

  /**
   * Appends an element to the back of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to append
   * @return resulting array
   */
  public abstract Array snoc(Value elem);

  /**
   * Gets the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param index index of the element to get
   * @return the corresponding element
   */
  public abstract Value get(long index);

  /**
   * Returns a copy of this array where the entry at the given position is
   * replaced by the given value.
   * @param pos position of the entry to replace
   * @param val value to put into this array
   * @return resulting array
   */
  public abstract Array put(long pos, Value val);

  /**
   * Returns the number of elements in this array.
   * Running time: <i>O(1)</i>
   * @return number of elements
   */
  public abstract long arraySize();

  /**
   * Concatenates this array with another one.
   * Running time: <i>O(log (min { this.arraySize(), other.arraySize() }))</i>
   * @param other array to append to the end of this array
   * @return resulting array
   */
  public abstract Array concat(Array other);

  /**
   * First element of this array, equivalent to {@code array.get(0)}.
   * Running time: <i>O(1)</i>
   * @return the first element
   */
  public abstract Value head();

  /**
   * Last element of this array, equivalent to {@code array.get(array.arraySize() - 1)}.
   * Running time: <i>O(1)</i>
   * @return last element
   */
  public abstract Value last();

  /**
   * Initial segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the last one.
   * Running time: <i>O(1)*</i>
   * @return initial segment
   */
  public abstract Array init();

  /**
   * Tail segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the first one.
   * Running time: <i>O(1)*</i>
   * @return tail segment
   */
  public abstract Array tail();

  /**
   * Extracts a contiguous part of this array.
   * @param pos position of first element
   * @param len number of elements
   * @param qc query context
   * @return the sub-array
   */
  public abstract Array subArray(long pos, long len, QueryContext qc);

  /**
   * Returns an array with the same elements as this one, but their order reversed.
   * Running time: <i>O(n)</i>
   * @param qc query context
   * @return reversed version of this array
   */
  public abstract Array reverseArray(QueryContext qc);

  /**
   * Checks if this array is empty.
   * Running time: <i>O(1)</i>
   * @return {@code true} if the array is empty, {@code false} otherwise
   */
  public abstract boolean isEmptyArray();

  /**
   * Inserts the given element at the given position into this array.
   * Running time: <i>O(log n)</i>
   * @param pos insertion position, must be between {@code 0} and {@code this.arraySize()}
   * @param value element to insert
   * @param qc query context
   * @return resulting array
   */
  public abstract Array insertBefore(long pos, Value value, QueryContext qc);

  /**
   * Removes the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param pos deletion position, must be between {@code 0} and {@code this.arraySize() - 1}
   * @param qc query context
   * @return resulting array
   */
  public abstract Array remove(long pos, QueryContext qc);

  @Override
  public final void materialize(final InputInfo info) throws QueryException {
    for(final Value value : members()) value.materialize(info);
  }

  /**
   * Iterator over the members of this array.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array members
   */
  public abstract ListIterator<Value> iterator(long start);

  /** Iterable over the elements of this array. */
  private Iterable<Value> iterable;

  /**
   * Iterator over the members of this array.
   * @return array over the array members
   */
  public final Iterable<Value> members() {
    if(iterable == null) iterable = () -> iterator(0);
    return iterable;
  }

  /**
   * Prepends the given elements to this array.
   * @param values values, with length at most {@link Array#MAX_SMALL}
   * @return resulting array
   */
  abstract Array consSmall(Value[] values);

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param values input values
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  static Value[] slice(final Value[] values, final int from, final int to) {
    final Value[] out = new Value[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, values.length);
    final int out0 = Math.max(-from, 0);
    System.arraycopy(values, in0, out, out0, in1 - in0);
    return out;
  }

  /**
   * Concatenates the two int arrays.
   * @param values1 first values
   * @param values2 second values
   * @return resulting array
   */
  static Value[] concat(final Value[] values1, final Value[] values2) {
    final int l = values1.length, r = values2.length, n = l + r;
    final Value[] out = new Value[n];
    System.arraycopy(values1, 0, out, 0, l);
    System.arraycopy(values2, 0, out, l, r);
    return out;
  }

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();

  @Override
  public final Value invValue(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, info);
    if(key == null) throw EMPTYFOUND_X.get(info, AtomType.ITR);
    return get(key, info);
  }

  @Override
  public final Item invItem(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    return invValue(qc, info, args).item(qc, info);
  }

  /**
   * Gets the value from this array.
   * @param key key to look for (must be an integer)
   * @param info input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public final Value get(final Item key, final InputInfo info) throws QueryException {
    if(!key.type.instanceOf(AtomType.ITR) && !key.type.isUntyped())
      throw typeError(key, AtomType.ITR, info);

    final long pos = key.itr(info), size = arraySize();
    if(pos > 0 && pos <= size) return get(pos - 1);
    throw (size == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(info, pos, size);
  }

  @Override
  public final int stackFrameSize() {
    return 0;
  }

  @Override
  public final int arity() {
    return 1;
  }

  @Override
  public final QNm funcName() {
    return null;
  }

  @Override
  public final QNm paramName(final int pos) {
    return new QNm("pos", "");
  }

  @Override
  public final FuncType funcType() {
    return ArrayType.get(SeqType.ITEM_ZM);
  }

  @Override
  public final Expr inlineExpr(final Expr[] exprs, final CompileContext cc, final InputInfo info) {
    return null;
  }

  @Override
  public final boolean isVacuousBody() {
    return false;
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo info) throws QueryException {
    if(arraySize() == 1) return get(0).atomValue(qc, info);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value value : members()) vb.add(value.atomValue(qc, info));
    return vb.value();
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo info) throws QueryException {
    if(atomSize() > 1) throw SEQFOUND_X.get(info, this);
    final Value value = atomValue(qc, info);
    return value.isEmpty() ? null : (Item) value;
  }

  @Override
  public final long atomSize() {
    long size = 0;
    for(final Value value : members()) {
      for(final Item item : value) size += item.atomSize();
    }
    return size;
  }

  /**
   * Returns a string representation of the array.
   * @param indent indent output
   * @param tb token builder
   * @param level current level
   * @param info input info
   * @throws QueryException query exception
   */
  public final void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo info) throws QueryException {

    tb.add('[');
    int c = 0;
    for(final Value value : members()) {
      if(c++ > 0) {
        tb.add(',');
        if(indent) tb.add(' ');
      }
      final long vs = value.size();
      if(vs != 1) tb.add('(');
      int cc = 0;
      for(int i = 0; i < vs; i++) {
        if(cc++ > 0) {
          tb.add(',');
          if(indent) tb.add(' ');
        }
        final Item item = value.itemAt(i);
        if(item instanceof Array) ((Array) item).string(indent, tb, level, info);
        else if(item instanceof Map) ((Map) item).string(indent, tb, level + 1, info);
        else tb.add(item.toString());
      }
      if(vs != 1) tb.add(')');
    }
    tb.add(']');
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    return tp == AtomType.ITEM || tp instanceof FuncType && instanceOf((FuncType) tp, false);
  }

  @Override
  public final FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo info,
      final boolean opt) throws QueryException {

    if(instanceOf(ft, true)) return this;
    throw typeError(this, ft, info);
  }

  /**
   * Checks if this is an instance of the specified type.
   * @param ft type
   * @param coerce coerce value
   * @return result of check
   */
  private boolean instanceOf(final FuncType ft, final boolean coerce) {
    if(ft instanceof MapType) return false;
    if(type.instanceOf(ft)) return true;

    final SeqType[] at = ft.argTypes;
    if(at != null && (at.length != 1 || !at[0].instanceOf(SeqType.ITR_O))) return false;

    final SeqType ret = ft.declType;
    if(ft instanceof ArrayType) {
      // no argument and return type: no check required
      if(ret.eq(SeqType.ITEM_ZM)) return true;
      // check types of members
      for(final Value value : members()) {
        if(!ret.instance(value)) return false;
      }
      return true;
    }
    // allow coercion
    return coerce || ret.eq(SeqType.ITEM_ZM);
  }

  @Override
  public final boolean deep(final Item item, final InputInfo info, final Collation coll)
      throws QueryException {

    if(item instanceof Array) {
      final Array o = (Array) item;
      if(arraySize() != o.arraySize()) return false;
      final Iterator<Value> iter1 = iterator(0), iter2 = o.iterator(0);
      while(iter1.hasNext()) {
        final Value value1 = iter1.next(), value2 = iter2.next();
        if(value1.size() != value2.size() ||
            !new DeepEqual(info).collation(coll).equal(value1, value2)) return false;
      }
      return true;
    }
    return item instanceof FItem && !(item instanceof Map) && super.deep(item, info, coll);
  }

  @Override
  public final String description() {
    return ARRAY;
  }

  @Override
  public boolean equals(final Object obj) {
    // [CG] could be enhanced
    return this == obj;
  }

  @Override
  public final void plan(final FElem plan) {
    final long size = arraySize();
    final FElem elem = planElem(ENTRIES, size, TYPE, seqType());
    final int max = (int) Math.min(size, 5);
    for(int i = 0; i < max; i++) get(i).plan(elem);
    addPlan(plan, elem);
  }

  @Override
  public final Object[] toJava() throws QueryException {
    final long size = arraySize();
    final Object[] tmp = new Object[(int) size];
    final Iterator<Value> iter = iterator(0);
    for(int i = 0; iter.hasNext(); i++) tmp[i] = iter.next().toJava();
    return tmp;
  }

  @Override
  public final String toString() {
    if(arraySize() == 0) return "[]";

    final TokenBuilder tb = new TokenBuilder().add('[');
    final Iterator<Value> iter = iterator(0);
    for(boolean fst = true; iter.hasNext(); fst = false) {
      tb.add(fst ? " " : ", ");
      final Value value = iter.next();
      final long vs = value.size();
      if(vs != 1) tb.add('(');
      for(int i = 0; i < vs; i++) {
        if(i != 0) tb.add(", ");
        tb.addExt(value.itemAt(i));
      }
      if(vs != 1) tb.add(')');
    }
    return tb.add(" ]").toString();
  }
}
