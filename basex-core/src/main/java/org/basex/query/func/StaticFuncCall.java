package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
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
 * @author BaseX Team 2005-17, BSD License
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
   * @param name function name
   * @param args arguments
   * @param sc static context
   * @param info input info
   */
  public StaticFuncCall(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo info) {
    this(name, args, sc, null, info);
  }

  /**
   * Copy constructor.
   * @param name function name
   * @param args arguments
   * @param sc static context
   * @param func referenced function (can be {@code null})
   * @param info input info
   */
  private StaticFuncCall(final QNm name, final Expr[] args, final StaticContext sc,
      final StaticFunc func, final InputInfo info) {
    super(info, args);
    this.sc = sc;
    this.name = name;
    this.func = func;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    super.compile(cc);

    // disallow call of private functions from module with different uri
    if(func.anns.contains(Annotation.PRIVATE) && !func.sc.baseURI().eq(sc.baseURI()))
      throw FUNCPRIVATE_X.get(info, name.string());

    // compile mutually recursive functions
    func.comp(cc);

    // try to inline the function
    final Expr inl = func.inlineExpr(exprs, cc, info);
    if(inl != null) return inl;

    seqType = func.seqType();
    return this;
  }

  @Override
  public StaticFuncCall optimize(final CompileContext cc) {
    // do not inline a static function after compilation as it must be recursive
    return this;
  }

  @Override
  public StaticFuncCall copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new StaticFuncCall(name, Arr.copyAll(cc, vm, exprs), sc, func, info));
  }

  /**
   * Initializes the function and checks for visibility.
   * @param sf function reference
   * @return self reference
   * @throws QueryException query exception
   */
  public StaticFuncCall init(final StaticFunc sf) throws QueryException {
    func = sf;
    if(sf.anns.contains(Annotation.PRIVATE) && !sc.baseURI().eq(sf.sc.baseURI()))
      throw FUNCPRIVATE_X.get(info, sf.name.string());
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
    return func.isVacuousBody();
  }

  @Override
  public boolean has(final Flag flag) {
    // check arguments, which will be evaluated before running the function code
    if(super.has(flag)) return true;
    // function code: position or context references of expression body have no effect
    if(flag == Flag.POS || flag == Flag.CTX) return false;
    // pass on check to function code
    return flag != Flag.UPD ? func.has(flag) : func.updating();
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

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof StaticFuncCall)) return false;
    final StaticFuncCall s = (StaticFuncCall) obj;
    return name.eq(s.name) && func == s.func && super.equals(obj);
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
    return new TokenBuilder(name.prefixId()).add(toString(SEP)).toString();
  }
}
