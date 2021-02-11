package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple XQuery tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SimpleTest extends QueryTest {
  static {
    create("<x>X</x>");

    queries = new Object[][] {
      { "Number 1", decimal(2), "1.+1." },

      { "Float 1", "xs:float('Infinity')" },
      { "Float 2", "xs:float('infinity')" },
      { "Float 3", booleans(true), "xs:float('INF') > 0" },
      { "Float 4", "xs:float('inf')" },
      { "Float 5", "xs:float('-Infinity')" },
      { "Float 6", "xs:float('-infinity')" },
      { "Float 7", booleans(true), "xs:float('-INF') < 0" },
      { "Float 8", "xs:float('-inf')" },

      { "Double 1", "xs:double('Infinity')" },
      { "Double 2", "xs:double('infinity')" },
      { "Double 3", booleans(true), "xs:double('INF') > 0" },
      { "Double 4", "xs:double('inf')" },
      { "Double 5", "xs:double('-Infinity')" },
      { "Double 6", "xs:double('-infinity')" },
      { "Double 7", booleans(true), "xs:double('-INF') < 0" },
      { "Double 8", "xs:double('-inf')" },

      { "Annotation 1", integers(1), "declare %local:x(.1) variable $a := 1; $a" },
      { "Annotation 2", integers(1), "declare %local:x(1.) variable $a := 1; $a" },
      { "Annotation 3", "declare %local:x(.) variable $a := 1; $a" },

      { "Compare 1", booleans(true), "xs:QName('b') = attribute a { 'b' }" },
      { "Compare 2", booleans(false), "<a/>/x = (c, ())" },
      { "Compare 3", booleans(false), "(4,5,6) < (1,2)" },
      { "Compare 4", booleans(false), "(4,5) < (1,2,3)" },
      { "Compare 5", booleans(false), "1234567890.12345678 = 1234567890.1234567" },
      { "Compare 6", booleans(false), "123456789012345678  = 123456789012345679" },

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
      { "And 4", booleans(false), "0 and (1 + 'x')" },

      { "Or 1", booleans(true), "<a/> or <a/>" },
      { "Or 2", booleans(true), "<a/> or (<a/> or <a/>)" },
      { "Or 3", booleans(false), "not(<a/>) or (not(<a/>) or not(<a/>))" },
      { "Or 4", booleans(true), "fold-left(true(), false(), function($a, $b) { $a or $b })" },
      { "Or 5", booleans(true), "1 or (1 + 'x')" },

      { "Seq 1", integers(), "((( )  )    )" },
      { "Seq 2", integers(1), "((( 1 )  )    )" },
      { "Seq 3", integers(1, 2), "((( 1,2 )  )    )" },
      { "Seq 4", integers(1, 2, 3), "(1, (( 2,3 )  )    )" },
      { "Seq 5", integers(1, 2, 3, 4), "(1, (( 2,3 )  ),4   )" },
      { "Seq 6", "()()" },
      { "Seq 7", "() ()" },

      { "IntersectExcept 1", empty(), "<a/> intersect <b/>" },
      { "IntersectExcept 2", empty(), "<a/> intersect <b/> except <c/>" },
      { "IntersectExcept 3", empty(), "<a/> except <b/> intersect <c/>" },
      { "IntersectExcept 4", integers(1), "count(<a/> except <b/>)" },
      { "IntersectExcept 5", strings("a"), "<a/> ! (. intersect (., <b/>))/name()" },

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
      { "Filter 12", empty(), "for $n in 0 to 1 return 'a'[position()= $n to 0]" },
      { "Filter 13", strings("a", "a"), "for $n in 0 to 1 return ('a','b')[position()= $n to 1]" },

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
      { "Path 3", integers(2, 2), "<a><b/><b/></a>/b/last()" },

      { "Cast 1", integers(1), "xs:integer('+1')" },
      { "Cast 2", "xs:integer('++1')" },
      { "Cast 3", booleans(false), "string('/') castable as xs:QName" },
      { "Cast 4", strings("error"),
        "try { '1999-12-31'/. castable as xs:date } catch err:XPTY0019 { 'error' }" },
      { "Cast 5", strings("bar"),
          "declare function local:shortcircuit($a) {"
          + "  if($a castable as xs:double and xs:double($a) gt 0) then $a else 'bar'"
          + "};"
          + "local:shortcircuit('foo')" },
      { "Cast 6", empty(), "xs:integer(())" },
      { "Cast 7", empty(), "xs:integer#1(())" },
      { "Cast 8", empty(), "xs:integer(?)(())" },
      { "Cast 9", strings("1", "2"), "('1', '2 3') ! xs:NMTOKENS(.)[1]" },
      { "Cast 10", "exactly-one(xs:NMTOKENS(<x>1 2</x>))" },

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

      { "Catch 1", "try { 1+'' } catch XPTY0004 { 1 }" },
      { "Catch 2", integers(1), "try { 1+'' } catch err:XPTY0004 { 1 }" },
      { "Catch 3", integers(1), "try { 1+'' } catch *:XPTY0004 { 1 }" },
      { "Catch 4", integers(1), "try { 1+'' } catch err:* { 1 }" },
      { "Catch 5", integers(1), "try { 1+'' } catch * { 1 }" },
      { "Catch 6", integers(1),
        "declare function local:f($x) { try { 1 idiv $x } catch * { 1 } }; local:f(0)" },

      { "NodeTest 1", strings("a"),
        "let $d as document-node(element()) := parse-xml('<!--a--><a/>') return name($d/*)" },
      { "NodeTest 2", strings("a"),
        "let $d as document-node(element(a)) := parse-xml('<!--a--><a/>') return name($d/*)" },

      { "FuncTest 1", integers(1), "xquery version '1.0';" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 42 };" +
        "local:foo()" },
      { "FuncTest 2", integers(1), "xquery:eval('" +
        "declare function local:foo() { count(local:bar()) };" +
        "declare function local:bar() { 1 };" +
        "local:foo()')" },
      { "FuncTest 3", "local:a(), local:a(1)" },
      { "FuncTest 4", empty(), "()/x[function($x as item()){1}(.)]" },

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
      { "Limits 11", "0 to 9223372036854775807" },
      { "Limits 11", "-9223372036854775807 to 9223372036854775807" },

      { "Empty 1", strings(""), "format-integer(let $x := random:integer() return (), '0')" },
      { "Empty 2", empty(), "math:sin(let $x := random:integer() return ())" },
      { "Empty 3", booleans(true), "let $a := () return empty($a)" },
      { "Empty 4", booleans(false), "let $a := () return exists($a)" },
      { "Empty 5", booleans(true), "declare function local:foo($x as empty-sequence())"
          + "as xs:boolean { empty($x) }; local:foo(())" },

      { "Map 1", strings("c", "a"), "<a/> ! (('b'!'c'), name())" },
      { "Map 2", integers(5), "((1 to 100000) ! 5)[1]" },
      { "Map 3", strings("a", "b"), "<a><b/></a>/b ! ancestor-or-self::node() ! name()" },
      { "Map 4", strings("a", "b"), "<_ a='a' b='b'/> ! (@a, @b) ! string()" },

      { "Constructor 1", strings("1"), "<n xmlns='u'>{attribute{'a'}{1}}</n>/@a/string()" },

      // #1140
      { "Pred 1", empty(), "declare function local:test() {" +
          "for $n in (1, 1) return <_><c/><w/></_>/*[$n[1]] }; local:test()/self::w" },
      { "Pred 2", empty(), "for $n in (2,2) return (<c><c0/></c>, <d><d0/><d2/></d>)/*[$n[$n]]" },
      { "Pred 3", strings("XML"), "(('XML')[1])[1]" },
      { "Pred 4", integers(1), "1[position() = 1 to 2]" },
      { "Pred 5", integers(1), "1[position() = (1,2)]" },
      { "Pred 6", integers(1), "count((text { 'x' }, element x {})[. instance of element()])" },

      { "FItem 1", integers(1), "declare context item := 0; last#0()" },
      { "FItem 2", integers(2), "declare context item := 1; let $f := last#0 return (2,3)[$f()]" },

      { "List 1", integers(1, 10000000000L),
        "for $i in (1, 10000000000) return (1 to $i)[last()]" },
      { "List 2", strings("x", "x"),
        "for $i in (1, 10000000000) return (1 to $i, 'x')[last()]" },
      { "List 3", integers(2, 10000000001L),
        "for $i in (1, 10000000000) return count((1 to $i, 'x'))" },

      { "Identity 1", booleans(false),
          "let $a := <a/> " +
          "let $b := <_>{ $a }</_> " +
          "return $b/a is $a" },
      { "Identity 2", booleans(true),
          "let $a := <a/> " +
          "let $b := (# db:copynode false #) { <_>{ $a }</_> } " +
          "return $b/a is $a" },
    };
  }
}
