package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param f original filter
   */
  IterFilter(final Filter f) {
    super(f.info, f.root, f.preds);
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

        // cache context
        final Value cv = ctx.value;
        final long cp = ctx.pos;
        final long cs = ctx.size;
        try {
          while(true) {
            ctx.checkStop();
            final Item it = iter.next();
            if(it == null) return null;
            if(preds(it, ctx)) return it;
          }
        } finally {
          ctx.value = cv;
          ctx.pos = cp;
          ctx.size = cs;
        }
      }
    };
  }
}
