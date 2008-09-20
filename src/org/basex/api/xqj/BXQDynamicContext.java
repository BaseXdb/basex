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
import org.basex.io.IOContent;
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
import org.basex.query.xquery.util.SeqBuilder;
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
    bind(qn, createDB(is), it);
  }

  public void bindDocument(final QName qn, final Reader r, final String base,
      final XQItemType it) throws XQException {
    bind(qn, createDB(r), it);
  }

  public void bindDocument(final QName qn, final Source s,
      final XQItemType it) throws XQException {
    bind(qn, createDB(s, it), it);
  }

  public void bindDocument(final QName qn, final String val, final String base,
      final XQItemType it) throws XQException {
    check(val, String.class);
    bind(qn, createDB(new IOContent(Token.token(val))), it);
  }

  public void bindDocument(final QName qn, final XMLReader r,
      final XQItemType it) throws XQException {
    bind(qn, createDB(r), it);
  }

  public void bindDocument(final QName qn, final XMLStreamReader sr,
      final XQItemType it) throws XQException {
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

  public void bindSequence(final QName qn, final XQSequence seq)
      throws XQException {
    try {
      bind(qn, new SeqBuilder(((BXQSequence) seq).result).finish(), null);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }      
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
   * Binds an item to the specified variable.
   * @param var variable name
   * @param it item to be bound
   * @param t target type
   * @throws XQException query exception
   */
  private void bind(final QName var, final Item it, final XQItemType t)
      throws XQException {
    check();
    check(var, QName.class);

    final QNm name = new QNm(Token.token(var.getLocalPart()));
    Var v = new Var(name);
    if(this instanceof BXQPreparedExpression) {
      v = query.ctx.vars.get(v);
      if(v == null) throw new BXQException(VAR, var);
    } else {
      query.ctx.vars.addGlobal(v);
    }
    
    try {
      v.item(t == null || ((BXQItemType) t).getType() == it.type ? it :
        check(t, it.type).e(it, null));
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
      return new BXQSequence(iter, ctx, this, (BXQConnection) par);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    } catch(final ProgressException ex) {
      throw new BXQException(TIMEOUT);
    }
  }
}
