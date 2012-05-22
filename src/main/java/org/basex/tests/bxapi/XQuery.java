package org.basex.tests.bxapi;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Wrapper for evaluating XQuery expressions.
 */
public final class XQuery implements Iterable<XdmItem> {
  /** Query processor. */
  private final QueryProcessor qp;
  /** Query iterator. */
  private Iter ir;

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
   * @throws XQueryException exception
   */
  public XQuery context(final Object value) {
    try {
      if(value != null) qp.context(value instanceof XdmValue ?
          ((XdmValue) value).internal() : value);
      return this;
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Binds a variable.
   * @param key key
   * @param value value to be bound
   * @return self reference
   * @throws XQueryException exception
   */
  public XQuery bind(final String key, final Object value) {
    try {
      qp.bind(key, value instanceof XdmValue ?
          ((XdmValue) value).internal() : value);
      return this;
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the {@code uri} is an empty string.
   * The default element namespaces is set if the {@code prefix} is empty.
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @return self reference
   */
  public XQuery namespace(final String prefix, final String uri) {
    try {
      qp.namespace(prefix, uri);
      return this;
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a collection.
   * @param name name of the collection
   * @param paths document paths
   * @throws XQueryException exception
   */
  public void addCollection(final String name, final String[] paths) {
    final StringList sl = new StringList();
    for(final String p : paths) sl.add(p);
    try {
      qp.ctx.resource.addCollection(name, sl.toArray());
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a document.
   * @param name name of the collection
   * @param path document path
   * @throws XQueryException exception
   */
  public void addDocument(final String name, final String path) {
    try {
      qp.ctx.resource.addDoc(name, path);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a resource.
   * @param name name of the collection
   * @param path document path
   * @throws XQueryException exception
   */
  public void addResource(final String name, final String path) {
    qp.ctx.resource.addResource(name, path);
  }

  /**
   * Adds a module.
   * @param uri module uri
   * @param file file reference
   * @throws XQueryException exception
   */
  public void addModule(final String uri, final String file) {
    qp.module(uri, file);
  }

  /**
   * Sets the base URI.
   * @param base base URI
   * @return self reference
   * @throws XQueryException exception
   */
  public XQuery baseURI(final String base) {
    qp.ctx.sc.baseURI(base);
    return this;
  }

  /**
   * Returns the next item, or {@code null} if all items have been returned.
   * @return next result item
   * @throws XQueryException exception
   */
  public XdmItem next() {
    try {
      if(ir == null) ir = qp.iter();
      return XdmItem.get(ir.next());
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Returns the result value.
   * @return result value
   * @throws XQueryException exception
   */
  public XdmValue value() {
    try {
      return XdmValue.get(qp.value());
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Closes the query; should always be called after all items have been
   * processed.
   * @throws XQueryException exception
   */
  public void close() {
    qp.close();
  }

  @Override
  public Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      /** Current item. */
      private XdmItem next;

      @Override
      public boolean hasNext() {
        if(next == null) next = XQuery.this.next();
        return next != null;
      }

      @Override
      public XdmItem next() {
        final XdmItem it = hasNext() ? next : null;
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
  public static String string(final String query, final XdmValue val,
      final Context ctx) {

    final XQuery qp = new XQuery(query, ctx).context(val);
    try {
      final XdmItem it = qp.next();
      return it == null ? "" : it.getString();
    } finally {
      qp.close();
    }
  }

  @Override
  public String toString() {
    return qp.query();
  }
}
