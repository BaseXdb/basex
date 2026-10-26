package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.build.Parser;
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
  /** No namespace declarations. */
  private static final Atts NO_NSP = new Atts();
  /** Name of the xsi:nil attribute. */
  private static final QNm NIL = new QNm(token("xsi:nil"), QueryText.XSI_URI);
  /** Two dashes. */
  private static final byte[] DASHES = token("--");
  /** Layouts that are inferred from a value if the plan assigns no layout, in order of priority. */
  private static final PlanEntry[] INFERRED = entries(PlanLayout.SIMPLE, PlanLayout.SIMPLE_PLUS,
      PlanLayout.RECORD, PlanLayout.MIXED);

  /**
   * Content key and attribute marker of a map.
   * @param content content key
   * @param marker attribute marker
   */
  private record Keys(byte[] content, byte[] marker) { }

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
  /** Fallback plan entry (can be {@code null}). */
  private final PlanEntry fallback;
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
    this.options = new MainOptions(new FnParseXml.ParseXmlOptions(), options);
    this.info = info;
    plan = PlanFn.plan(eopts, shared, info);
    fallback = entry(QNm.EMPTY);
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
    element(qName(map.keyAt(0).string(info), true, null), map.valueAt(0), handler);
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
   * @param value element value
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void element(final QNm name, final Value value, final XmlHandler handler)
      throws QueryException, IOException {
    // the selected layout guarantees a single item
    final PlanEntry pe = layout(name, value);
    final Item item = item(value);
    final boolean nilled = isNull(item);
    // serialized XML layout: parse the string back into an element
    if(pe.layout == PlanLayout.XML && !nilled) {
      node(parse(item.string(info)), handler);
      return;
    }
    // attributes are collected (and the content is checked) before the children are added
    final ArrayList<FAttr> attributes = new ArrayList<>();
    if(nilled) attributes.add(nil());
    else content(name, item, pe, attributes, handler);
    open(name, attributes, handler);
    if(!nilled) content(name, item, pe, null, handler);
    close(handler);
  }

  /**
   * Processes the attributes or children represented by an element value.
   * @param name element name
   * @param item element value
   * @param pe plan entry with the selected layout
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void content(final QNm name, final Item item, final PlanEntry pe,
      final ArrayList<FAttr> attributes, final XmlHandler handler)
      throws QueryException, IOException {

    switch(pe.layout) {
      case SIMPLE -> {
        if(attributes == null) text(item, handler);
      }
      case EMPTY_PLUS, SIMPLE_PLUS, LIST_PLUS, RECORD -> {
        final XQMap map = (XQMap) item;
        final Keys keys = keys(map, false);
        final long size = map.structSize();
        for(long i = 0; i < size; i++) {
          object(name, pe, map.keyAt(i).string(info), keys, map.valueAt(i), attributes, handler);
        }
      }
      case LIST -> {
        if(attributes == null) {
          for(final Value member : ((XQArray) item).members()) element(pe.child, member, handler);
        }
      }
      case SEQUENCE, MIXED ->
        members(name, (XQArray) item, attributes, handler);
      default -> { }
    }
  }

  /**
   * Processes an entry of a map value (attribute, simple content, or child element).
   * @param parent parent element name
   * @param pe plan entry of the parent element
   * @param key formatted entry key
   * @param keys content key and attribute marker of the map
   * @param value entry value
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void object(final QNm parent, final PlanEntry pe, final byte[] key, final Keys keys,
      final Value value, final ArrayList<FAttr> attributes, final XmlHandler handler)
      throws QueryException, IOException {

    switch(classify(key, keys)) {
      case CONTENT_KEY -> {
        final Item item = single(value);
        if(isNull(item)) {
          if(attributes != null) attributes.add(nil());
        } else if(attributes == null) {
          text(item, handler);
        }
      }
      case ATTRIBUTE_KEY -> {
        if(attributes != null) attributes.add(attribute(key, keys.marker(), parent, value));
      }
      default -> {
        if(attributes != null) return;
        // child element(s); reserved keys are element keys here
        final QNm name = qName(key, true, parent);
        if(single(value) instanceof final XQArray array &&
            (pe.layout == PlanLayout.LIST_PLUS || repeated(name, array))) {
          for(final Value member : array.members()) element(name, member, handler);
        } else {
          element(name, value, handler);
        }
      }
    }
  }

  /**
   * Processes the members of an array value (sequence and mixed layout).
   * @param parent parent element name
   * @param array array
   * @param attributes attributes to be collected ({@code null}: add children)
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void members(final QNm parent, final XQArray array, final ArrayList<FAttr> attributes,
      final XmlHandler handler) throws QueryException, IOException {

    // attributes and the nilled marker must precede all other members
    boolean closed = false, text = false;
    for(final Value member : array.members()) {
      final Item item = item(member);
      if(item instanceof final XQMap map) {
        final byte[] key = map.keyAt(0).string(info);
        final Keys keys = keys(map, true);
        final Value value = map.valueAt(0);
        final Slot slot = classify(key, keys);
        if(attributes != null) {
          if(slot == Slot.ATTRIBUTE_KEY) {
            if(closed) throw MAP_TO_ELEMENT_X.get(info,
                "Attributes must precede the other array members.");
            attributes.add(attribute(key, keys.marker(), parent, value));
          } else if(slot == Slot.CONTENT_KEY) {
            if(!isNull(single(value))) throw MAP_TO_ELEMENT_X.get(info,
                "Content key in array requires null.");
            if(closed) throw MAP_TO_ELEMENT_X.get(info,
                "Nilled marker must follow the attributes.");
            attributes.add(nil());
            closed = true;
          } else {
            if(slot == Slot.COMMENT_KEY) comment(string(value));
            else if(slot == Slot.PI_KEY) pi(value);
            closed = true;
          }
        } else if(slot == Slot.COMMENT_KEY) {
          handler.comment(string(value));
        } else if(slot == Slot.PI_KEY) {
          final byte[][] pi = pi(value);
          pi(pi[0], pi[1], handler);
        } else if(slot == Slot.CHILD_KEY) {
          element(qName(key, true, parent), value, handler);
        }
        text = false;
      } else {
        // adjacent strings would yield coalescing text nodes: only a 'list' plan,
        // which supplies the child element name, can represent such an array faithfully
        if(attributes != null && text) throw MAP_TO_ELEMENT_X.get(info,
            "Adjacent atomic array members require a 'list' plan.");
        if(attributes == null) text(item, handler);
        closed = true;
        text = true;
      }
    }
  }

  /**
   * Checks the value of a comment.
   * @param data comment
   * @throws QueryException query exception
   */
  private void comment(final byte[] data) throws QueryException {
    if(contains(data, DASHES) || endsWith(data, '-')) {
      throw MAP_TO_ELEMENT_X.get(info, "Invalid comment content.");
    }
  }

  /**
   * Returns the target and data of a processing instruction.
   * @param value value: { "#target": ..., "#data": ... }
   * @return target and data
   * @throws QueryException query exception
   */
  private byte[][] pi(final Value value) throws QueryException {
    if(!(single(value) instanceof final XQMap map)) throw MAP_TO_ELEMENT_X.get(info,
        "Invalid processing-instruction structure.");
    final byte[] name = string(map.get(PlanFn.TARGET)), data = string(map.get(PlanFn.DATA));
    if(!XMLToken.isNCName(name) || eq(lc(name), XML)) {
      throw MAP_TO_ELEMENT_X.get(info, "Invalid processing-instruction target.");
    }
    if(contains(data, FPI.CLOSE)) throw MAP_TO_ELEMENT_X.get(info,
        "Invalid processing-instruction content.");
    return new byte[][] { name, data };
  }

  /**
   * Adds a processing instruction.
   * @param target target
   * @param data data
   * @param handler XML handler
   * @throws IOException I/O exception
   */
  private static void pi(final byte[] target, final byte[] data, final XmlHandler handler)
      throws IOException {
    handler.pi(data.length == 0 ? target : concat(target, cpToken(' '), data));
  }

  /**
   * Adds a text node for simple content.
   * @param item content item
   * @param handler XML handler
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void text(final Item item, final XmlHandler handler) throws QueryException, IOException {
    final byte[] text = atom(item);
    if(text.length != 0) handler.text(text);
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
  private void node(final GNode node, final XmlHandler handler) throws QueryException, IOException {
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
      case PROCESSING_INSTRUCTION -> pi(node.name(), node.string(), handler);
      default -> { }
    }
  }

  /**
   * Classifies a map key.
   * @param key formatted key
   * @param keys content key and attribute marker of the map
   * @return slot
   */
  private Slot classify(final byte[] key, final Keys keys) {
    if(eq(key, keys.content())) return Slot.CONTENT_KEY;
    if(eq(key, PlanFn.COMMENT.string())) return Slot.COMMENT_KEY;
    if(eq(key, PlanFn.PI.string())) return Slot.PI_KEY;
    return startsWith(key, keys.marker()) ? Slot.ATTRIBUTE_KEY : Slot.CHILD_KEY;
  }

  /**
   * Returns the content key and the attribute marker of a map.
   * @param map map
   * @param member whether the map is an array member
   * @return content key and attribute marker
   * @throws QueryException query exception
   */
  private Keys keys(final XQMap map, final boolean member) throws QueryException {
    // content key: the key with the most '#' characters prepended by the forward conversion
    final byte[] option = plan.content.string();
    byte[] content = option;
    final long size = map.structSize();
    if(!member) {
      for(long i = 0; i < size; i++) {
        final byte[] key = map.keyAt(i).string(info);
        if(key.length > content.length && endsWith(key, option) &&
            hashes(key, key.length - option.length)) content = key;
      }
    }
    // attribute marker: '@' if the forward conversion reverted to it because of name conflicts
    byte[] mrk = marker;
    if(!startsWith(marker, '@')) {
      for(long i = 0; i < size; i++) {
        final byte[] key = map.keyAt(i).string(info);
        if(startsWith(key, '@') && !eq(key, content)) {
          mrk = XMLToken.AT;
          break;
        }
      }
    }
    return new Keys(content, mrk);
  }

  /**
   * Checks if a key starts with the specified number of {@code #} characters.
   * @param key key
   * @param count number of characters
   * @return result of check
   */
  private static boolean hashes(final byte[] key, final int count) {
    for(int c = 0; c < count; c++) {
      if(key[c] != '#') return false;
    }
    return true;
  }

  /**
   * Creates an attribute node.
   * @param key formatted key, starting with the attribute marker
   * @param mrk attribute marker
   * @param parent name of the parent element
   * @param value attribute value
   * @return attribute
   * @throws QueryException query exception
   */
  private FAttr attribute(final byte[] key, final byte[] mrk, final QNm parent,
      final Value value) throws QueryException {
    final QNm name = qName(substring(key, mrk.length), false, parent);
    final Item item = single(value);
    if(isNull(item)) throw MAP_TO_ELEMENT_X.get(info, "Null is not allowed as attribute value.");
    return new FAttr(name, atom(item));
  }

  /**
   * Creates an {@code xsi:nil} attribute for a nilled element.
   * @return attribute
   */
  private static FAttr nil() {
    return new FAttr(NIL, TRUE);
  }

  /**
   * Returns the plan entry of an element; entries with ignored layouts are skipped.
   * @param name element name ({@link QNm#EMPTY} for the fallback entry)
   * @return entry, or {@code null} if the plan has no entry for this element
   */
  private PlanEntry entry(final QNm name) {
    final PlanEntry pe = plan.entries.get(name);
    return pe != null && !pe.attribute && pe.layout != PlanLayout.DEEP_SKIP &&
      pe.layout != PlanLayout.ERROR ? pe : null;
  }

  /**
   * Selects the layout of an element value.
   * @param name element name
   * @param value element value
   * @return plan entry with the selected layout
   * @throws QueryException query exception
   */
  private PlanEntry layout(final QNm name, final Value value) throws QueryException {
    final PlanEntry selected = select(name, value);
    if(selected != null) return selected;
    final PlanEntry pe = entry(name);
    if(pe != null || fallback != null) throw MAP_TO_ELEMENT_X.get(info,
        Util.info("Value of element '%' does not match layout '%'.", name.string(),
        (pe != null ? pe : fallback).layout));
    throw MAP_TO_ELEMENT_X.get(info, value.size() == 1 && value.itemAt(0) instanceof XQArray ?
      "Array members must be strings or single-entry maps, or a 'list' plan is required." :
      Util.info("No layout matches the value of element '%'.", name.string()));
  }

  /**
   * Returns the first candidate layout of an element name that a value matches: the plan entry
   * and the fallback entry, or the inferred layouts if the plan contains neither.
   * @param name element name
   * @param value element value
   * @return plan entry, or {@code null} if the value matches no candidate layout
   * @throws QueryException query exception
   */
  private PlanEntry select(final QNm name, final Value value) throws QueryException {
    final PlanEntry pe = entry(name);
    if(pe == null && fallback == null) {
      for(final PlanEntry inferred : INFERRED) {
        if(matches(name, value, inferred)) return inferred;
      }
      return null;
    }
    return pe != null && matches(name, value, pe) ? pe :
      fallback != null && matches(name, value, fallback) ? fallback : null;
  }

  /**
   * Checks if the value of an element matches a layout.
   * @param name element name
   * @param value value
   * @param pe plan entry
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean matches(final QNm name, final Value value, final PlanEntry pe)
      throws QueryException {
    final Item item = item(value);
    if(item == null) return false;
    if(isNull(item)) return true;
    return switch(pe.layout) {
      case EMPTY -> item.type.instanceOf(BasicType.STRING) && item.string(info).length == 0;
      case SIMPLE -> item.type.instanceOf(BasicType.ANY_ATOMIC_TYPE);
      case EMPTY_PLUS, SIMPLE_PLUS, LIST_PLUS, RECORD ->
        item instanceof final XQMap map && matches(name, map, pe);
      case LIST ->
        item instanceof final XQArray array && validMembers(pe.child, array);
      case SEQUENCE, MIXED -> {
        if(!(item instanceof final XQArray array)) yield false;
        for(final Value member : array.members()) {
          // single-entry maps, and strings in mixed layout
          final Item mi = item(member);
          if(mi instanceof final XQMap map) {
            if(map.structSize() != 1) yield false;
          } else if(pe.layout != PlanLayout.MIXED || mi == null ||
              !mi.type.instanceOf(BasicType.STRING)) {
            yield false;
          }
        }
        yield true;
      }
      case XML -> item.type.instanceOf(BasicType.STRING);
      default -> true;
    };
  }

  /**
   * Checks if the keys of a map match a layout.
   * @param name element name
   * @param map map
   * @param pe plan entry ({@code empty-plus}, {@code simple-plus}, {@code list-plus},
   *   {@code record})
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean matches(final QNm name, final XQMap map, final PlanEntry pe)
      throws QueryException {
    final PlanLayout layout = pe.layout;
    final Keys keys = keys(map, false);
    boolean content = false;
    int elements = 0;
    final long size = map.structSize();
    for(long i = 0; i < size; i++) {
      final byte[] key = map.keyAt(i).string(info);
      final Item item = item(map.valueAt(i));
      switch(classify(key, keys)) {
        case ATTRIBUTE_KEY -> { }
        case CONTENT_KEY -> {
          // only simple content may have a value other than null
          if(layout != PlanLayout.SIMPLE_PLUS && !isNull(item)) return false;
          content = true;
        }
        default -> {
          if(layout == PlanLayout.LIST_PLUS) {
            // a single element key, named after the plan's child, with an array value
            if(++elements > 1 || !(item instanceof XQArray) ||
                !qName(key, true, name).eq(pe.child)) return false;
          } else if(layout != PlanLayout.RECORD) {
            return false;
          }
        }
      }
    }
    return layout != PlanLayout.SIMPLE_PLUS || content;
  }

  /**
   * Checks if an array value of a map entry represents repeated child elements.
   * @param name name of the child elements
   * @param array array
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean repeated(final QNm name, final XQArray array) throws QueryException {
    return array.structSize() != 0 && validMembers(name, array);
  }

  /**
   * Checks if all members of an array match an element name.
   * @param name element name
   * @param array array
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean validMembers(final QNm name, final XQArray array) throws QueryException {
    for(final Value member : array.members()) {
      if(select(name, member) == null) return false;
    }
    return true;
  }

  /**
   * Returns plan entries for the specified layouts.
   * @param layouts layouts
   * @return plan entries
   */
  private static PlanEntry[] entries(final PlanLayout... layouts) {
    final int ll = layouts.length;
    final PlanEntry[] entries = new PlanEntry[ll];
    for(int l = 0; l < ll; l++) {
      entries[l] = new PlanEntry();
      entries[l].layout = layouts[l];
    }
    return entries;
  }

  /**
   * Decodes a formatted name into a QName.
   * @param key formatted name
   * @param element element flag (false for attributes)
   * @param parent name of parent element (can be {@code null})
   * @return QName
   * @throws QueryException query exception
   */
  private QNm qName(final byte[] key, final boolean element, final QNm parent)
      throws QueryException {

    final byte[] name = plan.escape ? PlanFn.encode(key, plan.name == NameFormat.LEXICAL) : key;
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
        qnm.uri().length == 0 && qnm.eqLocal(XMLNS))) {
      throw MAP_TO_ELEMENT_X.get(info, "Namespace declaration is not allowed as attribute.");
    }
    // reject elements in the xmlns namespace
    if(element && eq(qnm.uri(), QueryText.XMLNS_URI)) {
      throw MAP_TO_ELEMENT_X.get(info, "Element name in the xmlns namespace is not allowed.");
    }

    // synthesize a prefix for namespaced attributes (forward conversion loses prefixes);
    // names in the xml namespace are always bound to the xml prefix
    final boolean xml = eq(qnm.uri(), QueryText.XML_URI);
    if((!element && qnm.uri().length != 0 || xml) && !qnm.hasPrefix()) {
      final byte[] prefix = xml ? XML : shared.prefix(qnm.uri());
      qnm = shared.qName(concat(prefix, cpToken(':'), qnm.local()), qnm.uri());
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
      final IO io = new IOContent(xml);
      final DBNode doc = new DBNode(options.get(MainOptions.INTPARSE) ?
        new XMLParser(io, options, false) : Parser.xmlParser(io, options));
      GNode element = null;
      for(final GNode child : doc.childIter()) {
        if(child.kind() == Kind.ELEMENT) {
          if(element != null) throw MAP_TO_ELEMENT_X.get(info,
              "Serialized XML must contain a single element.");
          element = child;
        } else if(!(child.kind() == Kind.TEXT && ws(child.string()))) {
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
    return atom(single(value));
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
   * @return item (the zero-length string if the value is empty)
   * @throws QueryException query exception
   */
  private Item single(final Value value) throws QueryException {
    final Item item = item(value);
    if(item == null) throw MAP_TO_ELEMENT_X.get(info,
        "Value with more than one item is not allowed.");
    return item;
  }

  /**
   * Returns the item of a value; the empty sequence is treated as the zero-length string.
   * @param value value
   * @return item, or {@code null} if the value has more than one item
   */
  private static Item item(final Value value) {
    return value.isEmpty() ? Str.EMPTY : value.size() == 1 ? value.itemAt(0) : null;
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
