package org.basex.query.value.array;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * An array storing {@link Value}s.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public abstract class XQArray extends XQData {
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
   * @param type function type
   */
  XQArray(final Type type) {
    super(type);
  }

  /**
   * The empty array.
   * Running time: <i>O(1)</i> and no allocation
   * @return (unique) instance of an empty array
   */
  public static XQArray empty() {
    return EmptyArray.EMPTY;
  }

  /**
   * Creates an array with a single member.
   * @param value single member
   * @return array
   */
  public static XQArray singleton(final Value value) {
    return new SingletonArray(value);
  }

  /**
   * Prepends a member to the front of this array.
   * Running time: <i>O(1)*</i>
   * @param head value to prepend
   * @return resulting array
   */
  public abstract XQArray prepend(Value head);

  /**
   * Appends a member to the end of this array.
   * Running time: <i>O(1)*</i>
   * @param last value to append
   * @return resulting array
   */
  public abstract XQArray append(Value last);

  /**
   * Gets the member at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param index index of the member to get
   * @return the corresponding member
   */
  public abstract Value get(long index);

  /**
   * Returns a copy of this array where the member at the given position is
   * replaced by the given value.
   * @param pos position of the member to replace
   * @param value value to put into this array
   * @return resulting array
   */
  public abstract XQArray put(long pos, Value value);

  /**
   * Returns the number of members in this array.
   * Running time: <i>O(1)</i>
   * @return number of members
   */
  public abstract long arraySize();

  /**
   * Concatenates this array with another one.
   * Running time: <i>O(log (min { this.arraySize(), other.arraySize() }))</i>
   * @param other array to append to the end of this array
   * @return resulting array
   */
  public abstract XQArray concat(XQArray other);

  /**
   * Returns the first member of this array.
   * Running time: <i>O(1)</i>
   * @return first member
   */
  public abstract Value head();

  /**
   * Returns the last member of this array.
   * Running time: <i>O(1)</i>
   * @return last member
   */
  public abstract Value foot();

  /**
   * Returns the array without the last member.
   * Running time: <i>O(1)*</i>
   * @return new array
   */
  public abstract XQArray trunk();

  /**
   * Returns the array without the first member.
   * same order), except for the first one.
   * Running time: <i>O(1)*</i>
   * @return new array
   */
  public abstract XQArray tail();

  /**
   * Extracts a contiguous part of this array.
   * @param pos position of first member
   * @param length number of member
   * @param qc query context
   * @return the sub-array
   */
  public abstract XQArray subArray(long pos, long length, QueryContext qc);

  /**
   * Returns an array with the same members as this one, but their order reversed.
   * Running time: <i>O(n)</i>
   * @param qc query context
   * @return reversed version of this array
   */
  public abstract XQArray reverseArray(QueryContext qc);

  /**
   * Inserts the given member at the given position into this array.
   * Running time: <i>O(log n)</i>
   * @param pos insertion position, must be between {@code 0} and {@code arraySize()}
   * @param value member to insert
   * @param qc query context
   * @return resulting array
   */
  public abstract XQArray insertBefore(long pos, Value value, QueryContext qc);

  /**
   * Removes the member at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param pos deletion position, must be between {@code 0} and {@code arraySize() - 1}
   * @param qc query context
   * @return resulting array
   */
  public abstract XQArray remove(long pos, QueryContext qc);

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeLong(arraySize());
    for(final Value member : members()) {
      Store.write(out, member);
    }
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Value value : members()) value.cache(lazy, ii);
  }

  /**
   * Iterator over the members of this array.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array members
   */
  public abstract ListIterator<Value> iterator(long start);

  /** Iterable over the members of this array. */
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
   * Prepends the given array to this array.
   * @param array small array
   * @return resulting array
   */
  abstract XQArray prepend(SmallArray array);

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param values input values
   * @param from first index, inclusive (can be negative)
   * @param to last index, exclusive (can be greater than {@code arr.length})
   * @return resulting array
   */
  static Value[] slice(final Value[] values, final int from, final int to) {
    final Value[] out = new Value[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, values.length);
    final int out0 = Math.max(-from, 0);
    Array.copy(values, in0, in1 - in0, out, out0);
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
    Array.copy(values1, l, out);
    Array.copyFromStart(values2, r, out, l);
    return out;
  }

  /**
   * Creates a new array type.
   * @param value value to be added
   * @return union type
   */
  final Type union(final Value value) {
    final SeqType mt = ((ArrayType) type).memberType, st = value.seqType();
    return mt.eq(st) ? type : ArrayType.get(mt.union(st));
  }

  /**
   * Checks that this array implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();

  @Override
  public final Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return getInternal(key(args[0], qc, ii), qc, ii, true);
  }

  /**
   * Gets the internal map value.
   * @param key key to look for
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @param error if {@code true}, raise error if index is out of bounds
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  public Value getInternal(final Item key, final QueryContext qc, final InputInfo ii,
      final boolean error) throws QueryException {
    final Item ki = (Item) SeqType.INTEGER_O.coerce(key, null, qc, null, ii);

    final long pos = ki.itr(ii), size = arraySize();
    if(pos > 0 && pos <= size) return get(pos - 1);

    if(error) throw (size == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(ii, pos, size);
    return null;
  }

  @Override
  public final QNm paramName(final int pos) {
    return new QNm("pos", "");
  }

  @Override
  public void refineType(final Expr expr) {
    final Type tp = type.intersect(expr.seqType().type);
    if(tp != null) type = tp;
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(arraySize() == 1) return get(0).atomValue(qc, ii);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value value : members()) vb.add(value.atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return atomValue(qc, ii).item(qc, ii);
  }

  @Override
  public final void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo ii) throws QueryException {

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
        if(item instanceof XQArray) ((XQArray) item).string(indent, tb, level, ii);
        else if(item instanceof XQMap) ((XQMap) item).string(indent, tb, level + 1, ii);
        else tb.add(item.toString());
      }
      if(vs != 1) tb.add(')');
    }
    tb.add(']');
  }

  @Override
  public final Item materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) throws QueryException {

    if(materialized(test, ii)) return this;

    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : members()) {
      qc.checkStop();
      ab.append(value.materialize(test, ii, qc));
    }
    return ab.array(this);
  }

  @Override
  public final boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    if(!funcType().declType.type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      for(final Value value : members()) {
        if(!value.materialized(test, ii)) return false;
      }
    }
    return true;
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    if(type.instanceOf(tp)) return true;

    final SeqType mt;
    if(tp instanceof ArrayType) {
      mt = ((ArrayType) tp).memberType;
    } else if(tp instanceof FuncType) {
      final FuncType ft = (FuncType) tp;
      if(ft.argTypes.length != 1 || !ft.argTypes[0].instanceOf(SeqType.INTEGER_O)) return false;
      mt = ft.declType;
    } else {
      return false;
    }

    if(mt.eq(SeqType.ITEM_ZM)) return true;

    // check types of members
    for(final Value value : members()) {
      if(!mt.instance(value)) return false;
    }
    return true;
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(this == item) return true;
    if(item instanceof XQArray) {
      final XQArray array = (XQArray) item;
      if(arraySize() != array.arraySize()) return false;
      final Iterator<Value> iter1 = iterator(0), iter2 = array.iterator(0);
      while(iter1.hasNext()) {
        final Value value1 = iter1.next(), value2 = iter2.next();
        if(!(deep != null ? deep.equal(value1, value2) : value1.equals(value2))) return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public final Object toJava() throws QueryException {
    // determine type (static or exact)
    final int sz = (int) arraySize();
    SeqType dt = funcType().declType;
    if(sz > 0 && dt.eq(SeqType.ITEM_ZM)) {
      dt = null;
      for(final Value member : members()) {
        final SeqType st = member.seqType();
        dt = dt == null ? st : dt.union(st);
      }
    }
    // convert to specific arrays
    if(dt.one()) {
      final Type tp = dt.type;
      if(tp == AtomType.BOOLEAN) {
        final BoolList list = new BoolList(sz);
        for(final Value member : members()) list.add(((Bln) member).bool(null));
        return list.finish();
      }
      if(tp == AtomType.BYTE) {
        final ByteList list = new ByteList(sz);
        for(final Value member : members()) list.add((byte) ((Int) member).itr());
        return list.finish();
      }
      if(tp.oneOf(AtomType.SHORT, AtomType.UNSIGNED_BYTE)) {
        final ShortList list = new ShortList(sz);
        for(final Value member : members()) list.add((short) ((Int) member).itr());
        return list.finish();
      }
      if(tp == AtomType.UNSIGNED_SHORT) {
        final char[] chars = new char[sz];
        int c = 0;
        for(final Value member : members()) chars[c++] = (char) ((Int) member).itr();
        return chars;
      }
      if(tp == AtomType.INT) {
        final IntList list = new IntList(sz);
        for(final Value member : members()) list.add((int) ((Int) member).itr());
        return list.finish();
      }
      if(tp.instanceOf(AtomType.INTEGER) && tp != AtomType.UNSIGNED_LONG) {
        final LongList list = new LongList(sz);
        for(final Value member : members()) list.add(((Int) member).itr());
        return list.finish();
      }
      if(tp == AtomType.FLOAT) {
        final FloatList list = new FloatList(sz);
        for(final Value member : members()) list.add(((Flt) member).flt());
        return list.finish();
      }
      if(tp == AtomType.DOUBLE) {
        final DoubleList list = new DoubleList(sz);
        for(final Value member : members()) list.add(((Dbl) member).dbl());
        return list.finish();
      }
      if(tp.instanceOf(AtomType.STRING)) {
        final StringList list = new StringList(sz);
        for(final Value member : members()) list.add((String) member.toJava());
        return list.finish();
      }
    }
    final ArrayList<Object> list = new ArrayList<>(sz);
    for(final Value member : members()) list.add(member.toJava());
    return list.toArray();
  }

  @Override
  public final String description() {
    return ARRAY;
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    final ExprList list = new ExprList();
    final long size = arraySize();
    final int max = (int) Math.min(size, 5);
    for(int i = 0; i < max; i++) list.add(get(i));
    plan.add(plan.create(this, ENTRIES, size), list.finish());
  }

  @Override
  public final void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Value member : members()) {
      if(!tb.moreInfo()) break;
      tb.add(tb.isEmpty() ? " " : ", ");
      final long ms = member.size();
      if(ms != 1) tb.add('(');
      for(int m = 0; m < ms; m++) {
        if(m != 0) tb.add(", ");
        tb.add(member.itemAt(m));
      }
      if(ms != 1) tb.add(')');
    }
    qs.braced("[ ", tb.add(' ').finish(), " ]");
  }
}
