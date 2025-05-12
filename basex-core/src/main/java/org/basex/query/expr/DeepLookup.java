package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
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
   * @param modifier modifier
   * @param expr context expression and key specifier
   */
  public DeepLookup(final InputInfo info, final Modifier modifier, final Expr... expr) {
    super(info, modifier, expr);
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
    if(item instanceof XQStruct struct) {
      if(struct instanceof XQArray array && ((ArrayType) array.type).valueType().mayBeStruct()) {
        // process members individually to preserve order
        int k = 0;
        for(final Value val : array.iterable()) {
          vb.add(valueFor(XQMap.get(Int.get(++k), val), true, qc));
          for(final Item it : val) {
            add(it, vb, qc);
          }
        }
      } else {
        vb.add(valueFor(item, true, qc));
        for(final Item it : struct.items(qc)) {
          add(it, vb, qc);
        }
      }
    }
  }

  @Override
  public DeepLookup copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new DeepLookup(info, modifier, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DeepLookup && super.equals(obj);
  }
}
