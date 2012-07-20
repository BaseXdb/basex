package org.basex.test.query.simple;

import org.basex.test.query.*;

/**
 * Simple XQuery tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SimpleTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<x>X</x>";

    queries = new Object[][] {
      { "Compare 1", "xs:QName('a') = attribute a { 'b' }" },

      { "FLWOR 1", itr(3), "(for $i in 1 to 5 return $i)[3]" },
      { "FLWOR 2", itr(4),
        "(for $a in 1 to 5 for $b in 1 to 5 return $a * $b)[7]" },
      { "FLWOR 3", bool(true), "declare namespace x = 'X'; " +
        "let $a := <a>0</a> let $b := $a return $b = 0" },
      { "FLWOR 4", itr(1),
        "for $a in (1,2) let $b := 'a' where $a = 1 return $a" },
      { "FLWOR 5", itr(1, 2),
        "for $a in (1,2) let $b := 'a'[$a = 1] return $a" },
      { "FLWOR 6", empty(),
        "for $a in (1,2) let $b := 3 where $b = 4 return $a" },
      { "FLWOR 7", itr(1, 2),
        "for $a in (1,2) let $b := 3[. = 4] return $a" },
      { "FLWOR 8", itr(2),
        "for $a at $p in (1,2) where $a = 2 return $p" },

      { "CompForLet 1", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 return $b" },
      { "CompForLet 2", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 let $c := 3 return $c" },
      { "CompForLet 3", itr(4, 4),
        "for $a in 1 to 2 let $b := 3 let $b := 4 return $b" },
      { "CompForLet 4", itr(3),
        "for $a score $s in 1 let $s := 3 return $s" },
      { "CompForLet 4", itr(1),
        "for $a at $p in 1 let $s := $p return $s" },

      { "ExtVar 1", itr(1), "declare variable $a external; 1" },
      { "ExtVar 2", "declare variable $a external; $a" },

      { "If  1", bool(true), "if(true()) then true() else false()" },
      { "If  2", bool(false), "if(false()) then true() else false()" },
      { "If  3", bool(true), "if(true() = true()) then true() else false()" },
      { "If  4", itr(1), "if(boolean(<x/>) eq true()) then 1 else 2" },
      { "If  5", itr(2), "if(boolean(<x/>) ne true()) then 1 else 2" },
      { "If  6", itr(2), "if(boolean(<x/>) eq false()) then 1 else 2" },
      { "If  7", itr(1), "if(boolean(<x/>) ne false()) then 1 else 2" },
      { "If  8", itr(1), "if(boolean(<x/>) = true()) then 1 else 2" },
      { "If  9", itr(2), "if(boolean(<x/>) != true()) then 1 else 2" },
      { "If 10", itr(2), "if(boolean(<x/>) = false()) then 1 else 2" },
      { "If 11", itr(1), "if(boolean(<x/>) != false()) then 1 else 2" },
      { "If 12", "if(<x/> = true()) then 1 else 2" },

      { "And 1", bool(true), "<a/> and <a/>" },
      { "And 2", bool(true), "<a/> and (<a/> and <a/>)" },
      { "And 3", bool(false), "<a/> and (<a/> and not(<a/>))" },

      { "Or 1", bool(true), "<a/> or <a/>" },
      { "Or 2", bool(true), "<a/> or (<a/> or <a/>)" },
      { "Or 3", bool(false), "not(<a/>) or (not(<a/>) or not(<a/>))" },

      { "Seq 1", itr(), "((( )  )    )" },
      { "Seq 2", itr(1), "((( 1 )  )    )" },
      { "Seq 3", itr(1, 2), "((( 1,2 )  )    )" },
      { "Seq 4", itr(1, 2, 3), "(1, (( 2,3 )  )    )" },
      { "Seq 5", itr(1, 2, 3, 4), "(1, (( 2,3 )  ),4   )" },
      { "Seq 6", "()()" },
      { "Seq 7", "() ()" },

      { "Filter 1", "1[1][error()]" },
      { "Filter 2", empty(), "1[1][<x/>/a]" },

      { "ContextItem 0", node(0), "." },
      { "ContextItem 1", node(0), "42[not(.)], ." },
      { "ContextItem 2", node(0), "try { 1[error()] } catch * {.}" },
      { "ContextItem 3", node(0), "try { 1[error()][1] } catch * {.}" },
      { "ContextItem 4", node(0), "try { 1[1][error()] } catch * {.}" },
      { "ContextItem 5", node(0),
        "try { let $a := <a><b/></a> return $a/b[error()] } catch * {.}" },
      { "ContextItem 6", itr(1),
        "declare function local:x() {1+<x/>};1[try { local:x() } catch *{.}]" },
      { "ContextItem 7", node(0), "try { <a/>/(1+'') } catch * {.}" },

      { "Path 1", empty(), "<a/>[./(@*)]" },

      { "Cast 1", itr(1), "xs:integer('+1')" },
      { "Cast 2", "xs:integer('++1')" },

      { "Div 1", "xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')" },
      { "Div 2", "xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')" },
    };
  }
}
