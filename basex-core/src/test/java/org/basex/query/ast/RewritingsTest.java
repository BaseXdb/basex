package org.basex.query.ast;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.up.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Checks query rewritings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RewritingsTest extends SandboxTest {
  /** Input file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Drops the database, resets optimizations. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
  }

  /** Database node sequences are simplified to their string and atomized values. */
  @Test public void dbNodeSeq() {
    execute(new CreateDB(NAME));
    execute(new Add("a", "<x>1</x>"));
    execute(new Add("b", "<x>2</x>"));

    // string context: the documents are replaced with their string values
    check("string-join(" + _DB_GET.args(NAME) + ")", 12, empty(_DB_GET));
    // numeric context: the documents are replaced with their atomized values
    check("sum(" + _DB_GET.args(NAME) + ")", 3, empty(_DB_GET));
  }

  /** Casts. */
  @Test public void gh1795() {
    check("for $n in 1 to 3 return xs:integer($n)[. = 1]", 1, empty(Cast.class));
    check("('a', 'b') ! xs:string(.)", "a\nb", empty(Cast.class), root(StrSeq.class));
    check("xs:string(''[. = <_/>])", "", empty(Cast.class), root(If.class));

    check("xs:string(<_/>[. = '']) = ''", true, empty(Cast.class), root(CmpSimpleG.class));
    check("xs:double(" + wrap(1) + ") + 2", 3,
        empty(Cast.class), type(ArithSimple.class, "xs:double"));
    check("(1, 2)[. != 0] ! (xs:byte(.)) ! (xs:integer(.) + 2)", "3\n4",
        count(Cast.class, 1), type(ArithSimple.class, "xs:integer"));

    error("(if(" + wrap("!") + "= 'a') then 'b') cast as xs:string", INVTYPE_X);
    error("((1 to 1000000000) ! (. || 'x')) cast as xs:string", INVTYPE_X);
    error("() cast as xs:string", INVTYPE_X);
    error("(1 to 100000)[. < 3] cast as xs:integer", INVTYPE_X);
  }

  /** Type promotions. */
  @Test public void gh1801() {
    check("{ xs:string(<_/>): '' } instance of map(xs:string, xs:string)", true);
    check("{ string(<_/>): '' } instance of map(xs:string, xs:string)", true);
  }

  /** Test. */
  @Test public void gh1694() {
    check("count(1)", 1, exists(Itr.class));
    execute(new CreateDB(NAME, "<_/>"));
    final String query =
      "declare function local:b($e) as xs:string { $e };\n" +
      "declare function local:a($db) {\n" +
      "  let $ids := local:b(" + _DB_GET.args(" $db ") + ")\n" +
      "  return " + _DB_GET.args(NAME) + "[*[1] = $ids]\n" +
      "};\n" +
      "local:a('" + NAME + "')";

    query(query, "<_/>");
  }

  /** Test. */
  @Test public void gh1726() {
    final String query =
      "let $xml := if((1, 0)[.]) then ( " +
      "  element a { element b {} update {} } " +
      ") else ( " +
      "  error() " +
      ") " +
      "let $b := $xml/* " +
      "return ($b, $b/..)";

    query(query, "<b/>\n<a><b/></a>");
  }

  /** Test. */
  @Test public void gh1723() {
    check("count(<a/>)", 1, root(Itr.class));
    check("count(<a/>/<b/>)", 1, root(Itr.class));
    check("count(<a/>/<b/>/<c/>/<d/>)", 1, root(Itr.class));
  }

  /** Test. */
  @Test public void gh1733() {
    check("(<a/>, <b/>)/1", "1\n1", root(SingletonSeq.class));
    check("<a/>/1", 1, root(Itr.class));
    check("<a/>/<a/>", "<a/>", root(CElem.class));
    check("(<a/>/{ 1:2 })?1", 2, root(Itr.class));
    check("<a/>/self::a/count(.)", 1, root(Itr.class));
    check("<a/>/a/count(.)", "", root(DualMap.class));

    // no rewriting possible
    check("(<a/>, <b/>)/<c/>", "<c/>\n<c/>", root(MixedPath.class));
  }

  /** Test. */
  @Test public void gh1741() {
    check("<a/>/<b/>[1]", "<b/>", root(CElem.class));
    check("<a/>/.[1]", "<a/>", root(CElem.class));
    check("<doc><x/><y/></doc>/*/..[1] ! name()", "doc", empty(IntPos.class));

    check("<a/>/<b/>[2]", "", empty());
    check("<a/>/.[2]", "", empty());
    check("<doc><x/><y/></doc>/*/..[2] ! name()", "", empty());
  }

  /** Path tests. */
  @Test public void gh1752() {
    check("'1' ! json:parse(.)/descendant::*[text()] = 1", true, empty(IterMap.class));
  }

  /** Static typing of computed maps. */
  @Test public void gh1766() {
    check("let $map := { 'c': 'x' } return $map?(1 to 6)", "", empty());
    check("let $map := { 'c': 'x' } return count($map?(1 to 6))", 0, root(Itr.class));

    check("let $map := { 1: 'x' } return $map?(1 to 6)", "x",
        type(DualMap.class, "xs:string*"));
    check("let $map := { 1: 'x' } return count($map?(1 to 6))", 1,
        type(DualMap.class, "xs:string*"));
  }

  /** Rewrite boolean comparisons. */
  @Test public void gh1775() {
    check("(false(), true()) ! (.  = true())", "false\ntrue", root(BlnSeq.class));
    check("(false(), true()) ! (. != false())", "false\ntrue", root(BlnSeq.class));

    check("(false(), true())[" + _RANDOM_DOUBLE.args() + " < 2] ! (.  = false())",
        "true\nfalse", exists(NOT));
    check("(false(), true())[" + _RANDOM_DOUBLE.args() + " < 2] ! (. != true())",
        "true\nfalse", exists(NOT));

    check("(if(" + _RANDOM_DOUBLE.args() + " + 1) then true() else ()) = true()",
        true, root(BOOLEAN));
  }

  /** Merge predicates. */
  @Test public void gh1777() {
    check("(5, 6, 7)[. > 5][. < 7]", 6, count(CmpIR.class, 1));
    check("('a', 'b')[2][2]", "", empty());
    check("('a', 'b')[1][1]", "a", root(Str.class));

    check("for $n in <x a='1'/>/@* where $n >= 1 where $n <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
    check("for $n in <x a='1'/>/@* where data($n) >= 1 where data($n) <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
    check("for $n in <x a='1'/>/@* where $n/data() >= 1 where $n/data() <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
  }

  /** Merge conjunctions. */
  @Test public void gh1776() {
    check("for $n in (1, 2, 3) where $n != 2 and $n != 3 return $n", 1,
        exists(NOT), exists(RangeSeq.class));

    check("<_>A</_>[. = <a>B</a> and . = 'A']", "", exists(NOT), empty(And.class));
    check("<_>A</_>[. = <a>A</a> and . = 'A']", "<_>A</_>", empty(And.class));
    check("<_>A</_>[. =" + wrap("A") + "and . = 'A']", "<_>A</_>",
        exists(NOT), exists(List.class));
    check("(<a/>, <b/>, <c/>)[name() = 'a' and name() = 'b']", "",
        exists(NOT), exists(StrSeq.class));
  }

  /** Merge of consecutive operations. */
  @Test public void gh1778() {
    check("(1 to 3)[. = 1 or . != 2 or . = 3 or . != 4]", "1\n2\n3", count(BytSeq.class, 2));

    check("for $n in (<a/>, <b/>, <c/>) " +
        "where name($n) != 'a' where $n = '' where name($n) != 'b' " +
        "return $n", "<c/>", exists(NOT), exists(StrSeq.class));
    check("for $n in (<a/>, <b/>, <c/>) " +
        "where name($n) != 'a' and $n = '' and name($n) != 'b' " +
        "return $n", "<c/>", exists(NOT), exists(StrSeq.class));
  }

  /** EBV checks. */
  @Test public void gh1769ebv() {
    check("if((<a/>, <b/>)) then 1 else 2", 1, root(Itr.class));
    check("if(data(<a/>)) then 1 else 2", 2, exists(IterPath.class));
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769data() {
    check("data(" + wrap(1) + ") + 2", 3, empty(DATA));
    check("string-join(data(" + wrap("X") + "))", "X", empty(DATA));
    check("data(data(" + wrap("X") + "))", "X", count(DATA, 1));
    check("string(data(" + wrap("X") + "))", "X", empty(DATA));
    check("data(string(" + wrap("X") + "))", "X", empty(DATA));

    check("<x>A</x>[data() = 'A']", "<x>A</x>", empty(DATA), count(ContextValue.class, 1));
    check("<x>A</x>[data() ! data() ! data() = 'A']", "<x>A</x>",
        empty(DATA), count(ContextValue.class, 1));
    check("<x>A</x>[data() = 'A']", "<x>A</x>", empty(DATA));

    check("<A>A</A> ! data() = data(<A>B</A>)", false, empty(DATA));

    check("data(string(<_/>)) instance of xs:string", true, root(Bln.class));
    check("data(xs:NMTOKENS(<_/>) ! xs:untypedAtomic(.)) instance of xs:untypedAtomic*",
        true, root(Bln.class));
    error("data(data(" + wrap("a") + ") coerce to element(e))", INVTYPE_X);
    error("data(" + wrap("a") + "coerce to element(e))", INVTYPE_X);
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769string() {
    check("('a', 'b')[string() = 'a']", "a", empty(STRING));
    check("('a', 'b')[string(.) = 'b']", "b", empty(STRING));

    check("<x>A</x>[string() = 'A']", "<x>A</x>", empty(STRING));
    check("<x>A</x>[string(.) = 'A']", "<x>A</x>", empty(STRING));
    check("<A>A</A> ! string() = data(<A>B</A>)", false, empty(STRING));

    check("max(<_>1</_> ! string(@a))", "", root(STRING));
    check("max((<_>1</_>, <_>2</_>) ! string(@a))", "", root(MAX));
    check("min(<_ _='A'/>/@_ ! string())", "A", root(MIN));

    check("string(" + wrap(1) + "[.= 1]) =" + wrap(1), true, exists(STRING));
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769number() {
    check("(0e0, 1e0)[number() = 1]", 1, empty(NUMBER));
    check("(0e0, 1e0)[number(.) = 1]", 1, empty(NUMBER));
    check("(0e0, 1e0, 2e0, 3e0, 4e0, 5e0)[number() = 1]", 1, empty(NUMBER));
    check("(0e0, 1e0, 2e0, 3e0, 4e0, 5e0)[number(.) = 1]", 1, empty(NUMBER));

    check("<_>1</_>[xs:double(.) = 1]", "<_>1</_>", empty(Cast.class));
    check("<_>1</_>[number(.) = 1]", "<_>1</_>", exists(NUMBER));

    check("xs:double(" + wrap(1) + ") + 2", 3, empty(Cast.class));
    check("(1e0, 2e0) ! (xs:double(.) + 1)", "2\n3", empty(Cast.class));
    check("for $v in (1e0, 2e0, 3e0, 4e0, 5e0, 6e0) return xs:double($v) + 1",
        "2\n3\n4\n5\n6\n7", empty(Cast.class));

    check("for $n in (10000000000000000, 1)[. != 0] return number($n) = 10000000000000001",
        "false\nfalse", exists(NUMBER));
    check("for $n in (10000000000000000, 1)[. != 0] return number($n) = "
        + "xs:double(10000000000000001)", "true\nfalse", exists(NUMBER));
    check("for $n in (10000000000000000, 1)[. != 0] return xs:double($n) = 10000000000000001",
        "false\nfalse", exists(Cast.class));
    check("for $n in (10000000000000000, 1)[. != 0] return xs:double($n) = "
        + "xs:double(10000000000000001)", "true\nfalse", exists(Cast.class));

    check("number(<?_ 1?>) + number(<_>2</_>)", 3, count(NUMBER, 1));
    check("xs:double(<?_ 1?>) + xs:double(<_>2</_>)", 3, count(Cast.class, 1));
  }

  /** Inlining of where clauses. */
  @Test public void gh1782() {
    check("1 ! (for $a in (1, 2) where $a = last() return $a)", 1, exists(GFLWOR.class));
    check("(3, 4)[. != 0] ! (for $a in (1, 2) where . = $a return $a)", "", exists(GFLWOR.class));
  }

  /** Rewriting of positional tests that might yield an error. */
  @Test public void gh1783() {
    execute(new Close());
    error("(position() = 3) = (position() = 3)", NOCTX_X);
    error(". = .", NOCTX_X);
  }

  /** Pre-evaluate predicates in filter expressions. */
  @Test public void gh1785() {
    check("<a/>[self::node()]", "<a/>", root(CElem.class));
    check("<a/>[self::*]", "<a/>", root(CElem.class));
    check("<a/>[self::*[name(..) = 'a']]", "", root(IterFilter.class));
    check("<a/>[self::text()]", "", empty());
  }

  /** Better typing of functions with context arguments. */
  @Test public void gh1787() {
    check("path(<a/>) instance of xs:string", true, root(Bln.class));
    check("<a/>[path() instance of xs:string]", "<a/>", root(CElem.class));
    check("<a/>[name() = 'a']", "<a/>", exists(CmpSimpleG.class));

    check("() => void() => node-name()", "", root(VOID));
    check("() => void() => prefix-from-QName()", "", root(VOID));
  }

  /** Functions with database access. */
  @Test public void gh1788() {
    execute(new CreateDB(NAME, "<x>A</x>"));
    check(_DB_TEXT.args(NAME, "A") + "/parent::x", "<x>A</x>", exists(_DB_TEXT));
    check(_DB_TEXT.args(NAME, "A") + "/parent::unknown", "", empty());
  }

  /** Inline context in filter expressions. */
  @Test public void gh1792() {
    check("1[. = 0]", "", empty());
    check("1[. * .]", 1, root(Itr.class));
    check("2[. * . = 4]", 2, root(Itr.class));
    check("2[number()]", "", empty());
    check("1[count(.)]", 1, root(Itr.class));
    check("1[count(.)]", 1, root(Itr.class));
    check("1[.[.[.[.[.[.[.[.[. = 1]]]]]]]]]", 1, root(Itr.class));
    check("1[1[1[1[1[1[1[1[1[1 = .]]]]]]]]]", 1, root(Itr.class));

    check("''[string()]", "", empty());
    check("'a'[string()]", "a", root(Str.class));
    check("'a'[string(.)]", "a", root(Str.class));
    check("'a'[string-length() = 1]", "a", root(Str.class));
    check("'a'[string-length(.) = 1]", "a", root(Str.class));
    check("' '[normalize-space()]", "", empty());
    check("''[data()]", "", empty());

    check("<_/>[data()]", "", root(IterFilter.class));
    check("(1, 2)[. = 3]", "", root(IterFilter.class));
  }

  /** Type checks. */
  @Test public void gh1791() {
    inline(true);
    check("function() as xs:double { <x>1</x> }() + 2", 3, empty(TypeCheck.class));
    check("declare function local:_() as xs:string { <x>A</x> }; local:_() = 'A'",
        true, empty(TypeCheck.class));
    check("declare function local:f($s as xs:string) { tokenize($s) };\n" +
        "<x/> ! local:f(.)", "", empty(TypeCheck.class));
  }

  /** Unary expression. */
  @Test public void gh1796() {
    check("-xs:byte(-128) instance of xs:byte", false, empty(Unary.class), empty(Cast.class));
    check("for $i in -128 return --xs:byte($i) instance of xs:byte", false,
        empty(Unary.class), empty(Cast.class));
    check("let $i := -128 return --xs:byte($i) instance of xs:byte", false,
        empty(Unary.class), empty(Cast.class));

    check("--xs:byte(" + wrap("-128") + ")", -128, empty(Unary.class), count(Cast.class, 2));
  }

  /** Coerce to expression. */
  @Test public void gh1798() {
    check("<a>1</a> coerce to xs:string", 1, root(Str.class));
    check(wrap(1) + "coerce to xs:string", 1, root(TypeCheck.class));
    check("(1, 2) coerce to xs:double+", "1\n2",
        empty(TypeCheck.class), root(DblSeq.class), count(Dbl.class, 2));
    check("(-128, 127) coerce to xs:byte+", "-128\n127",
        empty(TypeCheck.class), root(ItemSeq.class), count(Itr.class, 2));

    error("<a/> coerce to empty-sequence()", INVTYPE_X);
    error("(1, 128) coerce to xs:byte+", INVTYPE_X);
  }

  /** Treat and coerce, error messages. */
  @Test public void gh1799() {
    error("'a' coerce to node()", INVTYPE_X);
    error("'a' treat as  node()", NOTREAT_X_X_X);
  }

  /** Merge of operations with fn:not. */
  @Test public void gh1805() {
    check("<_/>[not(. = ('A', 'B'))][not(. = ('C', 'D'))]", "<_/>", count(CmpHashG.class, 1));
    check("<_/>[. != 'A'][. != 'B'][. != 'C'][. != 'D']", "<_/>",   count(CmpHashG.class, 1));

    check("(3, 4)[not(. = 1) and not(. = (2, 4))]", 3, count(NOT, 1), count(CmpHashG.class, 1));
    check("(3, 4)[not(. = (2, 4)) and . != 1]", 3, count(NOT, 1), count(CmpHashG.class, 1));

    check("(3, 4)[not(. = (2, 3)) and not(. = (1, 4))]", "",
        count(NOT, 1), count(CmpHashG.class, 1));
  }

  /** Comparisons with empty strings. */
  @Test public void gh1803() {
    check("<a/>[namespace-uri() eq '']", "<a/>", exists(NOT), empty(STRING));
    check("<a/>[local-name() eq '']", "", exists(NOT), empty(STRING));
    check("attribute { 'a' } { '' }[local-name() = '']", "", exists(NOT), empty(STRING));
    check("let $x := (<a/>, <a/>) where $x[. eq ''] return $x", "<a/>\n<a/>",
        exists(EMPTY), exists(SingleIterPath.class));

    check("string(<_/>) != ''", false, exists(EXISTS), exists(IterPath.class));
    check("string(<_/>) = ''", true, exists(EMPTY), exists(IterPath.class));
    check("string(<_/>) <= ''", true, exists(EMPTY), exists(IterPath.class));
    check("string(<_/>) >= ''", true, root(Bln.class));
    check("string(<_/>) < ''", false, root(Bln.class));

    check("('', 'a')[string() != '']", "a", root(IterFilter.class), empty(CmpG.class));
    check("('', 'a')[string() = '']", "", root(IterFilter.class), exists(NOT));
  }

  /** Comparisons with simple map operands. */
  @Test public void gh1804() {
    // rewritings in comparisons
    check("<_>A</_> ! text() = 'A'", true, exists(IterPath.class));
    check("<_>A</_> ! text() = 'A'", true, exists(IterPath.class));
    check("let $a := <_>A</_> return $a ! text() = $a ! text()", true,
        root(Pipeline.class), empty(IterPath.class), empty(IterMap.class));

    // EBV rewritings
    check("<a><b/></a>[b ! ..]", "<a><b/></a>", exists(CachedPath.class));

    // absolute paths
    check("text { 'a' } ! <x>{ . }</x>/text() = 'a'", true, exists(Pipeline.class));
    check("string(<a>a</a>) ! <x>{ . }</x>/text() = 'a'", true, empty(DualIterMap.class));
    check("<a>a</a>/string() ! <x>{ . }</x>/text() = 'a'", true, empty(DualIterMap.class));
  }

  /** Rewrite if to where. */
  @Test public void gh1806() {
    check("let $a := <_/> return if ($a = '') then $a else ()", "<_/>",
        empty(If.class), empty(Where.class), exists(IterFilter.class));
    check("for $a in (1, 2) return if ($a = 3) then $a else ()", "",
        empty(If.class), empty(Where.class), root(IterFilter.class));

    check("for $t in ('a', 'b') return if($t) then $t else ()", "a\nb",
        root(IterFilter.class), exists(ContextValue.class));
    check("for $t in ('a', 'b') return $t[$t]", "a\nb",
        root(IterFilter.class), exists(ContextValue.class));

    check("for $i in 1 to 2\n" +
        "for $f in if ($i = 1) then 1 else ()\n" +
        "return $f + $i\n", 2, empty(GFLWOR.class));
  }

  /** If expression with empty branches. */
  @Test public void gh1809() {
    check("if(" + wrap(1) + "[. = 1]) then () else ()", "", empty());
    check("if(void(1)) then () else ()", "", root(VOID), count(VOID, 1));
  }

  /** EBV simplifications: if, switch, typeswitch. */
  @Test public void gh1813() {
    // if expression
    check("(1, 2) ! boolean(if(.) then 'a' else <a/>)", "true\ntrue", root(SingletonSeq.class));
    check("(1 to 2) ! boolean(if(.) then '' else ())", "false\nfalse", root(SingletonSeq.class));
    check("boolean(if(" + _RANDOM_DOUBLE.args() + ") then '' else 0)", "false",
        root(List.class), exists(VOID));

    check("(1, 2)[if(.) then 0.0e0 else 0.0]", "", empty());
    check("(1, 2)[if(.) then '' else xs:anyURI('')]", "", empty());

    // no rewriting of numbers > 0
    check("(1, 2)[. != 0] ! boolean(if(.) then 'a' else 1)", "true\ntrue", exists(If.class));

    // switch expression
    check("for $i in (1 to 3)\n" +
        "return if(switch($i)\n" +
        "  case 1 return 0\n" +
        "  case 2 return ''\n" +
        "  default return ()\n" +
        ") then 'fail' else ''", "\n\n", root(SingletonSeq.class));

    // typeswitch expression
    check("for $i in ('a', 1)\n" +
        "return if(typeswitch($i)\n" +
        "  case xs:integer return 0\n" +
        "  case xs:string  return ''\n" +
        "  default return ()\n" +
        ") then 'fail' else ''", "\n", root(SingletonSeq.class));
  }

  /** Simple map, index rewritings. */
  @Test public void gh1814() {
    execute(new CreateDB(NAME, "<x><y><z>A</z></y></x>"));
    check("exists(x[y/z = 'A'])", true, exists(ValueAccess.class));
    check("exists(x[y ! z = 'A'])", true, exists(ValueAccess.class));
  }

  /** Typing: data references. */
  @Test public void gh1816() {
    execute(new CreateDB(NAME, "<x><y/></x>"));
    check("x/z", "", empty());
    check("(x, y)/z", "", empty());
    check("(x | y)/z", "", empty());
    check("(if(" + _RANDOM_DOUBLE.args() + ") then x else y)/z", "", empty());
    check("(let $_ := " + _RANDOM_DOUBLE.args() + " return x)/z", "", empty());
    check("((# bla #) { x })/z", "", empty());
    check("(switch (" + _RANDOM_DOUBLE.args() + ")\n" +
      "  case 1 return ()\n" +
      "  default return x\n" +
      ")/z", "", empty());

    check("(x, void(()))/z", "", empty());
    check("(x | void(()))/z", "", empty());
    check("(if(" + _RANDOM_DOUBLE.args() + ") then x)/z", "", empty());
  }

  /** FLWOR, no results, nondeterministic expressions. */
  @Test public void gh1819() {
    check("for $_ in () return <x/>", "", empty());
    check("for $_ in void(1) return 1", "", root(VOID));
    check("let $_ := 1 return <x/>", "<x/>", root(CElem.class));
    check("let $_ := void(1) return 1", 1, root(List.class), exists(VOID));
    check("for $_ in 1 to 2 return 3", "3\n3", root(SingletonSeq.class));
    check("for $_ in 1 to 2 return ()", "", empty());

    check("let $_ := void(1) return 1", 1, root(List.class), exists(VOID));

    // rewrite to simple map
    check("for $_ in 1 to 2 return <a/>", "<a/>\n<a/>", root(REPLICATE));
    check("for $_ in 1 to 2 return void(1)", "", root(REPLICATE));
  }

  /** Merge and/or expressions. */
  @Test public void gh1820() {
    // OR: merge
    check("(<_/>, <_/>) = 'a' or (<_/>, <_/>) = 'b'", false, empty(Or.class));
    check("(<_/>, <_/>) = 'a' or (<_/>, <_/>) = ('b', 'c')", false, empty(Or.class));
    check("<_>1</_>[. = 1 or . = 2]", "<_>1</_>", empty(Or.class), exists(CmpIR.class));
    check("<_>1</_>[not(. = 1) or . != (2, 3)]", "<_>1</_>", empty(Or.class));
    check("exists(let $x := <a><b>c</b><b>d</b></a> return $x[b = 'c' or b = 'd'])", true,
        count(CmpHashG.class, 1));

    // OR: no merge
    check("<_>1</_>[not(. = 1) or not(. = (2, 3))]", "<_>1</_>", exists(Or.class));
    check("<_>1</_>[. = 1 or not(. != (2, 3))]", "<_>1</_>", exists(Or.class));
    check("<_>1</_>[not(. = 1) or . = (2, 3)]", "", exists(Or.class));
    check("<_>1</_>[. = 1 or not(. = (2, 3))]", "<_>1</_>", exists(Or.class));

    check("<_>a</_>[. = 'a' or . != 'b']", "<_>a</_>", exists(Or.class));
    check("<_>a</_>[. = 'a' or .. != 'b']", "<_>a</_>", exists(Or.class));

    // AND: merge
    check("<_>1</_>[not(. = 1) and not(. = 2)]", "",
        exists(CmpIR.class), empty(CmpSimpleG.class));
    check("not((<_/>, <_/>) != 'a') and not((<_/>, <_/>) != 'b')", false,
        exists(CmpG.class), empty(CmpSimpleG.class));

    // AND: no merge
    check("(<_/>, <_/>) = 'a' and (<_/>, <_/>) = ('b', 'c')", false, exists(And.class));

    check("exists(let $x := <a><b>c</b><b>d</b></a> return $x[b = 'c' and b = 'd'])", true,
        count(CmpG.class, 2));

    check("<_>1</_>[. = 1 and . = 2]", "",
        count(CmpG.class, 1));
    check("<_>1</_>[not(. = 1) and . = 2]", "",
        count(CmpSimpleG.class, 2));
    check("<_>1</_>[. = 1 and not(. = 2)]", "<_>1</_>",
        count(CmpSimpleG.class, 2));

    check("(<_/>, <_/>) = '' and (<_/>, <_/>) = 'a'", false, exists(And.class));
  }

  /** Map/array lookups: better typing. */
  @Test public void gh1825() {
    check("[ 1 ](" + wrap(1) + ") instance of xs:integer", true, root(Bln.class));

    check("{ 'a': 2 }(" + wrap("a") + ") instance of xs:integer?", true, root(Bln.class));
    check("{ 1: 2 }(<_/>) instance of xs:integer?", true, root(Bln.class));
    check("{ 1: (2, 'a') }(<_/>) instance of xs:anyAtomicType*", true, root(Bln.class));
  }

  /** Rewriting of positional predicate. */
  @Test public void gh1827() {
    inline(true);
    check("declare function local:f($pos) { (1, 2)[position() < $pos] };\n" +
        "local:f(1)", "", empty());
  }

  /** Merge fn:empty and fn:exist functions. */
  @Test public void gh1829() {
    check("exists(<a/>/a) or exists(<b/>/b)",
        false, root(EXISTS), empty(Or.class), empty(BOOLEAN), exists(Union.class));
    check("for $i in (1 to 2)[. != 0] return exists($i[. = 1]) or exists($i[. = 2])",
        "true\ntrue", empty(Or.class), empty(EXISTS), root(DualMap.class));

    check("<a/>/a or <b/>/b", false, root(EXISTS), empty(Or.class), exists(Union.class));
    check("<a/>[a or b]", "", empty(Or.class), count(SingleIterPath.class, 1));

    check("<a/>[empty(b)][empty(c)]", "<a/>", count(EMPTY, 1), count(SingleIterPath.class, 1));
    check("<a/>[empty((b, c))]", "<a/>", count(SingleIterPath.class, 1));
    check("for $a in (<b/>, <c/>) return <a/>[empty(($a[. = 'i'], $a[. = 'j']))]", "<a/>\n<a/>",
        root(DualMap.class), exists(NOT), empty(If.class));

    check("for $a in (1 to 6)[. != 0] return <a/>[exists(($a[. = 1], $a[. = 1]))]",
        "<a/>", count(CmpSimpleG.class, 2), empty(EXISTS));
    check("exists(string-to-codepoints(<?_ x?>))",
        true, empty(STRING_TO_CODEPOINTS), empty(EXISTS), exists(BOOLEAN), exists(STRING));
    check("empty(string-to-codepoints(<?_ x?>))",
        false, empty(STRING_TO_CODEPOINTS), empty(EMPTY), exists(NOT), exists(STRING));
    check("exists(" + CHARACTERS.args(" <?_ x?> ") + ')',
        true, empty(CHARACTERS), empty(EXISTS), exists(BOOLEAN), exists(STRING));
    check("empty(" + CHARACTERS.args(" <?_ x?> ") + ')',
        false, empty(CHARACTERS), empty(EMPTY), exists(NOT), exists(STRING));

    check("exists(map:keys(map:entry(1, <_/>)))",
        true, root(Bln.class));
    check("exists(map:items(map:entry(1, <_/>)))",
        true, root(Bln.class));
    check("empty(map:keys(map:entry(1, <_/>)))",
        false, root(Bln.class));
    check("empty(map:items(map:entry(1, <_/>)))",
        false, root(Bln.class));

    // no rewritings
    check("exists(<a/>/a) and exists(<b/>/b)", false, exists(And.class), empty(EXISTS));
    check("for $i in (1 to 2)[. != 0] return exists($i[. = 1]) and exists($i[. = 2])",
        "false\nfalse", empty(And.class), empty(EXISTS), root(DualMap.class));
    check("<a/>/a and <b/>/b", false, exists(And.class));
    check("<a/>[a and b]", "", count(SingleIterPath.class, 2));

    check("<a/>[empty(b) or empty(c)]", "<a/>", exists(Or.class));

    final String e1 = " tokenize(" + wrap("a b") + ')', e2 = " tokenize(" + wrap("") + ')';
    check(EMPTY.args(e1)  + " and " + EMPTY.args(e2),  false, root(EMPTY));
    check(EXISTS.args(e1) + " or "  + EXISTS.args(e2), true,  root(EXISTS));
    check(EMPTY.args(e1)  + " or "  + EMPTY.args(e2),  true,  root(Or.class));
    check(EXISTS.args(e1) + " and " + EXISTS.args(e2), false, root(And.class));
    query("for $j in 6 to 11 "
        + "let $i := 1 "
        + "let $a := $i[. >= 2] "
        + "let $b := $j[. >= 11] "
        + "where exists($a) and exists($b) "
        + "return count($a)", "");
  }

  /** Documents with different default namespaces. */
  @Test public void gh1831() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<x xmlns='x'/>"));
    execute(new Add("b.xml", "<x/>"));
    query("x", "<x/>");
  }

  /** Equality tests on QNames. */
  @Test public void gh1823() {
    query("declare namespace p = 'U';\n" +
      "prefix-from-QName(QName('U', 'a')[. = xs:QName(" + wrap("p:a") + ")]) or\n" +
      "prefix-from-QName(QName('U', 'p:a')[. = xs:QName(" + wrap("p:a") + ")])",
      true);
    query("let $f := function($a) { string($a) }\n" +
      "return distinct-values(($f(QName('U', 'l')), $f(QName('U', 'p:l'))))",
      "l\np:l");
  }

  /** Static typing, maps. */
  @Test public void gh1834() {
    query("declare function local:f() as map(xs:string, xs:string) {" +
      "  map:entry(<e/> ! string(), '')" +
      "}; local:f()?*", "");
    query("declare function local:f() as map(xs:string, xs:string) {" +
      "  map:put({}, <e/> ! string(), '')" +
      "}; local:f()?*", "");
  }

  /** Set Expressions: Merge operands. */
  @Test public void gh1838() {
    check("<_><a/></_>/(* union *[b])", "<a/>",
        empty(Union.class), empty(SingleIterPath.class), count(IterStep.class, 1));
    check("<_><a/></_>/(* intersect *[b])", "",
        empty(Intersect.class), count(SingleIterPath.class, 1), count(IterStep.class, 2));
    check("<_><a/></_>/(* except *[b])", "<a/>",
        empty(Except.class), exists(EMPTY), count(SingleIterPath.class, 1));

    check("<_><a/></_>/*/(.[self::a] union .[self::b])", "<a/>",
        empty(Union.class));

    check("<_><a/></_>/(.[b][c] union .[d])", "",
        empty(Union.class), exists(And.class), exists(Or.class));
    check("<_><a/></_>/(.[b][c] intersect .[d])", "",
        empty(Intersect.class), empty(And.class));
    check("<_><a/></_>/(.[b][c] except .[d])", "",
        empty(Except.class), exists(EMPTY), empty(And.class));

    check("<_><a/></_>/(a union self::a)", "<a/>", empty(Union.class));

    // no optimization
    check("<_><a/></_>/(a union a/*[b])", "<a/>", exists(Union.class));
    check("<_><a/></_>/(a union a/<b/>)", "<a/>\n<b/>", exists(Union.class));
    check("<_><a/></_>/(a[1] union a[2])", "<a/>", exists(Union.class));
    check("<_/>/(<b/>[*] union <c/>[*])", "", exists(Union.class));
  }

  /** Logical expressions, DNFs/CNFs. */
  @Test public void gh1839() {
    // no rewriting
    check(gh1839("$a or $b"), "fttt",
        count(And.class, 0), count(Or.class, 1));
    // no rewriting
    check(gh1839("$a and $b"), "ffft",
        count(And.class, 1), count(Or.class, 0));

    // optimized: $a
    check(gh1839("$a or ($a and $b)"), "fftt",
        count(And.class, 0), count(Or.class, 0));
    // optimized: $a
    check(gh1839("$a and ($a or $b)"), "fftt",
        count(And.class, 0), count(Or.class, 0));

    // optimized: $a and ($b or $c)
    check(gh1839("($a and $b) or ($a and $c)"), "fffffttt",
        count(And.class, 1), count(Or.class, 1));
    // optimized: $a or ($b and $c)
    check(gh1839("($a or $b) and ($a or $c)"), "fffttttt",
        count(And.class, 1), count(Or.class, 1));

    // optimized: $a or ($b and ($c or $d))
    check(gh1839("($a or $b) and ($a or $c or $d)"), "fffffttttttttttt",
        count(And.class, 1), count(Or.class, 2));
    // optimized: $a and ($b or ($c and $d))
    check(gh1839("($a and $b) or ($a and $c and $d)"), "fffffffffffttttt",
        count(And.class, 2), count(Or.class, 1));

    // optimized: $a and $b
    check(gh1839("($a and $b) or ($a and $b and $c)"), "fffffftt",
        count(And.class, 1), count(Or.class, 0));
    // optimized: $a or $b
    check(gh1839("($a or $b) and ($a or $b or $c)"), "fftttttt",
        count(And.class, 0), count(Or.class, 1));
  }

  /**
   * Creates a query that concatenates booleans in a string.
   * @param query query string
   * @return query
   */
  private static String gh1839(final String query) {
    // extract variable names from query
    final StringList vars = new StringList();
    for(final String var : query.split("\\$")) {
      if(var.isEmpty()) continue;
      final char ch = var.charAt(0);
      if(Character.isLetter(ch)) vars.addUnique(String.valueOf(ch));
    }

    // generate query string with FLWOR expression
    final StringBuilder sb = new StringBuilder().append("string-join(\n");
    for(final String var : vars) {
      sb.append("  for $").append(var).append(" in (0, 1) !");
      sb.append(wrapContext()).append("! xs:boolean(.)\n");
    }
    sb.append("  return if(").append(query).append(") then 't' else 'f'\n");
    return sb.append(")").toString();
  }

    /** Combine position predicates. */
  @Test public void gh1840() {
    check("(1, 2, 3)[position() = 1 or position() = 1]", 1, root(Itr.class));
    check("(1, 2, 3)[position() = 1 or position() = 2]", "1\n2", root(RangeSeq.class));
    check("(1, 2, 3)[position() = 1 or position() = 3]", "1\n3", count(IntPos.class, 2));

    check("(1, 2, 3)[position() = 1 to 2 or position() = 1]", "1\n2", root(RangeSeq.class));
    check("(1, 2, 3)[position() = 1 to 2 or position() = 2]", "1\n2", root(RangeSeq.class));
    check("(1, 2, 3)[position() = 1 to 2 or position() = 3]", "1\n2\n3", root(RangeSeq.class));

    check("(1, 2, 3)[position() = 1 to 2 and position() = 1]", 1, root(Itr.class));
    check("(1, 2, 3)[position() = 1 to 2 and position() = 2 to 3]", 2, root(Itr.class));
    check("(1, 2, 3)[position() = 1 to 2 and position() = 3]", "", empty());
  }

  /** Distinct integer sequences. */
  @Test public void gh1841() {
    check("<_/>[position() = (1, 1)]", "<_/>", root(CElem.class));
    check("<_/>[position() = (1, 2, 1)]", "<_/>", root(CElem.class));
    check("<_/>[position() = (1, 2, 2.1)]", "<_/>", root(CElem.class));
    check("<_/>[position() = (3, 2, 2.1, 1)]", "<_/>", root(CElem.class));
  }

  /** Flatten expression lists. */
  @Test public void gh1842() {
    check("(<a/>, (<b/>, <c/>))", "<a/>\n<b/>\n<c/>", count(List.class, 1));

    check("count(<a/> union (<b/> union <c/>))", 3, count(Union.class, 1));
    check("count(<a/> intersect (<b/> intersect <c/>))", 0, root(Itr.class));
    check("count(<a/> except (<b/> except <c/>))", 1, root(Itr.class));

    check("<a/> ! (. > '0' or (. < '1' or . < '2'))", true, count(Or.class, 1));
    check("<a/> ! (. = '0' and (. < '1' and . != '2'))", false, count(And.class, 1));

    check("(<_/>[. != 'a'])[. != 'b']\n", "<_/>", count(IterFilter.class, 1), exists(NOT));
  }

  /** Merge simple map and filter expressions. */
  @Test public void gh1843() {
    check("(1, 2) ! .[. = 1]", 1, root(IterFilter.class));
    check("('a', 'b') ! .[. = 'b']", "b", root(IterFilter.class));
    check("(1, 2)[. = 1] ! .[. = 1]", 1, root(IterFilter.class), count(CmpSimpleG.class, 1));
  }

  /** Rewrite filters to simple maps. */
  @Test public void gh1845() {
    check("boolean(<a>A</a>[contains(., 'A')])", true, empty(IterFilter.class));
    check("for $n in (<a/>, <b/>) return if($n[name() = 'a']) then 1 else 2", "1\n2",
        empty(IterFilter.class));
    check("boolean(<a/>[data()][. = 'A'])", false, empty(BOOLEAN));
  }

  /** Name tests in where clauses, index rewritings. */
  @Test public void gh1851() {
    check("for $c in 1 to 10 return $c[. = 1]", 1, root(IterFilter.class));

    // move filters with positional tests and variable references into simple map
    check("for $c in 1 to 10 return $c[.]", 1, root(DualMap.class));
    check("for $c in 1 to 10 return $c[position() = (0, 2)]", "", empty());

    // no rewriting: position checks are replaced by items-at
    check("for $c in 1 to 10 return $c[position() =" + wrap(2) + "]", "", root(DualMap.class));
    check("for $c in 1 to 10 return $c[$c]", 1, root(DualMap.class));
  }

  /** Set expressions, DNFs/CNFs. */
  @Test public void gh1852() {
    // no rewriting
    check(gh1852("$a() union $b()"),
        "fttt", count(Intersect.class, 0), count(Union.class, 1));
    // no rewriting
    check(gh1852("$a() intersect $b()"),
        "ffft", count(Intersect.class, 1), count(Union.class, 0));

    // optimized: $a()
    check(gh1852("$a() union ($a() intersect $b())"),
        "fftt", count(Intersect.class, 0), count(Union.class, 0));
    // optimized: $a()
    check(gh1852("$a() intersect ($a() union $b())"),
        "fftt", count(Intersect.class, 0), count(Union.class, 0));

    // optimized: $a() intersect ($b() union $c)
    check(gh1852("($a() intersect $b()) union ($a() intersect $c())"),
        "fffffttt", count(Intersect.class, 1), count(Union.class, 1));
    // optimized: $a() union ($b() intersect $c())
    check(gh1852("($a() union $b()) intersect ($a() union $c())"),
        "fffttttt", count(Intersect.class, 1), count(Union.class, 1));

    // optimized: $a() union ($b() intersect ($c() union $d))
    check(gh1852("($a() union $b()) intersect ($a() union $c() union $d())"),
        "fffffttttttttttt", count(Intersect.class, 1), count(Union.class, 2));
    // optimized: $a() intersect ($b() union ($c() intersect $d()))
    check(gh1852("($a() intersect $b()) union ($a() intersect $c() intersect $d())"),
        "fffffffffffttttt", count(Intersect.class, 2), count(Union.class, 1));

    // optimized: $a() intersect $b()
    check(gh1852("($a() intersect $b()) union ($a() intersect $b() intersect $c())"),
        "fffffftt", count(Intersect.class, 1), count(Union.class, 0));
    // optimized: $a() union $b()
    check(gh1852("($a() union $b()) intersect ($a() union $b() union $c())"),
        "fftttttt", count(Intersect.class, 0), count(Union.class, 1));
  }

  /**
   * Creates a query that concatenates booleans in a string.
   * @param query query string
   * @return query
   */
  private static String gh1852(final String query) {
    // extract variable names from query
    final StringList vars = new StringList();
    for(final String var : query.split("\\$")) {
      if(var.isEmpty()) continue;
      final char ch = var.charAt(0);
      if(Character.isLetter(ch)) vars.addUnique(String.valueOf(ch));
    }

    // generate query string with FLWOR expression
    final StringBuilder sb = new StringBuilder();
    sb.append("let $n := <n/>\n");
    sb.append("return string-join(\n");
    for(final String var : vars) {
      sb.append("  for $").append(var).append(" in (function() {}, function() { $n })\n");
    }
    sb.append("  return if(").append(query).append(") then 't' else 'f' \n");
    return sb.append(")").toString();
  }

  /** Name tests in where clauses, index rewritings. */
  @Test public void gh1853() {
    execute(new CreateDB(NAME, "<a><b/></a>"));
    check("for $e in //* where name($e) = 'b' return $e", "<b/>", empty(Function.NAME));
    check("for $e in //* where local-name($e) = 'e' return $e", "", empty());
  }

  /** Nested predicates in FLWOR expression. */
  @Test public void gh1855() {
    execute(new CreateDB(NAME, "<a><b c='d'>e</b></a>"));
    check("count(let $e := 'e' return a[b[@c = 'd'] = $e])", 1,
        exists(ValueAccess.class));
    check("count(let $e := 'e' let $f := a[b[@c = 'd'] = $e] return $f)", 1,
        exists(ValueAccess.class));
  }

  /** Reoptimize newly created fn:boolean instances. */
  @Test public void gh1856() {
    check("if(<a/>/text()) then true() else false()", false, root(EXISTS));
  }

  /** Enforce index pragma, full-text. */
  @Test public void gh1860() {
    query(_DB_CREATE.args(NAME, "<_>a</_>", "_.xml", " { 'ftindex': true() }"));
    query("(# db:enforceindex #) {\n" +
      "  let $t := 'a'\n" +
      "  for $db in '" + NAME + "'\n" +
      "  return" + _DB_GET.args(" $db") + "/_[text() contains text { $t }]\n" +
      "}",
      "<_>a</_>");
  }

  /** Logical expressions, tertium non datur. */
  @Test public void gh1863() {
    check("(true(), false()) ! (. and not(.))", "false\nfalse", root(SingletonSeq.class));
    check("(true(), false()) ! (. or  not(.))", "true\ntrue",   root(SingletonSeq.class));
    check("(true(), false()) ! (not(.) and .)", "false\nfalse", root(SingletonSeq.class));
    check("(true(), false()) ! (not(.) or  .)", "true\ntrue",   root(SingletonSeq.class));

    check("(true(), false()) ! (. = false() and . = true())", "false\nfalse",
        root(SingletonSeq.class));
    check("(true(), false()) ! (. = true() or . = false())", "true\ntrue",
        root(SingletonSeq.class));

    check("for $i in 1 to 2 return $i and $i = 1 and not($i)", "false\nfalse",
        root(SingletonSeq.class));

    check("<a/>[text() = 'a']['ignored'][not(text() = 'a')]", "", empty());
    check("<a/>[not(text() = 'a')]['ignored'][text() = 'a']", "", empty());
  }

  /** Inline paths in FLWOR expressions. */
  @Test public void gh1865() {
    execute(new CreateDB(NAME, "<a><b c='d'><c/></b><c/></a>"));

    // child, attribute, self axis: merge steps
    check("for $a in /* return $a/c", "<c/>", empty(GFLWOR.class));
    check("for $n in //* return $n/c", "<c/>\n<c/>", empty(GFLWOR.class));
    check("for $n in //(a, b, c) return $n/@*", "c=\"d\"", empty(GFLWOR.class));
    check("for $n in //(a, b, c) return $n/self::d", "", empty(GFLWOR.class));

    // other axes: rewrite to simple map operator
    check("count(for $c in //c return $c/..)", 2, exists(DualMap.class));
    check("count(for $c in //* return $c//c)", 3, exists(DualIterMap.class));
    check("count(for $c in //* return $c/following::*)", 2, exists(DualIterMap.class));
    check("let $n := reverse((<a>A</a>, <b>B</b>)) return $n/text()", "B\nA",
        root(CachedPath.class));
  }

  /** FLWOR: Return clause, filter expression. */
  @Test public void gh1867() {
    check("for $a at $p in (1, 2) return $a[. = $p]", "1\n2", root(GFLWOR.class));
  }

  /** Distinct-values, optimization of arguments. */
  @Test public void gh1868() {
    check("let $s as xs:string* := distinct-values(" + wrap("x") + "! string()) return $s",
        "x", root(STRING));
  }

  /** Simple map, positional predicates. */
  @Test public void gh1874() {
    check("(1 to 10) ! .[.]", 1);
  }

  /** EBV Rewritings: count(expr), numeric checks. */
  @Test public void gh1875() {
    check("boolean(count(" + wrap("A") + "[. >= 'A']))", true, empty(COUNT));
    check("boolean(count(" + wrap("A") + "[. >= 'A']) != 0)", true, empty(COUNT));
  }

  /** Switch Expression: Rewrite to 'if' expression. */
  @Test public void gh1877() {
    check("switch(<_/>) case '' return 1 default return 2", 1, root(If.class));
    check("switch(<_/>) case '' case 'x' return 1 default return 2", 1, root(If.class));
  }

  /** Singleton sequences in predicates. */
  @Test public void gh1878() {
    query("<a/>[(1 to 2) ! 1]", "<a/>");
    query("<a/>[" + REPLICATE.args(1, 2) + ']', "<a/>");
  }

  /** Lacking filter rewriting. */
  @Test public void gh1879() {
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", root(CElem.class));
    check("let $a := <a/> return $a[./self::a]", "<a/>", root(CElem.class));
    check("let $a := <a/> return $a[self::a]", "<a/>", root(CElem.class));
  }

  /** Single let, inline where clause. */
  @Test public void gh1880() {
    check("let $a := <a/> where $a return $a", "<a/>",
        empty(GFLWOR.class), root(CElem.class));
    check("let $a := <a/> where $a/self::a return $a", "<a/>",
        empty(GFLWOR.class), root(CElem.class));
    check("let $a := <a/> where $a/self::a return $a[. = '']", "<a/>",
        empty(GFLWOR.class), count(IterFilter.class, 1));
    check("let $a := <a/> where $a[. = ''] return $a/self::a", "<a/>",
        empty(GFLWOR.class), root(IterFilter.class));
    check("let $a as element(a) := <a/> where $a return $a", "<a/>",
        root(CElem.class));

    // skip rewritings: let
    check("let $e := (<a/>, <b/>) where $e/self::a return $e", "<a/>\n<b/>", root(GFLWOR.class));
    check("let score $s := <a/> where $s return $s", "", root(GFLWOR.class));
  }

  /** Typeswitch: default branch with variable. */
  @Test public void gh1883() {
    query(
      "typeswitch(<x/> update { delete node x }) " +
      " case $i as xs:int return '-' " +
      " default $i return $i", "<x/>");
  }

  /** Simplify if expressions and comparisons. */
  @Test public void gh1885() {
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'S')", "false\ntrue", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') != 'S')", "true\nfalse", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'P')", "true\nfalse", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') != 'P')", "false\ntrue", empty(If.class));

    check("(0, 1) ! ((if(.) then <_/> else ()) = <_/>)", "false\ntrue", empty(If.class));
    check("(0, 1) ! ((if(.) then (1, 2) else ()) = (1, 2))", "false\ntrue", empty(If.class));
    check("(if((1, 2)[. = 0]) then () else (1, 2)) = (1, 2)", true, empty(If.class), root(NOT));

    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'X')", "false\nfalse", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'X')", "false\nfalse", empty(If.class));
  }

  /** Simple map implementation for two operands. */
  @Test public void gh1889() {
    check("(1 to 2) ! <a/>", "<a/>\n<a/>", root(REPLICATE));
    check("(3 to 13) ! void(.)", "", root(DualMap.class));
  }

  /** Rewrite FLWOR to simple map. */
  @Test public void gh1890() {
    check("for $i in (1 to 2)[. >= 0] return ($i + $i)", "2\n4", root(DualMap.class));
    check("for $a in (1 to 2)[. >= 0] for $b in (1 to 2) return ($a * 2)", "2\n2\n4\n4",
        root(DualIterMap.class), exists(REPLICATE));
  }

  /** Inlining in update expression. */
  @Test public void gh1891() {
    query("(<a/>, <b/>) update { rename node . as name() }", "<a/>\n<b/>");
    query("(<a/>, <b/>) ! . update { rename node . as name() }", "<a/>\n<b/>");
    query("(<a/>, <b/>) ! (. update { rename node . as name() })", "<a/>\n<b/>");
    query("for $n in (<a/>, <b/>) return $n ! . update { rename node . as name() }", "<a/>\n<b/>");
    query("'s' ! (<a/> update { delete node . })", "<a/>");
  }

  /** Static properties of closures. */
  @Test public void gh1892() {
    inline(true);
    check("1 ! (let $a := . return function() { $a }) ! .()", 1, root(Itr.class));
    query("function() { for $a in (1, 2) return function() { $a } }() ! .()", "1\n2");
  }

  /** FLWOR, positional variable. */
  @Test public void gh1893() {
    check("for $a at $p in 6 to 8 return $p", "1\n2\n3", root(RangeSeq.class));
    check("for $a at $p in ('a', 'b') return $p", "1\n2", root(RangeSeq.class));
  }

  /** Inline variables into simple map. */
  @Test public void gh1895() {
    check("let $a := (<a/>, <b/>) return $a ! name()", "a\nb", root(DualMap.class));
    check("let $doc := <e a=''/> return $doc/@a ! node-name()", "#a", root(NODE_NAME));
  }

  /** Simple maps, inline operands. */
  @Test public void gh1897() {
    inline(true);
    check("<a/> ! name()", "a", root(Function.NAME));
    check("'s' ! <_>{ . }</_>", "<_>s</_>", root(CElem.class));
    check("count#1 ! .('a')", 1, root(Itr.class));

    // do not generate nested node constructors
    check("namespace-uri(<a><b/></a>) ! <x xmlns='x'>{ . }</x> ! name()",
        "x", root(Pipeline.class));
  }

  /** Rewritings of positional tests. */
  @Test public void gh1898() {
    // argument of items-at can be a sequence of numbers
    check("for $a in (1, 2) return $a[$a[.]]", 1, root(DualMap.class));
    check("for $a in (1, 2) return $a[$a[.]]", 1, root(DualMap.class));
    check("for $a in (2, 3) return $a[$a[.]]", "", root(DualMap.class));
  }

  /** Inline filter expressions. */
  @Test public void gh1899() {
    check("for $a in (1, 'a') let $b := $a[.] return $a[$b]", "1\na",
        empty(Let.class), exists(HoistedFilter.class));
    check("function($a, $b) { $a ! element s { .[$b] } }(<a/>, ())", "<s/>",
        exists(HoistedFilter.class));
  }

  /** Inline cast expressions. */
  @Test public void gh1901() {
    check("<a id='x'/>/@id ! xs:string(.)", "x", root(Cast.class));
    check("<_/>/@id ! data()", "", root(DATA));
    check("<_/>[data()] ! base-uri()", "", root(BASE_URI));
    check("<_/>[not(data())] ! base-uri()", "", root(BASE_URI));
  }

  /** Size of type check results. */
  @Test public void gh1902() {
    inline(true);
    check("function() as xs:string+ {" + REPLICATE.args(" <x/>", 10000000000L) + " }() "
        + "=> count()",
      "10000000000", root(Itr.class));
    check("function() as xs:double+ { 1 to 100000000000000 }() => count()",
      "100000000000000", root(Itr.class));
  }

  /** Inlining of simple map operands. */
  @Test public void gh1905() {
    check("<a>b</a>/self::a ! string() ! element xml { . }", "<xml>b</xml>");
    check("<a>b</a>/self::a ! string() ! string() ! element xml { . }", "<xml>b</xml>");
    check("<a>b</a>/self::a ! string() ! string() ! string() ! element xml { . }", "<xml>b</xml>");
  }

  /** Type check, refine occurrence indicator. */
  @Test public void gh1906() {
    inline(true);
    check("function() as xs:string* { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string+ { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string? { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string  { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string ", true, root(Bln.class));
  }

  /** Atomization of group by expressions. */
  @Test public void gh1907() {
    query("let $i := 1 group by $n := ([], [ $i ]) return $n", 1);
    query("let $i := 1 group by $n := ([], [ $i ]) return $i", 1);
    query("let $i := 1 group by $n := ([], [ 1 ]) return $i", 1);
    query("let $i := 1 group by $n := ([], [ ]) return $i", 1);
    query("let $i := 1 group by $n := () return $i", 1);
  }

  /** Node constructors, better typing. */
  @Test public void gh1908() {
    check("<a/> treat as element(a)", "<a/>", root(CElem.class));
    check("attribute a {} treat as attribute(a)", "a=\"\"", root(CAttr.class));
    check("<?a?> treat as processing-instruction(a)", "<?a?>", root(CPI.class));
    check("<xml:a/> treat as element(xml:a)", "<xml:a/>", root(CElem.class));
    check("element Q{_}a {} treat as element(Q{_}a)", "<a xmlns=\"_\"/>", root(CElem.class));

    check("function() as element(a) { <a/> }() " +
        "instance of element(a)",
        true, root(Bln.class));
    check("function() as attribute(a) { attribute a {} }() " +
        "instance of attribute(a)",
        true, root(Bln.class));
    check("function() as processing-instruction(a) { <?a?> }() " +
        "instance of processing-instruction(a)",
        true, root(Bln.class));

    check("function() as element(xml:a) { <xml:a/> }() " +
        "instance of element(xml:a)",
        true, root(Bln.class));
    check("function() as element(Q{_}a) { element Q{_}a {} }() " +
        "instance of element(Q{_}a)",
        true, root(Bln.class));
  }

  /** Elvis Operator rewritings. */
  @Test public void gh1911() {
    check("head((<a/>[data()], 1))", 1, root(Otherwise.class));
    check("head((<a/>[data()], <b/>[data()], <c/>[data()]))", null, empty(Otherwise.class));

    check("head((1, <_/>))", 1, root(Itr.class));
    check("head((<item/>, <default/>))", "<item/>", root(CElem.class));

    check("let $a := <a/>[data()] return if($a) then $a else ()", "", root(IterFilter.class));
    check("let $a := <a/>[data()] return if($a) then $a else 0", 0, root(Otherwise.class));
    check("if(trace(<a/>)) then trace(<a/>) else 0", "<a/>", root(If.class));

    check("let $x := <x/> for $a in 1 to 2 for $b in $x return ($b, $b)[1]", "<x/>\n<x/>",
        root(REPLICATE)
    );
  }

  /** Discard redundant union tests. */
  @Test public void gh1914() {
    check("<a/>/(self::* | self::a)", "<a/>", root(CElem.class));

    check("<a/>/(* | a)", "", type(IterStep.class, "element()*"));
    check("<a/>/(a | *)", "", type(IterStep.class, "element()*"));
    check("<a/>/(a | * | b)", "", type(IterStep.class, "element()*"));

    check("(<a/> | <b/> | <a/>)/self::b", "<b/>", type(Union.class, "(element(a)|element(b))+"));
    check("(<a/>, <b/>, <a/>)/self::b", "<b/>", type(Union.class, "(element(a)|element(b))+"));
    check("count((<a/> | <b/>, <b/> | <a/>))", 4, type(List.class, "(element(a)|element(b))+"));
  }

  /** Push type checks into expressions. */
  @Test public void gh1915() {
    check("(switch(" + wrap("a") + ") "
        + "  case 'a' return <a/> "
        + "  case 'b' return <b/> "
        + "  default  return error()"
        + ") coerce to element(a)",
        "<a/>", exists("SwitchGroup/TypeCheck"));
    check("for $e in (<a/>, <b/>) "
        + "return (typeswitch($e) "
        + "  case element(a) return <a/> "
        + "  case element(b) return <b/> "
        + "  default return error() "
        + ") coerce to element()",
        "<a/>\n<b/>", empty(Treat.class));
  }

  /** Rewrite side-effecting let expressions. */
  @Test public void gh1917() {
    check("let $a := (# basex:nondeterministic #) { <a/> } return $a ! name()",
        "a", root(Pipeline.class));
  }

  /** Rewrite list to replicate. */
  @Test public void gh1918() {
    check("(1, 1)", "1\n1", root(SingletonSeq.class));
    check("let $a := (<a/>, <a/>) return $a[1] is $a[2]", false, exists(REPLICATE));

    // partial rewrites
    check("1, 1, <a/>", "1\n1\n<a/>", exists(SingletonSeq.class));
    check("<a/>, 1, 1", "<a/>\n1\n1", exists(SingletonSeq.class));
  }

  /** Node-constructing functions must not be pre-evaluated (fresh identity per call). */
  @Test public void preEvalConstructors() {
    // constant argument: parse-xml is not pre-evaluated to a document value
    check("parse-xml('<a/>')", "<a/>", exists(PARSE_XML));

    // identical constructions in a list: rewritten to replicate, but nodes stay distinct
    check("let $d := (parse-xml('<a/>'), parse-xml('<a/>')) return $d[1] is $d[2]", false,
        exists(REPLICATE), exists(PARSE_XML));

    // separate calls yield distinct node identities
    check("let $a := parse-xml('<a/>'), $b := parse-xml('<a/>') return $a is $b", false);
    check("count(distinct-ordered-nodes((parse-xml('<a/>'), parse-xml('<a/>'))))", 2,
        exists(PARSE_XML));
    check("count(distinct-ordered-nodes((json-to-xml('{}'), json-to-xml('{}'))))", 2,
        exists(JSON_TO_XML));
    check("count(distinct-ordered-nodes((csv-to-xml('a'), csv-to-xml('a'))))", 2,
        exists(CSV_TO_XML));
  }

  /** Switch expression: static and dynamic cases. */
  @Test public void gh1919() {
    query("switch('x') case" + wrap("x") + "return 1 case 'x' return 2 default return 3", 1);
  }

  /** Switch expression, merge branches. */
  @Test public void gh1920() {
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 2 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));
    check("switch(<_/>) case 'a' return 1 case 'b' return 2 case 'c' return 2 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));
    check("switch(<_/>) case 'a' return 1 case 'b' return 2 case 'c' return 3 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));

    check("switch(" + wrap("a") + ") case 'a' return 1 case 'b' return 1 default return 2", 1,
        root(If.class));
    check("switch(" + wrap("a") + ") case 'a' return 1 case 'b' return 2 default return 2", 1,
        root(If.class));
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 1 default return 2", 2,
        root(If.class));

    check("switch(" + wrap("a") + ") case 'a' return 1 default return 1", 1,
        root(Itr.class));
    check("switch(" + wrap("a") + ") case 'a' return 1 case 'b' return 1 default return 1", 1,
        root(Itr.class));
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 1 default return 1", 1,
        root(Itr.class));
  }

  /** Rewrite integer lists to range sequences. */
  @Test public void gh1924() {
    check("1, 2", "1\n2", root(RangeSeq.class));
    check("-1, 0, 1", "-1\n0\n1", root(RangeSeq.class));
    check("1, 2 to 3", "1\n2\n3", root(RangeSeq.class));
    check("5, 6 to 7, 8", "5\n6\n7\n8", root(RangeSeq.class));
    check("5 to 6, 7 to 8", "5\n6\n7\n8", root(RangeSeq.class));
    check("5 to 6, 8", "5\n6\n8", root(BytSeq.class));
    check("1, 3 to 4", "1\n3\n4", root(BytSeq.class));

    // partial rewrites
    check("<a/>, 0 to 1", "<a/>\n0\n1", exists(RangeSeq.class));
    check("0 to 1, <a/>", "0\n1\n<a/>", exists(RangeSeq.class));
    check("0 to 1, <a/>, 2 to 3", "0\n1\n<a/>\n2\n3", count(RangeSeq.class, 2));
    check("0, 1, <a/>, 2, 3", "0\n1\n<a/>\n2\n3", count(RangeSeq.class, 2));
    check("0, 1 to 2, <a/>, 2 to 3, 4", "0\n1\n2\n<a/>\n2\n3\n4",
        count(RangeSeq.class, 2), empty(Itr.class));
    check("0 to 1, 2 to 3, <a/>", "0\n1\n2\n3\n<a/>", count(RangeSeq.class, 1));
    check("0, reverse(1 to 2), <a/>", "0\n2\n1\n<a/>", count(RangeSeq.class, 1), exists(Itr.class));
    check("xs:byte(0), 1, 2, <a/>", "0\n1\n2\n<a/>", exists(RangeSeq.class));
    check("1 to 2, 1 to 2, 3, <a/>", "1\n2\n1\n2\n3\n<a/>", count(RangeSeq.class, 2));
  }

  /** DDO property of recursive function. */
  @Test public void gh1927() {
    query("declare function local:a($a) as xs:boolean {"
        + "if ($a) then local:a(text { '' }) "
        + "else if ($a) then local:a($a) "
        + "else false() }; "
        + "local:a(()) ! text { . }", false);
    query("declare function local:a($a) as xs:boolean {"
        + "if ($a) then local:a(text { '' }) "
        + "else if ($a) then local:a($a) "
        + "else false() }; "
        + "let $c := local:a(()) return text { $c }", false);
  }

  /** Pre-evaluate lookup expressions. */
  @Test public void gh1928() {
    // pre-evaluate lookup with zero or single input items
    check("()?1", "", empty());
    check("()?*", "", empty());
    check("{ 1: 2 }?*", 2, root(Itr.class));
    check("[ 1 ]?*", 1, root(Itr.class));
    check("(({ 1: 'A', 'x': 'B' }) treat as function(xs:anyAtomicType) as xs:string?)?1",
        "A", root(Str.class));

    // do not pre-evaluate lookups with multiple input items
    check("([ 1 ], { 2: 3 })?*", "1\n3", root(Lookup.class));
  }

  /** Rewrite distinct sequence checks. */
  @Test public void gh1930() {
    check("(1 to 2)[. = (5, 4, 3, 1, 2, 5)]", "1\n2", exists(RangeSeq.class));
    check("distinct-values((5, 3, 4, 2, 1, 6, 1))", "5\n3\n4\n2\n1\n6", root(BytSeq.class));
  }

  /** Rewrite group by to distinct-values(). */
  @Test public void gh1932() {
    check("for $a in 1 to 2 group by $a return $a", "1\n2", root(RangeSeq.class));
    check("for $a in 1 to 2 group by $b := $a return $b", "1\n2", root(RangeSeq.class));
    check("for $a in 1 to 2 group by $b := data($a) return $b", "1\n2", root(RangeSeq.class));

    check("for $a in (1, 2) group by $a return $a", "1\n2", root(RangeSeq.class));
    check("for $a in (1, 3) group by $a return $a", "1\n3", root(BytSeq.class));
    check("for $a in (1, 'a', 1) group by $a return $a", "1\na", root(ItemSeq.class));

    check("for $p in (1 to 2)[. >= 0] group by $q := string($p) return $q",
        "1\n2", root(DISTINCT_VALUES), exists(DualMap.class));
    check("for $a in (1 to 2)[. >= 0] group by $a return $a coerce to xs:double",
        "1\n2", root(DualMap.class));

    check("for $a in (1 to 2)[. >= 0] group by $b := string($a) return $b || 'x'",
        "1x\n2x", count(DualMap.class, 2));

    // do not rewrite group by clauses if value is not a single atomic item
    check("for $a in (1 to 6) group by $g := [ $a mod 1 ] return $g", 0, root(Itr.class));
    check("for $a allowing empty in () group by $a return $a", "", root(GFLWOR.class));
    check("for $a in (1 to 6) group by $g := [] return $g", "", root(GFLWOR.class));
    check("for $a as xs:byte in (1 to 2)[. >= 0] group by $a return $a", "1\n2",
        root(DISTINCT_VALUES), exists(GFLWOR.class));
    check("for $a in (1 to 2)[. >= 0] group by $a as xs:byte := $a return $a", "1\n2",
        root(GFLWOR.class));

    error("for $a as xs:byte in (127 to 128)[. >= 0] group by $a return $a", INVTYPE_X);
    error("for $a in (127 to 128)[. >= 0] group by $a as xs:byte := $a return $a", INVTYPE_X);
  }

  /** Rewrite expression range to replicate. */
  @Test public void gh1934() {
    check("for $i in (1 to 2)[. >= 0] return (1 to $i) ! $i", "1\n2\n2",
        root(DualIterMap.class), exists(REPLICATE));
    check("for $i in (1 to 2)[. >= 0] for $j in 1 to $i return $i", "1\n2\n2",
        root(DualIterMap.class), exists(REPLICATE));
  }

  /** Iterative path traversal, positional access. */
  @Test public void gh1935() {
    query("declare variable $x := <x><a><_/></a></x>; $x/a ! (_[$x/_ ! 1], .)", "<a><_/></a>");
  }

  /** Positional checks. */
  @Test public void gh1937() {
    check("1[position()]", 1, root(Itr.class));
    check("(1, 3)[position()]", "1\n3", root(BytSeq.class));
    check("('a', 'b')[position()[position() ! .]]", "a\nb", root(StrSeq.class));

    check("(1, 3, 5)[last() - 2]", 1, root(Itr.class));
    check("(1, 3, 5)[position() = last() - 1]", 3, root(Itr.class));
    check("(1, 3, 5, 7)[position() = last() idiv 2]", 3, root(Itr.class));
    check("(1, 3, 5, 7)[position() = last() div 2]", 3, root(Itr.class));

    check("(1, 3, 5)[not(position() = 2)]", "1\n5", root(SmallSeq.class));
    check("(1, 3, 5)[not(position() > 2)]", "1\n3", root(SubSeq.class));
    check("(1, 3, 5)[not(position() < 2)]", "3\n5", root(SubSeq.class));
    check("(1, <_/>[data()])[not(position() = 2)]", 1, root(REMOVE));

    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() = $i)]",
        "4\n3", exists(REMOVE));
    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() > $i)]",
        "3\n3\n4", exists(_UTIL_RANGE));
    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() < $i)]",
        "3\n4\n4", exists(_UTIL_RANGE));
  }

  /** Simple map, context value. */
  @Test public void gh1941() {
    query("<a/> ! <b>{ (<c>{ . }</c>, .) ! name()[. = 'a'] }</b>", "<b>a</b>");
  }

  /** If expression, boolean/not. */
  @Test public void gh1942() {
    check("for $a in (false(), true()) for $b in (false(), true()) return "
        + "if($a) then $b else not($b)", "true\nfalse\nfalse\ntrue", empty(If.class));
    check("for $a in (false(), true()) for $b in (false(), true()) return "
        + "if($a) then not($b) else $b", "false\ntrue\ntrue\nfalse", empty(If.class));

    check("for $a in (false(), true()) for $b in (0, 1) return "
        + "if($a) then not($b) else boolean($b)", "false\ntrue\ntrue\nfalse", empty(If.class));
    check("for $a in (false(), true()) for $b in (0, 1) return "
        + "if($a) then boolean($b) else not($b)", "true\nfalse\nfalse\ntrue", empty(If.class));

    check("for $a in (1, 2) for $b in (false(), true()) return "
        + "if($a) then $b else not($b)", "false\ntrue\nfalse\ntrue", empty(If.class));
    check("for $a in (1, 2) for $b in (false(), true()) return "
        + "if($a) then not($b) else $b", "true\nfalse\ntrue\nfalse", empty(If.class));

    check("let $a := <a/>[. = ''] for $b in (false(), true()) return "
        + "if($b) then exists($a) else empty($a)", "false\ntrue", empty(If.class));
    check("let $a := <a/>[. = ''] for $b in (false(), true()) return "
        + "if($b) then empty($a) else exists($a)", "true\nfalse", empty(If.class));

    check("let $a := <a/> for $b in (false(), true()) return "
        + "if($b) then $a = '' else $a != ''", "false\ntrue", empty(If.class));
    check("let $a := <a/> for $b in (false(), true()) return "
        + "if($b) then $a != '' else $a = ''", "true\nfalse", empty(If.class));
  }

  /** Inline arguments of replicated items and singleton sequences. */
  @Test public void gh1963() {
    check(REPLICATE.args("x", 2) + " ! (. = 'x')", "true\ntrue", root(SingletonSeq.class));
    check(REPLICATE.args(" <a/>", 2) + " ! (. = 'x')", "false\nfalse", root(REPLICATE));

    check("(10, 10) ! .[. = 5]", "", empty());
    check("(10 to 15) ! .[. = 5]", "", root(IterFilter.class));
    check(REPLICATE.args(" <a/>", 2) + " ! .[data()]", "", root(REPLICATE));
  }

  /** Inlining context item expression. */
  @Test public void gh1964() {
    query("let $a := <a/> return <b/>[. ! ./$a]", "<b/>");
    query("let $a := <a/> return <b/>[. ! data()[. = $a]]", "");
  }

  /** Faster fn:count operations. */
  @Test public void gh1965() {
    check("(1 to 2)[. = 1] => sort() => count()", 1, empty(SORT));
    check("(1 to 2)[. = 1] => reverse() => count()", 1, empty(REVERSE));
    check("(for $i in (1 to 2)[. = 1] order by $i return $i ) => count()", 1, empty(OrderBy.class));
  }

  /** Rewrite order by to fn:sort. */
  @Test public void gh1966() {
    check("for $i in (1 to 2)[. >= 0] order by 1 return $i", "1\n2",
        empty(OrderBy.class), root(IterFilter.class));
    check("for $i in (1 to 2)[. >= 0] order by 1 descending return $i", "1\n2",
        empty(OrderBy.class), root(IterFilter.class));

    check("for $i in (1 to 2)[. >= 0] order by $i return $i", "1\n2",
        empty(OrderBy.class), root(SORT));
    check("for $i in (1 to 2)[. >= 0] order by $i descending return $i", "2\n1",
        empty(OrderBy.class), root(REVERSE));
    check("for $i in (1 to 2)[. >= 0] order by $i return $i * 2", "2\n4",
        empty(OrderBy.class), root(DualMap.class));
    check("let $_ :=" + wrap(1) +
        "for $i in (1 to 2)[. >= 0] " +
        "order by $i " +
        "return $i + $_", "2\n3",
        empty(OrderBy.class), root(GFLWOR.class));

    check("for $i in (1 to 2)[. >= 0] order by $i empty greatest return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by $i collation '?lang=de' return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in ([ 1 ], [ 2 ]) order by $i return $i?*", "1\n2",
        exists(OrderBy.class));
    check("for $a in (1 to 2)[. >= 0] for $b in 3 to 4 order by $a return $a", "1\n1\n2\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by $i, 1 return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by -$i return $i", "2\n1",
        exists(OrderBy.class));
  }

  /** distinct-values: simplify arguments. */
  @Test public void gh1967() {
    java.util.function.BiConsumer<String, Integer> check = (query, result) ->
      check("count(distinct-values((" + query + ")))", result, root(Itr.class));

    // values will not be pre-evaluated as range is larger than CompileContext#MAX_PREEVAL
    check.accept("1 to 300000, 1 to 300000", 300000);
    check.accept("1 to 300000, 1", 300000);
    check.accept("1, 1 to 300000", 300000);
    check.accept("0, 1 to 300000", 300001);
    check.accept("1 to 300000, 300001", 300001);
    check.accept("0, 1 to 300000, 300001", 300002);
    check.accept("1 to 50, 51 to 300000, 300001 to 300006", 300006);

    check = (query, result) ->
      check("count(distinct-values((" + query + ")))", result, root(FnCount.class));
    check.accept("1 to 300000, 0", 300001);
    check.accept("300001, 1 to 300000", 300001);
    check.accept("300001, 1 to 300000, 0", 300002);
    check.accept("1 to 300000, 1, 0, 300000, 100, 10 to 10000, 300001", 300002);

    check("count(distinct-values((1 to 300000, 300002)))", 300001, root(COUNT));
    check("count(distinct-values((0, 2 to 300000)))", 300000, root(COUNT));
  }

  /** Existence checks, filter expressions. */
  @Test public void gh1971() {
    check("for $i in (1 to 2)[. >= 0] return exists($i[. = 1])", "true\nfalse",
        root(DualMap.class), exists(CmpSimpleG.class));
  }

  /** Parallel execution. */
  @Test public void gh1329() {
    query(_XQUERY_FORK_JOIN.args(REPLICATE.args(" function() { <x/>[@a] }", 10000)), "");
    query("1 ! fn:last#0()", 1);
  }

  /** Range and arithmetic expression. */
  @Test public void gh1972() {
    check("(1 to 2) ! (. + 1)", "2\n3", root(RangeSeq.class));
    check("reverse(1 to 2) ! (. - 1)", "1\n0", root(RangeSeq.class));
    check("reverse((1 to 2) ! (. - 1))", "1\n0", root(RangeSeq.class));
  }

  /** Count optimizations. */
  @Test public void gh1973() {
    check("count((<a/>, <b/>)[self::a])", 1, exists(DualMap.class));
  }

  /** EBV tests, count → exists. */
  @Test public void gh1974() {
    check("boolean(count(tokenize(" + wrap("a b") + ")))", true, root(EXISTS));
    check("boolean(count(tokenize(" + wrap("") + ")))", false, root(EXISTS));

    check("boolean(string-length(<_/>))", false, root(EXISTS), exists(IterPath.class));
    check("boolean(string-length(" + wrap("A") + "))", true, exists(STRING));
  }

  /** Faster data/string checks. */
  @Test public void gh1975() {
    final String checkSteps = count(IterStep.class, 2);

    String input = "<x><a>A</a></x>", output = "<a>A</a>";
    check(input + "/*[string-length() > 0]", output, checkSteps);
    check(input + "/*[string-length(.) > 0]", output, checkSteps);
    check(input + "/*[string()]", output, checkSteps);
    check(input + "/*[string(.)]", output, checkSteps);
    check(input + "/*[data()]", output, checkSteps);
    check(input + "/*[data(.)]", output, checkSteps);
    check(input + "/*[normalize-space()]", output, checkSteps, exists(NORMALIZE_SPACE));
    check(input + "/*[normalize-space(.)]", output, checkSteps, exists(NORMALIZE_SPACE));

    input = "<x><a>{ ' ' }</a></x>";
    output = "<a> </a>";
    check(input + "/*[string-length() > 0]", output, checkSteps);
    check(input + "/*[string-length(.) > 0]", output, checkSteps);
    check(input + "/*[string()]", output, checkSteps);
    check(input + "/*[string(.)]", output, checkSteps);
    check(input + "/*[data()]", output, checkSteps);
    check(input + "/*[data(.)]", output, checkSteps);
    check(input + "/*[normalize-space()]", "", checkSteps, exists(NORMALIZE_SPACE));
    check(input + "/*[normalize-space(.)]", "", checkSteps, exists(NORMALIZE_SPACE));
  }

  /** Lookup operator, iterative evaluation. */
  @Test public void gh1984() {
    query("head(([ 2 ], 3) ?1)", 2);
    query("[ {}, {} ] ?* [ ?* ]", "");
  }

  /** Simple maps, group by: Context check. */
  @Test public void gh1987() {
    query("function($m) { function() { $m?* } ! (" +
        "for $b in $m?0 let $c := .() group by $d := () return $c" +
        ")}({ 0: 1 })", 1);
  }

  /** Casts, cardinality tests. */
  @Test public void gh1998() {
    check("zero-or-one((1 to 2)[. = 2]) cast as xs:decimal?", 2, empty(ZERO_OR_ONE));
    check("exactly-one((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(EXACTLY_ONE));
    check("zero-or-one((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(ZERO_OR_ONE));
    check("one-or-more((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(ONE_OR_MORE));

    check("xs:decimal(exactly-one((1 to 2)[. = 2]))", 2, exists(EXACTLY_ONE));
    check("xs:decimal(one-or-more((1 to 2)[. = 2]))", 2, exists(ONE_OR_MORE));

    check("zero-or-one((1 to 2)[. = 3]) coerce to empty-sequence()", "", empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) coerce to xs:decimal" , 2, empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) coerce to xs:decimal?", 2, empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) coerce to xs:decimal+", 2, empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) coerce to xs:decimal*", 2, exists(ZERO_OR_ONE));

    check("exactly-one((1 to 2)[. = 2]) coerce to xs:decimal" , 2, empty(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) coerce to xs:decimal?", 2, exists(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) coerce to xs:decimal+", 2, exists(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) coerce to xs:decimal*", 2, exists(EXACTLY_ONE));

    check("one-or-more((1 to 2)[. = 2]) coerce to xs:decimal" , 2, empty(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) coerce to xs:decimal?", 2, empty(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) coerce to xs:decimal+", 2, empty(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) coerce to xs:decimal*", 2, exists(ONE_OR_MORE));
  }

  /** Inline transform-with expression. */
  @Test public void gh2000() {
    inline(true);
    check("function() { <a/> update { delete node b } }()", "<a/>", root(TransformWith.class));
  }

  /** Dynamic unroll limit.. */
  @Test public void gh2001() {
    check("(1, 3) ! (. * 2)", "2\n6", exists(ArithSimple.class));
    check("sum((1, 3) ! (. * 2))", 8, exists(ArithSimple.class));
    check("sum((# db:unrolllimit 6 #) { (1 to 6) ! (. * 2) })", 42, empty(ArithSimple.class));
    check("(1, 2)[. = 1]", 1, root(IterFilter.class));

    unroll(true);
    check("(1, 3) ! (. * 2)", "2\n6", empty(ArithSimple.class));
    check("sum((# db:unrolllimit 0 #) { (1, 3) ! (. * 2) })", 8, exists(ArithSimple.class));
    check("(1, 2)[. = 1]", 1, root(Itr.class));
  }

  /** Accessing iterators with known result size. */
  @Test public void gh2029() {
    check("((1 to 1000000000000) ! string())[last()]", 1000000000000L,
        root(Str.class));
    check("((1 to 1000000000000) ! string())[position() = 999999999999]", 999999999999L,
        root(Str.class));
    check("((1 to 1000000000000) ! string())[position() = last() - 999999999999]", 1,
        root(Str.class));

    check(REPLICATE.args(" <x/>", " <c>200000000000</c>") + "[last()]", "<x/>",
        root(FOOT));
    check(REPLICATE.args(" <x/>", " <c>200000000000</c>", true) + "[last()]", "<x/>",
        root(FOOT));

    check("for-each-pair((1 to 10000000000), (1 to 9999999999), "
        + "function($a, $b) { $a = $b })[last()]", true,
        root(FOOT));
  }

  /** Element constructors: Text concatenation. */
  @Test public void gh2031() {
    check("<a>{}</a>", "<a/>", empty(Empty.class));
    check("<a>{ () }</a>", "<a/>", empty(Empty.class));

    check("<a>{ 'Jack' }</a>", "<a>Jack</a>",
        count(Str.class, 1));
    check("<a>Hi { 'Jack' }123</a>", "<a>Hi Jack123</a>",
        count(Str.class, 1), empty(Itr.class));
    check("<a>Hi { text { 'Jack' } }123</a>", "<a>Hi Jack123</a>",
        count(Str.class, 1), empty(CTxt.class), empty(Itr.class));
    check("<a>{ 'Hi', 'Jack', 123 }</a>", "<a>Hi Jack 123</a>",
        count(Str.class, 1), empty(CTxt.class), empty(Itr.class));
    check("<a>{ 'Hi ', text { 'Jack' }, '123' }</a>", "<a>Hi Jack123</a>",
        count(Str.class, 1), empty(CTxt.class), empty(Itr.class));
    check("<a>{ 'Hi ', (text { 'Jack' }, (), '123') }</a>", "<a>Hi Jack123</a>",
        count(Str.class, 1), empty(CTxt.class), empty(Itr.class), empty(Empty.class));
    check("<a>{ 'Hi ', (), text { 'Jack' }, '123' }</a>", "<a>Hi Jack123</a>",
        count(Str.class, 1), empty(CTxt.class), empty(Itr.class), empty(Empty.class));
  }

  /** Access to values in node constructors. */
  @Test public void gh2032() {
    check("declare variable $CONSTANTS := "
        + "<xml><value>a</value><value>b</value><value>c</value></xml>/value;"
        + "('a', 'b', 'c', 'd', 'e', 'f')[. = $CONSTANTS]",
        "a\nb\nc", empty(FElem.class), empty(SmallSeq.class));

    check("for $a in (1 to 7) ! string() return <_>{ $a }</_> = '4'",
        "false\nfalse\nfalse\ntrue\nfalse\nfalse\nfalse",
        empty(CElem.class), exists(Str.class));

    check("declare variable $x :=" + REPLICATE.args(" <a>a</a>", 2) +
        "; data($x)", "a\na", exists(SingletonSeq.class), exists(Atm.class));
    check("declare variable $x :=" + REPLICATE.args(" <a>a</a>", 2) +
        "; distinct-values($x)", "a", root(Atm.class));
  }

  /** Merge redundant casts. */
  @Test public void gh2036() {
    final String loop = "(1 to 6) ! ";
    final String string = "1\n2\n3\n4\n5\n6";

    check(loop + "number(string())", string, empty(STRING));
    check(loop + "number(string(.))", string, empty(STRING));
    check(loop + "xs:double(.) ! number(string())", string, empty(NUMBER), empty(STRING));
    check(loop + "xs:double(.) ! number(string(.))", string, empty(NUMBER), empty(STRING));
    check(loop + "xs:double(.) ! number(xs:string(.))", string, count(Cast.class, 1));
    check(loop + "xs:double(.) ! number(xs:untypedAtomic(.))", string, count(Cast.class, 1));

    check(loop + "xs:int(string())", string, empty(STRING));
    check(loop + "xs:int(string(.))", string, empty(STRING));
    check(loop + "xs:double(.) ! xs:int(string())", string, empty(STRING), count(Cast.class, 1));
    check(loop + "xs:double(.) ! xs:int(string(.))", string, empty(STRING), count(Cast.class, 1));
    check(loop + "xs:double(.) ! xs:int(xs:string(.))", string, count(Cast.class, 1));
    check(loop + "xs:double(.) ! xs:int(xs:untypedAtomic(.))", string, count(Cast.class, 1));

    check(loop + "xs:integer(xs:decimal(.))", string, root(RangeSeq.class));
    check(loop + "xs:int(xs:double(.))", string, count(Cast.class, 1));
    check(loop + "xs:short(xs:float(.))", string, count(Cast.class, 1));

    check(loop + "xs:decimal(xs:byte(.))", string, count(Cast.class, 2));
    check(loop + "xs:integer(xs:double(.))", string, count(Cast.class, 2));
    check(loop + "xs:int(xs:float(.))", string, count(Cast.class, 2));

    check("let $n := %basex:inline function($v as xs:anyAtomicType) { number($v) } " +
        "return <_><a>1</a><a>b</a></_>/a ! $n(.)",
        "1\nNaN",
        empty(TypeCheck.class));
    check("let $n := %basex:inline function($v as xs:untypedAtomic) { number($v) } " +
        "return <_><a>1</a><a>b</a></_>/a ! $n(.)",
        "1\nNaN",
        empty(TypeCheck.class));
  }

  /** Attribute constructor. */
  @Test public void gh2054() {
    query("<x attr=\"a{}\"/>", "<x attr=\"a\"/>");
    query("<x>a{}</x>", "<x>a</x>");
  }

  /** Rewrite let/where to for. */
  @Test public void gh2058() {
    check("let $a := <a/>[data()] where $a return $a", "", root(IterFilter.class));
    check("let $a := <a/>[data()] where $a return string($a)", "", root(DualMap.class));
  }

  /** Rewrite fn:not, comparisons (related to GH-1775). */
  @Test public void gh2061() {
    check("(1 to 6) ! boolean(.)[. = not(.)]", "", empty());
    check("(1 to 6) ! boolean(.)[not(.) = .]", "", empty());

    check("(1 to 6) ! boolean(.) ! (. = not(.))",
        "false\nfalse\nfalse\nfalse\nfalse\nfalse", root(SingletonSeq.class));
    check("(1 to 6) ! boolean(.) ! (not(.) = .)",
        "false\nfalse\nfalse\nfalse\nfalse\nfalse", root(SingletonSeq.class));

    query("count((0 to 5)[boolean(.)  = true()])", 5);
    query("count((0 to 5)[boolean(.) != true()])", 1);
    query("count((0 to 5)[boolean(.) >= true()])", 5);
    query("count((0 to 5)[boolean(.) <= true()])", 6);
    query("count((0 to 5)[boolean(.) >  true()])", 0);
    query("count((0 to 5)[boolean(.) <  true()])", 1);

    query("count((0 to 5)[boolean(.)  = false()])", 1);
    query("count((0 to 5)[boolean(.) != false()])", 5);
    query("count((0 to 5)[boolean(.) >= false()])", 6);
    query("count((0 to 5)[boolean(.) <= false()])", 1);
    query("count((0 to 5)[boolean(.) >  false()])", 5);
    query("count((0 to 5)[boolean(.) <  false()])", 0);

    query("string-join("
        + "for $a in (false(), true())"
        + "for $b in (false(), true())"
        + "for $c in ($a = $b, $a != $b, $a >= $b, $a > $b, $a <= $b, $a <  $b)"
        + "return if($c) then 1 else 0)",
        "101010010011011100101010");
  }

  /** Unnest predicate with value comparison. */
  @Test public void gh2059() {
    check("<a/>[b[@c eq '']]", "", empty(CmpV.class));
    check("<a><b c=''/></a>[b[@c eq '']] => count()", 1, empty(CmpV.class));

    check("boolean(<a b=''/>[@b eq ''])", true, empty(CmpV.class), root(CmpSimpleG.class));
  }

  /** Comparisons with boolean lists. */
  @Test public void gh2060() {
    check("for $a in 1 to 6 "
        + "let $b := boolean($a mod 2) "
        + "let $c := boolean($a mod 3) "
        + "return every $d in ($b, $c) satisfies $d",
        "true\nfalse\nfalse\nfalse\ntrue\nfalse",
        root(DualMap.class), exists(And.class));

    unroll(true);
    check("some $i in 1 to 2 satisfies contains(<_/>/*, string($i))", false, exists(Or.class));
    check("every $i in 1 to 2 satisfies contains(<_/>/*, string($i))", false, exists(And.class));

    check("let $a := <a><b>1</b><c>2</c></a> "
        + "where every $b in (1, 2) satisfies $a/* = $b "
        + "return $a/b/text()",
        1, empty(NOT), empty(And.class), count(CmpG.class, 2));
  }

  /** XQuery, simple map and filter expressions: Unroll lists. */
  @Test public void gh2067() {
    unroll(true);

    // unroll simple maps
    check("(<a/>, <b/>) ! self::a", "<a/>", root(CElem.class));
    check("(<a/>[data()], <b/>[data()]) ! self::c", "", empty());

    // unroll filters
    check("(<a/>, <b/>)[. = 'x']",
        "", root(List.class), count(IterFilter.class, 2));
    check("(<a/>[data()], <b/>[data()])[. = 'x']",
        "", root(List.class), count(IterFilter.class, 2));
  }

  /** UnionTest, Path index. */
  @Test public void gh2068() {
    execute(new CreateDB(NAME, FILE));

    // count
    check("/html/(head, body) => count()", 2, root(Itr.class));
    check("//(title, li) => count()", 3, root(Itr.class));

    // distinct-values
    check("//(title, li) => distinct-values() => count()", 3, root(Itr.class));

    // empty paths
    check("/html/(ul, li, unknown, div, title)", "", empty());
  }

  /** Comparisons with range expressions. */
  @Test public void gh2070() {
    // rewrite to integer comparison
    check("<x a='1'/>/@a = 1 to 3", true, exists(CmpIR.class));
    check("<x a='1'/>/@a = 1 to 3", true, exists(CmpIR.class));

    check("xs:integer(<x>1</x>/text()) >= (1 to 3)", true , exists(CmpIR.class));
    check("xs:integer(<x>1</x>/text()) >  (1 to 3)", false, exists(CmpIR.class));
    check("xs:integer(<x>3</x>/text()) <= (1 to 3)", true , exists(CmpIR.class));
    check("xs:integer(<x>3</x>/text()) <  (1 to 3)", false, exists(CmpIR.class));

    check("xs:integer(<x>1</x>/text()) >= 1", true , exists(CmpIR.class));
    check("xs:integer(<x>1</x>/text()) >  1", false, exists(CmpIR.class));
    check("xs:integer(<x>3</x>/text()) <= 3", true , exists(CmpIR.class));
    check("xs:integer(<x>3</x>/text()) <  3", false, exists(CmpIR.class));

    check("<x a='2'/>/@a >= 1 to 3", true, exists(CmpR.class));
    check("<x a='2'/>/@a >  1 to 3", true, exists(CmpR.class));
    check("<x a='2'/>/@a <= 1 to 3", true, exists(CmpR.class));
    check("<x a='2'/>/@a <  1 to 3", true, exists(CmpR.class));

    check("<x a='2'/>/@a             >  1e0", true , exists(CmpR.class));
    check("xs:integer(<x a='2'/>/@a) >  1e0", true , exists(CmpR.class));
    check("<x a='1'/>/@a             >  1e0", false, exists(CmpR.class));
    check("xs:integer(<x a='1'/>/@a) >  1e0", false, exists(CmpR.class));
    check("<x a='1'/>/@a             >= 1e0", true , exists(CmpR.class));
    check("xs:integer(<x a='1'/>/@a) >= 1e0", true , exists(CmpR.class));

    check("<x a='2'/>/@a >  1.0", true , exists(CmpR.class));
    check("<x a='1'/>/@a >  1.0", false, exists(CmpR.class));
    check("<x a='1'/>/@a >= 1.0", true , exists(CmpR.class));

    check("<x a='2'/>/@a >  11111111111111111", false, exists(CmpR.class));
    check("<x a='2'/>/@a < -11111111111111111", false, exists(CmpR.class));

    // no rewrite
    check("<x a='2'/>/@a != 1 to 3", true, empty(CmpR.class));
    check("<x a='2'/>/@a =  2"     , true, empty(CmpR.class));
    check("trace(2) = 1 to 3", true , empty(CmpR.class));
    check("xs:decimal(<x>1</x>/text()) >= 1.0", true , empty(CmpR.class));
    check("(1.0000000000000001, xs:double(<?_ 1?>)) > 1.0", true, empty(CmpR.class));

    check("xs:integer(<x a='2'/>/@a) != 2  ", false, empty(CmpR.class));
    check("xs:integer(<x a='2'/>/@a) >  1.0", true , empty(CmpR.class));
    check("xs:integer(<x a='1'/>/@a) >  1.0", false, empty(CmpR.class));
    check("xs:integer(<x a='1'/>/@a) >= 1.0", true , empty(CmpR.class));

    check("xs:integer(<x a='2'/>/@a) >  11111111111111111", false, empty(CmpR.class));
    check("xs:integer(<x a='2'/>/@a) < -11111111111111111", false, empty(CmpR.class));

    error("<x/> = 1", FUNCCAST_X_X);
    error("<x/> = 1 to 2", FUNCCAST_X_X);
    error("<?_ 1?> = 1 to 2", CMPTYPES_X_X_X_X);
  }

  /** Database statistics: min, max, sum, avg. */
  @Test public void gh2071() {
    // single value
    execute(new CreateDB(NAME, "<xml><a>1</a></xml>"));
    check("//a => count()", 1, root(Itr.class));
    check("//a => min()", 1, root(Dbl.class));
    check("//a => max()", 1, root(Dbl.class));
    check("//a => sum()", 1, root(Dbl.class));
    check("//a => avg()", 1, root(Dbl.class));
    // access text nodes
    check("//a/text() => count()", 1, root(Itr.class));
    check("//a/text() => min()", 1, root(Dbl.class));
    check("//a/text() => max()", 1, root(Dbl.class));
    check("//a/text() => sum()", 1, root(Dbl.class));
    check("//a/text() => avg()", 1, root(Dbl.class));

    // access attributes
    execute(new CreateDB(NAME, "<xml><a b='1'/></xml>"));
    check("//@b => count()", 1, root(Itr.class));
    check("//@b => min()", 1, root(Dbl.class));
    check("//@b => max()", 1, root(Dbl.class));
    check("//@b => sum()", 1, root(Dbl.class));
    check("//@b => avg()", 1, root(Dbl.class));

    // double values
    execute(new CreateDB(NAME, "<xml><a>1.5</a><a>3.5</a></xml>"));
    check("//a => count()", 2, root(Itr.class));
    check("//a => min()", 1.5, root(Dbl.class));
    check("//a => max()", 3.5, root(Dbl.class));
    check("//a => sum()", 5  , root(Dbl.class));
    check("//a => avg()", 2.5, root(Dbl.class));

    // negative and positive values
    execute(new CreateDB(NAME, "<xml><a>-1</a><a>0</a><a>1</a></xml>"));
    check("//a => count()", 3, root(Itr.class));
    check("//a => min()", -1, root(Dbl.class));
    check("//a => max()",  1, root(Dbl.class));
    check("//a => sum()",  0, root(Dbl.class));
    check("//a => avg()",  0, root(Dbl.class));

    // empty value must yield an error
    execute(new CreateDB(NAME, "<xml><a>-1</a><a>1</a><a/></xml>"));
    check("//a/text() => count()", 2, root(Itr.class));
    check("//a => count()", 3, root(Itr.class));
    error("//a => min()", FUNCCAST_X_X);
    error("//a => max()", FUNCCAST_X_X);
    error("//a => sum()", FUNCCAST_X_X);
    error("//a => avg()", FUNCCAST_X_X);

    // queries across multiple elements
    execute(new CreateDB(NAME, "<xml><a>1</a><b>3</b><c>5</c></xml>"));
    check("//(b, c, d) => count()", 2, root(Itr.class));
    check("//(b, c, d) => min()", 3, root(Dbl.class));
    check("//(b, c, d) => max()", 5, root(Dbl.class));
    check("//(b, c, d) => sum()", 8, root(Dbl.class));
    check("//(b, c, d) => avg()", 4, root(Dbl.class));
    execute(new Close());

    // whitespace will be preserved in categories, but numbers will still be detected
    query(_DB_CREATE.args(NAME, " <a><b>1</b><b>3 </b><b> 5</b></a>", NAME));
    final String query = _DB_GET.args(NAME) + "//b => ";
    check(query + "count()", 3, root(Itr.class));
    check(query + "min()", 1, root(Dbl.class));
    check(query + "max()", 5, root(Dbl.class));
    check(query + "sum()", 9, root(Dbl.class));
    check(query + "avg()", 3, root(Dbl.class));

    // check default MAXCATS limit
    query(_DB_CREATE.args(NAME, " <a>{ (1 to 100) ! <b>{ . }</b> }</a>", NAME));
    check(query + "count()", 100, root(Itr.class));
    check(query + "min()", 1, root(Dbl.class));
    check(query + "max()", 100, root(Dbl.class));
    check(query + "sum()", 5050, root(Dbl.class));
    check(query + "avg()", 50.5, root(Dbl.class));

    // exceed MAXCATS limit
    query(_DB_CREATE.args(NAME, " <a>{ (1 to 1000) ! <b>{ . }</b> }</a>", NAME));
    check(query + "count()", 1000, root(Itr.class));
    check(query + "min()", 1, root(Dbl.class));
    check(query + "max()", 1000, root(Dbl.class));
    check(query + "sum()", 500500, root(SUM));
    check(query + "avg()", 500.5, root(AVG));

    // lower MAXCATS limit
    query(_DB_CREATE.args(NAME, " <a>{ (1 to 1000) ! <b>{ . }</b> }</a>", NAME,
        " { 'maxcats': 0 }"));
    check(query + "count()", 1000, root(Itr.class));
    check(query + "min()", 1, root(Dbl.class));
    check(query + "max()", 1000, root(Dbl.class));
    check(query + "sum()", 500500, root(SUM));
    check(query + "avg()", 500.5, root(AVG));
  }

  /** Dynamic function calls, check updates. */
  @Test public void gh2073() {
    error("let $f := if(<a/>/text()) then db:create#3 else db:add#3 "
        + "return $f('db', <a/>, 'a.xml')", FUNCUP_X);
  }

  /** Rewrite index-of to comparison. */
  @Test public void gh2077() {
    check("let $a := (1 to 100000) ! string() "
        + "let $b := (100000 to 200001) ! string() "
        + "return $a[exists(index-of($b, .))]", 100000,
        exists(CmpHashG.class));
    check("let $a := (1 to 100001) ! string() "
        + "let $b := (1 to 100000) ! string() "
        + "return $a[empty(index-of($b, .))]", 100001,
        exists(CmpHashG.class));

    check("for $a in (1 to 6) "
        + "return <a/>[exists(($a[. = 1], $a[. = 1]))]", "<a/>",
        count(CmpSimpleG.class, 1));
  }

  /** Simple maps and comparisons. */
  @Test public void gh2078() {
    check("<_ c='1'/>/(every $c in @c satisfies contains($c, '1'))", true, root(NOT));
    check("<_ c='1'/>/(some $c in @c satisfies contains($c, '1'))", true, root(BOOLEAN));
    check("<_ c='1'/>/((@c ! contains(., '1')) = true())", true, root(BOOLEAN));
    check("<_ c='1'/>/(@c ! contains(., '1')) = true()", true, root(BOOLEAN));
    check("(<_ c='1'/>/@c ! contains(., '1')) = true()", true, root(BOOLEAN));

    check("<xml>Ukraine</xml>[some $text in text() satisfies $text = 'Ukraine']",
        "<xml>Ukraine</xml>", count(CmpG.class, 1), empty(GFLWOR.class));
    check("<xml>Ukraine</xml>[every $text in text() satisfies $text = 'Ukraine']",
        "<xml>Ukraine</xml>", count(CmpG.class, 1), empty(GFLWOR.class));

    check("(1 to 1000) ! (. >= 1)  = true() ", true, root(Bln.class));
    check("(1 to 1000) ! (. >= 1) != false()", true, root(Bln.class));

    check("<_><a>0</a><a>1</a></_>/(a ! (.  =  1 )  = true() )", true, count(CmpG.class, 1));
    check("<_><a>0</a><a>1</a></_>/(a ! (.  =  1 )  = false())", true, count(CmpG.class, 1));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >=  1 )  = true() )", true, root(CmpR.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >=  1 ) != false())", true, root(CmpR.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >= '1')  = true() )", true, root(CmpSR.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >= '1') != false())", true, root(CmpSR.class));

    // must not be rewritten
    check("(1 to 1000) ! (. >= 1) != true() ", false, root(CmpG.class));
    check("(1 to 1000) ! (. >= 1)  = false()", false, root(CmpG.class));

    check("<_><a>0</a><a>1</a></_>/(a ! (.  = (0, 2)) != true() )", true, count(CmpG.class, 2));
    check("<_><a>0</a><a>1</a></_>/(a ! (.  = (0, 2))  = false())", true, count(CmpG.class, 2));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >=  1    )  = false())", true, root(CmpG.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >=  1    ) != true() )", true, root(CmpG.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >= '1'   )  = false())", true, root(CmpG.class));
    check("<_><a>0</a><a>1</a></_>/(a ! (. >= '1'   ) != true() )", true, root(CmpG.class));

    check("let $seq := (1 to 1000000000) ! string() return"
        + "  some $a in $seq satisfies (some $b in $seq satisfies $a = $b)",
        true, root(Bln.class));
    check("let $seq := (1 to 1000000000) ! string() return"
        + "  some $a in $seq satisfies (some $b in $seq satisfies $b = $a)",
        true, root(Bln.class));

    // GH-2216: Bug on map operation of empty sequence
    query("<x><y/></x>/y[(@a ! (. >= 0)) = false()]", "");
    query("<x><y/></x>/y[(@a ! (. >= 0)) = true()]", "");
    check("<_ c='1'/>/(every $c in @d satisfies contains($c, '1'))", true, root(NOT));
  }

  /** where false(). */
  @Test public void gh2080() {
    check("let $_ := void(1) where false() return ()", "", root(VOID));
    check("let $_ := void(1) where false() return void(2)", "", root(VOID));
  }

  /** Rewrite value to general comparison. */
  @Test public void gh2082() {
    check("<a/>/@a eq ''", "", exists(CmpV.class));
    check("boolean(<a/>/@a eq '')", false, exists(CmpSimpleG.class));
    check("<e a=''/>[@a eq ''] ! name()", "e", exists(CmpSimpleG.class));
    check("<e a=''/>[@a ne ''] ! name()", "", exists(CmpSimpleG.class));

    execute(new CreateDB(NAME, "<e a='a'/>"));
    check("/e[@a eq 'a'] ! name()", "e", exists(ValueAccess.class));
  }

  /** Predicates: further optimize rewritings of 'if' expressions. */
  @Test public void gh2096() {
    check("<x/>[if(" + _RANDOM_DOUBLE.args() + " + 1) then . = '1' else ()]", "",
        empty(If.class), empty(And.class));
    check("<x>1</x>[if(. castable as xs:integer) then . = 1 else false()]", "<x>1</x>",
        empty(If.class), empty(And.class));
  }

  /** Embed positional function calls in arguments. */
  @Test public void gh2104() {
    check("head((1 to 10) ! <_>{ . }</_>)", "<_>1</_>", root(CElem.class));
    check("tail((1 to 3) ! (. * 10))", "20\n30", root(DualMap.class));
    check("reverse((1 to 2) ! <_>{ . }</_>)", "<_>2</_>\n<_>1</_>", root(DualMap.class));
    check("subsequence((1 to 6) ! (. * 2), 2, 2)", "4\n6", root(DualMap.class));

    check(TRUNK.args(" (1 to 10)[. > 7] ! (. * .)"), "64\n81", root(DualMap.class));
    check(ITEMS_AT.args(" (1 to 10)[. > 6] ! (. * .)", 2), 64, root(DualMap.class));
    check(FOOT.args(" (1 to 10)[. > 5] ! (. * .)"), 100, root(DualMap.class));
  }

  /** Full-text search, enforceindex enabled, AIOOB. */
  @Test public void gh2110() {
    query(_DB_CREATE.args(NAME, "<_>a</_>", "_.xml", " { 'ftindex': true() }"));

    query("declare option db:enforceindex 'true';"
        + "let $db := '" + NAME + "' "
        + "let $rs := " + _DB_GET.args(" $db") + "/descendant::text()[. contains text 'A'] "
        + "return $rs", "a");
    query("let $db := '" + NAME + "' "
        + "let $rs := (# db:enforceindex #) { "
        + _DB_GET.args(" $db") + "//text()[. contains text 'A'] "
        + "} "
        + "return $rs", "a");
  }

  /** Context item declaration, unknown function. */
  @Test public void gh2122() {
    error("declare context item external := local:f(); ()", WHICHFUNC_X);
  }

  /** XQFT: distances. */
  @Test public void gh2123() {
    query("'5 28 x x 5 28' contains text '5 28' all words distance at most 1 words", true);

    query("'A B A' contains text 'A B' all words distance exactly 0 words", true);
    query("'A B A' contains text 'B A' all words distance exactly 0 words", true);
    query("'A B A B' contains text 'A B' all words distance exactly 0 words", true);
    query("'A B A B' contains text 'B A' all words distance exactly 0 words", true);

    query("'A B A' contains text 'A B' all words distance exactly 1 words", false);
    query("'A B A' contains text 'B A' all words distance exactly 1 words", false);
    query("'A B A B' contains text 'A B' all words distance exactly 2 words", true);
    query("'A B A B' contains text 'B A' all words distance exactly 2 words", true);

    query("'C B A B C' contains text 'A B C' all words distance exactly 0 words", true);
    query("'C B A B C' contains text 'A C B' all words distance exactly 0 words", true);
    query("'C B A B C' contains text 'B A C' all words distance exactly 0 words", true);
    query("'C B A B C' contains text 'B C A' all words distance exactly 0 words", true);
    query("'C B A B C' contains text 'C A B' all words distance exactly 0 words", true);
    query("'C B A B C' contains text 'C B A' all words distance exactly 0 words", true);

    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);
    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);
    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);
    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);
    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);
    query("'C B A B C' contains text 'A' ftand 'B' ftand 'C' distance exactly 0 words", true);

    query("'A B C D' contains text 'A B' ftand 'C D' distance exactly 0 words", true);
    query("'A B C D' contains text 'C D' ftand 'A B' distance exactly 0 words", true);
  }

  /** Optimizations for grouping single values. */
  @Test public void gh2125() {
    check("for $b in ('a-1') group by $c := substring-before($b, '-') return $c",
        "a", root(Str.class));
    check("for $b in <?_ a-1?> group by $c := substring-before($b, '-') return $c",
        "a", root(SUBSTRING_BEFORE));
    check("for $b in <?_ a-1?> group by $c := substring-before($b, '-') " +
        "return ($c, string-length($c))",
        "a\n1", root(Pipeline.class));
  }

  /** Inlined type check. */
  @Test public void gh2142() {
    inline(true);
    check("function() { let $s := function() as xs:string { <_>1</_>/text() }() "
        + "return ($s, number($s)) }()", "1\n1");
  }

  /** Path existence checks. */
  @Test public void gh2145() {
    check("name(<a><b/></a>[(b, b)])",
        "a", empty(List.class), empty(IterMap.class), count(SingleIterPath.class, 1));

    check("name(<a><b><c/></b></a>[b ! (c, d)])",
        "a", empty(List.class), empty(IterMap.class), exists(IterPath.class));
    check("name(<a><b><c/></b></a>[b ! (c, d, c)])",
        "a", empty(List.class), empty(IterMap.class), exists(IterPath.class));

    check("<_>A</_>[text() ! string(.)]", "<_>A</_>", exists(STRING));
    check("<_>A</_>[text() ! string()]", "<_>A</_>", exists(STRING));
    check("<_>A</_>[string()]", "<_>A</_>", exists(SingleIterPath.class));

    // GH-2182: EBV tests, rewriting to descendant::text()
    check("boolean(<a><b>x</b></a>/*[@nr = 0] ! string())", false, exists(CmpSimpleG.class));
    check("<a><b/></a>/*[self::c[empty(node())]]", "",
        exists(IterPath.class), exists(SingleIterPath.class));

    // GH-2215: Unexpected exception, mapping double attributes
    check("boolean((<a/> ! (a, b)))", false, exists(Pipeline.class));
    check("boolean(count(<T/>//*[@id = '1'] ! (@a, @b)))", false, exists(IterPath.class));
  }

  /** Simplified if expression yields errors. */
  @Test public void gh2161() {
    check("let $s := ('1', '2') where ($s ! (if(.) then <a/> else ())) return ()", "");
  }

  /** Invert logical arguments of fn:not. */
  @Test public void gh2164() {
    check("(1 to 6)[not(boolean(.))]", "", empty(BOOLEAN), exists(NOT));
    check("(1 to 6)[not(not(.))]", "1\n2\n3\n4\n5\n6", exists(BOOLEAN), empty(NOT));
    check("(1 to 6)[not(. != 1)]", 1, empty(NOT), "//@op = '='");
    check("(1 to 6)[not(position() != 3)]", 3, root(Itr.class));
    check("(1 to 6)[not(. = 3 or (. != 1 and . != 4))]", "1\n4", empty(NOT));

    final String e1 = " tokenize(" + wrap("a b") + ')', e2 = " tokenize(" + wrap("") + ')';
    check(NOT.args(EMPTY.args(e1)  + " and " + EMPTY.args(e2)),  true , root(EXISTS));
    check(NOT.args(EXISTS.args(e1) + " or "  + EXISTS.args(e2)), false,  root(EMPTY));
    check(NOT.args(EMPTY.args(e1)  + " or "  + EMPTY.args(e2)),  false,  root(And.class));
    check(NOT.args(EXISTS.args(e1) + " and " + EXISTS.args(e2)), true , root(Or.class));
  }

  /** Function Arity of Partial Function Applications. */
  @Test public void gh2166() {
    query("declare function local:a() { function($a) { $a(1) }(local:b(2, ?)) };"
        + "declare function local:b($b, $c) {};"
        + "local:a()", "");
  }

  /** Simple map, if expressions. */
  @Test public void gh2175() {
    check("<a>A</a> ! (if(<b>B</b>/text()) then 1 else ())",
        1, root(If.class));
    check("<a>A</a> ! (if(text()) then 1 else ())",
        1, root(If.class));
    check("<a>A</a> ! (if(<b>B</b>/text()) then . else ())",
        "<a>A</a>", root(If.class));
    check("<a>A</a> ! (if(text()) then . else ())",
        "<a>A</a>", root(IterFilter.class), empty(If.class));
    check("<a>A</a> ! (if(text()) then data() else ())",
        "A", root(DATA), empty(If.class));
    check("<a>A</a> ! (if(text()) then string() else ())",
        "A", root(DualMap.class), empty(If.class));
  }

  /** Compare untyped atomics with QNames. */
  @Test public void gh2183() {
    error("xs:QName('x') = <x/>", FUNCCAST_X_X_X);
    error("<x/> = xs:QName('x')", FUNCCAST_X_X_X);
    query("<x> x </x> = xs:QName('x')", true);

    error("xs:QName('x') = xs:untypedAtomic('')", FUNCCAST_X_X_X);
    error("xs:untypedAtomic('') = xs:QName('x')", FUNCCAST_X_X_X);
    query("xs:untypedAtomic(' x ') = xs:QName('x')", true);
  }

  /** Bug of operation on non-existing attribute. */
  @Test public void gh2190() {
    check("<x/>[@a >= 0 or @a <= 0]", "", empty(Or.class), empty(EXISTS));
    check("<x/>[xs:integer(@a) >= 0 or xs:integer(@a) <= 0]", "", empty(Or.class), exists(EXISTS));

    check("<x/>[position() >= 0 or position() <= 0]", "<x/>", root(CElem.class));
  }

  /** Bug on node selection with last(). */
  @Test public void gh2191() {
    query("<a><b/><b/></a> ! head(*/last())", 2);
    query("head(<a><b/><b/></a>/* ! last())", 2);
  }

  /** Merge exact and range comparisons. */
  @Test public void gh2195() {
    // string ranges
    check("<_>X</_>[. <= 'W'][.  = 'X']", "", empty());
    check("<_>X</_>[. <  'X'][.  = 'X']", "", empty());
    check("<_>X</_>[.  = 'W'][. >= 'X']", "", empty());
    check("<_>X</_>[. <  'X'][.  = 'X']", "", empty());
    check("<_>X</_>[. <= 'X'][.  = 'X']", "<_>X</_>", empty(CmpSR.class));
    check("<_>X</_>[.  = 'X'][. >= 'X']", "<_>X</_>", empty(CmpSR.class));

    // numeric ranges
    check("<_>5</_>[. <= 4][.  = 5]", "", empty());
    check("<_>5</_>[. <  5][.  = 5]", "", empty());
    check("<_>5</_>[.  = 4][. >= 5]", "", empty());
    check("<_>5</_>[. <  5][.  = 5]", "", empty());
    check("<_>5</_>[. <= 5][.  = 5]", "<_>5</_>", empty(CmpSimpleG.class));
    check("<_>5</_>[.  = 5][. >= 5]", "<_>5</_>", empty(CmpSimpleG.class));

    // integer ranges
    check("(1 to 9)[. <= 4][.  = 5]", "", empty());
    check("(1 to 9)[. <  5][.  = 5]", "", empty());
    check("(1 to 9)[.  = 4][. >= 5]", "", empty());
    check("(1 to 9)[. <  5][.  = 5]", "", empty());
    check("(1 to 9)[. <= 5][.  = 5]", 5, empty(CmpSimpleG.class));
    check("(1 to 9)[.  = 5][. >= 5]", 5, empty(CmpSimpleG.class));
  }

  /** Speed up substring operations. */
  @Test public void gh2196() {
    query("count("
        + "  let $input := string-join("
        + "    for $i in 1 to 10000"
        + "    for $i in 32 to 127"
        + "    return codepoints-to-string($i)"
        + "  )"
        + "  return (1 to string-length($input)) ! substring($input, ., 1)"
        + ")", 960000);
    query("count("
        + "  let $input := string-join((32 to 55000) ! codepoints-to-string(.))"
        + "  return (1 to string-length($input)) ! substring($input, ., 1)"
        + ")", 54969);
  }

  /** EBV, string(). */
  @Test public void gh2201() {
    query("boolean(data(<_>x</_>/text()))", true);
    query("boolean(string(<_>x</_>/text()))", true);

    query("boolean(<_>x</_>/text() ! data())", true);
    query("boolean(<_>x</_>/text() ! string())", true);

    query("boolean(<_>x</_> ! text() ! data())", true);
    query("boolean(<_>x</_> ! text() ! string())", true);
  }

  /** Internal crash on has-children() result comparison. */
  @Test public void gh2213() {
    query("<x/>/* ! has-children() = false()", false);
    query("<x/>/* ! has-children() = true()", false);
    query("<x/>/* ! nilled() = false()", false);
    query("<x/>/* ! nilled() = true()", false);
  }

  /** Bug on consecutive map with distinct-values. */
  @Test public void gh2217() {
    query("<F/>[(distinct-values((true(), (. = ''))) ! 1 castable as xs:boolean)]",
        "<F/>");
    query("<F/>[(distinct-values(. ! (boolean(.), . = <A/>)) ! count(.)) castable as xs:boolean]",
        "<F/>");
  }

  /** Refine parameter types to arguments types of function call. */
  @Test public void gh2259() {
    check("declare function local:t($a) { if($a instance of xs:integer) then 1 else 's' };" +
        "local:t('x')", "s", empty(If.class));
  }

  /** if/then/else = else. */
  @Test public void gh2261() {
    query("(if ((1 to 6)[. = 1]) then 1 else 1.0) = 1", true);
    query("(if ((1 to 6)[. = 1]) then 1.0 else 1) = 1", true);
    query("(if ((1 to 6)[. = 1]) then 1 else 2.0) = 1", true);
    query("(if ((1 to 6)[. = 1]) then 1.0 else 2) = 1", true);
    query("(if ((1 to 6)[. = 1]) then 2 else 1.0) = 1", false);
    query("(if ((1 to 6)[. = 1]) then 2.0 else 1) = 1", false);

    query("(if ((1 to 6)[. = 0]) then 1 else 1.0) = 1", true);
    query("(if ((1 to 6)[. = 0]) then 1.0 else 1) = 1", true);
    query("(if ((1 to 6)[. = 0]) then 1 else 2.0) = 1", false);
    query("(if ((1 to 6)[. = 0]) then 1.0 else 2) = 1", false);
    query("(if ((1 to 6)[. = 0]) then 2 else 1.0) = 1", true);
    query("(if ((1 to 6)[. = 0]) then 2.0 else 1) = 1", true);

    query("(if ((1 to 6)[. = 0]) then 1 else xs:int(<?_ 1?>)) = 1", true);
    query("(if ((1 to 6)[. = 0]) then xs:int(<?_ 1?>) else 1) = 1", true);
    query("(if ((1 to 6)[. = 0]) then 2 else xs:int(<?_ 1?>)) = 1", true);
    query("(if ((1 to 6)[. = 0]) then xs:int(<?_ 2?>) else 1) = 1", true);
    query("(if ((1 to 6)[. = 0]) then 1 else xs:int(<?_ 2?>)) = 1", false);
    query("(if ((1 to 6)[. = 0]) then xs:int(<?_ 1?>) else 2) = 1", false);

    query("<d>1</d> ! ((if (text()) then xs:integer(.) else 1) = 1)", true);
    query("<d>1</d> ! ((if (not(text())) then 1 else xs:integer(.)) = 1)", true);
  }

  /** Function call with function(*) parameter type. */
  @Test public void gh2263() {
    query("let $f := fn($f as function(*)) { fold-left(1, (), $f) } return $f(true#0)", true);
    query("(fn($f as function(*)) { fold-left(1, (), $f) })(true#0)", true);
  }

  /** Boolean comparisons in simple maps. */
  @Test public void gh2271() {
    check("<a/>/@* ! true() = true()", false, root(EXISTS));
    check("<a b='x'/>/@* ! true() = true()", true, root(EXISTS));
    check("<a b='x'/>/@* ! true() != false()", true, root(EXISTS));

    check("<a b='x'/>/@* ! true() = false()", false, root(Bln.class));
    check("<a b='x'/>/@* ! true() != true()", false, root(Bln.class));
    check("<a b='x'/>/@* ! false() = true()", false, root(Bln.class));
    check("<a b='x'/>/@* ! false() != false()", false, root(Bln.class));
  }

  /** Database optimization: Enabling UPDINDEX. */
  @Test public void gh2272() {
    query("db:create('test')");
    query("db:put('test', <a/>, 'a.xml'), db:optimize('test', true(), { 'updindex': true() })");
  }

  /** Faster EBV checks. */
  @Test public void gh2326() {
    query("let $b := <a><b/></a> return <a/>[* union $b/b]", "<a/>");
    check("let $b := <a><b/></a> return <a/>[* union <b/>]", "<a/>", root(CElem.class));
  }

  /** for clause: coercion. */
  @Test public void gh2334() {
    query("for $a as xs:integer in [ 1, 2 ] return $a", "1\n2");
  }

  /** Typing: Filters, instance of. */
  @Test public void gh2345() {
    check("(1 to 10000000000000, 'x')[. instance of xs:integer] instance of xs:integer*",
        true, root(Bln.class));
    check("(1 to 10, 'x')[. instance of xs:byte] instance of xs:byte*",
        true, root(Bln.class));
    check("(1 to 10, 'x')[. instance of array(*)] instance of fn(*)*",
        true, root(Bln.class));
    check("(1 to 10000000000000, 'x')[. instance of xs:integer][. instance of xs:string]",
        "", empty());

    // GH-2460
    check("<a><b/>x</a>/node()[. instance of element()]", "<b/>", type(IterPath.class,
        "element()*"));
  }

  /** Include data reference in equality check. */
  @Test public void gh2373() {
    execute(new CreateDB(NAME + 1));
    execute(new Add("1.xml", "<a/>"));
    execute(new Add("2.xml", "<b/>"));
    execute(new CreateDB(NAME + 2));
    execute(new Add("3.xml", "<c/>"));
    execute(new Add("4.xml", "<d/>"));
    query("db:get('" + NAME + "1'), db:get('" + NAME + "2')", "<a/>\n<b/>\n<c/>\n<d/>");
  }

  /** Recursive function items: Typing. */
  @Test public void gh2314() {
    query("declare function local:f($p) as xs:integer {"
        + " if($p < 100000) then local:f($p + 1) else $p"
        + "};"
        + "local:f(1)", 100000);
     query("let $f := fn($p, $self) as xs:integer {"
        + " if($p < 100000) then $self($p + 1, $self) else $p"
        + "}"
        + "return $f(1, $f)", 100000);
  }

  /** Repeated evaluation of nondeterministic code. */
  @Test public void gh2395() {
    query("count(distinct-values((1 to 5) ! prof:track((1 to 1000000)[. = 0])?time)) > 1",
        true);
  }

  /** Erroneous rewrite to pipeline operator. */
  @Test public void gh2415() {
    query("let $a := 'x' let $b := if($a) then (<a/>, <b/>) else error() return $b/name()",
        "a\nb");
  }

  /** DualIterMap context value setting. */
  @Test public void gh2569() {
    check("let $node := <X><A/><B/><C/></X>\n"
        + "let $pos := $node/A ! position()\n"
        + "let $nodes := for $c at $i in $node/* where $i gt $pos return $c\n"
        + "let $flat := $nodes ! (if (*) then * else .)\n"
        + "return element Y {$flat except $flat[1]}",
        "<Y><C/></Y>", exists(DualIterMap.class));
    check("let $node := <X><A/><B/><C/></X>\n"
        + "return $node/A ! position()\n"
        + "  -> (for $c at $i in $node/* where $i gt . return $c) ! (* otherwise .)\n"
        + "  -> element Y {. except head(.)}",
        "<Y><C/></Y>", exists(DualIterMap.class));
  }
}
