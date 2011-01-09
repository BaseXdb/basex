package org.basex.test.server;

import static org.junit.Assert.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the order of incoming commands.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public final class SemaphoreTest {
  /** Create random number. */
  static Random rand = new Random();
  /** Test database name. */
  private static final String NAME = "factbook";
  /** Test file. */
  private static final String FILE = "etc/xml/factbook.zip";
  /** Test queries. */
  final String [] q = {
      "xquery for $n in (doc('factbook')//province)[position() < 100] " +
      "       return insert node <test/> into $n",
      "xquery for $n in 1 to 100000 where $n = 0 return $n"
  };
  /** Number of performance tests. */
  private static final int TESTS = 5;
  /** List to administer the clients. */
  static LinkedList<ClientSession> sessions = new LinkedList<ClientSession>();

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session sess;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");
    sess = createSession();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    for(final ClientSession s : sessions) s.close();
    sess.execute(new DropDB(NAME));
    sess.close();
    // stop server instance
    server.stop();
  }

  /**
   * Runs a test for concurrent database creations.
   * @throws BaseXException database exception
   */
  @Test
  public void createTest() throws BaseXException {
    // drops database for clean test
    sess.execute(new DropDB(NAME));
    // create database for clean test
    sess.execute(new CreateDB(NAME, FILE));
    for(int i = 0; i < TESTS; ++i) {
      sessions.add(createSession());
    }
  }

  /** Number of done tests. */
  static int tdone;

  /** Efficiency test. */
  @Test
  public void runClients() {
    for(int n = 0; n < TESTS; ++n) {
      final int j = n;
      Performance.sleep(50 + rand.nextInt(200));
      new Thread() {
        @Override
        public void run() {
          try {
            final int t = rand.nextInt(2);
            sessions.get(j).execute(q[t]);
            synchronized(this) { ++tdone; }
          } catch(final BaseXException ex) {
            fail(ex.toString());
          }
        }
      }.start();
    }
    // wait until all test have been finished
    while(tdone < TESTS) Performance.sleep(100);
  }

  /**
   * Creates a client session.
   * @return client session
   */
  static ClientSession createSession() {
    try {
      return new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final IOException ex) {
      fail(ex.toString());
    }
    return null;
  }

  /**
   * Returns query result.
   * @param cmd command reference
   * @param session session
   * @return String result
   */
  String checkRes(final Command cmd, final Session session) {
    try {
      return session.execute(cmd);
    } catch(final BaseXException ex) {
      fail(ex.toString());
      return null;
    }
  }
}
