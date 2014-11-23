package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XQuery 3.0 function types.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class FuncType implements Type {
  /** Any function type. */
  public static final FuncType ANY_FUN = new FuncType(null, null, null);

  /** Annotations. */
  private final Ann ann;
  /** Argument types. */
  public final SeqType[] argTypes;
  /** Return type. */
  public final SeqType retType;

  /** This function type's sequence type (lazy instantiation). */
  private SeqType seqType;

  /**
   * Constructor.
   * @param ann annotations
   * @param argTypes argument types
   * @param retType return type
   */
  FuncType(final Ann ann, final SeqType[] argTypes, final SeqType retType) {
    this.ann = ann != null ? ann : new Ann();
    this.argTypes = argTypes;
    this.retType = retType;
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
    if(seqType == null) seqType = new SeqType(this);
    return seqType;
  }

  @Override
  public byte[] string() {
    return token(FUNCTION);
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    if(!(item instanceof FItem)) throw castError(ii, item, this);
    final FItem f = (FItem) item;
    return this == ANY_FUN ? f : f.coerceTo(this, qc, ii, false);
  }

  @Override
  public final Item cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) {
    throw Util.notExpected(value);
  }

  @Override
  public final Item castString(final String string, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) {
    throw Util.notExpected(string);
  }

  @Override
  public boolean eq(final Type t) {
    if(this == t) return true;
    if(t.getClass() != FuncType.class) return false;
    final FuncType ft = (FuncType) t;

    // check annotations
    final int as = ann.size();
    if(as != ft.ann.size()) return false;
    for(int a = 0; a < as; a++) {
      if(!ann.contains(ft.ann.names[a], ft.ann.values[a])) return false;
    }

    if(this == ANY_FUN || ft == ANY_FUN || argTypes.length != ft.argTypes.length) return false;
    final int al = argTypes.length;
    for(int i = 0; i < al; i++) {
      if(!argTypes[i].eq(ft.argTypes[i])) return false;
    }
    return retType.eq(ft.retType);
  }

  @Override
  public boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(!(t instanceof FuncType)) return t == AtomType.ITEM;
    if(t instanceof MapType || t instanceof ArrayType) return false;

    // check annotations
    final FuncType ft = (FuncType) t;
    final int as = ft.ann.size();
    for(int a = 0; a < as; a++) {
      if(!ann.contains(ft.ann.names[a], ft.ann.values[a])) return false;
    }

    // takes care of FunType.ANY
    if(this == ft || ft == ANY_FUN) return true;
    if(this == ANY_FUN || argTypes.length != ft.argTypes.length || !retType.instanceOf(ft.retType))
      return false;

    final int al = argTypes.length;
    for(int a = 0; a < al; a++) {
      if(!ft.argTypes[a].instanceOf(argTypes[a])) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type t) {
    if(!(t instanceof FuncType)) return AtomType.ITEM;

    final FuncType ft = (FuncType) t;
    if(this == ANY_FUN || ft == ANY_FUN) return ANY_FUN;

    final int al = argTypes.length;
    if(al != ft.argTypes.length) return ANY_FUN;

    final SeqType[] arg = new SeqType[al];
    for(int a = 0; a < al; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return ANY_FUN;
    }
    return get(ann.intersect(ft.ann), retType.union(ft.retType), arg);
  }

  @Override
  public Type intersect(final Type t) {
    // ensure commutativity
    if(t instanceof MapType || t instanceof ArrayType) return t.intersect(this);

    // the easy cases
    if(instanceOf(t)) return this;
    if(t.instanceOf(this)) return t;

    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      // ANY_FUN is excluded by the easy cases
      final SeqType rt = retType.intersect(ft.retType);
      final int al = argTypes.length;
      if(rt != null && al == ft.argTypes.length) {
        final SeqType[] arg = new SeqType[al];
        for(int a = 0; a < al; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
        final Ann a = ann.union(ft.ann);
        return a == null ? null : get(a, rt, arg);
      }
    }
    return null;
  }

  /**
   * Getter for function types.
   * @param ann annotations
   * @param ret return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final Ann ann, final SeqType ret, final SeqType... args) {
    return args == null || ret == null ? ANY_FUN : new FuncType(ann, args, ret);
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
   * @param type type
   * @return type or {@code null}
   */
  public static Type find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      if(Token.eq(ln, token(FUNCTION))) return ANY_FUN;
      if(Token.eq(ln, MAP)) return SeqType.ANY_MAP;
      if(Token.eq(ln, ARRAY)) return SeqType.ANY_ARRAY;
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
    final int al = args.length;
    final SeqType[] at = new SeqType[al];
    for(int a = 0; a < al; a++) at[a] = args[a] == null ? SeqType.ITEM_ZM : args[a].declaredType();
    return new FuncType(an, at, ret == null ? SeqType.ITEM_ZM : ret);
  }

  @Override
  public final ID id() {
    return Type.ID.FUN;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(ann.toString()).add(FUNCTION).add('(');
    if(this == ANY_FUN) {
      tb.add('*').add(')');
    } else {
      tb.addSep(argTypes, ", ").add(") as ").add(retType.toString());
    }
    return tb.toString();
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }
}
