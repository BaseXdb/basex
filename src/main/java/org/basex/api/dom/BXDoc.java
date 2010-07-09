package org.basex.api.dom;

import static org.basex.util.Token.*;
import org.basex.core.Main;
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
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

/**
 * DOM - Document implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public BXDoc getOwnerDocument() {
    return null;
  }

  @Override
  public BXNode adoptNode(final Node source) {
    error();
    return null;
  }

  @Override
  public BXAttr createAttribute(final String nm) {
    final QNm name = new QNm(token(nm));
    return new BXAttr(new FAttr(name, EMPTY, null));
  }

  @Override
  public BXAttr createAttributeNS(final String uri, final String qn) {
    final QNm name = new QNm(token(qn), Uri.uri(token(uri)));
    return new BXAttr(new FAttr(name, EMPTY, null));
  }

  @Override
  public BXCData createCDATASection(final String dat) {
    return new BXCData(new FTxt(token(dat), null));
  }

  @Override
  public BXComm createComment(final String dat) {
    return new BXComm(new FComm(token(dat), null));
  }

  @Override
  public BXDocFrag createDocumentFragment() {
    return new BXDocFrag(new FDoc(new NodIter(), node.base()));
  }

  @Override
  public BXElem createElement(final String nm) {
    final QNm name = new QNm(token(nm));
    return new BXElem(new FElem(name, node.base()));
  }

  @Override
  public BXElem createElementNS(final String uri, final String qn) {
    final QNm name = new QNm(token(qn), Uri.uri(token(uri)));
    return new BXElem(new FElem(name, node.base()));
  }

  @Override
  public EntityReference createEntityReference(final String name) {
    error();
    return null;
  }

  @Override
  public BXPI createProcessingInstruction(final String t, final String dat) {
    return new BXPI(new FPI(new QNm(token(t)), token(dat), null));
  }

  @Override
  public BXText createTextNode(final String dat) {
    return new BXText(new FTxt(token(dat), null));
  }

  @Override
  public DocumentType getDoctype() {
    return null;
  }

  @Override
  public BXElem getDocumentElement() {
    final BXNList list = getChildNodes();
    for(int l = 0; l < list.getLength(); l++) {
      final BXNode n = list.item(l);
      if(n.getNodeType() == Node.ELEMENT_NODE) return (BXElem) n;
    }
    Main.notexpected();
    return null;
  }

  @Override
  public String getDocumentURI() {
    return getBaseURI();
  }

  @Override
  public DOMConfiguration getDomConfig() {
    Main.notimplemented();
    return null;
  }

  @Override
  public BXElem getElementById(final String elementId) {
    Main.notimplemented();
    return null;
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String namespaceURI,
      final String localName) {
    Main.notimplemented();
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
    Main.notimplemented();
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
    Main.notimplemented();
    return null;
  }

  @Override
  public void normalizeDocument() {
    error();
  }

  @Override
  public BXNode renameNode(final Node n, final String namespaceURI,
      final String qualifiedName) {
    error();
    return null;
  }

  @Override
  public void setDocumentURI(final String documentURI) {
    error();
  }

  @Override
  public void setStrictErrorChecking(final boolean strictErrorChecking) {
    Main.notimplemented();
  }

  @Override
  public void setXmlStandalone(final boolean xmlStandalone) {
    Main.notimplemented();
  }

  @Override
  public void setXmlVersion(final String xmlVersion) {
    Main.notimplemented();
  }
}
