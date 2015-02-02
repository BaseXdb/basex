package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Static variable to which an expression can be assigned.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** If this variable can be bound from outside the query. */
  private final boolean external;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /** Bound value. */
  Value val;

  /**
   * Constructor for a variable declared in a query.
   * @param sc static context
   * @param scope variable scope
   * @param anns annotations
   * @param name variable name
   * @param type declared variable type
   * @param expr expression to be bound
   * @param external external flag
   * @param doc current xqdoc cache
   * @param info input info
   */
  StaticVar(final StaticContext sc, final VarScope scope, final AnnList anns, final QNm name,
      final SeqType type, final Expr expr, final boolean external, final String doc,
      final InputInfo info) {
    super(sc, anns, name, type, scope, doc, info);
    this.expr = expr;
    this.external = external;
    lazy = anns.contains(Annotation._BASEX_LAZY);
  }

  @Override
  public void compile(final QueryContext qc) throws QueryException {
    if(expr == null) throw VAREMPTY_X.get(info, '$' + Token.string(name.string()));
    if(dontEnter) throw circVarError(this);

    if(!compiled) {
      dontEnter = true;
      try {
        expr = expr.compile(qc, scope);
      } catch(final QueryException qe) {
        compiled = true;
        if(lazy) {
          expr = FnError.get(qe, expr.seqType());
          return;
        }
        throw qe.notCatchable();
      } finally {
        scope.cleanUp(this);
        dontEnter = false;
      }

      compiled = true;
      if(!lazy || expr.isValue()) bind(value(qc));
    }
  }

  /**
   * Evaluates this variable lazily.
   * @param qc query context
   * @return value of this variable
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc) throws QueryException {
    if(dontEnter) throw circVarError(this);

    if(lazy) {
      if(!compiled) throw Util.notExpected(this + " was not compiled.");
    } else {
      if(expr == null) throw VAREMPTY_X.get(info, this);
    }

    if(val != null) return val;
    dontEnter = true;

    final int fp = scope.enter(qc);
    try {
      return bind(expr.value(qc));
    } catch(final QueryException qe) {
      if(lazy) qe.notCatchable();
      throw qe;
    } finally {
      VarScope.exit(qc, fp);
      dontEnter = false;
    }
  }

  /**
   * Checks for the correct placement of updating expressions in this variable.
   * @throws QueryException query exception
   */
  void checkUp() throws QueryException {
    if(expr != null && expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
  }

  /**
   * Binds an expression to this variable from outside the query.
   * @param value value to bind
   * @param qc query context
   * @throws QueryException query exception
   */
  void bind(final Value value, final QueryContext qc) throws QueryException {
    if(!external || compiled) return;
    bind(declType == null || declType.instance(value) ? value : declType.cast(value, qc, sc, info));
  }

  /**
   * Binds the specified value to the variable.
   * @param value value to be set
   * @return self reference
   * @throws QueryException query exception
   */
  private Value bind(final Value value) throws QueryException {
    expr = value;
    val = value;
    if(declType != null) declType.treat(value, info);
    return val;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(NAM, name.string());
    if(expr != null) expr.plan(e);
    plan.add(e);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr == null || expr.accept(visitor);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(anns);
    tb.add(VARIABLE).add(' ').add(DOLLAR).add(name.string());
    if(declType != null) tb.add(' ').add(AS).add(' ').addExt(declType);
    if(external) tb.add(' ').add(EXTERNAL);
    if(expr != null) tb.add(' ').add(ASSIGN).add(' ').addExt(expr);
    return tb.add(';').toString();
  }

  @Override
  public byte[] id() {
    return Token.concat(new byte[] { '$' }, name.id());
  }

  /**
   * Checks if the expression bound to this variable has the given flag.
   * @param flag flag to check for
   * @return {@code true} if the expression has the given flag, {@code false} otherwise
   */
  boolean has(final Flag flag) {
    if(dontEnter || expr == null) return false;
    dontEnter = true;
    final boolean res = expr.has(flag);
    dontEnter = false;
    return res;
  }
}
