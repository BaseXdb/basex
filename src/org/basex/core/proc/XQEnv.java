package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.util.Performance;

/**
 * Evaluates the 'xqenv' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQEnv extends AQuery {
  /** Static XQuery instance. */
  private static XQueryProcessor qu = new XQueryProcessor("");

  /**
   * Constructor.
   * @param q query
   */
  public XQEnv(final String q) {
    super(PRINTING, q);
  }

  @Override
  protected boolean exec() {
    String query = args[0];

    long pars = 0;
    long comp = 0;
    long eval = 0;

    try {
      for(int i = 0; i < Prop.runs; i++) {
        if(query == null) {
          qu = new XQueryProcessor(query);
          query = "()";
        }
  
        qu.query = query;
        progress(qu);
  
        final Nodes nodes = context.current();
        qu.parse();
        pars += per.getTime();
        qu.compile(nodes);
        comp += per.getTime();
        result = qu.query(nodes);
        eval += per.getTime();
      }
      if(Prop.info) {
        info(qu.getInfo());
        info(NL + QUERYPARSE + Performance.getTimer(pars, Prop.runs));
        info(NL + QUERYCOMPILE + Performance.getTimer(comp, Prop.runs));
        info(NL + QUERYEVALUATE + Performance.getTimer(eval, Prop.runs));
      }
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    } catch(final Exception ex) {
      // to be removed as soon as implementation is complete...
      ex.printStackTrace();
      final String msg = ex.getMessage();
      return error("Implementation Bug? " +  ex.getClass().getSimpleName() +
          (msg != null ? ": " + msg : ""));
    }
    return true;
  }
}
