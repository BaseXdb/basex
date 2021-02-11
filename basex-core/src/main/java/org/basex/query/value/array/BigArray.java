package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * An array containing more elements than fit into a {@link SmallArray}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class BigArray extends XQArray {
  /** Left digit. */
  final Value[] left;
  /** Middle tree. */
  final FingerTree<Value, Value> middle;
  /** Right digit. */
  final Value[] right;

  /**
   * Constructor.
   * @param left left digit
   * @param middle middle tree
   * @param right right digit
   */
  BigArray(final Value[] left, final FingerTree<Value, Value> middle, final Value[] right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
  }

  /**
   * Constructor for arrays with an empty middle tree.
   * @param left left digit
   * @param right right digit
   */
  BigArray(final Value[] left, final Value[] right) {
    this.left = left;
    middle = FingerTree.empty();
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
  }

  @Override
  public boolean isEmptyArray() {
    return false;
  }

  @Override
  public long arraySize() {
    // O(1) because the middle tree caches its size
    return left.length + middle.size() + right.length;
  }

  @Override
  public Value head() {
    return left[0];
  }

  @Override
  public Value last() {
    return right[right.length - 1];
  }

  @Override
  public XQArray cons(final Value elem) {
    if(left.length < MAX_DIGIT) {
      final Value[] newLeft = slice(left, -1, left.length);
      newLeft[0] = elem;
      return new BigArray(newLeft, middle, right);
    }

    final int mid = MAX_DIGIT / 2;
    final Value[] newLeft = slice(left, -1, mid);
    newLeft[0] = elem;
    final Node<Value, Value> sub = new LeafNode(slice(left, mid, left.length));
    return new BigArray(newLeft, middle.cons(sub), right);
  }

  @Override
  public XQArray snoc(final Value elem) {
    if(right.length < MAX_DIGIT) {
      final Value[] newRight = slice(right, 0, right.length + 1);
      newRight[right.length] = elem;
      return new BigArray(left, middle, newRight);
    }

    final int mid = (MAX_DIGIT + 1) / 2;
    final Value[] newRight = slice(right, mid, right.length + 1);
    newRight[right.length - mid] = elem;
    final Node<Value, Value> sub = new LeafNode(slice(right, 0, mid));
    return new BigArray(left, middle.snoc(sub), newRight);
  }

  @Override
  public XQArray init() {
    if(right.length > MIN_DIGIT) {
      // right digit is safe, just shrink it
      return new BigArray(left, middle, slice(right, 0, right.length - 1));
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left digit
      final int l = left.length, r = right.length, n = l + r - 1;
      if(n <= MAX_SMALL) {
        final Value[] out = new Value[n];
        Array.copy(left, l, out);
        Array.copyFromStart(right, r - 1, out, l);
        return new SmallArray(out);
      }

      // balance left and right digit
      final int ll = n / 2, rl = n - ll, move = l - ll;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];
      Array.copy(left, ll, newLeft);
      Array.copyToStart(left, ll, move, newRight);
      Array.copyFromStart(right, r - 1, newRight, move);
      return new BigArray(newLeft, newRight);
    }

    // merge right digit with last node
    final Value[] ls = ((LeafNode) middle.last()).values, rs = right;
    final int ll = ls.length, rl = rs.length, n = ll + rl - 1;
    final Value[] newRight = new Value[n];
    Array.copy(ls, ll, newRight);
    Array.copyFromStart(rs, rl - 1, newRight, ll);
    return new BigArray(left, middle.init(), newRight);
  }

  @Override
  public XQArray tail() {
    if(left.length > MIN_DIGIT) {
      // left digit is safe, just shrink it
      return new BigArray(slice(left, 1, left.length), middle, right);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      final int l = left.length, r = right.length, n = l - 1 + r;
      if(n <= MAX_SMALL) {
        final Value[] out = new Value[n];
        Array.copyToStart(left, 1, l - 1, out);
        Array.copyFromStart(right, r, out, l - 1);
        return new SmallArray(out);
      }

      // balance left and right digit
      final int ll = n / 2, rl = n - ll;
      final Value[] newLeft = new Value[ll], newRight = new Value[rl];
      Array.copyToStart(left, 1, l - 1, newLeft);
      Array.copyFromStart(right, r - rl, newLeft, l - 1);
      Array.copyToStart(right, r - rl, rl, newRight);
      return new BigArray(newLeft, newRight);
    }

    // merge left digit with first node
    final Value[] ls = left, rs = ((LeafNode) middle.head()).values;
    final int ll = ls.length, rl = rs.length, n = ll - 1 + rl;
    final Value[] newLeft = new Value[n];
    Array.copyToStart(ls, 1, ll - 1, newLeft);
    Array.copyFromStart(rs, rl, newLeft, ll - 1);
    return new BigArray(newLeft, middle.tail(), right);
  }

  @Override
  public XQArray concat(final XQArray seq) {
    // empty array
    if(seq.isEmptyArray()) return this;

    if(seq instanceof SmallArray) {
      // merge with right digit
      final Value[] newRight = concat(right, ((SmallArray) seq).elems);
      final int r = newRight.length;
      if(r <= MAX_DIGIT) return new BigArray(left, middle, newRight);
      final int mid = r / 2;
      final Value[] leaf = slice(newRight, 0, mid);
      final FingerTree<Value, Value> newMid = middle.snoc(new LeafNode(leaf));
      return new BigArray(left, newMid, slice(newRight, mid, r));
    }

    final BigArray other = (BigArray) seq;

    // make nodes out of the digits facing each other
    final Value[] ls = right, rs = other.left;
    final int l = ls.length, n = l + rs.length;
    final int k = (n + MAX_LEAF - 1) / MAX_LEAF, s = (n + k - 1) / k;
    @SuppressWarnings("unchecked")
    final Node<Value, Value>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int curr = Math.min(n - p, s);
      final Value[] arr = new Value[curr];
      for(int j = 0; j < curr; j++, p++) arr[j] = p < l ? ls[p] : rs[p - l];
      midNodes[i] = new LeafNode(arr);
    }

    return new BigArray(left, middle.concat(midNodes, n, other.middle), other.right);
  }

  @Override
  public Value get(final long index) {
    // index in one of the digits?
    if(index < left.length) return left[(int) index];
    final long midSize = left.length + middle.size();
    if(index >= midSize) return right[(int) (index - midSize)];

    // the element is in the middle tree
    return middle.get(index - left.length);
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    long p = pos;
    if(p < left.length) {
      final Value[] newLeft = left.clone();
      newLeft[(int) p] = value;
      return new BigArray(newLeft, middle, right);
    }
    p -= left.length;

    final long m = middle.size();
    if(p < m) {
      return new BigArray(left, middle.set(p, value), right);
    }
    p -= m;

    final Value[] newRight = right.clone();
    newRight[(int) p] = value;
    return new BigArray(left, middle, newRight);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int l = left.length, r = right.length;
    final Value[] newLeft = new Value[r], newRight = new Value[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i];
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i];
    return new BigArray(newLeft, middle.reverse(qc), newRight);
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    qc.checkStop();
    final int l = left.length;
    if(pos <= l) {
      final int p = (int) pos;
      final Value[] temp = slice(left, 0, l + 1);
      Array.copy(temp, p, l - p, temp, p + 1);
      temp[p] = value;
      if(l < MAX_DIGIT) return new BigArray(temp, middle, right);

      final int m = (l + 1) / 2;
      return new BigArray(slice(temp, 0, m),
          middle.cons(new LeafNode(slice(temp, m, l + 1))), right);
    }

    final long midSize = middle.size();
    if(pos - l < midSize) return new BigArray(left, middle.insert(pos - l, value, qc), right);

    final int r = right.length;
    final int p = (int) (pos - l - midSize);
    final Value[] temp = slice(right, 0, r + 1);
    Array.copy(temp, p, r - p, temp, p + 1);
    temp[p] = value;
    if(r < MAX_DIGIT) return new BigArray(left, middle, temp);

    final int m = (r + 1) / 2;
    return new BigArray(left, middle.snoc(new LeafNode(slice(temp, 0, m))),
        slice(temp, m, r + 1));
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    if(pos < left.length) {
      // delete from left digit
      final int p = (int) pos, l = left.length;
      if(l > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Value[] newLeft = new Value[l - 1];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, newLeft.length - p, newLeft, p);
        return new BigArray(newLeft, middle, right);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int r = right.length, n = l - 1 + r;
        final Value[] vals = new Value[n];
        Array.copy(left, p, vals);
        Array.copy(left, p + 1, l - 1 - p, vals, p);
        Array.copyFromStart(right, r, vals, l - 1);
        return fromMerged(vals);
      }

      // extract a new left digit from the middle
      final Value[] head = ((LeafNode) middle.head()).values;
      final int r = head.length, n = l - 1 + r;

      if(r > MIN_LEAF) {
        // refill from neighbor
        final int move = (r - MIN_LEAF + 1) / 2;
        final Value[] newLeft = new Value[l - 1 + move];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, l - 1 - p, newLeft, p);
        Array.copyFromStart(head, move, newLeft, l - 1);
        final Value[] newHead = slice(head, move, r);
        return new BigArray(newLeft, middle.replaceHead(new LeafNode(newHead)), right);
      }

      // merge digit and head node
      final Value[] newLeft = new Value[n];
      Array.copy(left, p, newLeft);
      Array.copy(left, p + 1, l - 1 - p, newLeft, p);
      Array.copyFromStart(head, r, newLeft, l - 1);
      return new BigArray(newLeft, middle.tail(), right);
    }

    final long midSize = middle.size(), rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // delete from right digit
      final int p = (int) (pos - rightOffset), r = right.length;
      if(r > MIN_DIGIT) {
        // there is enough space, just delete the element
        final Value[] newRight = new Value[r - 1];
        Array.copy(right, p, newRight);
        Array.copy(right, p + 1, r - 1 - p, newRight, p);
        return new BigArray(left, middle, newRight);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int l = left.length, n = l + r - 1;
        final Value[] vals = new Value[n];
        Array.copy(left, l, vals);
        Array.copyFromStart(right, p, vals, l);
        Array.copy(right, p + 1, r - 1 - p, vals, l + p);
        return fromMerged(vals);
      }

      // extract a new right digit from the middle
      final Value[] last = ((LeafNode) middle.last()).values;
      final int l = last.length, n = l + r - 1;

      if(l > MIN_LEAF) {
        // refill from neighbor
        final int move = (l - MIN_LEAF + 1) / 2;
        final Value[] newLast = slice(last, 0, l - move);
        final Value[] newRight = new Value[r - 1 + move];
        Array.copyToStart(last, l - move, move, newRight);
        Array.copyFromStart(right, p, newRight, move);
        Array.copy(right, p + 1, r - 1 - p, newRight, move + p);
        return new BigArray(left, middle.replaceLast(new LeafNode(newLast)), newRight);
      }

      // merge last node and digit
      final Value[] newRight = new Value[n];
      Array.copy(last, l, newRight);
      Array.copyFromStart(right, p, newRight, l);
      Array.copy(right, p + 1, r - 1 - p, newRight, l + p);
      return new BigArray(left, middle.init(), newRight);
    }

    // delete in middle tree
    final TreeSlice<Value, Value> slice = middle.remove(pos - left.length, qc);

    if(slice.isTree()) {
      // middle tree did not underflow
      return new BigArray(left, slice.getTree(), right);
    }

    // tree height might change
    final Value[] mid = ((PartialLeafNode) slice.getPartial()).elems;
    final int l = left.length, m = mid.length, r = right.length;

    if(l > r) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (l - MIN_DIGIT + 1) / 2;
      final Value[] newLeft = slice(left, 0, l - move);
      final Value[] newMid = slice(left, l - move, l + m);
      Array.copyFromStart(mid, m, newMid, move);
      return new BigArray(newLeft, FingerTree.singleton(new LeafNode(newMid)), right);
    }

    if(r > MIN_DIGIT) {
      // steal from right digit
      final int move = (r - MIN_DIGIT + 1) / 2;
      final Value[] newMid = slice(mid, 0, m + move);
      Array.copyFromStart(right, move, newMid, m);
      final Value[] newRight = slice(right, move, r);
      return new BigArray(left, FingerTree.singleton(new LeafNode(newMid)), newRight);
    }

    // divide onto left and right digit
    final int ml = m / 2, mr = m - ml;
    final Value[] newLeft = slice(left, 0, l + ml);
    Array.copyFromStart(mid, ml, newLeft, l);
    final Value[] newRight = slice(right, -mr, r);
    Array.copyToStart(mid, ml, mr, newRight);
    return new BigArray(newLeft, newRight);
  }

  @Override
  public XQArray subArray(final long pos, final long len, final QueryContext qc) {
    qc.checkStop();

    // the easy cases
    final long midSize = middle.size(), size = left.length + midSize + right.length;
    if(len == 0) return XQArray.empty();
    if(len == size) return this;

    final long end = pos + len;
    if(end <= left.length) {
      // completely in left digit
      final int p = (int) pos, n = (int) len;
      if(len <= MAX_SMALL) return new SmallArray(slice(left, p, p + n));
      final int mid = p + n / 2;
      return new BigArray(slice(left, p, mid), slice(left, mid, p + n));
    }

    final long rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // completely in right digit
      final int p = (int) (pos - rightOffset), n = (int) len;
      if(len <= MAX_SMALL) return new SmallArray(slice(right, p, p + n));
      final int mid = p + n / 2;
      return new BigArray(slice(right, p, mid), slice(right, mid, p + n));
    }

    final int inLeft = pos < left.length ? (int) (left.length - pos) : 0,
        inRight = end > rightOffset ? (int) (end - rightOffset) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final Value[] newLeft = inLeft == left.length ? left : slice(left, (int) pos, left.length);
      final Value[] newRight = inRight == right.length ? right : slice(right, 0, inRight);
      return new BigArray(newLeft, middle, newRight);
    }

    if(middle.isEmpty()) {
      // merge left and right partial digits
      final Value[] out;
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

    final long inMiddle = len - inLeft - inRight;
    final FingerTree<Value, Value> mid;
    if(inMiddle == midSize) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = pos < left.length ? 0 : pos - left.length;
      final TreeSlice<Value, Value> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(!slice.isTree()) {
        final Value[] single = ((PartialLeafNode) slice.getPartial()).elems;
        if(inLeft > 0) {
          final Value[] out = slice(left, (int) pos, left.length + single.length);
          Array.copyFromStart(single, single.length, out, inLeft);
          return fromMerged(out);
        }
        if(inRight > 0) {
          final Value[] out = slice(single, 0, single.length + inRight);
          Array.copyFromStart(right, inRight, out, single.length);
          return fromMerged(out);
        }
        return new SmallArray(single);
      }

      mid = slice.getTree();
    }

    // `mid` is non-empty

    // create a left digit
    final int off = left.length - inLeft;
    final Value[] newLeft;
    final FingerTree<Value, Value> mid1;
    if(inLeft >= MIN_DIGIT) {
      newLeft = inLeft == left.length ? left : slice(left, off, left.length);
      mid1 = mid;
    } else {
      final Value[] head = ((LeafNode) mid.head()).values;
      if(inLeft == 0) {
        newLeft = head;
      } else {
        newLeft = slice(head, -inLeft, head.length);
        Array.copyToStart(left, off, inLeft, newLeft);
      }
      mid1 = mid.tail();
    }

    // create a right digit
    final Value[] newRight;
    final FingerTree<Value, Value> newMiddle;
    if(inRight >= MIN_DIGIT) {
      newMiddle = mid1;
      newRight = inRight == right.length ? right : slice(right, 0, inRight);
    } else if(!mid1.isEmpty()) {
      final Value[] last = ((LeafNode) mid1.last()).values;
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
      final Value[] out = slice(newLeft, 0, n);
      Array.copyFromStart(right, inRight, out, newLeft.length);
      return fromMerged(out);
    }

    return new BigArray(newLeft, newMiddle, newRight);
  }

  /**
   * Creates an array from two merged, possibly partial digits.
   * This method requires that the input array's length is not longer than {@code 2 * MAX_DIGIT}.
   * @param merged the merged digits
   * @return the array
   */
  private static XQArray fromMerged(final Value[] merged) {
    if(merged.length <= MAX_SMALL) return new SmallArray(merged);
    final int mid = merged.length / 2;
    return new BigArray(slice(merged, 0, mid), slice(merged, mid, merged.length));
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    final Value[] ls = left, rs = right;
    final int l = ls.length , r = rs.length, startPos;
    final long m = middle.size();
    final ListIterator<Value> sub;
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

    return new ListIterator<Value>() {
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
      public Value next() {
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
      public Value previous() {
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
      public void add(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void set(final Value e) {
        throw Util.notExpected();
      }

      @Override
      public void remove() {
        throw Util.notExpected();
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
  XQArray prepend(final SmallArray array) {
    final Value[] values = array.elems;
    final int a = values.length, b = left.length, n = a + b;

    // no need to change the middle tree
    if(n <= MAX_DIGIT) return new BigArray(concat(values, left), middle, right);
    // reuse the arrays
    if(a >= MIN_DIGIT && MIN_LEAF <= b && b <= MAX_LEAF)
      return new BigArray(values, middle.cons(new LeafNode(left)), right);

    // left digit is too big
    final int mid = n / 2, move = mid - a;
    final Value[] newLeft = slice(values, 0, mid);
    Array.copyFromStart(left, move, newLeft, a);
    final LeafNode leaf = new LeafNode(slice(left, move, b));
    return new BigArray(newLeft, middle.cons(leaf), right);
  }
}
