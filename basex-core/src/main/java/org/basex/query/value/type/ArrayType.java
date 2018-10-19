package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Type for arrays.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FuncType {
  /**
   * Constructor.
   * @param declType declared return type
   */
  ArrayType(final SeqType declType) {
    super(declType, SeqType.ITR_O);
  }

  @Override
  public byte[] string() {
    return Token.token(ARRAY);
  }

  @Override
  public XQArray cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {

    if(item instanceof XQArray) {
      final XQArray a = (XQArray) item;
      if(a.instanceOf(this)) return a;
    }
    throw typeError(item, this, info);
  }

  @Override
  public boolean eq(final Type type) {
    return this == type || type instanceof ArrayType && declType.eq(((ArrayType) type).declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    // the only non-function super-type of function is item()
    if(type == AtomType.ITEM || type == SeqType.ANY_ARRAY || type == SeqType.ANY_FUNC) return true;
    if(!(type instanceof FuncType) || type instanceof MapType || this == SeqType.ANY_ARRAY)
      return false;

    final FuncType ft = (FuncType) type;
    final int al = argTypes.length;
    if(al != ft.argTypes.length || !declType.instanceOf(ft.declType)) return false;
    if(type instanceof ArrayType) return true;

    // test function arguments of function type
    // example: ["A"] instance of function(xs:string) as xs:string
    for(int a = 0; a < al; a++) {
      if(!argTypes[a].instanceOf(ft.argTypes[a])) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type)) return type;
    if(type instanceof ArrayType) {
      final ArrayType mt = (ArrayType) type;
      return mt.instanceOf(this) ? this : get(declType.union(mt.declType));
    }
    return type instanceof MapType ? SeqType.ANY_FUNC :
      type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public ArrayType intersect(final Type type) {
    // case for item() and compatible FuncType, e.g. function(xs:anyAtomicType) as item()*
    // also excludes FuncType.ANY_FUN
    if(instanceOf(type)) return this;
    if(type instanceof ArrayType) {
      final ArrayType at = (ArrayType) type;
      if(at.instanceOf(this)) return at;
      final SeqType dt = declType.intersect(at.declType);
      return dt == null ? null : get(dt);
    }
    if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ITR_O)) {
        final SeqType dt = declType.intersect(ft.declType);
        return dt == null ? null : get(dt);
      }
    }
    return null;
  }

  /**
   * Creates a new array type.
   * @param declType declared return type
   * @return array type
   */
  public static ArrayType get(final SeqType declType) {
    return declType.eq(SeqType.ITEM_ZM) ? SeqType.ANY_ARRAY : new ArrayType(declType);
  }

  @Override
  public String toString() {
    return declType.eq(SeqType.ITEM_ZM) ? "array(*)" : "array(" + declType + ')';
  }
}
