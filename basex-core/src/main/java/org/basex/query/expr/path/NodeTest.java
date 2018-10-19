package org.basex.query.expr.path;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Extended node test.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class NodeTest extends Test {
  /** Extended type (can be {@code null}). */
  private final Type ext;

  /**
   * Convenience constructor for element tests.
   * @param name node name
   */
  public NodeTest(final QNm name) {
    this(NodeType.ELM, name);
  }

  /**
   * Constructor.
   * @param type node type (element, attribute, processing instruction)
   * @param name node name
   */
  public NodeTest(final NodeType type, final QNm name) {
    this(type, name, null);
  }

  /**
   * Constructor.
   * @param type node type (element, attribute, processing instruction)
   * @param name node name (can be {@code null} if extended node type is specified)
   * @param ext extended node type (can be {@code null})
   */
  public NodeTest(final NodeType type, final QNm name, final Type ext) {
    super(type);
    this.name = name;
    this.ext = ext;
  }

  @Override
  public Test copy() {
    return new NodeTest(type, name, ext);
  }

  @Override
  public boolean eq(final ANode node) {
    return type == node.type &&
      (name == null || node.qname().eq(name)) &&
      (ext == null || ext == AtomType.ATY || ext == AtomType.UTY ||
      type == NodeType.ATT && (ext == AtomType.AST || ext == AtomType.AAT || ext == AtomType.ATM));
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof NodeTest)) return false;
    final NodeTest nt = (NodeTest) obj;
    return type == nt.type &&
        (name == null ? nt.name == null : (nt.name != null && name.eq(nt.name))) &&
        (ext == null ? nt.ext == null : (nt.ext != null && ext.eq(nt.ext)));
  }

  @Override
  public NodeTest intersect(final Test other) {
    if(other instanceof NodeTest) {
      final NodeTest o = (NodeTest) other;
      if(type != null && o.type != null && type != o.type) return null;
      final NodeType nt = type != null ? type : o.type;
      if(name != null && o.name != null && !name.eq(o.name)) return null;
      final QNm n = name != null ? name : o.name;
      final boolean both = ext != null && o.ext != null;
      final Type e = ext == null ? o.ext : o.ext == null ? ext : ext.intersect(o.ext);
      return both && e == null ? null : new NodeTest(nt, n, e);
    }
    if(other instanceof KindTest) {
      return type.instanceOf(other.type) ? this : null;
    }
    if(other instanceof NameTest) {
      throw Util.notExpected(other);
    }
    return null;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(type.string()).add('(');
    if(name == null) tb.add('*');
    else tb.add(name.string());
    if(ext != null) tb.add(',').add(ext);
    return tb.add(')').toString();
  }
}
