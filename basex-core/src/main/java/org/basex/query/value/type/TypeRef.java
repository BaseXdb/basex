package org.basex.query.value.type;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Forward reference to a named item type.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TypeRef implements Type {
  /** Referenced type name. */
  private final QNm name;
  /** Input info ({@code null} once this reference has been resolved). */
  private InputInfo info;
  /** Referenced type (initially {@code item()}, may itself be a {@code TypeRef}). */
  private Type type = BasicType.ITEM;
  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param name referenced type name
   * @param info input info
   */
  public TypeRef(final QNm name, final InputInfo info) {
    this.name = name;
    this.info = info;
  }

  /**
   * Dereferences a type: returns the referenced type of a {@link TypeRef}, or the type itself.
   * @param type type
   * @return dereferenced type
   */
  public static Type deref(final Type type) {
    return type instanceof final TypeRef ref ? ref.deref() : type;
  }

  /**
   * Resolves this reference to the specified type (in place).
   * @param tp referenced type
   */
  public void resolve(final Type tp) {
    type = tp;
    info = null;
  }

  /**
   * Indicates if this reference has been resolved.
   * @return result of check
   */
  public boolean resolved() {
    return info == null;
  }

  /**
   * Returns the input info of an unresolved reference.
   * @return input info, or {@code null} if resolved
   */
  public InputInfo info() {
    return info;
  }

  /**
   * Returns the referenced type, following chained references.
   * @return referenced type
   */
  public Type deref() {
    Type tp = type;
    while(tp instanceof final TypeRef ref) tp = ref.type;
    return tp;
  }

  /**
   * Checks if this reference is part of a cycle of type references
   * (which would make {@link #deref()} loop indefinitely).
   * @return result of check
   */
  public boolean cyclic() {
    final Set<TypeRef> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    Type tp = this;
    while(tp instanceof final TypeRef ref) {
      if(!visited.add(ref)) return true;
      tp = ref.type;
    }
    return false;
  }

  @Override
  public Value cast(final Item item, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    return deref().cast(item, qc, ii);
  }

  @Override
  public Value cast(final Object value, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    return deref().cast(value, qc, ii);
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    return deref().read(in, qc);
  }

  @Override
  public SeqType seqType(final Occ occ) {
    // wrap this placeholder, so that in-place resolution propagates to all use sites
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public boolean eq(final Type tp) {
    return deref().eq(deref(tp));
  }

  @Override
  public boolean instanceOf(final Type tp) {
    return deref().instanceOf(deref(tp));
  }

  @Override
  public Type union(final Type tp) {
    return deref().union(deref(tp));
  }

  @Override
  public Type intersect(final Type tp) {
    return deref().intersect(deref(tp));
  }

  @Override
  public boolean isNumber() {
    return deref().isNumber();
  }

  @Override
  public boolean isUntyped() {
    return deref().isUntyped();
  }

  @Override
  public boolean isNumberOrUntyped() {
    return deref().isNumberOrUntyped();
  }

  @Override
  public boolean isStringOrUntyped() {
    return deref().isStringOrUntyped();
  }

  @Override
  public boolean isSortable() {
    return deref().isSortable();
  }

  @Override
  public BasicType atomic() {
    return deref().atomic();
  }

  @Override
  public ID id() {
    return deref().id();
  }

  @Override
  public boolean nsSensitive() {
    return deref().nsSensitive();
  }

  @Override
  public FuncType funcType() {
    return deref().funcType();
  }

  @Override
  public Kind kind() {
    return deref().kind();
  }

  @Override
  public QNm name() {
    return name;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Type tp && deref().equals(deref(tp));
  }

  @Override
  public int hashCode() {
    return deref().hashCode();
  }

  @Override
  public String toString() {
    return name != null ? Token.string(name.prefixString()) : deref().toString();
  }
}
