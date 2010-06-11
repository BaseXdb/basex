package org.basex.query;

import static org.basex.query.QueryTokens.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.iter.Iter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This abstract class contains various methods which allow querying in
 * the database. A variety of hierarchical parsers (XPath, XQuery, etc..)
 * can be implemented on top of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Progress {
  /** Query Info: Plan. */
  private static final byte[] PLAN = Token.token("QueryPlan");
  /** Expression context. */
  public final QueryContext ctx;
  /** Query. */
  public String query;
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
  public Result query() throws QueryException {
    compile();
    return ctx.eval();
  }

  /**
   * Evaluates the specified query and returns the result nodes.
   * @return result nodes
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
    query = qu;
    reset();
  }

  /**
   * Resets the processor.
   */
  public void reset() {
    parsed = false;
    compiled = false;
  }

  /**
   * Closes the processor.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    ctx.close();
  }

  /**
   * Returns query background information.
   * @param all show all information
   * @return background information
   */
  public String info(final boolean all) {
    final TokenBuilder tb = new TokenBuilder(ctx.info());
    if(all) tb.add(QUERYSTRING + query);
    return tb.toString();
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
