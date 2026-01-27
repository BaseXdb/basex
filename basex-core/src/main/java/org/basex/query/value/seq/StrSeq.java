package org.basex.query.value.seq;

import java.io.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Str xs:anyURI}, {@link Str xs:untypedAtomic} or
 * instance of {@link Str xs:string}, containing at least two of them.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StrSeq extends NativeSeq {
  /** Values. */
  private final byte[][] values;

  /**
   * Constructor.
   * @param values values
   * @param type type
   */
  private StrSeq(final byte[][] values, final Type type) {
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
    final byte[][] values = new byte[size][];
    for(int s = 0; s < size; s++) values[s] = in.readToken();
    return get(values, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeNum((int) size);
    for(final byte[] v : values) out.writeToken(v);
  }

  @Override
  public Item itemAt(final long index) {
    return get(values[(int) index], type);
  }

  @Override
  public Value reverse(final Job job) {
    final int sz = (int) size;
    final byte[][] tmp = new byte[sz][];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  @Override
  public String[] toJava() {
    final StringList sl = new StringList((int) size);
    for(final byte[] value : values) sl.add(value);
    return sl.finish();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final StrSeq seq ? Array.equals(values, seq.values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param values values (will be invalidated by this call)
   * @return value
   */
  public static Value get(final TokenList values) {
    return get(values, AtomType.STRING);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @param type type; must be xs:anyURI, xs:untypedAtomic or an instance of xs:string
   * @return value
   */
  public static Value get(final TokenList values, final Type type) {
    return values.isEmpty() ? Empty.VALUE : values.size() == 1 ? get(values.get(0), type) :
      new StrSeq(values.finish(), type);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @return value
   */
  public static Value get(final byte[][] values) {
    return get(values, AtomType.STRING);
  }

  /**
   * Creates a sequence with the specified values.
   * @param values values
   * @param type type; must be xs:anyURI, xs:untypedAtomic or an instance of xs:string
   * @return value
   */
  public static Value get(final byte[][] values, final Type type) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? get(values[0], type) : new StrSeq(values, type);
  }

  /**
   * Creates an item for the specified token.
   * @param string string
   * @param type type; must be xs:anyURI, xs:untypedAtomic or an instance of xs:string
   * @return value
   */
  private static Item get(final byte[] string, final Type type) {
    return type == AtomType.UNTYPED_ATOMIC ? Atm.get(string) :
      type == AtomType.ANY_URI ? Uri.get(string) : Str.get(string, type);
  }
}
