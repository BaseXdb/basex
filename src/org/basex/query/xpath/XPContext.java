package org.basex.query.xpath;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTOption;
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
  public NodeSet local;
  /** Leaf flag. */
  public boolean leaf;
  /** Fulltext options. */
  public FTOption fto;
  /** Fulltext position filters. */
  public FTPositionFilter ftpos;
  /** Reference to the root expression. */
  private Expr root;
  /** Query info counter. */
  private int cc;
  /** Current evaluation time. */
  private long evalTime;
 

  /**
   * Constructor.
   * @param expr root expression
   */
  public XPContext(final Expr expr) {
    root = expr;
  }

  // below, the current node set is passed on as argument; if the doc(...)
  // function is rewritten to generate the input document while compilation
  // time, the query could be optimized for different documents, and
  // instead of the data reference, the current nodeset would be passed
  // on as parameter. this might simplify other optimizations such as
  // checking if the current context is a root context, etc.

  @Override
  public XPContext compile(final Nodes n) throws QueryException {
    if(Prop.allInfo) compInfo(QUERYCOMP);
    local = n != null ? new NodeSet(n.pre, n.data) : null;
    root = root.compile(this);
    if(Prop.allInfo) compInfo(QUERYRESULT + "%", root);
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

  /** Maximum number of evaluation dumps. */
  private static final int MAXDUMP = 12;
  
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
    if(!Prop.allInfo || cc >= MAXDUMP) return v;

    final double time = ((System.nanoTime() - evalTime) / 10000) / 100.0;
    evalInfo(time + MS + ": " + e + " -> " + v);
    if(++cc == MAXDUMP) evalInfo(XPText.EVALSKIP);
    return v;
  }
  
}

