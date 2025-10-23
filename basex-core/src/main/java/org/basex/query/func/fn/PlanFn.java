package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Plan functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class PlanFn extends StandardFunc {
  /** Content string. */
  static final Str CONTENT = Str.get("#content");
  /** Comment string. */
  static final Str COMMENT = Str.get("#comment");
  /** PI string. */
  static final Str PI = Str.get("#processing-instruction");
  /** Data string. */
  static final Str DATA = Str.get("#data");
  /** Layout string. */
  static final Str LAYOUT = Str.get("layout");
  /** Type string. */
  static final Str TYPE = Str.get("type");
  /** Child string. */
  static final Str CHILD = Str.get("child");

  /** Conversion plan. */
  static final class Plan {
    /** Plan entries. */
    final QNmMap<PlanEntry> entries = new QNmMap<>();
    /** Name format. */
    NameFormat name;
    /** Attribute marker. */
    String marker;
  }

  /** Name format. */
  enum NameFormat {
    /** fn:name.       */ LEXICAL,
    /** fn:local-name. */ LOCAL,
    /** Q{uri}local.   */ EQNAME,
    /** Default.       */ DEFAULT;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Types. */
  enum PlanType {
    /** Type 'boolean'. */ BOOLEAN(AtomType.BOOLEAN),
    /** Type 'numeric'. */ NUMERIC(AtomType.NUMERIC),
    /** Type 'string'.  */ STRING(AtomType.STRING),
    /** Type 'skip'.    */ SKIP(null);

    /** Target type. */
    final AtomType type;

    /**
     * Constructor.
     * @param type type
     */
    PlanType(final AtomType type) {
      this.type = type;
    }

    /**
     * Returns a matching type for the specified node.
     * @param nodes nodes
     * @return layout
     */
    static PlanType get(final ANode... nodes) {
      PlanType type = BOOLEAN;
      for(final ANode node : nodes) {
        final byte[] value = node.string();
        if(type == BOOLEAN) {
          if(Bln.parse(value) == null) type = NUMERIC;
        }
        if(type == NUMERIC) {
          try {
            Dbl.parse(value, null);
          } catch(final QueryException ex) {
            Util.debug(ex);
            type = STRING;
            break;
          }
        }
      }
      return type;
    }

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Layouts. */
  enum PlanLayout {
    /** Layout 'empty'.       */ EMPTY,
    /** Layout 'empty-plus'.  */ EMPTY_PLUS,
    /** Layout 'simple'.      */ SIMPLE,
    /** Layout 'simple-plus'. */ SIMPLE_PLUS,
    /** Layout 'list'.        */ LIST,
    /** Layout 'list-plus'.   */ LIST_PLUS,
    /** Layout 'record'.      */ RECORD,
    /** Layout 'sequence'.    */ SEQUENCE,
    /** Layout 'mixed'.       */ MIXED,
    /** Layout 'xml'.         */ XML,
    /** Layout 'deep-skip'.   */ DEEP_SKIP,
    /** Layout 'error'.       */ ERROR;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Entry of a plan. */
  final class PlanEntry {
    /** Attribute flag. */
    boolean attribute;
    /** Layout ({@code null} for attributes). */
    PlanLayout layout;
    /** Type (can be {@code null}). */
    PlanType type;
    /** Child (can be {@code null}). */
    QNm child;

    /**
     * Casts an item to the target type.
     * @param item item
     * @return cast item
     */
    Item cast(final Item item) {
      if(type != null && type.type != null) {
        try {
          return type.type.cast(item, null, info);
        } catch(final QueryException ex) {
          Util.debug(ex);
        }
      }
      return item;
    }

    /**
     * Applies a layout.
     * @param node node
     * @param parent parent (can be {@code null})
     * @param plan plan
     * @param qc query context
     * @return value
     * @throws QueryException query exception
     */
    Item apply(final ANode node, final ANode parent, final Plan plan, final QueryContext qc)
        throws QueryException {

      final PlanEntry pe = valid(node) ? this : plan.entries.get(QNm.EMPTY);
      if(pe != null) {
        try {
          return pe.create(node, parent, plan, qc);
        } catch(final QueryException ex) {
          Util.debug(ex);
        }
      }
      throw PLAN_X_X.get(info, layout, node);
    }

    /**
     * Checks if the layout can be applied to a node.
     * @param node node
     * @return result of check
     */
    private boolean valid(final ANode node) {
      return switch(layout) {
        case EMPTY, EMPTY_PLUS ->
          children(NodeType.ELEMENT, node).isEmpty() && empty(children(NodeType.TEXT, node));
        case SIMPLE, SIMPLE_PLUS ->
          children(NodeType.ELEMENT, node).isEmpty();
        case LIST, LIST_PLUS -> {
          final ANodeList children = children(NodeType.ELEMENT, node);
          yield empty(children(NodeType.TEXT, node)) && equalNames(children) &&
            (children.isEmpty() || children.get(0).qname().eq(child));
        }
        case RECORD, SEQUENCE ->
          empty(children(NodeType.TEXT, node));
        default ->
          true;
      };
    }

    /**
     * Applies the layout.
     * @param node node
     * @param parent parent (can be {@code null})
     * @param plan plan
     * @param qc query context
     * @return resulting value
     * @throws QueryException query exception
     */
    private Item create(final ANode node, final ANode parent, final Plan plan,
        final QueryContext qc) throws QueryException {

      return switch(layout) {
        case EMPTY ->
          Str.EMPTY;
        case EMPTY_PLUS ->
          attributes(node, plan, qc).map();
        case SIMPLE ->
          cast(Str.get(node.string()));
        case SIMPLE_PLUS ->
          attributes(node, plan, qc).put(CONTENT, cast(Str.get(node.string()))).map();
        case LIST ->
          list(node, plan, qc);
        case LIST_PLUS ->
          attributes(node, plan, qc).put(nodeName(child, true, node, plan, qc),
              list(node, plan, qc)).map();
        case RECORD ->
          record(node, plan, qc);
        case SEQUENCE ->
          mixed(node, parent, plan, qc, true);
        case MIXED ->
          mixed(node, parent, plan, qc, false);
        case XML ->
          xml(node);
        case DEEP_SKIP ->
          Empty.VALUE;
        default ->
          throw PLAN_X_X.get(null, this, node);
      };
    }
  }

  /**
   * Returns a matching layout for the specified element.
   * @param node node
   * @param plan plan
   * @return layout
   */
  final PlanEntry entry(final ANode node, final Plan plan) {
    PlanEntry pe = plan.entries.get(node.qname());
    if(pe == null) pe = plan.entries.get(QNm.EMPTY);
    return pe != null ? pe : entry(node);
  }

  /**
   * Returns a plan entry for the specified nodes.
   * @param nodes nodes
   * @return entry
   */
  final PlanEntry entry(final ANode... nodes) {
    final PlanEntry pe = new PlanEntry();
    final ANodeList attributes = children(NodeType.ATTRIBUTE, nodes);
    final ANodeList elements = children(NodeType.ELEMENT, nodes);
    final ANodeList texts = children(NodeType.TEXT, nodes);
    if(elements.isEmpty() && texts.isEmpty()) {
      pe.layout = attributes.isEmpty() ? PlanLayout.EMPTY : PlanLayout.EMPTY_PLUS;
    } else if(elements.isEmpty()) {
      pe.layout = attributes.isEmpty() ? PlanLayout.SIMPLE : PlanLayout.SIMPLE_PLUS;
      pe.type = PlanType.get(nodes);
    } else if(empty(texts)) {
      if(equalNames(elements) && ((Checks<ANode>) node ->
          children(NodeType.ELEMENT, node).size() > 1).any(nodes)) {
        pe.layout = attributes.isEmpty() ? PlanLayout.LIST : PlanLayout.LIST_PLUS;
        pe.child = elements.get(0).qname();
      } else if(((Checks<ANode>) PlanFn::differentNames).all(nodes)) {
        pe.layout = PlanLayout.RECORD;
      } else {
        pe.layout = PlanLayout.SEQUENCE;
      }
    } else {
      pe.layout = PlanLayout.MIXED;
    }
    return pe;
  }

  /**
   * Checks if the string values of all nodes are empty.
   * @param nodes node list
   * @return result of check
   */
  static boolean empty(final ANodeList nodes) {
    return ((Checks<ANode>) node -> Token.normalize(node.string()).length == 0).all(nodes);
  }

  /**
   * Returns the children of the specified type.
   * @param type type to be found
   * @param nodes nodes
   * @return result of check
   */
  static ANodeList children(final NodeType type, final ANode... nodes) {
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
   * Checks if the names of the children of the specified node are distinct.
   * @param node node
   * @return result of check
   */
  private static boolean differentNames(final ANode node) {
    final QNmSet names = new QNmSet();
    for(final ANode child : children(NodeType.ELEMENT, node)) {
      if(child.type == NodeType.ELEMENT && !names.add(child.qname())) return false;
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
   * @param plan plan
   * @param qc query context
   * @return attributes
   * @throws QueryException query exception
   */
  private MapBuilder attributes(final ANode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final ANodeList attributes = children(NodeType.ATTRIBUTE, node);
    final MapBuilder mb = new MapBuilder(attributes.size());
    for(final ANode attr : attributes) {
      final PlanEntry entry = plan.entries.get(attr.qname());
      final Item value = Str.get(attr.string());
      mb.put(nodeName(attr, node, plan, qc), entry != null ? entry.cast(value) : value);
    }
    return mb;
  }

  /**
   * Returns a string representation of the name of the node.
   * @param node node
   * @param parent parent (can be {@code null})
   * @param plan plan
   * @param qc query context
   * @return name
   */
  static byte[] nodeName(final ANode node, final ANode parent, final Plan plan,
      final QueryContext qc) {
    return nodeName(node.qname(), node.type == NodeType.ELEMENT, parent, plan, qc);
  }

  /**
   * Returns a string representation of the name of the node.
   * @param qnm QName
   * @param element element flag
   * @param parent parent (can be {@code null})
   * @param plan plan
   * @param qc query context
   * @return name
   */
  static byte[] nodeName(final QNm qnm, final boolean element, final ANode parent,
      final Plan plan, final QueryContext qc) {
    final byte[] name = switch(plan.name) {
      case EQNAME ->
        qnm.uri().length != 0 ? qnm.eqName() : qnm.local();
      case LEXICAL ->
        qnm.string();
      case LOCAL ->
        qnm.local();
      default ->
        (element ? parent == null ? qnm.uri().length == 0 :
          Token.eq(parent.qname().uri(), qnm.uri()) : qnm.uri().length == 0) ? qnm.local() :
        Token.eq(qnm.uri(), QueryText.XML_URI) ? qnm.string() : qnm.eqName();
    };
    return qc.shared.token(!element && plan.marker != null ?
      Token.concat(plan.marker, name) : name);
  }

  /**
   * Returns a list item.
   * @param node node
   * @param plan plan
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private XQArray list(final ANode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final ANodeList children = children(NodeType.ELEMENT, node);
    final ArrayBuilder ab = new ArrayBuilder(qc, children.size());
    for(final ANode ch : children) {
      ab.add(entry(ch, plan).apply(ch, null, plan, qc));
    }
    return ab.array();
  }

  /**
   * Returns a record item.
   * @param node node
   * @param plan plan
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private XQMap record(final ANode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final MapBuilder map = attributes(node, plan, qc);
    final TokenObjectMap<ANodeList> cache = new TokenObjectMap<>();
    for(final ANode ch : children(NodeType.ELEMENT, node)) {
      cache.computeIfAbsent(nodeName(ch, node, plan, qc), ANodeList::new).add(ch);
    }
    for(final byte[] name : cache) {
      final ANodeList children = cache.get(name);
      final PlanEntry pe = entry(children.get(0), plan);
      if(pe.layout != PlanLayout.DEEP_SKIP) {
        final ArrayBuilder ab = new ArrayBuilder(qc, children.size());
        for(final ANode ch : children) {
          ab.add(pe.apply(ch, node, plan, qc));
        }
        final XQArray array = ab.array();
        map.put(name, array.structSize() == 1 ? array.memberAt(0) : array);
      }
    }
    return map.map();
  }

  /**
   * Returns a mixed-layout item.
   * @param node node
   * @param parent parent (can be {@code null}
   * @param plan plan
   * @param qc query context
   * @param ignoreEmpty ignore empty text nodes
   * @return array
   * @throws QueryException query exception
   */
  private XQArray mixed(final ANode node, final ANode parent, final Plan plan,
      final QueryContext qc, final boolean ignoreEmpty) throws QueryException {

    final ArrayBuilder ab = new ArrayBuilder();
    for(final ANode attr : children(NodeType.ATTRIBUTE, node)) {
      ab.add(new MapBuilder().put(nodeName(attr, node, plan, qc), attr.string()).map());
    }
    for(final ANode child : node.childIter()) {
      final Item item = switch((NodeType) child.type) {
        case COMMENT ->
          new MapBuilder().put(COMMENT, child.string()).map();
        case ELEMENT ->
          new MapBuilder().put(nodeName(child, parent, plan, qc),
            entry(child, plan).apply(child, node, plan, qc)).map();
        case PROCESSING_INSTRUCTION ->
          new MapBuilder().put(PI, child.name()).put(DATA, child.string()).map();
        case TEXT -> {
          final byte[] text = child.string();
          yield ignoreEmpty && Token.normalize(text).length == 0 ? null : Str.get(text);
        }
        default -> null;
      };
      if(item != null) ab.add(item);
    }
    return ab.array();
  }

  /**
   * Returns an XML item.
   * @param node node
   * @return array
   * @throws QueryException query exception
   */
  private static Str xml(final ANode node) throws QueryException {
    try {
      return Str.get(node.serialize(new SerializerOptions()).finish());
    } catch(final QueryIOException ex) {
      throw ex.getCause(null);
    }

  }
}
