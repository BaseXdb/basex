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
import org.basex.query.iter.*;
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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class XQArray extends XQStruct {
  /** Length. */
  protected long size;

  /**
   * Default constructor.
   * @param size size
   * @param type function type
   */
  XQArray(final long size, final Type type) {
    super(type);
    this.size = size;
  }

  /**
   * The empty array.
   * @return (unique) instance of an empty array
   */
  public static XQArray empty() {
    return EmptyArray.EMPTY;
  }

  /**
   * Creates an array with a single member.
   * @param value single value
   * @return array
   */
  public static XQArray singleton(final Value value) {
    return new SingletonArray(value);
  }

  /**
   * Creates an array with single-item members.
   * @param value single value
   * @return array
   */
  public static XQArray items(final Value value) {
    final long size = value.size();
    return size == 0 ? empty() : size == 1 ? singleton(value) : new ItemArray(value);
  }

  @Override
  public final long structSize() {
    return size;
  }

  /**
   * Gets the value at the given position in this array.
   * @param index index of the value to get
   * @return the corresponding value
   */
  public abstract Value memberAt(long index);

  /**
   * Prepends a value to the front of this array.
   * @param head value to prepend
   * @return resulting array
   */
  public abstract XQArray prepend(Value head);

  /**
   * Appends a value to the end of this array.
   * @param last value to append
   * @return resulting array
   */
  public abstract XQArray append(Value last);

  /**
   * Returns a copy of this array where the value at the given position is
   * replaced by the given value.
   * @param pos position of the value to replace
   * @param value value to put into this array
   * @return resulting array
   */
  public abstract XQArray put(long pos, Value value);

  /**
   * Returns a subsequence of this array with the given start and length.
   * @param start starting position
   * @param length number of items
   * @param qc query context
   * @return sub sequence
   */
  public final XQArray subArray(final long start, final long length, final QueryContext qc) {
    return length == 0 ? empty() :
           length == 1 ? singleton(memberAt(start)) :
           length == structSize() ? this :
           subArr(start, length, qc);
  }

  /**
   * Returns a subsequence of this array with the given start and length.
   * @param pos position of first member
   * @param length number of members
   * @param qc query context
   * @return the sub-array
   */
  protected abstract XQArray subArr(long pos, long length, QueryContext qc);

  /**
   * Inserts the given value at the given position into this array.
   * By default, the array will be converted to the tree representation,
   * because its runtime outweighs the possibly higher memory consumption.
   * @param pos insertion position, must be between {@code 0} and {@code arraySize()}
   * @param value value to insert
   * @param qc query context
   * @return resulting array
   */
  public abstract XQArray insertBefore(long pos, Value value, QueryContext qc);

  /**
   * Removes the value at the given position in this array.
   * By default, the array will be converted to the tree representation,
   * because its runtime outweighs the possibly higher memory consumption.
   * @param pos deletion position, must be between {@code 0} and {@code arraySize() - 1}
   * @param qc query context
   * @return resulting array
   */
  public abstract XQArray remove(long pos, QueryContext qc);

  /**
   * Returns an array with the same values as this one, but their order reversed.
   * @param qc query context
   * @return reversed version of this array
   */
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final ArrayBuilder ab = new ArrayBuilder(qc, size);
    for(long i = size - 1; i >= 0; i--) ab.add(memberAt(i));
    return ab.array(this);
  }

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeLong(structSize());
    for(final Value value : iterable()) Store.write(out, value);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Value value : iterable()) value.cache(lazy, ii);
  }

  /**
   * Iterator over the values of this array.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array values
   */
  public ListIterator<Value> iterator(final long start) {
    return new ListIterator<>() {
      private int index = (int) start;

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < structSize();
      }

      @Override
      public Value next() {
        return memberAt(index++);
      }

      @Override
      public int previousIndex() {
        return index - 1;
      }

      @Override
      public boolean hasPrevious() {
        return index > 0;
      }

      @Override
      public Value previous() {
        return memberAt(--index);
      }

      @Override
      public void set(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void add(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  /** Iterable over the values of this array. */
  private Iterable<Value> iterable;

  /**
   * Iterator over the values of this array.
   * @return array over the array values
   */
  public final Iterable<Value> iterable() {
    if(iterable == null) iterable = () -> iterator(0);
    return iterable;
  }

  @Override
  public Iter items() throws QueryException {
    return new Iter() {
      final Iterator<Value> values = iterator(0);
      Iter ir;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir != null) {
            final Item item = ir.next();
            if(item != null) return item;
          }
          if(!values.hasNext()) return null;
          ir = values.next().iter();
        }
      }
    };
  }

  /**
   * Creates a new array type.
   * @param value value to be added
   * @return union type
   */
  final ArrayType union(final Value value) {
    return ((ArrayType) type).union(value.seqType());
  }

  @Override
  public final Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return get(key(args[0], qc, ii), qc, ii);
  }

  /**
   * Gets a value from this array.
   * @param key key to look for
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  public final Value get(final Item key, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final long i = index(key, qc, ii);
    if(i > 0 && i <= size) return memberAt(i - 1);
    throw (size == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(ii, i, size);
  }

  /**
   * Gets a value from this array.
   * @param key key to look for
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  public final Value getOrNull(final Item key, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final long i = index(key, qc, ii);
    return i > 0 && i <= structSize() ? memberAt(i - 1) : null;
  }

  /**
   * Returns the array index.
   * @param key key to look for
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @return index
   * @throws QueryException query exception
   */
  private static long index(final Item key, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    return ((Int) SeqType.INTEGER_O.coerce(key, null, qc, null, ii)).itr();
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc, structSize());
    for(final Value value : iterable()) vb.add(value.atomValue(qc, ii));
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
    for(final Value value : iterable()) {
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

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Value value : iterable()) {
      qc.checkStop();
      ab.add(value.materialize(test, ii, qc));
    }
    return ab.array(this);
  }

  @Override
  public final boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    if(!funcType().declType.type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      for(final Value value : iterable()) {
        if(!value.materialized(test, ii)) return false;
      }
    }
    return true;
  }

  @Override
  public final boolean instanceOf(final Type tp, final boolean coerce) {
    if(coerce && tp instanceof FuncType) return type == tp;
    if(type.instanceOf(tp)) return true;

    final SeqType mt;
    if(tp instanceof ArrayType) {
      mt = ((ArrayType) tp).valueType();
    } else if(tp instanceof FuncType) {
      final FuncType ft = (FuncType) tp;
      if(ft.argTypes.length != 1 || !ft.argTypes[0].instanceOf(SeqType.INTEGER_O)) return false;
      mt = ft.declType;
    } else {
      return false;
    }
    if(!mt.eq(SeqType.ITEM_ZM)) {
      // check types of values
      for(final Value value : iterable()) {
        if(!mt.instance(value)) return false;
      }
    }
    return true;
  }

  /**
   * Converts this array to the given array type.
   * @param at array type
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @return coerced array
   * @throws QueryException query exception
   */
  public final XQArray coerceTo(final ArrayType at, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Value value : iterable()) {
      qc.checkStop();
      ab.add(at.valueType().coerce(value, null, qc, cc, ii));
    }
    return ab.array();
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(this == item) return true;
    if(item instanceof XQArray) {
      final XQArray array = (XQArray) item;
      if(structSize() != array.structSize()) return false;
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
    final int sz = (int) structSize();
    SeqType dt = funcType().declType;
    if(sz > 0 && dt.eq(SeqType.ITEM_ZM)) {
      dt = null;
      for(final Value value : iterable()) {
        final SeqType st = value.seqType();
        dt = dt == null ? st : dt.union(st);
      }
    }
    // convert to specific arrays
    if(dt.one()) {
      final Type tp = dt.type;
      if(tp == AtomType.BOOLEAN) {
        final BoolList list = new BoolList(sz);
        for(final Value value : iterable()) list.add(((Bln) value).bool(null));
        return list.finish();
      }
      if(tp == AtomType.BYTE) {
        final ByteList list = new ByteList(sz);
        for(final Value value : iterable()) list.add((byte) ((Int) value).itr());
        return list.finish();
      }
      if(tp.oneOf(AtomType.SHORT, AtomType.UNSIGNED_BYTE)) {
        final ShortList list = new ShortList(sz);
        for(final Value value : iterable()) list.add((short) ((Int) value).itr());
        return list.finish();
      }
      if(tp == AtomType.UNSIGNED_SHORT) {
        final char[] chars = new char[sz];
        int c = 0;
        for(final Value value : iterable()) chars[c++] = (char) ((Int) value).itr();
        return chars;
      }
      if(tp == AtomType.INT) {
        final IntList list = new IntList(sz);
        for(final Value value : iterable()) list.add((int) ((Int) value).itr());
        return list.finish();
      }
      if(tp.instanceOf(AtomType.INTEGER) && tp != AtomType.UNSIGNED_LONG) {
        final LongList list = new LongList(sz);
        for(final Value value : iterable()) list.add(((Int) value).itr());
        return list.finish();
      }
      if(tp == AtomType.FLOAT) {
        final FloatList list = new FloatList(sz);
        for(final Value value : iterable()) list.add(((Flt) value).flt());
        return list.finish();
      }
      if(tp == AtomType.DOUBLE) {
        final DoubleList list = new DoubleList(sz);
        for(final Value value : iterable()) list.add(((Dbl) value).dbl());
        return list.finish();
      }
      if(tp.instanceOf(AtomType.STRING)) {
        final StringList list = new StringList(sz);
        for(final Value value : iterable()) list.add((String) value.toJava());
        return list.finish();
      }
    }
    final ArrayList<Object> list = new ArrayList<>(sz);
    for(final Value value : iterable()) list.add(value.toJava());
    return list.toArray();
  }

  @Override
  public String description() {
    return ARRAY;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final ExprList list = new ExprList();
    final int max = (int) Math.min(size, 5);
    for(int i = 0; i < max; i++) list.add(memberAt(i));
    plan.add(plan.create(this, ENTRIES, size), list.finish());
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Value value : iterable()) {
      if(!tb.moreInfo()) break;
      tb.add(tb.isEmpty() ? " " : ", ");
      final long vs = value.size();
      if(vs != 1) tb.add('(');
      for(int m = 0; m < vs; m++) {
        if(m != 0) tb.add(", ");
        tb.add(value.itemAt(m));
      }
      if(vs != 1) tb.add(')');
    }
    qs.braced("[ ", tb.add(' ').finish(), " ]");
  }
}
