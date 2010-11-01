package org.basex.query;

import static org.basex.query.QueryTokens.*;
import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.query.expr.Expr;
import org.basex.query.func.FunJava;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * This abstract class contains various methods which allow querying in
 * the database. A variety of hierarchical parsers (XPath, XQuery, etc..)
 * can be implemented on top of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    this(qu, cx.current, cx);
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
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(!parsed) ctx.parse(query);
    parsed = true;
  }

  /**
   * Compiles the query.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    parse();
    if(!compiled) ctx.compile();
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
   * Evaluates the specified query and returns the result.
   * @return result of query
   * @throws QueryException query exception
   */
  public Result execute() throws QueryException {
    compile();
    return ctx.eval();
  }

  /**
   * Binds an object to a global variable. If the object is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param n name of variable
   * @param o object to be bound
   * @param t data type
   * @throws QueryException query exception
   */
  public void bind(final String n, final String o, final String t)
      throws QueryException {

    Object obj = o;
    if(t != null && t.length() != 0) {
      final QNm type = new QNm(Token.token(t));
      if(type.ns()) type.uri(ctx.ns.uri(type.pref(), false, null));
      final Type typ = Type.find(type, true);
      if(typ == null) NOTYPE.thrw(null, type);
      obj = typ.e(o, null);
    }
    bind(n, obj);
  }

  /**
   * Binds an object to a global variable. If the object is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param n name of variable
   * @param o object to be bound
   * @throws QueryException query exception
   */
  public void bind(final String n, final Object o) throws QueryException {
    Expr ex = o instanceof Expr ? (Expr) o : FunJava.type(o).e(o, null);
    final Var var = new Var(new QNm(Token.token(n))).bind(ex, ctx);
    Var gl = ctx.vars.global().get(var);
    if(gl != null && gl.type != null) {
      gl.bind(gl.type.type.e(var.item(ctx, null), ctx, null), ctx);
    }
    ctx.vars.setGlobal(var);
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
  public XMLSerializer getSerializer(final OutputStream os) throws IOException,
      QueryException {

    compile();

    SerializerProp sp = ctx.serProp;
    if(sp == null) {
      sp = new SerializerProp(ctx.context.prop.get(Prop.SERIALIZER));
      if(ctx.context.prop.is(Prop.WRAPOUTPUT)) {
        sp.set(SerializerProp.S_WRAP_PRE, NAMELC);
        sp.set(SerializerProp.S_WRAP_URI, URL);
      }
    }
    return new XMLSerializer(os, sp);
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
   * @param ns module namespace
   * @param file file name
   */
  public void module(final String ns, final String file) {
    ctx.modules.add(ns);
    ctx.modules.add(file);
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
   * Returns the query in its string representation.
   * @return query
   */
  public String query() {
    return query;
  }

  /**
   * Closes the processor.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    ctx.close();
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
   * Checks if the specified query performs updates.
   * @param ctx context reference
   * @param qu query
   * @return result of check
   */
  public static boolean updating(final Context ctx, final String qu) {
    // quick check for update keywords
    for(final String s : UPDATES) {
      if(qu.indexOf(s) != -1) {
        // keyword found; parse query to get sure
        try {
          final QueryProcessor qp = new QueryProcessor(qu, ctx);
          qp.parse();
          return qp.ctx.updating;
        } catch(final QueryException ex) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the query plan in the dot notation.
   * @param ser serializer
   * @throws Exception exception
   */
  public void plan(final Serializer ser) throws Exception {
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

  @Override
  public double prog() {
    return 0;
  }
}
