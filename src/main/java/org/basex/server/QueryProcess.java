package org.basex.server;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Performance;

/**
 * Container for processes executing a query with iterative results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class QueryProcess {
  /** Flag for timeout. */
  boolean running = true;
  /** Output. */
  PrintOutput out;
  /** Id. */
  int id;
  /** Processor. */
  private QueryProcessor processor;
  /** Iterator. */
  private Iter iter;
  /** Serializer. */
  private XMLSerializer serializer;
  /** Log. */
  private final ServerProcess sp;
  /** Update flag. */
  boolean updating;
  /** Timeout thread. */
  private Thread timeout;

  /**
   * Constructor.
   * @param i id
   * @param q query string
   * @param o output
   * @param s serverProcess
   * @throws IOException Exception
   */
  public QueryProcess(final int i, final String q, final PrintOutput o,
      final ServerProcess s) throws IOException {
    id = i;
    sp = s;
    out = o;
    processor = new QueryProcessor(q, sp.context);
    serializer = new XMLSerializer(out);
    updating = processor.ctx.updating;
    sp.context.sema.before(updating);
    try {
      iter = processor.iter();
      out.print(String.valueOf(id));
      send(true);
      startTimer();
    } catch(final QueryException ex) {
      sp.context.sema.after(updating);
      // invalid command was sent by a client; create error feedback
      sp.log.write(this, s, INFOERROR + ex.extended());
      out.print(String.valueOf(0));
      send(true);
      out.print(ex.extended());
      send(true);
      sp.queries.remove(this);
    }
  }

  /**
   * Returns the next item to the client.
   * @throws IOException Exception
   */
  public void more() throws IOException {
    Item item;
    try {
      if(!running) {
        send(false);
        send(true);
        out.print(SERVERTIME);
        send(true);
        close();
      } else {
        if((item = iter.next()) != null) {
          send(true);
          item.serialize(serializer);
          send(true);
        } else {
          send(false);
          send(false);
          close();
        }
      }
    } catch(final QueryException ex) {
      send(false);
      send(true);
      out.print(ex.extended());
      send(true);
      close();
    }
  }

  /**
   * Closes the query process.
   */
  public void close() {
    sp.context.sema.after(updating);
    try {
      serializer.close();
      processor.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    stopTimer();
    sp.queries.remove(this);
  }

  /**
   * Sends the success flag to the client.
   * @param ok success flag
   * @throws IOException Exception
   */
  void send(final boolean ok) throws IOException {
    sp.send(ok);
  }

  /**
   * Starts a timeout thread for the specified process.
   */
  private void startTimer() {
    final long to = sp.context.prop.num(Prop.TIMEOUT);
    if(to == 0 || updating) return;

    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(to * 1000);
        running = false;
      }
    };
    timeout.start();
  }

  /**
   * Stops the current timeout thread.
   */
  private void stopTimer() {
    if(timeout != null) timeout.interrupt();
  }
}
