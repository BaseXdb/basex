package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Flat item sequence.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ItemSeq extends Seq {
  /** Items. */
  private final Item[] items;

  /**
   * Constructor.
   * @param items items
   * @param size number of items
   * @param type node type
   */
  private ItemSeq(final Item[] items, final int size, final Type type) {
    super(size, type);
    this.items = items;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item head = items[0];
    if(head instanceof ANode) return head;
    throw EBV_X.get(ii, this);
  }

  @Override
  public Item itemAt(final long pos) {
    return items[(int) pos];
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : this) vb.add(item.atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final Item[] tmp = new Item[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = items[i];
    return new ItemSeq(tmp, sz, type);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof ItemSeq ? Array.equals(items, ((ItemSeq) obj).items) :
      super.equals(obj));
  }

  @Override
  public Value insert(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return copyRemove(pos, qc);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Item item : this) item.cache(lazy, ii);
  }

  @Override
  public long atomSize() {
    long sz = 0;
    for(final Item item : this) sz += item.atomSize();
    return sz;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @param type type (can be {@code null}, only considered if new sequence is created)
   * @return value
   */
  public static Value get(final Item[] values, final int size, final Type type) {
    return size == 0 ? Empty.VALUE : size == 1 ? values[0] :
      new ItemSeq(values, size, type != null ? type : AtomType.ITEM);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @param expr expression (can be {@code null}, only considered if new sequence is created)
   * @return value
   */
  public static Value get(final Item[] values, final int size, final Expr expr) {
    return get(values, size, expr != null ? expr.seqType().type : null);
  }
}
