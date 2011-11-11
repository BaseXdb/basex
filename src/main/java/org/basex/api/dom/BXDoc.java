package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.query.item.FAttr;
import org.basex.query.item.FComm;
import org.basex.query.item.FDoc;
import org.basex.query.item.FElem;
import org.basex.query.item.FPI;
import org.basex.query.item.FTxt;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.util.Util;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

/**
 * DOM - Document implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BXDoc extends BXNode implements Document {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXDoc(final ANode n) {
    super(n);
  }

  @Override
  public BXDoc getOwnerDocument() {
    return null;
  }

  @Override
  public BXNode adoptNode(final Node source) {
    readOnly();
    return null;
  }

  @Override
  public BXAttr createAttribute(final String nm) {
    final QNm name = new QNm(token(nm));
    return new BXAttr(new FAttr(name, EMPTY));
  }

  @Override
  public BXAttr createAttributeNS(final String uri, final String qn) {
    final QNm name = new QNm(token(qn), token(uri));
    return new BXAttr(new FAttr(name, EMPTY));
  }

  @Override
  public BXCData createCDATASection(final String dat) {
    return new BXCData(new FTxt(token(dat)));
  }

  @Override
  public BXComm createComment(final String dat) {
    return new BXComm(new FComm(token(dat)));
  }

  @Override
  public BXDocFrag createDocumentFragment() {
    return new BXDocFrag(new FDoc(node.baseURI()));
  }

  @Override
  public BXElem createElement(final String nm) {
    return new BXElem(new FElem(new QNm(token(nm))));
  }

  @Override
  public BXElem createElementNS(final String uri, final String qn) {
    return new BXElem(new FElem(new QNm(token(qn), token(uri))));
  }

  @Override
  public EntityReference createEntityReference(final String name) {
    readOnly();
    return null;
  }

  @Override
  public BXPI createProcessingInstruction(final String t, final String dat) {
    return new BXPI(new FPI(new QNm(token(t)), token(dat)));
  }

  @Override
  public BXText createTextNode(final String dat) {
    return new BXText(new FTxt(token(dat)));
  }

  @Override
  public DocumentType getDoctype() {
    return null;
  }

  @Override
  public BXElem getDocumentElement() {
    final BXNList list = getChildNodes();
    for(int l = 0; l < list.getLength(); ++l) {
      final BXNode n = list.item(l);
      if(n.getNodeType() == Node.ELEMENT_NODE) return (BXElem) n;
    }
    throw Util.notexpected();
  }

  @Override
  public String getDocumentURI() {
    return getBaseURI();
  }

  @Override
  public DOMConfiguration getDomConfig() {
    Util.notimplemented();
    return null;
  }

  @Override
  public BXElem getElementById(final String elementId) {
    Util.notimplemented();
    return null;
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String namespaceURI,
      final String localName) {
    Util.notimplemented();
    return null;
  }

  @Override
  public DOMImplementation getImplementation() {
    return BXDomImpl.get();
  }

  @Override
  public String getInputEncoding() {
    return UTF8;
  }

  @Override
  public boolean getStrictErrorChecking() {
    Util.notimplemented();
    return false;
  }

  @Override
  public String getXmlEncoding() {
    return UTF8;
  }

  @Override
  public boolean getXmlStandalone() {
    return false;
  }

  @Override
  public String getXmlVersion() {
    return "1.0";
  }

  @Override
  public BXNode importNode(final Node importedNode, final boolean deep) {
    Util.notimplemented();
    return null;
  }

  @Override
  public void normalizeDocument() {
    readOnly();
  }

  @Override
  public BXNode renameNode(final Node n, final String namespaceURI,
      final String qualifiedName) {
    readOnly();
    return null;
  }

  @Override
  public void setDocumentURI(final String documentURI) {
    readOnly();
  }

  @Override
  public void setStrictErrorChecking(final boolean strictErrorChecking) {
    Util.notimplemented();
  }

  @Override
  public void setXmlStandalone(final boolean xmlStandalone) {
    Util.notimplemented();
  }

  @Override
  public void setXmlVersion(final String xmlVersion) {
    Util.notimplemented();
  }
}
