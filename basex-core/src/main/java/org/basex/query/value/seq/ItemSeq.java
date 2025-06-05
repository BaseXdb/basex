package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Flat item sequence.
 *
 * @author BaseX Team, BSD License
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
  public Item itemAt(final long index) {
    return items[(int) index];
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final Item[] tmp = new Item[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = items[i];
    return new ItemSeq(tmp, sz, type);
  }

  @Override
  public Value shrink(final QueryContext qc) throws QueryException {
    // see ValueBuilder#add for types with compact representation
    for(int i = 0; i < size; i++) items[i] = items[i].rebuild(qc);
    refineType();
    return type.oneOf(AtomType.STRING, AtomType.UNTYPED_ATOMIC, AtomType.INTEGER, AtomType.DOUBLE,
        AtomType.BOOLEAN) ? rebuild(qc) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final ItemSeq seq ? Array.equals(items, seq.items) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param items items
   * @param type type (can be {@code null}, only considered if new sequence is created)
   * @return value
   */
  public static Value get(final Item[] items, final int size, final Type type) {
    return size == 0 ? Empty.VALUE : size == 1 ? items[0] :
      new ItemSeq(items, size, type != null ? type : AtomType.ITEM);
  }
}
