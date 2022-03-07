package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CArray extends Arr {
  /** Create sequences as array members. */
  private final boolean seq;

  /**
   * Constructor.
   * @param info input info
   * @param seq create sequences
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final boolean seq, final Expr... exprs) {
    super(info, SeqType.ARRAY_O, exprs);
    this.seq = seq;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    SeqType dt = null;
    if(seq) {
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
    // create array with single member
    if(exprs.length == 1 && (seq || exprs[0].size() == 1)) {
      return XQArray.member(exprs[0].value(qc));
    }

    final ArrayBuilder builder = new ArrayBuilder();
    for(final Expr expr : exprs) {
      if(seq) {
        builder.append(expr.value(qc));
      } else {
        final Iter iter = expr.iter(qc);
        for(Item item; (item = qc.next(iter)) != null;) builder.append(item);
      }
    }
    return builder.array(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = null;
    if(mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA, Simplify.COUNT)) {
      simplifyAll(mode, cc);
      expr = List.get(cc, info, exprs);
    }
    return expr != null ? cc.simplify(this, expr) : super.simplifyFor(mode, cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CArray(info, seq, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && seq == ((CArray) obj).seq && super.equals(obj);
  }

  @Override
  public String description() {
    return ARRAY;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(seq ? "[ " : ARRAY + " { ").tokens(exprs, SEP).token(seq ? " ]" : " }");
  }
}
