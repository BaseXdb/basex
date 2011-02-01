package org.basex.test.unresolved;

import java.io.IOException;
import java.util.Random;

import org.basex.BaseXServer;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

/**
 * Testing concurrent XQUF statements on a single database.
 *
 * @author BaseX Team 2005-11, ISC License
 */
public class XQUFStressTestClientServer {
  /** Server. */
  BaseXServer server;
  /** Number of queries per client. */
  static final int NQUERIES = 4;
  /** Number of clients. */
  static final int NCLIENTS = 2;
  /** Database name. */
  static final String DBNAME = "XQUFStress";
  /** Random number generator. */
  static final Random RND = new Random();
  
  /**
   * Starting test.
   */
  public void startTest() {
    server = new BaseXServer("-z");
    
    createDB();
    queryInsert();
    dropDB();
    
    server.stop();
  }

  /**
   * Performs the query.
   */
  private void queryInsert() {
    final String ins = "insert node <n/> into doc('" + DBNAME + "')/doc";
    
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    try {
      final Client[] cl = new Client[NCLIENTS];
      for(int i = 0; i < NCLIENTS; ++i) cl[i] = new Client(ins);
      for(final Client c : cl) c.start();
      for(final Client c : cl)
        c.join();

    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Prepares the test database.
   */
  private void createDB() {
    ClientSession s;
    try {
      s = newSession();
      s.execute(new CreateDB(DBNAME, "<doc/>"));
      p(s.info());
      s.close();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Helps printing to console.
   * @param s message string
   */
  private static void p(final String s) {
    System.out.println(s);
  }
  
  /**
   * Drops the database.
   */
  private void dropDB() {
    ClientSession s;
    try {
      s = newSession();
      s.execute(new DropDB(DBNAME));
      p(s.info());
      s.close();
      
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession("localhost", 1984, "admin", "admin");
  }
  
  /** Single client. */
  final class Client extends Thread {
    /** Client session. */
    private ClientSession session;
    /** Query to be executed by this client. */
    final String q;

    /**
     * Default constructor.
     * @param query query string
     */
    public Client(final String query) {
      q = query;
      try {
        session = newSession();
      } catch(IOException ex) {
        ex.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        for(int i = 0; i < NQUERIES; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));
          session.execute("xquery " + q);
        }
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * Main.
   * @param args args
   */
  public static void main(final String[] args) {
    new XQUFStressTestClientServer().startTest(); 
  }
 }
