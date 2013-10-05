package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class parses options specified in function arguments.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FuncOptions {
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
  public FuncOptions(final QNm name, final InputInfo ii) {
    test = new NodeTest(name);
    root = name;
    info = ii;
  }

  /**
   * Parses the options in the specified item.
   * @param it item to be converted
   * @param options options
   * @throws QueryException query exception
   */
  public void parse(final Item it, final Options options) throws QueryException {
    parse(it, options, false);
  }

  /**
   * Adopts the parameters from the specified item.
   * @param item item to be parsed
   * @param options options
   * @param ignore ignore unknown parameters
   * @throws QueryException query exception
   */
  public void parse(final Item item, final Options options, final boolean ignore)
      throws QueryException {

    if(item == null) return;

    // XQuery map: convert to internal map
    if(item instanceof Map) {
      final Map map = (Map) item;
      final ValueIter vi = map.keys().iter();
      for(Item it; (it = vi.next()) != null;) {
        if(!(it instanceof AStr)) FUNCMP.thrw(info, map.description(), AtomType.STR, it.type);
        final Value v = map.get(it, info);
        if(!v.isItem()) FUNCMP.thrw(info, map.description(), AtomType.ITEM, v);
        final String key = Token.string(it.string(null));
        final String val = Token.string(((Item) v).string(info));
        if(options.set(key, val, true) == null && options.predefined())
          ELMOPTION.thrw(info, key);
      }
    } else {
      if(!test.eq(item)) ELMMAPTYPE.thrw(info, root, item.type);

      // interpret options
      final AxisIter ai = ((ANode) item).children();
      for(ANode n; (n = ai.next()) != null;) {
        if(n.type != NodeType.ELM) continue;
        final QNm qn = n.qname();
        if(!eq(qn.uri(), root.uri())) {
          if(ignore) continue;
          ELMOPTION.thrw(info, n);
        }
        // retrieve key from element name and value from "value" attribute or text node
        final String key = string(qn.local());
        byte[] val = n.attribute(VALUE);
        if(val == null) val = n.string();
        if(options.set(key, string(val), true) == null && options.predefined())
          ELMOPTION.thrw(info, key);
      }
    }
  }

  /**
   * Converts the specified output parameter item to serialization parameters.
   * @param it input item
   * @param info input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializer(final Item it, final InputInfo info)
      throws QueryException {

    final SerializerOptions sopts = new SerializerOptions();
    parse(it, sopts, info);
    return sopts;
  }

  /**
   * Converts the specified output parameter item to a map.
   * @param it input item
   * @param sopts serialization parameters
   * @param info input info
   * @throws QueryException query exception
   */
  public static void parse(final Item it, final SerializerOptions sopts, final InputInfo info)
      throws QueryException {
    new FuncOptions(Q_SPARAM, info).parse(it, sopts, true);
  }
}
