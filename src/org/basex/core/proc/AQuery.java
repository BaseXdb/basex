package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.DOTSerializer;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Performance;

/**
 * Abstract class for database queries.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class AQuery extends Process {
  /** Performance measurements. */
  protected final Performance per = new Performance();

  /**
   * Constructor.
   * @param p command properties
   * @param a arguments
   */
  public AQuery(final int p, final String... a) {
    super(p, a);
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
            new Object[] { query == null ? "" : query });
        final Nodes nodes = context.current();
        progress(qu);

        qu.parse();
        pars += per.getTime();
        qu.compile(nodes);
        comp += per.getTime();
        result = qu.query(nodes);
        eval += per.getTime();
      }
      // convert query NodeSet to visualization node set
      if(result instanceof NodeSet) {
        final NodeSet ns = (NodeSet) result;
        final Nodes nodes = new Nodes(ns.nodes, ns.data);
        nodes.setFTData(ns.ftidpos, ns.ftpointer);
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
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    } catch(final ProgressException ex) {
      return false;
    } catch(final Exception ex) {
      ex.printStackTrace();
      return error("Implementation Bug? " +  ex.getClass().getSimpleName());
    }
  }

  /**
   * Outputs the result.
   * @param o output stream
   * @param p pretty printing
   * @throws IOException exception
   */
  protected void out(final PrintOutput o, final boolean p) throws IOException {
    final XMLSerializer ser = new XMLSerializer(Prop.serialize ? o :
      new NullOutput(), Prop.xmloutput, p);

    for(int i = 0; i < Prop.runs; i++) {
      result.serialize(i == 0 ? ser : new XMLSerializer(
          new NullOutput(!Prop.serialize), Prop.xmloutput, Prop.xqformat));
    }
    o.print(Prop.NL);
    if(Prop.info) outInfo(o, result.size());
    
    if(Prop.dotresult) {
      final CachedOutput out = new CachedOutput();
      result.serialize(new DOTSerializer(out));
      new IO(RESULTDOT).write(out.finish());
      try {
        new ProcessBuilder(Prop.dotty, RESULTDOT).start().waitFor();
      } catch(final InterruptedException ex) {
        throw new IOException(ex.toString());
      }
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
}
