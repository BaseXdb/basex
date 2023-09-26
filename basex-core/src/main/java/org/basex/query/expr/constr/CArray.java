package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Array constructor.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CArray extends Arr {
  /** Create sequences as array members. */
  private final boolean sequences;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param sequences create sequences
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final boolean sequences, final Expr... exprs) {
    super(info, SeqType.ARRAY_O, exprs);
    this.sequences = sequences;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final boolean values = allAreValues(true);
    if(exprs.length == 1 && (sequences || exprs[0].size() == 1)) {
      return cc.replaceWith(this, values
          ? XQArray.member(exprs[0].value(cc.qc))
          : cc.function(_UTIL_ARRAY_MEMBER, info, exprs));
    }

    SeqType dt = null;
    if(sequences) {
      dt = SeqType.union(exprs, true);
    } else {
      for(final Expr expr : exprs) {
        final SeqType st = expr.seqType().with(Occ.EXACTLY_ONE);
        dt = dt == null ? st : dt.union(st);
      }
    }
    if(dt != null) exprType.assign(ArrayType.get(dt));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayBuilder ab = new ArrayBuilder();
    for(final Expr expr : exprs) {
      if(sequences) {
        ab.append(expr.value(qc));
      } else {
        final Iter iter = expr.iter(qc);
        for(Item item; (item = qc.next(iter)) != null;) {
          ab.append(item);
        }
      }
    }
    return ab.array(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA)) {
      expr = List.get(cc, info, simplifyAll(mode, cc));
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CArray(info, sequences, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && sequences == ((CArray) obj).sequences && super.equals(obj);
  }

  @Override
  public String description() {
    return ARRAY;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(sequences ? "[ " : ARRAY + " { ").tokens(exprs, SEP).token(sequences ? " ]" : " }");
  }
}
