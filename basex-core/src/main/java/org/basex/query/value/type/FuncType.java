package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XQuery 3.0 function data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class FuncType implements Type {
  /** Any function type. */
  public static final FuncType ANY_FUN = new FuncType(null, null, null);

  /** Annotations. */
  public final Ann ann;
  /** Argument types. */
  public final SeqType[] args;
  /** Return type. */
  public final SeqType type;

  /** This function type's sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param a annotations
   * @param arg argument types
   * @param typ return type
   */
  FuncType(final Ann a, final SeqType[] arg, final SeqType typ) {
    ann = a != null ? a : new Ann();
    args = arg;
    type = typ;
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
  public final boolean isUntyped() {
    return false;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return false;
  }

  @Override
  public final boolean isStringOrUntyped() {
    return false;
  }

  @Override
  public final SeqType seqType() {
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public byte[] string() {
    return token(FUNCTION);
  }

  @Override
  public FItem cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(!(it instanceof FItem)) throw Err.cast(ii, this, it);
    final FItem f = (FItem) it;
    return this == ANY_FUN ? f : f.coerceTo(this, ctx, ii);
  }

  @Override
  public final Item cast(final Object o, final QueryContext ctx, final InputInfo ii) {
    throw Util.notexpected(o);
  }

  @Override
  public final Item castString(final String s, final QueryContext ctx,
      final InputInfo ii) {
    throw Util.notexpected(s);
  }

  @Override
  public boolean eq(final Type t) {
    if(this == t) return true;
    if(t.getClass() != FuncType.class) return false;
    final FuncType ft = (FuncType) t;

    // check annotations
    if(ann.size() != ft.ann.size()) return false;
    for(int i = 0; i < ann.size(); i++) {
      if(!ann.contains(ft.ann.names[i], ft.ann.values[i])) return false;
    }

    if(this == ANY_FUN || ft == ANY_FUN || args.length != ft.args.length) return false;
    for(int i = 0; i < args.length; i++) if(!args[i].eq(ft.args[i])) return false;
    return type.eq(ft.type);
  }

  @Override
  public boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(!(t instanceof FuncType)) return t == AtomType.ITEM;
    if(t instanceof MapType) return false;
    final FuncType ft = (FuncType) t;

    // check annotations
    for(int i = 0; i < ft.ann.size(); i++)
      if(!ann.contains(ft.ann.names[i], ft.ann.values[i])) return false;

    // takes care of FunType.ANY
    if(this == ft || ft == ANY_FUN) return true;
    if(this == ANY_FUN || args.length != ft.args.length ||
        !type.instanceOf(ft.type)) return false;
    for(int a = 0; a < args.length; a++) {
      if(!ft.args[a].instanceOf(args[a])) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type t) {
    if(!(t instanceof FuncType)) return AtomType.ITEM;
    final FuncType ft = (FuncType) t;
    if(this == ANY_FUN || ft == ANY_FUN || args.length != ft.args.length) return ANY_FUN;
    final SeqType[] arg = new SeqType[args.length];
    for(int i = 0; i < arg.length; i++) {
      arg[i] = args[i].intersect(ft.args[i]);
      if(arg[i] == null) return ANY_FUN;
    }
    return get(ann.intersect(ft.ann), type.union(ft.type), arg);
  }

  @Override
  public Type intersect(final Type t) {
    // ensure commutativity
    if(t instanceof MapType) return t.intersect(this);

    // the easy cases
    if(this.instanceOf(t)) return this;
    if(t.instanceOf(this)) return t;

    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      // ANY_FUN is excluded by the easy cases
      final SeqType rt = type.intersect(ft.type);
      if(rt != null && args.length == ft.args.length) {
        final SeqType[] arg = new SeqType[args.length];
        for(int i = 0; i < arg.length; i++) arg[i] = args[i].union(ft.args[i]);
        final Ann a = ann.union(ft.ann);
        return a == null ? null : get(a, rt, arg);
      }
    }
    return null;
  }

  /**
   * Getter for function types.
   * @param a annotations
   * @param ret return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final Ann a, final SeqType ret, final SeqType... args) {
    return args == null || ret == null ? ANY_FUN : new FuncType(a, args, ret);
  }

  /**
   * Getter for function types without annotations.
   * @param ret return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final SeqType ret, final SeqType... args) {
    return get(new Ann(), ret, args);
  }

  /**
   * Finds and returns the specified function type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static Type find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      if(Token.eq(ln, token(FUNCTION))) return FuncType.ANY_FUN;
      if(Token.eq(ln, MAP)) return SeqType.ANY_MAP;
    }
    return null;
  }

  /**
   * Getter for function types with a given arity.
   * @param a number of arguments
   * @return function type
   */
  public static FuncType arity(final int a) {
    final SeqType[] args = new SeqType[a];
    Arrays.fill(args, SeqType.ITEM_ZM);
    return get(null, SeqType.ITEM_ZM, args);
  }

  /**
   * Getter for a function's type.
   * @param an annotations
   * @param args formal parameters
   * @param ret return type
   * @return function type
   */
  public static FuncType get(final Ann an, final Var[] args, final SeqType ret) {
    final SeqType[] at = new SeqType[args.length];
    for(int a = 0; a < at.length; a++)
      at[a] = args[a] == null ? SeqType.ITEM_ZM : args[a].declaredType();
    return new FuncType(an, at, ret == null ? SeqType.ITEM_ZM : ret);
  }

  /**
   * Creates variables with types corresponding to this type's arguments.
   * @param vs array in which to write the variables
   * @param ctx query context
   * @param scp variable scope
   * @param ii input info
   * @return calls to the variables
   */
  public Expr[] args(final Var[] vs, final QueryContext ctx, final VarScope scp,
      final InputInfo ii) {

    final Expr[] refs = new Expr[vs.length];
    for(int i = 0; i < vs.length; i++)
      refs[i] = new VarRef(ii, vs[i] = scp.uniqueVar(ctx, args[i], true));
    return refs;
  }

  @Override
  public ID id() {
    return Type.ID.FUN;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(ann.toString()).add(FUNCTION).add('(');
    if(this == ANY_FUN) {
      tb.add('*').add(')');
    } else {
      tb.addSep(args, ", ").add(") as ").add(type.toString());
    }
    return tb.toString();
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }
}
