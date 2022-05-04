package org.basex.query.value.seq;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Int xs:float}, containing at least two of them.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FltSeq extends NativeSeq {
  /** Values. */
  private final float[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private FltSeq(final float[] values) {
    super(values.length, AtomType.FLOAT);
    this.values = values;
  }

  /**
   * Creates a value from the input stream.
   * @param in data input
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value read(final DataInput in, final Type type, final QueryContext qc)
      throws IOException, QueryException {
    final int size = in.readNum();
    final float[] values = new float[size];
    for(int s = 0; s < size; s++) values[s] = Flt.parse(in.readToken(), null);
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final double v : values) out.writeToken(Token.token(v));
  }

  @Override
  public Flt itemAt(final long pos) {
    return Flt.get(values[(int) pos]);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final float[] tmp = new float[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public float[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof FltSeq ? Arrays.equals(values, ((FltSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final float[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Flt.get(values[0]) : new FltSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final FloatList tmp = new FloatList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof FltSeq) {
        tmp.add(((FltSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.flt(null));
      }
    }
    return get(tmp.finish());
  }
}
