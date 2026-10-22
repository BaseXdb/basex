package org.basex.server;

import static org.basex.core.Text.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the login handshake of the client/server architecture.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientLoginTest extends SandboxTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void startServer() throws IOException {
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /** Rejects overlong login strings. */
  @Test public void longName() {
    assertThrows(IOException.class, () -> createClient("x".repeat(10_000), NAME).close());
  }

  /**
   * Rejects parallel logins of a user while a failed login is being delayed.
   * @throws Exception exception
   */
  @Test public void parallel() throws Exception {
    try(Session session = createClient()) {
      session.execute("CREATE USER " + NAME + ' ' + NAME);
    }
    try {
      assertThrows(LoginException.class, () -> createClient(UserText.ADMIN, "wrong"));
      // second failure is delayed by 500 ms: in the meantime, the same user is rejected,
      // while another user can log in
      parallel(List.of(
        () -> assertThrows(LoginException.class, () -> createClient(UserText.ADMIN, "wrong")),
        () -> {
          Performance.sleep(200);
          return assertThrows(LoginException.class, () -> createClient().close());
        },
        () -> {
          Performance.sleep(200);
          createClient(NAME, NAME).close();
          return null;
        }
      ));
      createClient().close();
    } finally {
      try(Session session = createClient()) {
        session.execute("DROP USER " + NAME);
      }
    }
  }

  /**
   * Limits the number of pending logins per client.
   * @throws Exception exception
   */
  @Test public void pending() throws Exception {
    final ArrayList<Socket> sockets = new ArrayList<>();
    try {
      for(int s = 0; s < 32; s++) {
        final Socket socket = new Socket(S_LOCALHOST, DB_PORT);
        sockets.add(socket);
        assertNotEquals(-1, socket.getInputStream().read());
      }
      try(Socket socket = new Socket(S_LOCALHOST, DB_PORT)) {
        assertEquals(-1, socket.getInputStream().read());
      }
      assertThrows(BaseXException.class, () -> createClient().close());
    } finally {
      for(final Socket socket : sockets) socket.close();
    }
    // wait until the pending logins have been dropped
    for(int i = 0;; i++) {
      try {
        createClient().close();
        break;
      } catch(final IOException ex) {
        if(i == 50) throw ex;
        Performance.sleep(100);
      }
    }
  }
}
