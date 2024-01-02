package org.basex.query.func.index;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Index function.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class IndexFn extends StandardFunc {
  /** QName. */
  static final QNm Q_NAME = new QNm("name");
  /** QName. */
  static final QNm Q_TYPE = new QNm("type");
  /** QName. */
  static final QNm Q_COUNT = new QNm("count");
  /** QName. */
  static final QNm Q_ENTRY = new QNm("entry");
  /** QName. */
  static final QNm Q_MIN = new QNm("min");
  /** QName. */
  static final QNm Q_MAX = new QNm("max");
  /** QName. */
  static final QNm Q_ELEMENT = new QNm("element");
  /** QName. */
  static final QNm Q_ATTRIBUTE = new QNm("attribute");
  /** Flag: flat output. */
  static final byte[] FLAT = token("flat");

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), false, visitor) && super.accept(visitor);
  }

  /**
   * Returns all entries of the specified value index.
   * @param data data reference
   * @param entries container for returning index entries
   * @param call calling function
   * @return text entries
   * @throws QueryException query exception
   */
  public static Iter entries(final Data data, final IndexEntries entries, final StandardFunc call)
      throws QueryException {

    final IndexType type = entries.type();
    type.check(data, call.info());
    return entries(data.index(type), entries);
  }

  /**
   * Returns all entries of the specified index.
   * @param index index
   * @param entries entries token
   * @return entry iterator
   */
  static Iter entries(final Index index, final IndexEntries entries) {
    final EntryIterator ei = index.entries(entries);

    return new BasicIter<FNode>(ei.size()) {
      @Override
      public FNode next() {
        final byte[] token = ei.next();
        return token == null ? null : get(token);
      }

      @Override
      public FNode get(final long i) {
        return get(ei.get((int) i));
      }

      /**
       * Returns an entry element with the specified token.
       * @param token token
       * @return element
       */
      private FNode get(final byte[] token) {
        return FElem.build(Q_ENTRY).add(Q_COUNT, ei.count()).add(token).finish();
      }
    };
  }
}
