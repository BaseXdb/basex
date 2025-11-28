package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Itr xs:byte}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BytSeq extends NativeSeq {
  /** Values. */
  private final byte[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private BytSeq(final byte[] values) {
    super(values.length, AtomType.BYTE);
    this.values = values;
  }

  /**
   * Creates a value from the input stream. Called from {@link Stores#read(DataInput, QueryContext)}.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException {
    final int size = in.readNum();
    final byte[] values = new byte[size];
    for(int s = 0; s < size; s++) values[s] = (byte) in.readNum();
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final byte v : values) out.writeNum(v);
  }

  @Override
  public Itr itemAt(final long index) {
    return new Itr(values[(int) index], type);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final byte[] tmp = new byte[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public byte[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final BytSeq seq ? Arrays.equals(values, seq.values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final byte[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Itr.get(values[0], AtomType.BYTE) : new BytSeq(values);
  }
}
