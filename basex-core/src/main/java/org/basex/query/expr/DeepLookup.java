package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Deep lookup expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DeepLookup extends ALookup {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr context expression and key specifier
   */
  public DeepLookup(final InputInfo info, final Expr... expr) {
    super(info, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      add(item, vb, qc);
    }
    return vb.value(this);
  }

  /**
   * Adds looked up items recursively.
   * @param item input item
   * @param vb value builder
   * @param qc query context
   * @throws QueryException query exception
   */
  private void add(final Item item, final ValueBuilder vb, final QueryContext qc)
      throws QueryException {
    if(item instanceof XQStruct) {
      vb.add(valueFor(item, true, qc));
      for(final Item it : ((XQStruct) item).items(qc)) {
        add(it, vb, qc);
      }
    }
  }

/*
declare function local:add($item, $key) {
  for $entry in if ($item instance of map(*)) {
    map:pairs($item)
  } else if ($item instance of array(*)) {
    for member $m at $p in $item
    return { 'key': $p, 'value': $m }
  }
  return (
    $entry[?key = $key]?value,
    $entry?value ! local:add(., $key)
  )
};
  */

  @Override
  public DeepLookup copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new DeepLookup(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DeepLookup && super.equals(obj);
  }
}
