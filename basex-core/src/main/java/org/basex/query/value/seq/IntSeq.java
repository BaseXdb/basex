package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Itr xs:integer}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class IntSeq extends NativeSeq {
  /** Values. */
  private final int[] values;

  /**
   * Constructor.
   * @param values values
   * @param type type
   */
  private IntSeq(final int[] values, final Type type) {
    super(values.length, type);
    this.values = values;
  }

  /**
   * Creates a value from the input stream.
   * Called from {@link Stores#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException {
    final int size = in.readNum();
    final int[] values = new int[size];
    for(int s = 0; s < size; s++) values[s] = (int) in.readLong();
    return get(values, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final int v : values) out.writeLong(v);
  }

  @Override
  public Itr itemAt(final long index) {
    return Itr.get(values[(int) index], type);
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
  public Value reverse(final Job job) {
    final int sz = (int) size;
    final int[] tmp = new int[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  /**
   * Returns the internal values.
   * @return values
   */
  public int[] values() {
    return values;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;

    int[] tmp = null;
    if(mode == Simplify.PREDICATE) {
      // remove duplicates, order data: (2, 1, 2) → 1 to 2
      tmp = new IntList((int) size).add(values).ddo().finish();
    } else if(mode == Simplify.DISTINCT) {
      // remove duplicates, but preserve order: (2, 1, 2) → (2, 1)
      final IntSet is = new IntSet(size);
      for(final int i : values) is.add(i);
      tmp = is.keys();
    }
    if(tmp != null) {
      final int tl = tmp.length;
      int t = 0;
      if(seqType().type == BasicType.INTEGER) {
        while(++t < tl && tmp[0] + t == tmp[t]);
      }
      if(t == tl) expr = RangeSeq.get(tmp[0], tl, true);
      else if(tl != size) expr = get(tmp, type);
    }

    return cc.simplify(this, expr, mode);
  }

  @Override
  public Object toJava() {
    switch((BasicType) type) {
      case BYTE:
        final ByteList bl = new ByteList((int) size);
        for(final int value : values) bl.add((byte) value);
        return bl.finish();
      case SHORT:
      case UNSIGNED_BYTE:
        final ShortList sl = new ShortList((int) size);
        for(final int value : values) sl.add((short) value);
        return sl.finish();
      case UNSIGNED_SHORT:
        final char[] chars = new char[(int) size];
        int c = 0;
        for(final int value : values) chars[c++] = (char) value;
        return chars;
      case INT:
        return values;
      default:
        final LongList il = new LongList((int) size);
        for(final int value : values) il.add(value);
        return il.finish();
    }
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final IntSeq seq ? type == seq.type &&
        Arrays.equals(values, seq.values) : super.equals(obj));
 }

  // STATIC METHODS ===============================================================================

  /**
   * Creates an xs:integer sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final int[] values) {
    return get(values, BasicType.INTEGER);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @param type type; must be an instance of xs:integer
   * @return value
   */
  public static Value get(final int[] values, final Type type) {
    // empty?
    final int vl = values.length;
    if(vl == 0) return Empty.VALUE;
    // single item?
    final int first = values[0];
    if(vl == 1) return Itr.get(first, type);
    // singleton or range?
    boolean same = true, asc = true, desc = true;
    for(int v = 1; v < vl && (same || asc || desc); v++) {
      final int i = values[v];
      if(same && i != first) same = false;
      if(asc  && i != first + v) asc = false;
      if(desc && i != first - v) desc = false;
    }
    return same ? SingletonSeq.get(Itr.get(first, type), vl) :
      asc || desc ? RangeSeq.get(first, vl, asc) : new IntSeq(values, type);
  }
}
