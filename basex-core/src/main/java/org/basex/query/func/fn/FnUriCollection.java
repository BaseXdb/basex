package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnUriCollection extends Docs {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter coll = collection(qc).iter();
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = qc.next(coll);
        // all items will be nodes
        return it == null ? null : Uri.uri(((ANode) it).baseURI(), false);
      }
    };
  }
}
