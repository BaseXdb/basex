package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnNot extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!arg(0).test(qc, info, false));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), inverted = invert(input, cc);
    return inverted != input ? inverted : this;
  }

  /**
   * Inverts the specified expression.
   * @param input input info
   * @param cc compilation context
   * @return inverted or original expression
   * @throws QueryException query exception
   */
  private Expr invert(final Expr input, final CompileContext cc) throws QueryException {
    // exists(A)  ->  empty(A)
    if(EXISTS.is(input)) return cc.function(EMPTY, info, input.args());
    // empty(A)  ->  exists(A)
    if(EMPTY.is(input)) return cc.function(EXISTS, info, input.args());
    // not(A)  ->  boolean(A)
    if(NOT.is(input)) return cc.function(BOOLEAN, info, input.args());

    // 'a' = 'b'  ->  'a' != 'b'
    if(input instanceof Cmp) {
      final Expr inverted = ((Cmp) input).invert(cc);
      if(inverted != input) return inverted;
    }
    // position() = 1  ->  position() != 1
    if(input instanceof CmpPos) {
      final Expr ex = ((CmpPos) input).invert(cc);
      if(ex != null) return ex;
    }
    // $node/text()  ->  empty($node/text())
    final SeqType st = input.seqType();
    if(st.type instanceof NodeType) return cc.function(EMPTY, info, input);

    // A = 1 or position() = 1  ->  A != 1 and position() != 1
    if(input instanceof Logical) {
      Expr[] args = input.args();
      final ExprList tmp = new ExprList(args.length);
      for(final Expr arg : args) {
        final Expr inverted = invert(arg, cc);
        if(inverted == arg) return input;
        tmp.add(inverted);
      }
      args = tmp.finish();
      return (input instanceof And ? new Or(info, args) : new And(info, args)).optimize(cc);
    }
    return input;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // not(boolean(A))  ->  not(A)
    arg(0, arg -> arg.simplifyFor(Simplify.EBV, cc));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {
    // negation: operator may be inverted in general comparison merge
    return NOT.is(ex) ? null : ex.mergeEbv(this, or, cc);
  }
}
