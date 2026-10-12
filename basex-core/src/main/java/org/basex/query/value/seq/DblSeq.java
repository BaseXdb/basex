package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Dbl xs:double}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DblSeq extends NativeSeq {
  /** Values. */
  private final double[] values;

  /**
   * Constructor.
   * @param values values
   */
  private DblSeq(final double[] values) {
    super(values.length, BasicType.DOUBLE);
    this.values = values;
  }

  /**
   * Creates a value from the input stream.
   * @param in data input
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in) throws IOException {
    final int size = in.readNum();
    final double[] values = new double[size];
    for(int s = 0; s < size; s++) values[s] = in.readDoubleCompact();
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final double v : values) out.writeDoubleCompact(v);
  }

  @Override
  public Dbl itemAt(final long index) {
    return Dbl.get(values[(int) index]);
  }

  @Override
  public Value reverse(final Job job) {
    final int sz = (int) size;
    final double[] tmp = new double[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public Value sort() {
    // NaN and negative zero: XQuery order and stability differ from Java
    for(final double d : values) {
      if(Double.isNaN(d) || d == 0 && Double.doubleToRawLongBits(d) < 0) return null;
    }
    final double[] tmp = values.clone();
    Arrays.sort(tmp);
    return get(tmp);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final DblSeq seq ? Arrays.equals(values, seq.values) :
      super.equals(obj));
  }

  @Override
  public double[] toJava() {
    return values;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final double[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Dbl.get(values[0]) : new DblSeq(values);
  }
}
