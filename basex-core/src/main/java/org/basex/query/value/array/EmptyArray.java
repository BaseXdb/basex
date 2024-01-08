package org.basex.query.value.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * The empty array.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class EmptyArray extends XQArray {
  /** The empty array. */
  static final EmptyArray EMPTY = new EmptyArray();

  /** Hidden constructor. */
  private EmptyArray() {
    super(SeqType.ARRAY);
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public XQArray prepend(final Value head) {
    return new SingletonArray(head);
  }

  @Override
  public XQArray append(final Value last) {
    return new SingletonArray(last);
  }

  @Override
  public Value get(final long index) {
    throw Util.notExpected();
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    throw Util.notExpected();
  }

  @Override
  public long arraySize() {
    return 0;
  }

  @Override
  public XQArray concat(final XQArray other) {
    return other;
  }

  @Override
  public Value head() {
    throw Util.notExpected();
  }

  @Override
  public Value foot() {
    throw Util.notExpected();
  }

  @Override
  public XQArray trunk() {
    throw Util.notExpected();
  }

  @Override
  public XQArray tail() {
    throw Util.notExpected();
  }

  @Override
  public XQArray subArray(final long pos, final long length, final QueryContext qc) {
    return this;
  }

  @Override
  public boolean isEmptyArray() {
    return true;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return this;
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return new SingletonArray(value);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public ListIterator<Value> iterator(final long size) {
    return Collections.emptyListIterator();
  }

  @Override
  void checkInvariants() {
    // nothing can go wrong
  }

  @Override
  XQArray prepend(final SmallArray array) {
    return array;
  }
}
