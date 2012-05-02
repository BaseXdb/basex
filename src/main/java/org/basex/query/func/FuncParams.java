package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.item.map.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class parses function parameters for the specified argument.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FuncParams {
  /** Attribute: value. */
  private static final QNm A_VALUE = new QNm("value");
  /** Root element. */
  private final QNm root;
  /** Node test. */
  private final ExtTest test;
  /** Calling expression (may be {@code null}). */
  private final ParseExpr expr;

  /**
   * Constructor.
   * @param name name of root node
   * @param e calling expression
   */
  public FuncParams(final QNm name, final ParseExpr e) {
    test = new ExtTest(NodeType.ELM, name);
    root = name;
    expr = e;
  }

  /**
   * Converts the parameters of the argument to a token map.
   * @param it item to be converted
   * @return map
   * @throws QueryException query exception
   */
  public TokenMap parse(final Item it) throws QueryException {
    // XQuery map: convert to internal map
    final InputInfo info = expr == null ? null : expr.info;
    if(it instanceof Map) return ((Map) it).tokenJavaMap(info);

    // initialize token map
    final TokenMap tm = new TokenMap();
    if(it == null) return tm;
    if(!it.type.isNode()) NODFUNTYPE.thrw(info, it, it.type);

    ANode n = (ANode) it;
    if(!test.eq(n)) GENERR.thrw(info, n);

    // interpret query parameters
    final AxisIter ai = n.children();
    while((n = ai.next()) != null) {
      final QNm qn = n.qname();
      if(!eq(qn.uri(), root.uri())) GENERR.thrw(info, n);
      final byte[] val = n.attribute(A_VALUE);
      tm.add(qn.local(), val == null ? n.string() : val);
    }
    return tm;
  }
}
