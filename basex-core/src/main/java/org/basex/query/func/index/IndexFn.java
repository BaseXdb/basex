package org.basex.query.func.index;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;

/**
 * Index function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class IndexFn extends StandardFunc {
  /** Name: count. */
  static final String COUNT = "count";
  /** Name: value. */
  static final String ENTRY = "entry";

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
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
    type.check(data, call.info);
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

    return new BasicIter<FElem>(ei.size()) {
      @Override
      public FElem next() {
        final byte[] token = ei.next();
        return token == null ? null : get(token);
      }

      @Override
      public FElem get(final long i) {
        return get(ei.get((int) i));
      }

      /**
       * Returns an entry element with the specified token.
       * @param token token
       * @return element
       */
      private FElem get(final byte[] token) {
        return new FElem(ENTRY).add(COUNT, token(ei.count())).add(token);
      }
    };
  }
}
