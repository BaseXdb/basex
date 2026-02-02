package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XDM function types.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class FuncType extends FType {
  /** Annotations. */
  public final AnnList anns;
  /** Return type of the function. */
  public final SeqType declType;
  /** Argument types (can be {@code null}, indicates that no types were specified). */
  public final SeqType[] argTypes;

  /**
   * Constructor.
   * @param declType declared return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final SeqType declType, final SeqType... argTypes) {
    this(AnnList.EMPTY, declType, argTypes);
  }

  /**
   * Constructor.
   * @param anns annotations
   * @param declType declared return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  private FuncType(final AnnList anns, final SeqType declType, final SeqType... argTypes) {
    this.anns = anns;
    this.declType = declType == null ? ITEM_ZM : declType;
    this.argTypes = argTypes;
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
    return get(AnnList.EMPTY, declType, args);
  }

  /**
   * Getter for a function's type.
   * @param anns annotations
   * @param declType declared return type (can be {@code null})
   * @param params parameters (can contain {@code null} references)
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType declType, final Var[] params) {
    final int pl = params.length;
    final SeqType[] argTypes = new SeqType[pl];
    for(int p = 0; p < pl; p++) {
      argTypes[p] = params[p] == null ? ITEM_ZM : params[p].declaredType();
    }
    return new FuncType(anns, declType, argTypes);
  }

  /**
   * Finds and returns the specified function type.
   * @param name name of type
   * @return type or {@code null}
   */
  public static Type get(final QNm name) {
    if(name.uri().length == 0) {
      switch(Token.string(name.local())) {
        case QueryText.FUNCTION:
        case QueryText.FN:       return FUNCTION;
        case QueryText.MAP:      return MAP;
        case QueryText.RECORD:   return RECORD;
        case QueryText.ARRAY:    return ARRAY;
      }
    }
    return null;
  }

  @Override
  public FuncType funcType() {
    return this;
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(!(item instanceof final FItem func)) throw typeError(item, this, info);
    return this == FUNCTION ? func : func.coerceTo(this, qc, null, info);
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(this == FUNCTION || type == FUNCTION || !(type instanceof final FuncType ft)) return false;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return false;
    for(int a = 0; a < arity; a++) {
      if(!argTypes[a].eq(ft.argTypes[a])) return false;
    }
    return declType.eq(ft.declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(FUNCTION, BasicType.ITEM)) return true;
    if(type instanceof final ChoiceItemType cit) return cit.hasInstance(this);
    if(this == FUNCTION || !(type instanceof final FuncType ft)) return false;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return false;
    for(int a = 0; a < arity; a++) {
      if(!ft.argTypes[a].instanceOf(argTypes[a])) return false;
    }
    for(final Ann ann : ft.anns) {
      if(!anns.contains(ann)) return false;
    }
    return declType.instanceOf(ft.declType);
  }

  @Override
  public Type union(final Type type) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    final FuncType ft = type.funcType();
    if(ft == null) return BasicType.ITEM;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return FUNCTION;

    final SeqType[] arg = new SeqType[arity];
    for(int a = 0; a < arity; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return FUNCTION;
    }
    return get(anns.union(ft.anns), declType.union(ft.declType), arg);
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof MapType || type instanceof ArrayType) return type.intersect(this);

    final FuncType ft = type.funcType();
    if(ft == null) return null;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return null;

    final AnnList an = anns.intersect(ft.anns);
    if(an == null) return null;
    final SeqType dt = declType.intersect(ft.declType);
    if(dt == null) return null;

    final SeqType[] arg = new SeqType[arity];
    for(int a = 0; a < arity; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
    return get(an, dt, arg);
  }

  /**
   * Return function type with fewer arguments.
   * @param arity arity of target type
   * @return function type
   */
  public FuncType with(final int arity) {
    return get(anns, declType, Arrays.copyOf(argTypes, arity));
  }

  @Override
  public BasicType atomic() {
    return null;
  }

  @Override
  public ID id() {
    return Type.ID.FUN;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(anns).token(QueryText.FUNCTION);
    if(this == FUNCTION) qs.params(WILDCARD);
    else qs.params(argTypes).token(QueryText.AS).token(declType);
    return qs.toString();
  }
}
