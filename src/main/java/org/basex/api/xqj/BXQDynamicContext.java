package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
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
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.w3c.dom.Node;

/**
 * Java XQuery API - Dynamic Context.
 *
 * @author BaseX Team 2005-12, BSD License
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
    qp = new QueryProcessor(qu, BXQDataSource.context());
    qp.ctx.sc.copy(sc.sc);
  }

  @Override
  public void bindAtomicValue(final QName qn, final String v, final XQItemType t)
      throws XQException {
    valid(t, XQItemType.class);
    bind(qn, new Atm(valid(v, String.class).toString()), t);
  }

  @Override
  public void bindBoolean(final QName qn, final boolean v, final XQItemType it)
      throws XQException {
    bind(qn, Bln.get(v), it);
  }

  @Override
  public void bindByte(final QName qn, final byte v, final XQItemType t)
      throws XQException {
    bind(qn, Int.get(v, AtomType.BYT), t);
  }

  @Override
  public void bindDocument(final QName qn, final InputStream is, final String base,
      final XQItemType t) throws XQException {
    bind(qn, createNode(is), t);
  }

  @Override
  public void bindDocument(final QName qn, final Reader r, final String base,
      final XQItemType t) throws XQException {
    bind(qn, createNode(r), t);
  }

  @Override
  public void bindDocument(final QName qn, final Source s, final XQItemType t)
      throws XQException {
    bind(qn, createNode(s, t), t);
  }

  @Override
  public void bindDocument(final QName qn, final String v, final String base,
      final XQItemType t) throws XQException {
    valid(v, String.class);
    bind(qn, createNode(new IOContent(v)), t);
  }

  @Override
  public void bindDocument(final QName qn, final XMLStreamReader sr, final XQItemType t)
      throws XQException {
    bind(qn, createNode(sr), t);
  }

  @Override
  public void bindDouble(final QName qn, final double v, final XQItemType t)
      throws XQException {
    bind(qn, Dbl.get(v), t);
  }

  @Override
  public void bindFloat(final QName qn, final float v, final XQItemType t)
      throws XQException {
    bind(qn, Flt.get(v), t);
  }

  @Override
  public void bindInt(final QName qn, final int v, final XQItemType t)
      throws XQException {
    bind(qn, Int.get(v), t);
  }

  @Override
  public void bindItem(final QName qn, final XQItem t) throws XQException {
    valid(t, XQItem.class);
    bind(qn, ((BXQItem) t).it, null);
  }

  @Override
  public void bindLong(final QName qn, final long v, final XQItemType t)
      throws XQException {
    bind(qn, new Dec(new BigDecimal(v), AtomType.LNG), t);
  }

  @Override
  public void bindNode(final QName qn, final Node v, final XQItemType t)
      throws XQException {
    bind(qn, create(v, null), t);
  }

  @Override
  public void bindObject(final QName qn, final Object v, final XQItemType t)
      throws XQException {
    bind(qn, create(v, null), t);
  }

  @Override
  public void bindSequence(final QName qn, final XQSequence s) throws XQException {
    valid(s, XQSequence.class);
    try {
      bind(qn, ((BXQSequence) s).result.value(), null);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public void bindShort(final QName qn, final short v, final XQItemType t)
      throws XQException {
    bind(qn, Int.get(v, AtomType.SHR), t);
  }

  @Override
  public void bindString(final QName qn, final String v, final XQItemType t)
      throws XQException {
    bind(qn, Str.get(valid(v, String.class)), t);
  }

  @Override
  public TimeZone getImplicitTimeZone() throws XQException {
    opened();
    return zone != null ? zone : new GregorianCalendar().getTimeZone();
  }

  @Override
  public void setImplicitTimeZone(final TimeZone tz) throws XQException {
    opened();
    zone = tz;
  }

  /**
   * Binds an item to the specified variable.
   * @param var variable name
   * @param v value to be bound
   * @param t target type
   * @throws XQException query exception
   */
  private void bind(final QName var, final Value v, final XQItemType t)
      throws XQException {

    opened();
    valid(var, QName.class);

    final Type tt = check(v.type, t);
    Value vl = v;
    // don't cast sequences
    if(tt != v.type && v instanceof Item) {
      try {
        vl = tt.cast((Item) v, qp.ctx, null);
      } catch(final QueryException ex) {
        throw new BXQException(ex);
      }
    }

    try {
      if(var == XQConstants.CONTEXT_ITEM) {
        qp.context(vl);
      } else {
        if(this instanceof BXQPreparedExpression) {
          final Var vr = qp.ctx.vars.get(new QNm(var));
          if(vr == null) throw new BXQException(VAR, var);
          vr.bind(vl, null);
        } else {
          qp.bind(var.getLocalPart(), vl);
        }
      }
    } catch(final QueryException ex) {
      throw new BXQException(ex);
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
    qctx.sc.ns = sc.sc.ns;

    try {
      if(sc.timeout != 0) {
        new Thread() {
          @Override
          public void run() {
            Performance.sleep(sc.timeout * 1000l);
            qctx.stop();
          }
        }.start();
      }
      qp.parse();
      qctx.compile();
      Iter iter = qctx.iter();
      if(sc.scrollable) iter = iter.value().cache();
      return new BXQSequence(iter, this, (BXQConnection) par);
    } catch(final QueryException ex) {
      final QNm qnm = ex.qname();
      throw new XQQueryException(ex.getMessage(),
        new QName(Token.string(qnm.uri()), Token.string(qnm.local())),
        ex.line(), ex.col(), -1);
    } catch(final ProgressException ex) {
      throw new XQCancelledException(TIMEOUT, null, null, -1, -1, -1,
          null, null, null);
    }
  }

  @Override
  public final void close() throws XQException {
    if(!closed) qp.close();
    super.close();
  }
}
