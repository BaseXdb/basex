package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.core.User;
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
  /** Query processor. */
  protected QueryProcessor qp;
  /** Parsing time. */
  protected long pars;
  /** Compilation time. */
  protected long comp;
  /** Evaluation time. */
  protected long eval;

  /**
   * Protected constructor.
   * @param p command properties
   * @param a arguments
   */
  protected AQuery(final int p, final String... a) {
    super(p | User.READ, a);
  }

  /**
   * Returns a new query instance.
   * @param query query
   * @return query instance
   */
  protected final boolean query(final String query) {
    final int runs = prop.num(Prop.RUNS);
    String err = null;
    try {
      for(int i = 0; i < runs; i++) {
        qp = new QueryProcessor(query, context);
        progress(qp);

        qp.parse();
        pars += per.getTime();
        if(i == 0) plan(qp, false);
        qp.compile();
        if(i == 0) plan(qp, true);
        comp += per.getTime();
        result = qp.query();
        eval += per.getTime();
        if(i + 1 < runs) qp.close();
      }

      // dump some query info
      execInfo();
      return true;
    } catch(final QueryException ex) {
      Main.debug(ex);
      err = ex.getMessage();
    } catch(final ProgressException ex) {
      err = PROGERR;
    } catch(final Exception ex) {
      ex.printStackTrace();
      err = Main.bug();
    }
    try { qp.close(); } catch(final IOException ex) { /* ignored */ }
    return error(err);
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    final boolean pretty = prop.is(Prop.XQFORMAT);
    final int runs = prop.num(Prop.RUNS);
    final boolean ser = prop.is(Prop.SERIALIZE);
    for(int i = 0; i < runs; i++) {
      final XMLSerializer xml = new XMLSerializer(i == 0 && ser ?
          o : new NullOutput(!ser), prop.is(Prop.XMLOUTPUT), pretty);
      result.serialize(xml);
      xml.close();
    }
    if(runs > 0) {
      outInfo(o, result.size());
      qp.close();
    }
    if(ser && (prop.is(Prop.INFO) || prop.is(Prop.XMLPLAN))) o.println();
  }

  // [LK] this method could be overwritten to check if the process
  //   performs updates
  @Override
  public boolean updating() {
    // final QueryParser p = new QueryParser(new QueryContext(context));
    // ...
    return super.updating();
  }

  /**
   * Adds evaluation information to the information string.
   */
  protected final void execInfo() {
    if(!prop.is(Prop.INFO)) return;
    final int runs = prop.num(Prop.RUNS);
    final String opt = qp.info(prop.is(Prop.ALLINFO));
    if(opt.length() != 0) info(opt);
    info(QUERYPARSE + Performance.getTimer(pars, runs));
    info(QUERYCOMPILE + Performance.getTimer(comp, runs));
    info(QUERYEVALUATE + Performance.getTimer(eval, runs));
  }

  /**
   * Adds output information to the information string.
   * @param out output stream
   * @param hits information
   */
  protected final void outInfo(final PrintOutput out, final long hits) {
    if(!prop.is(Prop.INFO)) return;
    final int runs = prop.num(Prop.RUNS);
    info(QUERYPRINT + per.getTimer(runs));
    info(QUERYTOTAL + perf.getTimer(runs));
    info(QUERYHITS + hits + " " + (hits == 1 ? VALHIT : VALHITS));
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
    if(c != prop.is(Prop.COMPPLAN)) return;

    // show dot plan
    if(prop.is(Prop.DOTPLAN)) {
      final CachedOutput out = new CachedOutput();
      final DOTSerializer ser = new DOTSerializer(out);
      qu.plan(ser);
      ser.close();
      final String dot = "plan.dot";
      IO.get(dot).write(out.finish());
      new ProcessBuilder(prop.get(Prop.DOTTY), dot).start().waitFor();
      //f.delete();
    }
    // show XML plan
    if(prop.is(Prop.XMLPLAN)) {
      final CachedOutput out = new CachedOutput();
      qu.plan(new XMLSerializer(out, false, true));
      info(QUERYPLAN);
      info.add(out.toString());
      info.add(NL);
    }
    // reset timer
    per.getTime();
  }
}
