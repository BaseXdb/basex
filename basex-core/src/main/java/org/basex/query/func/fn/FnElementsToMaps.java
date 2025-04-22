package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnElementsToMaps extends StandardFunc {
  /** Name format. */
  private enum NameFormat {
    /** fn:name.       */ LEXICAL,
    /** fn:local-name. */ LOCAL,
    /** Q{uri}local.   */ EQNAME,
    /** Default.       */ DEFAULT;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  /** Options. */
  public static class ElementsOptions extends Options {
    /** Option. */
    public static final BooleanOption UNIFORM = new BooleanOption("uniform", false);
    /** Option. */
    public static final StringOption ATTRIBUTE_MARKER = new StringOption("attribute-marker", "@");
    /** Option. */
    public static final EnumOption<NameFormat> NAME_FORMAT =
        new EnumOption<>("name-format", NameFormat.DEFAULT);
    /** Option. */
    public static final OptionsOption<Options> LAYOUTS =
        new OptionsOption<>("layouts", new Options());
  }

  /** Formatting hints. */
  private static final class Format {
    /** Name format. */
    private NameFormat name;
    /** Attribute marker. */
    private String marker;
    /** Layouts. */
    private QNmMap<Layout> layouts;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANodeList elements = new ANodeList();
    final Iter iter = arg(0).iter(qc);
    for(Item item; (item = iter.next()) != null;) elements.add(toElem(item, qc));
    final ElementsOptions options = toOptions(arg(1), new ElementsOptions(), qc);

    final Format format = new Format();
    format.name = options.get(ElementsOptions.NAME_FORMAT);
    format.marker = options.get(ElementsOptions.ATTRIBUTE_MARKER);
    format.layouts = new QNmMap<>();

    final QNmMap<Layout> layouts = format.layouts;
    for(final Map.Entry<String, String> entry : options.get(ElementsOptions.LAYOUTS).
        free().entrySet()) {
      final QNm name = QNm.parse(Token.token(entry.getKey()), sc());
      final Layout layout = EnumOption.get(Layout.class, entry.getValue());
      if(layout == null) {
        throw INVALIDOPTION_X.get(info, "Unknown layout: " + entry.getValue() + '.');
      }
      layouts.put(name, layout);
    }

    if(options.get(ElementsOptions.UNIFORM)) {
      // collect distinct element names
      final QNmMap<ANodeList> names = new QNmMap<>();
      for(final ANode element : elements) {
        for(final ANode desc : element.descendantIter(true)) {
          if(desc.type != NodeType.ELEMENT) continue;
          final QNm name = desc.qname();
          if(layouts.contains(name)) continue;
          ANodeList descs = names.get(name);
          if(descs == null) {
            descs = new ANodeList();
            names.put(name, descs);
          }
          descs.add(desc.finish());
        }
      }
      // assign layouts
      for(final QNm name : names) {
        if(!layouts.contains(name)) layouts.put(name, layout(names.get(name).finish()));
      }
    }

    // create result
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ANode element : elements) {
      final Value value = layout(element, format).apply(element, null, format);
      vb.add(new MapBuilder().put(nodeName(element, null, format), value).map());
    }
    return vb.value(this);
  }

  /**
   * Returns a matching layout for the specified node.
   * @param node node
   * @param format formatting hints
   * @return layout
   */
  private static Layout layout(final ANode node, final Format format) {
    final Layout layout = format.layouts.get(node.qname());
    return layout != null ? layout : layout(node);
  }

  /**
   * Returns a matching layout for the specified nodes.
   * @param nodes nodes
   * @return layout
   */
  private static Layout layout(final ANode... nodes) {
    final ANodeList attributes = children(NodeType.ATTRIBUTE, nodes);
    final ANodeList elements = children(NodeType.ELEMENT, nodes);
    final ANodeList texts = children(NodeType.TEXT, nodes);
    final Layout l;
    if(elements.isEmpty() && texts.isEmpty()) {
      l = attributes.isEmpty() ? Layout.EMPTY : Layout.EMPTY_PLUS;
    } else if(elements.isEmpty()) {
      l = attributes.isEmpty() ? Layout.SIMPLE : Layout.SIMPLE_PLUS;
    } else if(((Checks<ANode>) node -> empty(texts)).all(nodes)) {
      if(equalNames(elements) && ((Checks<ANode>) node ->
          children(NodeType.ELEMENT, nodes).size() > 1).any(nodes)) {
        l = attributes.isEmpty() ? Layout.LIST : Layout.LIST_PLUS;
      } else if(((Checks<ANode>) node -> differentNames(elements)).all(nodes)) {
        l = Layout.RECORD;
      } else {
        l = Layout.SEQUENCE;
      }
    } else {
      l = Layout.MIXED;
    }
    return l;
  }

  /**
   * Checks if the string values of all nodes are empty.
   * @param nodes node list
   * @return result of check
   */
  private static boolean empty(final ANodeList nodes) {
    return ((Checks<ANode>) node -> Token.normalize(node.string()).length == 0).all(nodes);
  }

  /**
   * Returns the children of the specified type.
   * @param type type to be found
   * @param nodes nodes
   * @return result of check
   */
  private static ANodeList children(final NodeType type, final ANode... nodes) {
    final ANodeList list = new ANodeList();
    for(final ANode node : nodes) {
      if(type == NodeType.ATTRIBUTE) {
        for(final ANode child : node.attributeIter()) {
          if(!Token.eq(child.qname().uri(), QueryText.XSI_URI)) list.add(child.finish());
        }
      } else {
        for(final ANode child : node.childIter()) {
          if(child.type == type) list.add(child.finish());
        }
      }
    }
    return list;
  }

  /**
   * Checks if the element names are distinct or different.
   * @param nodes node list
   * @return result of check
   */
  private static boolean differentNames(final ANodeList nodes) {
    final QNmSet names = new QNmSet();
    for(final ANode node : nodes) {
      if(node.type == NodeType.ELEMENT && !names.add(node.qname())) return false;
    }
    return !names.isEmpty();
  }

  /**
   * Checks if the element names are distinct or different.
   * @param nodes node list
   * @return result of check
   */
  private static boolean equalNames(final ANodeList nodes) {
    QNm name = null;
    for(final ANode node : nodes) {
      if(node.type == NodeType.ELEMENT) {
        if(name == null) name = node.qname();
        else if(!name.eq(node.qname())) return false;
      }
    }
    return true;
  }

  /**
   * Returns an attribute map.
   * @param node node
   * @param format name format
   * @return attributes
   * @throws QueryException query exception
   */
  private static MapBuilder attributes(final ANode node, final Format format)
      throws QueryException {
    final MapBuilder mb = new MapBuilder();
    for(final ANode attr : children(NodeType.ATTRIBUTE, node)) {
      mb.put(nodeName(attr, node, format), attr.string());
    }
    return mb;
  }

  /**
   * Returns the node name.
   * @param node node
   * @param parent parent (can be {@code null})
   * @param format name format
   * @return name
   */
  private static byte[] nodeName(final ANode node, final ANode parent, final Format format) {
    final QNm qnm = node.qname();
    final byte[] name;
    switch(format.name) {
      case EQNAME:
        name = qnm.uri().length != 0 ? qnm.eqName() : qnm.local(); break;
      case LEXICAL:
        name = qnm.string(); break;
      case LOCAL:
        name = qnm.local(); break;
      default:
        if(node.type == NodeType.ELEMENT ? parent == null ? qnm.uri().length == 0 :
            Token.eq(parent.qname().uri(), qnm.uri()) : qnm.uri().length == 0) {
          name = qnm.local();
        } else {
          name = Token.eq(qnm.uri(), QueryText.XML_URI) ? qnm.string() : qnm.eqName();
        }
        break;
    }
    return node.type == NodeType.ATTRIBUTE && format.marker != null ?
      Token.concat(format.marker, name) : name;
  }

  /**
   * Returns a mixed-layout item.
   * @param node node
   * @param parent parent (can be {@code null}
   * @param format formatting hints
   * @param ignoreEmpty ignore empty text nodes
   * @return array
   * @throws QueryException query exception
   */
  private static XQArray mixed(final ANode node, final ANode parent, final Format format,
      final boolean ignoreEmpty) throws QueryException {

    final ArrayBuilder ab = new ArrayBuilder();
    for(final ANode attr : children(NodeType.ATTRIBUTE, node)) {
      ab.add(new MapBuilder().put(nodeName(attr, node, format), attr.string()).map());
    }
    for(final ANode child : node.childIter()) {
      Item item = null;
      switch((NodeType) child.type) {
        case COMMENT:
          item = new MapBuilder().put("#comment", child.string()).map();
          break;
        case ELEMENT:
          item = new MapBuilder().put(nodeName(child, parent, format),
              layout(child, format).apply(child, node, format)).map();
          break;
        case PROCESSING_INSTRUCTION:
          item = new MapBuilder().put("#processing-instruction", child.name()).
              put("#data", child.string()).map();
          break;
        case TEXT:
          final byte[] text = child.string();
          item = ignoreEmpty && Token.normalize(text).length == 0 ? null : Str.get(text);
          break;
        default:
      }
      if(item != null) ab.add(item);
    }
    return ab.array();
  }

  /** Layouts. */
  private enum Layout {
    /** Layout 'empty'. */ EMPTY() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) {
        return Str.EMPTY;
      }
    },
    /** Layout 'empty-plus'. */ EMPTY_PLUS() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        return attributes(node, format).map();
      }
    },
    /** Layout 'simple'. */ SIMPLE() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) {
        return Str.get(node.string());
      }
    },
    /** Layout 'simple-plus'. */ SIMPLE_PLUS() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        return attributes(node, format).put("#content", node.string()).map();
      }
    },
    /** Layout 'list'. */ LIST() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        final ANodeList children = children(NodeType.ELEMENT, node);
        if(!equalNames(children) || !empty(children(NodeType.TEXT, node))) {
          return MIXED.apply(node, parent, format);
        }
        final ArrayBuilder ab = new ArrayBuilder();
        for(final ANode child : children) {
          ab.add(layout(child, format).apply(child, null, format));
        }
        return ab.array();
      }
    },
    /** Layout: 'list-plus'. */ LIST_PLUS() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        final ANodeList children = children(NodeType.ELEMENT, node);
        if(!equalNames(children) || !empty(children(NodeType.TEXT, node))) {
          return MIXED.apply(node, parent, format);
        }
        final MapBuilder map = attributes(node, format);
        if(!children.isEmpty()) {
          map.put(nodeName(children.get(0), node, format), LIST.apply(node, parent, format));
        }
        return map.map();
      }
    },
    /** Layout: 'record'. */ RECORD() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        if(!empty(children(NodeType.TEXT, node))) return MIXED.apply(node, parent, format);

        final MapBuilder map = attributes(node, format);
        final TokenObjectMap<ANodeList> cache = new TokenObjectMap<>();
        for(final ANode child : children(NodeType.ELEMENT, node)) {
          cache.computeIfAbsent(nodeName(child, node, format), ANodeList::new).add(child);
        }
        for(final byte[] name : cache) {
          final ArrayBuilder ab = new ArrayBuilder();
          for(final ANode child : cache.get(name)) {
            ab.add(layout(child, format).apply(child, node, format));
          }
          final XQArray array = ab.array();
          map.put(name, array.structSize() == 1 ? array.memberAt(0) : array);
        }
        return map.map();
     }
    },
    /** Layout 'sequence'. */ SEQUENCE() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        return mixed(node, parent, format, true);
      }
    },
    /** Layout 'mixed'. */ MIXED() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        return mixed(node, parent, format, false);
      }
    },
    /** Layout 'xml'. */ XML() {
      @Override
      Value apply(final ANode node, final ANode parent, final Format format) throws QueryException {
        final SerializerOptions sopts = new SerializerOptions();
        try {
          return Str.get(node.serialize(sopts).finish());
        } catch(final QueryIOException ex) {
          throw ex.getCause(null);
        }
      }
    };

    /**
     * Creates a map value for the given node.
     * @param node node
     * @param parent parent (can be {@code null})
     * @param format formatting hints
     * @return map
     * @throws QueryException query exception
     */
    abstract Value apply(ANode node, ANode parent, Format format) throws QueryException;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }
}
