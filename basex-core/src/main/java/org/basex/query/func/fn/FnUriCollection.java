package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUriCollection extends FnCollection {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter uri = collection(qc).iter();
    final long size = uri.size();

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(uri);
        return item != null ? toUri(item) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return toUri(uri.get(i));
      }
      @Override
      public long size() {
        return size;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // overwrite implementation of superclass
    return iter(qc).value(qc, this);
  }

  /**
   * Returns the URI of the collection node.
   * @param item node item
   * @return URI
   */
  private static Uri toUri(final Item item) {
    return Uri.get(((XNode) item).baseURI(), false);
  }
}
