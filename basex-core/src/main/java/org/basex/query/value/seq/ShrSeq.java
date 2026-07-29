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
 * Sequence of items of type {@link Itr xs:short}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ShrSeq extends ItrSeq {
  /** Values. */
  private final short[] values;

  /**
   * Constructor.
   * @param values shorts
   * @param type type
   */
  private ShrSeq(final short[] values, final Type type) {
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
    final short[] values = new short[size];
    for(int s = 0; s < size; s++) values[s] = (short) in.readNum();
    return get(values, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final short v : values) out.writeNum(v);
  }

  @Override
  public Itr itemAt(final long index) {
    return new Itr(values[(int) index], type);
  }

  @Override
  public long itrAt(final int index) {
    return values[index];
  }

  @Override
  int width() {
    return 2;
  }

  @Override
  public Value reverse(final Job job) {
    final int sz = (int) size;
    final short[] tmp = new short[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  @Override
  public Value sort() {
    final short[] tmp = values.clone();
    Arrays.sort(tmp);
    return get(tmp, type);
  }

  @Override
  public Object toJava() throws QueryException {
    return type == BasicType.SHORT ? values : super.toJava();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final ShrSeq seq ? type == seq.type &&
        Arrays.equals(values, seq.values) : super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates an xs:short sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final short[] values) {
    return get(values, BasicType.SHORT);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @param type type; must be xs:short or an instance of xs:integer
   * @return value
   */
  public static Value get(final short[] values, final Type type) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Itr.get(values[0], type) :
      refine(new ShrSeq(values, type));
  }
}
