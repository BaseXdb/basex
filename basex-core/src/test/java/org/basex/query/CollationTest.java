package org.basex.query;

import org.junit.*;

/**
 * XQuery collation tests.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class CollationTest extends AdvancedQueryTest {
  /** Collation arguments used in the tests. */
  private static final String ARGS = "lang=de;strength=primary";
  /** Collation used in the tests. */
  private static final String COLLATION = "http://basex.org/collation?" + ARGS;
  /** Default collation used in the tests. */
  private static final String PROLOG = "declare default collation '" + COLLATION + "'; ";
  /** Strengths. */
  private static final String[] STRENGTHS = {
    "primary", "secondary", "tertiary", "identical"
  };

  /** Checks the German collation and different strengths. */
  @Test
  public void german() {
    for(int s = 0; s < STRENGTHS.length; s++) {
      final String prolog = "declare default collation " +
          "'http://basex.org/collation?lang=de;strength=" + STRENGTHS[s] + "'; ";
      query(prolog + "'A'     ='A'      ", true);
      query(prolog + "'\u00c4'='A\u0308'", s < 3);
      query(prolog + "'A'     ='a'      ", s < 2);
      query(prolog + "'A'     ='\u00c4' ", s < 1);
      query(prolog + "'A'     ='B'      ", false);

      query(prolog + "count(('A', '\u00c4')[. >= 'A' and . <= 'B']) eq 2", true);
    }
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

  /** Tests functions. */
  @Test
  public void functions() {
    query("compare('a', '\u00c4', '" + COLLATION + "')", "0");
    query(PROLOG + "compare('a', '\u00c4')", "0");

    query(PROLOG + "contains('XaX', '\u00c4')", true);
    query(PROLOG + "starts-with('aX', '\u00c4')", true);
    query(PROLOG + "ends-with('Xa', '\u00c4')", true);
    query(PROLOG + "starts-with('Xa', '\u00c4')", false);
    query(PROLOG + "ends-with('aX', '\u00c4')", false);
    query(PROLOG + "substring-before('XaY', '\u00c4')", "X");
    query(PROLOG + "substring-after('XaY', '\u00c4')", "Y");

    query(PROLOG + "distinct-values(('a', '\u00c4'))", "a");
    query(PROLOG + "index-of('a', '\u00c4')", "1");
    query(PROLOG + "deep-equal('a', '\u00c4')", true);

    query(PROLOG + "min(('\u00c4', 'a'))", "\u00c4");
    query(PROLOG + "max(('a', '\u00c4'))", "a");

    query(PROLOG + "default-collation()", COLLATION);
    query("declare default collation '?" + ARGS + "'; default-collation()", COLLATION);
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
  @Ignore
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
}
