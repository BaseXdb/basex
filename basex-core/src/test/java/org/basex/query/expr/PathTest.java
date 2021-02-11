package org.basex.query.expr;

import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.basex.query.expr.path.*;
import org.basex.query.var.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for optimizations of the path expression (similar to {@link FilterTest}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class PathTest extends QueryPlanTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";
  /** First result. */
  private static final String LI1 = "<li>Exercise 1</li>";
  /** Second result. */
  private static final String LI2 = "<li>Exercise 2</li>";

  /** Creates a database. */
  @BeforeEach public void setUp() {
    execute(new CreateDB(NAME, FILE));
  }

  /** Drops the database. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
  }

  /** Filter expressions with a single predicate. */
  @Test public void onePredicate() {
    query("//ul/li['']", "");
    query("//ul/li['x']", LI1 + '\n' + LI2);
    query("//ul/li[<a b='{random:integer()}'/>]", LI1 + '\n' + LI2);

    query("//ul/li[0]", "");
    query("//ul/li[1]", LI1);
    query("//ul/li[2]", LI2);
    query("//ul/li[3]", "");
    query("//ul/li[last()]", LI2);
  }

  /** Following axis with multiple documents. */
  @Test public void following() {
    execute(new Add(NAME, FILE));
    query("(//ul)[1]/following::ul", "");
    query("//li/following::li", LI2 + '\n' + LI2);
  }

  /** Preceding axis with multiple documents. */
  @Test public void preceding() {
    execute(new Add(NAME, FILE));
    query("(//ul)[last()]/preceding::ul", "");
    query("(//ul)[1]/preceding::ul", "");
    query("//ul/preceding::ul", "");
    query("//li/preceding::li", LI1 + '\n' + LI1);
  }

  /** Filter expressions with two predicates (the last being a positional one). */
  @Test public void posAsLastPredicate() {
    // return first
    query("//ul/li[''][1]", "");
    query("//ul/li['x'][1]", LI1);
    query("//ul/li[<a b='{random:integer()}'/>][1]", LI1);

    query("//ul/li[0][1]", "");
    query("//ul/li[1][1]", LI1);
    query("//ul/li[3][1]", "");
    query("//ul/li[last()][1]", LI2);

    // return second
    query("//ul/li[''][2]", "");
    query("//ul/li['x'][2]", LI2);
    query("//ul/li[<a b='{random:integer()}'/>][2]", LI2);

    query("//ul/li[0][2]", "");
    query("//ul/li[1][2]", "");
    query("//ul/li[3][2]", "");
    query("//ul/li[last()][2]", "");

    // return last
    query("//ul/li[''][last()]", "");
    query("//ul/li['x'][last()]", LI2);
    query("//ul/li[<a b='{random:integer()}'/>][last()]", LI2);

    query("//ul/li[0][last()]", "");
    query("//ul/li[1][last()]", LI1);
    query("//ul/li[3][last()]", "");
    query("//ul/li[last()][last()]", LI2);

    // multiple positions
    query("//ul/li[''][position() = 1 to 2]", "");
    query("//ul/li['x'][position() = 1 to 2]", LI1 + '\n' + LI2);
    query("//ul/li[<a b='{random:integer()}'/>][position() = 1 to 2]", LI1 + '\n' + LI2);

    query("//ul/li[0][position() = 1 to 2]", "");
    query("//ul/li[1][position() = 1 to 2]", LI1);
    query("//ul/li[2][position() = 1 to 2]", LI2);
    query("//ul/li[3][position() = 1 to 2]", "");
    query("//ul/li[last()][position() = 1 to 2]", LI2);

    // variable position
    query("for $i in 1 to 2 return //ul/li[''][$i]", "");
    query("for $i in 1 to 2 return //ul/li['x'][$i]", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li[<a b='{random:integer()}'/>][$i]", LI1 + '\n' + LI2);

    query("for $i in 1 to 2 return //ul/li[0][$i]", "");
    query("for $i in 1 to 2 return //ul/li[1][$i]", LI1);
    query("for $i in 1 to 2 return //ul/li[2][$i]", LI2);
    query("for $i in 1 to 2 return //ul/li[3][$i]", "");
    query("for $i in 1 to 2 return //ul/li[last()][$i]", LI2);

    // variable predicates
    query("for $i in (1,'a') return //ul/li[''][$i]", "");
    query("for $i in (1,'a') return //ul/li['x'][$i]", LI1 + '\n' + LI1 + '\n' + LI2);
    query("for $i in (1,'a') return //ul/li[<a b='{random:integer()}'/>][$i]",
        LI1 + '\n' + LI1 + '\n' + LI2);

    query("for $i in (1,'a') return //ul/li[0][$i]", "");
    query("for $i in (1,'a') return //ul/li[1][$i]", LI1 + '\n' + LI1);
    query("for $i in (1,'a') return //ul/li[2][$i]", LI2 + '\n' + LI2);
    query("for $i in (1,'a') return //ul/li[3][$i]", "");
    query("for $i in (1,'a') return //ul/li[last()][$i]", LI2 + '\n' + LI2);
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void posAsFirstPredicate() {
    // return first
    query("//ul/li[1]['']", "");
    query("//ul/li[1]['x']", LI1);
    query("//ul/li[1][<a b='{random:integer()}'/>]", LI1);

    query("//ul/li[1][0]", "");
    query("//ul/li[1][1]", LI1);
    query("//ul/li[1][2]", "");
    query("//ul/li[1][last()]", LI1);

    // return second
    query("//ul/li[2]['']", "");
    query("//ul/li[2]['x']", LI2);
    query("//ul/li[2][<a b='{random:integer()}'/>]", LI2);

    query("//ul/li[2][0]", "");
    query("//ul/li[2][1]", LI2);
    query("//ul/li[2][2]", "");
    query("//ul/li[2][last()]", LI2);

    // return second
    query("//ul/li[3]['']", "");
    query("//ul/li[3]['x']", "");
    query("//ul/li[3][<a b='{random:integer()}'/>]", "");

    query("//ul/li[3][0]", "");
    query("//ul/li[3][1]", "");
    query("//ul/li[3][2]", "");
    query("//ul/li[3][last()]", "");

    // return last
    query("//ul/li[last()]['']", "");
    query("//ul/li[last()]['x']", LI2);
    query("//ul/li[last()][<a b='{random:integer()}'/>]", LI2);

    query("//ul/li[last()][0]", "");
    query("//ul/li[last()][1]", LI2);
    query("//ul/li[last()][2]", "");
    query("//ul/li[last()][last()]", LI2);

    // multiple positions
    query("//ul/li[position() = 1 to 2]['']", "");
    query("//ul/li[position() = 1 to 2]['x']", LI1 + '\n' + LI2);
    query("//ul/li[position() = 1 to 2][<a b='{random:integer()}'/>]", LI1 + '\n' + LI2);

    query("//ul/li[position() = 1 to 2][0]", "");
    query("//ul/li[position() = 1 to 2][1]", LI1);
    query("//ul/li[position() = 1 to 2][2]", LI2);
    query("//ul/li[position() = 1 to 2][3]", "");
    query("//ul/li[position() = 1 to 2][last()]", LI2);

    // variable position
    query("for $i in 1 to 2 return //ul/li[$i]['']", "");
    query("for $i in 1 to 2 return //ul/li[$i]['x']", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li[$i][<a b='{random:integer()}'/>]", LI1 + '\n' + LI2);

    query("for $i in 1 to 2 return //ul/li[$i][0]", "");
    query("for $i in 1 to 2 return //ul/li[$i][1]", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li[$i][2]");
    query("for $i in 1 to 2 return //ul/li[$i][last()]", LI1 + '\n' + LI2);

    // variable predicates
    query("for $i in (1,'a') return //ul/li[$i]['']", "");
    query("for $i in (1,'a') return //ul/li[$i]['x']", LI1 + '\n' + LI1 + '\n' + LI2);
    query("for $i in (1,'a') return //ul/li[$i][<a b='{random:integer()}'/>]",
        LI1 + '\n' + LI1 + '\n' + LI2);

    query("for $i in (1,'a') return //ul/li[$i][0]", "");
    query("for $i in (1,'a') return //ul/li[$i][1]", LI1 + '\n' + LI1);
    query("for $i in (1,'a') return //ul/li[$i][2]");
    query("for $i in (1,'a') return //ul/li[$i][last()]", LI1 + '\n' + LI2);
  }

  /** Caching of path expression results. */
  @Test public void gh1197() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<a><b/><b/></a>"));
    execute(new Add("a.xml", "<c><b/></c>"));
    query("//b[/a]", "<b/>\n<b/>");
  }

  /** Utilization of database statistics. */
  @Test public void gh1202() {
    execute(new CreateDB(NAME, "<x/>"));
    query("let $x := 'e' return element e {} / self::e[name() = $x]", "<e/>");
    query("let $x := 'f' return element f {} ! self::f[name() = $x]", "<f/>");
  }

  /** Retrieve double values from disk. */
  @Test public void gh1206() {
    execute(new CreateDB(NAME, "<x>a</x>"));
    query("/* castable as xs:double", false);
  }

  /** Index rewritings in nested XPath expressions. */
  @Test public void gh1210() {
    execute(new CreateDB(NAME, "<a><b>x</b></a>"));
    query("/a[b = .[b = 'x']/b]/b/text()", "x");
  }

  /** Single root expressions. */
  @Test public void gh1231() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<a/>"));
    execute(new Add("b.xml", "<b/>"));
    query(".[/a]", "<a/>");
    query(".[/b]", "<b/>");
  }

  /** Path to map rewritings. */
  @Test public void pathToMap() {
    query("<a/>[./name()]", "<a/>");
  }

  /** Path tests. */
  @Test public void gh1728() {
    query("<a/> ! (., .)/./(1, 2)[. = 1]", 1);
    query("<a/> ! (., .)/./1[. = 1]", 1);
  }

  /** Position checks. */
  @Test public void cmpPos() {
    check("<a/>/*[1]",
        "", exists(IterPosStep.class), exists(ItrPos.class));
    check("<a/>/*[position() = 1]",
        "", exists(IterPosStep.class), exists(ItrPos.class));
    check("for $i in 1 to 2 return <a/>/*[$i]",
        "", exists(IterPosStep.class), exists(VarRef.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i]",
        "", exists(IterPosStep.class), exists(Pos.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i to $i]",
        "", exists(IterPosStep.class), exists(Pos.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i to $i + 1]",
        "", exists(IterPosStep.class), exists(Pos.class));
    check("let $i := 1 return <a/>/*[position() = $i to $i + 1]",
        "", exists(IterPosStep.class), exists(ItrPos.class));
    check("let $i := 1 return <a/>/*[position() = 0 to $i]",
        "", exists(IterPosStep.class), exists(ItrPos.class));
    check("let $i := 0 return <a/>/*[position() = 1 to $i]",
        "", empty());
  }
}
