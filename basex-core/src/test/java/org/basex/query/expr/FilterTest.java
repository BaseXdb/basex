package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for optimizations of the filter expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FilterTest extends QueryPlanTest {
  /** Drops a test database. */
  @AfterAll public static void end() {
    execute(new DropDB(NAME));
  }

  /** Filter expressions with a single predicate. */
  @Test public void onePredicate() {
    // empty sequence
    query("()['x']", "");
    query("()[<a b='{random:integer()}'/>]", "");
    query("()[1]", "");
    query("()[last()]", "");

    // single item
    query("1['']", "");
    query("1['a']", 1);
    query("1[<a b='{random:integer()}'/>]", 1);

    query("1[0]", "");
    query("1[1]", 1);
    query("1[2]", "");
    query("1[last()]", 1);

    // numeric sequence
    query("(1 to 3)['']", "");
    query("(1 to 3)['a']", "1\n2\n3");
    query("(1 to 3)[<a b='{random:integer()}'/>]", "1\n2\n3");

    query("(1 to 3)[0]", "");
    query("(1 to 3)[1]", 1);
    query("(1 to 3)[3]", 3);
    query("(1 to 3)[4]", "");
    query("(1 to 3)[last()]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{.}'/>)['']", "");
    query("((1 to 2) ! <a b='{.}'/>)['a']", "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[<a b='{random:integer()}'/>]", "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("((1 to 2) ! <a b='{.}'/>)[0]", "");
    query("((1 to 2) ! <a b='{.}'/>)[1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[2]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[3]", "");
    query("((1 to 2) ! <a b='{.}'/>)[last()]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void singlePosAsLastPredicate() {
    // empty sequence
    query("()['x'][1]", "");
    query("()[<a b='{random:integer()}'/>][1]", "");
    query("()[1][1]", "");
    query("()[last()][1]", "");

    // single item
    query("1[''][1]", "");
    query("1['a'][1]", 1);
    query("1[<a b='{random:integer()}'/>][1]", 1);

    query("1[0][1]", "");
    query("1[1][1]", 1);
    query("1[2][1]", "");
    query("1[last()][1]", 1);

    // numeric sequence
    query("(1 to 3)[''][1]", "");
    query("(1 to 3)['a'][1]", 1);
    query("(1 to 3)[<a b='{random:integer()}'/>][1]", 1);

    query("(1 to 3)[0][1]", "");
    query("(1 to 3)[1][1]", 1);
    query("(1 to 3)[3][1]", 3);
    query("(1 to 3)[4][1]", "");
    query("(1 to 3)[last()][1]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{.}'/>)[''][1]", "");
    query("((1 to 2) ! <a b='{.}'/>)['a'][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[<a b='{random:integer()}'/>][1]", "<a b=\"1\"/>");

    query("((1 to 2) ! <a b='{.}'/>)[0][1]", "");
    query("((1 to 2) ! <a b='{.}'/>)[1][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[2][1]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[3][1]", "");
    query("((1 to 2) ! <a b='{.}'/>)[last()][1]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void singlePosAsFirstPredicate() {
    // empty sequence
    query("()[1]['x']", "");
    query("()[1][<a b='{random:integer()}'/>]", "");
    query("()[1][1]", "");
    query("()[1][last()]", "");

    // single item
    query("1[1]['']", "");
    query("1[1]['a']", 1);
    query("1[1][<a b='{random:integer()}'/>]", 1);

    query("1[1][0]", "");
    query("1[1][1]", 1);
    query("1[1][2]", "");
    query("1[1][last()]", 1);

    // numeric sequence
    query("(1 to 3)[1]['']", "");
    query("(1 to 3)[1]['a']", 1);
    query("(1 to 3)[1][<a b='{random:integer()}'/>]", 1);

    query("(1 to 3)[1][0]", "");
    query("(1 to 3)[1][1]", 1);
    query("(1 to 3)[1][2]", "");
    query("(1 to 3)[1][last()]", 1);

    // XML sequence
    query("((1 to 2) ! <a b='{.}'/>)[1]['']", "");
    query("((1 to 2) ! <a b='{.}'/>)[1]['a']", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[1][<a b='{random:integer()}'/>]", "<a b=\"1\"/>");

    query("((1 to 2) ! <a b='{.}'/>)[1][0]", "");
    query("((1 to 2) ! <a b='{.}'/>)[1][1]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[1][2]", "");
    query("((1 to 2) ! <a b='{.}'/>)[1][last()]", "<a b=\"1\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void multiplePosAsLastPredicate() {
    // empty sequence
    query("()['x'][position() = 1 to 2]", "");
    query("()[<a b='{random:integer()}'/>][position() = 1 to 2]", "");
    query("()[1][position() = 1 to 2]", "");
    query("()[last()][position() = 1 to 2]", "");

    // single item
    query("1[''][position() = 1 to 2]", "");
    query("1['a'][position() = 1 to 2]", 1);
    query("1[<a b='{random:integer()}'/>][position() = 1 to 2]", 1);

    query("1[0][position() = 1 to 2]", "");
    query("1[1][position() = 1 to 2]", 1);
    query("1[2][position() = 1 to 2]", "");
    query("1[last()][position() = 1 to 2]", 1);

    // numeric sequence
    query("(1 to 3)[''][position() = 1 to 2]", "");
    query("(1 to 3)['a'][position() = 1 to 2]", "1\n2");
    query("(1 to 3)[<a b='{random:integer()}'/>][position() = 1 to 2]", "1\n2");

    query("(1 to 3)[0][position() = 1 to 2]", "");
    query("(1 to 3)[1][position() = 1 to 2]", 1);
    query("(1 to 3)[3][position() = 1 to 2]", 3);
    query("(1 to 3)[4][position() = 1 to 2]", "");
    query("(1 to 3)[last()][position() = 1 to 2]", 3);

    // XML sequence
    query("((1 to 2) ! <a b='{.}'/>)[''][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{.}'/>)['a'][position() = 1 to 2]", "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[<a b='{random:integer()}'/>][position() = 1 to 2]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("((1 to 2) ! <a b='{.}'/>)[0][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{.}'/>)[1][position() = 1 to 2]", "<a b=\"1\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[2][position() = 1 to 2]", "<a b=\"2\"/>");
    query("((1 to 2) ! <a b='{.}'/>)[3][position() = 1 to 2]", "");
    query("((1 to 2) ! <a b='{.}'/>)[last()][position() = 1 to 2]", "<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void variablePosAsFirstPredicate() {
    // empty sequence
    query("for $i in 1 to 2 return ()[$i]['x']", "");
    query("for $i in 1 to 2 return ()[$i][<a b='{random:integer()}'/>]", "");
    query("for $i in 1 to 2 return ()[$i][1]", "");
    query("for $i in 1 to 2 return ()[$i][last()]", "");

    // single item
    query("for $i in 1 to 2 return 1[$i]['']", "");
    query("for $i in 1 to 2 return 1[$i]['a']", 1);
    query("for $i in 1 to 2 return 1[$i][<a b='{random:integer()}'/>]", 1);

    query("for $i in 1 to 2 return 1[$i][0]", "");
    query("for $i in 1 to 2 return 1[$i][1]", 1);
    query("for $i in 1 to 2 return 1[$i][2]", "");
    query("for $i in 1 to 2 return 1[$i][last()]", 1);

    // numeric sequence
    query("for $i in 1 to 2 return (1 to 3)[$i]['']", "");
    query("for $i in 1 to 2 return (1 to 3)[$i]['a']", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)[$i][<a b='{random:integer()}'/>]", "1\n2");

    query("for $i in 1 to 2 return (1 to 3)[$i][0]", "");
    query("for $i in 1 to 2 return (1 to 3)[$i][1]", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)[$i][2]", "");
    query("for $i in 1 to 2 return (1 to 3)[$i][last()]", "1\n2");

    // XML sequence
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i]['']", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i]['a']",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i][<a b='{random:integer()}'/>]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i][0]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i][1]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i][2]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[$i][last()]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void variablePosAsLastPredicate() {
    // empty sequence
    query("for $i in 1 to 2 return ()['x'][$i]", "");
    query("for $i in 1 to 2 return ()[<a b='{random:integer()}'/>][$i]", "");
    query("for $i in 1 to 2 return ()[1][$i]", "");
    query("for $i in 1 to 2 return ()[last()][$i]", "");

    // single item
    query("for $i in 1 to 2 return 1[''][$i]", "");
    query("for $i in 1 to 2 return 1['a'][$i]", 1);
    query("for $i in 1 to 2 return 1[<a b='{random:integer()}'/>][$i]", 1);

    query("for $i in 1 to 2 return 1[0][$i]", "");
    query("for $i in 1 to 2 return 1[1][$i]", 1);
    query("for $i in 1 to 2 return 1[2][$i]", "");
    query("for $i in 1 to 2 return 1[last()][$i]", 1);

    // numeric sequence
    query("for $i in 1 to 2 return (1 to 3)[''][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)['a'][$i]", "1\n2");
    query("for $i in 1 to 2 return (1 to 3)[<a b='{random:integer()}'/>][$i]", "1\n2");

    query("for $i in 1 to 2 return (1 to 3)[0][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)[1][$i]", 1);
    query("for $i in 1 to 2 return (1 to 3)[3][$i]", 3);
    query("for $i in 1 to 2 return (1 to 3)[4][$i]", "");
    query("for $i in 1 to 2 return (1 to 3)[last()][$i]", 3);

    // XML sequence
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[''][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)['a'][$i]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[<a b='{random:integer()}'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"2\"/>");

    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[0][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[1][$i]", "<a b=\"1\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[2][$i]", "<a b=\"2\"/>");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[3][$i]", "");
    query("for $i in 1 to 2 return ((1 to 2) ! <a b='{.}'/>)[last()][$i]", "<a b=\"2\"/>");
  }

  /** Variable predicates. */
  @Test public void variablePreds() {
    // empty sequence
    query("for $i in (1,'a',2) return <a b='{ $i }'/>[$i]", "<a b=\"1\"/>\n<a b=\"a\"/>");
    query("for $i in (1,'a',2) return <a b='{ $i }'/>[<b c='{random:integer()}'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>");
    query("for $i in (1,'a',2) "
        + "return <a b='{ $i }'/>[<a b='{random:integer()}'/>][<b c='{random:integer()}'/>][$i]",
        "<a b=\"1\"/>\n<a b=\"a\"/>");
  }

  /** Positional access, rewritings. */
  @Test public void posAccess() {
    query("(<a/>,<b/>)[position() > 1]", "<b/>");
    query("(<a/>,<b/>,<c/>)[position() > 2]", "<c/>");
    query("(<a/>,<b/>,<c/>)[position() = 2 to 3]", "<b/>\n<c/>");

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
    query("collection('" + NAME + "')[2]", "<two/>");
    query("db:open('" + NAME + "')[2]", "<two/>");
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
    check(expr + "[position() = 0 to last()]", "<a/>\n<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 1 to last()]", "<a/>\n<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 2 to last()]", "<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 3 to last()]", "<c/>", empty(LAST));
    check(expr + "[position() = 1 to last() - 1]", "<a/>\n<b/>", empty(LAST));

    expr = "((<a/>, <b/>, <c/>)[. = ''])";
    check(expr + "[position() = 0 to last()]", "<a/>\n<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 1 to last()]", "<a/>\n<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 2 to last()]", "<b/>\n<c/>", empty(LAST));
    check(expr + "[position() = 3 to last()]", "<c/>", empty(LAST));
    check(expr + "[position() = 1 to last() - 1]", "<a/>\n<b/>", empty(LAST));
    check(expr + "[position() = 1 to last() - 2]", "<a/>", exists(LAST));
  }
}
