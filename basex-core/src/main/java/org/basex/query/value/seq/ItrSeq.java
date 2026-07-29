package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Sequence of integer items, stored in the narrowest native representation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ItrSeq extends NativeSeq {
  /**
   * Constructor.
   * @param size number of items
   * @param type item type
   */
  ItrSeq(final int size, final Type type) {
    super(size, type);
  }

  /**
   * Returns the value at the specified index.
   * @param index index
   * @return value
   */
  public abstract long itrAt(int index);

  /**
   * Returns the number of bytes that is required for storing a single value.
   * @return width
   */
  abstract int width();

  @Override
  public Object toJava() throws QueryException {
    final int sz = (int) size;
    return switch((BasicType) type) {
      case BYTE -> {
        final ByteList list = new ByteList(sz);
        for(int i = 0; i < sz; i++) list.add((int) itrAt(i));
        yield list.finish();
      }
      case SHORT, UNSIGNED_BYTE -> {
        final ShortList list = new ShortList(sz);
        for(int i = 0; i < sz; i++) list.add((short) itrAt(i));
        yield list.finish();
      }
      case UNSIGNED_SHORT -> {
        final char[] chars = new char[sz];
        for(int i = 0; i < sz; i++) chars[i] = (char) itrAt(i);
        yield chars;
      }
      case INT -> {
        final IntList list = new IntList(sz);
        for(int i = 0; i < sz; i++) list.add((int) itrAt(i));
        yield list.finish();
      }
      default -> {
        final LongList list = new LongList(sz);
        for(int i = 0; i < sz; i++) list.add(itrAt(i));
        yield list.finish();
      }
    };
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {

    if(pos == 0) return super.test(qc, ii, pos);
    final int sz = (int) size;
    for(int i = 0; i < sz; i++) {
      if(itrAt(i) == pos) return true;
    }
    return false;
  }

  @Override
  public final Value shrink(final QueryContext qc) {
    // narrow xs:integer values to the smallest fitting representation
    final int width = width();
    if(type != BasicType.INTEGER || width == 1) return this;
    final int sz = (int) size;
    long min = itrAt(0), max = min;
    for(int i = 1; i < sz; i++) {
      if((i & 0xFFF) == 0) qc.checkStop();
      final long v = itrAt(i);
      if(v < min) min = v;
      else if(v > max) max = v;
      // stop as soon as the values do not fit a narrower representation
      else continue;
      if(minWidth(min, max) >= width) return this;
    }
    final int w = minWidth(min, max);
    if(w >= width) return this;

    if(w == 1) {
      final byte[] tmp = new byte[sz];
      for(int i = 0; i < sz; i++) tmp[i] = (byte) itrAt(i);
      return BytSeq.get(tmp, type);
    }
    if(w == 2) {
      final short[] tmp = new short[sz];
      for(int i = 0; i < sz; i++) tmp[i] = (short) itrAt(i);
      return ShrSeq.get(tmp, type);
    }
    final int[] tmp = new int[sz];
    for(int i = 0; i < sz; i++) tmp[i] = (int) itrAt(i);
    return IntSeq.get(tmp, type);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    final int sz = (int) size;
    int[] tmp = null;
    // the rewritings below are limited to values within the int range
    if(width() <= 4) {
      if(mode == Simplify.PREDICATE) {
        // remove duplicates, order data: (2, 1, 2) → 1 to 2
        final IntList list = new IntList(sz);
        for(int i = 0; i < sz; i++) list.add((int) itrAt(i));
        tmp = list.ddo().finish();
      } else if(mode == Simplify.DISTINCT) {
        // remove duplicates, but preserve order: (2, 1, 2) → (2, 1)
        final IntSet set = new IntSet(sz);
        for(int i = 0; i < sz; i++) set.add((int) itrAt(i));
        tmp = set.keys();
      }
    }
    if(tmp != null) {
      final int tl = tmp.length;
      int t = 0;
      if(type == BasicType.INTEGER) {
        while(++t < tl && tmp[0] + t == tmp[t]);
      }
      if(t == tl) expr = RangeSeq.get(tmp[0], tl, true);
      else if(tl != sz) expr = IntSeq.get(tmp, type);
    }

    return cc.simplify(this, expr, mode);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Returns the number of bytes that is required for storing the specified value range.
   * @param min smallest value
   * @param max largest value
   * @return width
   */
  public static int minWidth(final long min, final long max) {
    return min >= Byte.MIN_VALUE && max <= Byte.MAX_VALUE ? 1 :
           min >= Short.MIN_VALUE && max <= Short.MAX_VALUE ? 2 :
           min >= Integer.MIN_VALUE && max <= Integer.MAX_VALUE ? 4 : 8;
  }

  /**
   * Replaces the specified sequence with a singleton sequence or range if all values are
   * identical or consecutive.
   * @param seq sequence with at least two values
   * @return resulting value
   */
  static Value refine(final ItrSeq seq) {
    final int sz = (int) seq.size;
    final long first = seq.itrAt(0);
    // ranges are limited to xs:integer; other types would lose their identity
    final boolean range = seq.type == BasicType.INTEGER;
    boolean same = true, asc = range, desc = range;
    for(int v = 1; v < sz && (same || asc || desc); v++) {
      final long i = seq.itrAt(v);
      if(same && i != first) same = false;
      if(asc  && i != first + v) asc = false;
      if(desc && i != first - v) desc = false;
    }
    return same ? SingletonSeq.get(Itr.get(first, seq.type), sz) :
      asc || desc ? RangeSeq.get(first, sz, asc) : seq;
  }
}
