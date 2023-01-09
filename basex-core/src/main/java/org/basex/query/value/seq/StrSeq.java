package org.basex.query.value.seq;

import java.io.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Str xs:string}, containing at least two of them.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StrSeq extends NativeSeq {
  /** Values. */
  private final byte[][] values;

  /**
   * Constructor.
   * @param values values
   */
  private StrSeq(final byte[][] values) {
    super(values.length, AtomType.STRING);
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
    final byte[][] values = new byte[size][];
    for(int s = 0; s < size; s++) values[s] = in.readToken();
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final byte[] v : values) out.writeToken(v);
  }

  @Override
  public Str itemAt(final long pos) {
    return Str.get(values[(int) pos]);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final byte[][] array = new byte[sz][];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = values[i];
    return get(array);
  }

  @Override
  public String[] toJava() {
    final StringList sl = new StringList((int) size);
    for(final byte[] value : values) sl.add(value);
    return sl.finish();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof StrSeq ? Array.equals(values, ((StrSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values (will be invalidated by this call)
   * @return value
   */
  public static Value get(final TokenList values) {
    return values.isEmpty() ? Empty.VALUE : values.size() == 1 ? Str.get(values.get(0)) :
      new StrSeq(values.finish());
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final byte[][] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Str.get(values[0]) : new StrSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final TokenList tmp = new TokenList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof StrSeq) {
        tmp.add(((StrSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.string(null));
      }
    }
    return get(tmp);
  }
}
