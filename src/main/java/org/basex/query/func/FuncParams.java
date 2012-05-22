package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.item.map.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class parses parameters specified in function arguments.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FuncParams {
  /** Element: output:serialization-parameters. */
  public static final QNm Q_SPARAM = new QNm("serialization-parameters",
      QueryText.OUTPUTURI);
  /** Attribute: value. */
  private static final QNm A_VALUE = new QNm("value");

  /** Root element. */
  private final QNm root;
  /** Root node test. */
  private final ExtTest test;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param name name of root node
   * @param ii input info
   */
  public FuncParams(final QNm name, final InputInfo ii) {
    test = new ExtTest(NodeType.ELM, name);
    root = name;
    info = ii;
  }

  /**
   * Converts the parameters of the argument to a token map.
   * @param it item to be converted
   * @return map
   * @throws QueryException query exception
   */
  public TokenMap parse(final Item it) throws QueryException {
    // XQuery map: convert to internal map
    if(it instanceof Map) return ((Map) it).tokenJavaMap(info);

    // initialize token map
    final TokenMap tm = new TokenMap();
    if(it == null) return tm;
    if(it.type != NodeType.ELM || !test.eq((ANode) it)) ELMMAPTYPE.thrw(info, root, it);

    // interpret options
    final AxisIter ai = ((ANode) it).children();
    for(ANode n; (n = ai.next()) != null;) {
      if(n.type != NodeType.ELM) continue;
      final QNm qn = n.qname();
      if(!eq(qn.uri(), root.uri())) ELMOPTION.thrw(info, n);
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

  /**
   * Converts the specified parameters to serialization properties.
   * @param it input item
   * @return serialization string
   * @throws QueryException query exception
   */
  public static SerializerProp serializerProp(final Item it) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    if(it != null) {
      final TokenMap map = new FuncParams(Q_SPARAM, null).parse(it);
      for(final byte[] key : map) tb.add(key).add('=').add(map.get(key)).add(',');
    }
    return new SerializerProp(tb.toString());
  }
}
