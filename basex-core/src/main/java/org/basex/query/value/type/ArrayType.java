package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XDM array types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayType extends FType {
  /** Type of the array members. */
  private final SeqType memberType;

  /**
   * Constructor.
   * @param memberType member type
   */
  ArrayType(final SeqType memberType) {
    this.memberType = memberType;
  }

  /**
   * Returns the array type with the specified member type.
   * @param memberType member type
   * @return array type
   */
  public static ArrayType get(final SeqType memberType) {
    return memberType.arrayType();
  }

  /**
   * Returns the member type.
   * @return member type
   */
  public SeqType memberType() {
    return memberType;
  }

  @Override
  public Coercion coercion() {
    return memberType.type.coercion();
  }

  @Override
  public boolean attachable() {
    return memberType.type.attachable();
  }

  @Override
  public boolean global() {
    return memberType.type.global();
  }

  @Override
  public XQArray cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item instanceof final XQArray array && array.instanceOf(this, false)) return array;
    throw typeError(item, this, info);
  }

  @Override
  public XQArray read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException {
    int size = in.readNum();
    final ArrayBuilder ab = new ArrayBuilder(qc, size);
    while(--size >= 0) ab.add(Stores.read(in, qc));
    return ab.array(this);
  }

  @Override
  public boolean eq(final Type type) {
    return this == type || type instanceof final ArrayType at && memberType.eq(at.memberType);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(this == type || type.oneOf(ARRAY, FUNCTION, BasicType.ITEM)) return true;
    if(type instanceof final ArrayType at) {
      return this != ARRAY && memberType.instanceOf(at.memberType);
    }
    if(type instanceof final FuncType ft) {
      return memberType.instanceOf(ft.declType) && ft.unary(INTEGER_O);
    }
    return type instanceof final ChoiceItemType cit && cit.hasInstance(this);
  }

  @Override
  public Type union(final Type type) {
    return type instanceof ChoiceItemType ? type.union(this) :
      type.keptExact(this, true) ? this :
      keptExact(type, true) ? type :
      type instanceof final ArrayType at ? union(at.memberType) :
      type instanceof MapType ? FUNCTION :
      type instanceof FuncType ? type.union(this) : BasicType.ITEM;
  }

  /**
   * Returns the union of this type and the array type with the specified member type.
   * @param mt member type of the other array type
   * @return array type
   */
  public ArrayType union(final SeqType mt) {
    return memberType.eq(mt) ? this : get(memberType.union(mt));
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof final ArrayType at) {
      // disjoint member types: the empty array is still an instance of both types
      final SeqType mt = memberType.intersect(at.memberType);
      return get(mt != null ? mt : ERROR_O);
    }
    return type instanceof final FuncType ft && ft.unary(INTEGER_O) ? this : null;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(memberType, INTEGER_O);
  }

  @Override
  public BasicType atomic() {
    return memberType.type.atomic();
  }

  @Override
  public ID id() {
    return ID.ARRAY;
  }

  @Override
  public String name() {
    return QueryText.ARRAY;
  }

  @Override
  public String toString() {
    return toString(", ", this == ARRAY ? WILDCARD : new Object[] { memberType });
  }
}
