package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
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
    /** Option. */
    public static final BooleanOption LIBERAL = new BooleanOption("liberal", false);
  }

  /** Resolves the predefined xml prefix. */
  public static final UnaryOperator<byte[]> XML_PREFIX =
      prefix -> Token.eq(prefix, Token.XML) ? QueryText.XML_URI : null;

  /** Comment string. */
  static final Str COMMENT = Str.get("#comment");
  /** PI string. */
  static final Str PI = Str.get("#processing-instruction");
  /** Target string. */
  static final Str TARGET = Str.get("#target");
  /** Data string. */
  static final Str DATA = Str.get("#data");
  /** Layout string. */
  private static final Str LAYOUT = Str.get("layout");
  /** Type string. */
  private static final Str TYPE = Str.get("type");
  /** Child string. */
  private static final Str CHILD = Str.get("child");

  /** Conversion plan. */
  static final class Plan {
    /** Plan entries. */
    final QNmMap<PlanEntry> entries = new QNmMap<>();
    /** Name format. */
    NameFormat name;
    /** Attribute marker. */
    String marker;
    /** Content key. */
    Str content;
    /** Liberal mode: retain values that cannot be cast to a prescribed type. */
    boolean liberal;
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
  static final class PlanEntry {
    /** Attribute flag. */
    boolean attribute;
    /** Layout ({@code null} for attributes). */
    PlanLayout layout;
    /** Type (can be {@code null}). */
    PlanType type;
    /** Type was explicitly prescribed by the supplied plan (not inferred). */
    boolean explicitType;
    /** Child (can be {@code null}). */
    QNm child;
  }

  /**
   * Returns a resolver for the URIs of namespace prefixes.
   * @param qc query context
   * @param sc static context
   * @return resolver
   */
  public static UnaryOperator<byte[]> uris(final QueryContext qc, final StaticContext sc) {
    return prefix -> qc.ns.resolve(prefix, sc);
  }

  /**
   * Builds a conversion plan.
   * @param options options
   * @param uris resolves the URI of a namespace prefix ({@code null} if the prefix is unbound)
   * @param shared shared data references
   * @param info input info (can be {@code null})
   * @return conversion plan
   * @throws QueryException query exception
   */
  static Plan plan(final ElementsOptions options, final UnaryOperator<byte[]> uris,
      final SharedData shared, final InputInfo info) throws QueryException {
    final Plan plan = new Plan();
    plan.name = options.get(ElementsOptions.NAME_FORMAT);
    plan.marker = options.get(ElementsOptions.ATTRIBUTE_MARKER);
    plan.content = Str.get(options.get(ElementsOptions.CONTENT_KEY));
    plan.liberal = options.get(ElementsOptions.LIBERAL);

    final Value pln = options.get(ElementsOptions.PLAN);
    if(!pln.isEmpty()) {
      map(pln, info).forEach((key, value) -> {
        final byte[] token = key.string(info);
        final boolean attr = Token.startsWith(token, '@');
        final QNm name;
        if(Token.eq(token, Token.cpToken('*'))) {
          name = QNm.EMPTY;
        } else {
          name = shared.parseQName(attr ? Token.substring(token, 1) : token, true, uris);
        }
        // entries with keys that are no valid names are ignored
        if(name == null) return;

        final PlanEntry pe = new PlanEntry();
        pe.attribute = attr;
        final XQMap map = map(value, info);
        final Value layout = map.get(LAYOUT);
        if(!layout.isEmpty()) {
          final String string = Token.string(token(layout, info));
          pe.layout = Enums.get(PlanLayout.class, string);
          if(pe.layout == null) throw unexpected("layout", string, name, info);
        }
        final Value type = map.get(TYPE);
        if(!type.isEmpty()) {
          final String string = Token.string(token(type, info));
          pe.type = Enums.get(PlanType.class, string);
          if(pe.type == null) throw unexpected("type", string, name, info);
          pe.explicitType = true;
        }
        final Value child = map.get(CHILD);
        if(!child.isEmpty()) {
          final byte[] childName = token(child, info);
          pe.child = shared.parseQName(childName, true, uris);
          if(pe.child == null) {
            throw unexpected("child", Token.string(childName), name, info);
          }
        }
        plan.entries.put(name, pe);

        // error handling
        if(pe.layout == null) {
          if(!pe.attribute) throw missing("layout", name, info);
        } else if(pe.attribute) {
          throw unexpected("layout", pe.layout, name, info);
        }
        if(pe.layout != PlanLayout.LIST && pe.layout != PlanLayout.LIST_PLUS &&
            pe.child != null) {
          throw unexpected("child", pe.child, name, info);
        }
        if(pe.layout == PlanLayout.SIMPLE || pe.layout == PlanLayout.SIMPLE_PLUS) {
          // 'skip' is reserved for attributes
          if(pe.type == PlanType.SKIP) throw unexpected("type", pe.type, name, info);
        } else if(!pe.attribute && pe.type != null) {
          throw unexpected("type", pe.type, name, info);
        }
      });
    }
    return plan;
  }

  /**
   * Returns the map of a plan value.
   * @param value value
   * @param info input info (can be {@code null})
   * @return map
   * @throws QueryException query exception
   */
  private static XQMap map(final Value value, final InputInfo info) throws QueryException {
    if(value instanceof final XQMap map) return map;
    throw typeError(value, Types.MAP, info);
  }

  /**
   * Returns the string of a plan value.
   * @param value value
   * @param info input info (can be {@code null})
   * @return string
   * @throws QueryException query exception
   */
  private static byte[] token(final Value value, final InputInfo info) throws QueryException {
    if(value instanceof final Item item && item.type.isStringOrUntyped()) return item.string(info);
    throw typeError(value, BasicType.STRING, info);
  }

  /**
   * Returns an exception for a missing plan key.
   * @param key key
   * @param name node name
   * @param info input info (can be {@code null})
   * @return exception
   */
  private static QueryException missing(final String key, final QNm name, final InputInfo info) {
    return INVALIDOPTION_X.get(info, Util.info("Missing key '%' (node: %).", key, name));
  }

  /**
   * Returns an exception for an unexpected plan key.
   * @param key key
   * @param value value
   * @param name node name
   * @param info input info (can be {@code null})
   * @return exception
   */
  private static QueryException unexpected(final String key, final Object value, final QNm name,
      final InputInfo info) {
    return INVALIDOPTION_X.get(info, Util.info("Unexpected key '%':'%' (node: %).", key, value,
        name));
  }

  /**
   * Returns the plan entry for an element name, falling back to the wildcard entry.
   * @param name element name
   * @param plan plan
   * @return entry, or {@code null} if the plan has no entry for this element
   */
  static PlanEntry entry(final QNm name, final Plan plan) {
    final PlanEntry pe = plan.entries.get(name);
    // entries for attributes of the same name must be ignored
    return pe != null && !pe.attribute ? pe : plan.entries.get(QNm.EMPTY);
  }

  /**
   * Returns a plan entry for the specified nodes.
   * @param nodes nodes
   * @return entry
   */
  static PlanEntry entry(final GNode... nodes) {
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
  static boolean equalNames(final GNodeList nodes) {
    QNm name = null;
    for(final GNode node : nodes) {
      if(node.kind() == Kind.ELEMENT) {
        if(name == null) name = node.qname();
        else if(!name.eq(node.qname())) return false;
      }
    }
    return true;
  }
}
