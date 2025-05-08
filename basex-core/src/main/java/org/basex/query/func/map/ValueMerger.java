package org.basex.query.func.map;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Merger for map values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ValueMerger {
  /**
   * Returns a merged version of the old and new value.
   * @param key key
   * @param old old (can be {@code null})
   * @param value value
   * @return merged value, or new value if the old one is {@code null}
   * @throws QueryException query exception
   */
  Value merge(final Item key, final Value old, final Value value) throws QueryException {
    return old != null ? get(key, old, value) : value;
  }

  /**
   * Merges the old and new value.
   * @param key key
   * @param old old
   * @param value value
   * @return merged value, or {@code null} if insertion of map entry can be skipped
   * @throws QueryException query exception
   */
  abstract Value get(Item key, Value old, Value value) throws QueryException;

  /**
   * Returns the result type.
   * @param st input type
   * @return result type
   */
  SeqType type(final SeqType st) {
    return st;
  }
}

/**
 * Return {@code null} to indicate that insertion can be skipped.
 */
final class UseFirst extends ValueMerger {
  @Override
  Value get(final Item key, final Value old, final Value value) {
    return null;
  }
}

/**
 * Return new value.
 */
final class UseLast extends ValueMerger {
  @Override
  Value get(final Item key, final Value old, final Value value) {
    return value;
  }
}

/**
 * Concatenate values.
 */
final class Combine extends ValueMerger {
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param qc query context
   */
  Combine(final QueryContext qc) {
    this.qc = qc;
  }

  @Override
  Value get(final Item key, final Value old, final Value value) {
    return old.append(value, qc);
  }

  @Override
  SeqType type(final SeqType st) {
    return st.union(st.occ.add(st.occ));
  }
}

/**
 * Reject merge.
 */
final class Reject extends ValueMerger {
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   */
  Reject(final InputInfo info) {
    this.info = info;
  }

  @Override
  Value get(final Item key, final Value old, final Value value) throws QueryException {
    throw MERGE_DUPLICATE_X.get(info, key);
  }
}

/**
 * Invoke function to combine values.
 */
final class Invoke extends ValueMerger {
  /** Combiner function. */
  private final FItem function;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Query context. */
  private final QueryContext qc;
  /** HOF arguments. */
  private final HofArgs args;

  /**
   * Constructor.
   * @param function combiner function
   * @param info input info
   * @param qc query context
   */
  Invoke(final FItem function, final InputInfo info, final QueryContext qc) {
    this.function = function;
    this.info = info;
    this.qc = qc;
    args = new HofArgs(2);
  }

  @Override
  Value get(final Item key, final Value old, final Value value) throws QueryException {
    return function.invoke(qc, info, args.set(0, old).set(1, value).get());
  }

  @Override
  SeqType type(final SeqType st) {
    return st.union(function.funcType().declType);
  }
}

