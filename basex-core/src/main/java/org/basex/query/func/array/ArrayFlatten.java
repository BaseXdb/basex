package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class ArrayFlatten extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Iter iter = qc.iter(exprs[0]);
    for(Item it; (it = iter.next()) != null;) {
      if(it instanceof Array) addFlattened(vb, (Array) it);
      else vb.add(it);
    }
    return vb.value();
  }

  /**
   * Recursive helper method for flattening nested arrays.
   * @param vb sequence builder
   * @param arr current array
   */
  private static void addFlattened(final ValueBuilder vb, final Array arr) {
    final Iterator<Value> members = arr.members();
    while(members.hasNext()) {
      for(final Item it : members.next()) {
        if(it instanceof Array) addFlattened(vb, (Array) it);
        else vb.add(it);
      }
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      @SuppressWarnings("unchecked")
      private Iterator<Value>[] iters = new Iterator[2];
      private int p = -1;
      private Iter curr = qc.iter(exprs[0]);

      @Override
      public Item next() throws QueryException {
        for(;;) {
          final Item it = curr.next();

          if(it != null) {
            if(!(it instanceof Array)) return it;
            final Array arr = (Array) it;
            if(++p == iters.length) {
              @SuppressWarnings("unchecked")
              final Iterator<Value>[] temp = new Iterator[2 * p];
              System.arraycopy(iters, 0, temp, 0, p);
              iters = temp;
            }
            iters[p] = arr.members();
          } else if(p < 0) {
            return null;
          }

          while(!iters[p].hasNext()) {
            iters[p] = null;
            if(--p < 0) return null;
          }

          curr = iters[p].next().iter();
        }
      }
    };
  }
}
