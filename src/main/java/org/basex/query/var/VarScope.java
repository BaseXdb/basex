package org.basex.query.var;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The scope of variables, either the query, a use-defined or an inline function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class VarScope {
  /** stack of currently accessible variables. */
  private final VarStack current = new VarStack();
  /** Local variables in this scope. */
  private final ArrayList<Var> vars = new ArrayList<Var>();

  /** This scope's closure. */
  private final Map<Var, Expr> closure = new HashMap<Var, Expr>();

  /** This scope's parent scope, used for looking up non-local variables. */
  private final VarScope parent;

  /** Constructor for a top-level module. */
  public VarScope() {
    this(null);
  }

  /**
   * Constructor.
   * @param par parent scope
   */
  private VarScope(final VarScope par) {
    parent = par;
  }

  /**
   * Adds a variable to this scope.
   * @param var variable to be added
   * @return the variable (for convenience)
   */
  private Var add(final Var var) {
    var.slot = vars.size();
    for(final Var v : vars) if(v.is(var)) throw Util.notexpected(var);
    vars.add(var);
    current.push(var);
    return var;
  }

  /**
   * Resolves a variable and adds it to all enclosing scopes.
   * @param name variable name
   * @param qp parser
   * @param ctx query context
   * @param ii input info
   * @param err error to be thrown if the variable doesn't exist
   * @return variable reference
   * @throws QueryException if the variable can't be found
   */
  public Expr resolve(final QNm name, final QueryParser qp, final QueryContext ctx,
      final InputInfo ii, final Err err) throws QueryException {
    final Var v = current.get(name);
    if(v != null) return new VarRef(ii, v);

    if(parent != null) {
      final Expr nonLocal = parent.resolve(name, qp, ctx, ii, err);
      if(!(nonLocal instanceof VarRef)) return nonLocal;

      // a variable in the closure
      final Var local = new Var(ctx, name, nonLocal.type());
      add(local);
      closure.put(local, nonLocal);
      return new VarRef(ii, local);
    }

    // global variable
    GlobalVar global = ctx.globals.get(name);
    if(global == null && err != null) throw qp.error(err, '$' + string(name.string()));
    return global;
  }

  /**
   * Opens a new sub-scope inside this scope. The returned marker has to be supplied to
   * the corresponding call to {@link VarScope#close(int)} in order to mark the variables
   * as inaccessible.
   * @return marker for the current bindings
   */
  public int open() {
    return current.size;
  }

  /**
   * Closes the sub-scope and marks all contained variables as inaccessible.
   * @param marker marker for the start of the sub-scope
   */
  public void close(final int marker) {
    current.size = marker;
  }

  /**
   * Get a sub-scope of this scope.
   * @return sub-scope
   */
  public VarScope child() {
    return new VarScope(this);
  }

  /**
   * Parent scope of this scope.
   * @return parent
   */
  public VarScope parent() {
    return parent;
  }

  /**
   * Creates a variable with a unique, non-clashing variable name.
   * @param ctx context for variable ID
   * @param type type
   * @param param function parameter flag
   * @return variable
   */
  public Var uniqueVar(final QueryContext ctx, final SeqType type, final boolean param) {
    return add(new Var(ctx, new QNm(token(ctx.varIDs)), type, param));
  }

  /**
   * Creates a new local variable in this scope.
   * @param ctx query context
   * @param name variable name
   * @param typ type of the variable
   * @param param function parameter flag
   * @return the variable
   */
  public Var newLocal(final QueryContext ctx, final QNm name, final SeqType typ,
      final boolean param) {
    return add(new Var(ctx, name, typ, param));
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param ctx query context
   * @param var variable to copy
   * @return the variable
   */
  public Var newCopyOf(final QueryContext ctx, final Var var) {
    return add(new Var(ctx, var));
  }

  /**
   * Get the closure of this scope.
   * @return mapping from non-local to local variables
   */
  public Map<Var, Expr> closure() {
    return closure;
  }

  /**
   * Enters this scope.
   * @param ctx query context
   * @return old stack frame
   */
  public Value[] enter(final QueryContext ctx) {
    final Value[] old = ctx.stackFrame;
    ctx.stackFrame = new Value[vars.size()];
    return old;
  }

  /**
   * Exits this scope.
   * @param ctx query context
   * @param old stack frame of the enclosing scope, or {@code null}
   */
  public void exit(final QueryContext ctx, final Value[] old) {
    ctx.stackFrame = old;
  }

  /**
   * Deletes all unused variables from this scope and assigns stack slots.
   * This method should be run after compiling the scope.
   * @param expr the scope
   */
  public void cleanUp(final Scope expr) {
    final BitSet declared = new BitSet();
    final AtomicInteger counter = new AtomicInteger();
    expr.visit(new ASTVisitor() {
      @Override
      public boolean declared(final Var var) {
        declared.set(var.id);
        var.slot = counter.getAndIncrement();
        return true;
      }
    });

    // purge all unused variables
    final Iterator<Var> iter = vars.iterator();
    while(iter.hasNext()) {
      final Var v = iter.next();
      if(!declared.get(v.id)) {
        v.slot = -1;
        iter.remove();
      }
    }
  }

  @Override
  public String toString() {
    return Util.name(this) + vars.toString();
  }

  /**
   * Gathers all parameters in this scope.
   * @return array of parameters
   */
  public Var[] params() {
    int n = 0;
    for(final Var v : vars) if(v.param) n++;
    final Var[] arr = new Var[n];
    for(final Var v : vars) if(v.param) arr[arr.length - n--] = v;
    return arr;
  }

  /**
   * Stack-frame size needed for this scope.
   * @return stack-frame size
   */
  public int stackSize() {
    return vars.size();
  }

  /**
   * Copies this VarScope.
   * @param ctx query context
   * @param scp new parent scope
   * @param vs variable mapping
   * @return copied scope
   */
  public VarScope copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final VarScope sc = new VarScope(scp);
    for(final Var v : vars) vs.add(v.id, sc.newCopyOf(ctx, v));
    for(final Entry<Var, Expr> e : closure.entrySet()) {
      final Var v = vs.get(e.getKey().id);
      final Expr ex = e.getValue().copy(ctx, scp, vs);
      sc.closure.put(v, ex);
    }
    sc.current.clear();
    return sc;
  }
}
