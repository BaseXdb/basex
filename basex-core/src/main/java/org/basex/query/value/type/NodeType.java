package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XDM node types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class NodeType implements Type {
  /** Cached types. */
  static final EnumMap<Kind, NodeType> TYPES = new EnumMap<>(Kind.class);

  static {
    for(final Kind kind : Kind.values()) TYPES.put(kind, new NodeType(kind));
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
  public Kind kind;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param kind node kind
   */
  public NodeType(final Kind kind) {
    this.kind = kind;
  }

  /**
   * Returns an instance for the specified kind.
   * @param kind node kind
   * @return type
   */
  public static NodeType get(final Kind kind) {
    return TYPES.get(kind);
  }

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return !kind.oneOf(Kind.PROCESSING_INSTRUCTION, Kind.COMMENT, Kind.NODE);
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return isUntyped();
  }

  @Override
  public boolean isStringOrUntyped() {
    return true;
  }

  @Override
  public final boolean isSortable() {
    return true;
  }

  @Override
  public final XNode cast(final Item item, final QueryContext qc, final InputInfo info)
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
  public final SeqType seqType(final Occ occ) {
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  /**
   * Returns a node type description.
   * @return kind
   */
  public final String description() {
    return Token.string(kind.name).replace("-node", "");
  }

  /**
   * Returns the name of the node type.
   * @return type
   */
  public final byte[] test() {
    return kind.name;
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    if(type == this || type == BasicType.ITEM) return true;
    if(type instanceof final NodeType nt) {
      if(nt.kind == Kind.NODE) return true;
      if(nt.kind != kind) return false;
      return true;
    }
    if(type instanceof final ChoiceItemType cit) {
      return cit.hasInstance(this);
    }
    return false;
  }

  @Override
  public final Type union(final Type type) {
    if(type instanceof final ChoiceItemType cit) return cit.union(this);
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;
    if(type instanceof NodeType) return NODE;
    return BasicType.ITEM;
  }

  @Override
  public final Type intersect(final Type type) {
    if(type instanceof final ChoiceItemType cit) return cit.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;
    return null;
  }

  @Override
  public final BasicType atomic() {
    return kind == Kind.NODE ? BasicType.ANY_ATOMIC_TYPE :
      kind == Kind.PROCESSING_INSTRUCTION || kind == Kind.COMMENT ? BasicType.STRING :
      BasicType.UNTYPED_ATOMIC;
  }

  @Override
  public final ID id() {
    return kind.id;
  }

  @Override
  public final boolean refinable() {
    return kind == Kind.NODE;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  /**
   * Returns a string representation with the specified argument.
   * @param arg argument
   * @return string representation
   */
  public final String toString(final String arg) {
    return new TokenBuilder().add(kind.name).add('(').add(arg).add(')').toString();
  }

  @Override
  public final String toString() {
    return toString("");
  }
}
