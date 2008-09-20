package org.basex.query;

import static org.basex.Text.*;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.DOTSerializer;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This abstract query expression provides the architecture
 * for a compiled query.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class QueryContext extends Progress {
  /** Query Info: Plan. */
  public static final byte[] PLAN = Token.token("QueryPlan");
  /** Reference to the query file. */
  public IO file = Prop.xquery;
  /** Query string. */
  public String query;
  
  /** Maximum number of evaluation dumps. */
  protected static final int MAXDUMP = 16;
  /** Query info counter. */
  protected int cc;
  /** Current evaluation time. */
  protected long evalTime;
  /** Info flag. */
  protected boolean inf;

  /** String container for query background information. */
  protected final TokenBuilder info = new TokenBuilder();
  /** Optimization flag. */
  protected boolean firstOpt = true;
  /** Evaluation flag. */
  protected boolean firstEval = true;

  /**
   * Optimizes the expression.
   * @param nodes node context
   * @return query context
   * @throws QueryException query exception
   */
  protected abstract QueryContext compile(Nodes nodes) throws QueryException;
  
  /**
   * Evaluates the expression with the specified context set.
   * @param nodes initial context set
   * @return resulting value
   * @throws QueryException query exception
   */
  protected abstract Result eval(Nodes nodes) throws QueryException;
  
  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws Exception exception
   */
  protected abstract void plan(Serializer ser) throws Exception;

  /**
   * Adds some optimization info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public final void compInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(!firstOpt) info.add(QUERYSEP);
    firstOpt = false;
    info.add(string, ext);
    info.add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public final void evalInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(firstEval) {
      info.add(NL);
      info.add(QUERYEVAL);
      info.add(NL);
    }
    info.add(QUERYSEP);
    info.add(string, ext);
    info.add(NL);
    firstEval = false;
  }

  /**
   * Returns query background information.
   * @return warning
   */
  public final String info() {
    return info.toString();
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
  
  /**
   * Prints the plan to the specified file.
   * @throws Exception exception
   */
  public final void planXML() throws Exception {
    final CachedOutput out = new CachedOutput();
    final XMLSerializer ser = new XMLSerializer(out, true, true);
    ser.openElement(PLAN);
    plan(ser);
    ser.closeElement(PLAN);
    out.print(NL);
    info.add(out.finish());
    //IO.get(fn).write(out.finish());
  }
  
  /**
   * Shows the dot output via dotty.
   * @throws Exception exception
   */
  public final void planDot() throws Exception {
    final CachedOutput out = new CachedOutput();
    final DOTSerializer ser = new DOTSerializer(out);
    //ser.open(1);
    ser.openElement(PLAN);
    plan(ser);
    ser.closeElement(PLAN);
    //ser.close(1);
    
    final IO f = IO.get(PLANDOT);
    f.write(out.finish());
    new ProcessBuilder(Prop.dotty, PLANDOT).start().waitFor();
    //f.delete();
  }
}
