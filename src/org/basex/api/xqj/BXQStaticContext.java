package org.basex.api.xqj;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.util.Namespaces;
import org.basex.util.Token;

/**
 * Java XQuery API - Static Context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXQStaticContext implements XQStaticContext {
  /** Namespaces. */
  Namespaces ns = new Namespaces();
  /** Project context. */
  final Context ctx;
  /** Forward flag. */
  boolean scrollable;

  /**
   * Constructor.
   * @param c context
   */
  public BXQStaticContext(final Context c) {
    ctx = c;
  }
  
  public void declareNamespace(String prefix, String uri) throws XQException {
    try {
      BXQClose.check(prefix, String.class);
      BXQClose.check(uri, String.class);
      ns.index(new QNm(Token.token(prefix), Uri.uri(Token.token(uri))));
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public String getBaseURI() {
    BaseX.notimplemented();
    return null;
  }

  public int getBindingMode() {
    BaseX.notimplemented();
    return 0;
  }

  public int getBoundarySpacePolicy() {
    BaseX.notimplemented();
    return 0;
  }

  public int getConstructionMode() {
    BaseX.notimplemented();
    return 0;
  }

  public XQItemType getContextItemStaticType() {
    BaseX.notimplemented();
    return null;
  }

  public int getCopyNamespacesModeInherit() {
    BaseX.notimplemented();
    return 0;
  }

  public int getCopyNamespacesModePreserve() {
    BaseX.notimplemented();
    return 0;
  }

  public String getDefaultCollation() {
    BaseX.notimplemented();
    return null;
  }

  public String getDefaultElementTypeNamespace() {
    BaseX.notimplemented();
    return null;
  }

  public String getDefaultFunctionNamespace() {
    BaseX.notimplemented();
    return null;
  }

  public int getDefaultOrderForEmptySequences() {
    BaseX.notimplemented();
    return 0;
  }

  public int getHoldability() {
    BaseX.notimplemented();
    return 0;
  }

  public String[] getNamespacePrefixes() {
    BaseX.notimplemented();
    return null;
  }

  public String getNamespaceURI(String prefix) {
    BaseX.notimplemented();
    return null;
  }

  public int getOrderingMode() {
    BaseX.notimplemented();
    return 0;
  }

  public int getQueryLanguageTypeAndVersion() {
    BaseX.notimplemented();
    return 0;
  }

  public int getQueryTimeout() {
    BaseX.notimplemented();
    return 0;
  }

  public int getScrollability() {
    return scrollable ? XQConstants.SCROLLTYPE_SCROLLABLE :
      XQConstants.SCROLLTYPE_FORWARD_ONLY;
  }

  public void setBaseURI(String baseUri) {
    BaseX.notimplemented();
  }

  public void setBindingMode(int bindingMode) {
    BaseX.notimplemented();
  }

  public void setBoundarySpacePolicy(int policy) {
    BaseX.notimplemented();
  }

  public void setConstructionMode(int mode) {
    BaseX.notimplemented();
  }

  public void setContextItemStaticType(XQItemType contextItemType) {
    BaseX.notimplemented();
  }

  public void setCopyNamespacesModeInherit(int mode) {
    BaseX.notimplemented();
  }

  public void setCopyNamespacesModePreserve(int mode) {
    BaseX.notimplemented();
  }

  public void setDefaultCollation(String uri) {
    BaseX.notimplemented();
  }

  public void setDefaultElementTypeNamespace(String uri) {
    BaseX.notimplemented();
  }

  public void setDefaultFunctionNamespace(String uri) {
    BaseX.notimplemented();
  }

  public void setDefaultOrderForEmptySequences(int order) {
    BaseX.notimplemented();
  }

  public void setHoldability(int holdability) {
    BaseX.notimplemented();
  }

  public void setOrderingMode(int mode) {
    BaseX.notimplemented();
  }

  public void setQueryLanguageTypeAndVersion(int langType) {
    BaseX.notimplemented();
  }

  public void setQueryTimeout(int seconds) {
    BaseX.notimplemented();
  }

  public void setScrollability(int scrollability) {
    scrollable = scrollability == XQConstants.SCROLLTYPE_SCROLLABLE;
  }
}
