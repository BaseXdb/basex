package org.basex.query;

import static org.basex.Text.*;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.item.Item;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This abstract class contains various methods which allow querying in
 * the database. A variety of hierarchical parsers (XPath, XQuery, etc..)
 * can be implemented on top of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * Parses the specified query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(!parsed) ctx.parse(query);
    parsed = true;
  }
  
  /**
   * Compiles the query.
   * @param nodes node context
   * @throws QueryException query exception
   */
  public void compile(final Nodes nodes) throws QueryException {
    parse();
    if(!compiled) ctx.compile(nodes);
    compiled = true;
  }
  
  /**
   * Returns a result iterator.
   * @param nodes initial node set
   * @return result iterator
   * @throws QueryException query exception
   */
  public Item eval(final Nodes nodes) throws QueryException {
    compile(nodes);
    return ctx.iter().finish();
  }

  /**
   * Parses the specified query and returns the result.
   * @param nodes initial node set
   * @return result of query
   * @throws QueryException query exception
   */
  public Result query(final Nodes nodes) throws QueryException {
    compile(nodes);
    return ctx.eval(nodes);
  }

  /**
   * Parses the specified query and returns the result nodes.
   * @param nodes node context
   * @return result of query
   * @throws QueryException query exception
   */
  public Nodes queryNodes(final Nodes nodes) throws QueryException {
    final Result res = query(nodes);
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
