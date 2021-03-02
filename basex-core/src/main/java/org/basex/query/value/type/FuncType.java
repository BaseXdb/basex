package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * XQuery 3.0 function types.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public class FuncType implements Type {
  /** Name. */
  public static final byte[] FUNCTION = Token.token(QueryText.FUNCTION);
  /** Any function placeholder string. */
  static final String[] WILDCARD = { "*" };

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
  private FuncType(final AnnList anns, final SeqType declType, final SeqType... argTypes) {
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
    // cannot statically be instantiated due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public FItem cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    if(!(item instanceof FItem)) throw typeError(item, this, ii);
    final FItem func = (FItem) item;
    return this == SeqType.FUNCTION ? func : func.coerceTo(this, qc, ii, false);
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
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(type.getClass() != FuncType.class) return false;
    final FuncType ft = (FuncType) type;

    if(this == SeqType.FUNCTION || ft == SeqType.FUNCTION || argTypes.length != ft.argTypes.length)
      return false;

    final int al = argTypes.length;
    for(int a = 0; a < al; a++) {
      if(!argTypes[a].eq(ft.argTypes[a])) return false;
    }
    return declType.eq(ft.declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(type.oneOf(this, SeqType.FUNCTION, AtomType.ITEM)) return true;
    if(this == SeqType.FUNCTION || !(type instanceof FuncType) || type instanceof MapType ||
        type instanceof ArrayType) return false;

    final FuncType ft = (FuncType) type;
    final int al = argTypes.length;
    if(al != ft.argTypes.length) return false;
    for(int a = 0; a < al; a++) {
      if(!ft.argTypes[a].instanceOf(argTypes[a])) return false;
    }
    for(final Ann ann : ft.anns) {
      if(!anns.contains(ann)) return false;
    }
    return declType.instanceOf(ft.declType);
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(!(type instanceof FuncType)) return AtomType.ITEM;

    final FuncType ft = (FuncType) type;
    final int al = argTypes.length;
    if(al != ft.argTypes.length) return SeqType.FUNCTION;

    final SeqType[] arg = new SeqType[al];
    for(int a = 0; a < al; a++) {
      arg[a] = argTypes[a].intersect(ft.argTypes[a]);
      if(arg[a] == null) return SeqType.FUNCTION;
    }

    final AnnList an = anns.union(ft.anns);
    final SeqType dt = declType.union(ft.declType);
    return get(an, dt, arg);
  }

  @Override
  public Type intersect(final Type type) {
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(!(type instanceof FuncType)) return null;
    if(type instanceof MapType || type instanceof ArrayType) return type.intersect(this);

    final FuncType ft = (FuncType) type;
    final int al = argTypes.length;
    if(al != ft.argTypes.length) return null;

    final AnnList an = anns.intersect(ft.anns);
    if(an == null) return null;
    final SeqType dt = declType.intersect(ft.declType);
    if(dt == null) return null;

    final SeqType[] arg = new SeqType[al];
    for(int a = 0; a < al; a++) arg[a] = argTypes[a].union(ft.argTypes[a]);
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
      if(Token.eq(ln, FuncType.FUNCTION)) return SeqType.FUNCTION;
      if(Token.eq(ln, MapType.MAP)) return SeqType.MAP;
      if(Token.eq(ln, ArrayType.ARRAY)) return SeqType.ARRAY;
    }
    return null;
  }

  /**
   * Returns an info message for a similar function.
   * @param qname name of type
   * @return info string
   */
  public static byte[] similar(final QNm qname) {
    final byte[] ln = lc(qname.local());

    final TokenList list = new TokenList();
    list.add(AtomType.ITEM.qname().local()).add(FuncType.FUNCTION);
    list.add(MapType.MAP).add(ArrayType.ARRAY);
    for(final NodeType type : NodeType.values()) list.add(type.qname().local());
    final byte[][] values = list.finish();

    Object similar = Levenshtein.similar(ln, values);
    if(similar == null) {
      for(final byte[] value : values) {
        if(startsWith(value, ln)) {
          similar = value;
          break;
        }
      }
    }
    return QueryError.similar(qname.prefixId(XML), similar);
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
    final QueryString qs = new QueryString().token(anns).token(FUNCTION);
    if(this == SeqType.FUNCTION) qs.params(WILDCARD);
    else qs.params(argTypes).token(QueryText.AS).token(declType);
    return qs.toString();
  }
}
