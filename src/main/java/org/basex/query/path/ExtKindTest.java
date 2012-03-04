package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Extended kind test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ExtKindTest extends Test {
  /** Type name. */
  private final Type ext;
  /** Strip flag (would be more relevant if XMLSchema was supported). */
  private boolean strip;

  /**
   * Constructor.
   * @param t node type
   * @param nm optional node name
   * @param et extended node type
   * @param qc query context
   */
  public ExtKindTest(final NodeType t, final QNm nm, final Type et,
      final QueryContext qc) {
    type = t;
    name = nm;
    ext = et;
    strip = qc.sc.strip;
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
}
