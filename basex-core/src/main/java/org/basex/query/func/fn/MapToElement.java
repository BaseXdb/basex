package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.fn.PlanFn.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Converts maps to elements and sends the result to an XML handler.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapToElement {
  /** The fn:null QName, representing a nilled element. */
  public static final QNm NULL = new QNm(token("null"), QueryText.FN_URI);
  /** Resolves the predefined xml prefix. */
  public static final UnaryOperator<byte[]> XML_PREFIX =
      prefix -> eq(prefix, XML) ? QueryText.XML_URI : null;
  /** No namespace declarations. */
  private static final Atts NO_NSP = new Atts();

  /** Role that a map key plays in the reconstructed element. */
  private enum Slot {
    /** Attribute.               */ ATTRIBUTE_KEY,
    /** Simple content.          */ CONTENT_KEY,
    /** Comment.                 */ COMMENT_KEY,
    /** Processing instruction.  */ PI_KEY,
    /** Child element.           */ CHILD_KEY
  }

  /** Conversion plan. */
  private final Plan plan;
  /** Attribute marker. */
  private final byte[] marker;
  /** Resolves the URI of a namespace prefix. */
  private final UnaryOperator<byte[]> uris;
  /** Shared data references. */
  private final SharedData shared;
  /** Options for parsing serialized XML. */
  private final MainOptions options;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Namespace declarations of the open elements. */
  private final ArrayDeque<Atts> scopes = new ArrayDeque<>();
  /** Number of open elements with namespace declarations. */
  private int declared;

  /**
   * Constructor.
   * @param eopts options for converting between elements and maps
   * @param uris resolves the URI of a namespace prefix ({@code null} if the prefix is unbound)
   * @param shared shared data references
   * @param options main options
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public MapToElement(final ElementsOptions eopts, final UnaryOperator<byte[]> uris,
      final SharedData shared, final MainOptions options, final InputInfo info)
      throws QueryException {
    this.uris = uris;
    this.shared = shared;
    this.options = new MainOptions(options);
    this.info = info;
    plan = PlanFn.plan(eopts, uris, shared, info);
    if(plan.marker == null || plan.marker.isEmpty()) throw MAP_TO_ELEMENT_X.get(info,
        "Empty attribute marker is not allowed.");
    marker = token(plan.marker);
  }

  /**
   * Converts a single-entry map to an element.
   * @param value map
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void convert(final Value value, final XmlHandler handler)
      throws QueryException, IOException {
    if(!(value instanceof final XQMap map)) throw typeError(value, Types.MAP, info);
    if(map.structSize() != 1) throw MAP_TO_ELEMENT_X.get(info, "Single-entry map expected.");
    final Item key = map.keys().itemAt(0);
    final QNm name = qName(key.string(info), true, null);
    element(name, PlanFn.entry(name, plan), map.get(key), handler);
  }

  /**
   * Converts a parsed JSON value to an element.
   * @param item parsed value
   * @param root name of the root element (can be {@code null})
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void convert(final Item item, final String root, final XmlHandler handler)
      throws QueryException, IOException {
    final Item map = root != null ? XQMap.get(Str.get(root), item) : item;
    if(!(map instanceof XQMap)) throw MAP_TO_ELEMENT_X.get(info, "Single-entry map expected.");
    convert(map, handler);
  }

  /**
   * Converts an element with the specified name.
   * @param name element name
   * @param pe plan entry (can be {@code null})
   * @param value element content
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void element(final QNm name, final PlanEntry pe, final Value value,
      final XmlHandler handler) throws QueryException, IOException {
    // serialized XML layout: parse the string back into an element
    if(pe != null && pe.layout == PlanLayout.XML) {
      node(parse(string(value)), handler);
      return;
    }
    // attributes are collected (and the content is checked) before the children are added
    final ArrayList<FAttr> attributes = new ArrayList<>();
    content(name, value, pe, attributes, handler);
    open(name, attributes, handler);
    content(name, value, pe, null, handler);
    close(handler);
  }

  /**
   * Processes the attributes or children represented by an element content value.
   * @param name element name
   * @param value content value
   * @param pe plan entry (can be {@code null})
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void content(final QNm name, final Value value, final PlanEntry pe,
      final ArrayList<FAttr> attributes, final XmlHandler handler)
      throws QueryException, IOException {

    final Item item = single(value);
    if(item instanceof final XQMap map) {
      // object: attributes, simple content, and child elements
      final Value keys = map.keys();
      if(attributes != null) {
        boolean simple = false, child = false;
        for(final Item k : keys) {
          final Slot slot = classify(k.string(info));
          if(slot == Slot.CONTENT_KEY) simple = true;
          else if(slot != Slot.ATTRIBUTE_KEY) child = true;
        }
        // simple content and child elements cannot coexist in any layout
        if(simple && child) throw MAP_TO_ELEMENT_X.get(info,
            "Simple content cannot be combined with child elements.");
      }
      for(final Item k : keys) object(name, k.string(info), map.get(k), attributes, handler);
    } else if(item instanceof final XQArray array) {
      if(list(pe)) {
        // list layout: children are named after the plan's child entry
        if(attributes != null) {
          if(pe.child == null) throw MAP_TO_ELEMENT_X.get(info,
              "Missing child name for list layout.");
        } else {
          final PlanEntry cpe = PlanFn.entry(pe.child, plan);
          for(final Value member : array.members()) element(pe.child, cpe, member, handler);
        }
      } else {
        // sequence / mixed layout
        boolean text = false;
        for(final Value member : array.members()) {
          final Item mi = single(member);
          if(mi == null) continue;
          if(attributes != null) {
            // adjacent atomic members would yield coalescing text nodes: only a 'list' plan,
            // which supplies the child element name, can represent such an array faithfully
            final boolean t = !(mi instanceof XQMap) && !(mi instanceof XQArray) && !isNull(mi);
            if(t && text) throw MAP_TO_ELEMENT_X.get(info,
                "Adjacent atomic array members require a 'list' plan.");
            text = t;
          }
          sequence(name, mi, attributes, handler);
        }
      }
    } else if(item != null) {
      simple(item, attributes, handler);
    }
  }

  /**
   * Processes an entry of an object value (attribute, simple content, or child element).
   * @param parent parent element name
   * @param key formatted entry key
   * @param value entry value
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void object(final QNm parent, final byte[] key, final Value value,
      final ArrayList<FAttr> attributes, final XmlHandler handler)
      throws QueryException, IOException {

    switch(classify(key)) {
      case CONTENT_KEY -> {
        final Item item = single(value);
        if(item != null) simple(item, attributes, handler);
      }
      case ATTRIBUTE_KEY -> {
        if(attributes != null) attributes.add(attribute(key, parent, value));
      }
      default -> {
        if(attributes != null) return;
        // child element(s); reserved keys are invalid here and fail on the element name
        final QNm name = qName(key, true, parent);
        final PlanEntry pe = PlanFn.entry(name, plan);
        // an array is a repeated child element, unless the plan assigns a list layout to the name
        if(single(value) instanceof final XQArray array && !list(pe)) {
          for(final Value member : array.members()) element(name, pe, member, handler);
        } else {
          element(name, pe, value, handler);
        }
      }
    }
  }

  /**
   * Processes a member of a sequence/mixed array value.
   * @param parent parent element name
   * @param item array member
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void sequence(final QNm parent, final Item item, final ArrayList<FAttr> attributes,
      final XmlHandler handler) throws QueryException, IOException {

    if(item instanceof final XQMap map) {
      // processing instruction: { "#processing-instruction": { "#target": ..., "#data": ... } }
      final Value pi = map.get(PlanFn.PI);
      if(!pi.isEmpty()) {
        if(!(single(pi) instanceof final XQMap target)) throw MAP_TO_ELEMENT_X.get(info,
            "Invalid processing-instruction structure.");
        final byte[] name = string(target.get(PlanFn.TARGET));
        final byte[] data = string(target.get(PlanFn.DATA));
        if(attributes != null) {
          if(!XMLToken.isNCName(name) || eq(lc(name), token("xml"))) {
            throw MAP_TO_ELEMENT_X.get(info, "Invalid processing-instruction target.");
          }
          if(contains(data, FPI.CLOSE)) throw MAP_TO_ELEMENT_X.get(info,
              "Invalid processing-instruction content.");
        } else {
          handler.pi(data.length == 0 ? name : concat(name, cpToken(' '), data));
        }
        return;
      }
      // remaining members must be single-entry maps
      if(map.structSize() != 1) throw MAP_TO_ELEMENT_X.get(info,
          "Single-entry map expected in array.");
      final Item k = map.keys().itemAt(0);
      final byte[] key = k.string(info);
      final Value value = map.get(k);
      final Slot slot = classify(key);
      if(slot == Slot.COMMENT_KEY) {
        final byte[] data = string(value);
        if(attributes == null) {
          handler.comment(data);
        } else if(contains(data, token("--")) || endsWith(data, '-')) {
          throw MAP_TO_ELEMENT_X.get(info, "Invalid comment content.");
        }
      } else if(slot == Slot.CONTENT_KEY && !value.isEmpty() && isNull(value.itemAt(0))) {
        // nilled marker
        if(attributes != null) attributes.add(nil());
      } else if(slot == Slot.ATTRIBUTE_KEY) {
        if(attributes != null) attributes.add(attribute(key, parent, value));
      } else if(attributes == null) {
        final QNm name = qName(key, true, parent);
        element(name, PlanFn.entry(name, plan), value, handler);
      }
    } else if(attributes == null) {
      // text node
      handler.text(atom(item));
    } else if(isNull(item)) {
      throw MAP_TO_ELEMENT_X.get(info, "Unexpected null in array.");
    } else if(item instanceof XQArray) {
      throw MAP_TO_ELEMENT_X.get(info, "Unexpected nested array.");
    }
  }

  /**
   * Processes simple content: a nilled marker or a text node.
   * @param item content item
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void simple(final Item item, final ArrayList<FAttr> attributes,
      final XmlHandler handler) throws QueryException, IOException {
    if(isNull(item)) {
      if(attributes != null) attributes.add(nil());
    } else if(attributes == null) {
      handler.text(atom(item));
    }
  }

  /**
   * Opens an element and declares the namespaces required by its name and attributes.
   * @param name element name
   * @param attributes attributes
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void open(final QNm name, final ArrayList<FAttr> attributes,
      final XmlHandler handler) throws QueryException, IOException {

    // namespaces of the element name and its attributes
    final Atts inscope = new Atts();
    final byte[] prefix = name.prefix();
    if(!eq(prefix, XML) && name.hasURI()) inscope.add(prefix, name.uri());

    final int as = attributes.size();
    final Atts atts = new Atts(as);
    final QNmSet names = as > 1 ? new QNmSet() : null;
    for(final FAttr attr : attributes) {
      QNm qnm = attr.qname();
      if(names != null && !names.add(qnm)) throw MAP_TO_ELEMENT_X.get(info,
          "Duplicate attribute.");
      final byte[] ap = qnm.prefix();
      if(ap.length != 0 && qnm.hasURI() && !eq(ap, XML)) {
        // a prefix that is already bound to another URI is replaced
        final byte[] npref = Constr.addNS(inscope, ap, qnm.uri(), shared);
        if(npref != null) qnm = shared.qName(concat(npref, cpToken(':'), qnm.local()), qnm.uri());
      }
      atts.add(qnm.string(), attr.string());
    }

    // declare namespaces that are not in scope yet, undeclare the default namespace
    Atts nsp = NO_NSP;
    final int is = inscope.size();
    for(int i = 0; i < is; i++) {
      final byte[] uri = scope(inscope.name(i));
      if(uri == null || !eq(uri, inscope.value(i))) {
        if(nsp == NO_NSP) nsp = new Atts();
        nsp.add(inscope.name(i), inscope.value(i));
      }
    }
    if(prefix.length == 0 && !name.hasURI() && scope(EMPTY).length != 0) {
      if(nsp == NO_NSP) nsp = new Atts();
      nsp.add(EMPTY, EMPTY);
    }

    handler.openElem(name.string(), atts, nsp);
    if(nsp != NO_NSP) declared++;
    scopes.push(nsp);
  }

  /**
   * Closes an element.
   * @param handler XML handler
   * @throws IOException I/O exception
   */
  private void close(final XmlHandler handler) throws IOException {
    handler.closeElem();
    if(scopes.pop() != NO_NSP) declared--;
  }

  /**
   * Returns the URI that is bound to a prefix by the open elements.
   * @param prefix prefix
   * @return URI, or {@code null} if the prefix is not bound
   */
  private byte[] scope(final byte[] prefix) {
    if(declared != 0) {
      for(final Atts nsp : scopes) {
        final byte[] uri = nsp.value(prefix);
        if(uri != null) return uri;
      }
    }
    return prefix.length == 0 ? EMPTY : null;
  }

  /**
   * Sends a parsed node to the handler.
   * @param node node
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void node(final GNode node, final XmlHandler handler)
      throws QueryException, IOException {
    switch(node.kind()) {
      case ELEMENT -> {
        final ArrayList<FAttr> attributes = new ArrayList<>();
        for(final GNode attr : node.attributeIter()) {
          attributes.add(new FAttr(attr.qname(), attr.string()));
        }
        open(node.qname(), attributes, handler);
        for(final GNode child : node.childIter()) node(child, handler);
        close(handler);
      }
      case TEXT -> handler.text(node.string());
      case COMMENT -> handler.comment(node.string());
      case PROCESSING_INSTRUCTION -> {
        final byte[] data = node.string();
        handler.pi(data.length == 0 ? node.name() : concat(node.name(), cpToken(' '), data));
      }
      default -> { }
    }
  }

  /**
   * Classifies a map key. The content key and reserved keys ({@code #comment},
   * {@code #processing-instruction}) take precedence over the attribute marker.
   * @param key formatted key
   * @return slot
   */
  private Slot classify(final byte[] key) {
    if(eq(key, plan.content.string())) return Slot.CONTENT_KEY;
    if(eq(key, PlanFn.COMMENT.string())) return Slot.COMMENT_KEY;
    if(eq(key, PlanFn.PI.string())) return Slot.PI_KEY;
    return startsWith(key, marker) ? Slot.ATTRIBUTE_KEY : Slot.CHILD_KEY;
  }

  /**
   * Creates an attribute node.
   * @param key formatted key, starting with the attribute marker
   * @param parent name of the parent element
   * @param value attribute value
   * @return attribute
   * @throws QueryException query exception
   */
  private FAttr attribute(final byte[] key, final QNm parent, final Value value)
      throws QueryException {
    final QNm name = qName(substring(key, marker.length), false, parent);
    final Item item = single(value);
    if(isNull(item)) throw MAP_TO_ELEMENT_X.get(info, "Null is not allowed as attribute value.");
    return new FAttr(name, item == null ? EMPTY : atom(item));
  }

  /**
   * Creates an {@code xsi:nil} attribute for a nilled element.
   * @return attribute
   */
  private static FAttr nil() {
    return new FAttr(new QNm(token("xsi:nil"), QueryText.XSI_URI), TRUE);
  }

  /**
   * Checks if a plan entry assigns a list layout.
   * @param pe plan entry (can be {@code null})
   * @return result of check
   */
  private static boolean list(final PlanEntry pe) {
    return pe != null && (pe.layout == PlanLayout.LIST || pe.layout == PlanLayout.LIST_PLUS);
  }

  /**
   * Decodes a formatted name into a QName.
   * @param name formatted name
   * @param element element flag (false for attributes)
   * @param parent name of parent element (can be {@code null})
   * @return QName
   * @throws QueryException query exception
   */
  private QNm qName(final byte[] name, final boolean element, final QNm parent)
      throws QueryException {

    QNm qnm = null;
    if(name.length > 1 && name[0] == 'Q' && name[1] == '{') {
      // expanded name Q{uri}local
      final byte[][] parsed = QNm.parseExpanded(name, false);
      if(parsed != null) qnm = shared.qName(parsed[0], parsed[1]);
    } else if(indexOf(name, ':') != -1) {
      // lexical name with prefix: the prefix must resolve to a namespace
      qnm = shared.parseQName(name, true, uris);
      if(qnm != null && qnm.uri().length == 0) throw MAP_TO_ELEMENT_X.get(info,
          "Unbound namespace prefix.");
    } else if(XMLToken.isNCName(name)) {
      // bare local name: inherit parent namespace for descendant elements (default format)
      final byte[] uri = element && plan.name == NameFormat.DEFAULT && parent != null &&
        parent.uri().length != 0 ? parent.uri() : null;
      qnm = shared.qName(name, uri);
    }
    if(qnm == null) throw MAP_TO_ELEMENT_X.get(info, "Invalid element or attribute name.");

    // reject namespace declarations disguised as attributes (xmlns, xmlns:*)
    if(!element && (eq(qnm.uri(), QueryText.XMLNS_URI) ||
        qnm.uri().length == 0 && eq(qnm.local(), token("xmlns")))) {
      throw MAP_TO_ELEMENT_X.get(info, "Namespace declaration is not allowed as attribute.");
    }

    // synthesize a prefix for namespaced attributes (forward conversion loses prefixes)
    if(!element && qnm.uri().length != 0 && !qnm.hasPrefix() &&
        !eq(qnm.uri(), QueryText.XML_URI)) {
      qnm = shared.qName(concat(token("ns:"), qnm.local()), qnm.uri());
    }
    return qnm;
  }

  /**
   * Parses serialized XML into an element (xml layout).
   * @param xml serialized XML
   * @return element
   * @throws QueryException query exception
   */
  private GNode parse(final byte[] xml) throws QueryException {
    try {
      final DBNode doc = new DBNode(new XMLParser(new IOContent(xml), options, true));
      GNode element = null;
      for(final GNode child : doc.childIter()) {
        if(child.kind() == Kind.ELEMENT) {
          if(element != null) throw MAP_TO_ELEMENT_X.get(info,
              "Serialized XML must contain a single element.");
          element = child;
        } else if(!(child.kind() == Kind.TEXT && normalize(child.string()).length == 0)) {
          // reject extra content (more elements, non-whitespace text, comments, PIs)
          throw MAP_TO_ELEMENT_X.get(info, "Serialized XML must contain a single element.");
        }
      }
      if(element == null) throw MAP_TO_ELEMENT_X.get(info, "No element in serialized XML.");
      return element;
    } catch(final IOException ex) {
      throw MAP_TO_ELEMENT_X.get(info, ex);
    }
  }

  /**
   * Returns the string value of a (zero-or-one item) value.
   * @param value value
   * @return string
   * @throws QueryException query exception
   */
  private byte[] string(final Value value) throws QueryException {
    final Item item = single(value);
    return item == null ? EMPTY : atom(item);
  }

  /**
   * Returns the string value of an atomic item, rejecting nodes and function items.
   * @param item item
   * @return string
   * @throws QueryException query exception
   */
  private byte[] atom(final Item item) throws QueryException {
    if(!item.type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) throw MAP_TO_ELEMENT_X.get(info,
        "Atomic value expected.");
    return item.string(info);
  }

  /**
   * Returns the single item of a value, rejecting sequences of more than one item.
   * @param value value
   * @return item, or {@code null} if the value is empty
   * @throws QueryException query exception
   */
  private Item single(final Value value) throws QueryException {
    if(value.size() > 1) throw MAP_TO_ELEMENT_X.get(info,
        "Value with more than one item is not allowed.");
    return value.isEmpty() ? null : value.itemAt(0);
  }

  /**
   * Checks if an item is the {@code fn:null} marker.
   * @param item item (can be {@code null})
   * @return result of check
   */
  private static boolean isNull(final Item item) {
    return item instanceof final QNm qnm && NULL.eq(qnm);
  }
}
