package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XQuery 3.0 function types.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public class FuncType extends FType {
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
    this.declType = declType == null ? SeqType.ITEM_ZM : declType;
    this.argTypes = argTypes;
  }

  @Override
  public FuncType funcType() {
    return this;
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(!(item instanceof FItem)) throw typeError(item, this, info);
    final FItem func = (FItem) item;
    return this == SeqType.FUNCTION ? func : func.coerceTo(this, qc, null, info);
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(this == SeqType.FUNCTION || type == SeqType.FUNCTION || !(type instanceof FuncType))
      return false;

    final FuncType ft = (FuncType) type;
    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return false;
    for(int a = 0; a < arity; a++) {
      if(!argTypes[a].eq(ft.argTypes[a])) return false;
    }
    return declType.eq(ft.declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(type instanceof ChoiceItemType) return ((ChoiceItemType) type).hasInstance(this);
    if(this == SeqType.FUNCTION || !(type instanceof FuncType)) return false;

    final FuncType ft = (FuncType) type;
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
    if(ft == null) return AtomType.ITEM;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return SeqType.FUNCTION;

    final SeqType[] arg = new SeqType[arity];
    for(int a = 0; a < arity; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return SeqType.FUNCTION;
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
   * Return function type with fewer arguments.
   * @param arity arity of target type
   * @return function type
   */
  public FuncType with(final int arity) {
    return get(anns, declType, Arrays.copyOf(argTypes, arity));
  }

  /**
   * Finds and returns the specified function type.
   * @param name name of type
   * @return type or {@code null}
   */
  public static Type find(final QNm name) {
    if(name.uri().length == 0) {
      final String ln = Token.string(name.local());
      if(ln.equals(QueryText.FUNCTION) || ln.equals(QueryText.FN)) return SeqType.FUNCTION;
      if(ln.equals(QueryText.MAP)) return SeqType.MAP;
      if(ln.equals(QueryText.ARRAY)) return SeqType.ARRAY;
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
  public AtomType atomic() {
    return null;
  }

  @Override
  public ID id() {
    return Type.ID.FUN;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(anns).token(QueryText.FN);
    if(this == SeqType.FUNCTION) qs.params(WILDCARD);
    else qs.params(argTypes).token(QueryText.AS).token(declType);
    return qs.toString();
  }
}
