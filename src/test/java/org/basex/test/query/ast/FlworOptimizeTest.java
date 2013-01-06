package org.basex.test.query.ast;

import org.basex.core.*;
import org.basex.query.gflwor.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for rewritings of FLWOR-expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FlworOptimizeTest extends QueryPlanTest {
  /** Tests the relocation of a static let clause. */
  @Test public void moveTop() {
    check("let $b := <x>a</x> " +
        "for $i in 1 to 2 " +
        "let $m := $b " +
        "return $m/text()",

        "aa",
        Util.info("every $l in //% satisfies $l << //%", Let.class, For.class)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void moveMid() {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $a := $seq[$i] " +
        "return concat($i, $j, $a)",

        "12a 13a 23b",
        "let $a := //Let[@var = '$a'] return " +
        "//For[@var eq '$i'] << $a and $a << //For[@var eq '$j']"
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void dontMove() {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $b := $seq[$j] " +
        "return concat($i, $j, $b)",

        "12b 13c 23c",
        Util.info("every $f in //% satisfies $f << //%[@var eq '$b']",
            For.class, Let.class)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void dontMove2() {
    check("let $a := <a/> let $b := <b/> let $c := $a return ($c,$b)",
        "<a/>" + Prop.NL + "<b/>",
        Util.info("//Let[@var='$a'] << //Let[@var='$b'] and " +
            "//Let[@var='$b'] << //Let[@var='$c']")
    );
  }

  /** Tests the relocation of a static let clause. */
  @Test public void moveFor() {
    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b in $x " +
        "return $b",

        "<x/>" + Prop.NL + "<x/>",
        "//For[@var eq '$b'] << //For[@var eq '$a']",
        "every $for in //For satisfies exactly-one(//Let) << $for"
    );
  }
}
