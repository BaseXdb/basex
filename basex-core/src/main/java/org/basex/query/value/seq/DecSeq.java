package org.basex.query.value.seq;

import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence of items of type {@link Int xs:decimal}, containing at least two of them.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DecSeq extends NativeSeq {
  /** Values. */
  private final BigDecimal[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private DecSeq(final BigDecimal[] values) {
    super(values.length, AtomType.DECIMAL);
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
    final BigDecimal[] values = new BigDecimal[size];
    for(int s = 0; s < size; s++) values[s] = new BigDecimal(Token.string(in.readToken()));
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final BigDecimal v : values) out.writeToken(chopNumber(token(v.toPlainString())));
  }

  @Override
  public Dec itemAt(final long pos) {
    return Dec.get(values[(int) pos]);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final BigDecimal[] tmp = new BigDecimal[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public BigDecimal[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof DecSeq ? Array.equals(values, ((DecSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  private static Value get(final BigDecimal[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Dec.get(values[0]) : new DecSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final BigDecimal[] tmp = new BigDecimal[size];
    int t = 0;
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof DecSeq) {
        final int vs = (int) value.size();
        Array.copyFromStart(((DecSeq) value).values, vs, tmp, t);
        t += vs;
      } else {
        for(final Item item : value) tmp[t++] = item.dec(null);
      }
    }
    return get(tmp);
  }
}
