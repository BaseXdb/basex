package org.basex.test.cs;

import static org.junit.Assert.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.basex.BaseXServer;
import org.basex.core.Session;
import org.basex.core.Proc;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the order of incoming processes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class SemaphoreTest {
  /** Create random number. */
  static Random rand = new Random();
  /** Test file. */
  private static final String FILE = "etc/xml/factbook.xml";
  /** Test queries. */
  final String [] q = {
      "xquery for $n in (doc('factbook')//province)[position() < 100] " +
      "       return insert node <test/> into $n",
      "xquery for $n in 1 to 100000 where $n = 0 return $n"
  };
  /** Number of performance tests. */
  private static final int TESTS = 10;
  /** List to adminster the clients. */
  static LinkedList<ClientSession> sessions = new LinkedList<ClientSession>();

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session sess;

  /** Number of done tests. */
  static int tdone;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();
    sess = createSession();
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    closeSession(sess);
    for(final ClientSession s : sessions) {
      closeSession(s);
    }
    // Stop server instance.
    new BaseXServer("stop");
  }

  /** Runs a test for concurrent database creations. */
  @Test
  public void createTest() {
    // drops database for clean test
    exec(new DropDB("factbook"), sess);
    // create database for clean test
    exec(new CreateDB(FILE), sess);
    for(int i = 0; i < TESTS; i++) {
      sessions.add(createSession());
    }
  }

  /** Efficiency test. */
  @Test
  public void runClients() {
    for(int n = 0; n < TESTS; n++) {
      final int j = n;
      Performance.sleep(rand.nextInt(500));
      new Thread() {
        @Override
        public void run() {
          try {
            final int t = rand.nextInt(2);
            sessions.get(j).execute(q[t]);
            tdone++;
          } catch(final IOException ex) {
            fail(ex.toString());
          }
        }
      }.start();
    }
    // wait until all test have been finished
    while(tdone < sessions.size()) Performance.sleep(200);
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
   * Closes a client session.
   * @param s session to be closed
   */
  static void closeSession(final Session s) {
    try {
      s.close();
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /**
   * Returns query result.
   * @param pr process reference
   * @param session session
   * @return String result
   */
  String checkRes(final Proc pr, final Session session) {
    final CachedOutput co = new CachedOutput();
    try {
      session.execute(pr, co);
    } catch(final IOException ex) {
      fail(ex.toString());
    }
    return co.toString();
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  String exec(final Proc pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final IOException ex) {
      return ex.toString();
    }
  }
}
