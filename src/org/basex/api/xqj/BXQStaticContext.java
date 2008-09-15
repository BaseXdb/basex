package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import static javax.xml.xquery.XQConstants.*;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Token;

/**
 * Java XQuery API - Static Context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXQStaticContext implements XQStaticContext {
  /** Namespaces. */
  XQContext ctx = new XQContext();
  /** Context item type. */
  XQItemType type;
  /** Forward flag. */
  boolean scrollable;
  /** Binding mode (immediate). */
  boolean binding = true;
  /** Holdability. */
  boolean holdability = true;
  /** Language mode. */
  boolean xqueryx = true;
  /** Timeout. */
  int timeout = 0;

  public void declareNamespace(String prefix, String uri) throws XQException {
    try {
      BXQAbstract.check(prefix, String.class);
      BXQAbstract.check(uri, String.class);
      ctx.ns.index(new QNm(Token.token(prefix),
          Uri.uri(Token.token(uri))), true);
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public String getBaseURI() {
    return Token.string(ctx.baseURI.str());
  }

  public int getBindingMode() {
    return binding ? BINDING_MODE_IMMEDIATE : BINDING_MODE_DEFERRED;
  }

  public int getBoundarySpacePolicy() {
    return ctx.spaces ? BOUNDARY_SPACE_PRESERVE : BOUNDARY_SPACE_STRIP;
  }

  public int getConstructionMode() {
    return ctx.construct ? CONSTRUCTION_MODE_PRESERVE : CONSTRUCTION_MODE_STRIP; 
  }

  public XQItemType getContextItemStaticType() {
    return type;
  }

  public int getCopyNamespacesModeInherit() {
    return ctx.nsInherit ? COPY_NAMESPACES_MODE_INHERIT :
      COPY_NAMESPACES_MODE_NO_INHERIT;
  }

  public int getCopyNamespacesModePreserve() {
    return ctx.nsPreserve ? COPY_NAMESPACES_MODE_PRESERVE :
      COPY_NAMESPACES_MODE_NO_PRESERVE;
  }

  public String getDefaultCollation() {
    return Token.string(ctx.collation.str());
  }

  public String getDefaultElementTypeNamespace() {
    return Token.string(ctx.nsElem.str());
  }

  public String getDefaultFunctionNamespace() {
    return Token.string(ctx.nsFunc.str());
  }

  public int getDefaultOrderForEmptySequences() {
    return ctx.orderGreatest ? DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST :
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST;
  }

  public int getHoldability() {
    return holdability ? HOLDTYPE_HOLD_CURSORS_OVER_COMMIT :
      HOLDTYPE_CLOSE_CURSORS_AT_COMMIT;
  }

  public String[] getNamespacePrefixes() {
    final String[] pre = new String[ctx.ns.size];
    for(int p = 0; p < ctx.ns.size; p++) 
      pre[p] = Token.string(ctx.ns.names[p].ln());
    return pre;
  }

  public String getNamespaceURI(String prefix) throws XQException {
    BXQAbstract.check(prefix, String.class);
    final Uri uri = ctx.ns.find(Token.token(prefix));
    if(uri != null) return Token.string(uri.str());
    throw new BXQException(PRE, prefix);  }

  public int getOrderingMode() {
    return ctx.ordered ? ORDERING_MODE_ORDERED : ORDERING_MODE_UNORDERED;
  }

  public int getQueryLanguageTypeAndVersion() {
    return xqueryx ? LANGTYPE_XQUERYX : LANGTYPE_XQUERY;
  }

  public int getQueryTimeout() {
    return timeout;
  }

  public int getScrollability() {
    return scrollable ? SCROLLTYPE_SCROLLABLE : SCROLLTYPE_FORWARD_ONLY;
  }

  public void setBaseURI(String baseUri) throws XQException {
    BXQAbstract.check(baseUri, String.class);
    ctx.baseURI = Uri.uri(Token.token(baseUri));
  }

  public void setBindingMode(int mode) throws BXQException {
    if(mode != 0 && mode != 1) throw new BXQException(ARG, ARGB);
    binding = mode == BINDING_MODE_IMMEDIATE;
  }

  public void setBoundarySpacePolicy(int mode) throws BXQException {
    ctx.spaces = check(mode, ARGS) == BOUNDARY_SPACE_PRESERVE;
  }

  public void setConstructionMode(int mode) throws XQException {
    ctx.construct = check(mode, ARGC) == CONSTRUCTION_MODE_PRESERVE;
  }

  public void setContextItemStaticType(XQItemType contextItemType) {
    type = contextItemType;
  }

  public void setCopyNamespacesModeInherit(int mode) throws BXQException {
    ctx.nsInherit = check(mode, ARGN) == COPY_NAMESPACES_MODE_INHERIT;
  }

  public void setCopyNamespacesModePreserve(int mode) throws BXQException {
    ctx.nsPreserve = check(mode, ARGN) == COPY_NAMESPACES_MODE_PRESERVE;
  }

  public void setDefaultCollation(String uri) throws XQException {
    BXQAbstract.check(uri, String.class);
    ctx.collation = Uri.uri(Token.token(uri));
  }

  public void setDefaultElementTypeNamespace(String uri) throws XQException {
    BXQAbstract.check(uri, String.class);
    ctx.nsElem = Uri.uri(Token.token(uri));
  }

  public void setDefaultFunctionNamespace(String uri) throws XQException {
    BXQAbstract.check(uri, String.class);
    ctx.nsFunc = Uri.uri(Token.token(uri));
  }

  public void setDefaultOrderForEmptySequences(int mode) throws BXQException {
    ctx.orderGreatest = check(mode, ARGO) ==
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST;
  }

  public void setHoldability(int hold) throws BXQException {
    holdability = check(hold, ARGH) == HOLDTYPE_HOLD_CURSORS_OVER_COMMIT;
  }

  public void setOrderingMode(int mode) throws BXQException {
    ctx.ordered = check(mode, ARGO) == ORDERING_MODE_ORDERED;
  }

  public void setQueryLanguageTypeAndVersion(int mode) throws BXQException {
    xqueryx = check(mode, ARGL) == LANGTYPE_XQUERYX;
  }

  public void setQueryTimeout(int seconds) throws BXQException {
    if(seconds < 0) throw new BXQException(TIME);
    timeout = seconds;
  }

  public void setScrollability(int mode) throws BXQException {
    scrollable = check(mode, ARGR) == SCROLLTYPE_SCROLLABLE;
  }

  private int check(final int val, final String msg) throws BXQException {
    if(val != 1 && val != 2) throw new BXQException(ARG, msg);
    return val;
  }
}
