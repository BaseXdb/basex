package org.basex.query.expr;

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
 * @author BaseX Team 2005-23, BSD License
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
    query("for $i in (1, 'a', 2) return <a b='{ $i }'/>[$i]", "<a b=\"1\"/>\n<a b=\"a\"/>");
    query("for $i in (1, 'a', 2) return <a b='{ $i }'/>"
        + "[<b c='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>");
    query("for $i in (1, 'a', 2) return "
        + "<a b='{ $i }'/>[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]"
            + "[<b c='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>");
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
        root(_UTIL_RANGE), "//Int = 1");
    check(expr + "[position() = 0 to xs:integer(" + wrap(1) + ")]", "<a/>",
        root(_UTIL_RANGE), "//Int = 1");
  }

  /** Dynamic positional range expressions. */
  @Test public void gh2140() {
    final String pre = "((65 to 70) ! element { codepoints-to-string(.) } {})[. = ''][position() ";
    final String post = "] ! name(.) => string-join()";

    check(pre + " = last() - 1 to last() - 2" + post, "",       root(Str.class));
    check(pre + " = last() - 2 to last() - 2" + post, "D",      empty(Pos.class));
    check(pre + " = last() - 3 to last() - 2" + post, "CD",     exists(Pos.class));

    check(pre + "!= last() - 3 to last() - 2" + post, "ABCDEF", exists(CmpG.class));
    check(pre + "<= last() - 3 to last() - 2" + post, "ABCD",   exists(CmpG.class));
    check(pre + "<  last() - 3 to last() - 2" + post, "ABC",    exists(CmpG.class));
    check(pre + ">= last() - 3 to last() - 2" + post, "CDEF",   exists(CmpG.class));
    check(pre + ">  last() - 3 to last() - 2" + post, "DEF",    exists(CmpG.class));

    check(pre + " + 1 = last()" + post, "E", empty(CmpSimpleG.class), empty(Pos.class));
    check(pre + " - 1 = last()" + post, "", empty(CmpSimpleG.class), empty(Pos.class));
    check(pre + " + 1 < last()" + post, "ABCD", empty(CmpSimpleG.class), exists(Pos.class));
    check(pre + " - 1 < last()" + post, "ABCDEF", empty(CmpSimpleG.class), empty(Pos.class));
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
}
