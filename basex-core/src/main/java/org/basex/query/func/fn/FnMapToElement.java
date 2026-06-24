package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnMapToElement extends PlanFn {
  /** The fn:null QName, representing a nilled element. */
  private static final QNm NULL = new QNm(token("null"), QueryText.FN_URI);
  /** Target key (for processing instructions, as defined by the specification). */
  private static final Str TARGET = Str.get("#target");

  /** Role that a map key plays in the reconstructed element. */
  private enum Slot {
    /** Attribute.               */ ATTRIBUTE_KEY,
    /** Simple content.          */ CONTENT_KEY,
    /** Comment.                 */ COMMENT_KEY,
    /** Processing instruction.  */ PI_KEY,
    /** Child element.           */ CHILD_KEY
  }

  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final ElementsOptions options = toOptions(arg(1), new ElementsOptions(), qc);
    if(input.isEmpty()) return Empty.VALUE;

    final Plan plan = buildPlan(options, qc);
    if(plan.marker == null || plan.marker.isEmpty()) throw MAP_TO_ELEMENT_X.get(info,
        "Empty attribute marker is not allowed.");

    final XQMap map = toMap(input, qc);
    if(map.structSize() != 1) throw MAP_TO_ELEMENT_X.get(info, "Single-entry map expected.");
    final Item key = map.keys().itemAt(0);
    return element(key.string(info), map.get(key), null, plan, qc);
  }

  /**
   * Reconstructs an element from a key/value pair.
   * @param key formatted element name
   * @param value element content
   * @param parent name of parent element (can be {@code null})
   * @param plan conversion plan
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  private XNode element(final byte[] key, final Value value, final QNm parent, final Plan plan,
      final QueryContext qc) throws QueryException {
    return element(qName(key, true, parent, plan, qc), value, plan, qc);
  }

  /**
   * Reconstructs an element with the specified name.
   * @param name element name
   * @param value element content
   * @param plan conversion plan
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  private XNode element(final QNm name, final Value value, final Plan plan, final QueryContext qc)
      throws QueryException {
    final PlanEntry pe = plan.entries.get(name);
    // serialized XML layout: parse the string back into an element
    if(pe != null && pe.layout == PlanLayout.XML) return parse(string(value), qc);

    // collect attributes and children
    final ValueBuilder attributes = new ValueBuilder(qc), children = new ValueBuilder(qc);
    content(name, value, pe, plan, attributes, children, qc);

    // build element via the standard node constructor (computes namespaces)
    final FBuilder elem = FElem.build(name);
    final ValueBuilder content = new ValueBuilder(qc);
    content.add(attributes.value());
    content.add(children.value());
    final Constr constr = new Constr(elem, info, qc).add(content.value());
    if(constr.errAtt != null) throw MAP_TO_ELEMENT_X.get(info, "Attribute after content.");
    if(constr.duplAtt != null) throw MAP_TO_ELEMENT_X.get(info, "Duplicate attribute.");
    constr.namespaces(new Atts(), name);
    return elem.finish();
  }

  /**
   * Adds the attributes and children represented by an element content value.
   * @param name element name
   * @param value content value
   * @param pe plan entry (can be {@code null})
   * @param plan conversion plan
   * @param attributes attributes to be enriched
   * @param children children to be enriched
   * @param qc query context
   * @throws QueryException query exception
   */
  private void content(final QNm name, final Value value, final PlanEntry pe, final Plan plan,
      final ValueBuilder attributes, final ValueBuilder children, final QueryContext qc)
      throws QueryException {

    final Item item = single(value);
    if(item == null) {
      // empty content
    } else if(item instanceof final XQMap map) {
      // object: attributes, simple content, and child elements
      boolean simple = false, child = false;
      for(final Item k : map.keys()) {
        final Slot slot = classify(k.string(info), plan);
        if(slot == Slot.CONTENT_KEY) simple = true;
        else if(slot != Slot.ATTRIBUTE_KEY) child = true;
      }
      // simple content and child elements cannot coexist in any layout
      if(simple && child) throw MAP_TO_ELEMENT_X.get(info,
          "Simple content cannot be combined with child elements.");
      map.forEach((k, v) -> object(name, k.string(info), v, plan, attributes, children, qc));
    } else if(item instanceof final XQArray array) {
      if(pe != null && (pe.layout == PlanLayout.LIST || pe.layout == PlanLayout.LIST_PLUS)) {
        // list layout: children are named after the plan's child entry
        if(pe.child == null) throw MAP_TO_ELEMENT_X.get(info,
            "Missing child name for list layout.");
        for(final Value member : array.members()) {
          children.add(element(pe.child, member, plan, qc));
        }
      } else {
        // sequence / mixed layout
        boolean text = false;
        for(final Value member : array.members()) {
          final Item mi = single(member);
          if(mi == null) continue;
          // adjacent atomic members would yield coalescing text nodes: only a 'list' plan,
          // which supplies the child element name, can represent such an array faithfully
          final boolean t = !(mi instanceof XQMap) && !(mi instanceof XQArray) && !isNull(mi);
          if(t && text) throw MAP_TO_ELEMENT_X.get(info,
              "Adjacent atomic array members require a 'list' plan.");
          text = t;
          sequence(name, mi, plan, attributes, children, qc);
        }
      }
    } else if(isNull(item)) {
      // nilled element
      attributes.add(nil());
    } else {
      // simple content
      children.add(new FTxt(atom(item)));
    }
  }

  /**
   * Processes an entry of an object value (attribute, simple content, or child element).
   * @param parent parent element name
   * @param key formatted entry key
   * @param value entry value
   * @param plan conversion plan
   * @param attributes attributes to be enriched
   * @param children children to be enriched
   * @param qc query context
   * @throws QueryException query exception
   */
  private void object(final QNm parent, final byte[] key, final Value value, final Plan plan,
      final ValueBuilder attributes, final ValueBuilder children, final QueryContext qc)
      throws QueryException {

    switch(classify(key, plan)) {
      case CONTENT_KEY -> {
        // simple content (or nilled marker)
        final Item item = single(value);
        if(item != null) {
          if(isNull(item)) attributes.add(nil());
          else children.add(new FTxt(atom(item)));
        }
      }
      case ATTRIBUTE_KEY -> attributes.add(attribute(
          qName(substring(key, token(plan.marker).length), false, parent, plan, qc), value));
      default -> {
        // child element(s); reserved keys are invalid here and fail on the element name
        final QNm name = qName(key, true, parent, plan, qc);
        final PlanEntry pe = plan.entries.get(name);
        final Item item = single(value);
        // an array is a repeated child element, unless the plan assigns a list layout to the name
        if(item instanceof final XQArray array && (pe == null ||
            pe.layout != PlanLayout.LIST && pe.layout != PlanLayout.LIST_PLUS)) {
          for(final Value member : array.members()) {
            children.add(element(name, member, plan, qc));
          }
        } else {
          children.add(element(name, value, plan, qc));
        }
      }
    }
  }

  /**
   * Processes a member of a sequence/mixed array value.
   * @param parent parent element name
   * @param item array member
   * @param plan conversion plan
   * @param attributes attributes to be enriched
   * @param children children to be enriched
   * @param qc query context
   * @throws QueryException query exception
   */
  private void sequence(final QNm parent, final Item item, final Plan plan,
      final ValueBuilder attributes, final ValueBuilder children, final QueryContext qc)
      throws QueryException {

    if(item instanceof final XQMap map) {
      // processing instruction: { "#processing-instruction": ..., "#data": ... }
      final Value pi = map.get(PI);
      if(!pi.isEmpty()) {
        final Item target = pi.itemAt(0);
        final byte[] name = target instanceof final XQMap tm ? string(tm.get(TARGET)) :
          string(pi);
        final byte[] data = target instanceof final XQMap tm ? string(tm.get(DATA)) :
          string(map.get(DATA));
        if(!XMLToken.isNCName(name) || eq(lc(name), token("xml"))) throw MAP_TO_ELEMENT_X.get(
            info, "Invalid processing-instruction target.");
        if(contains(data, FPI.CLOSE)) throw MAP_TO_ELEMENT_X.get(info,
            "Invalid processing-instruction content.");
        children.add(new FPI(new QNm(name), data));
        return;
      }
      // remaining members must be single-entry maps
      if(map.structSize() != 1) throw MAP_TO_ELEMENT_X.get(info,
          "Single-entry map expected in array.");
      final Item k = map.keys().itemAt(0);
      final byte[] key = k.string(info);
      final Value value = map.get(k);
      final Slot slot = classify(key, plan);
      if(slot == Slot.COMMENT_KEY) {
        // comment
        final byte[] data = string(value);
        if(contains(data, token("--")) || endsWith(data, '-')) throw MAP_TO_ELEMENT_X.get(info,
            "Invalid comment content.");
        children.add(new FComm(data));
      } else if(slot == Slot.CONTENT_KEY && !value.isEmpty() && isNull(value.itemAt(0))) {
        // nilled marker
        attributes.add(nil());
      } else if(slot == Slot.ATTRIBUTE_KEY) {
        // attribute
        attributes.add(attribute(qName(substring(key, token(plan.marker).length), false, parent,
            plan, qc), value));
      } else {
        // child element
        children.add(element(key, value, parent, plan, qc));
      }
    } else if(isNull(item)) {
      throw MAP_TO_ELEMENT_X.get(info, "Unexpected null in array.");
    } else if(item instanceof XQArray) {
      throw MAP_TO_ELEMENT_X.get(info, "Unexpected nested array.");
    } else {
      // text node
      children.add(new FTxt(atom(item)));
    }
  }

  /**
   * Classifies a map key. The content key and reserved keys ({@code #comment},
   * {@code #processing-instruction}) take precedence over the attribute marker.
   * @param key formatted key
   * @param plan conversion plan
   * @return slot
   */
  private Slot classify(final byte[] key, final Plan plan) {
    if(eq(key, plan.content.string())) return Slot.CONTENT_KEY;
    if(eq(key, COMMENT.string())) return Slot.COMMENT_KEY;
    if(eq(key, PI.string())) return Slot.PI_KEY;
    final byte[] marker = token(plan.marker);
    return marker.length != 0 && startsWith(key, marker) ? Slot.ATTRIBUTE_KEY : Slot.CHILD_KEY;
  }

  /**
   * Creates an attribute node.
   * @param name attribute name
   * @param value attribute value
   * @return attribute
   * @throws QueryException query exception
   */
  private FAttr attribute(final QNm name, final Value value) throws QueryException {
    final Item item = single(value);
    if(item != null && isNull(item)) throw MAP_TO_ELEMENT_X.get(info,
        "Null is not allowed as attribute value.");
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
   * Decodes a formatted name into a QName.
   * @param name formatted name
   * @param element element flag (false for attributes)
   * @param parent name of parent element (can be {@code null})
   * @param plan conversion plan
   * @param qc query context
   * @return QName
   * @throws QueryException query exception
   */
  private QNm qName(final byte[] name, final boolean element, final QNm parent, final Plan plan,
      final QueryContext qc) throws QueryException {

    QNm qnm = null;
    if(name.length > 1 && name[0] == 'Q' && name[1] == '{') {
      // expanded name Q{uri}local
      final byte[][] parsed = QNm.parseExpanded(name, false);
      if(parsed != null) qnm = qc.shared.qName(parsed[0], parsed[1]);
    } else if(indexOf(name, ':') != -1) {
      // lexical name with prefix: the prefix must resolve to a namespace
      qnm = qc.shared.parseQName(name, true, qc, sc());
      if(qnm != null && qnm.uri().length == 0) throw MAP_TO_ELEMENT_X.get(info,
          "Unbound namespace prefix.");
    } else if(XMLToken.isNCName(name)) {
      // bare local name: inherit parent namespace for descendant elements (default format)
      final byte[] uri = element && plan.name == NameFormat.DEFAULT && parent != null &&
        parent.uri().length != 0 ? parent.uri() : null;
      qnm = qc.shared.qName(name, uri);
    }
    if(qnm == null) throw MAP_TO_ELEMENT_X.get(info, "Invalid element or attribute name.");

    // reject namespace declarations disguised as attributes (xmlns, xmlns:*)
    if(!element && (eq(qnm.uri(), QueryText.XMLNS_URI) ||
        qnm.uri().length == 0 && eq(qnm.local(), token("xmlns")))) {
      throw MAP_TO_ELEMENT_X.get(info, "Namespace declaration is not allowed as attribute.");
    }

    // synthesize a prefix for namespaced attributes (forward conversion loses prefixes)
    if(!element && qnm.uri().length != 0 && !qnm.hasPrefix() && !eq(qnm.uri(), QueryText.XML_URI)) {
      qnm = qc.shared.qName(concat(token("ns:"), qnm.local()), qnm.uri());
    }
    return qnm;
  }

  /**
   * Parses serialized XML into an element (xml layout).
   * @param xml serialized XML
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  private XNode parse(final byte[] xml, final QueryContext qc) throws QueryException {
    try {
      final IO io = new IOContent(xml);
      final DBNode doc = new DBNode(new XMLParser(io, new MainOptions(qc.context.options), true));
      XNode element = null;
      for(final GNode child : doc.childIter()) {
        if(child.kind() == Kind.ELEMENT) {
          if(element != null) throw MAP_TO_ELEMENT_X.get(info,
              "Serialized XML must contain a single element.");
          element = (XNode) child;
        } else if(!(child.kind() == Kind.TEXT && normalize(child.string()).length == 0)) {
          // reject extra content (more elements, non-whitespace text, comments, PIs)
          throw MAP_TO_ELEMENT_X.get(info, "Serialized XML must contain a single element.");
        }
      }
      if(element == null) throw MAP_TO_ELEMENT_X.get(info, "No element in serialized XML.");
      return element.materialize(d -> false, info, qc);
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
   * @param item item
   * @return result of check
   */
  private static boolean isNull(final Item item) {
    return item instanceof final QNm qnm && NULL.eq(qnm);
  }
}
