package org.basex.query.func.index;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

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
 * @author BaseX Team 2005-14, BSD License
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

    final Index index;
    final boolean avl;
    final IndexType it = entries.type();
    if(it == IndexType.TEXT) {
      index = data.textIndex;
      avl = data.meta.textindex;
    } else if(it == IndexType.ATTRIBUTE) {
      index = data.attrIndex;
      avl = data.meta.attrindex;
    } else {
      index = data.ftxtIndex;
      avl = data.meta.ftxtindex;
    }
    if(!avl) throw BXDB_INDEX_X.get(call.info, data.meta.name,
        it.toString().toLowerCase(Locale.ENGLISH));
    return entries(index, entries);
  }

  /**
   * Returns all entries of the specified index.
   * @param index index
   * @param entries entries token
   * @return entry iterator
   */
  static Iter entries(final Index index, final IndexEntries entries) {
    return new Iter() {
      final EntryIterator ei = index.entries(entries);
      @Override
      public ANode next() {
        final byte[] token = ei.next();
        return token == null ? null :
          new FElem(ENTRY).add(COUNT, token(ei.count())).add(token);
      }
    };
  }
}
