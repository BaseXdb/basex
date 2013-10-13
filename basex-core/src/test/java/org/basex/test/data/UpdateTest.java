package org.basex.test.data;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Tim Petrowsky
 */
public abstract class UpdateTest extends SandboxTest {
  /** Test file we do updates with. */
  private static final String TESTFILE = "src/test/resources/test.xml";
  /** Main memory flag; can be changed for testing. */
  private static boolean mainmem;

  /** JUnit tag. */
  static final byte[] T_JUNIT = token("junit");
  /** JUnit tag. */
  static final byte[] T_FOO = token("foo");
  /** JUnit tag. */
  static final byte[] T_NAME = token("name");
  /** JUnit tag. */
  static final byte[] T_PARENTNODE = token("parentnode");
  /** JUnit tag. */
  static final byte[] T_CONTEXTNODE = token("contextnode");
  /** JUnit tag. */
  static final byte[] T_ID = token("id");
  /** JUnit tag. */
  static final byte[] T_B = token("b");
  /** Test file size in nodes. */
  int size;

  /**
   * Initializes the test class.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    final MainOptions opts = context.options;
    opts.set(MainOptions.TEXTINDEX, false);
    opts.set(MainOptions.ATTRINDEX, false);
    opts.set(MainOptions.MAINMEM, mainmem);
  }

  /**
   * Creates the database.
   */
  @Before
  public final void setUp() {
    exec(new CreateDB(NAME, TESTFILE));
    size = context.data().meta.size;
  }

  /**
   * Deletes the test database.
   */
  @After
  public final void tearDown() {
    if(mainmem) return;
    exec(new Close());
    exec(new DropDB(NAME));
  }

  /**
   * Reloads the database.
   */
  static final void reload() {
    if(mainmem) return;
    exec(new Close());
    exec(new Open(NAME));
  }

  /**
   * Tests byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  static final void assertArraysEquals(final byte[] exp, final byte[] act) {
    assertEquals("array lengths don't equal", exp.length, act.length);
    for(int i = 0; i < exp.length; ++i) assertEquals(exp[i], act[i]);
  }

  /**
   * Tests for correct data size.
   */
  @Test
  public final void size() {
    assertEquals("Unexpected size!", size, context.data().meta.size);
    reload();
    assertEquals("Unexpected size!", size, context.data().meta.size);
  }

  /**
   * Executes the specified command. Gives feedback and stops the test
   * if errors occur.
   * @param cmd command reference
   */
  private static void exec(final Command cmd) {
    try {
      cmd.execute(context);
    } catch(final BaseXException ex) {
      Util.errln(ex);
    }
  }
}
