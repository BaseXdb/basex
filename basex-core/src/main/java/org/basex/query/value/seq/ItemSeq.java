package org.basex.query.value.seq;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ItemSeq extends Seq {
  /** Item array. */
  private final Item[] items;
  /** Item Types. */
  private Type ret;

  /**
   * Constructor.
   * @param items items
   * @param size size
   */
  private ItemSeq(final Item[] items, final int size) {
    super(size);
    this.items = items;
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
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(items[0] instanceof ANode) return items[0];
    throw EBV.get(ii, this);
  }

  @Override
  public SeqType seqType() {
    if(ret == null) {
      Type t = items[0].type;
      for(int s = 1; s < size; s++) {
        if(t != items[s].type) {
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
    return items == is.items && size == is.size;
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    System.arraycopy(items, 0, arr, index, (int) size);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return items[(int) pos];
  }

  @Override
  public boolean homogeneous() {
    return ret != null && ret != AtomType.ITEM;
  }

  @Override
  public Value reverse() {
    final int s = items.length;
    final Item[] tmp = new Item[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = items[r];
    return get(tmp, s, type);
  }

  @Override
  public boolean has(final Flag flag) {
    if(flag == Flag.UPD) {
      for(int l = 0; l < size; l++) {
        if(items[l].has(Flag.UPD)) return true;
      }
    }
    return false;
  }

  @Override
  public Value materialize(final InputInfo ii) throws QueryException {
    final int s = (int) size;
    final ValueBuilder vb = new ValueBuilder(s);
    for(int i = 0; i < s; i++) vb.add(itemAt(i).materialize(ii));
    return vb.value();
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    final int s = (int) size;
    final ValueBuilder vb = new ValueBuilder(s);
    for(int i = 0; i < s; i++) vb.add(itemAt(i).atomValue(ii));
    return vb.value();
  }

  @Override
  public long atomSize() {
    long s = 0;
    for(int i = 0; i < size; i++) s += itemAt(i).atomSize();
    return s;
  }
}
