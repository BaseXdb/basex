package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.fingertree.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An array containing more members than fit into a {@link SingletonArray} or {@link SmallArray}.
 *
 * @author BaseX Team 2005-23, BSD License
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
   * @param type type
   */
  BigArray(final Value[] left, final FingerTree<Value, Value> middle, final Value[] right,
      final Type type) {
    super(type);
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
   * @param type type
   */
  BigArray(final Value[] left, final Value[] right, final Type type) {
    this(left, FingerTree.empty(), right, type);
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
  public Value foot() {
    return right[right.length - 1];
  }

  @Override
  public XQArray prepend(final Value head) {
    final Type tp = union(head);
    final int ll = left.length;
    if(ll < MAX_DIGIT) {
      final Value[] newLeft = slice(left, -1, ll);
      newLeft[0] = head;
      return new BigArray(newLeft, middle, right, tp);
    }

    final int mid = MAX_DIGIT / 2;
    final Value[] newLeft = slice(left, -1, mid);
    newLeft[0] = head;
    final Node<Value, Value> sub = new LeafNode(slice(left, mid, ll));
    return new BigArray(newLeft, middle.prepend(sub), right, tp);
  }

  @Override
  public XQArray append(final Value last) {
    final Type tp = union(last);
    final int rl = right.length;
    if(rl < MAX_DIGIT) {
      final Value[] newRight = slice(right, 0, rl + 1);
      newRight[rl] = last;
      return new BigArray(left, middle, newRight, tp);
    }

    final int mid = (MAX_DIGIT + 1) / 2;
    final Value[] newRight = slice(right, mid, rl + 1);
    newRight[rl - mid] = last;
    final Node<Value, Value> sub = new LeafNode(slice(right, 0, mid));
    return new BigArray(left, middle.append(sub), newRight, tp);
  }

  @Override
  public XQArray trunk() {
    final int ll = left.length, rl = right.length;
    if(rl > MIN_DIGIT) {
      // right digit is safe, just shrink it
      return new BigArray(left, middle, slice(right, 0, rl - 1), type);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left digit
      final int n = ll + rl - 1;
      if(n <= MAX_SMALL) {
        final Value[] out = new Value[n];
        Array.copy(left, ll, out);
        Array.copyFromStart(right, rl - 1, out, ll);
        return new SmallArray(out, type);
      }

      // balance left and right digit
      final int ns = n / 2, ne = n - ns, move = ll - ns;
      final Value[] newLeft = new Value[ns], newRight = new Value[ne];
      Array.copy(left, ns, newLeft);
      Array.copyToStart(left, ns, move, newRight);
      Array.copyFromStart(right, rl - 1, newRight, move);
      return new BigArray(newLeft, newRight, type);
    }

    // merge right digit with last node
    final Value[] ls = ((LeafNode) middle.foot()).values, rs = right;
    final int ns = ls.length, ne = rs.length, n = ns + ne - 1;
    final Value[] newRight = new Value[n];
    Array.copy(ls, ns, newRight);
    Array.copyFromStart(rs, ne - 1, newRight, ns);
    return new BigArray(left, middle.trunk(), newRight, type);
  }

  @Override
  public XQArray tail() {
    final int ll = left.length, rl = right.length;
    if(ll > MIN_DIGIT) {
      // left digit is safe, just shrink it
      return new BigArray(slice(left, 1, ll), middle, right, type);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      final int n = ll - 1 + rl;
      if(n <= MAX_SMALL) {
        final Value[] out = new Value[n];
        Array.copyToStart(left, 1, ll - 1, out);
        Array.copyFromStart(right, rl, out, ll - 1);
        return new SmallArray(out, type);
      }

      // balance left and right digit
      final int ns = n / 2, nl = n - ns;
      final Value[] newLeft = new Value[ns], newRight = new Value[nl];
      Array.copyToStart(left, 1, ll - 1, newLeft);
      Array.copyFromStart(right, rl - nl, newLeft, ll - 1);
      Array.copyToStart(right, rl - nl, nl, newRight);
      return new BigArray(newLeft, newRight, type);
    }

    // merge left digit with first node
    final Value[] ls = left, rs = ((LeafNode) middle.head()).values;
    final int ns = ls.length, ne = rs.length, n = ns - 1 + ne;
    final Value[] newLeft = new Value[n];
    Array.copyToStart(ls, 1, ns - 1, newLeft);
    Array.copyFromStart(rs, ne, newLeft, ns - 1);
    return new BigArray(newLeft, middle.tail(), right, type);
  }

  @Override
  public XQArray concat(final XQArray array) {
    // empty array
    if(array.isEmptyArray()) return this;
    if(array instanceof SingletonArray) return append(((SingletonArray) array).member);

    final Type tp = type.union(array.type);
    if(array instanceof SmallArray) {
      // merge with right digit
      final Value[] newRight = concat(right, ((SmallArray) array).members);
      final int nrl = newRight.length;
      if(nrl <= MAX_DIGIT) return new BigArray(left, middle, newRight, tp);
      final int mid = nrl / 2;
      final Value[] leaf = slice(newRight, 0, mid);
      final FingerTree<Value, Value> newMid = middle.append(new LeafNode(leaf));
      return new BigArray(left, newMid, slice(newRight, mid, nrl), tp);
    }

    // make nodes out of the digits facing each other
    final BigArray other = (BigArray) array;
    final Value[] ls = right, rs = other.left;
    final int ns = ls.length, ne = ns + rs.length;
    final int k = (ne + MAX_LEAF - 1) / MAX_LEAF, s = (ne + k - 1) / k;
    @SuppressWarnings("unchecked")
    final Node<Value, Value>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int curr = Math.min(ne - p, s);
      final Value[] arr = new Value[curr];
      for(int j = 0; j < curr; j++, p++) arr[j] = p < ns ? ls[p] : rs[p - ns];
      midNodes[i] = new LeafNode(arr);
    }

    return new BigArray(left, middle.concat(midNodes, ne, other.middle), other.right, tp);
  }

  @Override
  public Value get(final long index) {
    // index in one of the digits?
    final int ll = left.length;
    if(index < ll) return left[(int) index];

    final long me = ll + middle.size();
    if(index >= me) return right[(int) (index - me)];

    // the member is in the middle tree
    return middle.get(index - ll);
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    final Type tp = union(value);
    long p = pos;
    final int ll = left.length;
    if(p < ll) {
      final Value[] newLeft = left.clone();
      newLeft[(int) p] = value;
      return new BigArray(newLeft, middle, right, tp);
    }
    p -= ll;

    final long ms = middle.size();
    if(p < ms) {
      return new BigArray(left, middle.set(p, value), right, tp);
    }
    p -= ms;

    final Value[] newRight = right.clone();
    newRight[(int) p] = value;
    return new BigArray(left, middle, newRight, tp);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    qc.checkStop();
    final int ll = left.length, rl = right.length;
    final Value[] newLeft = new Value[rl], newRight = new Value[ll];
    for(int i = 0; i < rl; i++) newLeft[i] = right[rl - 1 - i];
    for(int i = 0; i < ll; i++) newRight[i] = left[ll - 1 - i];
    return new BigArray(newLeft, middle.reverse(qc), newRight, type);
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    qc.checkStop();
    final Type tp = union(value);
    final int ll = left.length;
    if(pos <= ll) {
      final int p = (int) pos;
      final Value[] temp = slice(left, 0, ll + 1);
      Array.copy(temp, p, ll - p, temp, p + 1);
      temp[p] = value;
      if(ll < MAX_DIGIT) return new BigArray(temp, middle, right, tp);

      final int m = (ll + 1) / 2;
      return new BigArray(slice(temp, 0, m),
          middle.prepend(new LeafNode(slice(temp, m, ll + 1))), right, tp);
    }

    final long ms = middle.size();
    if(pos - ll < ms) return new BigArray(left, middle.insert(pos - ll, value, qc), right, tp);

    final int rl = right.length, p = (int) (pos - ll - ms);
    final Value[] temp = slice(right, 0, rl + 1);
    Array.copy(temp, p, rl - p, temp, p + 1);
    temp[p] = value;
    if(rl < MAX_DIGIT) return new BigArray(left, middle, temp, tp);

    final int m = (rl + 1) / 2;
    return new BigArray(left, middle.append(new LeafNode(slice(temp, 0, m))),
        slice(temp, m, rl + 1), tp);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int ll = left.length, rl = right.length;
    if(pos < ll) {
      // delete from left digit
      final int p = (int) pos;
      if(ll > MIN_DIGIT) {
        // there is enough space, just delete the member
        final Value[] newLeft = new Value[ll - 1];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, newLeft.length - p, newLeft, p);
        return new BigArray(newLeft, middle, right, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int n = ll - 1 + rl;
        final Value[] vals = new Value[n];
        Array.copy(left, p, vals);
        Array.copy(left, p + 1, ll - 1 - p, vals, p);
        Array.copyFromStart(right, rl, vals, ll - 1);
        return fromMerged(vals);
      }

      // extract a new left digit from the middle
      final Value[] head = ((LeafNode) middle.head()).values;
      final int hl = head.length, n = ll - 1 + hl;

      if(hl > MIN_LEAF) {
        // refill from neighbor
        final int move = (hl - MIN_LEAF + 1) / 2;
        final Value[] newLeft = new Value[ll - 1 + move];
        Array.copy(left, p, newLeft);
        Array.copy(left, p + 1, ll - 1 - p, newLeft, p);
        Array.copyFromStart(head, move, newLeft, ll - 1);
        final Value[] newHead = slice(head, move, hl);
        return new BigArray(newLeft, middle.replaceHead(new LeafNode(newHead)), right, type);
      }

      // merge digit and head node
      final Value[] newLeft = new Value[n];
      Array.copy(left, p, newLeft);
      Array.copy(left, p + 1, ll - 1 - p, newLeft, p);
      Array.copyFromStart(head, hl, newLeft, ll - 1);
      return new BigArray(newLeft, middle.tail(), right, type);
    }

    final long ms = middle.size(), ro = ll + ms;
    if(pos >= ro) {
      // delete from right digit
      final int p = (int) (pos - ro);
      if(rl > MIN_DIGIT) {
        // there is enough space, just delete the member
        final Value[] newRight = new Value[rl - 1];
        Array.copy(right, p, newRight);
        Array.copy(right, p + 1, rl - 1 - p, newRight, p);
        return new BigArray(left, middle, newRight, type);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int n = ll + rl - 1;
        final Value[] vals = new Value[n];
        Array.copy(left, ll, vals);
        Array.copyFromStart(right, p, vals, ll);
        Array.copy(right, p + 1, rl - 1 - p, vals, ll + p);
        return fromMerged(vals);
      }

      // extract a new right digit from the middle
      final Value[] last = ((LeafNode) middle.foot()).values;
      final int sl = last.length, n = sl + rl - 1;

      if(sl > MIN_LEAF) {
        // refill from neighbor
        final int move = (sl - MIN_LEAF + 1) / 2;
        final Value[] newLast = slice(last, 0, sl - move), newRight = new Value[rl - 1 + move];
        Array.copyToStart(last, sl - move, move, newRight);
        Array.copyFromStart(right, p, newRight, move);
        Array.copy(right, p + 1, rl - 1 - p, newRight, move + p);
        return new BigArray(left, middle.replaceLast(new LeafNode(newLast)), newRight, type);
      }

      // merge last node and digit
      final Value[] newRight = new Value[n];
      Array.copy(last, sl, newRight);
      Array.copyFromStart(right, p, newRight, sl);
      Array.copy(right, p + 1, rl - 1 - p, newRight, sl + p);
      return new BigArray(left, middle.trunk(), newRight, type);
    }

    // delete in middle tree
    final TreeSlice<Value, Value> slice = middle.remove(pos - ll, qc);

    if(slice.isTree()) {
      // middle tree did not underflow
      return new BigArray(left, slice.getTree(), right, type);
    }

    // tree height might change
    final Value[] mid = ((PartialLeafNode) slice.getPartial()).elems;
    final int ml = mid.length;

    if(ll > rl) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (ll - MIN_DIGIT + 1) / 2;
      final Value[] newLeft = slice(left, 0, ll - move), newMid = slice(left, ll - move, ll + ml);
      Array.copyFromStart(mid, ml, newMid, move);
      return new BigArray(newLeft, FingerTree.singleton(new LeafNode(newMid)), right, type);
    }

    if(rl > MIN_DIGIT) {
      // steal from right digit
      final int move = (rl - MIN_DIGIT + 1) / 2;
      final Value[] newMid = slice(mid, 0, ml + move);
      Array.copyFromStart(right, move, newMid, ml);
      final Value[] newRight = slice(right, move, rl);
      return new BigArray(left, FingerTree.singleton(new LeafNode(newMid)), newRight, type);
    }

    // divide onto left and right digit
    final int hl = ml / 2, mr = ml - hl;
    final Value[] newLeft = slice(left, 0, ll + hl);
    Array.copyFromStart(mid, hl, newLeft, ll);
    final Value[] newRight = slice(right, -mr, rl);
    Array.copyToStart(mid, hl, mr, newRight);
    return new BigArray(newLeft, newRight, type);
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();

    // the easy cases
    final int ll = left.length, rl = right.length;
    final long ms = middle.size(), size = ll + ms + rl;
    if(length == 0) return empty();
    if(length == 1) return new SingletonArray(get(pos));
    if(length == size) return this;

    final long end = pos + length;
    if(end <= ll) {
      // completely in left digit
      final int p = (int) pos, n = (int) length;
      if(length <= MAX_SMALL) return new SmallArray(slice(left, p, p + n), type);
      final int mid = p + n / 2;
      return new BigArray(slice(left, p, mid), slice(left, mid, p + n), type);
    }

    final long ro = ll + ms;
    if(pos >= ro) {
      // completely in right digit
      final int p = (int) (pos - ro), n = (int) length;
      if(length <= MAX_SMALL) return new SmallArray(slice(right, p, p + n), type);
      final int mid = p + n / 2;
      return new BigArray(slice(right, p, mid), slice(right, mid, p + n), type);
    }

    final int inLeft = pos < ll ? (int) (ll - pos) : 0,
        inRight = end > ro ? (int) (end - ro) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final Value[] newLeft = inLeft == ll ? left : slice(left, (int) pos, ll);
      final Value[] newRight = inRight == rl ? right : slice(right, 0, inRight);
      return new BigArray(newLeft, middle, newRight, type);
    }

    if(middle.isEmpty()) {
      // merge left and right partial digits
      final Value[] out;
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
    final FingerTree<Value, Value> mid;
    if(inMiddle == ms) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = pos < ll ? 0 : pos - ll;
      final TreeSlice<Value, Value> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(!slice.isTree()) {
        final Value[] single = ((PartialLeafNode) slice.getPartial()).elems;
        final int sl = single.length;
        if(inLeft > 0) {
          final Value[] out = slice(left, (int) pos, ll + sl);
          Array.copyFromStart(single, sl, out, inLeft);
          return fromMerged(out);
        }
        if(inRight > 0) {
          final Value[] out = slice(single, 0, sl + inRight);
          Array.copyFromStart(right, inRight, out, sl);
          return fromMerged(out);
        }
        return new SmallArray(single, type);
      }

      mid = slice.getTree();
    }

    // `mid` is non-empty

    // create a left digit
    final int off = ll - inLeft;
    final Value[] newLeft;
    final FingerTree<Value, Value> mid1;
    if(inLeft >= MIN_DIGIT) {
      newLeft = inLeft == ll ? left : slice(left, off, ll);
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
      newRight = inRight == rl ? right : slice(right, 0, inRight);
    } else if(!mid1.isEmpty()) {
      final Value[] last = ((LeafNode) mid1.foot()).values;
      final int sl = last.length;
      newMiddle = mid1.trunk();
      if(inRight == 0) {
        newRight = last;
      } else {
        newRight = slice(last, 0, sl + inRight);
        Array.copyFromStart(right, inRight, newRight, sl);
      }
    } else {
      // not enough members for a right digit
      if(inRight == 0) return fromMerged(newLeft);
      final int nll = newLeft.length, n = nll + inRight;
      final Value[] out = slice(newLeft, 0, n);
      Array.copyFromStart(right, inRight, out, nll);
      return fromMerged(out);
    }

    return new BigArray(newLeft, newMiddle, newRight, type);
  }

  /**
   * Creates an array from two merged, possibly partial digits.
   * This method requires that the input array's length is not longer than {@code 2 * MAX_DIGIT}.
   * @param merged the merged digits
   * @return the array
   */
  private XQArray fromMerged(final Value[] merged) {
    final int ml = merged.length;
    if(ml <= MAX_SMALL) return new SmallArray(merged, type);
    final int mid = ml / 2;
    return new BigArray(slice(merged, 0, mid), slice(merged, mid, ml), type);
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    final Value[] ls = left, rs = right;
    final int ll = ls.length , rl = rs.length, startPos;
    final long ms = middle.size();
    final ListIterator<Value> sub;
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
      public Value next() {
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
      public Value previous() {
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
    final int ll = left.length, rl = right.length;
    if(ll < MIN_DIGIT || ll > MAX_DIGIT) throw new AssertionError("Left digit: " + ll);
    if(rl < MIN_DIGIT || rl > MAX_DIGIT) throw new AssertionError("Right digit: " + rl);
    middle.checkInvariants();
  }

  @Override
  XQArray prepend(final SmallArray array) {
    final Type tp = type.union(array.type);
    final Value[] values = array.members;
    final int vl = values.length, ll = left.length, n = vl + ll;

    // no need to change the middle tree
    if(n <= MAX_DIGIT) return new BigArray(concat(values, left), middle, right, tp);
    // reuse the arrays
    if(vl >= MIN_DIGIT && MIN_LEAF <= ll && ll <= MAX_LEAF)
      return new BigArray(values, middle.prepend(new LeafNode(left)), right, tp);

    // left digit is too big
    final int mid = n / 2, move = mid - vl;
    final Value[] newLeft = slice(values, 0, mid);
    Array.copyFromStart(left, move, newLeft, vl);
    final LeafNode leaf = new LeafNode(slice(left, move, ll));
    return new BigArray(newLeft, middle.prepend(leaf), right, tp);
  }
}
