package org.basex.local.single;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test class performs some incremental updates.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
@RunWith(Parameterized.class)
public final class UpdIndexTest extends SandboxTest {
  /**
   * Test parameters.
   * @return parameters
   */
  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(new Object[][] {
        { false, false }, { true, false }, { false, true }, { true, true }
    });
  }

  /** Number of steps. */
  private static final int STEPS = 10;
  /** Maximum number of entries. */
  private static final int MAX = 2000 / STEPS;

  /** Incremental index update flag. */
  private final boolean updindex;
  /** Main memory flag. */
  private final boolean mainmem;

  /**
   * Constructor.
   * @param updindex incremental index update flag
   * @param mainmem main memory flag
   */
  public UpdIndexTest(final boolean updindex, final boolean mainmem) {
    this.updindex = updindex;
    this.mainmem = mainmem;
  }

  /**
   * Initializes the test.
   */
  @Before
  public void init() {
    set(MainOptions.UPDINDEX, updindex);
    set(MainOptions.MAINMEM, mainmem);
    execute(new CreateDB(NAME, "<xml/>"));
    set(MainOptions.AUTOFLUSH, false);
    set(MainOptions.TOKENINDEX, true);
  }

  /**
   * Finishes the test.
   */
  @After
  public void finish() {
    set(MainOptions.TOKENINDEX, false);
    execute(new DropDB(NAME));
  }

  /**
   * Incremental test.
   */
  @Test
  public void insertInto() {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) query("insert node <x/> into /*");
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   */
  @Test
  public void insertBefore() {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> before /*[1]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   */
  @Test
  public void insertAfter() {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> after /*[last()]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   */
  @Test
  public void insertDeep() {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> into //*[not(*)]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   */
  @Test
  public void replaceValue()  {
    final Random rnd = new Random();
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < MAX * STEPS; i++) {
      final char ch = (char) ('@' + (rnd.nextInt() & 0x1F));
      sb.append(ch == '@' ? ' ' : ch);
      query("replace value of node /* with '" + sb + '\'');
      query("string-length(/*)", sb.length());
    }
  }
}
