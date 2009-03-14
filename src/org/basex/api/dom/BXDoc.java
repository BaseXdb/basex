package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.query.item.FAttr;
import org.basex.query.item.FComm;
import org.basex.query.item.FDoc;
import org.basex.query.item.FElem;
import org.basex.query.item.FPI;
import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Uri;
import org.basex.query.iter.NodIter;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  @Override
  public Document getOwnerDocument() {
    return null;
  }

  public Node adoptNode(final Node source) {
    error();
    return null;
  }

  public Attr createAttribute(final String nm) {
    final QNm name = new QNm(token(nm));
    return new BXAttr(new FAttr(name, EMPTY, null));
  }

  public Attr createAttributeNS(final String uri, final String qn) {
    final QNm name = new QNm(token(qn), Uri.uri(token(uri)));
    return new BXAttr(new FAttr(name, EMPTY, null));
  }

  public CDATASection createCDATASection(final String dat) {
    return new BXCData(new FTxt(token(dat), null));
  }

  public Comment createComment(final String dat) {
    return new BXComm(new FComm(token(dat), null));
  }

  public DocumentFragment createDocumentFragment() {
    return new BXDocFrag(new FDoc(new NodIter(), node.base()));
  }

  public Element createElement(final String nm) {
    final QNm name = new QNm(token(nm));
    return new BXElem(new FElem(name, node.base(), null));
  }

  public Element createElementNS(final String uri, final String qn) {
    final QNm name = new QNm(token(qn), Uri.uri(token(uri)));
    return new BXElem(new FElem(name, node.base(), null));
  }

  public EntityReference createEntityReference(final String name) {
    error();
    return null;
  }

  public BXPI createProcessingInstruction(final String t, final String dat) {
    return new BXPI(new FPI(new QNm(token(t)), token(dat), null));
  }

  public Text createTextNode(final String dat) {
    return new BXText(new FTxt(token(dat), null));
  }

  public DocumentType getDoctype() {
    return null;
  }

  public Element getDocumentElement() {
    final NodeList list = getChildNodes();
    for(int l = 0; l < list.getLength(); l++) {
      final Node n = list.item(l);
      if(n.getNodeType() == Node.ELEMENT_NODE) return (Element) n;
    }
    BaseX.notexpected();
    return null;
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
    return BXDomImpl.get();
  }

  public String getInputEncoding() {
    return UTF8;
  }

  public boolean getStrictErrorChecking() {
    BaseX.notimplemented();
    return false;
  }

  public String getXmlEncoding() {
    return UTF8;
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
    error();
  }

  public Node renameNode(final Node n, final String namespaceURI,
      final String qualifiedName) {
    error();
    return null;
  }

  public void setDocumentURI(final String documentURI) {
    error();
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
