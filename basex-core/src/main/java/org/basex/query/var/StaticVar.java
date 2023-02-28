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
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** Indicates if this variable can be bound from outside the query. */
  public final boolean external;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /**
   * Constructor for a variable declared in a query.
   * @param var variable
   * @param expr expression to be bound
   * @param anns annotations
   * @param doc xqdoc string
   * @param external external flag
   * @param vs variable scope
   */
  StaticVar(final Var var, final Expr expr, final AnnList anns, final String doc,
      final boolean external, final VarScope vs) {
    super(var.name, var.declType, anns, doc, vs, var.info);
    this.expr = expr;
    this.external = external;
    lazy = anns.contains(Annotation._BASEX_LAZY);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(expr == null) throw VAREMPTY_X.get(info, name());
    if(dontEnter) throw CIRCVAR_X.get(info, name());
    if(!compiled) {
      compiled = dontEnter = true;

      final QueryFocus focus = pushFocus(cc.qc);
      cc.pushScope(vs);
      try {
        expr = expr.compile(cc);
      } catch(final QueryException qe) {
        if(!lazy) throw qe.notCatchable();
        expr = cc.error(qe, expr);
      } finally {
        cc.removeScope(this);
        cc.qc.focus = focus;
      }
      dontEnter = false;

      // dynamic compilation, eager evaluation: pre-evaluate expressions
      if(expr instanceof Value || cc.dynamic && !lazy) {
        try {
          cc.replaceWith(expr, value(cc.qc));
        } catch(final QueryException ex) {
          if(ex.error() != NOCTX_X) throw ex;
        }
      }
    }
    return null;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(dontEnter) throw CIRCVAR_X.get(info, name());
    if(!lazy && expr == null) throw VAREMPTY_X.get(info, name());

    if(value == null) {
      dontEnter = true;
      final QueryFocus focus = pushFocus(qc);
      try {
        super.value(qc);
      } catch(final QueryException qe) {
        if(lazy) qe.notCatchable();
        throw qe;
      } finally {
        qc.focus = focus;
        dontEnter = false;
      }
    }
    return value;
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
    if(external && !compiled) {
      value = declType == null || declType.instance(val) ? val :
        declType.cast(val, true, qc, sc, info);
      expr = value;
    }
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
    final boolean has = expr.has(flags);
    dontEnter = false;
    return has;
  }

  /**
   * Assigns a new query focus with the global context value.
   * @param qc query context
   * @return old focus
   */
  private static QueryFocus pushFocus(final QueryContext qc) {
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qf.value = qc.finalContext ? qc.contextScope.value : null;
    qc.focus = qf;
    return focus;
  }

  @Override
  public String description() {
    return "variable declaration";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(DECLARE).token(anns).token(VARIABLE).token(name());
    if(declType != null) qs.token(AS).token(declType);
    if(external) qs.token(EXTERNAL);
    if(expr != null) qs.token(":=").token(expr);
    qs.token(';');
  }
}
