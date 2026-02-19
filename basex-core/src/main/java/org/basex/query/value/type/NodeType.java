package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XDM node types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NodeType implements Type {
  /** Cached types. */
  static final EnumMap<Kind, NodeType> TYPES = new EnumMap<>(Kind.class);

  static {
    for(final Kind kind : Kind.values()) TYPES.put(kind, new NodeType(kind, null));
  }

  /** Node type: node. */
  public static final NodeType NODE = TYPES.get(Kind.NODE);
  /** Node type: text. */
  public static final NodeType TEXT = TYPES.get(Kind.TEXT);
  /** Node type: processing instruction. */
  public static final NodeType PROCESSING_INSTRUCTION = TYPES.get(Kind.PROCESSING_INSTRUCTION);
  /** Node type: element. */
  public static final NodeType ELEMENT = TYPES.get(Kind.ELEMENT);
  /** Node type: document. */
  public static final NodeType DOCUMENT = TYPES.get(Kind.DOCUMENT);
  /** Node type: attribute. */
  public static final NodeType ATTRIBUTE = TYPES.get(Kind.ATTRIBUTE);
  /** Node type: comment. */
  public static final NodeType COMMENT = TYPES.get(Kind.COMMENT);
  /** Node type: namespace. */
  public static final NodeType NAMESPACE = TYPES.get(Kind.NAMESPACE);

  /** Node kind. */
  private final Kind kind;
  /** Node test. */
  public final Test test;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param kind node kind
   * @param test node test (can be {@code null})
   */
  private NodeType(final Kind kind, final Test test) {
    this.kind = kind;
    this.test = test;
  }

  /**
   * Returns an instance for the specified kind.
   * @param kind node kind
   * @return type
   */
  public static NodeType get(final Kind kind) {
    return TYPES.get(kind);
  }

  /**
   * Returns an instance for the specified test.
   * @param test node test
   * @return type
   */
  public static NodeType get(final Test test) {
    final Kind kind = test.kind;
    return test instanceof NodeTest ? get(kind) : new NodeType(kind, test);
  }

  @Override
  public Kind kind() {
    return kind;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return !kind.oneOf(Kind.PROCESSING_INSTRUCTION, Kind.COMMENT, Kind.NODE);
  }

  @Override
  public boolean isNumberOrUntyped() {
    return isUntyped();
  }

  @Override
  public boolean isStringOrUntyped() {
    return true;
  }

  @Override
  public boolean isSortable() {
    return true;
  }

  @Override
  public XNode cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item.type == this) return (XNode) item;
    throw typeError(item, this, info);
  }

  @Override
  public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return kind.cast(value, info);
  }

  @Override
  public XNode read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    return cast(in.readToken(), qc, null);
  }

  @Override
  public SeqType seqType(final Occ occ) {
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public boolean eq(final Type type) {
    if(this == type) return true;
    if(type instanceof final NodeType nt) {
      if(nt.kind != kind) return false;
      if(test == null ? nt.test == null : test.equals(nt.test)) return true;
    }
    return false;
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(type == this || type == BasicType.ITEM) return true;
    if(type instanceof final NodeType nt) {
      if(nt.kind == Kind.NODE) return true;
      if(nt.kind != kind) return false;
      if(nt.test == null) return true;
      return test != null && test.instanceOf(nt.test);
    }
    if(type instanceof final ChoiceItemType cit) {
      return cit.hasInstance(this);
    }
    return false;
  }

  @Override
  public Type union(final Type type) {
    if(type instanceof final ChoiceItemType cit) return cit.union(this);
    if(type.instanceOf(this)) return this;
    if(instanceOf(type)) return type;
    if(type instanceof final NodeType nt) {
      // at this stage, both test and nt.test are present
      if(kind == nt.kind) return get(Test.get(Arrays.asList(test, nt.test)));
      return NODE;
    }
    return BasicType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof final ChoiceItemType cit) return cit.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;
    if(type instanceof final NodeType nt && kind == nt.kind) {
      // at this stage, both test and nt.test are present
      final Test t = test.intersect(nt.test);
      if(t != null) return get(t);
    }
    return null;
  }

  @Override
  public BasicType atomic() {
    return kind == Kind.NODE ? BasicType.ANY_ATOMIC_TYPE :
      kind == Kind.PROCESSING_INSTRUCTION || kind == Kind.COMMENT ? BasicType.STRING :
      BasicType.UNTYPED_ATOMIC;
  }

  @Override
  public ID id() {
    return kind.id;
  }

  @Override
  public boolean refinable() {
    return kind == Kind.NODE;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public Object name() {
    return kind;
  }

  @Override
  public String toString() {
    return test != null ? test.toString() : kind.toString("");
  }
}
