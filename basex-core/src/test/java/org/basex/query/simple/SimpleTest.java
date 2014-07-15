package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple XQuery tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class SimpleTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<x>X</x>";

    queries = new Object[][] {
      { "Number 1", dec(2), "1.+1." },

      { "Annotation 1", itr(1), "declare %local:x(.1) variable $a := 1; $a" },
      { "Annotation 2", itr(1), "declare %local:x(1.) variable $a := 1; $a" },
      { "Annotation 3", "declare %local:x(.) variable $a := 1; $a" },

      { "Compare 1", "xs:QName('a') = attribute a { 'b' }" },
      { "Compare 2", bool(false), "<a/>/x = (c, ())" },

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

      { "ForLet 1", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 return $b" },
      { "ForLet 2", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 let $c := 3 return $c" },
      { "ForLet 3", itr(4, 4),
        "for $a in 1 to 2 let $b := 3 let $b := 4 return $b" },
      { "ForLet 4", itr(3),
        "for $a score $s in 1 let $s := 3 return $s" },
      { "ForLet 5", itr(1),
        "for $a at $p in 1 let $s := $p return $s" },
      { "ForLet 6",
        "let $a as xs:string := <a/> return 1" },

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
      { "Filter 3", str("b"), "name(<x><a/><b c='d'/></x>/(a,b)[@c])" },
      { "Filter 4", str("b"), "name(<x><a/><b/></x>/(b,a)[self::b])" },
      { "Filter 5", empty(), "<x><a><b c='d'/></a></x>/(a,b)[@c]" },
      { "Filter 6", bool(true), "empty((1,2,3)[3][2])" },
      { "Filter 7", bool(true), "empty((1,2,3)[position() = 3][2])" },
      { "Filter 8", itr(1), "1[boolean(max((<a>1</a>, <b>2</b>)))]" },
      { "Filter 9", str("x"), "string(<n><a/><a>x</a></n>/a/text()[.][.])" },
      { "Filter 10", str("x"), "string(<n><a/><a>x</a></n>/a/text()[1][1])" },

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
      { "ContextItem 8", itr(1, 1), "('a', 'b') ! count(.)" },

      { "Path 1", empty(), "<a/>[./(@*)]" },

      { "Cast 1", itr(1), "xs:integer('+1')" },
      { "Cast 2", "xs:integer('++1')" },
      { "Cast 3", bool(false), "string('/') castable as xs:QName" },
      { "Cast 4", str("error"),
        "try { '1999-12-31'/. castable as xs:date } catch err:XPTY0019 { 'error' }" },

      { "Div 1", "xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')" },
      { "Div 2", "xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')" },

      { "Mixed 1", "(<a/>,<b/>)/(if(name() = 'a') then <a/> else 2)/." },

      { "Typeswitch 1", itr(1),
        "typeswitch(<a>1</a>) case xs:string return 1 default return 1" },
      { "Typeswitch 2", itr(1),
        "typeswitch(<a>1</a>) case $a as xs:string return 1 default return 1" },
      { "Typeswitch 3", itr(1, 2),
        "typeswitch(<a>1</a>) case $a as xs:string return (1,2) default return (1,2)" },

      { "FunctionContext 0", "declare function local:x() { /x }; local:x()" },

      { "XQuery 3.0", empty(), "xquery version '3.0'; <a/>/node()" },
      { "XQuery 1.0", empty(), "xquery version '1.0'; <a/>/node()" },

      { "DeclFun 1", itr(0, 1), "declare function local:x($x as xs:integer) { $x }; " +
        "let $a := 0, $b := 1 return try { local:x( (1 to 20) ) } catch * { ($a,$b) }" },
      { "DeclFun 2",
        "declare function local:b($p as element()) { element a {} };" +
        "declare function local:a($s as xs:string) { local:b($s) }; local:a('x')" },

      { "Catch 1", "try { 1+'' } catch XPTY0004 { 1 }" },
      { "Catch 2", itr(1), "try { 1+'' } catch err:XPTY0004 { 1 }" },
      { "Catch 3", itr(1), "try { 1+'' } catch *:XPTY0004 { 1 }" },
      { "Catch 4", itr(1), "try { 1+'' } catch err:* { 1 }" },
      { "Catch 5", itr(1), "try { 1+'' } catch * { 1 }" },
      { "Catch 6", itr(1),
        "declare function local:f($x) { try { 1 idiv $x } catch * { 1 } }; local:f(0)" },

      { "FuncTest 1", itr(1), "xquery version '1.0';" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 42 };" +
        "local:foo()" },
      { "FuncTest 2", itr(1), "xquery:eval('" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 1 };" +
        "local:foo()')" },
      { "FuncTest 3", "local:a(), local:a(1)" },

      { "StaticVar 1", "declare variable $CONFIG := $CONFIG; delete node <a/>" },

      { "Limits 1", itr(9223372036854775806L), "2 * 4611686018427387903" },
      { "Limits 2", "2 * 4611686018427387904" }, // overflow
      { "Limits 3", itr(-9223372036854775808L), "-2 * 4611686018427387904" },
      { "Limits 4", "4611686018427387905 * -2" }, // underflow
      { "Limits 5", itr(-9223372036854775808L), "xs:decimal('18446744073709551616') idiv -2" },
      { "Limits 6", "xs:decimal('18446744073709551616') idiv 2" }, // does not fit
      { "Limits 7", itr(9223372036854775806L), "xs:decimal('18446744073709551612') idiv 2" },
      { "Limits 8", "-9223372036854775808 idiv -1" },
      { "Limits 9", "-9223372036854775807 - 1024" },
      { "Limits 10", "-9223372036854775808 - 1" },
      // { "Limits 11", itr(-9223372036854775808L), "-9223372036854775808" },

      { "Empty 1", str(""), "format-integer(let $x := random:integer() return (), '0')" },
      { "Empty 2", empty(), "math:sin(let $x := random:integer() return ())" },
      { "Empty 3", bool(true), "let $a := () return empty($a)" },
      { "Empty 4", bool(false), "let $a := () return exists($a)" },
      { "Empty 5", bool(true), "declare function local:foo($x as empty-sequence())"
          + "as xs:boolean { empty($x) }; local:foo(())" }
    };
  }
}
