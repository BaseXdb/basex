package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FuncOptions {
  /** QName. */
  public static final QNm Q_SPARAM = QNm.get("serialization-parameters", OUTPUT_URI);
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
   * @param root name of root node
   * @param info input info
   */
  FuncOptions(final QNm root, final InputInfo info) {
    test = new NodeTest(root);
    this.root = root;
    this.info = info;
  }

  /**
   * Extracts options from the specified item.
   * @param it item to be converted
   * @param options options
   * @throws QueryException query exception
   */
  void parse(final Item it, final Options options) throws QueryException {
    parse(it, options, INVALIDOPT_X);
  }

  /**
   * Extracts options from the specified item.
   * @param item item to be parsed
   * @param options options
   * @param error raise error if parameter is unknown
   * @throws QueryException query exception
   */
  private void parse(final Item item, final Options options, final QueryError error)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    if(item != null) {
      try {
        if(!(item instanceof Map || test.eq(item)))
          throw ELMMAP_X_X_X.get(info, root.prefixId(XML), item.type, item);
        options.parse(tb.add(optString(item)).toString());
      } catch(final BaseXException ex) {
        throw error.get(info, ex);
      }
    }
  }

  /**
   * Builds a string representation of the specified value. The specified value may be
   * another map or an atomic value that can be converted to a string.
   * @param item item
   * @return string
   * @throws QueryException query exception
   */
  private String optString(final Item item) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    if(item instanceof Map) {
      final Map map = (Map) item;
      for(final Item it : map.keys()) {
        if(!(it instanceof AStr)) throw EXPTYPE_X_X_X.get(info, AtomType.STR, it.type, it);
        tb.add(it.string(info)).add('=');
        final Value val = map.get(it, info);
        if(!(val instanceof Item)) throw EXPTYPE_X_X_X.get(info, AtomType.ITEM, val.seqType(), val);
        tb.add(optString((Item) val).replace(",", ",,")).add(',');
      }
    } else if(item.type == NodeType.ELM) {
      // interpret options
      for(final ANode node : ((ANode) item).children()) {
        if(node.type != NodeType.ELM) continue;
        // ignore elements in other namespace
        final QNm qn = node.qname();
        if(!eq(qn.uri(), root.uri())) continue;
        // retrieve key from element name and value from "value" attribute or text node
        byte[] v;
        if(hasElements(node)) {
          v = token(optString(node));
        } else {
          v = node.attribute(VALUE);
          if(v == null) v = node.string();
        }
        tb.add(string(qn.local())).add('=').add(optString(Str.get(v)).replace(",", ",,")).add(',');
      }
    } else {
      tb.add(item.string(info));
    }
    return tb.toString();
  }

  /**
   * Checks if the specified node has elements as children.
   * @param node node
   * @return result of check
   */
  private static boolean hasElements(final ANode node) {
    for(final ANode n : node.children()) {
      if(n.type == NodeType.ELM) return true;
    }
    return false;
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
    final SerializerOptions so = new SerializerOptions();
    so.set(SerializerOptions.METHOD, SerialMethod.XML);
    return serializer(it, so, info);
  }

  /**
   * Converts the specified output parameter item to serialization parameters.
   * @param it input item
   * @param sopts serialization parameters
   * @param info input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializer(final Item it, final SerializerOptions sopts,
      final InputInfo info) throws QueryException {
    new FuncOptions(Q_SPARAM, info).parse(it, sopts, SEROPT_X);
    return sopts;
  }
}
