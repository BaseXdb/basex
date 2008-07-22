package org.basex.query.xpath;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.FTTokenizer;

/**
 * Query context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class XPContext extends QueryContext {
  /** Data reference. */
  public NodeSet local;
  /** Leaf flag. */
  public boolean leaf;
  
  // TODO... use fulltext flags
  
  /** Current fulltext item. */
  public FTTokenizer ftitem;
  /** Current fulltext position filter. */
  public FTPositionFilter ftpos;
  /** Current fulltext options. */
  //public FTOpt ftopt;
  
  /** Reference to the root expression. */
  private Expr root;

  /**
   * Constructor.
   * @param expr root expression
   * @param qu input query
   */
  public XPContext(final Expr expr, final String qu) {
    root = expr;
    query = qu;
  }

  @Override
  public XPContext compile(final Nodes n) throws QueryException {
    inf = Prop.allInfo;
    if(inf) compInfo(QUERYCOMP);
    local = n != null ? new NodeSet(n.pre, n.data) : null;
    root = root.compile(this);
    if(inf) compInfo(QUERYRESULT + "%", root);
    return this;
  }

  @Override
  public Result eval(final Nodes nodes) throws QueryException {
    evalTime = System.nanoTime();
    local = new NodeSet(nodes.pre, nodes.data);
    return eval(root);
  }
  
  @Override
  public void plan(final Serializer ser) throws Exception {
    root.plan(ser);
  }

  /**
   * Evaluates the expression with the specified context set.
   * Additionally provides a context.
   * @param e current expression
   * @return resulting XPathValue
   * @throws QueryException evaluation exception
   */
  public Item eval(final Expr e) throws QueryException {
    checkStop();

    final Item v = e.eval(this);
    if(!inf || cc >= MAXDUMP) return v;

    final double time = ((System.nanoTime() - evalTime) / 10000) / 100.0;
    evalInfo(time + MS + ": " + e + " -> " + v);
    if(++cc == MAXDUMP) evalInfo(XPText.EVALSKIP);
    return v;
  }
  
}

