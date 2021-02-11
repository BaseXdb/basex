package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-21, BSD License
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
    checkVisible();

    // compile mutually recursive functions
    func.comp(cc);

    // try to inline the function
    final Expr inlined = func.inline(exprs, cc);
    if(inlined != null) return inlined;

    exprType.assign(func.seqType());
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
    checkVisible();
    return this;
  }

  /**
   * Checks if the called function is visible
   * (i.e., has no private annotation or is in the same namespace).
   * @throws QueryException query exception
   */
  private void checkVisible() throws QueryException {
    if(func.anns.contains(Annotation.PRIVATE) && !func.sc.baseURI().eq(sc.baseURI()))
      throw FUNCPRIVATE_X.get(info, name.string());
  }

  /**
   * Returns the called function if already known.
   * @return the function or {@code null}
   */
  public StaticFunc func() {
    return func;
  }

  @Override
  public boolean vacuous() {
    return func.vacuousBody();
  }

  @Override
  public boolean has(final Flag... flags) {
    // check arguments, which will be evaluated previous to the function body
    if(super.has(flags)) return true;
    // function code: position or context references of expression body have no effect
    if(Flag.POS.in(flags) || Flag.CTX.in(flags)) return false;
    // function code: check for updates
    if(Flag.UPD.in(flags) && func.updating()) return true;
    // check remaining flags
    final Flag[] flgs = Flag.UPD.remove(flags);
    return flgs.length != 0 && func.has(flgs);
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
    for(int a = 0; a < al; ++a) args[a] = exprs[a].value(qc);
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
  public String description() {
    return "function";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string(), TAILCALL, tco), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(name.prefixId()).params(exprs);
  }
}
