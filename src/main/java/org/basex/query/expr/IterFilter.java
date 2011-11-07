package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Iterative filter expression without position predicates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param f original filter
   */
  IterFilter(final Filter f) {
    super(f.input, f.root, f.preds);
    type = f.type;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      /** Iterator. */
      Iter iter;

      @Override
      public Item next() throws QueryException {
        // first call - initialize iterator
        if(iter == null) iter = ctx.iter(root);

        // loop through all items
        while(true) {
          ctx.checkStop();
          final Item it = iter.next();
          if(it == null) return null;
          if(preds(it, ctx)) return it;
        }
      }
    };
  }
}
