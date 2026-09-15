package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.fn.PlanFn.*;
import org.basex.query.util.*;
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
 * Converts elements to maps.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ElementToMap {
  /** Conversion plan. */
  private final Plan plan;
  /** Shared data references. */
  private final SharedData shared;
  /** Interruptible job. */
  private final Job job;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param eopts options for converting between elements and maps
   * @param uris resolves the URI of a namespace prefix ({@code null} if the prefix is unbound)
   * @param shared shared data references
   * @param job interruptible job
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public ElementToMap(final ElementsOptions eopts, final UnaryOperator<byte[]> uris,
      final SharedData shared, final Job job, final InputInfo info) throws QueryException {
    this.shared = shared;
    this.job = job;
    this.info = info;
    plan = PlanFn.plan(eopts, uris, shared, info);
  }

  /**
   * Converts a document or element node to a map, or to the value of its root entry.
   * @param node document or element node
   * @param root name of the root element to be omitted (can be {@code null})
   * @return map, value of the root entry, or empty sequence
   * @throws QueryException query exception
   */
  public Item convert(final XNode node, final byte[] root) throws QueryException {
    // a document node is represented by its single element child (may be preceded by comments, PIs)
    XNode elem = node;
    if(elem.type.instanceOf(NodeType.DOCUMENT)) {
      for(final GNode child : elem.childIter()) {
        if(child.kind() == Kind.ELEMENT) {
          elem = (XNode) child;
          break;
        }
      }
    }
    final Item value = apply(entry(elem), elem);
    if(value.isEmpty()) return value;

    final byte[] name = nodeName(elem, null);
    if(root == null) return XQMap.get(Str.get(name), value);
    if(!Token.eq(name, root)) {
      throw JSON_SERIALIZE_X.get(info, Util.info("Root element '%' expected, found '%'", root,
          name));
    }
    return value;
  }

  /**
   * Returns a matching plan entry for the specified element.
   * @param node node
   * @return plan entry
   */
  private PlanEntry entry(final GNode node) {
    final PlanEntry pe = PlanFn.entry(node.qname(), plan);
    return pe != null ? pe : PlanFn.entry(node);
  }

  /**
   * Casts an item to the target type of a plan entry. If a prescribed type cannot be applied,
   * an error is raised, or the original value is retained (liberal mode); empty and
   * whitespace-only content is never affected.
   * @param pe plan entry
   * @param item item
   * @return cast item
   * @throws QueryException query exception
   */
  private Item cast(final PlanEntry pe, final Str item) throws QueryException {
    final byte[] value = item.string();
    if(applyType(pe, value)) {
      try {
        final Item cast = switch(pe.type) {
          case BOOLEAN -> {
            final Boolean b = Bln.parse(value);
            yield b != null ? Bln.get(b) : null;
          }
          case INTEGER -> Itr.get(item.itr(info));
          case DECIMAL -> Dec.get(item.dec(info));
          case DOUBLE -> Dbl.get(item.dbl(info));
          default -> null;
        };
        if(cast != null) return cast;
      } catch(final QueryException ex) {
        Util.debug(ex);
      }
      // value could not be cast to the prescribed type
      if(!plan.liberal && pe.explicitType) throw PLAN_TYPE_X_X.get(info, value, pe.type);
    }
    return Atm.get(value);
  }

  /**
   * Checks whether a prescribed type is to be applied: a non-string type is prescribed and the
   * content is neither empty nor whitespace-only.
   * @param pe plan entry
   * @param value string value
   * @return result of check
   */
  private static boolean applyType(final PlanEntry pe, final byte[] value) {
    return pe.type != null && pe.type != PlanType.STRING && pe.type != PlanType.SKIP &&
        Token.normalize(value).length != 0;
  }

  /**
   * Applies the layout of a plan entry.
   * @param entry plan entry
   * @param node node
   * @return value
   * @throws QueryException query exception
   */
  private Item apply(final PlanEntry entry, final GNode node) throws QueryException {

    PlanEntry pe = entry;
    if(!valid(pe, node)) {
      // fall back to the wildcard layout, which must be applicable as well
      pe = plan.entries.get(QNm.EMPTY);
      if(pe != null && !valid(pe, node)) pe = null;
    }
    if(pe != null) {
      try {
        return create(pe, node);
      } catch(final QueryException ex) {
        // a type error is final; it must not trigger layout fallback
        if(ex.error() == PLAN_TYPE_X_X) throw ex;
        Util.debug(ex);
      }
    }
    throw PLAN_X_X.get(info, entry.layout, node);
  }

  /**
   * Checks if the layout of a plan entry can be applied to a node.
   * @param pe plan entry
   * @param node node
   * @return result of check
   */
  private static boolean valid(final PlanEntry pe, final GNode node) {
    return switch(pe.layout) {
      case EMPTY, EMPTY_PLUS ->
        PlanFn.children(Kind.ELEMENT, node).isEmpty() &&
        PlanFn.empty(PlanFn.children(Kind.TEXT, node));
      case SIMPLE, SIMPLE_PLUS ->
        PlanFn.children(Kind.ELEMENT, node).isEmpty();
      case LIST, LIST_PLUS -> {
        final GNodeList children = PlanFn.children(Kind.ELEMENT, node);
        yield PlanFn.empty(PlanFn.children(Kind.TEXT, node)) && PlanFn.equalNames(children) &&
          (pe.child == null || children.isEmpty() || children.get(0).qname().eq(pe.child));
      }
      case RECORD, SEQUENCE ->
        PlanFn.empty(PlanFn.children(Kind.TEXT, node));
      default ->
        true;
    };
  }

  /**
   * Applies the layout of a plan entry.
   * @param pe plan entry
   * @param node node
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item create(final PlanEntry pe, final GNode node) throws QueryException {

    return switch(pe.layout) {
      case EMPTY ->
        Str.EMPTY;
      case EMPTY_PLUS ->
        attributes(node).map();
      case SIMPLE ->
        cast(pe, Str.get(node.string()));
      case SIMPLE_PLUS -> {
        final MapBuilder mb = attributes(node);
        yield mb.put(contentKey(mb), cast(pe, Str.get(node.string()))).map();
      }
      case LIST ->
        list(node);
      case LIST_PLUS -> {
        final MapBuilder mb = attributes(node);
        // if the plan supplies no child name, the name of the first child is adopted
        final GNodeList children = PlanFn.children(Kind.ELEMENT, node);
        final QNm name = pe.child != null ? pe.child :
          children.isEmpty() ? null : children.get(0).qname();
        if(name != null) mb.put(nodeName(name, true, node, plan.marker), list(node));
        yield mb.map();
      }
      case RECORD ->
        record(node);
      case SEQUENCE ->
        mixed(node, true);
      case MIXED ->
        mixed(node, false);
      case XML ->
        xml(node);
      case DEEP_SKIP ->
        Empty.VALUE;
      default ->
        throw PLAN_X_X.get(null, pe.layout, node);
    };
  }

  /**
   * Returns the plan entry for an attribute.
   * @param name attribute name
   * @return entry, or {@code null} if the plan has no entry for this attribute
   */
  private PlanEntry attributeEntry(final QNm name) {
    final PlanEntry pe = plan.entries.get(name);
    return pe != null && pe.attribute ? pe : null;
  }

  /**
   * Returns an attribute map.
   * @param node node
   * @return attributes
   * @throws QueryException query exception
   */
  private MapBuilder attributes(final GNode node) throws QueryException {
    final GNodeList attributes = PlanFn.children(Kind.ATTRIBUTE, node);
    final MapBuilder mb = new MapBuilder(attributes.size());
    // a marker that does not distinguish attributes from child elements is replaced by '@'
    final String marker = conflict(node, attributes) ? "@" : plan.marker;
    for(final GNode attr : attributes) {
      final PlanEntry entry = attributeEntry(attr.qname());
      // attributes with the type 'skip' are omitted
      if(entry != null && entry.type == PlanType.SKIP) continue;
      final byte[] value = attr.string();
      mb.put(nodeName(attr.qname(), false, node, marker),
          entry != null ? cast(entry, Str.get(value)) : Atm.get(value));
    }
    return mb;
  }

  /**
   * Checks if the names of attributes and child elements of a node conflict.
   * @param node node
   * @param attributes attributes of the node
   * @return result of check
   */
  private boolean conflict(final GNode node, final GNodeList attributes) {
    if(attributes.isEmpty() || "@".equals(plan.marker)) return false;
    final TokenSet names = new TokenSet();
    for(final GNode child : PlanFn.children(Kind.ELEMENT, node)) {
      names.add(nodeName(child, node));
    }
    for(final GNode attr : attributes) {
      final PlanEntry entry = attributeEntry(attr.qname());
      if((entry == null || entry.type != PlanType.SKIP) &&
          names.contains(nodeName(attr, node))) return true;
    }
    return false;
  }

  /**
   * Returns the content key, prepending {@code #} characters to avoid clashes with existing keys.
   * @param mb map builder with the keys generated so far
   * @return content key
   * @throws QueryException query exception
   */
  private Str contentKey(final MapBuilder mb) throws QueryException {
    Str key = plan.content;
    while(mb.contains(key)) key = Str.get(Token.concat(Token.cpToken('#'), key.string()));
    return key;
  }

  /**
   * Returns a string representation of the name of the node.
   * @param node node
   * @param parent parent (can be {@code null})
   * @return name
   */
  private byte[] nodeName(final GNode node, final GNode parent) {
    return nodeName(node.qname(), node.kind() == Kind.ELEMENT, parent, plan.marker);
  }

  /**
   * Returns a string representation of the name of the node.
   * @param qnm QName
   * @param element element flag
   * @param parent parent (can be {@code null})
   * @param marker attribute marker (can be {@code null})
   * @return name
   */
  private byte[] nodeName(final QNm qnm, final boolean element, final GNode parent,
      final String marker) {
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
    return shared.token(!element && marker != null ? Token.concat(marker, name) : name);
  }

  /**
   * Returns a list item.
   * @param node node
   * @return array
   * @throws QueryException query exception
   */
  private XQArray list(final GNode node) throws QueryException {
    final GNodeList children = PlanFn.children(Kind.ELEMENT, node);
    final ArrayBuilder ab = new ArrayBuilder(job, children.size());
    for(final GNode ch : children) {
      ab.add(apply(entry(ch), ch));
    }
    return ab.array();
  }

  /**
   * Returns a record item.
   * @param node node
   * @return array
   * @throws QueryException query exception
   */
  private XQMap record(final GNode node) throws QueryException {
    final MapBuilder map = attributes(node);
    final TokenObjectMap<GNodeList> cache = new TokenObjectMap<>();
    for(final GNode ch : PlanFn.children(Kind.ELEMENT, node)) {
      cache.computeIfAbsent(nodeName(ch, node), GNodeList::new).add(ch);
    }
    for(final byte[] name : cache) {
      final GNodeList children = cache.get(name);
      final PlanEntry pe = entry(children.get(0));
      if(pe.layout != PlanLayout.DEEP_SKIP) {
        final ArrayBuilder ab = new ArrayBuilder(job, children.size());
        for(final GNode ch : children) {
          ab.add(apply(pe, ch));
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
   * @param ignoreEmpty ignore empty text nodes
   * @return array
   * @throws QueryException query exception
   */
  private XQArray mixed(final GNode node, final boolean ignoreEmpty) throws QueryException {

    final ArrayBuilder ab = new ArrayBuilder(job);
    for(final GNode attr : PlanFn.children(Kind.ATTRIBUTE, node)) {
      ab.add(new MapBuilder().put(nodeName(attr, node), attr.string()).map());
    }
    for(final GNode child : node.childIter()) {
      final Item item = switch(child.kind()) {
        case COMMENT ->
          new MapBuilder().put(PlanFn.COMMENT, child.string()).map();
        case ELEMENT ->
          new MapBuilder().put(nodeName(child, node), apply(entry(child), child)).map();
        case PROCESSING_INSTRUCTION ->
          new MapBuilder().put(PlanFn.PI, new MapBuilder().put(PlanFn.TARGET, child.name()).
            put(PlanFn.DATA, child.string()).map()).map();
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
