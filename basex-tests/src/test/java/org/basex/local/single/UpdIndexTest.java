package org.basex.local.single;

import java.util.*;
import java.util.stream.Stream;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This test class performs some incremental updates.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UpdIndexTest extends SandboxTest {
  /**
   * Test parameters.
   * @return parameters
   */
  public static Stream<Arguments> params() {
    return Stream.of(
      Arguments.of(false, false),
      Arguments.of(true, false),
      Arguments.of(false, true),
      Arguments.of(true, true)
    );
  }

  /** Number of steps. */
  private static final int STEPS = 10;
  /** Maximum number of entries. */
  private static final int MAX = 2000 / STEPS;

  /**
   * Initializes the test.
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  private void init(final boolean updindex, final boolean mainmem) {
    set(MainOptions.UPDINDEX, updindex);
    set(MainOptions.MAINMEM, mainmem);
    execute(new CreateDB(NAME, "<xml/>"));
    set(MainOptions.AUTOFLUSH, false);
    set(MainOptions.TOKENINDEX, true);
  }

  /**
   * Finishes the test.
   */
  @AfterEach public void finish() {
    set(MainOptions.TOKENINDEX, false);
    execute(new DropDB(NAME));
  }

  /**
   * Incremental test.
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  @ParameterizedTest
  @MethodSource("params")
  public void insertInto(final boolean updindex, final boolean mainmem) {
    init(updindex, mainmem);
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
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  @ParameterizedTest
  @MethodSource("params")
  public void insertBefore(final boolean updindex, final boolean mainmem) {
    init(updindex, mainmem);
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
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  @ParameterizedTest
  @MethodSource("params")
  public void insertAfter(final boolean updindex, final boolean mainmem) {
    init(updindex, mainmem);
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
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  @ParameterizedTest
  @MethodSource("params")
  public void insertDeep(final boolean updindex, final boolean mainmem) {
    init(updindex, mainmem);
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
   * @param updindex incremental index update flag.
   * @param mainmem main memory flag.
   */
  @ParameterizedTest
  @MethodSource("params")
  public void replaceValue(final boolean updindex, final boolean mainmem)  {
    init(updindex, mainmem);
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
