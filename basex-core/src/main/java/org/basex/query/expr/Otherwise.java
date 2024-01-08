package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Otherwise expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Otherwise extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Otherwise(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int el = exprs.length - 1;
    for(int e = 0; e < el; e++) {
      final Iter input = exprs[e].iter(qc);
      final long size = input.size();
      // size is known, results exist: return items iterator
      if(size > 0) return input;
      // unknown result size: retrieve first item
      if(size < 0) {
        final Item item = qc.next(input);
        if(item != null) return new Iter() {
          boolean next;

          @Override
          public Item next() throws QueryException {
            if(next) return qc.next(input);
            next = true;
            return item;
          }
        };
      }
    }
    return exprs[el].iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    for(final Expr expr : exprs) {
      final Value value = expr.value(qc);
      if(!value.isEmpty()) return value;
    }
    return Empty.VALUE;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      exprs[e] = cc.compileOrError(exprs[e], e == 0);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    flatten(cc);
    removeEmpty(cc);

    // chop remaining expressions if an operator yields one or more items
    // 1 otherwise 2  ->  1
    int el = exprs.length - 1;
    int e = -1;
    while(++e < el && !exprs[e].seqType().oneOrMore());
    if(e < el) exprs = Arrays.copyOf(exprs, el);

    el = exprs.length;
    if(el == 0) return cc.emptySeq(this);
    if(el == 1) return exprs[0];

    // determine result type
    Occ occ = null;
    for(final Expr expr : exprs) {
      final Occ o = expr.seqType().occ;
      occ = occ == null ? o : occ.union(o);
    }
    final SeqType st = SeqType.union(exprs, false);
    exprType.assign(st != null ? st.type : AtomType.ITEM, occ).data(exprs);

    return this;
  }

  @Override
  public boolean vacuous() {
    return ((Checks<Expr>) Expr::vacuous).all(exprs);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Otherwise && super.equals(obj);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Otherwise(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, ' ' + OTHERWISE + ' ', true);
  }
}
