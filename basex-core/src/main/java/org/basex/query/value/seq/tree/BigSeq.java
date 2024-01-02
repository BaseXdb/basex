package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence containing more items than fit into a {@link SmallSeq}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class BigSeq extends TreeSeq {
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
   * @param type item type
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

  /**
   * Constructor for sequences with an empty middle tree.
   * @param left left digit
   * @param right right digit
   * @param type item type
   */
  BigSeq(final Item[] left, final Item[] right, final Type type) {
    this(left, FingerTree.empty(), right, type);
  }

  @Override
  public Item itemAt(final long index) {
    // index in one of the digits?
    final int ll = left.length;
    if(index < ll) return left[(int) index];

    final long me = size - right.length;
    if(index >= me) return right[(int) (index - me)];

    // the item is in the middle tree
    return middle.get(index - ll);
  }

  @Override
  public TreeSeq reverse(final QueryContext qc) {
    qc.checkStop();
    final int ll = left.length, rl = right.length;
    final Item[] newLeft = new Item[rl], newRight = new Item[ll];
    for(int i = 0; i < rl; i++) newLeft[i] = right[rl - 1 - i];
    for(int i = 0; i < ll; i++) newRight[i] = left[ll - 1 - i];
    return new BigSeq(newLeft, middle.reverse(qc), newRight, type);
  }

  @Override
  public TreeSeq insertBefore(final long pos, final Item item, final QueryContext qc) {
    qc.checkStop();
    final Type tp = type.union(item.type);
    final int ll = left.length;
    if(pos <= ll) {
      final int p = (int) pos;
      final Item[] temp = slice(left, 0, ll + 1);
      Array.copy(temp, p, ll - p, temp, p + 1);
      temp[p] = item;
      if(ll < MAX_DIGIT) return new BigSeq(temp, middle, right, tp);

      final int m = (ll + 1) / 2;
      return new BigSeq(slice(temp, 0, m),
          middle.prepend(new LeafNode(slice(temp, m, ll + 1))), right, tp);
    }

    final long ms = middle.size();
    if(pos - ll < ms) return new BigSeq(left, middle.insert(pos - ll, item, qc), right, tp);

    final int rl = right.length, p = (int) (pos - ll - ms);
    final Item[] temp = slice(right, 0, rl + 1);
    Array.copy(temp, p, rl - p, temp, p + 1);
    temp[p] = item;
    if(rl < MAX_DIGIT) return new BigSeq(left, middle, temp, tp);

    final int m = (rl + 1) / 2;
    return new BigSeq(left, middle.append(new LeafNode(slice(temp, 0, m))),
        slice(temp, m, rl + 1), tp);
  }

  @Override
  public TreeSeq remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int ll = left.length, rl = right.length;
    if(pos < ll) {
      // delete from left digit
      final int p = (int) pos;
      if(ll > MIN_DIGIT) {
        // there is enough space, just delete the item
        final Item[] newLeft = new Item[ll - 1];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, newLeft.length - p, newLeft, p);
        return new BigSeq(newLeft, middle, right, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int n = ll - 1 + rl;
        final Item[] vals = new Item[n];
        Array.copy(left, p, vals);
        Array.copy(left, p + 1, ll - 1 - p, vals, p);
        Array.copyFromStart(right, rl, vals, ll - 1);
        return fromMerged(vals);
      }

      // extract a new left digit from the middle
      final Item[] head = ((LeafNode) middle.head()).values;
      final int hl = head.length, n = ll - 1 + hl;

      if(hl > MIN_LEAF) {
        // refill from neighbor
        final int move = (hl - MIN_LEAF + 1) / 2;
        final Item[] newLeft = new Item[ll - 1 + move];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, ll - 1 - p, newLeft, p);
        Array.copyFromStart(head, move, newLeft, ll - 1);
        final Item[] newHead = slice(head, move, hl);
        return new BigSeq(newLeft, middle.replaceHead(new LeafNode(newHead)), right, type);
      }

      // merge digit and head node
      final Item[] newLeft = new Item[n];
      Array.copy(left, p, newLeft);
      Array.copy(left, p + 1, ll - 1 - p, newLeft, p);
      Array.copyFromStart(head, hl, newLeft, ll - 1);
      return new BigSeq(newLeft, middle.tail(), right, type);
    }

    final long ms = middle.size(), ro = left.length + ms;
    if(pos >= ro) {
      // delete from right digit
      final int p = (int) (pos - ro);
      if(rl > MIN_DIGIT) {
        // there is enough space, just delete the item
        final Item[] newRight = new Item[rl - 1];
        Array.copy(right, p, newRight);
        Array.copy(right, p + 1, rl - 1 - p, newRight, p);
        return new BigSeq(left, middle, newRight, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int n = ll + rl - 1;
        final Item[] vals = new Item[n];
        Array.copy(left, ll, vals);
        Array.copyFromStart(right, p, vals, ll);
        Array.copy(right, p + 1, rl - 1 - p, vals, ll + p);
        return fromMerged(vals);
      }

      // extract a new right digit from the middle
      final Item[] last = ((LeafNode) middle.foot()).values;
      final int sl = last.length, n = sl + rl - 1;

      if(sl > MIN_LEAF) {
        // refill from neighbor
        final int move = (sl - MIN_LEAF + 1) / 2;
        final Item[] newLast = slice(last, 0, sl - move), newRight = new Item[rl - 1 + move];
        Array.copyToStart(last, sl - move, move, newRight);
        Array.copyFromStart(right, p, newRight, move);
        Array.copy(right, p + 1, rl - 1 - p, newRight, move + p);
        return new BigSeq(left, middle.replaceLast(new LeafNode(newLast)), newRight, type);
      }

      // merge last node and digit
      final Item[] newRight = new Item[n];
      Array.copy(last, sl, newRight);
      Array.copyFromStart(right, p, newRight, sl);
      Array.copy(right, p + 1, rl - 1 - p, newRight, sl + p);
      return new BigSeq(left, middle.trunk(), newRight, type);
    }

    // delete in middle tree
    final TreeSlice<Item, Item> slice = middle.remove(pos - ll, qc);

    if(slice.isTree()) {
      // middle tree did not underflow
      return new BigSeq(left, slice.getTree(), right, type);
    }

    // tree height might change
    final Item[] mid = ((PartialLeafNode) slice.getPartial()).elems;
    final int ml = mid.length;

    if(ll > rl) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (ll - MIN_DIGIT + 1) / 2;
      final Item[] newLeft = slice(left, 0, ll - move), newMid = slice(left, ll - move, ll + ml);
      Array.copyFromStart(mid, ml, newMid, move);
      return new BigSeq(newLeft, FingerTree.singleton(new LeafNode(newMid)), right, type);
    }

    if(rl > MIN_DIGIT) {
      // steal from right digit
      final int move = (rl - MIN_DIGIT + 1) / 2;
      final Item[] newMid = slice(mid, 0, ml + move);
      Array.copyFromStart(right, move, newMid, ml);
      final Item[] newRight = slice(right, move, rl);
      return new BigSeq(left, FingerTree.singleton(new LeafNode(newMid)), newRight, type);
    }

    // divide onto left and right digit
    final int hl = ml / 2, mr = ml - hl;
    final Item[] newLeft = slice(left, 0, ll + hl);
    Array.copyFromStart(mid, hl, newLeft, ll);
    final Item[] newRight = slice(right, -mr, rl);
    Array.copyToStart(mid, hl, mr, newRight);
    return new BigSeq(newLeft, newRight, type);
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();

    // the easy cases
    final int ll = left.length, rl = right.length;
    final long ms = middle.size();
    final long end = pos + length;
    if(end <= ll) {
      // completely in left digit
      final int p = (int) pos, n = (int) length;
      if(length <= MAX_SMALL) return new SmallSeq(slice(left, p, p + n), type);
      final int mid = p + n / 2;
      return new BigSeq(slice(left, p, mid), slice(left, mid, p + n), type);
    }

    final long ro = ll + ms;
    if(pos >= ro) {
      // completely in right digit
      final int p = (int) (pos - ro), n = (int) length;
      if(length <= MAX_SMALL) return new SmallSeq(slice(right, p, p + n), type);
      final int mid = p + n / 2;
      return new BigSeq(slice(right, p, mid), slice(right, mid, p + n), type);
    }

    final int inLeft = pos < ll ? (int) (ll - pos) : 0,
        inRight = end > ro ? (int) (end - ro) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final Item[] newLeft = inLeft == ll ? left : slice(left, (int) pos, ll);
      final Item[] newRight = inRight == rl ? right : slice(right, 0, inRight);
      return new BigSeq(newLeft, middle, newRight, type);
    }

    if(middle.isEmpty()) {
      // merge left and right partial digits
      final Item[] out;
      if(inLeft == 0) {
        out = inRight == rl ? right : slice(right, 0, inRight);
      } else if(inRight == 0) {
        out = inLeft == ll ? left : slice(left, ll - inLeft, ll);
      } else {
        out = slice(left, ll - inLeft, ll + inRight);
        Array.copyFromStart(right, inRight, out, inLeft);
      }
      return fromMerged(out);
    }

    final long inMiddle = length - inLeft - inRight;
    final FingerTree<Item, Item> mid;
    if(inMiddle == ms) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = pos < ll ? 0 : pos - ll;
      final TreeSlice<Item, Item> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(!slice.isTree()) {
        final Item[] single = ((PartialLeafNode) slice.getPartial()).elems;
        final int sl = single.length;
        if(inLeft > 0) {
          final Item[] out = slice(left, (int) pos, ll + sl);
          Array.copyFromStart(single, sl, out, inLeft);
          return fromMerged(out);
        }
        if(inRight > 0) {
          final Item[] out = slice(single, 0, sl + inRight);
          Array.copyFromStart(right, inRight, out, sl);
          return fromMerged(out);
        }
        return new SmallSeq(single, type);
      }

      mid = slice.getTree();
    }

    // `mid` is non-empty

    // create a left digit
    final int off = ll - inLeft;
    final Item[] newLeft;
    final FingerTree<Item, Item> mid1;
    if(inLeft >= MIN_DIGIT) {
      newLeft = inLeft == ll ? left : slice(left, off, ll);
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
      newRight = inRight == rl ? right : slice(right, 0, inRight);
    } else if(!mid1.isEmpty()) {
      final Item[] last = ((LeafNode) mid1.foot()).values;
      final int sl = last.length;
      newMiddle = mid1.trunk();
      if(inRight == 0) {
        newRight = last;
      } else {
        newRight = slice(last, 0, sl + inRight);
        Array.copyFromStart(right, inRight, newRight, sl);
      }
    } else {
      // not enough items for a right digit
      if(inRight == 0) return fromMerged(newLeft);
      final int nll = newLeft.length, n = nll + inRight;
      final Item[] out = slice(newLeft, 0, n);
      Array.copyFromStart(right, inRight, out, nll);
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
    final int ml = merged.length;
    if(ml <= MAX_SMALL) return new SmallSeq(merged, type);
    final int mid = ml / 2;
    return new BigSeq(slice(merged, 0, mid), slice(merged, mid, ml), type);
  }

  @Override
  public TreeSeq concat(final TreeSeq seq) {
    final Type tp = type.union(seq.type);
    if(seq instanceof SmallSeq) {
      // merge with right digit
      final Item[] newRight = concat(right, ((SmallSeq) seq).items);
      final int nrl = newRight.length;
      if(nrl <= MAX_DIGIT) return new BigSeq(left, middle, newRight, tp);
      final int mid = nrl / 2;
      final Item[] leaf = slice(newRight, 0, mid);
      final FingerTree<Item, Item> newMid = middle.append(new LeafNode(leaf));
      return new BigSeq(left, newMid, slice(newRight, mid, nrl), tp);
    }

    // make nodes out of the digits facing each other
    final BigSeq other = (BigSeq) seq;
    final Item[] ls = right, rs = other.left;
    final int ns = ls.length, ne = ns + rs.length;
    final int k = (ne + MAX_LEAF - 1) / MAX_LEAF, s = (ne + k - 1) / k;
    @SuppressWarnings("unchecked")
    final Node<Item, Item>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int curr = Math.min(ne - p, s);
      final Item[] arr = new Item[curr];
      for(int j = 0; j < curr; j++, p++) arr[j] = p < ns ? ls[p] : rs[p - ns];
      midNodes[i] = new LeafNode(arr);
    }

    return new BigSeq(left, middle.concat(midNodes, ne, other.middle), other.right, tp);
  }

  @Override
  public ListIterator<Item> iterator(final long start) {
    final Item[] ls = left, rs = right;
    final int ll = ls.length , rl = rs.length, startPos;
    final long ms = middle.size();
    final ListIterator<Item> sub;
    if(start < ll) {
      startPos = (int) start - ll;
      sub = middle.listIterator(0);
    } else if(start - ll < ms) {
      startPos = 0;
      sub = middle.listIterator(start - ll);
    } else {
      startPos = (int) (start - ll - ms) + 1;
      sub = middle.listIterator(ms);
    }

    return new ListIterator<>() {
      int pos = startPos;

      @Override
      public int nextIndex() {
        return pos < 0 ? ll + pos
             : pos > 0 ? (int) (ll + ms + pos - 1)
             : ll + sub.nextIndex();
      }

      @Override
      public boolean hasNext() {
        return pos <= rl;
      }

      @Override
      public Item next() {
        if(pos < 0) {
          // in left digit
          return ls[ll + pos++];
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
        return pos < 0 ? ll + pos - 1
             : pos > 0 ? (int) (ll + ms + pos - 2)
             : ll + sub.previousIndex();
      }

      @Override
      public boolean hasPrevious() {
        return pos > -ll;
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
          return ls[ll - 1];
        }

        // in left digit
        return ls[ll + --pos];
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
    return new BasicIter<>(size) {
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
      public Item get(final long i) {
        return itemAt(i);
      }
      @Override
      public boolean valueIter() {
        return true;
      }
      @Override
      public BigSeq value(final QueryContext qc, final Expr expr) {
        return BigSeq.this;
      }
    };
  }

  @Override
  TreeSeq prepend(final SmallSeq seq) {
    final Type tp = type.union(seq.type);
    final Item[] values = seq.items;
    final int vl = values.length, ll = left.length, n = vl + ll;

    // no need to change the middle tree
    if(n <= MAX_DIGIT) return new BigSeq(concat(values, left), middle, right, tp);
    // reuse the arrays
    if(vl >= MIN_DIGIT && MIN_LEAF <= ll && ll <= MAX_LEAF)
      return new BigSeq(values, middle.prepend(new LeafNode(left)), right, tp);

    // left digit is too big
    final int mid = n / 2, move = mid - vl;
    final Item[] newLeft = slice(values, 0, mid);
    Array.copyFromStart(left, move, newLeft, vl);
    final LeafNode leaf = new LeafNode(slice(left, move, ll));
    return new BigSeq(newLeft, middle.prepend(leaf), right, tp);
  }
}
