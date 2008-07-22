package org.basex.query;

import static org.basex.Text.*;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.TokenBuilder;

/**
 * This abstract class contains various methods which allow querying in
 * the database. A variety of hierarchical parsers (XPath, XQuery, etc..)
 * can be implemented on top of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class QueryProcessor extends Progress {
  /** Initial node set. */
  public String query;
  /** Expression context. */
  protected QueryContext context;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Default Constructor.
   * @param q query
   */
  public QueryProcessor(final String q) {
    query = q;
  }

  /**
   * Parses the specified query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    context = create();
    progress(context);
  }

  /**
   * Parses the specified query and returns the query context.
   * @return query expression
   * @throws QueryException query exception
   */
  protected abstract QueryContext create()
    throws QueryException;
  
  /**
   * Compiles the query.
   * @param nodes node context
   * @throws QueryException query exception
   */
  public final void compile(final Nodes nodes) throws QueryException {
    if(context == null) parse();
    context.compile(nodes);
    try {
      if(Prop.xmlplan) context.planXML();
      if(Prop.dotplan) context.planDot();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    compiled = true;
  }

  /**
   * Parses the specified query and returns the result nodes.
   * @param nodes node context
   * @return result of query
   * @throws QueryException query exception
   */
  public final Nodes queryNodes(final Nodes nodes) throws QueryException {
    final Result res = query(nodes);
    if(!(res instanceof NodeSet)) throw new QueryException(QUERYNODESERR);

    final NodeSet ns = (NodeSet) res;
    return new Nodes(ns.nodes, ns.data);
  }

  /**
   * Parses the specified query and returns the result.
   * @param n node context
   * @return result of query
   * @throws QueryException query exception
   */
  public final Result query(final Nodes n) throws QueryException {
    if(!compiled) compile(n);
    return context.eval(n);
  }
  
  /**
   * Returns query background information.
   * @return background information
   */
  public final String getInfo() {
    final TokenBuilder tb = new TokenBuilder(context.info());

    //if(Prop.xmlplan) tb.add(QUERYPLAN + NL, PLANXML);
    //if(Prop.dotplan) tb.add(QUERYPLAN + NL, PLANDOT);
    
    if(Prop.allInfo) {
      tb.add(NL + QUERYSTRING);
      tb.add(query);
      tb.add(NL);
    }
    return tb.toString();
  }
  
  @Override
  public final String tit() {
    return QUERYEVAL;
  }

  @Override
  public final String det() {
    return QUERYEVAL;
  }

  @Override
  public final double prog() {
    return 0;
  }
}
