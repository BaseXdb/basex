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
  /** Return type of the function. */
  public final SeqType declType;
  /** Argument types (can be {@code null}, indicated that no types were specified). */
  public final SeqType[] argTypes;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param declType declared return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final SeqType declType, final SeqType... argTypes) {
    this(new AnnList(), declType, argTypes);
  }

  /**
   * Constructor.
   * @param anns annotations
   * @param declType declared return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final AnnList anns, final SeqType declType, final SeqType... argTypes) {
    this.anns = anns;
    this.declType = declType == null ? SeqType.ITEM_ZM : declType;
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
  public final boolean isSortable() {
    return false;
  }

  @Override
  public SeqType seqType(final Occ occ) {
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public byte[] string() {
    return token(FUNCTION);
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {

    if(!(item instanceof FItem)) throw typeError(item, this, info);
    final FItem func = (FItem) item;
    return this == SeqType.ANY_FUNC ? func : func.coerceTo(this, qc, info, false);
  }

  @Override
  public final Item cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo info) {
    throw Util.notExpected(value);
  }

  @Override
  public final Item castString(final String string, final QueryContext qc, final StaticContext sc,
      final InputInfo info) {
    throw Util.notExpected(string);
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(type.getClass() != FuncType.class) return false;
    final FuncType ft = (FuncType) type;

    if(this == SeqType.ANY_FUNC || ft == SeqType.ANY_FUNC ||
        argTypes.length != ft.argTypes.length) return false;

    final int al = argTypes.length;
    for(int a = 0; a < al; a++) {
      if(!argTypes[a].eq(ft.argTypes[a])) return false;
    }
    return declType.eq(ft.declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    // the only non-function super-type of function is item()
    if(type == AtomType.ITEM || type == SeqType.ANY_FUNC) return true;
    if(!(type instanceof FuncType) || type instanceof MapType || type instanceof ArrayType ||
        this == SeqType.ANY_FUNC) return false;

    final FuncType ft = (FuncType) type;
    final int al = argTypes.length;
    if(al != ft.argTypes.length || !declType.instanceOf(ft.declType)) return false;

    for(int a = 0; a < al; a++) {
      if(!ft.argTypes[a].instanceOf(argTypes[a])) return false;
    }
    for(final Ann ann : ft.anns) if(!anns.contains(ann)) return false;
    return true;
  }

  @Override
  public Type union(final Type type) {
    if(!(type instanceof FuncType)) return AtomType.ITEM;

    final FuncType ft = (FuncType) type;
    if(this == SeqType.ANY_FUNC || ft == SeqType.ANY_FUNC) return SeqType.ANY_FUNC;

    final int al = argTypes.length;
    if(al != ft.argTypes.length) return SeqType.ANY_FUNC;

    final SeqType[] arg = new SeqType[al];
    for(int a = 0; a < al; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return SeqType.ANY_FUNC;
    }
    return get(anns.union(ft.anns), declType.union(ft.declType), arg);
  }

  @Override
  public Type intersect(final Type type) {
    // ensure commutativity
    if(type instanceof MapType || type instanceof ArrayType) return type.intersect(this);

    // the easy cases
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      // ANY_FUN is excluded by the easy cases
      final SeqType dt = declType.intersect(ft.declType);
      final int al = argTypes.length;
      if(dt != null && al == ft.argTypes.length) {
        final SeqType[] arg = new SeqType[al];
        for(int a = 0; a < al; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
        final AnnList list = anns.intersect(ft.anns);
        return list == null ? null : get(list, dt, arg);
      }
    }
    return null;
  }

  /**
   * Getter for function types.
   * @param anns annotations
   * @param declType declared return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType declType, final SeqType... args) {
    return new FuncType(anns, declType, args);
  }

  /**
   * Getter for function types without annotations.
   * @param declType declared return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final SeqType declType, final SeqType... args) {
    return get(new AnnList(), declType, args);
  }

  /**
   * Finds and returns the specified function type.
   * @param name name of type
   * @return type or {@code null}
   */
  public static Type find(final QNm name) {
    if(name.uri().length == 0) {
      final byte[] ln = name.local();
      if(Token.eq(ln, token(FUNCTION))) return SeqType.ANY_FUNC;
      if(Token.eq(ln, token(MAP))) return SeqType.ANY_MAP;
      if(Token.eq(ln, token(ARRAY))) return SeqType.ANY_ARRAY;
    }
    return null;
  }

  /**
   * Getter for a function's type.
   * @param anns annotations
   * @param declType declared return type (can be {@code null})
   * @param params formal parameters
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType declType, final Var[] params) {
    final int pl = params.length;
    final SeqType[] argTypes = new SeqType[pl];
    for(int p = 0; p < pl; p++) {
      argTypes[p] = params[p] == null ? SeqType.ITEM_ZM : params[p].declaredType();
    }
    return new FuncType(anns, declType, argTypes);
  }

  @Override
  public final AtomType atomic() {
    return null;
  }

  @Override
  public final ID id() {
    return Type.ID.FUN;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(anns.toString()).add(FUNCTION).add('(');
    if(this == SeqType.ANY_FUNC) {
      tb.add('*').add(')');
    } else {
      tb.addSep(argTypes, ", ").add(") as ").add(declType.toString());
    }
    return tb.toString();
  }
}
