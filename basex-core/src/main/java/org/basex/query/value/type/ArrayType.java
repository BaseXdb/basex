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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FuncType {
  /**
   * Constructor.
   * @param valueType return type
   */
  ArrayType(final SeqType valueType) {
    super(valueType, SeqType.ITR);
  }

  @Override
  public byte[] string() {
    return Token.token(ARRAY);
  }

  @Override
  public Array cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {

    if(item instanceof Array) {
      final Array a = (Array) item;
      if(a.instanceOf(this)) return a;
    }
    throw castError(item, this, info);
  }

  @Override
  public boolean eq(final Type t) {
    return this == t || t instanceof ArrayType && valueType.eq(((ArrayType) t).valueType);
  }

  @Override
  public boolean instanceOf(final Type t) {
    // the only non-function super-type of function is item()
    if(t == AtomType.ITEM || t == SeqType.ANY_ARRAY || t == SeqType.ANY_FUN) return true;
    if(!(t instanceof FuncType) || t instanceof MapType || this == SeqType.ANY_ARRAY) return false;

    final FuncType ft = (FuncType) t;
    final int al = argTypes.length;
    if(al != ft.argTypes.length || !valueType.instanceOf(ft.valueType)) return false;
    if(t instanceof ArrayType) return true;

    // test function arguments of function type
    // example: ["A"] instance of function(xs:string) as xs:string
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
      return mt.instanceOf(this) ? this : get(valueType.union(mt.valueType));
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
      final SeqType vt = valueType.intersect(mt.valueType);
      return vt == null ? null : get(vt);
    }
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ITR)) {
        final SeqType vt = valueType.intersect(ft.valueType);
        return vt == null ? null : get(vt);
      }
    }
    return null;
  }

  /**
   * Creates a new array type.
   * @param valueType value type
   * @return array type
   */
  public static ArrayType get(final SeqType valueType) {
    return valueType.eq(SeqType.ITEM_ZM) ? SeqType.ANY_ARRAY : new ArrayType(valueType);
  }

  @Override
  public String toString() {
    return valueType.eq(SeqType.ITEM_ZM) ? "array(*)" : "array(" + valueType + ')';
  }
}
