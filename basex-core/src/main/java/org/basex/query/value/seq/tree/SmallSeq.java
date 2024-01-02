package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A small sequence that is stored in a single Java array.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class SmallSeq extends TreeSeq {
  /** The items. */
  final Item[] items;

  /**
   * Constructor.
   * @param items items
   * @param type item type
   */
  SmallSeq(final Item[] items, final Type type) {
    super(items.length, type);
    this.items = items;
    assert items.length >= 2 && items.length <= MAX_SMALL;
  }

  @Override
  public Item itemAt(final long index) {
    return items[(int) index];
  }

  @Override
  public TreeSeq reverse(final QueryContext qc) {
    final int il = items.length;
    final Item[] tmp = new Item[il];
    for(int i = 0; i < il; i++) tmp[i] = items[il - 1 - i];
    return new SmallSeq(tmp, type);
  }

  @Override
  public TreeSeq insertBefore(final long pos, final Item item, final QueryContext qc) {
    qc.checkStop();
    final Type tp = type.union(item.type);
    final int p = (int) pos, il = items.length;
    final Item[] out = new Item[il + 1];
    Array.copy(items, p, out);
    out[p] = item;
    Array.copy(items, p, il - p, out, p + 1);

    return il < MAX_SMALL ? new SmallSeq(out, tp) :
      new BigSeq(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, il + 1), tp);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int il = items.length, p = (int) pos;
    if(il == 2) return items[p == 0 ? 1 : 0];

    final Item[] out = new Item[il - 1];
    Array.copy(items, p, out);
    Array.copy(items, p + 1, il - 1 - p, out, p);
    return new SmallSeq(out, type);
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, n = (int) length;
    return new SmallSeq(slice(items, p, p + n), type);
  }

  @Override
  public TreeSeq concat(final TreeSeq other) {
    return other.prepend(this);
  }

  @Override
  public ListIterator<Item> iterator(final long start) {
    return new ListIterator<>() {
      private int index = (int) start;

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < items.length;
      }

      @Override
      public Item next() {
        return items[index++];
      }

      @Override
      public int previousIndex() {
        return index - 1;
      }

      @Override
      public boolean hasPrevious() {
        return index > 0;
      }

      @Override
      public Item previous() {
        return items[--index];
      }

      @Override
      public void set(final Item e) {
        throw Util.notExpected();
      }

      @Override
      public void add(final Item e) {
        throw Util.notExpected();
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<>(size) {
      @Override
      public Item get(final long i) {
        return items[(int) i];
      }
      @Override
      public boolean valueIter() {
        return true;
      }
      @Override
      public SmallSeq value(final QueryContext qc, final Expr expr) {
        return SmallSeq.this;
      }
    };
  }

  @Override
  TreeSeq prepend(final SmallSeq seq) {
    final Type tp = type.union(seq.type);
    final Item[] tmp = seq.items;
    final int tl = tmp.length, il = items.length, n = tl + il;

    // both arrays can be used as digits
    if(Math.min(tl, il) >= MIN_DIGIT) return new BigSeq(tmp, items, tp);

    final Item[] out = new Item[n];
    Array.copy(tmp, tl, out);
    Array.copyFromStart(items, il, out, tl);
    if(n <= MAX_SMALL) return new SmallSeq(out, tp);

    final int mid = n / 2;
    return new BigSeq(slice(out, 0, mid), slice(out, mid, n), tp);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof SmallSeq ? Arrays.equals(items, ((SmallSeq) obj).items) :
      super.equals(obj));
  }
}
