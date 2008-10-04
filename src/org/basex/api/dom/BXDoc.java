package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.xquery.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * DOM - Document Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXDoc extends BXNode implements Document {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXDoc(final Nod n) {
    super(n);
  }
  
  public Node adoptNode(final Node source) {
    BaseX.notimplemented();
    return null;
  }

  public Attr createAttribute(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public Attr createAttributeNS(final String uri, final String qn) {
    BaseX.notimplemented();
    return null;
  }

  public CDATASection createCDATASection(final String dat) {
    BaseX.notimplemented();
    return null;
  }

  public Comment createComment(final String dat) {
    BaseX.notimplemented();
    return null;
  }

  public DocumentFragment createDocumentFragment() {
    BaseX.notimplemented();
    return null;
  }

  public Element createElement(final String tagName) {
    BaseX.notimplemented();
    return null;
  }

  public Element createElementNS(final String uri, final String qn) {
    BaseX.notimplemented();
    return null;
  }

  public EntityReference createEntityReference(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public BXPI createProcessingInstruction(final String t, final String dat) {
    BaseX.notimplemented();
    return null;
  }

  public Text createTextNode(final String dat) {
    BaseX.notimplemented();
    return null;
  }

  public DocumentType getDoctype() {
    return null;
  }

  public Element getDocumentElement() {
    try {
      return (Element) finish(node.child().next());
    } catch(final Exception e) {
      BaseX.debug(e);
      return null;
    }
  }

  public String getDocumentURI() {
    return getBaseURI();
  }

  public DOMConfiguration getDomConfig() {
    BaseX.notimplemented();
    return null;
  }

  public Element getElementById(final String elementId) {
    BaseX.notimplemented();
    return null;
  }

  public NodeList getElementsByTagName(final String name) {
    return getElements(name);
  }

  public NodeList getElementsByTagNameNS(final String namespaceURI,
      final String localName) {
    BaseX.notimplemented();
    return null;
  }

  public DOMImplementation getImplementation() {
    BaseX.notimplemented();
    return null;
  }

  public String getInputEncoding() {
    return Token.UTF8;
  }

  public boolean getStrictErrorChecking() {
    BaseX.notimplemented();
    return false;
  }

  public String getXmlEncoding() {
    return Token.UTF8;
  }

  public boolean getXmlStandalone() {
    return false;
  }

  public String getXmlVersion() {
    return "1.0";
  }

  public Node importNode(final Node importedNode, final boolean deep) {
    BaseX.notimplemented();
    return null;
  }

  public void normalizeDocument() {
    BaseX.notimplemented();
  }

  public Node renameNode(final Node n, final String namespaceURI,
      final String qualifiedName) {
    BaseX.notimplemented();
    return null;
  }

  public void setDocumentURI(final String documentURI) {
    BaseX.notimplemented();
  }

  public void setStrictErrorChecking(final boolean strictErrorChecking) {
    BaseX.notimplemented();
  }

  public void setXmlStandalone(final boolean xmlStandalone) {
    BaseX.notimplemented();
  }

  public void setXmlVersion(final String xmlVersion) {
    BaseX.notimplemented();
  }
}
