package org.basex.query;

import static org.basex.Text.*;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This abstract class contains various methods which allow querying in
 * the database. A variety of hierarchical parsers (XPath, XQuery, etc..)
 * can be implemented on top of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Progress {
  /** Query Info: Plan. */
  private static final byte[] PLAN = Token.token("QueryPlan");
  /** Expression context. */
  public final QueryContext ctx = new QueryContext();
  /** Initial node set. */
  public String query;
  /** Parsed flag. */
  private boolean parsed;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Default Constructor.
   * @param q query
   */
  public QueryProcessor(final String q) {
    query = q;
    Prop.read();
    progress(ctx);
  }
  
  /**
   * XQuery Constructor.
   * @param qu query
   * @param f query file reference
   */
  public QueryProcessor(final String qu, final IO f) {
    this(qu);
    ctx.file = f;
  }
  
  /**
   * XQuery Constructor.
   * @param qu query
   * @param n initial nodes
   */
  public QueryProcessor(final String qu, final Nodes n) {
    this(qu);
    ctx.nodes(n);
  }

  /**
   * Parses the specified query.
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
   * Returns the result as an item.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Item eval() throws QueryException {
    return iter().finish();
  }

  /**
   * Parses the specified query and returns the result.
   * @return result of query
   * @throws QueryException query exception
   */
  public Result query() throws QueryException {
    compile();
    return ctx.eval();
  }

  /**
   * Parses the specified query and returns the result nodes.
   * @return result of query
   * @throws QueryException query exception
   */
  public Nodes queryNodes() throws QueryException {
    final Result res = query();
    if(!(res instanceof Nodes)) throw new QueryException(QUERYNODESERR);
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
  public void setQuery(final String qu) {
    query = qu;;
  }
  
  /**
   * Returns query background information.
   * @return background information
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder(ctx.info());
    if(Prop.allInfo) tb.add(NL + QUERYSTRING + query + NL);
    return tb.toString();
  }
  
  /**
   * Returns the query plan in the dot notation.
   * @param ser serializer
   * @throws Exception exception
   */
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(PLAN);
    ctx.plan(ser);
    ser.closeElement();
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
