package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Simple XQuery tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SimpleTest extends SandboxTest {
  /** Creates the test database (context item: {@code <x>X</x>}). */
  @BeforeAll public static void beforeClass() {
    set(MainOptions.STRIPWS, true);
    execute(new CreateDB(NAME, "<x>X</x>"));
  }

  /** Drops the test database. */
  @AfterAll public static void afterClass() {
    execute(new DropDB(NAME));
    set(MainOptions.STRIPWS, false);
  }

  /** Numbers. */
  @Test public void number() {
    query("1.+1.", 2);
  }

  /** xs:float. */
  @Test public void xsFloat() {
    error("xs:float('Infinity')", FUNCCAST_X_X);
    error("xs:float('infinity')", FUNCCAST_X_X);
    query("xs:float('INF') > 0", true);
    error("xs:float('inf')", FUNCCAST_X_X);
    error("xs:float('-Infinity')", FUNCCAST_X_X);
    error("xs:float('-infinity')", FUNCCAST_X_X);
    query("xs:float('-INF') < 0", true);
    error("xs:float('-inf')", FUNCCAST_X_X);
    query("xs:float('+INF') > 0", true);
  }

  /** xs:double. */
  @Test public void xsDouble() {
    error("xs:double('Infinity')", FUNCCAST_X_X);
    error("xs:double('infinity')", FUNCCAST_X_X);
    query("xs:double('INF') > 0", true);
    error("xs:double('inf')", FUNCCAST_X_X);
    error("xs:double('-Infinity')", FUNCCAST_X_X);
    error("xs:double('-infinity')", FUNCCAST_X_X);
    query("xs:double('-INF') < 0", true);
    error("xs:double('-inf')", FUNCCAST_X_X);
    query("xs:double('+INF') > 0", true);
  }

  /** xs:unsignedLong comparisons. */
  @Test public void unsignedLong() {
    query("xs:unsignedLong('3') eq 3.1", false);
    query("3.1 eq xs:unsignedLong('3')", false);
    query("xs:unsignedLong(3) lt 3.1", true);
    query("3.1 gt xs:unsignedLong(3)", true);
    query("compare(3.1, xs:unsignedLong('3'))", 1);
    query("compare(xs:unsignedLong('3'), 3.1)", -1);
  }

  /** Annotations. */
  @Test public void annotation() {
    query("declare %local:x(.1) variable $a := 1; $a", 1);
    query("declare %local:x(1.) variable $a := 1; $a", 1);
    error("declare %local:x(.) variable $a := 1; $a", NUMBER_X);
  }

  /** Comparisons. */
  @Test public void compare() {
    query("xs:QName('b') = attribute a { 'b' }", true);
    query("<a/>/x = (c, ())", false);
    query("(4, 5, 6) < (1, 2)", false);
    query("(4, 5) < (1, 2, 3)", false);
    query("1234567890.12345678 = 1234567890.1234567", false);
    query("123456789012345678  = 123456789012345679", false);
    // GH-2112, GH-2115
    query("xs:decimal(1.13) gt xs:double(1.13)", true);
    query("xs:decimal(1.13) gt xs:float(1.13)", true);
    query("xs:decimal(1.13) le xs:double(1.13)", false);
    query("xs:decimal(1.13) le xs:float(1.13)", false);
    // GH-2113, GH-2114
    query("xs:float (1.13) ge xs:double(1.13)", false);
    query("xs:float (1.13) le xs:double(1.13)", true);
    query("xs:float (1.13) lt xs:double(1.13)", true);
    query("xs:float (1.13) gt xs:double(1.13)", false);
    query("xs:double(1.13) ge xs:float (1.13)", true);
    query("xs:double(1.13) le xs:float (1.13)", false);
    query("xs:double(1.13) lt xs:float (1.13)", false);
    query("xs:double(1.13) gt xs:float (1.13)", true);

    query("xs:hexBinary('41') = xs:untypedAtomic('41')", true);
    query("xs:untypedAtomic('41') = xs:hexBinary('41')", true);
    query("xs:untypedAtomic('41') <= xs:hexBinary('41')", true);
    query("xs:hexBinary('41') <= xs:untypedAtomic('41')", true);

    query("string-join(replicate(1, 40)) -> (xs:float(.) = <x>{ . }</x>)", false);
    query("string-join(replicate(1, 40)) -> (<x>{ . }</x> = xs:float(.))", false);
    query("string-join(replicate(1, 40)) -> (xs:double(.) = <x>{ . }</x>)", true);
    query("string-join(replicate(1, 40)) -> (<x>{ . }</x> = xs:double(.))", true);
  }

  /** FLWOR expressions. */
  @Test public void flwor() {
    query("(for $i in 1 to 5 return $i)[3]", 3);
    query("(for $a in 1 to 5 for $b in 1 to 5 return $a * $b)[7]", 4);
    query("declare namespace x = 'X'; let $a := <a>0</a> let $b := $a return $b = 0", true);
    query("for $a in (1, 2) let $b := 'a' where $a = 1 return $a", 1);
    query("for $a in (1, 2) let $b := 'a'[$a = 1] return $a", "1\n2");
    query("for $a in (1, 2) let $b := 3 where $b = 4 return $a", "");
    query("for $a in (1, 2) let $b := 3[. = 4] return $a", "1\n2");
    query("for $a at $p in (1, 2) where $a = 2 return $p", 2);
  }

  /** For/let clauses. */
  @Test public void forLet() {
    query("for $a in 1 to 2 let $b := 3 return $b", "3\n3");
    query("for $a in 1 to 2 let $b := 3 let $c := 3 return $c", "3\n3");
    query("for $a in 1 to 2 let $b := 3 let $b := 4 return $b", "4\n4");
    query("for $a score $s in 1 let $s := 3 return $s", 3);
    query("for $a at $p in 1 let $s := $p return $s", 1);
  }

  /** External variables. */
  @Test public void extVar() {
    query("declare variable $a external; 1", 1);
    error("declare variable $a external; $a", VAREMPTY_X);
    query("declare variable $x as enum('a', 'b') := 'a'; $x", "a");
    query("declare variable $x as enum('a', 'b') := xs:anyURI('a'); $x", "a");
  }

  /** If expressions. */
  @Test public void ifExpr() {
    query("if(true()) then true() else false()", true);
    query("if(false()) then true() else false()", false);
    query("if(true() = true()) then true() else false()", true);
    query("if(boolean(<x/>) eq true()) then 1 else 2", 1);
    query("if(boolean(<x/>) ne true()) then 1 else 2", 2);
    query("if(boolean(<x/>) eq false()) then 1 else 2", 2);
    query("if(boolean(<x/>) ne false()) then 1 else 2", 1);
    query("if(boolean(<x/>) = true()) then 1 else 2", 1);
    query("if(boolean(<x/>) != true()) then 1 else 2", 2);
    query("if(boolean(<x/>) = false()) then 1 else 2", 2);
    query("if(boolean(<x/>) != false()) then 1 else 2", 1);
    error("if(<x/> = true()) then 1 else 2", FUNCCAST_X_X_X);
  }

  /** Logical 'and'. */
  @Test public void andExpr() {
    query("<a/> and <a/>", true);
    query("<a/> and (<a/> and <a/>)", true);
    query("<a/> and (<a/> and not(<a/>))", false);
    query("0 and (1 + 'x')", false);
  }

  /** Logical 'or'. */
  @Test public void orExpr() {
    query("<a/> or <a/>", true);
    query("<a/> or (<a/> or <a/>)", true);
    query("not(<a/>) or (not(<a/>) or not(<a/>))", false);
    query("fold-left(true(), false(), function($a, $b) { $a or $b })", true);
    query("1 or (1 + 'x')", true);
  }

  /** Sequences. */
  @Test public void seq() {
    query("((( )  )    )", "");
    query("((( 1 )  )    )", 1);
    query("((( 1, 2 )  )    )", "1\n2");
    query("(1, (( 2,3 )  )    )", "1\n2\n3");
    query("(1, (( 2,3 )  ),4   )", "1\n2\n3\n4");
  }

  /** Intersect/except. */
  @Test public void intersectExcept() {
    query("<a/> intersect <b/>", "");
    query("<a/> intersect <b/> except <c/>", "");
    query("<a/> except <b/> intersect <c/>", "");
    query("count(<a/> except <b/>)", 1);
    query("<a/> ! (. intersect (., <b/>))/name()", "a");
  }

  /** Filters. */
  @Test public void filter() {
    error("1[1][error()]", FUNERR1);
    query("1[1][<x/>/a]", "");
    query("name(<x><a/><b c='d'/></x>/(a, b)[@c])", "b");
    query("name(<x><a/><b/></x>/(b, a)[self::b])", "b");
    query("<x><a><b c='d'/></a></x>/(a, b)[@c]", "");
    query("empty((1, 2, 3)[3][2])", true);
    query("empty((1, 2, 3)[position() = 3][2])", true);
    query("1[boolean(max((<a>1</a>, <b>2</b>)))]", 1);
    query("string(<n><a/><a>x</a></n>/a/text()[.][.])", "x");
    query("string(<n><a/><a>x</a></n>/a/text()[1][1])", "x");
    query("1[1 to 2]", 1);
    query("for $n in 0 to 1 return 'a'[position()= $n to 0]", "");
    query("for $n in 0 to 1 return ('a', 'b')[position()= $n to 1]", "a\na");
  }

  /** Context item. */
  @Test public void contextItem() {
    query(".", "<x>X</x>");
    query("42[not(.)], .", "<x>X</x>");
    query("try { 1[error()] } catch * {.}", "<x>X</x>");
    query("try { 1[error()][1] } catch * {.}", "<x>X</x>");
    query("try { 1[1][error()] } catch * {.}", "<x>X</x>");
    query("try { let $a := <a><b/></a> return $a/b[error()] } catch * {.}", "<x>X</x>");
    query("declare function local:x() {1+<x/>};1[try { local:x() } catch *{.}]", 1);
    query("try { <a/>/(1+'') } catch * {.}", "<x>X</x>");
    query("('a', 'b') ! count(.)", "1\n1");
  }

  /** Paths. */
  @Test public void path() {
    query("<a/>[./(@*)]", "");
    query("<_><x><x>A</x>B</x></_>//x/node()[last()] ! string()", "A\nB");
    query("<a><b/><b/></a>/b/last()", "2\n2");
    error("<local:a/>/self::local :*", QUERYEND_X);
  }

  /** Casts. */
  @Test public void cast() {
    query("xs:integer('+1')", 1);
    error("xs:integer('++1')", FUNCCAST_X_X);
    query("string('/') castable as xs:QName", false);
    query("try { '1999-12-31'/. castable as xs:date } catch err:XPTY0004 { 'error' }", "error");
    query("declare function local:shortcircuit($a) {"
        + "  if($a castable as xs:double and xs:double($a) gt 0) then $a else 'bar'"
        + "};"
        + "local:shortcircuit('foo')", "bar");
    query("xs:integer(())", "");
    query("xs:integer#1(())", "");
    query("xs:integer(?)(())", "");
    query("('1', '2 3') ! xs:NMTOKENS(.)[1]", "1\n2");
    error("exactly-one(xs:NMTOKENS(<x>1 2</x>))", EXACTLYONE);
  }

  /** Mixed content. */
  @Test public void mixed() {
    error("(<a/>, <b/>)/(if(name() = 'a') then <a/> else 2)/.", PATHNODE_X_X_X);
  }

  /** Typeswitch. */
  @Test public void typeswitch() {
    query("typeswitch(<a>1</a>) case xs:string return 1 default return 1", 1);
    query("typeswitch(<a>1</a>) case $a as xs:string return 1 default return 1", 1);
    query("typeswitch(<a>1</a>) case $a as xs:string return (1, 2) default return (1, 2)", "1\n2");
    query("(xs:byte(0), xs:short(0), xs:int(0), xs:long(0), 0) ! "
        + "(typeswitch (.)"
        + " case xs:byte return 1"
        + " case xs:short return 2"
        + " case xs:int return 3"
        + " case xs:long return 4"
        + " case xs:decimal | xs:integer return 5"
        + " default return 6"
        + ")", "1\n2\n3\n4\n5");
    query("(0, xs:byte(0)) ! "
        + "(typeswitch (.)"
        + " case xs:integer return 1"
        + " case xs:byte return 2"
        + " default return 3"
        + ")", "1\n1");
  }

  /** Function context. */
  @Test public void functionContext() {
    error("declare function local:x() { /x }; local:x()", NOCTX_X);
  }

  /** XQuery version declarations. */
  @Test public void xqueryVersion() {
    query("xquery version '3.0'; <a/>/node()", "");
    query("xquery version '1.0'; <a/>/node()", "");
  }

  /** Declared functions. */
  @Test public void declFun() {
    query("declare function local:x($x as xs:integer) { $x }; "
        + "let $a := 0, $b := 1 return try { local:x( (1 to 20) ) } catch * { $a, $b }", "0\n1");
  }

  /** Catch clauses. */
  @Test public void catchClause() {
    error("try { 1+'' } catch XPTY0004 { 1 }", NONUMBER_X_X);
    query("try { 1+'' } catch err:XPTY0004 { 1 }", 1);
    query("try { 1+'' } catch *:XPTY0004 { 1 }", 1);
    query("try { 1+'' } catch err:* { 1 }", 1);
    query("try { 1+'' } catch * { 1 }", 1);
    query("declare function local:f($x) { try { 1 idiv $x } catch * { 1 } }; local:f(0)", 1);
  }

  /** Node tests. */
  @Test public void nodeTest() {
    query("let $d as document-node(element()) := parse-xml('<!--a--><a/>') return name($d/*)", "a");
    query("let $d as document-node(element(a)) := parse-xml('<!--a--><a/>') return name($d/*)",
        "a");
  }

  /** Function tests. */
  @Test public void funcTest() {
    query("xquery version '1.0';"
        + "declare function local:foo() { count(local:bar()) };"
        + "declare function local:bar() { 42 };"
        + "local:foo()", 1);
    query("xquery:eval('"
        + "declare function local:foo() { count(local:bar()) };"
        + "declare function local:bar() { 1 };"
        + "local:foo()')", 1);
    error("local:a(), local:a(1)", WHICHFUNC_X);
    query("()/x[function($x as item()){1}(.)]", "");
  }

  /** Numeric limits. */
  @Test public void limits() {
    query("2 * 4611686018427387903", 9223372036854775806L);
    error("2 * 4611686018427387904", RANGE_X);
    query("-2 * 4611686018427387904", -9223372036854775808L);
    error("4611686018427387905 * -2", RANGE_X);
    query("xs:decimal('18446744073709551616') idiv -2", -9223372036854775808L);
    error("xs:decimal('18446744073709551616') idiv 2", RANGE_X);
    query("xs:decimal('18446744073709551612') idiv 2", 9223372036854775806L);
    error("-9223372036854775808 idiv -1", RANGE_X);
    error("-9223372036854775807 - 1024", RANGE_X);
    error("-9223372036854775808 - 1", RANGE_X);
  }

  /** Empty sequences. */
  @Test public void eempty() {
    query("format-integer(let $x := random:integer() return (), '0')", "");
    query("math:sin(let $x := random:integer() return ())", "");
    query("let $a := () return empty($a)", true);
    query("let $a := () return exists($a)", false);
    query("declare function local:foo($x as empty-sequence()) as xs:boolean { empty($x) }; "
        + "local:foo(())", true);
  }

  /** Simple map operator. */
  @Test public void map() {
    query("<a/> ! (('b' ! 'c'), name())", "c\na");
    query("((1 to 100000) ! 5)[1]", 5);
    query("<a><b/></a>/b ! ancestor-or-self::node() ! name()", "a\nb");
    query("<_ a='a' b='b'/> ! (@a, @b) ! string()", "a\nb");
  }

  /** Constructors. */
  @Test public void constructor() {
    query("<n xmlns='u'>{ attribute { 'a' }{ 1 }}</n>/@a/string()", 1);
  }

  /** Predicates (#1140). */
  @Test public void pred() {
    query("declare function local:test() {"
        + "for $n in (1, 1) return <_><c/><w/></_>/*[$n[1]] }; local:test()/self::w", "");
    query("for $n in (2, 2) return (<c><c0/></c>, <d><d0/><d2/></d>)/*[$n[$n]]", "");
    query("(('XML')[1])[1]", "XML");
    query("1[position() = 1 to 2]", 1);
    query("1[position() = (1, 2)]", 1);
    query("count((text { 'x' }, element x {})[. instance of element()])", 1);
  }

  /** Function items. */
  @Test public void fItem() {
    query("declare context item := 0; last#0()", 1);
    query("declare context item := 1; let $f := last#0 return (2, 3)[$f()]", 2);
  }

  /** Lists. */
  @Test public void list() {
    query("for $i in (1, 10000000000) return (1 to $i)[last()]", "1\n10000000000");
    query("for $i in (1, 10000000000) return (1 to $i, 'x')[last()]", "x\nx");
    query("for $i in (1, 10000000000) return count((1 to $i, 'x'))", "2\n10000000001");
  }

  /** Node identity. */
  @Test public void identity() {
    query("let $a := <a/> let $b := <_>{ $a }</_> return $b/a is $a", false);
    query("let $a := <a/> let $b := (# db:copynode false #) { <_>{ $a }</_> } "
        + "return $b/a is $a", true);
  }

  /** Range expressions. */
  @Test public void range() {
    query("count((1 to 10) ! (. to . + 9))", 100);
    query("count((1 to 10) ! (. to . - -9))", 100);
    query("count((1 to 100_000) ! (. to . + 9_999))", 1_000_000_000);
    query("count((-9223372036854775807 - 1) to 9223372036854775807)", Long.MAX_VALUE);
    query("head((-9223372036854775807 - 1) to 9223372036854775807)", Long.MIN_VALUE);
  }

  /** Arithmetics with durations. */
  @Test public void arith() {
    query("string(.5 * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(<_>.5</_> * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * .5)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * <_>.5</_>)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') div .5)", "P2Y");
    query("string(xs:yearMonthDuration('P1Y') div <_>.5</_>)", "P2Y");
  }

  /** URI-qualified names. */
  @Test public void uriQualifiedName() {
    query("declare function local:text($t) {$t}; "
        + "Q{http://www.w3.org/2005/xquery-local-functions}text#1('abc')", "abc");
    query("declare function Q{http://www.w3.org/2005/xquery-loc"
        + "al-functions}text($t) {$t}; local:text('abc')", "abc");
    query("declare function local:text($t) {$t}; "
        + "Q{http://www.w3.org/2005/xquery-local-functions}text('abc')", "abc");
    query("declare function local:text($t) {$t}; "
        + "'abc' => Q{http://www.w3.org/2005/xquery-local-functions}text()", "abc");
  }

  /** Subsequence with large offsets and lengths (no integer overflow). */
  @Test public void subSequence() {
    query("subsequence(1 to 2000, 2000, 9223372036854775807)", 2000);
    query("subsequence(1 to 5, 2, 9223372036854775807)", "2\n3\n4\n5");
    query("subsequence(1 to 5, 3, 2147483648)", "3\n4\n5");
  }

  /** Substring with large positions and lengths (no integer overflow). */
  @Test public void subString() {
    query("substring('hello', 1, 9223372036854775807)", "hello");
    query("substring('hello', 2, 9223372036854775807)", "ello");
    query("substring('hello', 3, 2147483648)", "llo");
    query("substring('hello', -2, 9223372036854775807)", "hello");
  }

  /** Insert-before with an extreme position (no integer underflow). */
  @Test public void insertBefore() {
    query("insert-before((1, 2, 3), -9223372036854775807 - 1, 99)", "99\n1\n2\n3");
    query("insert-before((1, 2, 3), 5, 99)", "1\n2\n3\n99");
  }

  /** Subarray bounds check with a large length (no integer overflow). */
  @Test public void subArray() {
    error("array:subarray([1, 2, 3], 2, 9223372036854775807)", ARRAYBOUNDS_X_X);
    query("array:subarray([1, 2, 3, 4, 5], 2, 3)", "[2,3,4]");
  }
}
