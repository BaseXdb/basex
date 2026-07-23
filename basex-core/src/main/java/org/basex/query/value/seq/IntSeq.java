package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Itr xs:integer}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class IntSeq extends ItrSeq {
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
  public long itrAt(final int index) {
    return values[index];
  }

  @Override
  int width() {
    return 4;
  }

  @Override
  public Value reverse(final Job job) {
    final int sz = (int) size;
    final int[] tmp = new int[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  @Override
  public Value sort() {
    final int[] tmp = values.clone();
    Arrays.sort(tmp);
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
  public Object toJava() throws QueryException {
    return type == BasicType.INT ? values : super.toJava();
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
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Itr.get(values[0], type) :
      refine(new IntSeq(values, type));
  }
}
