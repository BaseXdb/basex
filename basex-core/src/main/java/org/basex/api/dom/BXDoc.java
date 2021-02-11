package org.basex.api.dom;

import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Document implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BXDoc extends BXNode implements Document {
  /**
   * Constructor.
   * @param node node reference
   */
  public BXDoc(final ANode node) {
    super(node);
  }

  @Override
  public BXDoc getOwnerDocument() {
    return null;
  }

  @Override
  public BXNode adoptNode(final Node node) {
    throw readOnly();
  }

  @Override
  public BXAttr createAttribute(final String name) {
    return new BXAttr(new FAttr(new QNm(name), EMPTY));
  }

  @Override
  public BXAttr createAttributeNS(final String uri, final String name) {
    return new BXAttr(new FAttr(new QNm(name, uri), EMPTY));
  }

  @Override
  public BXCData createCDATASection(final String value) {
    return new BXCData(new FTxt(value));
  }

  @Override
  public BXComm createComment(final String value) {
    return new BXComm(new FComm(value));
  }

  @Override
  public BXDocFrag createDocumentFragment() {
    return new BXDocFrag(new FDoc(nd.baseURI()));
  }

  @Override
  public BXElem createElement(final String name) {
    return new BXElem(new FElem(new QNm(name)));
  }

  @Override
  public BXElem createElementNS(final String uri, final String name) {
    return new BXElem(new FElem(new QNm(name, uri)));
  }

  @Override
  public EntityReference createEntityReference(final String name) {
    throw readOnly();
  }

  @Override
  public BXPI createProcessingInstruction(final String name, final String value) {
    return new BXPI(new FPI(name, value));
  }

  @Override
  public BXText createTextNode(final String value) {
    return new BXText(new FTxt(value));
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
    throw Util.notExpected();
  }

  @Override
  public String getDocumentURI() {
    return getBaseURI();
  }

  @Override
  public DOMConfiguration getDomConfig() {
    throw notImplemented();
  }

  @Override
  public BXElem getElementById(final String id) {
    throw notImplemented();
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String uri, final String name) {
    throw notImplemented();
  }

  @Override
  public DOMImplementation getImplementation() {
    return BXDomImpl.get();
  }

  @Override
  public String getInputEncoding() {
    return Strings.UTF8;
  }

  @Override
  public boolean getStrictErrorChecking() {
    throw notImplemented();
  }

  @Override
  public String getXmlEncoding() {
    return Strings.UTF8;
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
  public BXNode importNode(final Node node, final boolean deep) {
    throw notImplemented();
  }

  @Override
  public void normalizeDocument() {
    throw readOnly();
  }

  @Override
  public BXNode renameNode(final Node node, final String uri, final String name) {
    throw readOnly();
  }

  @Override
  public void setDocumentURI(final String uri) {
    throw readOnly();
  }

  @Override
  public void setStrictErrorChecking(final boolean value) {
    throw notImplemented();
  }

  @Override
  public void setXmlStandalone(final boolean value) {
    throw notImplemented();
  }

  @Override
  public void setXmlVersion(final String value) {
    throw notImplemented();
  }
}
