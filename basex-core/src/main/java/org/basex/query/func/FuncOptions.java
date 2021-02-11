package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class parses options specified in function arguments.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FuncOptions {
  /** QName. */
  public static final QNm Q_SPARAM = new QNm(OUTPUT_PREFIX, SERIALIZATION_PARAMETERS, OUTPUT_URI);
  /** Value. */
  private static final byte[] VALUE = token("value");

  /** Root element. */
  private final QNm root;
  /** Root node test (can be {@code null}). */
  private final NameTest test;
  /** Input info. */
  private final InputInfo info;

  /** Accept unknown options. */
  private boolean acceptUnknown;

  /**
   * Constructor.
   * @param info input info
   */
  public FuncOptions(final InputInfo info) {
    this(null, info);
  }

  /**
   * Constructor.
   * @param root name of root node (can be {@code null})
   * @param info input info
   */
  public FuncOptions(final QNm root, final InputInfo info) {
    test = root == null ? null : new NameTest(root);
    this.root = root;
    this.info = info;
  }

  /**
   * Accept unknown options.
   * @return self reference
   */
  public FuncOptions acceptUnknown() {
    acceptUnknown = true;
    return this;
  }

  /**
   * Assigns values to the specified options.
   * @param item item to be converted (can be {@link Empty#VALUE})
   * @param options options
   * @param <T> option type
   * @return specified options
   * @throws QueryException query exception
   */
  public <T extends Options> T assign(final Item item, final T options) throws QueryException {
    return assign(item, options, INVALIDOPT_X);
  }

  /**
   * Assigns values to the specified options.
   * @param item item to be parsed (can be {@link Empty#VALUE})
   * @param options options
   * @param <T> option type
   * @param error error to be raised
   * @return specified options
   * @throws QueryException query exception
   */
  private <T extends Options> T assign(final Item item, final T options, final QueryError error)
      throws QueryException {

    if(item != Empty.VALUE) {
      try {
        if(item instanceof XQMap) {
          options.assign((XQMap) item, !acceptUnknown, info);
        } else {
          if(test == null) throw MAP_X_X.get(info, item.type, item);
          if(!test.matches(item)) throw ELMMAP_X_X_X.get(info, root.prefixId(XML), item.type, item);
          options.assign(toString((ANode) item, error));
        }
      } catch(final BaseXException ex) {
        throw error.get(info, ex);
      }
    }
    return options;
  }

  /**
   * Builds a string representation of the specified node.
   * @param node node
   * @param error error to be raised
   * @return string
   * @throws QueryException query exception
   */
  private String toString(final ANode node, final QueryError error) throws QueryException {
    final ANode n = node.attributeIter().next();
    if(n != null) throw error.get(info, Util.info("Invalid attribute: '%'", n.name()));

    final TokenBuilder tb = new TokenBuilder();
    // interpret options
    for(final ANode child : node.childIter()) {
      if(child.type != NodeType.ELEMENT) continue;

      // ignore elements in other namespace
      final QNm qname = child.qname();
      if(!eq(qname.uri(), root.uri())) {
        if(qname.uri().length == 0)
          throw error.get(info, Util.info("Element has no namespace: '%'", qname));
        continue;
      }
      // retrieve key from element name and value from "value" attribute or text node
      final String name = string(qname.local());
      String value = null;

      if(name.equals(SerializerOptions.USE_CHARACTER_MAPS.name())) {
        value = SerializerOptions.characterMap(child);
        if(value == null) throw error.get(info, "Character map is invalid.");
      } else if(hasElements(child)) {
        value = toString(child, error);
      } else {
        for(final ANode attr : child.attributeIter()) {
          if(eq(attr.name(), VALUE)) {
            value = string(attr.string());
            if(name.equals(SerializerOptions.CDATA_SECTION_ELEMENTS.name())) {
              value = cDataSectionElements(child, value);
            }
          } else {
            // Conflicts with QT3TS, Serialization-json-34 etc.
            throw error.get(info, Util.info("Invalid attribute: '%'", attr.name()));
          }
        }
        if(value == null) value = string(child.string());
      }
      tb.add(name).add('=').add(value.trim().replace(",", ",,")).add(',');
    }
    return tb.toString();
  }

  /**
   * Converts QName with prefixes to the EQName notation.
   * @param elem root element
   * @param value value
   * @return name with resolved QNames
   */
  private static String cDataSectionElements(final ANode elem, final String value) {
    if(!Strings.contains(value, ':')) return value;

    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] name : split(normalize(token(value)), ' ')) {
      final int i = indexOf(name, ':');
      if(i == -1) {
        tb.add(name);
      } else {
        final byte[] vl = elem.nsScope(null).value(substring(name, 0, i));
        if(vl != null) {
          tb.add(QNm.eqName(vl, substring(name, i + 1)));
        } else {
          tb.add(name);
        }
      }
      tb.add(' ');
    }
    return tb.toString();
  }

  /**
   * Checks if the specified node has elements as children.
   * @param node node
   * @return result of check
   */
  private static boolean hasElements(final ANode node) {
    for(final ANode nd : node.childIter()) {
      if(nd.type == NodeType.ELEMENT) return true;
    }
    return false;
  }

  /**
   * Converts the specified output parameter item to serialization parameters.
   * @param item input item
   * @param ii input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializer(final Item item, final InputInfo ii)
      throws QueryException {
    final SerializerOptions so = new SerializerOptions();
    so.set(SerializerOptions.METHOD, SerialMethod.XML);
    return serializer(item, so, ii);
  }

  /**
   * Converts the specified output parameter item to serializer options.
   * @param item input item
   * @param sopts serialization parameters
   * @param ii input info
   * @return serialization parameters
   * @throws QueryException query exception
   */
  public static SerializerOptions serializer(final Item item, final SerializerOptions sopts,
      final InputInfo ii) throws QueryException {
    return new FuncOptions(Q_SPARAM, ii).assign(item, sopts, SEROPT_X);
  }
}
