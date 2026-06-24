package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Plan functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class PlanFn extends StandardFunc {
  /** Options for converting between elements and maps. */
  public static class ElementsOptions extends Options {
    /** Option. */
    public static final StringOption ATTRIBUTE_MARKER = new StringOption("attribute-marker", "@");
    /** Option. */
    public static final StringOption CONTENT_KEY = new StringOption("content-key", "#content");
    /** Option. */
    public static final EnumOption<NameFormat> NAME_FORMAT =
        new EnumOption<>("name-format", NameFormat.DEFAULT);
    /** Option. */
    public static final ValueOption PLAN = new ValueOption("plan", Types.MAP_ZO);
  }

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
    /** Content key. */
    Str content = CONTENT;
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
    /** Type 'integer'. */ INTEGER,
    /** Type 'decimal'. */ DECIMAL,
    /** Type 'double'.  */ DOUBLE,
    /** Type 'boolean'. */ BOOLEAN,
    /** Type 'string'.  */ STRING,
    /** Type 'skip'.    */ SKIP;

    /**
     * Infers a data type for the string values of the specified nodes.
     * @param nodes nodes
     * @return type
     */
    static PlanType get(final GNode... nodes) {
      boolean dbl = true, integer = true, decimal = true, leadingZero = false, bool = true;
      for(final GNode node : nodes) {
        final byte[] value = node.string(), trimmed = Token.trim(value);
        if(dbl) {
          try {
            final double d = Dbl.parse(value, null);
            if(Double.isNaN(d) || Double.isInfinite(d)) dbl = false;
          } catch(final QueryException ex) {
            Util.debug(ex);
            dbl = false;
          }
        }
        if(integer) {
          if(integerLexical(trimmed)) leadingZero |= leadingZero(trimmed);
          else integer = false;
        }
        if(decimal && !decimalLexical(trimmed)) decimal = false;
        if(bool && Bln.parse(trimmed) == null) bool = false;
      }
      if(dbl) return integer ? leadingZero ? STRING : INTEGER : decimal ? DECIMAL : DOUBLE;
      return bool ? BOOLEAN : STRING;
    }

    /**
     * Checks if a trimmed token is in the lexical space of xs:integer.
     * @param token token
     * @return result of check
     */
    private static boolean integerLexical(final byte[] token) {
      final int tl = token.length;
      int t = tl > 0 && (token[0] == '+' || token[0] == '-') ? 1 : 0;
      if(t == tl) return false;
      for(; t < tl; t++) {
        if(!Token.digit(token[t])) return false;
      }
      return true;
    }

    /**
     * Checks if a trimmed integer token starts with a leading zero followed by another digit.
     * @param token token (in the lexical space of xs:integer)
     * @return result of check
     */
    private static boolean leadingZero(final byte[] token) {
      final int tl = token.length, t = tl > 0 && (token[0] == '+' || token[0] == '-') ? 1 : 0;
      return t + 1 < tl && token[t] == '0';
    }

    /**
     * Checks if a trimmed token is in the lexical space of xs:decimal.
     * @param token token
     * @return result of check
     */
    private static boolean decimalLexical(final byte[] token) {
      final int tl = token.length;
      int t = tl > 0 && (token[0] == '+' || token[0] == '-') ? 1 : 0;
      boolean digit = false, dot = false;
      for(; t < tl; t++) {
        final byte b = token[t];
        if(Token.digit(b)) digit = true;
        else if(b == '.' && !dot) dot = true;
        else return false;
      }
      return digit;
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
    Item cast(final Str item) {
      if(type != null) {
        try {
          switch(type) {
            case BOOLEAN -> {
              final Boolean b = Bln.parse(item.string());
              if(b != null) return Bln.get(b);
            }
            case INTEGER -> {
              return Itr.get(item.itr(info));
            }
            case DECIMAL -> {
              return Dec.get(item.dec(info));
            }
            case DOUBLE -> {
              return Dbl.get(item.dbl(info));
            }
            default -> { }
          }
        } catch(final QueryException ex) {
          Util.debug(ex);
        }
      }
      return Atm.get(item.string());
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
    Item apply(final GNode node, final GNode parent, final Plan plan, final QueryContext qc)
        throws QueryException {

      PlanEntry pe = this;
      if(!valid(node)) {
        // fall back to the wildcard layout, which must be applicable as well
        pe = plan.entries.get(QNm.EMPTY);
        if(pe != null && !pe.valid(node)) pe = null;
      }
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
    private boolean valid(final GNode node) {
      return switch(layout) {
        case EMPTY, EMPTY_PLUS ->
          children(Kind.ELEMENT, node).isEmpty() && empty(children(Kind.TEXT, node));
        case SIMPLE, SIMPLE_PLUS ->
          children(Kind.ELEMENT, node).isEmpty();
        case LIST, LIST_PLUS -> {
          final GNodeList children = children(Kind.ELEMENT, node);
          yield empty(children(Kind.TEXT, node)) && equalNames(children) &&
            (children.isEmpty() || children.get(0).qname().eq(child));
        }
        case RECORD, SEQUENCE ->
          empty(children(Kind.TEXT, node));
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
    private Item create(final GNode node, final GNode parent, final Plan plan,
        final QueryContext qc) throws QueryException {

      return switch(layout) {
        case EMPTY ->
          Str.EMPTY;
        case EMPTY_PLUS ->
          attributes(node, plan, qc).map();
        case SIMPLE ->
          cast(Str.get(node.string()));
        case SIMPLE_PLUS -> {
          final MapBuilder mb = attributes(node, plan, qc);
          yield mb.put(contentKey(mb, plan), cast(Str.get(node.string()))).map();
        }
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
   * Builds a conversion plan from the function options.
   * @param options options
   * @param qc query context
   * @return conversion plan
   * @throws QueryException query exception
   */
  final Plan buildPlan(final ElementsOptions options, final QueryContext qc)
      throws QueryException {
    final Plan plan = new Plan();
    plan.name = options.get(ElementsOptions.NAME_FORMAT);
    plan.marker = options.get(ElementsOptions.ATTRIBUTE_MARKER);
    plan.content = Str.get(options.get(ElementsOptions.CONTENT_KEY));

    final Value pln = options.get(ElementsOptions.PLAN);
    if(!pln.isEmpty()) {
      toMap(pln, qc).forEach((key, value) -> {
        final byte[] token = key.string(info);
        final boolean attr = Token.startsWith(token, '@');
        final QNm name;
        if(Token.eq(token, Token.cpToken('*'))) {
          name = QNm.EMPTY;
        } else {
          name = qc.shared.parseQName(attr ? Token.substring(token, 1) : token, true, qc, sc());
        }
        // entries with keys that are no valid names are ignored
        if(name == null) return;

        final PlanEntry pe = new PlanEntry();
        pe.attribute = attr;
        final XQMap map = toMap(value, qc);
        final Value layout = map.get(LAYOUT);
        if(!layout.isEmpty()) {
          final String string = toString(layout, qc);
          pe.layout = Enums.get(PlanLayout.class, string);
          if(pe.layout == null) throw unexpected("layout", string, name);
        }
        final Value type = map.get(TYPE);
        if(!type.isEmpty()) {
          final String string = toString(type, qc);
          pe.type = Enums.get(PlanType.class, string);
          if(pe.type == null) throw unexpected("type", string, name);
        }
        final Value child = map.get(CHILD);
        if(!child.isEmpty()) {
          final byte[] childName = toToken(child, qc);
          pe.child = qc.shared.parseQName(childName, true, qc, sc());
          if(pe.child == null) throw unexpected("child", Token.string(childName), name);
        }
        plan.entries.put(name, pe);

        // error handling
        if(pe.layout == null) {
          if(!pe.attribute) throw missing("layout", name);
        } else if(pe.attribute) {
          throw unexpected("layout", pe.layout, name);
        }
        if(pe.layout == PlanLayout.LIST || pe.layout == PlanLayout.LIST_PLUS) {
          if(pe.child == null) throw missing("child", name);
        } else if(pe.child != null) {
          throw unexpected("child", pe.child, name);
        }
        if(pe.layout == PlanLayout.SIMPLE || pe.layout == PlanLayout.SIMPLE_PLUS ||
            pe.attribute) {
          if(pe.attribute) {
            if(pe.type == null) throw missing("type", name);
          } else if(pe.type == PlanType.SKIP) {
            throw unexpected("type", pe.type, name);
          }
        } else if(pe.type != null) {
          throw unexpected("type", pe.type, name);
        }
      });
    }
    return plan;
  }

  /**
   * Returns an exception for a missing plan key.
   * @param key key
   * @param name node name
   * @return exception
   */
  private QueryException missing(final String key, final QNm name) {
    return INVALIDOPTION_X.get(info, Util.info("Missing key '%' (node: %).", key, name));
  }

  /**
   * Returns an exception for an unexpected plan key.
   * @param key key
   * @param value value
   * @param name node name
   * @return exception
   */
  private QueryException unexpected(final String key, final Object value, final QNm name) {
    return INVALIDOPTION_X.get(info, Util.info("Unexpected key '%':'%' (node: %).", key, value,
        name));
  }

  /**
   * Returns a matching layout for the specified element.
   * @param node node
   * @param plan plan
   * @return layout
   */
  final PlanEntry entry(final GNode node, final Plan plan) {
    PlanEntry pe = plan.entries.get(node.qname());
    // entries for attributes of the same name must be ignored
    if(pe == null || pe.attribute) pe = plan.entries.get(QNm.EMPTY);
    return pe != null ? pe : entry(node);
  }

  /**
   * Returns the plan entry for an attribute.
   * @param name attribute name
   * @param plan plan
   * @return entry, or {@code null} if the plan has no entry for this attribute
   */
  private static PlanEntry attributeEntry(final QNm name, final Plan plan) {
    final PlanEntry pe = plan.entries.get(name);
    return pe != null && pe.attribute ? pe : null;
  }

  /**
   * Returns a plan entry for the specified nodes.
   * @param nodes nodes
   * @return entry
   */
  final PlanEntry entry(final GNode... nodes) {
    final PlanEntry pe = new PlanEntry();
    final GNodeList attributes = children(Kind.ATTRIBUTE, nodes);
    final GNodeList elements = children(Kind.ELEMENT, nodes);
    final GNodeList texts = children(Kind.TEXT, nodes);
    if(elements.isEmpty() && texts.isEmpty()) {
      pe.layout = attributes.isEmpty() ? PlanLayout.EMPTY : PlanLayout.EMPTY_PLUS;
    } else if(elements.isEmpty()) {
      pe.layout = attributes.isEmpty() ? PlanLayout.SIMPLE : PlanLayout.SIMPLE_PLUS;
      pe.type = PlanType.get(nodes);
    } else if(empty(texts)) {
      if(equalNames(elements) && Checks.any(nodes, node ->
          children(Kind.ELEMENT, node).size() > 1)) {
        pe.layout = attributes.isEmpty() ? PlanLayout.LIST : PlanLayout.LIST_PLUS;
        pe.child = elements.get(0).qname();
      } else if(Checks.all(nodes, PlanFn::differentNames)) {
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
  static boolean empty(final GNodeList nodes) {
    return Checks.all(nodes, node -> Token.normalize(node.string()).length == 0);
  }

  /**
   * Returns the children of the specified type.
   * @param kind kind to be found
   * @param nodes nodes
   * @return result of check
   */
  static GNodeList children(final Kind kind, final GNode... nodes) {
    final GNodeList list = new GNodeList();
    for(final GNode node : nodes) {
      if(kind == Kind.ATTRIBUTE) {
        for(final GNode child : node.attributeIter()) {
          if(!Token.eq(child.qname().uri(), QueryText.XSI_URI)) list.add(child);
        }
      } else {
        for(final GNode child : node.childIter()) {
          if(child.kind() == kind) list.add(child);
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
  private static boolean differentNames(final GNode node) {
    final QNmSet names = new QNmSet();
    for(final GNode child : children(Kind.ELEMENT, node)) {
      if(child.kind() == Kind.ELEMENT && !names.add(child.qname())) return false;
    }
    return !names.isEmpty();
  }

  /**
   * Checks if the element names are distinct or different.
   * @param nodes node list
   * @return result of check
   */
  private static boolean equalNames(final GNodeList nodes) {
    QNm name = null;
    for(final GNode node : nodes) {
      if(node.kind() == Kind.ELEMENT) {
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
  private MapBuilder attributes(final GNode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final GNodeList attributes = children(Kind.ATTRIBUTE, node);
    final MapBuilder mb = new MapBuilder(attributes.size());
    // a marker that does not distinguish attributes from child elements is replaced by '@'
    final String marker = conflict(node, attributes, plan, qc) ? "@" : plan.marker;
    for(final GNode attr : attributes) {
      final PlanEntry entry = attributeEntry(attr.qname(), plan);
      // attributes with the type 'skip' are omitted
      if(entry != null && entry.type == PlanType.SKIP) continue;
      final Str value = Str.get(attr.string());
      mb.put(nodeName(attr.qname(), false, node, plan, qc, marker),
          entry != null ? entry.cast(value) : value);
    }
    return mb;
  }

  /**
   * Checks if the names of attributes and child elements of a node conflict.
   * @param node node
   * @param attributes attributes of the node
   * @param plan plan
   * @param qc query context
   * @return result of check
   */
  private static boolean conflict(final GNode node, final GNodeList attributes, final Plan plan,
      final QueryContext qc) {
    if(attributes.isEmpty() || "@".equals(plan.marker)) return false;
    final TokenSet names = new TokenSet();
    for(final GNode child : children(Kind.ELEMENT, node)) {
      names.add(nodeName(child, node, plan, qc));
    }
    for(final GNode attr : attributes) {
      final PlanEntry entry = attributeEntry(attr.qname(), plan);
      if((entry == null || entry.type != PlanType.SKIP) &&
          names.contains(nodeName(attr, node, plan, qc))) return true;
    }
    return false;
  }

  /**
   * Returns the content key, prepending {@code #} characters to avoid clashes with existing keys.
   * @param mb map builder with the keys generated so far
   * @param plan plan
   * @return content key
   * @throws QueryException query exception
   */
  private static Str contentKey(final MapBuilder mb, final Plan plan) throws QueryException {
    Str key = plan.content;
    while(mb.contains(key)) key = Str.get(Token.concat(Token.cpToken('#'), key.string()));
    return key;
  }

  /**
   * Returns a string representation of the name of the node.
   * @param node node
   * @param parent parent (can be {@code null})
   * @param plan plan
   * @param qc query context
   * @return name
   */
  static byte[] nodeName(final GNode node, final GNode parent, final Plan plan,
      final QueryContext qc) {
    return nodeName(node.qname(), node.kind() == Kind.ELEMENT, parent, plan, qc);
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
  static byte[] nodeName(final QNm qnm, final boolean element, final GNode parent,
      final Plan plan, final QueryContext qc) {
    return nodeName(qnm, element, parent, plan, qc, plan.marker);
  }

  /**
   * Returns a string representation of the name of the node.
   * @param qnm QName
   * @param element element flag
   * @param parent parent (can be {@code null})
   * @param plan plan
   * @param qc query context
   * @param marker attribute marker (can be {@code null})
   * @return name
   */
  static byte[] nodeName(final QNm qnm, final boolean element, final GNode parent,
      final Plan plan, final QueryContext qc, final String marker) {
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
    return qc.shared.token(!element && marker != null ? Token.concat(marker, name) : name);
  }

  /**
   * Returns a list item.
   * @param node node
   * @param plan plan
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  private XQArray list(final GNode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final GNodeList children = children(Kind.ELEMENT, node);
    final ArrayBuilder ab = new ArrayBuilder(qc, children.size());
    for(final GNode ch : children) {
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
  private XQMap record(final GNode node, final Plan plan, final QueryContext qc)
      throws QueryException {
    final MapBuilder map = attributes(node, plan, qc);
    final TokenObjectMap<GNodeList> cache = new TokenObjectMap<>();
    for(final GNode ch : children(Kind.ELEMENT, node)) {
      cache.computeIfAbsent(nodeName(ch, node, plan, qc), GNodeList::new).add(ch);
    }
    for(final byte[] name : cache) {
      final GNodeList children = cache.get(name);
      final PlanEntry pe = entry(children.get(0), plan);
      if(pe.layout != PlanLayout.DEEP_SKIP) {
        final ArrayBuilder ab = new ArrayBuilder(qc, children.size());
        for(final GNode ch : children) {
          ab.add(pe.apply(ch, node, plan, qc));
        }
        final XQArray array = ab.array();
        map.put(name, array.structSize() == 1 ? array.valueAt(0) : array);
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
  private XQArray mixed(final GNode node, final GNode parent, final Plan plan,
      final QueryContext qc, final boolean ignoreEmpty) throws QueryException {

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final GNode attr : children(Kind.ATTRIBUTE, node)) {
      ab.add(new MapBuilder().put(nodeName(attr, node, plan, qc), attr.string()).map());
    }
    for(final GNode child : node.childIter()) {
      final Item item = switch(child.kind()) {
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
  private static Str xml(final GNode node) throws QueryException {
    try {
      return Str.get(node.serialize(new SerializerOptions()).finish());
    } catch(final QueryIOException ex) {
      throw ex.getCause(null);
    }

  }
}
