package org.basex.test.storage;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.io.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public abstract class DataUpdateTest {
  /** JUnit tag. */
  protected static final byte[] JUNIT = token("junit");
  /** Test file we do updates with. */
  private static final String TESTFILE = "etc/xml/test.xml";
  /** Test database name. */
  private final String dbname = getClass().getSimpleName();
  /** Database context. */
  protected Context ctx;
  /** Test file size in nodes. */
  protected int size;

  /**
   * Initializes the test class.
   */
  @BeforeClass
  public static final void setUpBeforeClass() {
    Prop.read();
    Prop.textindex = false;
    Prop.attrindex = false;
    Prop.chop = true;
  }

  /**
   * Creates the database.
   */
  @Before
  public void setUp() {
    try {
      ctx = new Context();
      ctx.data(CreateDB.xml(ctx, IO.get(TESTFILE), dbname));
      size = ctx.data().meta.size;
    } catch(final Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Deletes the test-database.
   */
  @After
  public void tearDown() {
    try {
      ctx.close();
      DropDB.drop(dbname);
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Reloads the database.
   */
  void reload() {
    new Close().execute(ctx);
    new Open(dbname).execute(ctx);
  }

  /**
   * Tests byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  void assertByteArraysEqual(final byte[] exp, final byte[] act) {
    assertEquals("array lengths don't equal", exp.length, act.length);
    for(int i = 0; i < exp.length; i++) assertEquals(exp[i], act[i]);
  }

  /**
   * Tests for correct data size.
   */
  @Test
  public void testSize() {
    assertEquals("Unexpected size!", size, ctx.data().meta.size);
    reload();
    assertEquals("Unexpected size!", size, ctx.data().meta.size);
  }
}
