package org.basex.query.value.array;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
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
  /**
   * Default constructor.
   * @param type function type
   */
  XQArray(final Type type) {
    super(type);
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
   * @param member single value
   * @return array
   */
  public static XQArray get(final Value member) {
    return new SingletonArray(member);
  }

  /**
   * Creates an array with single-item members.
   * @param members members
   * @return array
   */
  public static XQArray items(final Value members) {
    final long size = members.size();
    return size == 0 ? empty() : size == 1 ? get(members) : new ItemArray(members);
  }

  /**
   * Returns the value at the given position.
   * The specified value must be lie within the valid bounds.
   * @param index index position
   * @return value
   */
  public abstract Value memberAt(long index);

  @Override
  public Iter itemsIter() {
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

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc, structSize());
    for(final Value value : iterable()) vb.add(value.atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr ex = this;
    if(mode.oneOf(Simplify.NUMBER, Simplify.DATA)) ex = items(cc.qc);
    return cc.simplify(this, ex, mode);
  }

  /**
   * Returns a subsequence with the given start and length.
   * @param pos starting position
   * @param length number of items
   * @param job interruptible job
   * @return new subarray
   */
  public final XQArray subArray(final long pos, final long length, final Job job) {
    return length == 0 ? empty() :
           length == 1 ? get(memberAt(pos)) :
           length == structSize() ? this :
           subArr(pos, length, job);
  }

  /**
   * Returns a subsequence with the given start and length.
   * @param pos position of first member (>= 0)
   * @param length number of members (1 < length < size())
   * @param job interruptible job
   * @return new subarray
   */
  protected abstract XQArray subArr(long pos, long length, Job job);

  /**
   * Replaces a value at the specified position.
   * @param pos position of the value to replace
   * @param value value to put into this array
   * @param job interruptible job
   * @return new array
   */
  public abstract XQArray putMember(long pos, Value value, Job job);

  /**
   * Appends a value.
   * @param value value to append
   * @param job interruptible job
   * @return new array
   */
  public final XQArray appendMember(final Value value, final Job job) {
    return insertMember(structSize(), value, job);
  }

  /**
   * Inserts a value at the given position.
   * @param pos insertion position, must be between 0 and {@link #structSize()}
   * @param value value to insert
   * @param job interruptible job
   * @return new array
   */
  public abstract XQArray insertMember(long pos, Value value, Job job);

  /**
   * Removes a value at the given position.
   * @param pos deletion position, must be between 0 and {@link #structSize() - 1}
   * @param job interruptible job
   * @return new array
   */
  public abstract XQArray removeMember(long pos, Job job);

  /**
   * Returns an array with the same values as this one, but their order reversed.
   * @param job interruptible job
   * @return new array
   */
  public XQArray reverseArray(final Job job) {
    job.checkStop();
    final long size = structSize();
    final ArrayBuilder ab = new ArrayBuilder(job, size);
    for(long i = size - 1; i >= 0; i--) ab.add(memberAt(i));
    return ab.array(this);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Value value : iterable()) value.cache(lazy, ii);
  }

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeLong(structSize());
    for(final Value value : iterable()) Stores.write(out, value);
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
    final long i = index(key, qc, ii), size = structSize();
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
    return ((Itr) Types.INTEGER_O.coerce(key, null, qc, null, ii)).itr();
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
        if(item instanceof final XQArray array) array.string(indent, tb, level, ii);
        else if(item instanceof final XQMap map) map.string(indent, tb, level + 1, ii);
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

    final ArrayBuilder ab = new ArrayBuilder(qc, structSize());
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
    if(tp instanceof final ArrayType at) {
      mt = at.valueType();
    } else if(tp instanceof final FuncType ft) {
      if(ft.argTypes.length != 1 || !ft.argTypes[0].instanceOf(Types.INTEGER_O)) return false;
      mt = ft.declType;
    } else {
      return false;
    }
    if(!mt.eq(Types.ITEM_ZM)) {
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

    final ArrayBuilder ab = new ArrayBuilder(qc, structSize());
    for(final Value value : iterable()) {
      qc.checkStop();
      ab.add(at.valueType().coerce(value, null, qc, cc, ii));
    }
    return ab.array(at);
  }

  @Override
  public final boolean refineType() {
    Type refined = null;
    for(final Value value : iterable()) {
      final ArrayType at = ArrayType.get(value.seqType());
      refined = refined == null ? at : refined.union(at);
      if(refined.eq(type)) return true;
    }
    type = refined;
    return true;
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(this == item) return true;
    if(item instanceof final XQArray array) {
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
  protected final XQArray rebuild(final Job job) throws QueryException {
    final ArrayBuilder ab = new ArrayBuilder(job, structSize());
    for(final Value value : iterable()) ab.add(value.shrink(job));
    return ab.array(this);
  }

  @Override
  public final Object toJava() throws QueryException {
    // determine type (static or exact)
    final int sz = (int) structSize();
    SeqType dt = funcType().declType;
    if(sz > 0 && dt.type == AtomType.ITEM) {
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
        for(final Value value : iterable()) list.add((byte) ((Itr) value).itr());
        return list.finish();
      }
      if(tp.oneOf(AtomType.SHORT, AtomType.UNSIGNED_BYTE)) {
        final ShortList list = new ShortList(sz);
        for(final Value value : iterable()) list.add((short) ((Itr) value).itr());
        return list.finish();
      }
      if(tp == AtomType.UNSIGNED_SHORT) {
        final char[] chars = new char[sz];
        int c = 0;
        for(final Value value : iterable()) chars[c++] = (char) ((Itr) value).itr();
        return chars;
      }
      if(tp == AtomType.INT) {
        final IntList list = new IntList(sz);
        for(final Value value : iterable()) list.add((int) ((Itr) value).itr());
        return list.finish();
      }
      if(tp.instanceOf(AtomType.INTEGER) && tp != AtomType.UNSIGNED_LONG) {
        final LongList list = new LongList(sz);
        for(final Value value : iterable()) list.add(((Itr) value).itr());
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
    final long size = structSize(), max = Math.min(size, 5);
    for(int m = 0; m < max; m++) list.add(memberAt(m));
    plan.add(plan.create(this, ENTRIES, size), list.finish());
  }

  @Override
  public void toString(final QueryString qs) {
    if(structSize() == 0) {
      qs.token("[]");
    } else {
      final TokenBuilder tb = new TokenBuilder();
      for(final Value value : iterable()) {
        if(!tb.moreInfo()) break;
        tb.add(tb.isEmpty() ? " " : ", ");
        final long vs = value.size();
        if(vs != 1) tb.add('(');
        for(int m = 0; m < vs; m++) {
          if(!tb.moreInfo()) break;
          if(m != 0) tb.add(", ");
          final Item item = value.itemAt(m);
          tb.add(qs.error() ? item.toErrorString() : item);
        }
        if(vs != 1) tb.add(')');
      }
      qs.braced("[ ", tb.finish(), " ]");
    }
  }
}
