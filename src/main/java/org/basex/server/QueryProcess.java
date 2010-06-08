package org.basex.server;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Container for processes executing a query with iterative results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class QueryProcess extends Progress {
  /** Processor. */
  private final QueryProcessor proc;
  /** Serializer. */
  private final XMLSerializer xml;
  /** Log. */
  private final ServerProcess sp;

  /** Monitored flag. */
  private boolean monitored;
  /** Iterator. */
  private Iter iter;
  /** Current item. */
  private Item item;

  /**
   * Constructor.
   * @param q query string
   * @param o output
   * @param s serverProcess
   * @throws IOException I/O exception
   */
  QueryProcess(final String q, final PrintOutput o,
      final ServerProcess s) throws IOException {

    proc = new QueryProcessor(q, s.context);
    xml = new XMLSerializer(o);
    sp = s;
  }

  /**
   * Constructor.
   * @throws QueryException query exception
   */
  void init() throws QueryException {
    startTimeout(sp.context.prop.num(Prop.TIMEOUT));
    proc.parse();
    monitored = true;
    sp.context.lock.before(proc.ctx.updating);
    iter = proc.iter();
    item = iter.next();
  }

  /**
   * Returns the next item to the client.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void next() throws IOException, QueryException {
    if(stopped) throw new QueryException(SERVERTIMEOUT);

    if(item != null) {
      // item found: send {ITEM}
      item.serialize(xml);
      item = iter.next();
    } else {
      // no item found: empty result indicates end of iterator
      close();
    }
  }
  
  /**
   * Closes the query process.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    proc.stopTimeout();
    xml.close();
    proc.close();
    sp.queries.remove(this);
    if(monitored) sp.context.lock.after(proc.ctx.updating);
  }
}
