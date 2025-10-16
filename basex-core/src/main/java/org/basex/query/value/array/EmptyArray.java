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
    super(ArrayType.ARRAY);
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public Value memberAt(final long index) {
    throw Util.notExpected();
  }

  @Override
  public long structSize() {
    return 0;
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
  public XQArray insertMember(final long pos, final Value value, final QueryContext qc) {
    return get(value);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public XQArray removeMember(final long pos, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return Collections.emptyListIterator();
  }
}
