package org.basex.api.xqj;

import static javax.xml.xquery.XQConstants.*;
import static org.basex.api.xqj.BXQText.*;
import static org.basex.util.Token.*;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;

import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.StaticContext;
import org.basex.query.item.SeqType;
import org.basex.query.item.Uri;

/**
 * Java XQuery API - Static Context.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXQStaticContext implements XQStaticContext {
  /** Static context. */
  final StaticContext sc;
  /** Forward flag. */
  boolean scrollable;
  /** Timeout. */
  int timeout;

  /** Binding mode (immediate). */
  private boolean binding = true;
  /** Holdability. */
  private boolean holdability = true;
  /** Language mode. */
  private boolean xqueryx = true;

  /**
   * Constructor, specifying a user name and password.
   * @param name user name
   * @param pw password
   * @throws XQException if authentication fails
   */
  protected BXQStaticContext(final String name, final String pw)
      throws XQException {

    if(name != null) {
      final Context ctx = BXQDataSource.context();
      ctx.user = ctx.users.get(name);
      if(ctx.user == null || !string(ctx.user.password).equals(md5(pw)))
        throw new BXQException(DENIED, name);
    }
    sc = new StaticContext();
 }

  @Override
  public void declareNamespace(final String prefix, final String uri)
      throws XQException {

    try {
      BXQAbstract.valid(prefix, String.class);
      BXQAbstract.valid(uri, String.class);
      sc.namespace(prefix, uri);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public String getBaseURI() {
    final IO io = sc.baseIO();
    return io != null ? io.url() : "";
  }

  @Override
  public int getBindingMode() {
    return binding ? BINDING_MODE_IMMEDIATE : BINDING_MODE_DEFERRED;
  }

  @Override
  public int getBoundarySpacePolicy() {
    return sc.spaces ? BOUNDARY_SPACE_PRESERVE : BOUNDARY_SPACE_STRIP;
  }

  @Override
  public int getConstructionMode() {
    return sc.construct ? CONSTRUCTION_MODE_PRESERVE : CONSTRUCTION_MODE_STRIP;
  }

  @Override
  public XQItemType getContextItemStaticType() {
    return sc.initType == null ? null : new BXQItemType(sc.initType.type);
  }

  @Override
  public int getCopyNamespacesModeInherit() {
    return sc.nsInherit ? COPY_NAMESPACES_MODE_INHERIT :
      COPY_NAMESPACES_MODE_NO_INHERIT;
  }

  @Override
  public int getCopyNamespacesModePreserve() {
    return sc.nsPreserve ? COPY_NAMESPACES_MODE_PRESERVE :
      COPY_NAMESPACES_MODE_NO_PRESERVE;
  }

  @Override
  public String getDefaultCollation() {
    return string(sc.collation.string());
  }

  @Override
  public String getDefaultElementTypeNamespace() {
    return sc.nsElem == null ? "" : string(sc.nsElem);
  }

  @Override
  public String getDefaultFunctionNamespace() {
    return sc.nsFunc == null ? "" : string(sc.nsFunc);
  }

  @Override
  public int getDefaultOrderForEmptySequences() {
    return sc.orderGreatest ? DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST :
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST;
  }

  @Override
  public int getHoldability() {
    return holdability ? HOLDTYPE_HOLD_CURSORS_OVER_COMMIT :
      HOLDTYPE_CLOSE_CURSORS_AT_COMMIT;
  }

  @Override
  public String[] getNamespacePrefixes() {
    final byte[][] atts = sc.ns.prefixes();
    final String[] pre = new String[atts.length];
    for(int p = 0; p < pre.length; ++p) pre[p] = string(atts[p]);
    return pre;
  }

  @Override
  public String getNamespaceURI(final String prefix) throws XQException {
    BXQAbstract.valid(prefix, String.class);
    final byte[] uri = sc.ns.staticURI(token(prefix));
    if(uri != null) return string(uri);
    throw new BXQException(PRE, prefix);
  }

  @Override
  public int getOrderingMode() {
    return sc.ordered ? ORDERING_MODE_ORDERED : ORDERING_MODE_UNORDERED;
  }

  @Override
  public int getQueryLanguageTypeAndVersion() {
    return xqueryx ? LANGTYPE_XQUERYX : LANGTYPE_XQUERY;
  }

  @Override
  public int getQueryTimeout() {
    return timeout;
  }

  @Override
  public int getScrollability() {
    return scrollable ? SCROLLTYPE_SCROLLABLE : SCROLLTYPE_FORWARD_ONLY;
  }

  @Override
  public void setBaseURI(final String baseUri) throws XQException {
    BXQAbstract.valid(baseUri, String.class);
    sc.baseURI(baseUri);
  }

  @Override
  public void setBindingMode(final int mode) throws BXQException {
    if(mode != 0 && mode != 1) throw new BXQException(ARG, ARGB);
    binding = mode == BINDING_MODE_IMMEDIATE;
  }

  @Override
  public void setBoundarySpacePolicy(final int mode) throws BXQException {
    sc.spaces = check(mode, ARGS) == BOUNDARY_SPACE_PRESERVE;
  }

  @Override
  public void setConstructionMode(final int mode) throws XQException {
    sc.construct = check(mode, ARGC) == CONSTRUCTION_MODE_PRESERVE;
  }

  @Override
  public void setContextItemStaticType(final XQItemType contextItemType)
      throws XQException {

    sc.initType = contextItemType == null ? null :
      SeqType.get(((BXQItemType) contextItemType).getType(), 1);
  }

  @Override
  public void setCopyNamespacesModeInherit(final int mode) throws BXQException {
    sc.nsInherit = check(mode, ARGN) == COPY_NAMESPACES_MODE_INHERIT;
  }

  @Override
  public void setCopyNamespacesModePreserve(final int m) throws BXQException {
    sc.nsPreserve = check(m, ARGN) == COPY_NAMESPACES_MODE_PRESERVE;
  }

  @Override
  public void setDefaultCollation(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    sc.collation = Uri.uri(token(uri));
  }

  @Override
  public void setDefaultElementTypeNamespace(final String uri)
      throws XQException {
    BXQAbstract.valid(uri, String.class);
    sc.nsElem = !uri.isEmpty() ? token(uri) : null;
  }

  @Override
  public void setDefaultFunctionNamespace(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    sc.nsFunc = !uri.isEmpty() ? token(uri) : null;
  }

  @Override
  public void setDefaultOrderForEmptySequences(final int mode)
      throws BXQException {
    sc.orderGreatest = check(mode, ARGO) ==
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST;
  }

  @Override
  public void setHoldability(final int hold) throws BXQException {
    holdability = check(hold, ARGH) == HOLDTYPE_HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public void setOrderingMode(final int mode) throws BXQException {
    sc.ordered = check(mode, ARGO) == ORDERING_MODE_ORDERED;
  }

  @Override
  public void setQueryLanguageTypeAndVersion(final int m) throws BXQException {
    xqueryx = check(m, ARGL) == LANGTYPE_XQUERYX;
  }

  @Override
  public void setQueryTimeout(final int seconds) throws BXQException {
    if(seconds < 0) throw new BXQException(TIME);
    timeout = seconds;
  }

  @Override
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
  private static int check(final int val, final String msg)
      throws BXQException {
    if(val != 1 && val != 2) throw new BXQException(ARG, msg);
    return val;
  }
}
