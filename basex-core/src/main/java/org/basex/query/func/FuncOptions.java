package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class parses options specified in function arguments.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * Extracts options from the specified item.
   * @param it item to be converted
   * @param options options
   * @throws QueryException query exception
   */
  public void parse(final Item it, final Options options) throws QueryException {
    parse(it, options, Err.INVALIDOPT);
  }

  /**
   * Extracts options from the specified item.
   * @param item item to be parsed
   * @param options options
   * @param error raise error if parameter is unknown
   * @throws QueryException query exception
   */
  public void parse(final Item item, final Options options, final Err error)
      throws QueryException {

    if(item == null) return;
    try {
      // XQuery map: convert to internal map
      if(item instanceof Map) {
        final Map map = (Map) item;
        for(final Item it : map.keys()) {
          if(!(it instanceof AStr)) FUNCMP.thrw(info, map.description(), AtomType.STR, it.type);
          final Value val = map.get(it, info);
          if(!val.isItem()) FUNCMP.thrw(info, map.description(), AtomType.ITEM, val);
          options.assign(string(it.string(null)), value(val));
        }
      } else {
        if(!test.eq(item)) ELMMAPTYPE.thrw(info, root, item.type);

        // interpret options
        final AxisIter ai = ((ANode) item).children();
        for(ANode n; (n = ai.next()) != null;) {
          if(n.type != NodeType.ELM) continue;
          final QNm qn = n.qname();
          final String name = string(qn.local());
          // ignore elements in other namespace
          if(eq(qn.uri(), root.uri())) {
            // retrieve key from element name and value from "value" attribute or text node
            final byte[] val = n.attribute(VALUE);
            options.assign(name, string(val == null ? n.string() : val));
          }
        }
      }
    } catch(final BaseXException ex) {
      error.thrw(info, ex);
    }
  }

  /**
   * Returns a string representation of the specified value. The specified value may be
   * another map or an atomic value that can be converted to a string.
   * @param value value
   * @return string representation
   * @throws QueryException query exception
   */
  private String value(final Value value) throws QueryException {
    if(value instanceof Map) {
      final Map m = (Map) value;
      final TokenBuilder tb = new TokenBuilder();
      for(final Item i : m.keys()) {
        if(!(i instanceof AStr)) FUNCMP.thrw(info, m.description(), AtomType.STR, i.type);
        final Value vl = m.get(i, info);
        if(!vl.isItem()) FUNCMP.thrw(info, m.description(), AtomType.ITEM, vl);
        final String s = string(((Item) vl).string(info));
        tb.add(i.string(info)).add('=').add(s.replace(",", ",,")).add(',');
      }
      return tb.toString();
    }
    return string(((Item) value).string(info));
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
    return parse(it, new SerializerOptions(), info);
  }

  /**
   * Converts the specified output parameter item to a map.
   * @param it input item
   * @param sopts serialization parameters
   * @param info input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions parse(final Item it, final SerializerOptions sopts,
      final InputInfo info) throws QueryException {
    new FuncOptions(Q_SPARAM, info).parse(it, sopts, Err.SEROPT);
    return sopts;
  }
}
