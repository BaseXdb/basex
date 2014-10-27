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
    create("<x>X</x>");

    queries = new Object[][] {
      { "Number 1", decimal(2), "1.+1." },

      { "Annotation 1", integers(1), "declare %local:x(.1) variable $a := 1; $a" },
      { "Annotation 2", integers(1), "declare %local:x(1.) variable $a := 1; $a" },
      { "Annotation 3", "declare %local:x(.) variable $a := 1; $a" },

      { "Compare 1", "xs:QName('a') = attribute a { 'b' }" },
      { "Compare 2", booleans(false), "<a/>/x = (c, ())" },
      { "Compare 3", booleans(false), "(4,5,6) < (1,2)" },
      { "Compare 4", booleans(false), "(4,5) < (1,2,3)" },

      { "FLWOR 1", integers(3), "(for $i in 1 to 5 return $i)[3]" },
      { "FLWOR 2", integers(4),
        "(for $a in 1 to 5 for $b in 1 to 5 return $a * $b)[7]" },
      { "FLWOR 3", booleans(true), "declare namespace x = 'X'; " +
        "let $a := <a>0</a> let $b := $a return $b = 0" },
      { "FLWOR 4", integers(1),
        "for $a in (1,2) let $b := 'a' where $a = 1 return $a" },
      { "FLWOR 5", integers(1, 2),
        "for $a in (1,2) let $b := 'a'[$a = 1] return $a" },
      { "FLWOR 6", empty(),
        "for $a in (1,2) let $b := 3 where $b = 4 return $a" },
      { "FLWOR 7", integers(1, 2),
        "for $a in (1,2) let $b := 3[. = 4] return $a" },
      { "FLWOR 8", integers(2),
        "for $a at $p in (1,2) where $a = 2 return $p" },

      { "ForLet 1", integers(3, 3),
        "for $a in 1 to 2 let $b := 3 return $b" },
      { "ForLet 2", integers(3, 3),
        "for $a in 1 to 2 let $b := 3 let $c := 3 return $c" },
      { "ForLet 3", integers(4, 4),
        "for $a in 1 to 2 let $b := 3 let $b := 4 return $b" },
      { "ForLet 4", integers(3),
        "for $a score $s in 1 let $s := 3 return $s" },
      { "ForLet 5", integers(1),
        "for $a at $p in 1 let $s := $p return $s" },
      { "ForLet 6",
        "let $a as xs:string := <a/> return 1" },

      { "ExtVar 1", integers(1), "declare variable $a external; 1" },
      { "ExtVar 2", "declare variable $a external; $a" },

      { "If  1", booleans(true), "if(true()) then true() else false()" },
      { "If  2", booleans(false), "if(false()) then true() else false()" },
      { "If  3", booleans(true), "if(true() = true()) then true() else false()" },
      { "If  4", integers(1), "if(boolean(<x/>) eq true()) then 1 else 2" },
      { "If  5", integers(2), "if(boolean(<x/>) ne true()) then 1 else 2" },
      { "If  6", integers(2), "if(boolean(<x/>) eq false()) then 1 else 2" },
      { "If  7", integers(1), "if(boolean(<x/>) ne false()) then 1 else 2" },
      { "If  8", integers(1), "if(boolean(<x/>) = true()) then 1 else 2" },
      { "If  9", integers(2), "if(boolean(<x/>) != true()) then 1 else 2" },
      { "If 10", integers(2), "if(boolean(<x/>) = false()) then 1 else 2" },
      { "If 11", integers(1), "if(boolean(<x/>) != false()) then 1 else 2" },
      { "If 12", "if(<x/> = true()) then 1 else 2" },

      { "And 1", booleans(true), "<a/> and <a/>" },
      { "And 2", booleans(true), "<a/> and (<a/> and <a/>)" },
      { "And 3", booleans(false), "<a/> and (<a/> and not(<a/>))" },

      { "Or 1", booleans(true), "<a/> or <a/>" },
      { "Or 2", booleans(true), "<a/> or (<a/> or <a/>)" },
      { "Or 3", booleans(false), "not(<a/>) or (not(<a/>) or not(<a/>))" },
      { "Or 4", booleans(true), "fold-left(true(), false(), function($a, $b) { $a or $b })" },

      { "Seq 1", integers(), "((( )  )    )" },
      { "Seq 2", integers(1), "((( 1 )  )    )" },
      { "Seq 3", integers(1, 2), "((( 1,2 )  )    )" },
      { "Seq 4", integers(1, 2, 3), "(1, (( 2,3 )  )    )" },
      { "Seq 5", integers(1, 2, 3, 4), "(1, (( 2,3 )  ),4   )" },
      { "Seq 6", "()()" },
      { "Seq 7", "() ()" },

      { "Filter 1", "1[1][error()]" },
      { "Filter 2", empty(), "1[1][<x/>/a]" },
      { "Filter 3", strings("b"), "name(<x><a/><b c='d'/></x>/(a,b)[@c])" },
      { "Filter 4", strings("b"), "name(<x><a/><b/></x>/(b,a)[self::b])" },
      { "Filter 5", empty(), "<x><a><b c='d'/></a></x>/(a,b)[@c]" },
      { "Filter 6", booleans(true), "empty((1,2,3)[3][2])" },
      { "Filter 7", booleans(true), "empty((1,2,3)[position() = 3][2])" },
      { "Filter 8", integers(1), "1[boolean(max((<a>1</a>, <b>2</b>)))]" },
      { "Filter 9", strings("x"), "string(<n><a/><a>x</a></n>/a/text()[.][.])" },
      { "Filter 10", strings("x"), "string(<n><a/><a>x</a></n>/a/text()[1][1])" },
      { "Filter 11", "1[1 to 2]" },

      { "ContextItem 0", nodes(0), "." },
      { "ContextItem 1", nodes(0), "42[not(.)], ." },
      { "ContextItem 2", nodes(0), "try { 1[error()] } catch * {.}" },
      { "ContextItem 3", nodes(0), "try { 1[error()][1] } catch * {.}" },
      { "ContextItem 4", nodes(0), "try { 1[1][error()] } catch * {.}" },
      { "ContextItem 5", nodes(0),
        "try { let $a := <a><b/></a> return $a/b[error()] } catch * {.}" },
      { "ContextItem 6", integers(1),
        "declare function local:x() {1+<x/>};1[try { local:x() } catch *{.}]" },
      { "ContextItem 7", nodes(0), "try { <a/>/(1+'') } catch * {.}" },
      { "ContextItem 8", integers(1, 1), "('a', 'b') ! count(.)" },

      { "Path 1", empty(), "<a/>[./(@*)]" },
      { "Path 2", strings("A", "B"), "<_><x><x>A</x>B</x></_>//x/node()[last()] ! string()" },

      { "Cast 1", integers(1), "xs:integer('+1')" },
      { "Cast 2", "xs:integer('++1')" },
      { "Cast 3", booleans(false), "string('/') castable as xs:QName" },
      { "Cast 4", strings("error"),
        "try { '1999-12-31'/. castable as xs:date } catch err:XPTY0019 { 'error' }" },

      { "Div 1", "xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')" },
      { "Div 2", "xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')" },

      { "Mixed 1", "(<a/>,<b/>)/(if(name() = 'a') then <a/> else 2)/." },

      { "Typeswitch 1", integers(1),
        "typeswitch(<a>1</a>) case xs:string return 1 default return 1" },
      { "Typeswitch 2", integers(1),
        "typeswitch(<a>1</a>) case $a as xs:string return 1 default return 1" },
      { "Typeswitch 3", integers(1, 2),
        "typeswitch(<a>1</a>) case $a as xs:string return (1,2) default return (1,2)" },

      { "FunctionContext 0", "declare function local:x() { /x }; local:x()" },

      { "XQuery 3.0", empty(), "xquery version '3.0'; <a/>/node()" },
      { "XQuery 1.0", empty(), "xquery version '1.0'; <a/>/node()" },

      { "DeclFun 1", integers(0, 1), "declare function local:x($x as xs:integer) { $x }; " +
        "let $a := 0, $b := 1 return try { local:x( (1 to 20) ) } catch * { ($a,$b) }" },
      { "DeclFun 2",
        "declare function local:b($p as element()) { element a {} };" +
        "declare function local:a($s as xs:string) { local:b($s) }; local:a('x')" },

      { "Catch 1", "try { 1+'' } catch XPTY0004 { 1 }" },
      { "Catch 2", integers(1), "try { 1+'' } catch err:XPTY0004 { 1 }" },
      { "Catch 3", integers(1), "try { 1+'' } catch *:XPTY0004 { 1 }" },
      { "Catch 4", integers(1), "try { 1+'' } catch err:* { 1 }" },
      { "Catch 5", integers(1), "try { 1+'' } catch * { 1 }" },
      { "Catch 6", integers(1),
        "declare function local:f($x) { try { 1 idiv $x } catch * { 1 } }; local:f(0)" },

      { "FuncTest 1", integers(1), "xquery version '1.0';" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 42 };" +
        "local:foo()" },
      { "FuncTest 2", integers(1), "xquery:eval('" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 1 };" +
        "local:foo()')" },
      { "FuncTest 3", "local:a(), local:a(1)" },

      { "StaticVar 1", "declare variable $CONFIG := $CONFIG; delete node <a/>" },

      { "Limits 1", integers(9223372036854775806L), "2 * 4611686018427387903" },
      { "Limits 2", "2 * 4611686018427387904" }, // overflow
      { "Limits 3", integers(-9223372036854775808L), "-2 * 4611686018427387904" },
      { "Limits 4", "4611686018427387905 * -2" }, // underflow
      { "Limits 5", integers(-9223372036854775808L), "xs:decimal('18446744073709551616') idiv -2" },
      { "Limits 6", "xs:decimal('18446744073709551616') idiv 2" }, // does not fit
      { "Limits 7", integers(9223372036854775806L), "xs:decimal('18446744073709551612') idiv 2" },
      { "Limits 8", "-9223372036854775808 idiv -1" },
      { "Limits 9", "-9223372036854775807 - 1024" },
      { "Limits 10", "-9223372036854775808 - 1" },

      { "Empty 1", strings(""), "format-integer(let $x := random:integer() return (), '0')" },
      { "Empty 2", empty(), "math:sin(let $x := random:integer() return ())" },
      { "Empty 3", booleans(true), "let $a := () return empty($a)" },
      { "Empty 4", booleans(false), "let $a := () return exists($a)" },
      { "Empty 5", booleans(true), "declare function local:foo($x as empty-sequence())"
          + "as xs:boolean { empty($x) }; local:foo(())" },

      { "Map 1", strings("c", "a"), "<a/> ! (('b'!'c'), name())" },
      { "Map 2", integers(5), "((1 to 100000) ! 5)[1]" },
      { "Map 3", strings("a", "b"), "<a><b/></a>/b ! ancestor-or-self::node() ! name()" },
    };
  }
}
