package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.DOTSerializer;
import org.basex.data.Nodes;
import org.basex.data.PrintSerializer;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.util.Performance;

/**
 * Evaluates the 'xquery' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class XQuery extends Proc {
  /** Auxiliary performance measurements. */
  protected final Performance per = new Performance();

  @Override
  protected boolean exec() {
    return query(XQueryProcessor.class, cmd.args());
  }

  @Override
  protected void out(final PrintOutput o) throws Exception {
    out(o, Prop.xqformat);
  }

  /**
   * Outputs the result.
   * @param o output stream
   * @param p pretty printing
   * @throws Exception exception
   */
  protected void out(final PrintOutput o, final boolean p) throws Exception {
    final PrintSerializer ser = new PrintSerializer(Prop.serialize ? o :
      new NullOutput(), Prop.xmloutput, p);

    for(int i = 0; i < Prop.runs; i++) {
      result.serialize(i == 0 ? ser : new PrintSerializer(
          new NullOutput(!Prop.serialize), Prop.xmloutput, Prop.xqformat));
    }
    o.print(Prop.NL);
    if(Prop.info) outInfo(o, result.size());
    
    if(Prop.dotresult) {
      final CachedOutput out = new CachedOutput();
      result.serialize(new DOTSerializer(out));
      new IO(RESULTDOT).write(out.finish());
      new ProcessBuilder(Prop.dotty, RESULTDOT).start().waitFor();
    }
  }

  /**
   * Adds query information to the information string.
   * @param out output stream
   * @param hits information
   */
  protected final void outInfo(final PrintOutput out, final int hits) {
    info(QUERYPRINT + per.getTimer(Prop.runs) + NL);
    info(QUERYTOTAL + perf.getTimer(Prop.runs) + NL);
    info(QUERYHITS + hits + " " + (hits == 1 ? VALHIT : VALHITS) + NL);
    info(QUERYPRINTED + Performance.formatSize(out.size()));
    //info(QUERYMEM, Performance.getMem() + NL);
  }

  /**
   * Returns a new query instance.
   * @param cls query class
   * @param query query
   * @return query instance
   */
  protected final boolean query(final Class<? extends QueryProcessor> cls,
      final String query) {

    long pars = 0;
    long comp = 0;
    long eval = 0;

    QueryProcessor qu = null;
    try {
      for(int i = 0; i < Prop.runs; i++) {
        qu = cls.getConstructor(new Class[] { String.class }).newInstance(
            new Object[] { query });
        final Nodes nodes = context.current();
        progress(qu);

        qu.parse();
        pars += per.getTime();
        qu.compile(nodes);
        comp += per.getTime();
        result = qu.eval(nodes);
        eval += per.getTime();
      }
      // convert query NodeSet to visualization node set
      if(result instanceof NodeSet) {
        final NodeSet ns = (NodeSet) result;
        final Nodes nodes = new Nodes(ns.nodes, ns.data);
        nodes.setFTData(ns.ftidpos, ns.ftpointer, 
            ((XPathProcessor) qu).ftSearchStrings());
        result = nodes;
      }
      // dump some query info
      if(Prop.info) {
        info(qu.getInfo());
        info(QUERYPARSE + Performance.getTimer(pars, Prop.runs) + NL);
        info(QUERYCOMPILE + Performance.getTimer(comp, Prop.runs) + NL);
        info(QUERYEVALUATE + Performance.getTimer(eval, Prop.runs) + NL);
      }
      return true;
    } catch(final IllegalArgumentException ex) {
      throw ex;
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    } catch(final Exception ex) {
      if(ex.getClass() == RuntimeException.class) return false;
      ex.printStackTrace();
      return error("Implementation Bug? " +  ex.getClass().getSimpleName());
    }
  }
}
