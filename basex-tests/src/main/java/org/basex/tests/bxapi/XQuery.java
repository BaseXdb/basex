package org.basex.tests.bxapi;

import java.util.*;

import javax.xml.namespace.*;

import org.basex.core.Context;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Wrapper for evaluating XQuery expressions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
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
      Util.debug(ex);
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
      qp.bind(key, value instanceof XdmValue ? ((XdmValue) value).internal() : value);
      return this;
    } catch(final QueryException ex) {
      Util.debug(ex);
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
      Util.debug(ex);
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
      for(final Map.Entry<String, String> e : map.entrySet()) {
        tm.put(Token.token(e.getKey()), Token.token(e.getValue()));
      }
      qp.sc.decFormats.put(new QNm(name).id(), new DecFormatter(null, tm));
      return this;
    } catch(final QueryException ex) {
      Util.debug(ex);
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
      qp.ctx.resources.addCollection(name, sl.toArray(), qp.sc.baseIO());
    } catch(final QueryException ex) {
      Util.debug(ex);
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
      qp.ctx.resources.addDoc(name, path, qp.sc.baseIO());
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw new XQueryException(ex);
    }
  }

  /**
   * Adds a resource.
   * @param name name of the collection
   * @param strings document path, encoding
   * @throws XQueryException exception
   */
  public void addResource(final String name, final String... strings) {
    qp.ctx.resources.addResource(name, strings);
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
    qp.sc.baseURI(base);
    return this;
  }

  /**
   * Creates a function call to the function with the given name with the given arguments.
   * @param fName function name
   * @param args function arguments
   * @return the function call
   */
  public Object funcCall(final String fName, final Expr... args) {
    try {
      final QNm qName = new QNm(fName);
      qName.uri(qName.hasPrefix() ? NSGlobal.uri(qName.prefix()) : qp.sc.funcNS);
      return Functions.get().get(qName, args, qp.sc, null);
    } catch(QueryException ex) {
      Util.debug(ex);
      throw new XQueryException(ex);
    }
  }

  /**
   * Returns the next item, or {@code null} if all items have been returned.
   * @return next result item
   * @throws XQueryException exception
   */
  public XdmItem next() {
    Item it = null;
    try {
      if(ir == null) ir = qp.iter();
      it = ir.next();
      return it != null ? XdmItem.get(it) : null;
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw new XQueryException(ex);
    } finally {
      if(it == null) qp.close();
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
      Util.debug(ex);
      throw new XQueryException(ex);
    } finally {
      qp.close();
    }
  }

  /**
   * Returns serialization properties.
   * @return serialization properties
   */
  public SerializerOptions serializer() {
    return qp.ctx.serParams();
  }

  /**
   * Closes the query; will be called whenever if items have been returned.
   * Should be manually called if not all items are retrieved.
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
        throw Util.notExpected();
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
  public static String string(final String query, final XdmValue val, final Context ctx) {
    final XdmValue xv = new XQuery(query, ctx).context(val).value();
    return xv.size() == 0 ? "" : xv.getString();
  }

  @Override
  public String toString() {
    return qp.query();
  }
}
