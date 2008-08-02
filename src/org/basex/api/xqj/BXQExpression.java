package org.basex.api.xqj;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;
import org.basex.core.CommandParser;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.iter.Iter;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * XQuery expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXQExpression implements XQExpression {
  /** Database connection. */
  final BXQConnection conn;

  /**
   * Constructor.
   * @param connection connection
   */
  BXQExpression(final BXQConnection connection) {
    conn = connection;
  }

  public void cancel() {}

  public void close() {}

  public void executeCommand(final String cmd) throws XQException {
    try {
      new CommandParser(cmd).parse();
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public void executeCommand(final Reader cmd) {}

  public XQResultSequence executeQuery(final String query) throws XQException {
    try {
      final XQueryProcessor xquery = new XQueryProcessor(query);
      xquery.create();
      final XQContext ctx = xquery.ctx;
      final Iter iter = ctx.compile(conn.ctx.current()).iter();
      return new BXQResultSequence(ctx, iter);
    } catch(final QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    }
  }

  public XQResultSequence executeQuery(final Reader query) throws XQException {
    try {
      return executeQuery(BXQConnection.read(query));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public XQResultSequence executeQuery(final InputStream query)
      throws XQException {
    try {
      return executeQuery(BXQConnection.read(query));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public XQStaticContext getStaticContext() {
    return null;
  }

  public boolean isClosed() {
    return false;
  }

  public void bindAtomicValue(final QName varName, final String value,
      final XQItemType type) {
  }

  public void bindBoolean(final QName varName, final boolean value,
      final XQItemType type) {
  }

  public void bindByte(final QName varName, final byte value,
      final XQItemType type) {
  }

  public void bindDocument(final QName varName, final String value,
      final String baseURI, final XQItemType type) {
  }

  public void bindDocument(final QName varName, final Reader value,
      final String baseURI, final XQItemType type) {
  }

  public void bindDocument(final QName varName, final InputStream value,
      final String baseURI, final XQItemType type) {
  }

  public void bindDocument(final QName varName, final XMLReader value,
      final XQItemType type) {
  }

  public void bindDocument(final QName varName, final XMLStreamReader value,
      final XQItemType type) {
  }

  public void bindDocument(final QName varName, final Source value,
      final XQItemType type) {
  }

  public void bindDouble(final QName varName, final double value,
      final XQItemType type) {
  }

  public void bindFloat(final QName varName, final float value,
      final XQItemType type) {
  }

  public void bindInt(final QName varName, final int value,
      final XQItemType type) {
  }

  public void bindItem(final QName varName, final XQItem value) {
  }

  public void bindLong(final QName varName, final long value,
      final XQItemType type) {
  }

  public void bindNode(final QName varName, final Node value,
      final XQItemType type) {
  }

  public void bindObject(final QName varName, final Object value,
      final XQItemType type) {
  }

  public void bindSequence(final QName varName, final XQSequence value) {
  }

  public void bindShort(final QName varName, final short value,
      final XQItemType type) {
  }

  public void bindString(final QName varName, final String value,
      final XQItemType type) {
  }

  public TimeZone getImplicitTimeZone() {
    return null;
  }

  public void setImplicitTimeZone(final TimeZone implicitTimeZone) {
  }
}
