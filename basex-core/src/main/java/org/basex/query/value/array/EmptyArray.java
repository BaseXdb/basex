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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class EmptyArray extends XQArray {
  /** The empty array. */
  static final EmptyArray EMPTY = new EmptyArray();

  /** Hidden constructor. */
  private EmptyArray() {
    super(0, SeqType.ARRAY);
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public XQArray prepend(final Value head) {
    return singleton(head);
  }

  @Override
  public XQArray append(final Value last) {
    return singleton(last);
  }

  @Override
  public Value memberAt(final long index) {
    throw Util.notExpected();
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    throw Util.notExpected();
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return this;
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return singleton(value);
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return Collections.emptyListIterator();
  }
}
