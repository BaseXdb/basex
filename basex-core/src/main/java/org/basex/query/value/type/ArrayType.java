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
 * @author BaseX Team 2005-19, BSD License
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
      final InputInfo ii) throws QueryException {

    if(item instanceof XQArray) {
      final XQArray a = (XQArray) item;
      if(a.instanceOf(this)) return a;
    }
    throw typeError(item, this, ii);
  }

  @Override
  public boolean eq(final Type type) {
    return this == type || type instanceof ArrayType && declType.eq(((ArrayType) type).declType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    return instanceOf(type, true);
  }

  @Override
  public Type union(final Type type) {
    if(instanceOf(type, false)) return type;

    if(type instanceof ArrayType) {
      final ArrayType at = (ArrayType) type;
      return at.instanceOf(this, false) ? this : get(declType.union(at.declType));
    }
    return type instanceof MapType  ? SeqType.ANY_FUNC :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(instanceOf(type, false)) return this;

    if(type instanceof ArrayType) {
      final ArrayType at = (ArrayType) type;
      if(at.instanceOf(this, false)) return at;
      final SeqType dt = declType.intersect(at.declType);
      if(dt != null) return get(dt);
    } else if(type instanceof FuncType) {
      final FuncType ft = (FuncType) type;
      if(ft.argTypes.length == 1 && ft.argTypes[0].instanceOf(SeqType.ITR_O)) {
        final SeqType dt = declType.intersect(ft.declType);
        if(dt != null) return get(dt);
      }
    }
    return null;
  }

  /**
   * Instance test.
   * @param type type to be compared
   * @param generic check against generic array type
   * @return result of check
   */
  private boolean instanceOf(final Type type, final boolean generic) {
    return type == AtomType.ITEM || type == SeqType.ANY_FUNC || type == SeqType.ANY_ARRAY ||
        type instanceof FuncType && !(type instanceof MapType) &&
        instanceOf((FuncType) type, generic ? SeqType.ANY_ARRAY : this);
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
