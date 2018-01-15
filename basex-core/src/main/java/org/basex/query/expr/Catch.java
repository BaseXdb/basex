package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Catch clause.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error QNames. */
  public static final QNm[] NAMES = {
    create(E_CODE), create(E_DESCRIPTION), create(E_VALUE), create(E_MODULE),
    create(E_LINE_NUMBER), create(E_COLUM_NUMBER), create(E_ADDITIONAL)
  };
  /** Error types. */
  public static final SeqType[] TYPES = {
    SeqType.QNM_O, SeqType.STR_ZO, SeqType.ITEM_ZM, SeqType.STR_ZO,
    SeqType.ITR_ZO, SeqType.ITR_ZO, SeqType.ITEM_ZM
  };

  /** Error variables. */
  private final Var[] vars;
  /** Supported codes. */
  private final NameTest[] codes;

  /**
   * Constructor.
   * @param info input info
   * @param codes supported error codes
   * @param vars variables to be bound
   */
  public Catch(final InputInfo info, final NameTest[] codes, final Var[] vars) {
    super(info, null, SeqType.ITEM_ZM);
    this.vars = vars;
    this.codes = codes;
  }

  @Override
  public Catch compile(final CompileContext cc) {
    try {
      expr = expr.compile(cc);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    }
    return optimize(cc);
  }

  @Override
  public Catch optimize(final CompileContext cc) {
    return (Catch) adoptType(expr);
  }

  /**
   * Returns the value of the caught expression.
   * @param qc query context
   * @param qe thrown exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final QueryException qe) throws QueryException {
    int i = 0;
    for(final Value value : values(qe)) qc.set(vars[i++], value);
    Util.debug(qe);
    return expr.value(qc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Var[] vrs = new Var[NAMES.length];
    final int vl = vrs.length;
    for(int v = 0; v < vl; v++) vrs[v] = cc.vs().addNew(NAMES[v], TYPES[v], false, cc.qc, info);
    final Catch ctch = new Catch(info, codes.clone(), vrs);
    final int val = vars.length;
    for(int v = 0; v < val; v++) vm.put(vars[v].id, ctch.vars[v]);
    ctch.expr = expr.copy(cc, vm);
    return ctch;
  }

  @Override
  public Catch inline(final Var var, final Expr ex, final CompileContext cc) {
    try {
      final Expr sub = expr.inline(var, ex, cc);
      if(sub == null) return null;
      expr = sub;
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    }
    return this;
  }

  /**
   * Returns this clause as an inlineable expression.
   * @param qe caught exception
   * @param cc compilation context
   * @return equivalent expression
   * @throws QueryException query exception during inlining
   */
  Expr asExpr(final QueryException qe, final CompileContext cc) throws QueryException {
    if(expr instanceof Value) return expr;
    int v = 0;
    Expr ex = expr;
    for(final Value value : values(qe)) {
      final Expr ex2 = ex.inline(vars[v++], value, cc);
      if(ex2 != null) ex = ex2;
      if(ex instanceof Value) break;
    }
    return ex;
  }

  /**
   * Returns all error values.
   * @param qe exception
   * @return values
   */
  public static Value[] values(final QueryException qe) {
    final byte[] io = qe.file() == null ? EMPTY : token(qe.file());
    final Value value = qe.value();
    return new Value[] { qe.qname(),
        Str.get(qe.getLocalizedMessage()), value == null ? Empty.SEQ : value,
        Str.get(io), Int.get(qe.line()), Int.get(qe.column()),
        Str.get(qe.getMessage().replaceAll("\r\n?", "\n")) };
  }

  /**
   * Checks if one of the specified errors match the thrown error.
   * @param qe thrown error
   * @return result of check
   */
  boolean matches(final QueryException qe) {
    final QNm code = qe.qname();
    for(final NameTest c : codes) if(c.eq(code)) return true;
    return false;
  }

  /**
   * Creates an error QName with the specified name.
   * @param n name
   * @return QName
   */
  private static QNm create(final byte[] n) {
    return new QNm(concat(ERR_PREFIX, COLON, n), ERROR_URI);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Var var : vars) if(!visitor.declared(var)) return false;
    return visitAll(visitor, expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Catch)) return false;
    final Catch c = (Catch) obj;
    return Array.equals(vars, c.vars) && Array.equals(codes, c.codes) && super.equals(obj);
  }

  @Override
  public String toString() {
    return "catch * { " + expr + " }";
  }
}
