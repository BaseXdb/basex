package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Static variable to which an expression can be assigned.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** If this variable can be bound from outside the query. */
  public final boolean external;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /** Bound value. */
  Value val;

  /**
   * Constructor for a variable declared in a query.
   * @param vs variable scope
   * @param anns annotations
   * @param var variable
   * @param expr expression to be bound
   * @param external external flag
   * @param doc current xqdoc cache
   */
  StaticVar(final VarScope vs, final AnnList anns, final Var var, final Expr expr,
      final boolean external, final String doc) {
    super(anns, var.name, var.type, vs, doc, var.info);
    this.expr = expr;
    this.external = external;
    lazy = anns.contains(Annotation._BASEX_LAZY);
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
    if(expr == null) throw VAREMPTY_X.get(info, name());
    if(dontEnter) throw CIRCVAR_X.get(info, name());

    if(!compiled) {
      dontEnter = true;
      cc.pushScope(vs);
      try {
        expr = expr.compile(cc);
      } catch(final QueryException qe) {
        compiled = true;
        if(lazy) {
          expr = cc.error(qe, expr);
          return;
        }
        throw qe.notCatchable();
      } finally {
        cc.removeScope(this);
        dontEnter = false;
      }

      compiled = true;
      if(!lazy || expr.isValue()) bind(value(cc.qc));
    }
  }

  /**
   * Evaluates this variable lazily.
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

    if(val != null) return val;
    dontEnter = true;

    final int fp = vs.enter(qc);
    try {
      return bind(expr.value(qc));
    } catch(final QueryException qe) {
      if(lazy) qe.notCatchable();
      throw qe;
    } finally {
      VarScope.exit(fp, qc);
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
   * Binds an external value.
   * @param value value to bind
   * @param qc query context
   * @throws QueryException query exception
   */
  void bind(final Value value, final QueryContext qc) throws QueryException {
    if(!external || compiled) return;
    bind(type == null || type.instance(value) ? value : type.cast(value, qc, sc, info));
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
    if(type != null) type.treat(value, name, info);
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
    tb.add(VARIABLE).add(' ').add(name());
    if(type != null) tb.add(' ').add(AS).add(' ').addExt(type);
    if(external) tb.add(' ').add(EXTERNAL);
    if(expr != null) tb.add(' ').add(ASSIGN).add(' ').addExt(expr);
    return tb.add(';').toString();
  }

  @Override
  public byte[] id() {
    return Token.concat(new byte[] { '$' }, name.id());
  }

  /**
   * Returns the name of the variable.
   * @return name
   */
  private String name() {
    return new TokenBuilder().add(DOLLAR).add(name.string()).toString();
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
