package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XQuery 3.0 function types.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public class FuncType implements Type {
  /** Annotations. */
  public final AnnList anns;
  /** Declared type, {@code null} if not specified. */
  public final SeqType type;
  /** Argument types (can be {@code null}). */
  public final SeqType[] argTypes;

  /** This function type's sequence type (lazy instantiation). */
  private SeqType seqType;

  /**
   * Constructor.
   * @param type return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final SeqType type, final SeqType... argTypes) {
    this(null, type, argTypes);
  }

  /**
   * Constructor.
   * @param anns annotations (can be {@code null})
   * @param type return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final AnnList anns, final SeqType type, final SeqType... argTypes) {
    this.anns = anns == null ? new AnnList() : anns;
    this.type = type == null ? SeqType.ITEM_ZM : type;
    this.argTypes = argTypes;
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

    if(!(item instanceof FItem)) throw castError(item, this, ii);
    final FItem f = (FItem) item;
    return this == SeqType.ANY_FUN ? f : f.coerceTo(this, qc, ii, false);
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

    if(this == SeqType.ANY_FUN || ft == SeqType.ANY_FUN || argTypes.length != ft.argTypes.length)
      return false;

    final int al = argTypes.length;
    for(int i = 0; i < al; i++) {
      if(!argTypes[i].eq(ft.argTypes[i])) return false;
    }
    return type.eq(ft.type);
  }

  @Override
  public boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(t == AtomType.ITEM || t == SeqType.ANY_FUN) return true;
    if(!(t instanceof FuncType) || t instanceof MapType || t instanceof ArrayType) return false;

    // check annotations
    final FuncType ft = (FuncType) t;
    for(final Ann ann : ft.anns) if(!anns.contains(ann)) return false;

    // takes care of FunType.ANY
    if(this == SeqType.ANY_FUN || argTypes.length != ft.argTypes.length ||
        !type.instanceOf(ft.type)) return false;

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
    if(this == SeqType.ANY_FUN || ft == SeqType.ANY_FUN) return SeqType.ANY_FUN;

    final int al = argTypes.length;
    if(al != ft.argTypes.length) return SeqType.ANY_FUN;

    final SeqType[] arg = new SeqType[al];
    for(int a = 0; a < al; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return SeqType.ANY_FUN;
    }
    return get(anns.intersect(ft.anns), type.union(ft.type), arg);
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
      final SeqType rt = type.intersect(ft.type);
      final int al = argTypes.length;
      if(rt != null && al == ft.argTypes.length) {
        final SeqType[] arg = new SeqType[al];
        for(int a = 0; a < al; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
        final AnnList a = anns.union(ft.anns);
        return a == null ? null : get(a, rt, arg);
      }
    }
    return null;
  }

  /**
   * Getter for function types.
   * @param anns annotations
   * @param type return type ({@code null}: any return type)
   * @param args argument types ({@code null}: any function)
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType type, final SeqType... args) {
    return args == null ? SeqType.ANY_FUN : new FuncType(anns, type, args);
  }

  /**
   * Getter for function types without annotations.
   * @param type return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final SeqType type, final SeqType... args) {
    return get(null, type, args);
  }

  /**
   * Finds and returns the specified function type.
   * @param type type
   * @return type or {@code null}
   */
  public static Type find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      if(Token.eq(ln, token(FUNCTION))) return SeqType.ANY_FUN;
      if(Token.eq(ln, token(MAP))) return SeqType.ANY_MAP;
      if(Token.eq(ln, token(ARRAY))) return SeqType.ANY_ARRAY;
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
    return get(new AnnList(), SeqType.ITEM_ZM, args);
  }

  /**
   * Getter for a function's type.
   * @param anns annotations (can be {@code null})
   * @param type return type (can be {@code null})
   * @param args formal parameters
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType type, final Var[] args) {
    final int al = args.length;
    final SeqType[] at = new SeqType[al];
    for(int a = 0; a < al; a++) at[a] = args[a] == null ? SeqType.ITEM_ZM : args[a].declaredType();
    return new FuncType(anns, type, at);
  }

  @Override
  public final ID id() {
    return Type.ID.FUN;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(anns.toString()).add(FUNCTION).add('(');
    if(this == SeqType.ANY_FUN) {
      tb.add('*').add(')');
    } else {
      tb.addSep(argTypes, ", ").add(") as ").add(type.toString());
    }
    return tb.toString();
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }
}
