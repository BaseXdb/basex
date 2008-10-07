package org.basex.query.xpath;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.QueryException;
import org.basex.query.QueryContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;

/**
 * Query context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class XPContext extends QueryContext {
  /** Data reference. */
  public NodeSet item;
  
  /** Leaf flag. */
  public boolean leaf;
  /** Count number of xpath fulltext query tokens. */
  public int ftcount;
  /** Current fulltext item. */
  public FTTokenizer ftitem;
  /** Current fulltext position filter. */
  public FTPositionFilter ftpos;
  /** Flag for fulltext index use. */
  public boolean iu;
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
    item = n != null ? new NodeSet(n.nodes, n.data) : null;
    root = root.compile(this);
    if(inf) compInfo(QUERYRESULT + "%", root);
    return this;
  }

  @Override
  public Result eval(final Nodes nodes) throws QueryException {
    evalTime = System.nanoTime();
    item = new NodeSet(nodes.nodes, nodes.data);
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

    final Item it = e.eval(this);
    if(inf) {
      final double t = ((System.nanoTime() - evalTime) / 10000) / 100d;
      evalInfo(t + MS + ": " + e + " -> " + it);
      inf = ++cc < MAXDUMP;
      if(!inf) evalInfo(XPText.EVALSKIP);
    }
    return it;
  }
}

