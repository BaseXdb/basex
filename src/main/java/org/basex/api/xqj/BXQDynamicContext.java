package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQCancelledException;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQSequence;
import org.basex.core.ProgressException;
import org.basex.io.IOContent;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.w3c.dom.Node;

/**
 * Java XQuery API - Dynamic Context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class BXQDynamicContext extends BXQAbstract
    implements XQDynamicContext {

  /** Context. */
  protected final BXQStaticContext sc;
  /** Query processor. */
  protected final QueryProcessor qp;
  /** Time zone. */
  private TimeZone zone;

  /**
   * Constructor.
   * @param s static context
   * @param qu query
   * @param c closer
   */
  protected BXQDynamicContext(final String qu, final BXQStaticContext s,
      final BXQConnection c) {
    super(c);
    sc = s;
    qp = new QueryProcessor(qu, s.context);
    qp.ctx.copy(sc.ctx);
  }

  public void bindAtomicValue(final QName qn, final String v,
      final XQItemType t) throws XQException {
    valid(t, XQItemType.class);
    bind(qn, new Atm(Token.token(valid(v, String.class).toString())), t);
  }

  public void bindBoolean(final QName qn, final boolean v, final XQItemType it)
      throws XQException {
    bind(qn, Bln.get(v), it);
  }

  public void bindByte(final QName qn, final byte v, final XQItemType t)
      throws XQException {
    bind(qn, Itr.get(v, Type.BYT), t);
  }

  public void bindDocument(final QName qn, final InputStream is,
      final String base, final XQItemType t) throws XQException {
    bind(qn, createDB(is), t);
  }

  public void bindDocument(final QName qn, final Reader r, final String base,
      final XQItemType t) throws XQException {
    bind(qn, createDB(r), t);
  }

  public void bindDocument(final QName qn, final Source s, final XQItemType t)
      throws XQException {
    bind(qn, createDB(s, t), t);
  }

  public void bindDocument(final QName qn, final String v, final String base,
      final XQItemType t) throws XQException {
    valid(v, String.class);
    bind(qn, createDB(new IOContent(Token.token(v))), t);
  }

  public void bindDocument(final QName qn, final XMLStreamReader sr,
      final XQItemType t) throws XQException {
    bind(qn, createDB(sr), t);
  }

  public void bindDouble(final QName qn, final double v, final XQItemType t)
      throws XQException {
    bind(qn, Dbl.get(v), t);
  }

  public void bindFloat(final QName qn, final float v, final XQItemType t)
      throws XQException {
    bind(qn, Flt.get(v), t);
  }

  public void bindInt(final QName qn, final int v, final XQItemType t)
      throws XQException {
    bind(qn, Itr.get(v), t);
  }

  public void bindItem(final QName qn, final XQItem t) throws XQException {
    valid(t, XQItem.class);
    bind(qn, ((BXQItem) t).it, null);
  }

  public void bindLong(final QName qn, final long v, final XQItemType t)
      throws XQException {
    bind(qn, new Dec(new BigDecimal(v), Type.LNG), t);
  }

  public void bindNode(final QName qn, final Node n, final XQItemType t)
      throws XQException {
    bind(qn, create(n, null), t);
  }

  public void bindObject(final QName qn, final Object v, final XQItemType t)
      throws XQException {
    bind(qn, create(v, null), t);
  }

  public void bindSequence(final QName qn, final XQSequence s)
      throws XQException {

    valid(s, XQSequence.class);
    try {
      bind(qn, ((BXQSequence) s).result.finish(), null);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public void bindShort(final QName qn, final short v, final XQItemType t)
      throws XQException {
    bind(qn, Itr.get(v, Type.SHR), t);
  }

  public void bindString(final QName qn, final String v, final XQItemType t)
      throws XQException {
    bind(qn, Str.get(valid(v, String.class)), t);
  }

  public TimeZone getImplicitTimeZone() throws XQException {
    opened();
    return zone != null ? zone : new GregorianCalendar().getTimeZone();
  }

  public void setImplicitTimeZone(final TimeZone tz) throws XQException {
    opened();
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
    opened();

    valid(var, QName.class);

    final Type tt = check(it.type, t);
    Item i = it;
    if(tt != it.type) {
      try {
        i = tt.e(it, null);
      } catch(final QueryException ex) {
        throw new BXQException(ex);
      }
    }

    if(var == XQConstants.CONTEXT_ITEM) {
      qp.ctx.item = i;
    } else {
      final QNm name = new QNm(Token.token(var.getLocalPart()));
      Var v = new Var(name, true);
      if(this instanceof BXQPreparedExpression) {
        v = qp.ctx.vars.get(v);
        if(v == null) throw new BXQException(VAR, var);
      } else {
        qp.ctx.vars.addGlobal(v);
      }

      try {
        v.bind(i, null);
      } catch(final QueryException ex) {
        throw new BXQException(ex);
      }
    }
  }

  /**
   * Executes the specified query and returns the result iterator.
   * @return result sequence
   * @throws XQException exception
   */
  protected final BXQSequence execute() throws XQException {
    opened();
    final QueryContext qctx = qp.ctx;
    qctx.ns = sc.ctx.ns;

    try {
      if(sc.timeout != 0) {
        new Thread() {
          @Override
          public void run() {
            Performance.sleep(sc.timeout * 1000);
            qctx.stop();
          }
        }.start();
      }
      qp.parse();
      qctx.compile();
      Iter iter = qctx.iter();
      if(sc.scrollable) iter = SeqIter.get(iter);
      return new BXQSequence(iter, this, (BXQConnection) par);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage(), new QName(ex.code()),
          ex.line(), ex.col(), -1);
    } catch(final ProgressException ex) {
      throw new XQCancelledException(TIMEOUT, null, null, -1, -1, -1,
          null, null, null);
    }
  }

  @Override
  public final void close() throws XQException {
    try {
      if(!closed) qp.close();
    } catch(final IOException ex) {
      throw new XQQueryException(ex.getMessage());
    }
    super.close();
  }
}
