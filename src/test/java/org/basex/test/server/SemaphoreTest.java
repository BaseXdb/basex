package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.core.Command;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the order of incoming commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class SemaphoreTest {
  /** Create random number. */
  static Random rand = new Random();
  /** Test database name. */
  private static final String NAME = Util.name(SemaphoreTest.class);
  /** Test file. */
  private static final String FILE = "etc/test/factbook.zip";
  /** Test queries. */
  static final String [] QUERIES = {
    "xquery for $n in (db:open('" + NAME + "')//province)[position() < 100] " +
    "       return insert node <test/> into $n",
    "xquery for $n in 1 to 100000 where $n = 0 return $n"
  };
  /** Number of performance tests. */
  private static final int TESTS = 5;

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session sess;

  /** Starts the server.
   * @throws IOException exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z", "-p9999", "-e9998");
    sess = newSession();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    sess.execute(new DropDB(NAME));
    sess.close();
    // stop server instance
    server.stop();
  }

  /**
   * Runs a test for concurrent database creations.
   * @throws IOException I/O exception
   */
  @Test
  public void createTest() throws IOException {
    // drops database for clean test
    sess.execute(new DropDB(NAME));
    // create database for clean test
    sess.execute(new CreateDB(NAME, FILE));
  }

  /** Efficiency test.
   * @throws Exception exception
   */
  @Test
  public void runClients() throws Exception {
    final Client[] cl = new Client[TESTS];
    for(int i = 0; i < TESTS; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    for(final Client c : cl) c.session.close();
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
    } catch(final IOException ex) {
      fail(Util.message(ex));
      return null;
    }
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
  }

  /** Single client. */
  static class Client extends Thread {
    /** Client session. */
    ClientSession session;

    /**
     * Default constructor.
     */
    public Client() {
      try {
        session = newSession();
      } catch(final IOException ex) {
        fail(Util.message(ex));
      }
    }

    @Override
    public void run() {
      try {
        final int t = rand.nextInt(2);
        session.execute(QUERIES[t]);
      } catch(final IOException ex) {
        fail(Util.message(ex));
      }
    }
  }
}
