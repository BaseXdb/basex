package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import org.basex.BaseX;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Var;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * Java XQuery API - Dynamic Context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class BXQDynamicContext extends BXQClose implements XQDynamicContext {
  /** Context. */
  protected final BXQStaticContext ctx;
  /** Query processor. */
  protected final XQueryProcessor query;

  /**
   * Constructor.
   * @param c closer
   * @param sc static context
   * @param in query input
   */
  protected BXQDynamicContext(final String in, final BXQStaticContext sc,
      final BXQConnection c) {
    super(c);
    query = new XQueryProcessor(in);
    ctx = sc;
  }
  
  public void bindAtomicValue(final QName qn, final String arg1,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindBoolean(final QName qn, final boolean val,
      final XQItemType it) throws XQException {
    bind(qn, Bln.get(val), it);
  }

  public void bindByte(final QName qn, final byte val, final XQItemType it)
      throws XQException {
    bind(qn, new Itr(val, Type.BYT), it);
  }

  public void bindDocument(final QName qn, final InputStream arg1,
      final String arg2, final XQItemType arg3) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final Reader arg1, final String arg2,
      final XQItemType arg3) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final Source arg1,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final String arg1, final String arg2,
      final XQItemType arg3) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final XMLReader arg1,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final XMLStreamReader arg1,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindDouble(final QName qn, final double val, 
      final XQItemType it) throws XQException {
    bind(qn, Dbl.get(val), it);
  }

  public void bindFloat(final QName qn, final float val, 
      final XQItemType it) throws XQException {
    bind(qn, Flt.get(val), it);
  }

  public void bindInt(final QName qn, final int val, final XQItemType it)
      throws XQException {
    bind(qn, Itr.get(val), it);
  }

  public void bindItem(final QName qn, final XQItem arg1) {
    BaseX.notimplemented();
  }

  public void bindLong(final QName qn, final long val, final XQItemType it)
    throws XQException {
    bind(qn, new Itr(val, Type.LNG), it);
  }

  public void bindNode(final QName qn, final Node arg1, final XQItemType it){
    BaseX.notimplemented();
  }

  public void bindObject(final QName qn, final Object arg1,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindSequence(final QName qn, final XQSequence arg1) {
    BaseX.notimplemented();
  }

  public void bindShort(final QName qn, final short val,
      final XQItemType it) throws XQException {
    bind(qn, new Itr(val, Type.SHR), it);
  }

  public void bindString(final QName qn, final String val,
      final XQItemType it) throws XQException {
    bind(qn, Str.get(Token.token(val)), it);
  }

  public TimeZone getImplicitTimeZone() {
    BaseX.notimplemented();
    return null;
  }

  public void setImplicitTimeZone(final TimeZone arg0) {
    BaseX.notimplemented();
  }

  /**
   * Binds the specified variable to the specified item. 
   * @param var variable
   * @param it item
   * @param t target type
   * @throws XQException query exception
   */
  private void bind(final QName var, final Item it, final XQItemType t)
      throws XQException {
    check();
    check(var);
    try {
      final Var v = new Var(new QNm(Token.token(var.getLocalPart())));
      final BXQItemType bit = (BXQItemType) t;
      final Item i = bit != null && bit.type == it.type ? it :
        check(t, it.type).e(it, null);
      //final Item i = check(t, it.type).e(it, null);
      query.ctx.vars.addGlobal(v.item(i));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Executes the specified query and returns the result iterator.
   * @param sc static context
   * @return result sequence
   * @throws XQException exception
   */
  protected XQResultSequence execute(final BXQStaticContext sc)
      throws XQException {

    check();
    final XQContext ctx = query.ctx;
    ctx.ns = sc.ns;
    
    try {
      query.create();
      Iter iter = ctx.compile(sc.ctx.current()).iter();
      if(sc.scrollable && !(iter instanceof SeqIter)) iter = new SeqIter(iter);
      return new BXQSequence(iter, ctx, this, sc, (BXQConnection) par);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    }
  }
}
