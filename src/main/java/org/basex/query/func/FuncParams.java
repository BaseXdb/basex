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

    if(it.type != NodeType.ELM || !test.eq((ANode) it))
      ELMMAPTYPE.thrw(info, it, root, it.type);

    // interpret query parameters
    final AxisIter ai = ((ANode) it).children();
    for(ANode n; (n = ai.next()) != null;) {
      if(n.type != NodeType.ELM) continue;
      final QNm qn = n.qname();
      if(!eq(qn.uri(), root.uri())) GENERR.thrw(info, n);
      // retrieve key from element name and value from "value" attribute or text node
      final byte[] key = qn.local();
      byte[] val = n.attribute(A_VALUE);
      if(val == null) val = n.string();
      // separate multiple entries with ","
      final byte[] o = tm.get(key);
      if(o != null) val = new TokenBuilder(o).add(',').add(val).finish();
      tm.add(key, val);
    }
    return tm;
  }
}
