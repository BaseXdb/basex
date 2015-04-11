package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Str xs:string}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
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
    super(values.length, AtomType.STR);
    this.values = values;
  }

  @Override
  public Str itemAt(final long pos) {
    return Str.get(values[(int) pos]);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof StrSeq && Arrays.equals(values, ((StrSeq) cmp).values);
  }

  @Override
  public String[] toJava() {
    final int vl = values.length;
    final String[] tmp = new String[vl];
    for(int v = 0; v < vl; v++) tmp[v] = Token.string(values[v]);
    return tmp;
  }

  @Override
  public Value reverse() {
    final int vl = values.length;
    final byte[][] tmp = new byte[vl][];
    for(int l = 0, r = vl - 1; l < vl; l++, r--) tmp[l] = values[r];
    return get(tmp);
  }

  @Override
  public Seq insert(final long pos, final Item item) {
    if(!(item instanceof Str)) return copyInsert(pos, item);

    final int p = (int) pos, n = values.length;
    final byte[] val = ((Str) item).string();
    final byte[][] out = new byte[n + 1][];
    System.arraycopy(values, 0, out, 0, p);
    out[p] = val;
    System.arraycopy(values, p, out, p + 1, n - p);
    return new StrSeq(out);
  }

  @Override
  public Value remove(final long pos) {
    final int p = (int) pos, n = values.length - 1;
    if(n == 1) return Str.get(values[1 - p]);
    final byte[][] out = new byte[n][];
    System.arraycopy(values, 0, out, 0, p);
    System.arraycopy(values, p + 1, out, p, n - p);
    return new StrSeq(out);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items (will be invalidated by this call)
   * @return value
   */
  public static Value get(final TokenList items) {
    return items.isEmpty() ? Empty.SEQ : items.size() == 1 ? Str.get(items.get(0)) :
      new StrSeq(items.finish());
  }

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  public static Value get(final byte[][] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Str.get(items[0]) :
      new StrSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final byte[][] tmp = new byte[size][];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof StrSeq) {
        final StrSeq sq = (StrSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).string(null);
      }
    }
    return get(tmp);
  }
}
