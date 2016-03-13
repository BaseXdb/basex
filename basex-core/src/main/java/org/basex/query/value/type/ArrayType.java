package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Type for arrays.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FuncType {
  /**
   * Constructor.
   * @param rt return type
   */
  ArrayType(final SeqType rt) {
    super(rt, SeqType.ITR);
  }

  @Override
  public byte[] string() {
    return ARRAY;
  }

  @Override
  public Array cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    if(item instanceof Array) {
      final Array a = (Array) item;
      if(a.instanceOf(this)) return a;
    }
    throw castError(ii, item, this);
  }

  @Override
  public boolean eq(final Type t) {
    return this == t || t instanceof ArrayType && type.eq(((ArrayType) t).type);
  }

  @Override
  public boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(t == AtomType.ITEM || t == SeqType.ANY_ARRAY || t == SeqType.ANY_FUN) return true;
    if(!(t instanceof FuncType) || t instanceof MapType) return false;

    final FuncType ft = (FuncType) t;
    final int al = argTypes.length;
    if(al != ft.argTypes.length || !type.instanceOf(ft.type)) return false;
    if(t instanceof ArrayType) return true;

    // test function arguments of function type
    for(int a = 0; a < al; a++) {
      if(!argTypes[a].instanceOf(ft.argTypes[a])) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type t) {
    if(instanceOf(t)) return t;
    if(t instanceof ArrayType) {
      final ArrayType mt = (ArrayType) t;
      return mt.instanceOf(this) ? this : get(type.union(mt.type));
    }
    return t instanceof MapType ? SeqType.ANY_FUN : t instanceof FuncType ? t.union(this) :
      AtomType.ITEM;
  }

  @Override
  public ArrayType intersect(final Type t) {
    // case for item() and compatible FuncType, e.g. function(xs:anyAtomicType) as item()*
    // also excludes FuncType.ANY_FUN
    if(instanceOf(t)) return this;
    if(t instanceof ArrayType) {
      final ArrayType mt = (ArrayType) t;
      if(mt.instanceOf(this)) return mt;
      final SeqType rt = type.intersect(mt.type);
      return rt == null ? null : get(rt);
    }
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ITR)) {
        final SeqType rt = type.intersect(ft.type);
        return rt == null ? null : get(rt);
      }
    }
    return null;
  }

  /**
   * Creates a new array type.
   * @param val value type
   * @return array type
   */
  public static ArrayType get(final SeqType val) {
    return val.eq(SeqType.ITEM_ZM) ? SeqType.ANY_ARRAY : new ArrayType(val);
  }

  @Override
  public String toString() {
    return type.eq(SeqType.ITEM_ZM) ? "array(*)" : "array(" + type + ')';
  }
}
