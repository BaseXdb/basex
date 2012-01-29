package org.basex.query.item;

import java.util.Arrays;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.UserFunc;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import static org.basex.query.QueryText.*;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * XQuery 3.0 function data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class FuncType implements Type {
  /** Any function type. */
  public static final FuncType ANY_FUN = new FuncType(null, null);

  /** Argument types. */
  public final SeqType[] args;
  /** Return type. */
  public final SeqType ret;

  /** This function type's sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param arg argument types
   * @param rt return type
   */
  FuncType(final SeqType[] arg, final SeqType rt) {
    args = arg;
    ret = rt;
  }

  @Override
  public final boolean isNode() {
    return false;
  }

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final SeqType seq() {
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public final boolean isString() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return false;
  }

  @Override
  public final boolean isFunction() {
    return true;
  }

  @Override
  public byte[] string() {
    return Token.token(FUNCTION);
  }

  @Override
  public FItem e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(!it.type.isFunction()) throw Err.cast(ii, this, it);
    final FItem f = (FItem) it;
    return this == ANY_FUN ? f : f.coerceTo(this, ctx, ii);
  }

  @Override
  public final Item e(final Object o, final InputInfo ii) {
    throw Util.notexpected(o);
  }

  @Override
  public final boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(!(t instanceof FuncType)) return t == AtomType.ITEM;
    final FuncType ft = (FuncType) t;

    // takes care of FunType.ANY
    if(this == ft || ft == ANY_FUN) return true;
    if(this == ANY_FUN || args.length != ft.args.length ||
        !ret.instance(ft.ret)) return false;
    for(int a = 0; a < args.length; a++) {
      if(!ft.args[a].instance(args[a])) return false;
    }
    return true;
  }

  /**
   * Getter for function types.
   * @param ret return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final SeqType ret, final SeqType... args) {
    if(args == null || ret == null) return ANY_FUN;
    return new FuncType(args, ret);
  }

  /**
   * Getter for function types with a given arity.
   * @param a number of arguments
   * @return function type
   */
  public static FuncType arity(final int a) {
    final SeqType[] args = new SeqType[a];
    Arrays.fill(args, SeqType.ITEM_ZM);
    return get(SeqType.ITEM_ZM, args);
  }

  /**
   * Getter for a function's type.
   * @param f user-defined function
   * @return function type
   */
  public static FuncType get(final UserFunc f) {
    final SeqType[] at = new SeqType[f.args.length];
    for(int a = 0; a < at.length; a++) {
      at[a] = f.args[a] == null || f.args[a].type == null ?
          SeqType.ITEM_ZM : f.args[a].type;
    }
    return new FuncType(at, f.ret == null ? SeqType.ITEM_ZM : f.ret);
  }

  /**
   * Sets the types of the given variables.
   * @param vars variables to type
   * @return the variables for convenience
   */
  public final Var[] type(final Var[] vars) {
    if(this != ANY_FUN) {
      for(int v = 0; v < vars.length; v++)
        if(vars[v] != null && args[v] != SeqType.ITEM_ZM)
          vars[v].type = args[v];
    }
    return vars;
  }

  @Override
  public boolean isDuration() {
    return false;
  }

  @Override
  public boolean isDate() {
    return false;
  }

  @Override
  public boolean isMap() {
    return false;
  }

  @Override
  public int id() {
    return 7;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(FUNCTION).add('(');
    if(this == ANY_FUN) {
      tb.add('*').add(')');
    } else {
      tb.addSep(args, ", ").add(") as ").add(ret.toString());
    }
    return tb.toString();
  }
}
