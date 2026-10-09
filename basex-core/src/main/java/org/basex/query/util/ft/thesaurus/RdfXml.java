package org.basex.query.util.ft.thesaurus;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Reader for a subset of RDF/XML, which passes on the triples of a document.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class RdfXml {
  /** RDF namespace URI. */
  private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  /** RDF type. */
  static final byte[] TYPE = token(RDF_URI + "type");

  /** RDF element name: RDF. */
  private static final QNm RDF = new QNm("rdf:RDF", RDF_URI);
  /** RDF element name: description. */
  private static final QNm DESCRIPTION = new QNm("rdf:Description", RDF_URI);
  /** RDF attribute name: about. */
  private static final QNm ABOUT = new QNm("rdf:about", RDF_URI);
  /** RDF attribute name: ID. */
  private static final QNm ID = new QNm("rdf:ID", RDF_URI);
  /** RDF attribute name: node ID. */
  private static final QNm NODE_ID = new QNm("rdf:nodeID", RDF_URI);
  /** RDF attribute name: resource. */
  private static final QNm RESOURCE = new QNm("rdf:resource", RDF_URI);
  /** XML attribute name: base. */
  private static final QNm BASE = new QNm(QueryText.BASE, QueryText.XML_URI);
  /** XML attribute name: lang. */
  private static final QNm LANG = new QNm(QueryText.LANG, QueryText.XML_URI);

  /** Handler for triples. */
  interface Handler {
    /**
     * Passes on a triple with a resource object.
     * @param subject subject
     * @param predicate predicate
     * @param object object
     */
    void resource(byte[] subject, byte[] predicate, byte[] object);

    /**
     * Passes on a triple with a literal object.
     * @param subject subject
     * @param predicate predicate
     * @param value value
     * @param lang language (empty if unknown)
     */
    void literal(byte[] subject, byte[] predicate, byte[] value, byte[] lang);
  }

  /** Handler for triples. */
  private final Handler handler;
  /** Counter for blank nodes. */
  private int blank;
  /** Prefix for node IDs, which are local to a document. */
  private byte[] prefix;
  /** Counter for documents. */
  private int document;

  /**
   * Constructor.
   * @param handler handler for triples
   */
  RdfXml(final Handler handler) {
    this.handler = handler;
  }

  /**
   * Checks if an element is an RDF root element.
   * @param element element
   * @return result of check
   */
  static boolean root(final GNode element) {
    return element.qname().eq(RDF);
  }

  /**
   * Passes on the triples of the node elements of an RDF root element.
   * @param root RDF root element
   */
  void parse(final XNode root) {
    // inherited base URI and language
    Uri base = Uri.EMPTY;
    byte[] lang = null;
    for(XNode node = root; node != null; node = node.parent()) {
      final byte[] bs = node.attribute(BASE);
      if(bs != null) base = Uri.get(bs, false).resolve(base);
      if(lang == null) lang = node.attribute(LANG);
    }
    if(lang == null) lang = EMPTY;
    prefix = token("_:" + ++document + '-');
    for(final GNode node : root.childIter()) {
      if(node.kind() == Kind.ELEMENT) node((XNode) node, base, lang);
    }
  }

  /**
   * Passes on the triples of a node element.
   * @param node node element
   * @param parentBase base URI of the parent
   * @param parentLang language of the parent (empty if unknown)
   * @return subject
   */
  private byte[] node(final XNode node, final Uri parentBase, final byte[] parentLang) {
    final Uri base = base(node, parentBase);
    final byte[] lang = lang(node, parentLang);
    final byte[] about = node.attribute(ABOUT), id = node.attribute(ID);
    final byte[] subject = about != null ? resolve(base, about) : id != null ?
      resolve(base, concat("#", id)) : blank(node.attribute(NODE_ID));
    if(!node.qname().eq(DESCRIPTION)) handler.resource(subject, TYPE, name(node));

    for(final GNode child : node.childIter()) {
      if(child.kind() != Kind.ELEMENT) continue;
      final XNode property = (XNode) child;
      final byte[] predicate = name(property);
      final byte[] resource = property.attribute(RESOURCE), nodeId = property.attribute(NODE_ID);
      final Uri pbase = base(property, base);
      final byte[] plang = lang(property, lang);
      byte[] object = null;
      if(resource != null) {
        object = resolve(pbase, resource);
      } else if(nodeId != null) {
        object = blank(nodeId);
      } else {
        for(final GNode nested : property.childIter()) {
          if(nested.kind() == Kind.ELEMENT) object = node((XNode) nested, pbase, plang);
        }
      }
      if(object != null) handler.resource(subject, predicate, object);
      else handler.literal(subject, predicate, property.string(), plang);
    }
    return subject;
  }

  /**
   * Returns the IRI of an element name.
   * @param element element
   * @return IRI
   */
  private static byte[] name(final XNode element) {
    final QNm qname = element.qname();
    return concat(qname.uri(), qname.local());
  }

  /**
   * Returns a blank node identifier.
   * @param id node ID (can be {@code null})
   * @return identifier
   */
  private byte[] blank(final byte[] id) {
    return id != null ? concat(prefix, id) : token("_:" + ++blank);
  }

  /**
   * Resolves a reference against a base URI.
   * @param base base URI
   * @param ref reference
   * @return resolved reference
   */
  private static byte[] resolve(final Uri base, final byte[] ref) {
    return base.resolve(Uri.get(ref, false)).string();
  }

  /**
   * Returns the base URI of an element.
   * @param element element
   * @param parent base URI of the parent
   * @return base URI
   */
  private static Uri base(final XNode element, final Uri parent) {
    final byte[] base = element.attribute(BASE);
    return base != null ? parent.resolve(Uri.get(base, false)) : parent;
  }

  /**
   * Returns the language of an element.
   * @param element element
   * @param parent language of the parent (empty if unknown)
   * @return language (empty if unknown)
   */
  private static byte[] lang(final XNode element, final byte[] parent) {
    final byte[] lang = element.attribute(LANG);
    return lang != null ? lang : parent;
  }
}
