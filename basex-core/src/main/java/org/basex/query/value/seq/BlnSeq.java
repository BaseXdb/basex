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

/**
 * Sequence of items of type {@link Bln xs:boolean}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BlnSeq extends NativeSeq {
  /** Distinct boolean sequence. */
  private static final BlnSeq FT = new BlnSeq(new boolean[] { false, true });
  /** Distinct boolean sequence. */
  private static final BlnSeq TF = new BlnSeq(new boolean[] { true, false });
  /** Values. */
  private final boolean[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private BlnSeq(final boolean[] values) {
    super(values.length, AtomType.BOOLEAN);
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
    final boolean[] values = new boolean[size];
    for(int s = 0; s < size; s++) values[s] = in.readBool();
    return get(values);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final boolean v : values) out.writeBool(v);
  }

  @Override
  public Bln itemAt(final long index) {
    return Bln.get(values[(int) index]);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final boolean[] tmp = new boolean[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.DISTINCT, Simplify.PREDICATE) && this != FT && this != TF) {
      // replace with new sequence
      boolean f = false, t = false;
      for(final boolean b : values) {
        if(b) t = true;
        else f = true;
        if(f && t) return values[0] ? TF : FT;
      }
      return Bln.get(t);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public boolean[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final BlnSeq seq ? Arrays.equals(values, seq.values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final boolean[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Bln.get(values[0]) : new BlnSeq(values);
  }
}
