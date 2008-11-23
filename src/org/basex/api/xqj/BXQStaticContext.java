package org.basex.api.xqj;

import static javax.xml.xquery.XQConstants.*;
import static org.basex.api.xqj.BXQText.*;
import static org.basex.util.Token.*;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Atts;

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

  public void declareNamespace(final String prefix, final String uri)
      throws XQException {
    try {
      BXQAbstract.valid(prefix, String.class);
      BXQAbstract.valid(uri, String.class);
      final QNm name = new QNm(token(prefix), Uri.uri(token(uri)));
      if(uri.length() != 0) ctx.ns.add(name);
      else ctx.ns.delete(name);
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public String getBaseURI() {
    return string(ctx.baseURI.str());
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
    return string(ctx.collation.str());
  }

  public String getDefaultElementTypeNamespace() {
    return string(ctx.nsElem);
  }

  public String getDefaultFunctionNamespace() {
    return string(ctx.nsFunc);
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
    final Atts atts = ctx.ns.atts;
    final String[] pre = new String[atts.size];
    for(int p = 0; p < pre.length; p++) pre[p] = string(atts.key[p]);
    return pre;
  }

  public String getNamespaceURI(final String prefix) throws XQException {
    BXQAbstract.valid(prefix, String.class);
    final byte[] uri = ctx.ns.find(token(prefix));
    if(uri != null) return string(uri);
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

  public void setBaseURI(final String baseUri) throws XQException {
    BXQAbstract.valid(baseUri, String.class);
    ctx.baseURI = Uri.uri(token(baseUri));
  }

  public void setBindingMode(final int mode) throws BXQException {
    if(mode != 0 && mode != 1) throw new BXQException(ARG, ARGB);
    binding = mode == BINDING_MODE_IMMEDIATE;
  }

  public void setBoundarySpacePolicy(final int mode) throws BXQException {
    ctx.spaces = check(mode, ARGS) == BOUNDARY_SPACE_PRESERVE;
  }

  public void setConstructionMode(final int mode) throws XQException {
    ctx.construct = check(mode, ARGC) == CONSTRUCTION_MODE_PRESERVE;
  }

  public void setContextItemStaticType(final XQItemType contextItemType) {
    type = contextItemType;
  }

  public void setCopyNamespacesModeInherit(final int mode) throws BXQException {
    ctx.nsInherit = check(mode, ARGN) == COPY_NAMESPACES_MODE_INHERIT;
  }

  public void setCopyNamespacesModePreserve(final int m) throws BXQException {
    ctx.nsPreserve = check(m, ARGN) == COPY_NAMESPACES_MODE_PRESERVE;
  }

  public void setDefaultCollation(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.collation = Uri.uri(token(uri));
  }

  public void setDefaultElementTypeNamespace(final String uri)
      throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.nsElem = token(uri);
  }

  public void setDefaultFunctionNamespace(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.nsFunc = token(uri);
  }

  public void setDefaultOrderForEmptySequences(final int mode)
      throws BXQException {
    ctx.orderGreatest = check(mode, ARGO) ==
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST;
  }

  public void setHoldability(final int hold) throws BXQException {
    holdability = check(hold, ARGH) == HOLDTYPE_HOLD_CURSORS_OVER_COMMIT;
  }

  public void setOrderingMode(final int mode) throws BXQException {
    ctx.ordered = check(mode, ARGO) == ORDERING_MODE_ORDERED;
  }

  public void setQueryLanguageTypeAndVersion(final int m) throws BXQException {
    xqueryx = check(m, ARGL) == LANGTYPE_XQUERYX;
  }

  public void setQueryTimeout(final int seconds) throws BXQException {
    if(seconds < 0) throw new BXQException(TIME);
    timeout = seconds;
  }

  public void setScrollability(final int mode) throws BXQException {
    scrollable = check(mode, ARGR) == SCROLLTYPE_SCROLLABLE;
  }

  /**
   * Performs a value check.
   * @param val input value
   * @param msg error message
   * @return specified input value
   * @throws BXQException exception
   */
  private int check(final int val, final String msg) throws BXQException {
    if(val != 1 && val != 2) throw new BXQException(ARG, msg);
    return val;
  }
}
