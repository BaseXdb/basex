package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.InputStream;
import java.io.Reader;
import java.util.GregorianCalendar;
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
import org.basex.core.ProgressException;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.item.Atm;
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
import org.basex.util.Action;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * Java XQuery API - Dynamic Context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class BXQDynamicContext extends BXQAbstract implements XQDynamicContext {
  /** Context. */
  protected final BXQStaticContext sc;
  /** Query processor. */
  protected final XQueryProcessor query;

  /**
   * Constructor.
   * @param in query input
   * @param s static context
   * @param c closer
   */
  protected BXQDynamicContext(final String in, final BXQStaticContext s,
      final BXQConnection c) {
    super(c);
    query = new XQueryProcessor(in);
    sc = s;
  }
  
  public void bindAtomicValue(final QName qn, final String val,
      final XQItemType it) throws XQException {
    bind(qn, new Atm(Token.token(val)), it);
  }

  public void bindBoolean(final QName qn, final boolean val,
      final XQItemType it) throws XQException {
    bind(qn, Bln.get(val), it);
  }

  public void bindByte(final QName qn, final byte val, final XQItemType it)
      throws XQException {
    bind(qn, new Itr(val, Type.BYT), it);
  }

  public void bindDocument(final QName qn, final InputStream is,
      final String base, final XQItemType it) throws XQException {
    bind(qn, createDB(content(is)), it);
  }

  public void bindDocument(final QName qn, final Reader r, final String base,
      final XQItemType it) throws XQException {
    bind(qn, createDB(content(r)), it);
  }

  public void bindDocument(final QName qn, final Source is,
      final XQItemType it) {
    BaseX.notimplemented();
  }

  public void bindDocument(final QName qn, final String val, final String base,
      final XQItemType it) throws XQException {
    check(val, String.class);
    bind(qn, createDB(Token.token(val)), it);
  }

  public void bindDocument(final QName qn, final XMLReader r,
      final XQItemType it) throws XQException {
    check(r, XMLReader.class);
    bind(qn, createDB(r), it);
  }

  public void bindDocument(final QName qn, final XMLStreamReader sr,
      final XQItemType it) throws XQException {
    check(sr, XMLStreamReader.class);
    bind(qn, createDB(sr), it);
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

  public void bindItem(final QName qn, final XQItem it) throws XQException {
    bind(qn, ((BXQItem) it).it, null);
  }

  public void bindLong(final QName qn, final long val, final XQItemType it)
    throws XQException {
    bind(qn, new Itr(val, Type.LNG), it);
  }

  public void bindNode(final QName qn, final Node n, final XQItemType it){
    BaseX.notimplemented();
  }

  public void bindObject(final QName qn, final Object v,
      final XQItemType it) throws XQException {
    check(v, Object.class);
    bind(qn, v instanceof XQItem ? ((BXQItem) v).it : createItem(v), it);
  }

  public void bindSequence(final QName qn, final XQSequence seq) {
    BaseX.notimplemented();
  }

  public void bindShort(final QName qn, final short val,
      final XQItemType it) throws XQException {
    bind(qn, new Itr(val, Type.SHR), it);
  }

  public void bindString(final QName qn, final String val,
      final XQItemType it) throws XQException {
    check(val, String.class);
    bind(qn, Str.get(Token.token(val)), it);
  }

  private TimeZone zone;

  public TimeZone getImplicitTimeZone() throws XQException {
    check();
    return zone != null ? zone : new GregorianCalendar().getTimeZone();
  }

  public void setImplicitTimeZone(final TimeZone tz) throws XQException {
    check();
    zone = tz;
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
    check(var, QName.class);
    
    try {
      final Var v = new Var(new QNm(Token.token(var.getLocalPart())));
      final BXQItemType bit = (BXQItemType) t;
      final Item i = t == null || bit.type == it.type ? it :
          check(t, it.type).e(it, null);
      query.ctx.vars.addGlobal(v.item(i));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Executes the specified query and returns the result iterator.
   * @return result sequence
   * @throws XQException exception
   */
  protected XQResultSequence execute() throws XQException {
    check();
    final XQContext ctx = query.ctx;
    ctx.ns = sc.ctx.ns;
    
    try {
      if(sc.timeout != 0) {
        new Action() {
          public void run() {
            ctx.stop();
          }
        }.delay(sc.timeout * 1000);
      }
      query.create();
      Iter iter = ctx.compile(null).iter();
      if(sc.scrollable && !(iter instanceof SeqIter)) iter = new SeqIter(iter);
      return new BXQSequence(iter, ctx, this, sc, (BXQConnection) par);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    } catch(final ProgressException ex) {
      throw new BXQException(TIMEOUT);
    }
  }
}
