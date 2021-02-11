package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Static variable to which an expression can be assigned.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** If this variable can be bound from outside the query. */
  public final boolean external;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /** Bound value. */
  Value value;

  /**
   * Constructor for a variable declared in a query.
   * @param vs variable scope
   * @param anns annotations
   * @param var variable
   * @param expr expression to be bound
   * @param external external flag
   * @param doc xqdoc string
   */
  StaticVar(final VarScope vs, final AnnList anns, final Var var, final Expr expr,
      final boolean external, final String doc) {
    super(anns, var.name, var.declType, vs, doc, var.info);
    this.expr = expr;
    this.external = external;
    lazy = anns.contains(Annotation._BASEX_LAZY);
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
    if(expr == null) throw VAREMPTY_X.get(info, name());
    if(dontEnter) throw CIRCVAR_X.get(info, name());
    if(compiled) return;
    compiled = true;

    dontEnter = true;
    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
    } catch(final QueryException qe) {
      declType = null;
      if(lazy) {
        expr = cc.error(qe, expr);
        return;
      }
      throw qe.notCatchable();
    } finally {
      cc.removeScope(this);
      dontEnter = false;
    }

    // by default, pre-evaluate deterministic, non-lazy expressions
    if(expr instanceof Value || !(lazy || expr.has(Flag.NDT))) cc.replaceWith(expr, value(cc.qc));
  }

  /**
   * Evaluates this variable.
   * @param qc query context
   * @return value of this variable
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc) throws QueryException {
    if(dontEnter) throw CIRCVAR_X.get(info, name());

    if(lazy) {
      if(!compiled) throw Util.notExpected(this + " was not compiled.");
    } else {
      if(expr == null) throw VAREMPTY_X.get(info, name());
    }

    if(value != null) return value;
    dontEnter = true;

    final int fp = vs.enter(qc);
    try {
      return bindValue(expr.value(qc), qc);
    } catch(final QueryException qe) {
      if(lazy) qe.notCatchable();
      throw qe;
    } finally {
      VarScope.exit(fp, qc);
      dontEnter = false;
    }
  }

  /**
   * Ensures that the variable expression is not updating.
   * @throws QueryException query exception
   */
  void checkUp() throws QueryException {
    if(expr != null && expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
  }

  /**
   * Binds an external value and casts it to the declared type (if specified).
   * @param val value to bind
   * @param qc query context
   * @throws QueryException query exception
   */
  void bind(final Value val, final QueryContext qc) throws QueryException {
    if(!external || compiled) return;
    bindValue(declType == null || declType.instance(val) ? val :
      declType.cast(val, true, qc, sc, info), qc);
  }

  /**
   * Binds the specified value to the variable.
   * @param val value to be set
   * @param qc query context
   * @return self reference
   * @throws QueryException query exception
   */
  private Value bindValue(final Value val, final QueryContext qc) throws QueryException {
    expr = val;
    value = val;
    if(declType != null) declType.treat(val, name, qc, info);
    return value;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr == null || expr.accept(visitor);
  }

  @Override
  public byte[] id() {
    return Token.concat(Token.DOLLAR, name.id());
  }

  /**
   * Returns the name of the variable.
   * @return name
   */
  private String name() {
    return Strings.concat(Token.DOLLAR, name.string());
  }

  /**
   * Indicates if the expression bound to this variable has one of the specified compiler
   * properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  boolean has(final Flag... flags) {
    if(dontEnter || expr == null) return false;
    dontEnter = true;
    final boolean res = expr.has(flags);
    dontEnter = false;
    return res;
  }

  @Override
  public String description() {
    return "variable declaration";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(DECLARE).token(anns).token(VARIABLE).token(name());
    if(declType != null) qs.token(AS).token(declType);
    if(external) qs.token(EXTERNAL);
    if(expr != null) qs.token(ASSIGN).token(expr);
    qs.token(';');
  }
}
