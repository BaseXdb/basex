package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.iter.Iter;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * BaseX XQuery prepared expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQPreparedExpression implements XQPreparedExpression  {
  private final XQueryProcessor query;
  private final Nodes nodes;
  private boolean closed;

  /**
   * Constructor.
   * @param xquery query instance
   * @param xnodes node reference
   */
  public BXQPreparedExpression(final XQueryProcessor xquery,
      final Nodes xnodes) {
    query = xquery;
    nodes = xnodes;
  }

  public void bindAtomicValue(final QName qn, final String arg1,
      final XQItemType arg2) {
  }

  public void bindBoolean(final QName qn, final boolean arg1,
      final XQItemType arg2) {
  }

  public void bindByte(final QName qn, final byte arg1, final XQItemType arg2) {
  }

  public void bindDocument(final QName qn, final InputStream arg1,
      final String arg2, final XQItemType arg3) {
  }

  public void bindDocument(final QName qn, final Reader arg1, final String arg2,
      final XQItemType arg3) {
  }

  public void bindDocument(final QName qn, final Source arg1,
      final XQItemType arg2) {
  }

  public void bindDocument(final QName qn, final String arg1, final String arg2,
      final XQItemType arg3) {
  }

  public void bindDocument(final QName qn, final XMLReader arg1,
      final XQItemType arg2) {
  }

  public void bindDocument(final QName qn, final XMLStreamReader arg1,
      final XQItemType arg2) {
  }

  public void bindDouble(final QName qn, final double arg1, 
      final XQItemType arg2) {
  }

  public void bindFloat(final QName qn, final float arg1, 
      final XQItemType arg2) {
  }

  public void bindInt(final QName qn, final int arg1, 
      final XQItemType arg2) {
  }

  public void bindItem(final QName qn, final XQItem arg1) {
  }

  public void bindLong(final QName qn, final long arg1, final XQItemType arg2) {
  }

  public void bindNode(final QName qn, final Node arg1, final XQItemType arg2){
  }

  public void bindObject(final QName qn, final Object arg1,
      final XQItemType arg2) {
  }

  public void bindSequence(final QName qn, final XQSequence arg1) {
  }

  public void bindShort(final QName qn, final short arg1,
      final XQItemType arg2) {
  }

  public void bindString(final QName qn, final String arg1,
      final XQItemType arg2) {
  }

  public void cancel() {
  }

  public void close() {
    closed = true;
  }

  public BXQResultSequence executeQuery() throws XQException {
    try {
      query.create();
      final XQContext ctx = query.ctx;
      final Iter iter = ctx.compile(nodes).iter();
      return new BXQResultSequence(ctx, iter);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    }
  }

  public QName[] getAllExternalVariables() {
    return null;
  }

  public QName[] getAllUnboundExternalVariables() {
    return null;
  }

  public TimeZone getImplicitTimeZone() {
    return null;
  }

  public XQStaticContext getStaticContext() {
    return null;
  }

  public XQSequenceType getStaticResultType() {
    return null;
  }

  public XQSequenceType getStaticVariableType(final QName qn) {
    return null;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setImplicitTimeZone(final TimeZone arg0) {
  }
}
