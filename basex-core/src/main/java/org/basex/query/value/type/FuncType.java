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
  /** Declared return type (observed by instance-of, coercion and subtyping). */
  public final SeqType declType;
  /**
   * Refined return type, a subtype of {@link #declType} inferred from the function body. Used for
   * result typing (e.g. dynamic calls, folds); never observed by instance-of, so that a function
   * whose body is more specific than its declared return type still matches only its declaration.
   */
  public final SeqType refinedType;
  /** Argument types (can be {@code null}, indicates that no types were specified). */
  public final SeqType[] argTypes;

  /**
   * Constructor.
   * @param declType declared return type (can be {@code null})
   * @param argTypes argument types (can be {@code null})
   */
  FuncType(final SeqType declType, final SeqType... argTypes) {
    this(AnnList.EMPTY, declType, null, argTypes);
  }

  /**
   * Constructor.
   * @param anns annotations
   * @param declType declared return type (can be {@code null})
   * @param refinedType refined return type ({@code null}: same as declared type)
   * @param argTypes argument types (can be {@code null})
   */
  private FuncType(final AnnList anns, final SeqType declType, final SeqType refinedType,
      final SeqType[] argTypes) {
    this.anns = anns;
    this.declType = declType == null ? ITEM_ZM : declType;
    // the refined type must be a subtype of the declared type; a body type that is not (e.g. the
    // integer body of 'fn() as xs:double { 1 }', whose result is coerced) contributes no refinement
    this.refinedType = refinedType == null || !refinedType.instanceOf(this.declType) ? this.declType
                                                                                     : refinedType;
    this.argTypes = argTypes;
  }

  /**
   * Returns a copy of this function type with a refined (more specific) return type.
   * @param rt refined return type
   * @return function type
   */
  public FuncType withRefinedType(final SeqType rt) {
    return rt.eq(refinedType) ? this : new FuncType(anns, declType, rt, argTypes);
  }

  /**
   * Getter for function types.
   * @param anns annotations
   * @param declType declared return type
   * @param args argument types
   * @return function type
   */
  public static FuncType get(final AnnList anns, final SeqType declType, final SeqType... args) {
    return new FuncType(anns, declType, null, args);
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
    return new FuncType(anns, declType, null, argTypes);
  }

  /**
   * Finds and returns the specified function type.
   * @param name name of type
   * @return type or {@code null}
   */
  public static Type get(final QNm name) {
    if(name.uri().length != 0) return null;
    return switch(Token.string(name.local())) {
      case QueryText.FUNCTION, QueryText.FN -> FUNCTION;
      case QueryText.MAP    -> MAP;
      case QueryText.RECORD -> RECORD;
      case QueryText.ARRAY  -> ARRAY;
      default -> null;
    };
  }

  @Override
  public FuncType funcType() {
    return this;
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof final FItem func) return func.coerceTo(this, qc, null, info);
    throw typeError(item, this, info);
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
    if(this == type) return this;
    if(type instanceof ChoiceItemType) return type.union(this);
    final FuncType ft = type.funcType();

    // two function types (a map or array is viewed through its function type) with matching arity:
    // union argument, declared and refined return types structurally. This must happen before the
    // instance-of short-circuits below, as those ignore the refined type: returning a single
    // operand would drop the other's refined return type, unsoundly narrowing the result type of a
    // call on the union.
    if(this != FUNCTION && ft != null && ft != FUNCTION && argTypes != null &&
        ft.argTypes != null && argTypes.length == ft.argTypes.length) {
      final int arity = argTypes.length;
      final SeqType[] arg = new SeqType[arity];
      for(int a = 0; a < arity; a++) {
        arg[a] = argTypes[a].intersect(ft.argTypes[a]);
        if(arg[a] == null) return FUNCTION;
      }
      return new FuncType(anns.union(ft.anns), declType.union(ft.declType),
          refinedType.union(ft.refinedType), arg);
    }

    if(type.instanceOf(this)) return this;
    if(instanceOf(type)) return type;
    return ft == null ? BasicType.ITEM : FUNCTION;
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);

    // if one function type is a subtype of the other, the intersection is that operand. Its refined
    // return type is still met with the other's (the instance-of check ignores the refined type),
    // yielding the most specific result type for calls on the intersection; if the refined types
    // are disjoint, the operand is kept unchanged (no less precise than before).
    if(instanceOf(type)) {
      if(type instanceof final FuncType ft) {
        final SeqType rf = refinedType.intersect(ft.refinedType);
        return rf == null ? this : withRefinedType(rf);
      }
      return this;
    }
    if(type.instanceOf(this)) {
      if(type instanceof final FuncType ft) {
        final SeqType rf = refinedType.intersect(ft.refinedType);
        return rf == null ? type : ft.withRefinedType(rf);
      }
      return type;
    }

    if(type instanceof MapType || type instanceof ArrayType) return type.intersect(this);

    final FuncType ft = type.funcType();
    if(ft == null) return null;

    final int arity = argTypes.length, nargs = ft.argTypes.length;
    if(arity != nargs) return null;

    final AnnList an = anns.intersect(ft.anns);
    if(an == null) return null;
    final SeqType dt = declType.intersect(ft.declType);
    if(dt == null) return null;
    final SeqType rf = refinedType.intersect(ft.refinedType);

    final SeqType[] arg = new SeqType[arity];
    for(int a = 0; a < arity; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
    return new FuncType(an, dt, rf, arg);
  }

  /**
   * Return function type with fewer arguments.
   * @param arity arity of target type
   * @return function type
   */
  public FuncType with(final int arity) {
    return new FuncType(anns, declType, refinedType, Arrays.copyOf(argTypes, arity));
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
  public String name() {
    return QueryText.FN;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(anns);
    qs.token(toString(", ", this == FUNCTION ? WILDCARD : argTypes));
    if(this != FUNCTION) qs.token(QueryText.AS).token(declType);
    return qs.toString();
  }
}
