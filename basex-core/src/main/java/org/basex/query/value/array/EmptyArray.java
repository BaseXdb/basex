package org.basex.query.value.array;

import java.util.*;

import org.basex.core.jobs.*;
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
    super(Types.ARRAY);
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public Value valueAt(final long index) {
    throw Util.notExpected();
  }

  @Override
  public long structSize() {
    return 0;
  }

  @Override
  public XQArray putMember(final long pos, final Value value, final Job job) {
    throw Util.notExpected();
  }

  @Override
  public XQArray reverseArray(final Job job) {
    return this;
  }

  @Override
  public XQArray insertMember(final long pos, final Value value, final Job job) {
    return get(value);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final Job job) {
    throw Util.notExpected();
  }

  @Override
  public XQArray removeMember(final long pos, final Job job) {
    throw Util.notExpected();
  }

  @Override
  public ListIterator<Value> iterator(final long start) {
    return Collections.emptyListIterator();
  }
}
