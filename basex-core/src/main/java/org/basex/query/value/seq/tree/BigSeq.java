package org.basex.query.value.seq.tree;

import java.util.*;

import org.basex.query.iter.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * A sequence containing more elements than fit into a {@link SmallSeq}.
 *
 * @author BaseX Team 2005-15, BSD License
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
   * @param ret type of all items in this sequence, can be {@code null}
   */
  BigSeq(final Item[] left, final FingerTree<Item, Item> middle, final Item[] right,
      final Type ret) {
    super(left.length + middle.size() + right.length, ret);
    this.left = left;
    this.middle = middle;
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
  }

  @Override
  public Item itemAt(final long index) {
    // index out of range?
    if(index < 0) throw new IndexOutOfBoundsException("Index < 0: " + index);
    if(index >= size) throw new IndexOutOfBoundsException(index + " >= " + size);

    // index in one of the digits?
    if(index < left.length) return left[(int) index];
    final long midSize = size - right.length;
    if(index >= midSize) return right[(int) (index - midSize)];

    // the element is in the middle tree
    return middle.get(index - left.length);
  }

  @Override
  public TreeSeq reverse() {
    final int l = left.length, r = right.length;
    final Item[] newLeft = new Item[r], newRight = new Item[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i];
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i];
    return new BigSeq(newLeft, middle.reverse(), newRight, ret);
  }

  @Override
  public TreeSeq insert(final long pos, final Item val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    final int l = left.length;
    if(pos <= l) {
      final int p = (int) pos;
      final Item[] temp = slice(left, 0, l + 1);
      System.arraycopy(temp, p, temp, p + 1, l - p);
      temp[p] = val;
      if(l < MAX_DIGIT) return new BigSeq(temp, middle, right, null);

      final int m = (l + 1) / 2;
      return new BigSeq(slice(temp, 0, m),
          middle.cons(new LeafNode(slice(temp, m, l + 1))), right, null);
    }

    final long midSize = middle.size();
    if(pos - l < midSize) return new BigSeq(left, middle.insert(pos - l, val), right, null);

    final int r = right.length;
    final int p = (int) (pos - l - midSize);
    final Item[] temp = slice(right, 0, r + 1);
    System.arraycopy(temp, p, temp, p + 1, r - p);
    temp[p] = val;
    if(r < MAX_DIGIT) return new BigSeq(left, middle, temp, null);

    final int m = (r + 1) / 2;
    return new BigSeq(left, middle.snoc(new LeafNode(slice(temp, 0, m))),
        slice(temp, m, r + 1), null);
  }

  @Override
  public TreeSeq remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos >= size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    if(pos < left.length) {
      // delete from left digit
      final int p = (int) pos, l = left.length;
      if(l > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Item[] newLeft = new Item[l - 1];
        System.arraycopy(left, 0, newLeft, 0, p);
        System.arraycopy(left, p + 1, newLeft, p, newLeft.length - p);
        return new BigSeq(newLeft, middle, right, ret);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int r = right.length, n = l - 1 + r;
        final Item[] vals = new Item[n];
        System.arraycopy(left, 0, vals, 0, p);
        System.arraycopy(left, p + 1, vals, p, l - 1 - p);
        System.arraycopy(right, 0, vals, l - 1, r);
        return fromMerged(vals, ret);
      }

      // extract a new left digit from the middle
      final Item[] head = ((LeafNode) middle.head()).values;
      final int r = head.length, n = l - 1 + r;

      if(r > MIN_LEAF) {
        // refill from neighbor
        final int move = (r - MIN_LEAF + 1) / 2;
        final Item[] newLeft = new Item[l - 1 + move];
        System.arraycopy(left, 0, newLeft, 0, p);
        System.arraycopy(left, p + 1, newLeft, p, l - 1 - p);
        System.arraycopy(head, 0, newLeft, l - 1, move);
        final Item[] newHead = slice(head, move, r);
        return new BigSeq(newLeft, middle.replaceHead(new LeafNode(newHead)), right, ret);
      }

      // merge digit and head node
      final Item[] newLeft = new Item[n];
      System.arraycopy(left, 0, newLeft, 0, p);
      System.arraycopy(left, p + 1, newLeft, p, l - 1 - p);
      System.arraycopy(head, 0, newLeft, l - 1, r);
      return new BigSeq(newLeft, middle.tail(), right, ret);
    }

    final long midSize = middle.size(), rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // delete from right digit
      final int p = (int) (pos - rightOffset), r = right.length;
      if(r > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Item[] newRight = new Item[r - 1];
        System.arraycopy(right, 0, newRight, 0, p);
        System.arraycopy(right, p + 1, newRight, p, r - 1 - p);
        return new BigSeq(left, middle, newRight, ret);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int l = left.length, n = l + r - 1;
        final Item[] vals = new Item[n];
        System.arraycopy(left, 0, vals, 0, l);
        System.arraycopy(right, 0, vals, l, p);
        System.arraycopy(right, p + 1, vals, l + p, r - 1 - p);
        return fromMerged(vals, ret);
      }

      // extract a new right digit from the middle
      final Item[] last = ((LeafNode) middle.last()).values;
      final int l = last.length, n = l + r - 1;

      if(l > MIN_LEAF) {
        // refill from neighbor
        final int move = (l - MIN_LEAF + 1) / 2;
        final Item[] newLast = slice(last, 0, l - move);
        final Item[] newRight = new Item[r - 1 + move];
        System.arraycopy(last, l - move, newRight, 0, move);
        System.arraycopy(right, 0, newRight, move, p);
        System.arraycopy(right, p + 1, newRight, move + p, r - 1 - p);
        return new BigSeq(left, middle.replaceLast(new LeafNode(newLast)), newRight, ret);
      }

      // merge last node and digit
      final Item[] newRight = new Item[n];
      System.arraycopy(last, 0, newRight, 0, l);
      System.arraycopy(right, 0, newRight, l, p);
      System.arraycopy(right, p + 1, newRight, l + p, r - 1 - p);
      return new BigSeq(left, middle.init(), newRight, ret);
    }

    // delete in middle tree
    final TreeSlice<Item, Item> slice = middle.remove(pos - left.length);

    if(slice.isTree()) {
      // middle tree did not underflow
      return new BigSeq(left, slice.getTree(), right, ret);
    }

    // tree height might change
    final Item[] mid = ((PartialLeafNode) slice.getPartial()).elems;
    final int l = left.length, m = mid.length, r = right.length;

    if(l > r) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (l - MIN_DIGIT + 1) / 2;
      final Item[] newLeft = slice(left, 0, l - move);
      final Item[] newMid = slice(left, l - move, l + m);
      System.arraycopy(mid, 0, newMid, move, m);
      return new BigSeq(newLeft, FingerTree.singleton(new LeafNode(newMid)), right, ret);
    }

    if(r > MIN_DIGIT) {
      // steal from right digit
      final int move = (r - MIN_DIGIT + 1) / 2;
      final Item[] newMid = slice(mid, 0, m + move);
      System.arraycopy(right, 0, newMid, m, move);
      final Item[] newRight = slice(right, move, r);
      return new BigSeq(left, FingerTree.singleton(new LeafNode(newMid)), newRight, ret);
    }

    // divide onto left and right digit
    final int ml = m / 2, mr = m - ml;
    final Item[] newLeft = slice(left, 0, l + ml);
    System.arraycopy(mid, 0, newLeft, l, ml);
    final Item[] newRight = slice(right, -mr, r);
    System.arraycopy(mid, ml, newRight, 0, mr);
    final Type rt = ret;
    return new BigSeq(newLeft, FingerTree.<Item>empty(), newRight, rt);
  }

  @Override
  public Value subSeq(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    final long midSize = middle.size();
    if(len > size - pos)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > " + size);

    // the easy cases
    if(len == 0) return Empty.SEQ;
    if(len == 1) return itemAt(pos);
    if(len == size) return this;

    final long end = pos + len;
    if(end <= left.length) {
      // completely in left digit
      final int p = (int) pos, n = (int) len;
      if(len <= MAX_SMALL) return new SmallSeq(slice(left, p, p + n), ret);
      final int mid = p + n / 2;
      final Type rt = ret;
      return new BigSeq(slice(left, p, mid), FingerTree.<Item>empty(), slice(left, mid, p + n), rt);
    }

    final long rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // completely in right digit
      final int p = (int) (pos - rightOffset), n = (int) len;
      if(len <= MAX_SMALL) return new SmallSeq(slice(right, p, p + n), ret);
      final int mid = p + n / 2;
      final Type rt = ret;
      return new BigSeq(slice(right, p, mid), FingerTree.<Item>empty(),
          slice(right, mid, p + n), rt);
    }

    final int inLeft = pos < left.length ? (int) (left.length - pos) : 0,
        inRight = end > rightOffset ? (int) (end - rightOffset) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final Item[] newLeft = inLeft == left.length ? left : slice(left, (int) pos, left.length);
      final Item[] newRight = inRight == right.length ? right : slice(right, 0, inRight);
      return new BigSeq(newLeft, middle, newRight, ret);
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
        System.arraycopy(right, 0, out, inLeft, inRight);
      }
      return fromMerged(out, ret);
    }

    final long inMiddle = len - inLeft - inRight;
    final FingerTree<Item, Item> mid;
    if(inMiddle == midSize) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = pos < left.length ? 0 : pos - left.length;
      final TreeSlice<Item, Item> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(!slice.isTree()) {
        final Item[] single = ((PartialLeafNode) slice.getPartial()).elems;
        if(inLeft > 0) {
          final Item[] out = slice(left, (int) pos, left.length + single.length);
          System.arraycopy(single, 0, out, inLeft, single.length);
          return fromMerged(out, ret);
        }
        if(inRight > 0) {
          final Item[] out = slice(single, 0, single.length + inRight);
          System.arraycopy(right, 0, out, single.length, inRight);
          return fromMerged(out, ret);
        }
        return new SmallSeq(single, ret);
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
        System.arraycopy(left, off, newLeft, 0, inLeft);
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
        System.arraycopy(right, 0, newRight, last.length, inRight);
      }
    } else {
      // not enough elements for a right digit
      if(inRight == 0) return fromMerged(newLeft, ret);
      final int n = newLeft.length + inRight;
      final Item[] out = slice(newLeft, 0, n);
      System.arraycopy(right, 0, out, newLeft.length, inRight);
      return fromMerged(out, ret);
    }

    return new BigSeq(newLeft, newMiddle, newRight, ret);
  }

  /**
   * Creates a sequence from two merged, possibly partial digits.
   * This method requires that the input array's length is not longer than {@code 2 * MAX_DIGIT}.
   * @param merged the merged digits
   * @param rt element type
   * @return the array
   */
  private TreeSeq fromMerged(final Item[] merged, final Type rt) {
    if(merged.length <= MAX_SMALL) return new SmallSeq(merged, rt);
    final int mid = merged.length / 2;
    return new BigSeq(slice(merged, 0, mid), FingerTree.<Item>empty(),
        slice(merged, mid, merged.length), rt);
  }

  @Override
  public TreeSeq concat(final TreeSeq seq) {
    final Type retType = type == seq.type ? type : null;
    if(seq instanceof SmallSeq) {
      // merge with right digit
      final Item[] newRight = concat(right, ((SmallSeq) seq).elems);
      final int r = newRight.length;
      if(r <= MAX_DIGIT) return new BigSeq(left, middle, newRight, retType);
      final int mid = r / 2;
      final Item[] leaf = slice(newRight, 0, mid);
      final FingerTree<Item, Item> newMid = middle.snoc(new LeafNode(leaf));
      return new BigSeq(left, newMid, slice(newRight, mid, r), retType);
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

    return new BigSeq(left, middle.concat(midNodes, n, bigOther.middle), bigOther.right, retType);
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
        if(pos > r) throw new NoSuchElementException();
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
        if(pos <= -l) throw new NoSuchElementException();
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
        throw new UnsupportedOperationException();
      }

      @Override
      public void set(final Item e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public ValueIter iter() {
    return new ValueIter() {
      private long pos;
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
        return BigSeq.this.itemAt(i);
      }

      @Override
      public long size() {
        return size;
      }

      @Override
      public Value value() {
        return BigSeq.this;
      }
    };
  }

  @Override
  void checkInvariants() {
    final int l = left.length, r = right.length;
    if(l < MIN_DIGIT || l > MAX_DIGIT) throw new AssertionError("Left digit: " + l);
    if(r < MIN_DIGIT || r > MAX_DIGIT) throw new AssertionError("Right digit: " + r);
    middle.checkInvariants();
  }

  @Override
  TreeSeq consSmall(final Item[] vals) {
    final int a = vals.length, b = left.length, n = a + b;
    if(n <= MAX_DIGIT) {
      // no need to change the middle tree
      return new BigSeq(concat(vals, left), middle, right, null);
    }

    if(a >= MIN_DIGIT && MIN_LEAF <= b && b <= MAX_LEAF) {
      // reuse the arrays
      return new BigSeq(vals, middle.cons(new LeafNode(left)), right, null);
    }

    // left digit is too big
    final int mid = n / 2, move = mid - a;
    final Item[] newLeft = slice(vals, 0, mid);
    System.arraycopy(left, 0, newLeft, a, move);
    final LeafNode leaf = new LeafNode(slice(left, move, b));
    return new BigSeq(newLeft, middle.cons(leaf), right, null);
  }
}
