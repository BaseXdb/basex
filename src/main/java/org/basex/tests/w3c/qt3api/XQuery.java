package org.basex.tests.w3c.qt3api;

import java.util.Iterator;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.util.Util;

/**
 * Wrapper for evaluating XQuery expressions.
 */
public final class XQuery implements Iterable<XQItem> {
  /** Query processor. */
  final QueryProcessor qp;
  /** Query iterator. */
  Iter ir;

  /**
   * Constructor.
   * @param query query
   * @param ctx database context
   */
  public XQuery(final String query, final Context ctx) {
    qp = new QueryProcessor(query, ctx);
  }

  /**
   * Binds an initial context.
   * @param value context value to be bound
   * @return self reference
   * @throws XQException exception
   */
  public XQuery context(final XQValue value) {
    try {
      if(value != null) qp.context(value.internal());
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
    return this;
  }

  /**
   * Binds a variable.
   * @param key key
   * @param value value to be bound
   * @return self reference
   * @throws XQException exception
   */
  public XQuery bind(final String key, final XQValue value) {
    try {
      qp.bind(key, value.internal());
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
    return this;
  }

  /**
   * Returns the next result item.
   * @return next item
   * @throws XQException exception
   */
  public XQItem next() {
    try {
      if(ir == null) ir = qp.iter();
      return XQItem.get(ir.next());
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  /**
   * Returns the result value.
   * @return result value
   * @throws XQException exception
   */
  public XQValue value() {
    try {
      return XQValue.get(qp.value());
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  /**
   * Closes the query; should always be called after all items have been
   * processed.
   * @throws XQException exception
   */
  public void close() {
    try {
      qp.close();
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  @Override
  public Iterator<XQItem> iterator() {
    return new Iterator<XQItem>() {
      /** Current item. */
      private XQItem next;

      @Override
      public boolean hasNext() {
        if(next == null) next = XQuery.this.next();
        return next != null;
      }

      @Override
      public XQItem next() {
        final XQItem it = hasNext() ? next : null;
        next = null;
        return it;
      }

      @Override
      public void remove() {
        Util.notexpected();
      }
    };
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param val optional context
   * @param ctx database context
   * @return optional expected test suite result
   */
  public static String string(final String query, final XQValue val,
      final Context ctx) {

    final XQuery qp = new XQuery(query, ctx).context(val);
    try {
      final XQItem it = qp.next();
      return it == null ? "" : it.getString();
    } finally {
      qp.close();
    }
  }
}
