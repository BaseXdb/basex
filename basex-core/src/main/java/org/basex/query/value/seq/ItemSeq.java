package org.basex.query.value.seq;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ItemSeq extends Seq {
  /** Item array. */
  private final Item[] item;
  /** Item Types. */
  private Type ret;

  /**
   * Constructor.
   * @param it items
   * @param s size
   */
  private ItemSeq(final Item[] it, final int s) {
    super(s);
    item = it;
  }

  /**
   * Constructor.
   * @param it items
   * @param s size
   * @param t sequence type
   */
  ItemSeq(final Item[] it, final int s, final Type t) {
    this(it, s);
    ret = t;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) throws QueryException {
    if(item[0] instanceof ANode) return item[0];
    throw CONDTYPE.get(ii, this);
  }

  @Override
  public SeqType type() {
    if(ret == null) {
      Type t = item[0].type;
      for(int s = 1; s < size; s++) {
        if(t != item[s].type) {
          t = AtomType.ITEM;
          break;
        }
      }
      ret = t;
      type = t;
    }
    return SeqType.get(ret, Occ.ONE_MORE);
  }

  @Override
  public boolean iterable() {
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof ItemSeq)) return false;
    final ItemSeq is = (ItemSeq) cmp;
    return item == is.item && size == is.size;
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    System.arraycopy(item, 0, arr, start, (int) size);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return item[(int) pos];
  }

  @Override
  public boolean homogeneous() {
    return ret != null && ret != AtomType.ITEM;
  }

  @Override
  public Value reverse() {
    final int s = item.length;
    final Item[] tmp = new Item[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = item[r];
    return get(tmp, s, type);
  }
}
