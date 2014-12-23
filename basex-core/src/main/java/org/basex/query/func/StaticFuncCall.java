package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncCall extends FuncCall {
  /** Static context of this function call. */
  private final StaticContext sc;
  /** Function name. */
  final QNm name;
  /** Function reference. */
  StaticFunc func;

  /**
   * Function call constructor.
   * @param info input info
   * @param name function name
   * @param args arguments
   * @param sc static context
   */
  public StaticFuncCall(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo info) {
    this(name, args, sc, null, info);
  }

  /**
   * Copy constructor.
   * @param info input info
   * @param name function name
   * @param args arguments
   * @param sc static context
   * @param func referenced function
   */
  private StaticFuncCall(final QNm name, final Expr[] args, final StaticContext sc,
      final StaticFunc func, final InputInfo info) {
    super(info, args);
    this.sc = sc;
    this.name = name;
    this.func = func;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);

    // disallow call of private functions from module with different uri
    if(func.ann.contains(Ann.Q_PRIVATE) && !Token.eq(func.sc.baseURI().string(),
        sc.baseURI().string())) throw FUNCPRIVATE_X.get(info, name.string());

    // compile mutually recursive functions
    func.compile(qc);

    // try to inline the function
    final Expr inl = func.inlineExpr(exprs, qc, scp, info);
    if(inl != null) return inl;
    seqType = func.seqType();
    return this;
  }

  @Override
  public StaticFuncCall optimize(final QueryContext qc, final VarScope scp) {
    // do not inline a static function after compilation as it must be recursive
    return this;
  }

  @Override
  public StaticFuncCall copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] args = Arr.copyAll(qc, scp, vs, exprs);
    final StaticFuncCall call = new StaticFuncCall(name, args, sc, func, info);
    call.seqType = seqType;
    call.size = size;
    return call;
  }

  /**
   * Initializes the function and checks for visibility.
   * @param f function reference
   * @return self reference
   * @throws QueryException query exception
   */
  public StaticFuncCall init(final StaticFunc f) throws QueryException {
    func = f;
    if(f.ann.contains(Ann.Q_PRIVATE) && !Token.eq(sc.baseURI().string(),
        f.sc.baseURI().string())) throw FUNCPRIVATE_X.get(info, f.name.string());
    return this;
  }

  /**
   * Returns the called function if already known, {@code null} otherwise.
   * @return the function
   */
  public StaticFunc func() {
    return func;
  }

  @Override
  public boolean isVacuous() {
    return func != null && func.isVacuous();
  }

  @Override
  public boolean has(final Flag flag) {
    // check arguments, which will be evaluated before running the function code
    if(super.has(flag)) return true;
    // function code: position or context references will have no effect on calling code
    if(flag == Flag.FCS || flag == Flag.CTX) return false;
    // pass on check to function code
    return func == null || (flag == Flag.UPD && !sc.mixUpdates ? func.updating : func.has(flag));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name.string(), TCL, tailCall), exprs);
  }

  @Override
  public String description() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(name.string()).add(toString(SEP)).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticFuncCall(this) && super.accept(visitor);
  }

  @Override
  public StaticFunc evalFunc(final QueryContext qc) {
    return func;
  }

  @Override
  Value[] evalArgs(final QueryContext qc) throws QueryException {
    final int al = exprs.length;
    final Value[] args = new Value[al];
    for(int a = 0; a < al; ++a) args[a] = qc.value(exprs[a]);
    return args;
  }
}
