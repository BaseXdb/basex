package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.data.DOTSerializer;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Performance;

/**
 * Abstract class for database queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class AQuery extends Process {
  /** Performance measurements. */
  protected final Performance per = new Performance();
  /** Performance measurements. */
  protected QueryProcessor qp;

  /**
   * Constructor.
   * @param p command properties
   * @param a arguments
   */
  protected AQuery(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Returns a new query instance.
   * @param query query
   * @return query instance
   */
  protected final boolean query(final String query) {
    long pars = 0;
    long comp = 0;
    long eval = 0;

    try {
      for(int i = 0; i < Prop.runs; i++) {
        qp = new QueryProcessor(query, context.current(), context);
        progress(qp);

        qp.parse();
        pars += per.getTime();
        if(i == 0) plan(qp, false);
        qp.compile();
        if(i == 0) plan(qp, true);
        comp += per.getTime();
        result = qp.query();
        eval += per.getTime();
        if(i + 1 < Prop.runs) qp.close();
      }

      // dump some query info
      if(Prop.info) {
        info(NL + qp.info());
        info(QUERYPARSE + Performance.getTimer(pars, Prop.runs) + NL);
        info(QUERYCOMPILE + Performance.getTimer(comp, Prop.runs) + NL);
        info(QUERYEVALUATE + Performance.getTimer(eval, Prop.runs) + NL);
      }
      return true;
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    } catch(final ProgressException ex) {
      return error(Prop.server ? SERVERTIME : "");
    } catch(final Exception ex) {
      ex.printStackTrace();
      return error(BaseX.bug());
    }
  }

  /**
   * Outputs the result.
   * @param o output stream
   * @param p pretty printing
   * @throws IOException exception
   */
  protected void out(final PrintOutput o, final boolean p) throws IOException {
    for(int i = 0; i < Prop.runs; i++) {
      final XMLSerializer xml = new XMLSerializer(i == 0 && Prop.serialize ?
          o : new NullOutput(!Prop.serialize), Prop.xmloutput, p);
      result.serialize(xml);
      xml.close();
    }
    if(Prop.runs > 0) {
      if(Prop.info) outInfo(o, result.size());
      qp.close();
    }
  }

  /**
   * Adds query information to the information string.
   * @param out output stream
   * @param hits information
   */
  protected final void outInfo(final PrintOutput out, final long hits) {
    info(QUERYPRINT + per.getTimer(Prop.runs) + NL);
    info(QUERYTOTAL + perf.getTimer(Prop.runs) + NL);
    info(QUERYHITS + hits + " " + (hits == 1 ? VALHIT : VALHITS) + NL);
    info(QUERYPRINTED + Performance.format(out.size()));
    //info(QUERYMEM, Performance.getMem());
  }

  /**
   * Creates query plans.
   * @param qu query reference
   * @param c compiled flag
   * @throws Exception exception
   */
  private void plan(final QueryProcessor qu, final boolean c) throws Exception {
    if(c != Prop.compplan) return;

    // show dot plan
    if(Prop.dotplan) {
      final CachedOutput out = new CachedOutput();
      final DOTSerializer ser = new DOTSerializer(out);
      qu.plan(ser);
      ser.close();
      IO.get(PLANDOT).write(out.finish());
      new ProcessBuilder(Prop.dotty, PLANDOT).start().waitFor();
      //f.delete();
    }
    // dump query plan
    if(Prop.xmlplan) {
      final CachedOutput out = new CachedOutput();
      qu.plan(new XMLSerializer(out, false, true));
      info(NL + QUERYPLAN + NL);
      info(out + NL);
    }
    // reset timer
    per.getTime();
  }
}
