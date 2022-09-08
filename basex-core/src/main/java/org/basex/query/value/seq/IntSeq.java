package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Int xs:integer}, containing at least two of them.
 *
 * @author BaseX Team 2005-22, BSD License
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
   * Creates a value from the input stream.
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
    if(mode == Simplify.DISTINCT) {
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
   * @param values values (will be invalidated by this call)
   * @return value
   */
  public static Value get(final LongList values) {
    return values.isEmpty() ? Empty.VALUE : values.size() == 1 ? Int.get(values.get(0)) :
      new IntSeq(values.finish(), AtomType.INTEGER);
  }

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
   * @param type type
   * @return value
   */
  public static Value get(final long[] values, final Type type) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Int.get(values[0], type) : new IntSeq(values, type);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param type item type
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final Type type, final int size, final Value... values) throws QueryException {
    final LongList tmp = new LongList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof IntSeq) {
        tmp.add(((IntSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.itr(null));
      }
    }
    return get(tmp.finish(), type);
  }
}
