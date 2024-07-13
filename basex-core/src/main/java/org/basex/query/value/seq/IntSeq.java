package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Int xs:integer}, containing at least two of them.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class IntSeq extends NativeSeq {
  /** Values. */
  private final long[] values;

  /**
   * Constructor.
   * @param values values
   * @param type type
   */
  private IntSeq(final long[] values, final Type type) {
    super(values.length, type);
    this.values = values;
  }

  /**
   * Creates a value from the input stream. Called from {@link Store#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException {
    final int size = in.readNum();
    final long[] values = new long[size];
    for(int s = 0; s < size; s++) values[s] = in.readLong();
    return get(values, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final long v : values) out.writeLong(v);
  }

  @Override
  public Int itemAt(final long pos) {
    return Int.get(values[(int) pos], type);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {

    if(pos == 0) return super.test(qc, ii, pos);
    for(final long value : values) {
      if(value == pos) return true;
    }
    return false;
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final long[] tmp = new long[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  /**
   * Returns the internal values.
   * @return values
   */
  public long[] values() {
    return values;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.DISTINCT, Simplify.PREDICATE)) {
      // replace with new sequence or range sequence
      final long[] tmp = new LongList((int) size).add(values).ddo().finish();
      final int tl = tmp.length;
      int t = 0;
      if(seqType().type == AtomType.INTEGER) {
        while(++t < tl && tmp[0] + t == tmp[t]);
      }
      if(t == tl) expr = RangeSeq.get(tmp[0], tl, true);
      else if(tl != size) expr = get(tmp, type);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYTE:
        final ByteList bl = new ByteList((int) size);
        for(final long value : values) bl.add((byte) value);
        return bl.finish();
      case SHORT:
      case UNSIGNED_BYTE:
        final ShortList sl = new ShortList((int) size);
        for(final long value : values) sl.add((short) value);
        return sl.finish();
      case UNSIGNED_SHORT:
        final char[] chars = new char[(int) size];
        int c = 0;
        for(final long value : values) chars[c++] = (char) value;
        return chars;
      case INT:
        final IntList il = new IntList((int) size);
        for(final long value : values) il.add((int) value);
        return il.finish();
      default:
        return values;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof IntSeq)) return super.equals(obj);
    final IntSeq is = (IntSeq) obj;
    return type == is.type && Arrays.equals(values, is.values);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates an xs:integer sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final long[] values) {
    return get(values, AtomType.INTEGER);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @param type item type; must be instance of xs:integer
   * @return value
   */
  public static Value get(final long[] values, final Type type) {
    // empty?
    final int vl = values.length;
    if(vl == 0) return Empty.VALUE;
    // single item?
    final long first = values[0];
    if(vl == 1) return Int.get(first, type);
    // singleton or range?
    boolean singleton = true, range = true;
    int v = 0;
    while((singleton || range) && ++v < vl) {
      final long l = values[v];
      singleton &= l == first;
      range &= l == first + v;
    }
    if(v == vl) {
      if(singleton) return SingletonSeq.get(Int.get(first, type), vl);
      if(type == AtomType.INTEGER) return RangeSeq.get(first, vl, true);
    }
    return new IntSeq(values, type);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param type item type; must be instance of xs:integer
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Type type, final long size, final Value... values)
      throws QueryException {
    final LongList list = new LongList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof IntSeq) {
        list.add(((IntSeq) value).values);
      } else {
        for(final Item item : value) list.add(item.itr(null));
      }
    }
    return get(list.finish(), type);
  }
}
