package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.expr.Expr;
import org.basex.query.func.JavaFunc;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.json.JsonMapConverter;

/**
 * This class is an entry point for evaluating XQuery implementations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Progress {
  /** Expression context. */
  public final QueryContext ctx;
  /** Query. */
  private String query;
  /** Parsed flag. */
  private boolean parsed;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Default constructor.
   * @param qu query to process
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Context cx) {
    this(qu, cx.current(), cx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param nodes initial context set
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Nodes nodes, final Context cx) {
    query = qu;
    ctx = new QueryContext(cx);
    ctx.nodes = nodes;
    progress(ctx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param o initial context expression
   * @param cx database context
   * @throws QueryException query exception
   */
  public QueryProcessor(final String qu, final Object o, final Context cx)
      throws QueryException {
    this(qu, o instanceof ItemCache ? ((ItemCache) o).value() :
        o instanceof Expr ? (Expr) o : JavaFunc.type(o).e(o, null), cx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param expr initial context expression
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Expr expr, final Context cx) {
    query = qu;
    ctx = new QueryContext(cx);
    ctx.ctxItem = expr;
    progress(ctx);
  }

  /**
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(parsed) return;
    ctx.parse(query);
    parsed = true;
  }

  /**
   * Compiles the query.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    parse();
    if(compiled) return;
    ctx.compile();
    compiled = true;
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    compile();
    return ctx.iter();
  }

  /**
   * Returns a result value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    compile();
    return ctx.value();
  }

  /**
   * Evaluates the specified query and returns the result.
   * @return result of query
   * @throws QueryException query exception
   */
  public Result execute() throws QueryException {
    compile();
    return ctx.eval();
  }

  /**
   * Binds a value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type. If {@code "json"} is specified as data type,
   * the value is interpreted according to the rules specified in
   * {@link JsonMapConverter}.
   * @param name name of variable
   * @param value value to be bound
   * @param type data type
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value,
      final String type) throws QueryException {
    ctx.bind(name, value, type);
    return this;
  }

  /**
   * Binds a value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param name name of variable
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value)
      throws QueryException {
    ctx.bind(name, value);
    return this;
  }

  /**
   * Sets a value as context item. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value) throws QueryException {
    ctx.ctxItem = value instanceof Expr ? (Expr) value :
      JavaFunc.type(value).e(value, null);
    return this;
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the {@code uri} is an empty string.
   * The default element namespaces is set if the {@code prefix} is empty.
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor namespace(final String prefix, final String uri)
      throws QueryException {
    ctx.sc.namespace(prefix, uri);
    return this;
  }

  /**
   * Returns a serializer for the given output stream.
   * Optional output declarations within the query will be included in the
   * serializer instance.
   * @param os output stream
   * @return serializer instance
   * @throws IOException query exception
   * @throws QueryException query exception
   */
  public Serializer getSerializer(final OutputStream os) throws IOException,
      QueryException {

    compile();
    try {
      return Serializer.get(os, ctx.serProp(true));
    } catch(final SerializerException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Evaluates the specified query and returns the result nodes.
   * @return result nodes
   * @throws QueryException query exception
   */
  public Nodes queryNodes() throws QueryException {
    final Result res = execute();
    if(!(res instanceof Nodes)) {
      // convert empty result to node set
      if(res.size() == 0) return new Nodes(ctx.nodes.data);
      // otherwise, throw error
      QUERYNODES.thrw(null);
    }
    return (Nodes) res;
  }

  /**
   * Adds a module reference.
   * @param uri module uri
   * @param file file name
   */
  public void module(final String uri, final String file) {
    ctx.modDeclared.add(token(uri), token(file));
  }

  /**
   * Sets a new query. Should be called before parsing the query.
   * @param qu query
   */
  public void query(final String qu) {
    query = qu;
    parsed = false;
    compiled = false;
  }

  /**
   * Returns the query string.
   * @return query
   */
  public String query() {
    return query;
  }

  /**
   * Closes the processor.
   * @throws QueryException query exception
   */
  public void close() throws QueryException {
    // reset database properties to initial value
    if(ctx.globalOpt != null) {
      for(final Entry<String, Object> e : ctx.globalOpt.entrySet()) {
        ctx.context.prop.set(e.getKey(), e.getValue());
      }
      ctx.globalOpt = null;
    }
    // close all database connections
    ctx.resource.close();
    if(ctx.jdbc != null) ctx.jdbc.close();
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  public int updates() {
    return ctx.updates.size();
  }

  /**
   * Returns query background information.
   * @return background information
   */
  public String info() {
    return ctx.info();
  }

  /**
   * Removes comments from the specified string.
   * @param qu query string
   * @param max maximum string length
   * @return result
   */
  public static String removeComments(final String qu, final int max) {
    final StringBuilder sb = new StringBuilder();
    int m = 0;
    boolean s = false;
    final int cl = qu.length();
    for(int c = 0; c < cl && sb.length() < max; ++c) {
      final char ch = qu.charAt(c);
      if(ch == 0x0d) continue;
      if(ch == '(' && c + 1 < cl && qu.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        ++m;
        ++c;
      } else if(m != 0 && ch == ':' && c + 1 < cl && qu.charAt(c + 1) == ')') {
        --m;
        ++c;
      } else if(m == 0) {
        if(ch > ' ') sb.append(ch);
        else if(!s) sb.append(' ');
        s = ch <= ' ';
      }
    }
    if(sb.length() >= max) sb.append("...");
    return sb.toString().trim();
  }

  /**
   * Returns the query plan in the dot notation.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void plan(final Serializer ser) throws IOException {
    ctx.plan(ser);
  }

  @Override
  public String tit() {
    return QUERYEVAL;
  }

  @Override
  public String det() {
    return QUERYEVAL;
  }
}
