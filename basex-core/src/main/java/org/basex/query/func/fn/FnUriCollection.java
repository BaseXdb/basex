package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnUriCollection extends Docs {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter coll = collection(qc).iter();
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item item = qc.next(coll);
        // all items will be nodes
        return item == null ? null : Uri.uri(((ANode) item).baseURI(), false);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : collection(qc)) vb.add(Uri.uri(((ANode) item).baseURI(), false));
    return vb.value(this);
  }
}
