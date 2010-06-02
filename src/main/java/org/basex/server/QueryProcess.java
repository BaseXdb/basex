package org.basex.server;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Context;
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
 */
public class QueryProcess {
  
  /** Id. */
  public int id;
  /** Output. */
  private PrintOutput out;
  /** Processor. */
  private QueryProcessor processor;
  /** Iterator. */
  private Iter iter;
  /** Serializer. */
  private XMLSerializer serializer;
  /** Log. */
  private Log log;
  
  /**
   * Constructor.
   * @param i id
   * @param o output
   * @param l log
   */
  public QueryProcess(final int i, final PrintOutput o, final Log l) {
    this.id = i;
    this.out = o;
    this.log = l;
  }
  
  /**
   * Starts the query process.
   * @param s input string
   * @param ctx context
   * @throws IOException Exception
   */
  public void start(final String s, final Context ctx) throws IOException {
    processor = new QueryProcessor(s, ctx);
    try {
    iter = processor.iter();
    serializer = new XMLSerializer(out);
    } catch(QueryException ex) {
      // invalid command was sent by a client; create error feedback
      log.write(this, s, INFOERROR + ex.extended());
      send(false);
      out.print(ex.extended());
      send(true);
    }
    out.print(String.valueOf(id));
    send(true);
  }
  
  /**
   * Returns the next item to the client.
   * @throws IOException Exception
   * @throws QueryException Exception
   */
  public void more() throws IOException, QueryException {
    Item item;
    if((item = iter.next()) != null) {
      send(true);
      item.serialize(serializer);
      send(true);
    } else {
      send(false);
      close();
    }
  }
  
  /**
   * Closes the query process.
   * @throws IOException Exception
   */
  public void close() throws IOException {
    serializer.close();
    processor.close();
  }
  
  /**
   * Sends the success flag to the client.
   * @param ok success flag
   * @throws IOException I/O exception
   */
  private void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }
}
