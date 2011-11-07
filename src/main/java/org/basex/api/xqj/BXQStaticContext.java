package org.basex.api.xqj;

import static javax.xml.xquery.XQConstants.*;
import static org.basex.api.xqj.BXQText.*;
import static org.basex.util.Token.*;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;
import org.basex.core.Context;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.item.Uri;

/**
 * Java XQuery API - Static Context.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class BXQStaticContext implements XQStaticContext {
  /** Database context. */
  final Context context;
  /** Query context. */
  final QueryContext ctx;
  /** Forward flag. */
  boolean scrollable;
  /** Timeout. */
  int timeout;

  /** Context item type. */
  private XQItemType type;
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

    context = new Context();
    if(name != null) {
      context.user = context.users.get(name);
      if(context.user == null || !string(context.user.password).equals(md5(pw)))
        throw new BXQException(DENIED, name);
    }
    ctx = new QueryContext(context);
 }

  @Override
  public void declareNamespace(final String prefix, final String uri)
      throws XQException {
    try {
      BXQAbstract.valid(prefix, String.class);
      BXQAbstract.valid(uri, String.class);
      final QNm name = new QNm(token(prefix), token(uri));
      if(!uri.isEmpty()) ctx.ns.add(name, null);
      else ctx.ns.delete(name);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public String getBaseURI() {
    return string(ctx.baseURI.atom());
  }

  @Override
  public int getBindingMode() {
    return binding ? BINDING_MODE_IMMEDIATE : BINDING_MODE_DEFERRED;
  }

  @Override
  public int getBoundarySpacePolicy() {
    return ctx.spaces ? BOUNDARY_SPACE_PRESERVE : BOUNDARY_SPACE_STRIP;
  }

  @Override
  public int getConstructionMode() {
    return ctx.construct ? CONSTRUCTION_MODE_PRESERVE : CONSTRUCTION_MODE_STRIP;
  }

  @Override
  public XQItemType getContextItemStaticType() {
    return type;
  }

  @Override
  public int getCopyNamespacesModeInherit() {
    return ctx.nsInherit ? COPY_NAMESPACES_MODE_INHERIT :
      COPY_NAMESPACES_MODE_NO_INHERIT;
  }

  @Override
  public int getCopyNamespacesModePreserve() {
    return ctx.nsPreserve ? COPY_NAMESPACES_MODE_PRESERVE :
      COPY_NAMESPACES_MODE_NO_PRESERVE;
  }

  @Override
  public String getDefaultCollation() {
    return string(ctx.collation.atom());
  }

  @Override
  public String getDefaultElementTypeNamespace() {
    return string(ctx.nsElem);
  }

  @Override
  public String getDefaultFunctionNamespace() {
    return string(ctx.nsFunc);
  }

  @Override
  public int getDefaultOrderForEmptySequences() {
    return ctx.orderGreatest ? DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST :
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST;
  }

  @Override
  public int getHoldability() {
    return holdability ? HOLDTYPE_HOLD_CURSORS_OVER_COMMIT :
      HOLDTYPE_CLOSE_CURSORS_AT_COMMIT;
  }

  @Override
  public String[] getNamespacePrefixes() {
    final byte[][] atts = ctx.ns.prefixes();
    final String[] pre = new String[atts.length];
    for(int p = 0; p < pre.length; ++p) pre[p] = string(atts[p]);
    return pre;
  }

  @Override
  public String getNamespaceURI(final String prefix) throws XQException {
    BXQAbstract.valid(prefix, String.class);
    final byte[] uri = ctx.ns.find(token(prefix));
    if(uri != null) return string(uri);
    throw new BXQException(PRE, prefix);
  }

  @Override
  public int getOrderingMode() {
    return ctx.ordered ? ORDERING_MODE_ORDERED : ORDERING_MODE_UNORDERED;
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
    ctx.base(baseUri);
  }

  @Override
  public void setBindingMode(final int mode) throws BXQException {
    if(mode != 0 && mode != 1) throw new BXQException(ARG, ARGB);
    binding = mode == BINDING_MODE_IMMEDIATE;
  }

  @Override
  public void setBoundarySpacePolicy(final int mode) throws BXQException {
    ctx.spaces = check(mode, ARGS) == BOUNDARY_SPACE_PRESERVE;
  }

  @Override
  public void setConstructionMode(final int mode) throws XQException {
    ctx.construct = check(mode, ARGC) == CONSTRUCTION_MODE_PRESERVE;
  }

  @Override
  public void setContextItemStaticType(final XQItemType contextItemType) {
    type = contextItemType;
  }

  @Override
  public void setCopyNamespacesModeInherit(final int mode) throws BXQException {
    ctx.nsInherit = check(mode, ARGN) == COPY_NAMESPACES_MODE_INHERIT;
  }

  @Override
  public void setCopyNamespacesModePreserve(final int m) throws BXQException {
    ctx.nsPreserve = check(m, ARGN) == COPY_NAMESPACES_MODE_PRESERVE;
  }

  @Override
  public void setDefaultCollation(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.collation = Uri.uri(token(uri));
  }

  @Override
  public void setDefaultElementTypeNamespace(final String uri)
      throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.nsElem = token(uri);
  }

  @Override
  public void setDefaultFunctionNamespace(final String uri) throws XQException {
    BXQAbstract.valid(uri, String.class);
    ctx.nsFunc = token(uri);
  }

  @Override
  public void setDefaultOrderForEmptySequences(final int mode)
      throws BXQException {
    ctx.orderGreatest = check(mode, ARGO) ==
      DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST;
  }

  @Override
  public void setHoldability(final int hold) throws BXQException {
    holdability = check(hold, ARGH) == HOLDTYPE_HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public void setOrderingMode(final int mode) throws BXQException {
    ctx.ordered = check(mode, ARGO) == ORDERING_MODE_ORDERED;
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
  private int check(final int val, final String msg) throws BXQException {
    if(val != 1 && val != 2) throw new BXQException(ARG, msg);
    return val;
  }
}
