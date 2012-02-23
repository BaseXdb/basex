package org.basex.api.xqj;

import static org.basex.util.Token.*;

import java.util.*;

import javax.xml.namespace.*;
import javax.xml.stream.*;

import org.basex.api.jaxp.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XML Stream Reader implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterStreamReader implements XMLStreamReader {
  /** Properties. */
  private static final Properties PROPS = new Properties();
  /** Namespaces references. */
  private final NSContext ns = new NSContext();
  /** Result iterator. */
  private final Iter result;
  /** Next flag. */
  private boolean next;
  /** Node iterator. */
  private NodeReader read;
  /** Attributes. */
  private NodeCache atts;

  /** Current state. */
  int kind = START_DOCUMENT;
  /** Current node. */
  ANode node;

  /**
   * Constructor.
   * @param res result iterator
   */
  IterStreamReader(final Iter res) {
    result = res;
    // included for wrapping the stream reader into an XML event reader
    PROPS.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
  }

  @Override
  public void close() {
  }

  @Override
  public int getAttributeCount() {
    return (int) attributes().size();
  }

  @Override
  public String getAttributeLocalName(final int i) {
    return string(attributes().get(i).name());
  }

  @Override
  public QName getAttributeName(final int i) {
    return attributes().get(i).qname().toJava();
  }

  @Override
  public String getAttributeNamespace(final int i) {
    return string(attributes().get(i).qname().uri());
  }

  @Override
  public String getAttributePrefix(final int i) {
    return string(attributes().get(i).qname().prefix());
  }

  @Override
  public String getAttributeType(final int i) {
    final String name = getAttributeLocalName(i);
    for(final String a : ATTYPES) if(name.equals(a)) return name;
    return "CDATA";
  }

  /** Attribute types. */
  private static final String[] ATTYPES = {
    "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES"
  };

  @Override
  public String getAttributeValue(final int i) {
    return string(attributes().get(i).string());
  }

  @Override
  public String getAttributeValue(final String s, final String s1) {
    for(int a = 0; a < atts.size(); ++a) {
      if(!s1.equals(getAttributeLocalName(a))) continue;
      if(s == null || s.equals(getAttributeNamespace(a)))
        return getAttributeValue(a);
    }
    return null;
  }

  /**
   * Caches and returns the attributes for the current element.
   * @return node cache
   */
  private NodeCache attributes() {
    if(atts == null) {
      checkType(START_ELEMENT, ATTRIBUTE);
      atts = new NodeCache();
      final AxisIter ai = node.attributes();
      for(ANode n; (n = ai.next()) != null;) atts.add(n.finish());
    }
    return atts;
  }

  @Override
  public String getCharacterEncodingScheme() {
    return null;
  }

  @Override
  public String getElementText() throws XMLStreamException {
    checkType(START_ELEMENT);
    next();

    final TokenBuilder tb = new TokenBuilder();
    while(kind != END_ELEMENT) {
      if(isType(CHARACTERS, CDATA, SPACE, ENTITY_REFERENCE)) {
        tb.add(node.string());
      } else if(isType(END_DOCUMENT)) {
        throw new XMLStreamException("Unexpected end of document.");
      } else if(isType(START_ELEMENT)) {
        throw new XMLStreamException("START_ELEMENT not expected.");
      } else {
        checkType(PROCESSING_INSTRUCTION, COMMENT);
      }
      next();
    }
    return tb.toString();
  }

  @Override
  public String getEncoding() {
    return null;
  }

  @Override
  public int getEventType() {
    return kind;
  }

  @Override
  public String getLocalName() {
    checkType(START_ELEMENT, END_ELEMENT, ENTITY_REFERENCE);
    return string(node.name());
  }

  @Override
  public Location getLocation() {
    return new LocationImpl();
  }

  @Override
  public QName getName() {
    checkType(START_ELEMENT, END_ELEMENT, ENTITY_REFERENCE);
    return node.qname().toJava();
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return new BXNamespaceContext(ns);
  }

  @Override
  public int getNamespaceCount() {
    checkType(START_ELEMENT, END_ELEMENT, NAMESPACE);
    return 0;
  }

  @Override
  public String getNamespacePrefix(final int i) {
    checkType(START_ELEMENT, END_ELEMENT, NAMESPACE);
    return null;
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public String getNamespaceURI(final String s) {
    if(s == null) throw new IllegalArgumentException();
    checkType(START_ELEMENT, END_ELEMENT, NAMESPACE);
    final byte[] uri = ns.staticURI(token(s));
    return uri == null ? null : string(uri);
  }

  @Override
  public String getNamespaceURI(final int i) {
    checkType(START_ELEMENT, END_ELEMENT, NAMESPACE);
    return null;
  }

  @Override
  public String getPIData() {
    checkType(PROCESSING_INSTRUCTION);
    final byte[] val = node.string();
    final int i = indexOf(val, ' ');
    return string(i == -1 ? EMPTY : substring(val, i + 1));
  }

  @Override
  public String getPITarget() {
    checkType(PROCESSING_INSTRUCTION);
    final byte[] val = node.string();
    final int i = indexOf(val, ' ');
    return string(i == -1 ? val : substring(val, 0, i));
  }

  @Override
  public String getPrefix() {
    checkType(START_ELEMENT, END_ELEMENT);
    final QNm qn = node.qname();
    return !qn.hasPrefix() ? null : string(qn.prefix());
  }

  @Override
  public Object getProperty(final String s) {
    if(s == null) throw new IllegalArgumentException();
    return PROPS.get(s);
  }

  @Override
  public String getText() {
    checkType(CHARACTERS, COMMENT);
    return string(node.string());
  }

  @Override
  public char[] getTextCharacters() {
    return getText().toCharArray();
  }

  @Override
  public int getTextCharacters(final int ss, final char[] ac, final int ts,
      final int l) {

    checkType(CHARACTERS, COMMENT);
    final String value = getText();
    final int vl = value.length();
    if(ss >= vl) return 0;
    int se = ss + l;
    if(se > vl) se = value.length();
    value.getChars(ss, se, ac, ts);
    return se - ss;
  }

  @Override
  public int getTextLength() {
    checkType(CHARACTERS, COMMENT);
    return node.string().length;
  }

  @Override
  public int getTextStart() {
    checkType(CHARACTERS, COMMENT);
    return 0;
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public boolean hasName() {
    return isType(START_ELEMENT, END_ELEMENT);
  }

  @Override
  public boolean hasNext() throws XMLStreamException {
    if(next) return true;
    next = true;
    atts = null;
    try {
      if(read != null) {
        if(read.hasNext()) {
          read.next();
        } else {
          read = null;
          kind = END_DOCUMENT;
          return true;
        }
      }
      if(read == null) {
        final Item it = result.next();
        if(it == null) return false;
        if(!(it instanceof ANode)) throw new XMLStreamException();
        node = (ANode) it;
        read = it instanceof DBNode ? new DBNodeReader() : new FNodeReader();
      }
    } catch(final QueryException ex) {
      throw new XMLStreamException(ex);
    }
    return node != null;
  }

  @Override
  public boolean hasText() {
    return isType(CHARACTERS, DTD, ENTITY_REFERENCE, COMMENT, SPACE);
  }

  @Override
  public boolean isAttributeSpecified(final int i) {
    checkType(START_ELEMENT, ATTRIBUTE);
    return true;
  }

  @Override
  public boolean isCharacters() {
    return isType(CHARACTERS);
  }

  @Override
  public boolean isEndElement() {
    return isType(END_ELEMENT);
  }

  @Override
  public boolean isStandalone() {
    return false;
  }

  @Override
  public boolean isStartElement() {
    return isType(START_ELEMENT);
  }

  @Override
  public boolean isWhiteSpace() {
    return isCharacters() && ws(node.string());
  }

  @Override
  public int next() throws XMLStreamException {
    if(next && node == null || !next && !hasNext())
      throw new NoSuchElementException();

    next = false;
    // disallow top level attributes
    if(node.type == NodeType.ATT && read == null)
      throw new XMLStreamException();
    return kind;
  }

  @Override
  public int nextTag() throws XMLStreamException {
    next();
    while(kind == CHARACTERS && isWhiteSpace() ||
        kind == CDATA && isWhiteSpace() || kind == SPACE ||
        kind == PROCESSING_INSTRUCTION || kind == COMMENT) {
      next();
    }
    checkType(START_ELEMENT, END_ELEMENT);
    return kind;
  }

  @Override
  public void require(final int t, final String uri, final String ln)
      throws XMLStreamException {
    checkType(t);
    if(uri != null && !uri.equals(getNamespaceURI())) {
      throw new XMLStreamException();
    }
    if(ln != null && !ln.equals(getLocalName())) {
      throw new XMLStreamException();
    }
  }

  @Override
  public boolean standaloneSet() {
    return false;
  }

  /**
   * Sets the current event type.
   */
  void type() {
    switch(node.nodeType()) {
      case DOC: kind = START_DOCUMENT; return;
      case ATT: kind = ATTRIBUTE; return;
      case ELM: kind = START_ELEMENT; return;
      case COM: kind = COMMENT; return;
      case PI : kind = PROCESSING_INSTRUCTION; return;
      default:  kind = CHARACTERS;
    }
  }

  /**
   * Tests the validity of the specified types.
   * @param valid input types
   */
  private void checkType(final int... valid) {
    if(!isType(valid)) throw new IllegalStateException("Invalid Type: " + kind);
  }

  /**
   * Tests if one of the specified values matches the current kind.
   * @param valid input types
   * @return result of check
   */
  private boolean isType(final int... valid) {
    for(final int v : valid) if(kind == v) return true;
    return false;
  }

  /**
   * Reader for {@link FNode} instances.
   */
  abstract static class NodeReader {
    /**
     * Checks if the node reader can return more nodes.
     * @return result of check
     */
    abstract boolean hasNext();
    /**
     * Checks if the node reader can return more nodes.
     */
    abstract void next();
  }

  /** Reader for traversing {@link DBNode} instances. */
  private final class DBNodeReader extends NodeReader {
    /** Node reference. */
    private final DBNode dbnode;
    /** Data size. */
    private final int s;
    /** Parent stack. */
    private final IntList parent = new IntList();
    /** Pre stack. */
    private final IntList pre = new IntList();
    /** Current pre value. */
    private int p;

    /** Constructor. */
    DBNodeReader() {
      dbnode = ((DBNode) node).copy();
      node = dbnode;
      p = dbnode.pre;
      final int k = dbnode.data.kind(p);
      s = p + dbnode.data.size(p, k);
      finish(k, 0);
    }

    @Override
    boolean hasNext() {
      return p < s || pre.size() > 0;
    }

    @Override
    void next() {
      if(p == s) {
        endElem();
        return;
      }

      final Data data = dbnode.data;
      final int k = data.kind(p);
      final int pa = data.parent(p, k);
      if(parent.size() > 0 && parent.peek() >= pa) {
        endElem();
        return;
      }
      finish(k, pa);
    }

    /**
     * Processes the end of an element.
     */
    private void endElem() {
      dbnode.set(pre.pop(), Data.ELEM);
      parent.pop();
      kind = END_ELEMENT;
    }

    /**
     * Finishing step.
     * @param k node kind
     * @param pa parent reference
     */
    private void finish(final int k, final int pa) {
      dbnode.set(p, k);
      if(k == Data.ELEM) {
        pre.push(p);
        parent.push(pa);
      }
      p += dbnode.data.attSize(p, k);
      type();
    }
  }

  /** Reader for traversing {@link FNode} instances. */
  private final class FNodeReader extends NodeReader {
    /** Axis iterator. */
    private final ArrayList<AxisIter> iter = new ArrayList<AxisIter>();
    /** Node stack. */
    private final ArrayList<ANode> nodes = new ArrayList<ANode>();
    /** Stack level. */
    private int l;

    /** Constructor. */
    FNodeReader() {
      iter.add(node.self());
      hasNext();
    }

    @Override
    boolean hasNext() {
      final ANode n = iter.get(l).next();
      if(n != null) {
        while(l >= nodes.size()) nodes.add(null);
        nodes.set(l, n);
        node = n;
        type();
        if(kind == START_ELEMENT) {
          while(l + 1 >= iter.size()) iter.add(null);
          iter.set(++l, n.children());
        }
      } else {
        if(--l < 0) return false;
        node = nodes.get(l);
        kind = END_ELEMENT;
      }
      return true;
    }

    @Override
    void next() { }
  }

  /** Dummy location implementation. */
  static final class LocationImpl implements Location {
    @Override
    public int getCharacterOffset() { return -1; }
    @Override
    public int getColumnNumber() { return -1; }
    @Override
    public int getLineNumber() { return -1; }
    @Override
    public String getPublicId() { return null; }
    @Override
    public String getSystemId() { return null; }
  }
}
