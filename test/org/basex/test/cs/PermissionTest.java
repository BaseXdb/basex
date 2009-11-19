package org.basex.test.cs;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import org.basex.BaseXServer;
import org.basex.core.Process;
import org.basex.core.Session;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.Revoke;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
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
  static Session adminSession;
  /** Socket reference. */
  static Session testSession;
  
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
      adminSession = new ClientSession(server.context, ADMIN, ADMIN);
      if(server.context.users.get("test") != null) { 
      ok(new DropUser("test"), adminSession);
      }
      ok(new CreateUser("test", "test"), adminSession);
      testSession = new ClientSession(server.context, "test", "test");
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }
  
  /** Tests revoke and grant permissions. */
  @Test
  public final void revokegrantPerms() {
    no(new Grant("admin", "test"), testSession);
    ok(new Grant("admin", "test"), adminSession);
    no(new Revoke("admin", "admin"), testSession);
    no(new Revoke("admin", "admin"), adminSession);
    ok(new Revoke("admin", "test"), testSession);
    // [CG] Admin right should include Create right?
    ok(new Grant("admin", "test"), adminSession);
    ok(new Grant("create", "test"), testSession);
  }
  
  /** Tests create permissions. */
  @Test
  public final void createPerms() {
    ok(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    ok(new CreateIndex("SUMMARY"), testSession);
    ok(new CreateUser("test2", "test2"), testSession);
    ok(new DropUser("test2"), testSession);
    ok(new Revoke("create", "test"), adminSession);
    ok(new Revoke("admin", "test"), adminSession);
    no(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    // [CG] Index can be created without admin or create rights??
    // no(new CreateIndex("SUMMARY"), testSession);
    no(new CreateUser("test2", "test2"), testSession);
    ok(new CreateUser("test2", "test2"), adminSession);
  }
  
  /** Tests drop permissions. */
  @Test
  public final void dropPerms() {
    ok(new Grant("admin", "test"), adminSession);
    // [CG] Selfdropping...
    // no(new DropUser("test"), testSession);
    no(new DropUser("admin"), testSession);
    // [CG] Dropping of logged in user...
    // no(new DropUser("test"), adminSession);
    ok(new DropUser("test2"), testSession);
    ok(new DropDB("test"), testSession);
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
  
  /** Stops the server. */
  @AfterClass
  public static void stop() {
    try {
      adminSession.close();
      testSession.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }

    // Stop server instance.
    new BaseXServer("stop");
  }
}
