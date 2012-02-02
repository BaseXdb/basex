package org.basex.test.data;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.data.Data;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Tim Petrowsky
 */
public abstract class UpdateTest {
  /** Test database name. */
  private static final String DB = Util.name(UpdateTest.class);
  /** Test file we do updates with. */
  private static final String TESTFILE = "src/test/resources/test.xml";
  /** Main memory flag. */
  private static boolean mainmem;

  /** JUnit tag. */
  static final byte[] JUNIT = token("junit");
  /** JUnit tag. */
  static final byte[] FOO = token("foo");
  /** JUnit tag. */
  static final byte[] NAME = token("name");
  /** JUnit tag. */
  static final byte[] PARENTNODE = token("parentnode");
  /** JUnit tag. */
  static final byte[] CONTEXTNODE = token("contextnode");
  /** JUnit tag. */
  static final byte[] ID = token("id");
  /** JUnit tag. */
  static final byte[] B = token("b");
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Test file size in nodes. */
  int size;

  /**
   * Initializes the test class.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    final Prop prop = CONTEXT.prop;
    prop.set(Prop.TEXTINDEX, false);
    prop.set(Prop.ATTRINDEX, false);
    prop.set(Prop.MAINMEM, mainmem);
  }

  /**
   * Closes the test database.
   */
  @AfterClass
  public static void finish() {
    CONTEXT.close();
  }

  /**
   * Creates the database.
   */
  @Before
  public final void setUp() {
    exec(new CreateDB(DB, TESTFILE));
    size = CONTEXT.data().meta.size;
  }

  /**
   * Deletes the test database.
   */
  @After
  public final void tearDown() {
    if(mainmem) return;
    exec(new Close());
    exec(new DropDB(DB));
  }

  /**
   * Reloads the database.
   */
  final void reload() {
    if(mainmem) return;
    exec(new Close());
    exec(new Open(DB));
  }

  /**
   * Tests byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  final void assertArraysEquals(final byte[] exp, final byte[] act) {
    assertEquals("array lengths don't equal", exp.length, act.length);
    for(int i = 0; i < exp.length; ++i) assertEquals(exp[i], act[i]);
  }

  /**
   * Tests for correct data size.
   */
  @Test
  public final void size() {
    assertEquals("Unexpected size!", size, CONTEXT.data().meta.size);
    reload();
    assertEquals("Unexpected size!", size, CONTEXT.data().meta.size);
  }

  /**
   * Executes the specified command. Gives feedback and stops the test
   * if errors occur.
   * @param cmd command reference
   */
  private void exec(final Command cmd) {
    try {
      cmd.execute(CONTEXT);
    } catch(final BaseXException ex) {
      Util.errln(ex.getMessage());
    }
  }
}
