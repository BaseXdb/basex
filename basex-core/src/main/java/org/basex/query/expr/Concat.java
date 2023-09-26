package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Concat expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Concat extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Concat(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.STRING_O, exprs);
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      for(final Item item : expr.atomValue(qc, info)) tb.add(item.string(info));
    }
    return Str.get(tb.finish());
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // merge adjacent values, ignore empty sequences
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      if(expr instanceof Value) {
        for(final Item item : expr.atomValue(cc.qc, info)) tb.add(item.string(info));
      } else {
        if(!tb.isEmpty()) list.add(Str.get(tb.next()));
        list.add(expr);
      }
    }
    if(list.isEmpty()) return cc.replaceWith(this, Str.get(tb.finish()));
    if(!tb.isEmpty()) list.add(Str.get(tb.finish()));

    // single expression left: replace with string call
    final int ls = list.size();
    if(ls == 1) {
      final Expr arg = list.peek();
      if(arg.seqType().zeroOrOne()) {
        return cc.replaceWith(this, cc.function(Function.STRING, info, arg));
      }
    }

    // replace old with new expressions
    if(ls != el) cc.info(QueryText.OPTMERGE_X, this);
    exprs = list.finish();
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Concat && super.equals(obj);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Concat(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " || ", true);
  }
}
