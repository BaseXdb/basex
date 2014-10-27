package org.basex.query.util;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.util.collation.BaseXCollationOptions.*;
import org.junit.*;

/**
 * XQuery collation tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class CollationTest extends AdvancedQueryTest {
  /** Collation arguments used in the tests. */
  private static final String ARGS = "lang=de;strength=primary";
  /** Collation used in the tests. */
  private static final String COLLATION = "http://basex.org/collation?" + ARGS;
  /** Default collation used in the tests. */
  private static final String PROLOG = "declare default collation '" + COLLATION + "'; ";

  /** Checks the German collation and different strengths. */
  @Test
  public void german() {
    for(final Strength strength : Strength.values()) {
      final int s = strength.ordinal();
      final String prolog = "declare default collation " +
          "'http://basex.org/collation?lang=de;strength=" + strength + "'; ";
      query(prolog + "'A'     ='A'      ", true);
      query(prolog + "'\u00c4'='A\u0308'", s < 3);
      query(prolog + "'A'     ='a'      ", s < 2);
      query(prolog + "'A'     ='\u00c4' ", s < 1);
      query(prolog + "'A'     ='B'      ", false);

      query(prolog + "count(('A', '\u00c4')[. >= 'A' and . <= 'B']) eq 2", true);
    }
  }

  /** Case-insensitive collation. */
  @Test public void caseInsensitive() {
    final String prolog = "declare default collation " +
        "'http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive'; ";
    query(prolog + "'A'='A'", true);
    query(prolog + "'A'='a'", true);
    query(prolog + "'a'='A'", true);
    query(prolog + "'\u00c4'='A\u00e4'", false);
    query(prolog + "compare('a', 'A')", "0");
  }

  /** Tests errors. */
  @Test
  public void errors() {
    error("compare('a', 'b', 'http://basex.org/collation?unknown=value')", WHICHCOLL_X);
    error("compare('a', 'b', 'http://basex.org/collation?strength=unknown')", WHICHCOLL_X);
    error("compare('a', 'b', 'http://basex.org/collation?decomposition=unknown')", WHICHCOLL_X);
    error("compare('a', 'b', 'http://basex.org/collation?lang=unknown')", WHICHCOLL_X);
  }

  /** Tests operators. */
  @Test
  public void operators() {
    query(PROLOG + "'\u00c4'  = 'a'", true);
    query(PROLOG + "'\u00c4' != 'a'", false);
    query(PROLOG + "'\u00c4'  > 'a'", false);
    query(PROLOG + "'\u00c4'  < 'a'", false);
    query(PROLOG + "'\u00c4' <= 'a'", true);
    query(PROLOG + "'\u00c4' >= 'a'", true);

    query(PROLOG + "'\u00c4' eq 'a'", true);
    query(PROLOG + "'\u00c4' ne 'a'", false);
    query(PROLOG + "'\u00c4' gt 'a'", false);
    query(PROLOG + "'\u00c4' lt 'a'", false);
    query(PROLOG + "'\u00c4' le 'a'", true);
    query(PROLOG + "'\u00c4' ge 'a'", true);
  }

  /** Tests the order by clause. */
  @Test
  public void orderBy() {
    // identical strings
    query(PROLOG + "for $a in ('b', 'b') order by $a return $a", "b b");
    query("for $a in ('b', 'b') order by $a collation '" + COLLATION + "'return $a",
        "b b");
    // different strings
    query(PROLOG + "for $a in ('\u00c4', 'b') order by $a return $a", "\u00c4 b");
    query("for $a in ('\u00c4', 'b') order by $a collation '" + COLLATION + "'return $a",
        "\u00c4 b");
    query("for $a in ( '\u00c4', 'A' ) " +
        "group by $b := $a collation '?lang=de;strength=primary' " +
        "return count($a)", 2);
  }

  /** Tests the group by clause. */
  @Test
  public void groupBy() {
    // identical strings
    query(PROLOG + "for $a in ('a','a') group by $b:=$a return count($a)", "2");
    query("for $a in ('a','a') group by $b:=$a return count($a)", "2");
    // different strings
    query(PROLOG + "for $a in ('a','\u00c4') group by $b:=$a return count($a)", "2");
    query("for $a in ('a','\u00c4') group by $b:=$a collation '" + COLLATION +
        "' return count($a)", "2");
    query("for $a in 'a' group by $a, $a:=$a collation '" + COLLATION +
        "' return $a", "a");
  }

  /** Disallow index rewritings. */
  @Test
  public void index() {
    query(PROLOG + "doc('<X>&#xe4;</X>')/X/text()[. = 'a']", "\u00e4");
    query(PROLOG + "doc('<X>&#xe4;</X>')/X[text() = 'a']", "<X>\u00e4</X>");
  }
}
