package org.basex.examples.server;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;

/**
 * This class connects multiple clients to one server instance.
 * It sets up three clients with read/write access to the database.
 * The modified database contents are shown after the clients have run.
 *
 * @author BaseX Team, BSD License
 */
public final class ServerConcurrency {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    System.out.println("=== ServerConcurrency ===");

    // Start a server with a known admin password
    System.out.println("\n* Start server.");
    BaseXServer server = new BaseXServer("-c", "PASSWORD admin");

    // Create a client session with host name, port, username and password
    System.out.println("\n* Create a client session.");
    try(ClientSession session =
        new ClientSession("localhost", 1984, "admin", "admin", System.out)) {
      // Create a database
      System.out.println("\n* Create a database.");
      session.execute("CREATE DB input src/main/resources/xml/input.xml");

      // Set up clients that simultaneously read from and write to the database
      System.out.println("\n* Run one reader and two writer threads.");
      try(ExecutorService executor = Executors.newCachedThreadPool()) {
        List<Future<Void>> clients = List.of(
          executor.submit(() -> client("//li")),
          executor.submit(() -> client("insert node <li>new node</li> as last into //ul")),
          executor.submit(() -> client("insert node <new>One more</new> as last into /html/body"))
        );
        // Wait until the clients have run their queries; errors are rethrown
        for(Future<Void> client : clients) client.get();
      }

      // Show modified database contents
      System.out.println("\n* Show modified database contents:");
      session.execute("XQUERY //li");

      // Drop the database
      System.out.println("\n* Drop the database.");
      session.execute("DROP DB input");

      // Close the client session
      System.out.println("\n* Close the client session.");
    } finally {
      // Stop the server
      System.out.println("\n* Stop server.");
      server.stop();
    }
  }

  /**
   * Runs a query several times in a new client session.
   * @param query query to be run
   * @return {@code null}
   * @throws Exception exception
   */
  private static Void client(final String query) throws Exception {
    try(ClientSession session = new ClientSession("localhost", 1984, "admin", "admin")) {
      session.execute("OPEN input");
      for(int i = 0; i < 10; i++) {
        session.execute("XQUERY " + query);
        Thread.sleep(ThreadLocalRandom.current().nextInt(100));
      }
    }
    return null;
  }
}
