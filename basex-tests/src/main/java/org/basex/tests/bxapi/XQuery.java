package org.basex.tests.bxapi;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.format.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Wrapper for evaluating XQuery expressions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQuery implements Iterable<XdmItem>, Closeable {
  /** Query processor. */
  private final QueryProcessor qp;
  /** Query iterator. */
  private Iter iter;

  /**
   * Constructor.
   * @param query query
   * @param context database context
   */
  public XQuery(final String query, final Context context) {
    qp = new QueryProcessor(query, context);
  }

  /**
   * Binds an initial context.
   * @param value context value to be bound
   * @return self reference
   * @throws XQueryException exception
   */
  public XQuery context(final XdmValue value) {
    qp.context(value.internal());
    return this;
  }

  /**
   * Binds a variable.
   * @param key key
   * @param value value to be bound
   * @return self reference
   * @throws XQueryException exception
   */
  public XQuery bind(final String key, final XdmValue value) {
    try {
      qp.bind(key, value.internal());
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
   * Declares a decimal format.
   * @param name qname
   * @param map format
   * @return self reference
   */
  public XQuery decimalFormat(final QName name, final HashMap<String, String> map) {
    try {
      final TokenMap tm = new TokenMap();
      for(final Entry<String, String> e : map.entrySet()) {
        tm.put(Token.token(e.getKey()), Token.token(e.getValue()));
      }
      qp.sc.decFormats.put(new QNm(name).id(), new DecFormatter(tm, null));
      return this;
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a collection.
   * @param name name of the collection (can be empty string)
   * @param paths document paths
   * @throws XQueryException exception
   */
  public void addCollection(final String name, final String[] paths) {
    final StringList sl = new StringList();
    for(final String p : paths) sl.add(p);
    try {
      qp.qc.resources.addCollection(name, sl.toArray(), qp.sc);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Returns a collection of document nodes.
   * @param name name of the collection (empty string for default collection)
   * @return reference
   * @throws XQueryException exception
   */
  public XdmValue collection(final String name) {
    try {
      return XdmValue.get(qp.qc.resources.collection(name.isEmpty() ? null :
        new QueryInput(name, qp.sc), null));
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Returns a document node.
   * @param name name of the document
   * @return reference
   * @throws XQueryException exception
   */
  public XdmValue document(final String name) {
    try {
      return XdmItem.get(qp.qc.resources.doc(new QueryInput(name, qp.sc), null));
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a document.
   * @param name name of the document (can be {@code null})
   * @param path document path
   * @throws XQueryException exception
   */
  public void addDocument(final String name, final String path) {
    try {
      qp.qc.resources.addDoc(name, path, qp.sc);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a resource.
   * @param name name of the collection
   * @param strings document path and encoding
   * @throws XQueryException exception
   */
  public void addResource(final String name, final String... strings) {
    qp.qc.resources.addResource(name, strings);
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
    qp.sc.baseURI(base.equals("#UNDEFINED") ? null : base);
    return this;
  }

  /**
   * Returns the next item, or {@code null} if all items have been returned.
   * @return next result item
   * @throws XQueryException exception
   */
  public XdmItem next() {
    Item item = null;
    try {
      if(iter == null) iter = qp.iter();
      item = iter.next();
      return item != null ? XdmItem.get(item) : null;
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    } finally {
      if(item == null) qp.close();
    }
  }

  /**
   * Returns the result value.
   * @return result value
   * @throws XQueryException exception
   */
  public XdmValue value() {
    try {
      final Value value = qp.value();
      value.cache(false, null);
      return XdmValue.get(value);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    } finally {
      qp.close();
    }
  }

  /**
   * Returns the query processor.
   * @return query processor
   */
  public QueryProcessor qp() {
    return qp;
  }

  @Override
  public void close() {
    qp.close();
  }

  @Override
  public Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      private XdmItem next;

      @Override
      public boolean hasNext() {
        if(next == null) next = XQuery.this.next();
        return next != null;
      }

      @Override
      public XdmItem next() {
        final XdmItem item = hasNext() ? next : null;
        next = null;
        return item;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param value optional context
   * @param context database context
   * @return optional expected test suite result
   */
  public static String string(final String query, final XdmValue value, final Context context) {
    final XdmValue xv = new XQuery(query, context).context(value).value();
    return xv.size() == 0 ? "" : xv.getString();
  }

  @Override
  public String toString() {
    return qp.query();
  }
}
