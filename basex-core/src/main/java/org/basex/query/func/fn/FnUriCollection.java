package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUriCollection extends FnCollection {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // URIs of the files of a directory, or documents of a collection
    final Value value = collection(true, qc);
    if(value.type.instanceOf(BasicType.ANY_URI)) return value.iter();

    final Iter uri = value.iter();
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

      private static Item toUri(final Item item) {
        return item instanceof final XNode node ? Uri.get(node.baseURI(), false) : item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // override implementation of superclass
    return iter(qc).value(qc, this);
  }
}
