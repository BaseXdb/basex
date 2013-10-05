package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class parses parameters specified in function arguments.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FuncParams {
  /** QName. */
  public static final QNm Q_SPARAM = QNm.get("serialization-parameters", OUTPUTURI);
  /** Value. */
  private static final String VALUE = "value";

  /** Root element. */
  private final QNm root;
  /** Root node test. */
  private final NodeTest test;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param name name of root node
   * @param ii input info
   */
  public FuncParams(final QNm name, final InputInfo ii) {
    test = new NodeTest(name);
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
    return parse(it, false);
  }

  /**
   * Converts the parameters of the argument to a token map.
   * @param it item to be converted
   * @param ignore ignore unknown parameters
   * @return map
   * @throws QueryException query exception
   */
  public TokenMap parse(final Item it, final boolean ignore) throws QueryException {
    // XQuery map: convert to internal map
    if(it instanceof Map) return ((Map) it).tokenMap(info);

    // initialize token map
    final TokenMap tm = new TokenMap();
    if(it == null) return tm;
    if(!test.eq(it)) ELMMAPTYPE.thrw(info, root, it.type);

    // interpret options
    final AxisIter ai = ((ANode) it).children();
    for(ANode n; (n = ai.next()) != null;) {
      if(n.type != NodeType.ELM) continue;
      final QNm qn = n.qname();
      if(!eq(qn.uri(), root.uri())) {
        if(ignore) continue;
        ELMOPTION.thrw(info, n);
      }
      // retrieve key from element name and value from "value" attribute or text node
      final byte[] key = qn.local();
      byte[] val = n.attribute(VALUE);
      if(val == null) val = n.string();
      // separate multiple entries with ","
      final byte[] o = tm.get(key);
      if(o != null) val = new TokenBuilder(o).add(',').add(val).finish();
      tm.put(key, val);
    }
    return tm;
  }

  /**
   * Converts the specified output parameter item to serialization parameters.
   * @param it input item
   * @param info input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializerProp(final Item it, final InputInfo info)
      throws QueryException {
    return serializerProp(serializerMap(it, info), info);
  }

  /**
   * Converts the specified output parameter item to a map.
   * @param it input item
   * @param info input info
   * @return serialization string
   * @throws QueryException query exception
   */
  public static TokenMap serializerMap(final Item it, final InputInfo info)
      throws QueryException {
    return new FuncParams(Q_SPARAM, info).parse(it, true);
  }

  /**
   * Converts the keys and values from the specified map to serialization parameters.
   * @param map map with serialization parameters
   * @param info input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializerProp(final TokenMap map, final InputInfo info)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : map) tb.add(key).add('=').add(map.get(key)).add(',');

    final SerializerOptions sp = new SerializerOptions(tb.toString());
    if(!sp.unknown.isEmpty()) SERWHICH.thrw(info, sp.unknown.get(0));
    final Object[] cm = SerializerOptions.S_USE_CHARACTER_MAPS;
    if(!sp.get(cm).isEmpty()) SERWHICH.thrw(info, AOptions.toString(cm));
    return sp;
  }
}
