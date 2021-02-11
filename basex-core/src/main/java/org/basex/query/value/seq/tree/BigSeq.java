package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence containing more elements than fit into a {@link SmallSeq}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class BigSeq extends TreeSeq {
  /** Left digit. */
  final Item[] left;
  /** Middle tree. */
  final FingerTree<Item, Item> middle;
  /** Right digit. */
  final Item[] right;

  /**
   * Constructor.
   * @param left left digit
   * @param middle middle tree
   * @param right right digit
   * @param type type of all items in this sequence, can be {@code null}
   */
  BigSeq(final Item[] left, final FingerTree<Item, Item> middle, final Item[] right,
      final Type type) {
    super(left.length + middle.size() + right.length, type);
    this.left = left;
    this.middle = middle;
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
  }

  @Override
  public Item itemAt(final long index) {
    // index in one of the digits?
    if(index < left.length) return left[(int) index];
    final long midSize = size - right.length;
    if(index >= midSize) return right[(int) (index - midSize)];

    // the element is in the middle tree
    return middle.get(index - left.length);
  }

  @Override
  public TreeSeq reverse(final QueryContext qc) {
    qc.checkStop();
    final int l = left.length, r = right.length;
    final Item[] newLeft = new Item[r], newRight = new Item[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i];
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i];
    return new BigSeq(newLeft, middle.reverse(qc), newRight, type);
  }

  @Override
  public TreeSeq insert(final long pos, final Item item, final QueryContext qc) {
    qc.checkStop();
    final Type tp = type.union(item.type);
    final int l = left.length;
    if(pos <= l) {
      final int p = (int) pos;
      final Item[] temp = slice(left, 0, l + 1);
      Array.copy(temp, p, l - p, temp, p + 1);
      temp[p] = item;
      if(l < MAX_DIGIT) return new BigSeq(temp, middle, right, tp);

      final int m = (l + 1) / 2;
      return new BigSeq(slice(temp, 0, m),
          middle.cons(new LeafNode(slice(temp, m, l + 1))), right, tp);
    }

    final long midSize = middle.size();
    if(pos - l < midSize) return new BigSeq(left, middle.insert(pos - l, item, qc), right, tp);

    final int r = right.length;
    final int p = (int) (pos - l - midSize);
    final Item[] temp = slice(right, 0, r + 1);
    Array.copy(temp, p, r - p, temp, p + 1);
    temp[p] = item;
    if(r < MAX_DIGIT) return new BigSeq(left, middle, temp, tp);

    final int m = (r + 1) / 2;
    return new BigSeq(left, middle.snoc(new LeafNode(slice(temp, 0, m))),
        slice(temp, m, r + 1), tp);
  }

  @Override
  public TreeSeq remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    if(pos < left.length) {
      // delete from left digit
      final int p = (int) pos, l = left.length;
      if(l > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Item[] newLeft = new Item[l - 1];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, newLeft.length - p, newLeft, p);
        return new BigSeq(newLeft, middle, right, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int r = right.length, n = l - 1 + r;
        final Item[] vals = new Item[n];
        Array.copy(left, p, vals);
        Array.copy(left, p + 1, l - 1 - p, vals, p);
        Array.copyFromStart(right, r, vals, l - 1);
        return fromMerged(vals);
      }

      // extract a new left digit from the middle
      final Item[] head = ((LeafNode) middle.head()).values;
      final int r = head.length, n = l - 1 + r;

      if(r > MIN_LEAF) {
        // refill from neighbor
        final int move = (r - MIN_LEAF + 1) / 2;
        final Item[] newLeft = new Item[l - 1 + move];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, l - 1 - p, newLeft, p);
        Array.copyFromStart(head, move, newLeft, l - 1);
        final Item[] newHead = slice(head, move, r);
        return new BigSeq(newLeft, middle.replaceHead(new LeafNode(newHead)), right, type);
      }

      // merge digit and head node
      final Item[] newLeft = new Item[n];
      Array.copy(left, p, newLeft);
      Array.copy(left, p + 1, l - 1 - p, newLeft, p);
      Array.copyFromStart(head, r, newLeft, l - 1);
      return new BigSeq(newLeft, middle.tail(), right, type);
    }

    final long midSize = middle.size(), rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // delete from right digit
      final int p = (int) (pos - rightOffset), r = right.length;
      if(r > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Item[] newRight = new Item[r - 1];
        Array.copy(right, p, newRight);
        Array.copy(right, p + 1, r - 1 - p, newRight, p);
        return new BigSeq(left, middle, newRight, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int l = left.length, n = l + r - 1;
        final Item[] vals = new Item[n];
        Array.copy(left, l, vals);
        Array.copyFromStart(right, p, vals, l);
        Array.copy(right, p + 1, r - 1 - p, vals, l + p);
        return fromMerged(vals);
      }

      // extract a new right digit from the middle
      final Item[] last = ((LeafNode) middle.last()).values;
      final int l = last.length, n = l + r - 1;

      if(l > MIN_LEAF) {
        // refill from neighbor
        final int move = (l - MIN_LEAF + 1) / 2;
        final Item[] newLast = slice(last, 0, l - move);
        final Item[] newRight = new Item[r - 1 + move];
        Array.copyToStart(last, l - move, move, newRight);
        Array.copyFromStart(right, p, newRight, move);
        Array.copy(right, p + 1, r - 1 - p, newRight, move + p);
        return new BigSeq(left, middle.replaceLast(new LeafNode(newLast)), newRight, type);
      }

      // merge last node and digit
      final Item[] newRight = new Item[n];
      Array.copy(last, l, newRight);
      Array.copyFromStart(right, p, newRight, l);
      Array.copy(right, p + 1, r - 1 - p, newRight, l + p);
      return new BigSeq(left, middle.init(), newRight, type);
    }

    // delete in middle tree
    final TreeSlice<Item, Item> slice = middle.remove(pos - left.length, qc);

    if(slice.isTree()) {
      // middle tree did not underflow
      return new BigSeq(left, slice.getTree(), right, type);
    }

    // tree height might change
    final Item[] mid = ((PartialLeafNode) slice.getPartial()).elems;
    final int l = left.length, m = mid.length, r = right.length;

    if(l > r) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (l - MIN_DIGIT + 1) / 2;
      final Item[] newLeft = slice(left, 0, l - move);
      final Item[] newMid = slice(left, l - move, l + m);
      Array.copyFromStart(mid, m, newMid, move);
      return new BigSeq(newLeft, FingerTree.singleton(new LeafNode(newMid)), right, type);
    }

    if(r > MIN_DIGIT) {
      // steal from right digit
      final int move = (r - MIN_DIGIT + 1) / 2;
      final Item[] newMid = slice(mid, 0, m + move);
      Array.copyFromStart(right, move, newMid, m);
      final Item[] newRight = slice(right, move, r);
      return new BigSeq(left, FingerTree.singleton(new LeafNode(newMid)), newRight, type);
    }

    // divide onto left and right digit
    final int ml = m / 2, mr = m - ml;
    final Item[] newLeft = slice(left, 0, l + ml);
    Array.copyFromStart(mid, ml, newLeft, l);
    final Item[] newRight = slice(right, -mr, r);
    Array.copyToStart(mid, ml, mr, newRight);
    return new BigSeq(newLeft, FingerTree.empty(), newRight, type);
  }

  @Override
  protected Seq subSeq(final long offset, final long length, final QueryContext qc) {
    qc.checkStop();
    final long midSize = middle.size();
    final long end = offset + length;
    if(end <= left.length) {
      // completely in left digit
      final int p = (int) offset, n = (int) length;
      if(length <= MAX_SMALL) return new SmallSeq(slice(left, p, p + n), type);
      final int mid = p + n / 2;
      return new BigSeq(slice(left, p, mid), FingerTree.empty(), slice(left, mid, p + n), type);
    }

    final long rightOffset = left.length + midSize;
    if(offset >= rightOffset) {
      // completely in right digit
      final int p = (int) (offset - rightOffset), n = (int) length;
      if(length <= MAX_SMALL) return new SmallSeq(slice(right, p, p + n), type);
      final int mid = p + n / 2;
      return new BigSeq(slice(right, p, mid), FingerTree.empty(), slice(right, mid, p + n), type);
    }

    final int inLeft = offset < left.length ? (int) (left.length - offset) : 0,
        inRight = end > rightOffset ? (int) (end - rightOffset) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final Item[] newLeft = inLeft == left.length ? left : slice(left, (int) offset, left.length);
      final Item[] newRight = inRight == right.length ? right : slice(right, 0, inRight);
      return new BigSeq(newLeft, middle, newRight, type);
    }

    if(middle.isEmpty()) {
      // merge left and right partial digits
      final Item[] out;
      if(inLeft == 0) {
        out = inRight == right.length ? right : slice(right, 0, inRight);
      } else if(inRight == 0) {
        out = inLeft == left.length ? left : slice(left, left.length - inLeft, left.length);
      } else {
        out = slice(left, left.length - inLeft, left.length + inRight);
        Array.copyFromStart(right, inRight, out, inLeft);
      }
      return fromMerged(out);
    }

    final long inMiddle = length - inLeft - inRight;
    final FingerTree<Item, Item> mid;
    if(inMiddle == midSize) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = offset < left.length ? 0 : offset - left.length;
      final TreeSlice<Item, Item> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(!slice.isTree()) {
        final Item[] single = ((PartialLeafNode) slice.getPartial()).elems;
        if(inLeft > 0) {
          final Item[] out = slice(left, (int) offset, left.length + single.length);
          Array.copyFromStart(single, single.length, out, inLeft);
          return fromMerged(out);
        }
        if(inRight > 0) {
          final Item[] out = slice(single, 0, single.length + inRight);
          Array.copyFromStart(right, inRight, out, single.length);
          return fromMerged(out);
        }
        return new SmallSeq(single, type);
      }

      mid = slice.getTree();
    }

    // `mid` is non-empty

    // create a left digit
    final int off = left.length - inLeft;
    final Item[] newLeft;
    final FingerTree<Item, Item> mid1;
    if(inLeft >= MIN_DIGIT) {
      newLeft = inLeft == left.length ? left : slice(left, off, left.length);
      mid1 = mid;
    } else {
      final Item[] head = ((LeafNode) mid.head()).values;
      if(inLeft == 0) {
        newLeft = head;
      } else {
        newLeft = slice(head, -inLeft, head.length);
        Array.copyToStart(left, off, inLeft, newLeft);
      }
      mid1 = mid.tail();
    }

    // create a right digit
    final Item[] newRight;
    final FingerTree<Item, Item> newMiddle;
    if(inRight >= MIN_DIGIT) {
      newMiddle = mid1;
      newRight = inRight == right.length ? right : slice(right, 0, inRight);
    } else if(!mid1.isEmpty()) {
      final Item[] last = ((LeafNode) mid1.last()).values;
      newMiddle = mid1.init();
      if(inRight == 0) {
        newRight = last;
      } else {
        newRight = slice(last, 0, last.length + inRight);
        Array.copyFromStart(right, inRight, newRight, last.length);
      }
    } else {
      // not enough elements for a right digit
      if(inRight == 0) return fromMerged(newLeft);
      final int n = newLeft.length + inRight;
      final Item[] out = slice(newLeft, 0, n);
      Array.copyFromStart(right, inRight, out, newLeft.length);
      return fromMerged(out);
    }

    return new BigSeq(newLeft, newMiddle, newRight, type);
  }

  /**
   * Creates a sequence from two merged, possibly partial digits.
   * This method requires that the input array's length is not longer than {@code 2 * MAX_DIGIT}.
   * @param merged the merged digits
   * @return the array
   */
  private TreeSeq fromMerged(final Item[] merged) {
    if(merged.length <= MAX_SMALL) return new SmallSeq(merged, type);
    final int mid = merged.length / 2;
    return new BigSeq(slice(merged, 0, mid), FingerTree.empty(),
        slice(merged, mid, merged.length), type);
  }

  @Override
  public TreeSeq concat(final TreeSeq seq) {
    final Type tp = type.eq(seq.type) ? type : null;
    if(seq instanceof SmallSeq) {
      // merge with right digit
      final Item[] newRight = concat(right, ((SmallSeq) seq).items);
      final int r = newRight.length;
      if(r <= MAX_DIGIT) return new BigSeq(left, middle, newRight, tp);
      final int mid = r / 2;
      final Item[] leaf = slice(newRight, 0, mid);
      final FingerTree<Item, Item> newMid = middle.snoc(new LeafNode(leaf));
      return new BigSeq(left, newMid, slice(newRight, mid, r), tp);
    }

    final BigSeq bigOther = (BigSeq) seq;

    // make nodes out of the digits facing each other
    final Item[] ls = right, rs = bigOther.left;
    final int l = ls.length, n = l + rs.length;
    final int k = (n + MAX_LEAF - 1) / MAX_LEAF, s = (n + k - 1) / k;
    @SuppressWarnings("unchecked")
    final Node<Item, Item>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int curr = Math.min(n - p, s);
      final Item[] arr = new Item[curr];
      for(int j = 0; j < curr; j++, p++) arr[j] = p < l ? ls[p] : rs[p - l];
      midNodes[i] = new LeafNode(arr);
    }

    return new BigSeq(left, middle.concat(midNodes, n, bigOther.middle), bigOther.right, tp);
  }

  @Override
  public ListIterator<Item> iterator(final long start) {
    final Item[] ls = left, rs = right;
    final int l = ls.length , r = rs.length, startPos;
    final long m = middle.size();
    final ListIterator<Item> sub;
    if(start < l) {
      startPos = (int) start - l;
      sub = middle.listIterator(0);
    } else if(start - l < m) {
      startPos = 0;
      sub = middle.listIterator(start - l);
    } else {
      startPos = (int) (start - l - m) + 1;
      sub = middle.listIterator(m);
    }

    return new ListIterator<Item>() {
      int pos = startPos;

      @Override
      public int nextIndex() {
        return pos < 0 ? l + pos
             : pos > 0 ? (int) (l + m + pos - 1)
                       : l + sub.nextIndex();
      }

      @Override
      public boolean hasNext() {
        return pos <= r;
      }

      @Override
      public Item next() {
        if(pos < 0) {
          // in left digit
          return ls[l + pos++];
        }

        if(pos == 0) {
          // in middle tree
          if(sub.hasNext()) return sub.next();
          pos = 1;
        }

        // in right digit
        return rs[pos++ - 1];
      }

      @Override
      public int previousIndex() {
        return pos < 0 ? l + pos - 1
             : pos > 0 ? (int) (l + m + pos - 2)
                       : l + sub.previousIndex();
      }

      @Override
      public boolean hasPrevious() {
        return pos > -l;
      }

      @Override
      public Item previous() {
        if(pos > 0) {
          // in right digit
          if(--pos > 0) return rs[pos - 1];
        }

        if(pos == 0) {
          // in middle tree
          if(sub.hasPrevious()) return sub.previous();
          pos = -1;
          return ls[l - 1];
        }

        // in left digit
        return ls[l + --pos];
      }

      @Override
      public void add(final Item e) {
        throw Util.notExpected();
      }

      @Override
      public void set(final Item e) {
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
    return new BasicIter<Item>(size) {
      private Iterator<Item> sub;

      @Override
      public Item next() {
        if(pos >= size) return null;
        final long p = pos++;
        if(p < left.length) return left[(int) p];
        final long r = size - right.length;
        if(p >= r) return right[(int) (p - r)];
        if(sub == null) sub = middle.iterator();
        return sub.next();
      }

      @Override
      public Value iterValue() {
        return BigSeq.this;
      }

      @Override
      public Item get(final long i) {
        return itemAt(i);
      }

      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return BigSeq.this;
      }
    };
  }

  @Override
  TreeSeq prepend(final SmallSeq seq) {
    final Type tp = type.union(seq.type);
    final Item[] values = seq.items;
    final int l = values.length, b = left.length, n = l + b;

    // no need to change the middle tree
    if(n <= MAX_DIGIT) return new BigSeq(concat(values, left), middle, right, tp);
    // reuse the arrays
    if(l >= MIN_DIGIT && MIN_LEAF <= b && b <= MAX_LEAF)
      return new BigSeq(values, middle.cons(new LeafNode(left)), right, tp);

    // left digit is too big
    final int mid = n / 2, move = mid - l;
    final Item[] newLeft = slice(values, 0, mid);
    Array.copyFromStart(left, move, newLeft, l);
    final LeafNode leaf = new LeafNode(slice(left, move, b));
    return new BigSeq(newLeft, middle.cons(leaf), right, tp);
  }
}
