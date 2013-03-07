package org.basex.query.path;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Extended node test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ExtTest extends Test {
  /** Extended type. */
  private final Type ext;
  /** Strip flag (only relevant if specified type is {@code xs:untyped}). */
  private final boolean strip;

  /**
   * Constructor.
   * @param t node type
   * @param nm optional node name
   */
  public ExtTest(final NodeType t, final QNm nm) {
    this(t, nm, null, false);
  }

  /**
   * Constructor.
   * @param t node type
   * @param nm optional node name
   * @param et extended node type
   * @param st strip flag; only relevant if specified type is {@code xs:untyped}
   */
  public ExtTest(final NodeType t, final QNm nm, final Type et, final boolean st) {
    type = t;
    name = nm;
    ext = et;
    strip = st;
  }

  @Override
  public Test copy() {
    return new ExtTest(type, name, ext, strip);
  }

  @Override
  public boolean eq(final ANode node) {
    return node.type == type &&
      (name == null || node.qname(tmpq).eq(name)) &&
      (ext == null || ext == AtomType.ATY ||
      (node instanceof DBNode || strip) && ext == AtomType.UTY ||
      type == NodeType.ATT && (ext == AtomType.AST ||
      ext == AtomType.AAT || ext == AtomType.ATM));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(name == null) tb.add('*');
    else tb.add(name.string());
    if(ext != null) tb.add(',').addExt(ext);
    return tb.toString();
  }

  @Override
  public Test intersect(final Test other) {
    if(other instanceof ExtTest) {
      final ExtTest o = (ExtTest) other;
      if(type != null && o.type != null && type != o.type) return null;
      final NodeType nt = type != null ? type : o.type;
      if(name != null && o.name != null && !name.eq(o.name)) return null;
      final QNm n = name != null ? name : o.name;
      final boolean both = ext != null && o.ext != null;
      final Type e = ext == null ? o.ext : o.ext == null ? ext : ext.intersect(o.ext);
      return both && e == null ? null : new ExtTest(nt, n, e, strip || o.strip);
    } else if(other instanceof KindTest) {
      return type.instanceOf(other.type) ? this : null;
    } else if(other instanceof NameTest) {
      throw Util.notexpected(other);
    }

    return null;
  }
}
