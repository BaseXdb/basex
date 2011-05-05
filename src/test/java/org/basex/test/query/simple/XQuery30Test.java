package org.basex.test.query.simple;


/**
 * XQuery 3.0 tests (former: 1.1).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class XQuery30Test extends QueryTest {
  /** Constructor. */
  static {
    doc = "<dummy/>";

    queries = new Object[][] {
      { "FLWOR 1", itr(1), "for $i in (1,1) group by $i return $i" },
      { "FLWOR 2", itr(1, 2), "for $i in (1, 2, 2, 1) group by $i return $i" },
      { "FLWOR 3", itr(1, 1, 2, 2, 2, 2, 1, 1),
         "for $x in (1, 2, 2, 1) for $y in ('a','a') group by $y return $x " },
      { "FLWOR 4", itr(1, 2, 1, 1, 2, 2),
         "for $x in (1, 2) for $y in ('b','a','a') group by $y return $x " },
      { "FLWOR 5", itr(1, 2),
         "for $a in 1 let $b := (1, 2) group by $a return $b" },

      { "FLWOR group varref", itr(2, 1),
         "for $x in (2,1) let $y:=($x+1) group by $y return $x" },
      { "GFLWOR varref ordered", itr(1, 2),
         "for $x in (2,1) let $y:=($x+1) group by $y order by $y return $x" },
      { "FLWOR group ngvar", itr(3, 2, 1),
         "for $x in (1,2,3) for $y in ('a') group by $x order by $y, " +
         "$x descending return $x" },

      { "FLWOR Err 1", "let $x := (1,2) group by $x return $x" },
      { "FLWOR Err 2", "let $x := (1,2) group by $z return $x" },
      { "FLWOR Err 3",
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },
      { "FLWOR Err 4",
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },
      { "FLWOR 6", itr(2),
        "for $a in 1 for $a in 2 group by $a return $a" },
      { "FLWOR 7", itr(2, 3),
        "for $a in 1 for $a in (2,3) group by $a return $a" },
      { "FLWOR 8", itr(1, 1),
       "for $s in (1,1) let $p := $s group by " +
       "$p order by $p return $s" },
      { "FLWOR 9", itr(0),
        "for $s in (1) let $p := () group by $p order by $p return 0" },
      { "FLWOR 10", itr(1),
        "for $i as xs:integer in 1 for $i in 1 return $i" },
      { "FLWOR Err 4 (global)",
        "declare variable $a := 1; for $b in 1 group by $a return $b" },
      { "FLWOR 11", itr(1),
        "for $x in (1,1) let $y := () group by $x order by $y return 1" },
    };
  }
}
