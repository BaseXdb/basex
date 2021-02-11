package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A FLWOR clause.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class Clause extends ParseExpr {
  /** All variables declared in this clause. */
  Var[] vars;

  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param vars declared variables
   */
  Clause(final InputInfo info, final SeqType seqType, final Var... vars) {
    super(info, seqType);
    this.vars = vars;
  }

  /**
   * Cleans unused variables from this clause.
   * @param decl variables declared by this FLWOR expression
   * @param used list of the IDs of all variables used in the following clauses
   * @return {@code true} if something changed, {@code false} otherwise
   */
  @SuppressWarnings("unused")
  boolean clean(final IntObjMap<Var> decl, final BitArray used) {
    return false;
  }

  /**
   * Evaluates the clause.
   * @param sub wrapped evaluator
   * @return evaluator
   */
  abstract Eval eval(Eval sub);

  @Override
  public abstract Clause compile(CompileContext cc) throws QueryException;

  @Override
  public abstract Clause optimize(CompileContext cc) throws QueryException;

  @Override
  public abstract Clause inline(InlineContext ic) throws QueryException;

  @Override
  public abstract Clause copy(CompileContext cc, IntObjMap<Var> vm);

  /**
   * Checks if the given clause (currently: let or where) can be slid over this clause.
   * @param cl clause
   * @return result of check
   */
  boolean skippable(final Clause cl) {
    return cl.accept(new ASTVisitor() {
      @Override
      public boolean used(final VarRef ref) {
        for(final Var var : vars) {
          if(var.is(ref.var)) return false;
        }
        return true;
      }
    });
  }

  /**
   * All declared variables of this clause.
   * @return declared variables
   */
  public final Var[] vars() {
    return vars;
  }

  /**
   * Checks if the given variable is declared by this clause.
   * @param var variable
   * @return {code true} if the variable was declared here, {@code false} otherwise
   */
  public final boolean declares(final Var var) {
    for(final Var decl : vars) {
      if(var.is(decl)) return true;
    }
    return false;
  }

  /**
   * Calculates the minimum and maximum number of results.
   * @param minMax minimum and maximum number of incoming tuples
   */
  public void calcSize(@SuppressWarnings("unused") final long[] minMax) { }
}
