package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Type for arrays.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FuncType {
  /**
   * Constructor.
   * @param rt return type
   */
  ArrayType(final SeqType rt) {
    super(new Ann(), new SeqType[] { SeqType.ITR }, rt);
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
      if(a.hasType(this)) return a;
    }
    throw castError(ii, item, this);
  }

  @Override
  public boolean eq(final Type t) {
    if(this == t) return true;
    if(!(t instanceof ArrayType)) return false;
    return retType.eq(((ArrayType) t).retType);
  }

  @Override
  public boolean instanceOf(final Type t) {
    return t instanceof ArrayType ? retType.instanceOf(((ArrayType) t).retType) :
      super.instanceOf(t);
  }

  @Override
  public Type union(final Type t) {
    if(instanceOf(t)) return t;
    if(t instanceof ArrayType) {
      final ArrayType mt = (ArrayType) t;
      return mt.instanceOf(this) ? this : get(retType.union(mt.retType));
    }
    return t instanceof MapType ? ANY_FUN : t instanceof FuncType ? t.union(this) : AtomType.ITEM;
  }

  @Override
  public ArrayType intersect(final Type t) {
    // case for item() and compatible FuncType, e.g. function(xs:anyAtomicType) as item()*
    // also excludes FuncType.ANY_FUN
    if(instanceOf(t)) return this;
    if(t instanceof ArrayType) {
      final ArrayType mt = (ArrayType) t;
      if(mt.instanceOf(this)) return mt;
      final SeqType rt = retType.intersect(mt.retType);
      return rt == null ? null : get(rt);
    }
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ITR)) {
        final SeqType rt = retType.intersect(ft.retType);
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
    return retType.eq(SeqType.ITEM_ZM) ? "array(*)" : "array(" + retType + ')';
  }
}
