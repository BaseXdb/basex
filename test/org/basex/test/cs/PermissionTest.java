package org.basex.test.cs;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import org.basex.BaseXServer;
import org.basex.core.Process;
import org.basex.core.Session;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.Revoke;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests user permissions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class PermissionTest {
  
  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;
  
  /** Starts the server. */
  @BeforeClass
  public static void start() {
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();

    // wait for server to be started
    Performance.sleep(200);
    
    try {
      session1 = new ClientSession(server.context, ADMIN, ADMIN);
      if(server.context.users.get("test") != null) { 
      ok(new DropUser("test"), session1);
      }
      ok(new CreateUser("test", "test"), session1);
      session2 = new ClientSession(server.context, "test", "test");
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }
  
  /** Global permission tests. */
  @Test
  public final void globalPerms() {
    no(new Grant("admin", "test"), session2);
    ok(new Grant("create", "test"), session1);
    ok(new CreateDB("<xml>This is a test</xml>", "test"), session2);
    ok(new Revoke("create", "test"), session1);
    no(new CreateDB("<xml>This is a test</xml>", "test"), session2);
    ok(new Grant("admin", "test"), session1);
    ok(new CreateUser("test2", "test2"), session2);
    ok(new DropUser("test2"), session2);
    //no(new DropUser("test"), session2);
    //no(new DropUser("admin"), session2);
    //no(new Revoke("admin", "test"), session2);
    ok(new Revoke("admin", "test"), session1);
  }
  
  /** Local permission tests. */
  @Test
  public final void localPerms() {
    
  }
  
  
  /**
   * Assumes that this command is successful.
   * @param pr process reference
   * @param s Session
   */
  static void ok(final Process pr, final Session s) {
    final String msg = process(pr, s);
    if(msg != null) fail(msg);
  }

  /**
   * Assumes that this command fails.
   * @param pr process reference
   * @param s Session
   */
  private void no(final Process pr, final Session s) {
    ok(process(pr, s) != null);
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  private static String process(final Process pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
