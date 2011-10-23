package org.basex.server;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.util.Performance;

/**
 * This class delays the response to the specified client.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class ClientDelayer extends Thread {
  /** Server instance. */
  private final BaseXServer server;
  /** Client listener. */
  private final ClientListener listener;
  /** Delay. */
  private int delay;

  /**
   * Constructor.
   * @param del delay
   * @param cl client listener
   * @param srv server instance
   */
  public ClientDelayer(final int del, final ClientListener cl,
      final BaseXServer srv) {

    delay = del;
    server = srv;
    listener = cl;
    setDaemon(true);
  }

  @Override
  public void run() {
    // loop until delay is exhausted, or until server is stopped
    while(server.running && --delay > 0) Performance.sleep(1000);

    try {
      // send negative flag
      listener.send(false);
    } catch(final IOException ex) {
      // socket was closed..
    }
  }
}
