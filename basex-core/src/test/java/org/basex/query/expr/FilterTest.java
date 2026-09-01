package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for optimizations of the filter expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FilterTest extends SandboxTest {
  /** Drops a test database. */
  @AfterAll public static void end() {
    execute(new DropDB(NAME));
  }

  /** Filter expressions with a single predicate. */
  @Test public void onePredicate() {
    // empty sequence
    query("()['x']", "");
    query("()[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "");
    query("()[1]", "");
    query("()[last()]", "");

    // single item
    query("1['']", "");
    query("1['a']", 1);
    query("1[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", 1);

    query("1[0]", "");
    query("1[1]", 1);
    query("1[2]", "");
    query("1[last()]", 1);

    // numeric sequence
    query("(1 to 3)['']", "");
    query("(1 to 3)['a']", "1\n2\n3");
    query("(1 to 3)[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "1\n2\n3");

    query("(1 to 3)[0]", "");
    query("(1 to 3)[1]", 1);
    query("(1 to 3)[3]", 3);
    query("(1 to 3)[4]", "");
    query("(1 to 3)[last()]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{  .}'/>)['']", "");
    query("((1 to 2) ! <a b='{  .}'/>)['a']", "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{  .}'/>)[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("((1 to 2) ! <a b='{ . }'/>)[0]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[2]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[3]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[last()]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void singlePosAsLastPredicate() {
    // empty sequence
    query("()['x'][1]", "");
    query("()[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][1]", "");
    query("()[1][1]", "");
    query("()[last()][1]", "");

    // single item
    query("1[''][1]", "");
    query("1['a'][1]", 1);
    query("1[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][1]", 1);

    query("1[0][1]", "");
    query("1[1][1]", 1);
    query("1[2][1]", "");
    query("1[last()][1]", 1);

    // numeric sequence
    query("(1 to 3)[''][1]", "");
    query("(1 to 3)['a'][1]", 1);
    query("(1 to 3)[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][1]", 1);

    query("(1 to 3)[0][1]", "");
    query("(1 to 3)[1][1]", 1);
    query("(1 to 3)[3][1]", 3);
    query("(1 to 3)[4][1]", "");
    query("(1 to 3)[last()][1]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{ . }'/>)[''][1]", "");
    query("((1 to 2) ! <a b='{ . }'/>)['a'][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][1]", "<a b=\"1\"/>");

    query("((1 to 2) ! <a b='{ . }'/>)[0][1]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[2][1]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[3][1]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[last()][1]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void singlePosAsFirstPredicate() {
    // empty sequence
    query("()[1]['x']", "");
    query("()[1][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "");
    query("()[1][1]", "");
    query("()[1][last()]", "");

    // single item
    query("1[1]['']", "");
    query("1[1]['a']", 1);
    query("1[1][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", 1);

    query("1[1][0]", "");
    query("1[1][1]", 1);
    query("1[1][2]", "");
    query("1[1][last()]", 1);

    // numeric sequence
    query("(1 to 3)[1]['']", "");
    query("(1 to 3)[1]['a']", 1);
    query("(1 to 3)[1][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", 1);

    query("(1 to 3)[1][0]", "");
    query("(1 to 3)[1][1]", 1);
    query("(1 to 3)[1][2]", "");
    query("(1 to 3)[1][last()]", 1);

    // XML sequence
    query("((1 to 2) ! <a b='{ . }'/>)[1]['']", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1]['a']", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[1]"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "<a b=\"1\"/>");

    query("((1 to 2) ! <a b='{ . }'/>)[1][0]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[1][2]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1][last()]", "<a b=\"1\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void multiplePosAsLastPredicate() {
    // empty sequence
    query("()['x'][position() = 1 to 2]", "");
    query("()[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][position() = 1 to 2]", "");
    query("()[1][position() = 1 to 2]", "");
    query("()[last()][position() = 1 to 2]", "");

    // single item
    query("1[''][position() = 1 to 2]", "");
    query("1['a'][position() = 1 to 2]", 1);
    query("1[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][position() = 1 to 2]", 1);

    query("1[0][position() = 1 to 2]", "");
    query("1[1][position() = 1 to 2]", 1);
    query("1[2][position() = 1 to 2]", "");
    query("1[last()][position() = 1 to 2]", 1);

    // numeric sequence
    query("(1 to 3)[''][position() = 1 to 2]", "");
    query("(1 to 3)['a'][position() = 1 to 2]", "1\n2");
    query("(1 to 3)[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][position() = 1 to 2]", "1\n2");

    query("(1 to 3)[0][position() = 1 to 2]", "");
    query("(1 to 3)[1][position() = 1 to 2]", 1);
    query("(1 to 3)[3][position() = 1 to 2]", 3);
    query("(1 to 3)[4][position() = 1 to 2]", "");
    query("(1 to 3)[last()][position() = 1 to 2]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{ . }'/>)[''][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{ . }'/>)['a'][position() = 1 to 2]", "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][position() = 1 to 2]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("((1 to 2) ! <a b='{ . }'/>)[0][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[1][position() = 1 to 2]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[2][position() = 1 to 2]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{ . }'/>)[3][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{ . }'/>)[last()][position() = 1 to 2]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void variablePosAsFirstPredicate() {
    // empty sequence
    query("for $i in 1 to 2 return ()[$i]['x']", "");
    query("for $i in 1 to 2 return ()[$i][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "");
    query("for $i in 1 to 2 return ()[$i][1]", "");
    query("for $i in 1 to 2 return ()[$i][last()]", "");

    // single item
    query("for $i in 1 to 2 return 1[$i]['']", "");
    query("for $i in 1 to 2 return 1[$i]['a']", 1);
    query("for $i in 1 to 2 return 1[$i][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", 1);

    query("for $i in 1 to 2 return 1[$i][0]", "");
    query("for $i in 1 to 2 return 1[$i][1]", 1);
    query("for $i in 1 to 2 return 1[$i][2]", "");
    query("for $i in 1 to 2 return 1[$i][last()]", 1);

    // numeric sequence
    query("for $i in 1 to 2 return (1 to 3)[$i]['']", "");
    query("for $i in 1 to 2 return (1 to 3)[$i]['a']", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)[$i]"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "1\n2");

    query("for $i in 1 to 2 return (1 to 3)[$i][0]", "");
    query("for $i in 1 to 2 return (1 to 3)[$i][1]", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)[$i][2]", "");
    query("for $i in 1 to 2 return (1 to 3)[$i][last()]", "1\n2");

    // XML sequence
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i]['']", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i]['a']",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i]"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i][0]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i][1]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i][2]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[$i][last()]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void variablePosAsLastPredicate() {
    // empty sequence
    query("for $i in 1 to 2 return ()['x'][$i]", "");
    query("for $i in 1 to 2 return ()[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]", "");
    query("for $i in 1 to 2 return ()[1][$i]", "");
    query("for $i in 1 to 2 return ()[last()][$i]", "");

    // single item
    query("for $i in 1 to 2 return 1[''][$i]", "");
    query("for $i in 1 to 2 return 1['a'][$i]", 1);
    query("for $i in 1 to 2 return 1[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]", 1);

    query("for $i in 1 to 2 return 1[0][$i]", "");
    query("for $i in 1 to 2 return 1[1][$i]", 1);
    query("for $i in 1 to 2 return 1[2][$i]", "");
    query("for $i in 1 to 2 return 1[last()][$i]", 1);

    // numeric sequence
    query("for $i in 1 to 2 return (1 to 3)[''][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)['a'][$i]", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]", "1\n2");

    query("for $i in 1 to 2 return (1 to 3)[0][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)[1][$i]", 1);
    query("for $i in 1 to 2 return (1 to 3)[3][$i]", 3);
    query("for $i in 1 to 2 return (1 to 3)[4][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)[last()][$i]", 3);

    // XML sequence
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[''][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)['a'][$i]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[0][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[1][$i]", "<a b=\"1\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[2][$i]", "<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[3][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{ . }'/>)[last()][$i]", "<a b=\"2\"/>");
  }

  /** Variable predicates. */
  @Test public void variablePreds() {
    // empty sequence
    check("for $i in (1, 'a', 2) return <a b='{ $i }'/>[$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>",
        exists(HoistedFilter.class));
    check("for $i in (1, 'a', 2) return <a b='{ $i }'/>"
        + "[<b c='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>",
        exists(CachedFilter.class));
    check("for $i in (1, 'a', 2) return "
        + "<a b='{ $i }'/>[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]"
            + "[<b c='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>",
        exists(CachedFilter.class));
  }

  /** Hoisted filter: loop-invariant predicates evaluated once per filter call. */
  @Test public void hoistedFilter() {
    // single predicate, mixed type
    check("for $i in (1, 'x', 2, '') return ('a', 'b', 'c')[$i]",
        "a\na\nb\nc\nb",
        exists(HoistedFilter.class));
    check("for $i in (1, <a/>) return ('a', 'b')[$i]",
        "a\na\nb",
        exists(HoistedFilter.class));
    // single predicate, mixed type, out-of-range values
    check("for $i in (-1, 0, 1.5, 4, 'x') return ('a', 'b')[$i]",
        "a\nb",
        exists(HoistedFilter.class));
    // empty predicate value at runtime
    check("for $i in (1, 'a', 2) return ('A', 'B', 'C')[if(string($i) = 'a') then () else $i]",
        "A\nB",
        exists(HoistedFilter.class));
    // multiple predicates
    check("for $i in (1, 'x'), $j in (1, 'y') return ('a', 'b', 'c')[$i][$j]",
        "a\na\na\na\nb\nc",
        exists(HoistedFilter.class));
    check("for $i in (1, 'x'), $j in (4, 'y') return ('a', 'b')[$i][$j]",
        "a\na\nb",
        exists(HoistedFilter.class));
    // predicate yielding a sequence
    check("for $i in 1 to 2 return ('a', 'b', 'c')[<a/>, $i]",
        "a\nb\nc\na\nb\nc",
        exists(HoistedFilter.class));
  }

  /** Positional access, rewritings. */
  @Test public void posAccess() {
    query("(<a/>, <b/>)[position() > 1]", "<b/>");
    query("(<a/>, <b/>, <c/>)[position() > 2]", "<c/>");
    query("(<a/>, <b/>, <c/>)[position() = 2 to 3]", "<b/>\n<c/>");

    query("count((1 to 100000000)[position() != 1])", 99999999);
    query("count((1 to 100000000)[position() != 0])", 100000000);

    query("for $i in 1 to 2 return (1, 2)[.][$i]", "1\n2");
    query("for $i in 1 to 2 return (2, 1)[.][$i]", "");

    query("for $i in 1 to 2 return (1, 2)[$i][.]", 1);
    query("for $i in 1 to 2 return (2, 1)[$i][.]", 1);
  }

  /** Start position. */
  @Test public void documents() {
    execute(new CreateDB(NAME));
    execute(new Add("one", "<one/>"));
    execute(new Add("two", "<two/>"));
    execute(new Close());
    query(COLLECTION.args(NAME) + "[2]", "<two/>");
    query(_DB_GET.args(NAME) + "[2]", "<two/>");
  }

  /** Start position. */
  @Test public void gh1641() {
    query("(1 to 2)[position() = .]", "1\n2");
    query("(1 to 2)[position() != .]", "");
    query("((1 to 2)[. != 0])[position() != .]", "");
  }

  /** Rewrite positional tests. */
  @Test public void positional() {
    String expr = "(<a/>, <b/>, <c/>)";
    check(expr + "[position() = 0 to last()]", "<a/>\n<b/>\n<c/>",
        empty(LAST), root(List.class));
    check(expr + "[position() = 1 to last()]", "<a/>\n<b/>\n<c/>",
        empty(LAST), root(List.class));
    check(expr + "[position() = 2 to last()]", "<b/>\n<c/>",
        empty(LAST), root(List.class));
    check(expr + "[position() = 3 to last()]", "<c/>",
        empty(LAST), root(CElem.class));
    check(expr + "[position() = 1 to last() - 1]", "<a/>\n<b/>",
        empty(LAST), root(List.class));

    expr = "((<a/>, <b/>, <c/>)[. = ''])";
    check(expr + "[position() = 0 to last()]", "<a/>\n<b/>\n<c/>",
        empty(LAST), root(IterFilter.class));
    check(expr + "[position() = 1 to last()]", "<a/>\n<b/>\n<c/>",
        empty(LAST), root(IterFilter.class));
    check(expr + "[position() = 2 to last()]", "<b/>\n<c/>",
        empty(LAST), root(TAIL));
    check(expr + "[position() = 3 to last()]", "<c/>",
        empty(LAST), root(_UTIL_RANGE));
    check(expr + "[position() = 1 to last() - 1]", "<a/>\n<b/>",
        empty(LAST), root(TRUNK));
    check(expr + "[position() = 1 to last() - 2]", "<a/>",
        exists(LAST), root(CachedFilter.class));

    check(expr + "[position() = -65535 to xs:integer(" + wrap(1) + ")]", "<a/>",
        root(_UTIL_RANGE), "//Itr = 1");
    check(expr + "[position() = 0 to xs:integer(" + wrap(1) + ")]", "<a/>",
        root(_UTIL_RANGE), "//Itr = 1");
  }

  /** Dynamic positional range expressions. */
  @Test public void gh2140() {
    final String pre = "((65 to 70) ! element { codepoints-to-string(.) } {})[. = ''][position() ";
    final String post = "] ! name(.) => string-join()";

    check(pre + " = last() - 1 to last() - 2" + post, "",       root(Str.class));
    check(pre + " = last() - 2 to last() - 2" + post, "D",      exists(ITEMS_AT));
    check(pre + " = last() - 2 to last()"     + post, "DEF",    exists(SUBSEQUENCE));
    check(pre + " = last() - 3 to last() - 2" + post, "CD",     exists(Pos.class));

    check(pre + "!= last() - 3 to last() - 2" + post, "ABCDEF", exists(CmpG.class));
    check(pre + "<= last() - 3 to last() - 2" + post, "ABCD",   exists(CmpG.class));
    check(pre + "<  last() - 3 to last() - 2" + post, "ABC",    exists(CmpG.class));
    check(pre + ">= last() - 3 to last() - 2" + post, "CDEF",   exists(CmpG.class));
    check(pre + ">  last() - 3 to last() - 2" + post, "DEF",    exists(CmpG.class));

    check(pre + " + 1 = last()" + post, "E", empty(CmpSimpleG.class), exists(ITEMS_AT));
    check(pre + " - 1 = last()" + post, "", root(Str.class));
    check(pre + " + 1 < last()" + post, "ABCD", empty(CmpSimpleG.class), exists(Pos.class));
    check(pre + " - 1 < last()" + post, "ABCDEF", exists(DualMap.class), empty(Pos.class));
    check("for $i in -3 to 3 return " + pre + " + 1 = $i" + post,
        "\n\n\n\n\nA\nB", empty(CmpSimpleG.class));

    query("let $i := 1 return <x><a/></x>/*[position() >= $i to 0]", "");

    // GH-2220: Bug on arithmetic operations with last() and position()
    check("document { <S/> }//S[last() * 150 >= position()]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
    check("document { <S/> }//S[last() * 150000 >= position()]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
    check("document { <S/> }//S[position() <= last() * 1500000000000]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
    check("document { <S/> }//S[last() * -150 <= position()]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
    check("document { <S/> }//S[last() * -150000 <= position()]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
    check("document { <S/> }//S[position() >= last() * -150000000000]", "<S/>",
        empty(Arith.class), empty(ArithSimple.class));
  }

  /** Rewrite positional range tests. */
  @Test public void positionalRange() {
    final String pre = "(1 to 1000000)[. < 1][position() ";
    final String post = "]";

    // equal
    check(pre + "=  1         to last()    " + post, "", empty(Pos.class), root(IterFilter.class));
    check(pre + "=  0         to last()    " + post, "", empty(Pos.class), root(IterFilter.class));
    check(pre + "=  1         to last() + 1" + post, "", empty(Pos.class), root(IterFilter.class));
    check(pre + "= -2         to last() + 2" + post, "", empty(Pos.class), root(IterFilter.class));
    check(pre + "= last()     to 1         " + post, "", empty());
    check(pre + "= last() + 1 to last() + 2" + post, "", empty());

    check(pre + "= last() to last() - 1" + post, "", empty());
    check(pre + "= last() to last()    " + post, "", exists(HEAD), exists(RangeSeq.class));
    check(pre + "= last() to last() + 1" + post, "", exists(HEAD), exists(RangeSeq.class));
    check(pre + "= last() to last() + 2" + post, "", exists(HEAD), exists(RangeSeq.class));

    // not equal: various optimizations are currently discarded
    check(pre + "!= 0          to last() + 1" + post, "", empty());
    check(pre + "!= last() + 1 to last() + 2" + post, "", empty(Pos.class));
  }

  /** Rewrites of positional predicates to function calls. */
  @Test public void positionalRewrites() {
    final String pre = "(1 to 1000000)[. < 6][position() ", post = "]";

    // E[position() = INT to last()] → util:range(E, INT)
    check(pre + "= 2 to last()" + post, "2\n3\n4\n5", exists(TAIL));
    check(pre + "= 3 to last()" + post, "3\n4\n5", exists(_UTIL_RANGE));

    // E[position() = (INT1, INT2)] → items-at(E, (INT1, INT2))
    check(pre + "= (1, 3)" + post, "1\n3", exists(ITEMS_AT));
    check(pre + "= (3, 1)" + post, "1\n3", exists(ITEMS_AT));

    // positions are not known statically: they are sorted and deduplicated
    check(pre + "= ((<a>3</a>, <a>1</a>, <a>3</a>) ! xs:integer(.))" + post, "1\n3",
        exists(ITEMS_AT), exists(SORT), exists(DISTINCT_VALUES));
  }

  /** The root of a predicate path is dropped: E[./S] → E[S]. */
  @Test public void contextPath() {
    // context value as root
    check("<a><b/></a>[./b]", "<a><b/></a>", empty(ContextValue.class));
    check("<a><b><c/></b></a>[./b/c]", "<a><b><c/></b></a>", empty(ContextValue.class));
    check("<a><x/></a>[./b]", "", empty(ContextValue.class));
    // the root of the predicate equals the root of the filter
    check("let $n := <a><b/></a> return $n[$n/b]", "<a><b/></a>", empty(ContextValue.class));
  }

  /** Predicates with mixed operands. */
  @Test public void predicates() {
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

    // GH-1140
    query("declare function local:test() {"
        + "for $n in (1, 1) return <_><c/><w/></_>/*[$n[1]] }; local:test()/self::w", "");
    query("for $n in (2, 2) return (<c><c0/></c>, <d><d0/><d2/></d>)/*[$n[$n]]", "");
    query("(('XML')[1])[1]", "XML");
    query("1[position() = 1 to 2]", 1);
    query("1[position() = (1, 2)]", 1);
    query("count((text { 'x' }, element x {})[. instance of element()])", 1);
  }

  /** Predicates on generalized nodes. */
  @Test public void generalizedNodes() {
    query("count((jtree({ 'a': 1 }), <x/>)[self::xnode()])", 1);
    query("count((jtree({ 'a': 1 }), <x/>)[exists(self::xnode())])", 1);
    query("count((jtree({ 'a': 1 }), <x/>)/self::xnode())", 1);
    query("count((jtree({ 'a': 1 }), <x/>)[self::jnode()])", 1);
    query("count((jtree({ 'a': 1 }), <x/>)[self::node()])", 2);
    query("count(jtree({ 'a': 1 })[self::xnode()])", 0);

    // predicates must not widen the node test of the step they are attached to
    final String mix = "let $x := (jtree({ 'a': { 'b': 'x' } }), <r><a><b>x</b></a></r>) return ";
    query(mix + "count($x/descendant::a)", 2);
    query(mix + "count($x/descendant::a[. instance of xnode()])", 1);
    query(mix + "count($x/descendant::a[. instance of jnode()])", 1);
    query(mix + "count($x/descendant::a[. instance of element()])", 1);
    query(mix + "count($x/descendant::b[. instance of xnode()])", 1);
    query(mix + "count(($x/descendant::a)[self::xnode()])", 1);

    // xnode() is not a supertype of jnode(): the self step must not be merged away
    final String jb = "let $x := jtree({ 'a': { 'b': 1 } })//b return ";
    query(jb + "count($x[self::xnode()])", 0);
    query(jb + "count(filter($x, fn($n) { boolean($n[self::xnode()]) }))", 0);
    query(jb + "count($x => for-each(fn($n) { $n[self::xnode()] }))", 0);
    query(jb + "count($x[self::jnode()])", 1);

    // predicates that are rewritten to a self step must not exclude JSON nodes
    final String mixed = "let $x := (jtree({ 'a': { 'b': 'x' } }), <r><a><b>x</b></a></r>)/a ";
    query(mixed + "return count($x ! (.[b = 'x'], 'z'))", 4);
    query(mixed + "return count($x ! ('z', .[b = 'x']))", 4);
    query(mixed + "return count(for $n in $x return ($n[b = 'x'], 'z'))", 4);
  }

  /** GH-2687: NPE from AndExpr filter over ContextValueRef step. */
  @Test public void gh2687() {
    query("()/.[true() and true()]", "");
    query("declare function all-whitespace($arg) { normalize-space($arg) = '' };\n"
        + "<a/>/.[true() and all-whitespace('')]", "<a/>");
  }

  /** Predicates with choice item types. */
  @Test public void choiceItemType() {
    final String func = "declare function local:f($x as (xs:integer | xs:string)) ";
    query(func + "{ (10, 20, 30)[$x] }; local:f(2)", 20);
    query(func + "{ (10, 20, 30)[$x] }; local:f('a')", "10\n20\n30");
    query(func + "{ (10, 20, 30) ! .[$x] }; local:f(2)", "");
    query(func + "{ (10, 20, 30) ! .[$x] }; local:f('a')", "10\n20\n30");
  }
}
