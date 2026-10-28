package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for optimizations of the path expression (similar to {@link FilterTest}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathTest extends SandboxTest {
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
    query("//ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI1 + '\n' + LI2);

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

    // attributes are never on the following axis
    query("parse-xml('<a><b x=\"1\" y=\"2\"><c/></b><d/></a>')//@x/following::node() ! name()",
        "c\nd");
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
    query("//ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][1]", LI1);

    query("//ul/li[0][1]", "");
    query("//ul/li[1][1]", LI1);
    query("//ul/li[3][1]", "");
    query("//ul/li[last()][1]", LI2);

    // return second
    query("//ul/li[''][2]", "");
    query("//ul/li['x'][2]", LI2);
    query("//ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][2]", LI2);

    query("//ul/li[0][2]", "");
    query("//ul/li[1][2]", "");
    query("//ul/li[3][2]", "");
    query("//ul/li[last()][2]", "");

    // return last
    check("//ul/li[last()]", LI2, exists(IterLastStep.class));
    check("//ul/li[''][last()]", "", empty());
    check("//ul/li['x'][last()]", LI2, exists(IterLastStep.class));
    check("//ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][last()]", LI2,
        exists(CachedStep.class));

    check("//ul/li[0][last()]", "", empty());
    check("//ul/li[1][last()]", LI1, exists(CachedStep.class));
    check("//ul/li[3][last()]", "", exists(CachedStep.class));
    check("//ul/li[last()][last()]", LI2, exists(CachedStep.class));

    // multiple positions
    query("//ul/li[''][position() = 1 to 2]", "");
    query("//ul/li['x'][position() = 1 to 2]", LI1 + '\n' + LI2);
    query("//ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]"
        + "[position() = 1 to 2]", LI1 + '\n' + LI2);

    query("//ul/li[0][position() = 1 to 2]", "");
    query("//ul/li[1][position() = 1 to 2]", LI1);
    query("//ul/li[2][position() = 1 to 2]", LI2);
    query("//ul/li[3][position() = 1 to 2]", "");
    query("//ul/li[last()][position() = 1 to 2]", LI2);

    // variable position
    query("for $i in 1 to 2 return //ul/li[''][$i]", "");
    query("for $i in 1 to 2 return //ul/li['x'][$i]", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]", LI1 + '\n' + LI2);

    query("for $i in 1 to 2 return //ul/li[0][$i]", "");
    query("for $i in 1 to 2 return //ul/li[1][$i]", LI1);
    query("for $i in 1 to 2 return //ul/li[2][$i]", LI2);
    query("for $i in 1 to 2 return //ul/li[3][$i]", "");
    query("for $i in 1 to 2 return //ul/li[last()][$i]", LI2);

    // variable predicates
    query("for $i in (1, 'a') return //ul/li[''][$i]", "");
    query("for $i in (1, 'a') return //ul/li['x'][$i]", LI1 + '\n' + LI1 + '\n' + LI2);
    query("for $i in (1, 'a') return //ul/li[<a b='{" + _RANDOM_INTEGER.args() + " }'/>][$i]",
        LI1 + '\n' + LI1 + '\n' + LI2);

    query("for $i in (1, 'a') return //ul/li[0][$i]", "");
    query("for $i in (1, 'a') return //ul/li[1][$i]", LI1 + '\n' + LI1);
    query("for $i in (1, 'a') return //ul/li[2][$i]", LI2 + '\n' + LI2);
    query("for $i in (1, 'a') return //ul/li[3][$i]", "");
    query("for $i in (1, 'a') return //ul/li[last()][$i]", LI2 + '\n' + LI2);
  }

  /** Filter expressions with two predicates (the first being a positional one). */
  @Test public void posAsFirstPredicate() {
    // return first
    query("//ul/li[1]['']", "");
    query("//ul/li[1]['x']", LI1);
    query("//ul/li[1][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI1);

    query("//ul/li[1][0]", "");
    query("//ul/li[1][1]", LI1);
    query("//ul/li[1][2]", "");
    query("//ul/li[1][last()]", LI1);

    // return second
    query("//ul/li[2]['']", "");
    query("//ul/li[2]['x']", LI2);
    query("//ul/li[2][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI2);

    query("//ul/li[2][0]", "");
    query("//ul/li[2][1]", LI2);
    query("//ul/li[2][2]", "");
    query("//ul/li[2][last()]", LI2);

    // return second
    query("//ul/li[3]['']", "");
    query("//ul/li[3]['x']", "");
    query("//ul/li[3][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", "");

    query("//ul/li[3][0]", "");
    query("//ul/li[3][1]", "");
    query("//ul/li[3][2]", "");
    query("//ul/li[3][last()]", "");

    // return last
    query("//ul/li[last()]['']", "");
    query("//ul/li[last()]['x']", LI2);
    query("//ul/li[last()][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI2);

    query("//ul/li[last()][0]", "");
    query("//ul/li[last()][1]", LI2);
    query("//ul/li[last()][2]", "");
    query("//ul/li[last()][last()]", LI2);

    // multiple positions
    query("//ul/li[position() = 1 to 2]['']", "");
    query("//ul/li[position() = 1 to 2]['x']", LI1 + '\n' + LI2);
    query("//ul/li[position() = 1 to 2]"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI1 + '\n' + LI2);

    query("//ul/li[position() = 1 to 2][0]", "");
    query("//ul/li[position() = 1 to 2][1]", LI1);
    query("//ul/li[position() = 1 to 2][2]", LI2);
    query("//ul/li[position() = 1 to 2][3]", "");
    query("//ul/li[position() = 1 to 2][last()]", LI2);

    // variable position
    query("for $i in 1 to 2 return //ul/li[$i]['']", "");
    query("for $i in 1 to 2 return //ul/li[$i]['x']", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li[$i]"
        + "[<a b='{" + _RANDOM_INTEGER.args() + " }'/>]", LI1 + '\n' + LI2);

    query("for $i in 1 to 2 return //ul/li[$i][0]", "");
    query("for $i in 1 to 2 return //ul/li[$i][1]", LI1 + '\n' + LI2);
    query("for $i in 1 to 2 return //ul/li[$i][2]");
    query("for $i in 1 to 2 return //ul/li[$i][last()]", LI1 + '\n' + LI2);

    // variable predicates
    query("for $i in (1, 'a') return //ul/li[$i]['']", "");
    query("for $i in (1, 'a') return //ul/li[$i]['x']", LI1 + '\n' + LI1 + '\n' + LI2);
    query("for $i in (1, 'a') return //ul/li[$i][<a b='{" + _RANDOM_INTEGER.args() + " }'/>]",
        LI1 + '\n' + LI1 + '\n' + LI2);

    query("for $i in (1, 'a') return //ul/li[$i][0]", "");
    query("for $i in (1, 'a') return //ul/li[$i][1]", LI1 + '\n' + LI1);
    query("for $i in (1, 'a') return //ul/li[$i][2]");
    query("for $i in (1, 'a') return //ul/li[$i][last()]", LI1 + '\n' + LI2);
  }

  /** Caching of path expression results. */
  @Test public void gh1197() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<a><b/><b/></a>"));
    execute(new Add("a.xml", "<c><b/></c>"));
    query("//b[/a]", "<b/>\n<b/>");
  }

  /** Caching of path expression results: constructed nodes. */
  @Test public void cacheConstructed() {
    query("declare %basex:inline(0) function local:f($n) { $n ! (<a><b/></a>/b) }; "
        + "let $r := (1 to 3) ! local:f(.) return ($r[1] is $r[2], $r[2] is $r[3])",
        "false\nfalse");
    query("let $r := { 'a': { 'b': 1 } } ! (for $i in 1 to 3 return a) "
        + "return ($r[1] is $r[2], $r[2] is $r[3])", "false\nfalse");
  }

  /** Caching of path expression results: free variables. */
  @Test public void cacheVariables() {
    query("for $d in (<x><a/></x>, <x><a/><a/></x>) return count((1 to 100)[. <= count($d/a)])",
        "1\n2");
    query("let $f := function($d) { count((1 to 100)[. <= count($d/a)]) } "
        + "return sum((1 to 20) ! $f(<x>{ (1 to .) ! <a/> }</x>))", 210);
    query("declare %basex:inline(0) function local:f($d, $k) { "
        + "if ($k > 0) then (count((1 to 100)[. <= count($d/a)]), "
        + "local:f(<x>{ $d/a, <a/> }</x>, $k - 1)) }; local:f(<x><a/></x>, 3)", "1\n2\n3");
  }

  /** Caching of path expression results: updated copies. */
  @Test public void cacheTransform() {
    query("declare %basex:inline(0) function local:f($n) { count($n ! /r/a) }; "
        + "copy $c := document { <r><a/></r> } "
        + "modify (if (sum((1 to 3) ! local:f($c/r)) = 3) then insert node <a/> into $c/r else ()) "
        + "return local:f($c/r)", 2);
    query("declare %basex:inline(0) function local:f($n) { count($n ! /r/a) }; "
        + "(document { <r><a/></r> } transform with { let $d := . return "
        + "if (sum((1 to 3) ! local:f($d)) = 3) then insert node <a/> into r else () }) "
        + "! local:f(.)", 2);
  }

  /** Utilization of database statistics. */
  @Test public void gh1202() {
    execute(new CreateDB(NAME, "<x/>"));
    query("let $x := 'e' return element e {} / self::e[name() = $x]", "<e/>");
    query("let $x := 'f' return element f {} ! self::f[name() = $x]", "<f/>");
  }

  /** Name comparisons in databases with prefixed names. */
  @Test public void nameWithPrefixes() {
    execute(new CreateDB(NAME, "<r xmlns:p='u'><p:city/><city/></r>"));
    query("//*[name() = 'city'] ! name()", "city");
    query("//*[name() = 'p:city'] ! name()", "p:city");
    query("//*[name() = ('city', 'p:city')] ! name()", "p:city\ncity");
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
        "", exists(IterPosStep.class), exists(IntPos.class));
    check("<a/>/*[position() = 1]",
        "", exists(IterPosStep.class), exists(IntPos.class));
    check("for $i in 1 to 2 return <a/>/*[$i]",
        "", exists(IterPosStep.class), exists(VarRef.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i]",
        "", exists(IterPosStep.class), exists(SimplePos.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i to $i]",
        "", exists(IterPosStep.class), exists(SimplePos.class));
    check("for $i in 1 to 2 return <a/>/*[position() = $i to $i + 1]",
        "", exists(IterPosStep.class), exists(SimplePos.class));
    check("let $i := 1 return <a/>/*[position() = $i to $i + 1]",
        "", exists(IterPosStep.class), empty(VarRef.class), exists(IntPos.class));
    check("let $i := 1 return <a/>/*[position() = 0 to $i]",
        "", exists(IterPosStep.class), empty(VarRef.class), exists(IntPos.class));
    check("let $i := 0 return <a/>/*[position() = 1 to $i]",
        "", empty());

    // positions below 1, maximum integer
    check("for $i in 0 to 1 return count(<a><b/><b/></a>/b[position() = $i - 5 to "
        + "$i + 9223372036854775806])", "2\n2", exists(SimplePos.class));
  }

  /** Union node tests. */
  @Test public void unionNodeTest() {
    final String el = "<x a='a'><e>e</e>t<!--c--><?p p?></x>";
    query(el + "/attribute::(document-node()|a|e|text()) ! string()", "a");
    query(el + "/@(a|processing-instruction()) ! string()", "a");
    query(el + "/attribute::(node()|processing-instruction()) ! string()", "a");
    query(el + "/child::(a|e|text()) ! string()", "e\nt");
    query(el + "/child::(a|e|processing-instruction()) ! string()", "e\np");
    query(el + "/child::(a|e|text()|comment()) ! string()", "e\nt\nc");
    query(el + "/descendant-or-self::(text()|*|comment()) ! string()", "et\ne\ne\nt\nc");

    check(el + "/child::(text()|text()) ! string()", "t",
        type(IterStep.class, "text()*"));
    check(el + "/child::(text()|e|text()) ! string()", "e\nt",
        type(IterStep.class, "(text()|element(e))*"));
  }

  /** Refine node tests. */
  @Test public void gh2464() {
    String[] tests = { empty(Instance.class), type(IterPath.class, "xnode()*"),
        "//IterStep/@test = 'xnode()'" };
    check("<a/>/node()[self::node()]", "", tests);
    check("<a/>/node()[. instance of node()]", "", tests);

    tests = new String[] { empty(Instance.class), type(IterPath.class, "element()*"),
        "//IterStep/@test = '*'" };
    check("<a/>/node()[self::*]", "", tests);
    check("<a/>/node()[. instance of element()]", "", tests);
    check("<a/>/*[. instance of element()]", "", tests);

    tests = new String[] { empty(Instance.class), type(IterPath.class, "element(a)*"),
        "//IterStep/@test = 'a'" };
    check("<a/>/node()[self::a]", "", tests);
    check("<a/>/node()[. instance of element(a)]", "", tests);
    check("<a/>/*[. instance of element(a)]", "", tests);
    check("<a/>/*[. instance of element(a)]", "", tests);
    check("<a/>/a[. instance of element(a)]", "", tests);
    check("<a/>/*[node-name() = #a]", "", tests);
    check("<a/>/a[node-name() = #a]", "", tests);

    check("<a/>/.[element()]", "", "//IterStep/@test = '*'");
    check("<a/>/.[*]", "", "//IterStep/@test = '*'");
    check("<a/>/.[a]", "", "//IterStep/@test = 'a'");
    check("<a/>/.[element(a)]", "", "//IterStep/@test = 'a'");
  }

  /** Instance checks: document-node(...). */
  @Test public void gh2521() {
    query("document {} instance of document-node()", true);
    query("document { <a/> } instance of document-node()", true);
    query("document { <a/>, <b/> } instance of document-node()", true);
    query("document { 'text' } instance of document-node()", true);

    query("document {} instance of document-node(*)", false);
    query("document { <a/> } instance of document-node(*)", true);
    query("document { <a/>, <b/> } instance of document-node(*)", false);
    query("document { 'text' } instance of document-node(*)", false);

    query("document {} instance of document-node(a)", false);
    query("document { <a/> } instance of document-node(a)", true);
    query("document { <a/>, <b/> } instance of document-node(a)", false);
    query("document { <b/> } instance of document-node(a)", false);
    query("document { 'text' } instance of document-node(a)", false);
  }

  /** Name tests must reject processing instructions. */
  @Test public void gh2764() {
    query("declare function local:f($done, $todo) {\n"
        + "  if(empty($todo)) then $done else local:f(($done, head($todo)), tail($todo))\n"
        + "};\n"
        + "local:f((), (<e/>, <?pi?>))/self::*", "<e/>");
  }

  /** Whitespace after the abbreviated attribute axis ({@code @}). */
  @Test public void gh2765() {
    query("<a b='c'/>/@ b", "b=\"c\"");
    query("<a b='c'/>/@ *", "b=\"c\"");
  }

  /** Keep name test when merging descendant-or-self step; preserve narrowed type of self step. */
  @Test public void gh2768() {
    check("declare function local:f($html as node()) {\n"
        + "  $html//text() except $html/descendant-or-self::b//node()\n"
        + "}; let $html := <a>X</a> return local:f($html)", "X",
        exists("IterStep[@test = 'b']"));
    check("declare function local:f($html as node()) { $html/self::b }; local:f#1", "local:f#1",
        type(IterStep.class, "(jnode(b)|element(b))?"));
  }

  /** Static subtyping of named document tests (child tests, not only the node kind). */
  @Test public void docTest() {
    // instance-of check via a typed argument (not a value): static instanceOf/intersect apply
    final String f = "declare function local:f($n as %type) as xs:boolean { $n instance of %of };"
        + " local:f(document { <a/> })";
    // differing child names: neither is an instance of the other
    query(f.replace("%type", "document-node(a)").replace("%of", "document-node(b)"), false);
    query(f.replace("%type", "document-node(a)").replace("%of", "document-node(a)"), true);
    // named child is an instance of the generic and the wildcard document test
    query(f.replace("%type", "document-node(a)").replace("%of", "document-node()"), true);
    query(f.replace("%type", "document-node(a)").replace("%of", "document-node(*)"), true);
    // element wildcard is not statically an instance of a named test, but they may intersect
    query(f.replace("%type", "document-node(*)").replace("%of", "document-node(a)"), true);
    query(f.replace("%type", "document-node(*)").replace("%of", "document-node(b)"), false);
  }
  /** Checks if descendant-or-self::node() steps are rewritten. */
  @Test public void mergeDesc() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("//*", null, "//@axis = 'descendant'");
    check("//(b, *)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b | *)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b | *)[text()]", null, exists(IterPath.class), empty(Union.class),
        "//@axis = 'descendant'");
    check("//(b, *)[1]", null, "not(//@axis = 'descendant')");
  }

  /** Checks if descendant steps are rewritten to child steps. */
  @Test public void descToChild() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("descendant::a", null, "//@axis = 'child'");
    check("descendant::b", null, "//@axis = 'child'");
    check("descendant::c", null, "//@axis = 'child'");
    check("descendant::*", null, "not(//@axis = 'child')");
  }

  /** Merge descendant steps. */
  @Test public void mergeDescendantSteps() {
    check("<a/>//descendant::*", "", count(IterPath.class, 1), count(IterStep.class, 1));
    check("<a/>//descendant::text()", "", count(IterPath.class, 1), count(IterStep.class, 1));
    check("<a/>//(descendant::a, descendant::b)", "",
        count(IterPath.class, 1), count(IterStep.class, 1));
  }

  /** Simplification of descendant-or-self and ancestor-or-self steps. */
  @Test public void descendantOrSelf() {
    check("document { <a/> }/descendant-or-self::a", "<a/>",
        exists("IterStep[@axis = 'descendant']"));
    check("<a/>/ancestor-or-self::document-node()", "",
        exists("IterStep[@axis = 'ancestor']"));
    check("document { <a b='c'/> }/descendant::a//@*", "b=\"c\"",
        exists("IterStep[@axis = 'descendant']"));
    check("<a b='c'/>/descendant-or-self::attribute()", "", root(Empty.class));
    check("document { <a/> }//self::a", "<a/>",
        exists("IterStep[@axis = 'descendant'][@test = 'a']"));

    check("(document {}, <a/>)/descendant-or-self::document-node()", "",
        exists("IterStep[@axis = 'self']"));

    check("(<a/> | text { 'a' })/ancestor-or-self::text()", "a",
        exists("IterStep[@axis = 'self']"));
    check("(<a/> | text { 'a' })/ancestor-or-self::comment()", "",
        exists("IterStep[@axis = 'self']"));
    check("(<a/> | text { 'a' })/ancestor-or-self::attribute()", "",
        exists("IterStep[@axis = 'self']"));
    check("(<a/> | text { 'a' })/ancestor-or-self::processing-instruction()", "",
        exists("IterStep[@axis = 'self']"));

    check("text { 'a' }/ancestor-or-self::text()", "a", root(CTxt.class));
    check("(<a/> | <b/>)/ancestor-or-self::text()", "", empty());
    check("document {}/descendant-or-self::document-node()", "", root(CDoc.class));
    check("(<a/> | <b/>)/descendant-or-self::document-node()", "", empty());

    // GH-2223: merge descendant-or-self step with the subsequent step
    check("<A><B/></A>/descendant-or-self::node()/child::*", "<B/>",
        count(IterStep.class, 1), "//@axis = 'descendant'");
    check("<A><B/></A>/descendant-or-self::node()/descendant::*", "<B/>",
        count(IterStep.class, 1), "//@axis = 'descendant'");
    check("<A/>/descendant-or-self::node()/descendant-or-self::*", "<A/>",
        count(IterStep.class, 1), "//@axis = 'descendant-or-self'");

    check("<A><B/></A>/descendant-or-self::node()/(* | text())", "<B/>",
        count(IterStep.class, 1), "//@axis = 'descendant'");
    check("<A><B/>X</A>/descendant-or-self::node()/(* | text())", "<B/>\nX",
        count(IterStep.class, 1), "//@axis = 'descendant'");
    check("<A><B/></A>/descendant-or-self::node()/(descendant::* | text())", "<B/>",
        count(IterStep.class, 1), "//@axis = 'descendant'");
    check("<A><B/></A>/descendant-or-self::node()/(* | descendant::text())", "<B/>",
        count(IterStep.class, 1), "//@axis = 'descendant'");

    check("<A><B/></A>/descendant-or-self::node()/(* | text())[..]", "<B/>",
        count(IterStep.class, 2), "//@axis = 'descendant'");
    check("<A><B/>X</A>/descendant-or-self::node()/(* | text())[..]", "<B/>\nX",
        count(IterStep.class, 2), "//@axis = 'descendant'");
    check("<A><B/></A>/descendant-or-self::node()/(descendant::* | text())[..]", "<B/>",
        count(IterStep.class, 2), "//@axis = 'descendant'");
    check("<A><B/></A>/descendant-or-self::node()/(* | descendant::text())[..]", "<B/>",
        count(IterStep.class, 2), "//@axis = 'descendant'");

    query("<a>X</a>/descendant-or-self::b//node()", "");
    query("declare function local:f($html as node()) {"
        + "  $html//text() except $html/descendant-or-self::b//node()"
        + "};"
        + "let $html := <a>X</a>"
        + "return local:f($html)", "X");
  }

  /** Ancestor steps on database and fragment nodes. */
  @Test public void ancestorSteps() {
    query("<a>{ (<b><c/></b> update {})/c }</a>/c/ancestor::*", "<a><c/></a>");
  }

  /** Remove redundant self steps. */
  @Test public void selfSteps() {
    check("<a/>/.", "<a/>", root(CElem.class));
    check("<a/>/./././.", "<a/>", root(CElem.class));
    check("<a/>[.]", "<a/>", root(CElem.class));
    check("<a/>/self::element()", "<a/>", root(CElem.class));
    check("attribute a { 0 }/self::attribute()", "a=\"0\"", root(CAttr.class));
    check("<a/>/self::*", "<a/>", root(CElem.class));

    // a self step yields at most one result
    check("<a/>[count(self::*) = 1]", "<a/>", root(CElem.class));
    check("<a/>[self::* = self::*]", "<a/>", root(CElem.class));
  }

  /** Merge adjacent steps in path expressions. */
  @Test public void mergeSteps() {
    // merge self steps
    check("<a/>/self::*/self::a", "<a/>", root(CElem.class));
    check("<a/>/self::*/self::Q{}a", "<a/>", root(CElem.class));

    check("<a/>/self::*/self::b", "", empty());
    check("<a/>/self::Q{}*/self::a", "<a/>", root(CElem.class));
    check("<a/>/self::a/self::*", "<a/>", root(CElem.class));
    check("<a/>/self::a/self::node()", "<a/>", root(CElem.class));

    // merge descendant and self steps
    check("document { <a/> }//self::a", "<a/>", count(IterStep.class, 1));
    check("document { <a/> }//*/self::a", "<a/>", count(IterStep.class, 1));

    // combined kind tests
    check("document { <a/>, <b/> }/(a, b)/self::a", "<a/>", count(IterStep.class, 1));
    check("document { <a/>, <b/> }/a/(self::a, self::b)", "<a/>", count(IterStep.class, 1));
    check("document { <a/>, <b/> }/(a, b)/(self::b, self::a)", "<a/>\n<b/>",
        count(IterStep.class, 1));
  }

  /** Merge steps and predicates with self steps. */
  @Test public void mergeSelfPredicates() {
    // merge self steps
    check("<a/>/self::*[self::a]", "<a/>", root(CElem.class));
    check("<a/>/self::*[self::b]", "", empty());
    check("<a/>/self::a[self::*]", "<a/>", root(CElem.class));
    check("<a/>/self::a[self::node()]", "<a/>", root(CElem.class));

    // nested predicates
    check("<a/>/self::a[self::a[self::a[self::a]]]", "<a/>", root(CElem.class));

    // combined kind test
    check("document { <a/>, <b/> }/a[self::a | self::b]", "<a/>", count(IterStep.class, 1));

    // merge self steps of predicates with the enclosing step
    check("<a/>/*[self::b]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[self::b and true()]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[self::b][true()]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
  }

  /** Combined kind tests. */
  @Test public void combinedKindTests() {
    // merge identical steps, rewrite to iterative path
    check("<a/>/(* | *)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(*, *)",  "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a | b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)",  "", root(IterPath.class), empty(List.class));

    // merge descendant-or-self step, rewrite to iterative path
    check("<a/>//(a | b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)",   "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a | b)[text()]", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)[text()]",  "", root(IterPath.class), empty(List.class));
    check("<_><a>x</a><b/></_>/(a, b)[text()]", "<a>x</a>",
        root(IterPath.class), empty(List.class));

    // rewrite to union expression
    check("<a/>/(*, @*)", "", root(MixedPath.class), exists(Union.class));
  }

  /** Rewrite name tests to self steps. */
  @Test public void nameTests() {
    check("<a/>[node-name() eq xs:QName('a')]", "<a/>", root(CElem.class));
    check("<a/>[local-name() eq 'a']", "<a/>", root(CElem.class));

    check("<a/>[local-name() = ('a', 'b', '')]", "<a/>", root(CElem.class));
    check("<a/>[local-name() = 'a' or local-name() = 'b']", "<a/>", root(CElem.class));
    check("<a/>[node-name() = (xs:QName('a'), xs:QName('b'))]", "<a/>", root(CElem.class));
    check("<a/>[local-name() = ('a', 'a', 'a')]", "<a/>", root(CElem.class));

    check("(<a/>, <b/>)[. = '!'][local-name() = 'a']", "", empty(LOCAL_NAME));

    check("comment {}[local-name() = '']", "<!---->", root(CComm.class));
    check("text { 'a' }[local-name() = '']", "a", root(CTxt.class));

    final String prolog = "declare default element namespace 'A'; ";
    check(prolog + "<a/>[node-name() eq QName('A', 'a')]",
        "<a xmlns=\"A\"/>", root(CElem.class));
    check(prolog + "<a/>[namespace-uri() eq 'A']",
        "<a xmlns=\"A\"/>", root(CElem.class));

    // no rewritings
    check("<a/>[local-name() != 'a']", "", exists(LOCAL_NAME));
    check("<a/>[local-name() =" + wrap("a") + "]", "<a/>", exists(LOCAL_NAME));
    check("<a/>[node-name() = xs:QName(" + wrap("a") + ")]", "<a/>", exists(NODE_NAME));
    check("parse-xml('<a/>')[name(*) = 'a']", "<a/>", exists(Function.NAME));

    // name tests with variable references
    check("let $x := 'g' return <g/>[name() = $x]", "<g/>",
        root(IterFilter.class), exists(IterFilter.class));
    check("let $x := 'g' return <g/> ! self::g[name() = $x]", "<g/>",
        root(IterPath.class));
    check("let $x := 'g' return <g/> ! self::*[local-name() = $x]", "<g/>",
        root(CElem.class));
    check("let $x := 'g' return <g/> ! *[local-name() = $x]", "",
        root(IterPath.class));
  }

  /** Static typing: Intersection of name tests. */
  @Test public void nameTestIntersection() {
    final String xml = "<a xmlns=\"x\"/>";
    query(xml + "[self::Q{x}a[local-name() = 'a'][namespace-uri() = 'x']]", xml);
    query(xml + "[self::*:a[local-name() = 'a'][namespace-uri() = 'x']]", xml);
    query(xml + "[self::*[local-name() = 'a'][namespace-uri() = 'x']]", xml);
    query(xml + "[self::a[local-name() = 'a'][namespace-uri() = 'x']]", "");

    query("<_><n/></_>/Q{}n instance of element(Q{}n)", true);
    query("<_><n/></_>/Q{}n instance of element(Q{}o)", false);
    query("<_><n/></_>/Q{}n instance of element(n)   ", true);
    query("<_><n/></_>/Q{}n instance of element(o)   ", false);
    query("<_><n/></_>/Q{}n instance of element()    ", true);

    query("<_><n/></_>/Q{}* instance of element(Q{}n)", true);
    query("<_><n/></_>/Q{}* instance of element(Q{}o)", false);
    query("<_><n/></_>/Q{}* instance of element(n)   ", true);
    query("<_><n/></_>/Q{}* instance of element(o)   ", false);
    query("<_><n/></_>/Q{}* instance of element()    ", true);

    query("<_><n/></_>/*:n  instance of element(Q{}n)", true);
    query("<_><n/></_>/*:n  instance of element(Q{}o)", false);
    query("<_><n/></_>/*:n  instance of element(n)   ", true);
    query("<_><n/></_>/*:n  instance of element(o)   ", false);
    query("<_><n/></_>/*:n  instance of element()    ", true);

    query("<_><n/></_>/n    instance of element(Q{}n)", true);
    query("<_><n/></_>/n    instance of element(Q{}o)", false);
    query("<_><n/></_>/n    instance of element(n)   ", true);
    query("<_><n/></_>/n    instance of element(o)   ", false);
    query("<_><n/></_>/n    instance of element()    ", true);

    query("<_><n/></_>/*    instance of element(Q{}n)", true);
    query("<_><n/></_>/*    instance of element(Q{}o)", false);
    query("<_><n/></_>/*    instance of element(n)   ", true);
    query("<_><n/></_>/*    instance of element(o)   ", false);
    query("<_><n/></_>/*    instance of element()    ", true);
  }

  /** The node test of a step is adopted from the type of its result. */
  @Test public void refinedTest() {
    execute(new CreateDB(NAME, "<x>t<!--c--><y>u</y></x>"));
    final String db = _DB_GET.args(NAME);

    // kind tests: the result type carries no test of its own
    check(db + "//text()", "t\nu", exists(IterPath.class));
    check(db + "//comment()", "<!--c-->", exists(IterPath.class));
    // the type is narrowed by a predicate, and the test is adopted
    check(db + "//node()[self::text()]", "t\nu", exists(IterPath.class));
  }

  /** Static optimizations of paths without results (see also GH-1630). */
  @Test public void emptyPath() {
    // steps after a step without results are dropped: A/void(.)/B → A/void(.)
    check("<a/>/void(.)/b", "", empty(IterPath.class));

    // check combination of axis and node test and axis
    check("<e a='A'/>/attribute::text()", "", empty());
    check("<e a='A'/>/attribute::attribute()", "a=\"A\"", exists(IterPath.class));
    check("<e a='A'/>/ancestor::text()", "", empty());
    check("<e a='A'/>/parent::text()", "", empty());
    check("<e a='A'/>/parent::*", "", exists(IterPath.class));
    check("attribute a { 0 }/child::attribute()", "", empty());
    check("<e a='A'/>/attribute::a/child::attribute()", "", empty());

    // check step after expression that yields document nodes
    check("document { <a/> }/self::*", "", empty());
    check("document { <a/> }/self::*", "", empty());
    check("document { <a/> }/self::text()", "", empty());

    check("document { <a/> }/child::document-node()", "", empty());
    check("document { <a/> }/child::attribute()", "", empty());
    check("document { <a/> }/child::*", "<a/>", exists(IterPath.class));

    check("document { <a/> }/descendant-or-self::attribute()", "", empty());
    check("document { <a/> }/parent::node()", "", empty());
    check("document { <a/> }/ancestor::node()", "", empty());
    check("document { <a/> }/following::node()", "", empty());
    check("document { <a/> }/preceding-sibling::node()", "", empty());

    // skip further tests if previous node type is unknown, or if current test accepts all nodes
    check("(<a/>," + wrap(1) + "[. = 0])/node()", "", exists(IterStep.class));

    // check step after any other expression
    check("<a/>/self::text()", "", empty());
    check("comment {}/child::node()", "", empty());
    check("text { 0 }/child::node()", "", empty());
    check("attribute a { 0 }/following-sibling::node()", "", empty());
    check("attribute a { 0 }/preceding-sibling::node()", "", empty());
    check("comment {}/following-sibling::node()", "", exists(IterPath.class));
    check("comment {}/preceding-sibling::node()", "", exists(IterStep.class));

    check("attribute a { 0 }/child::node()", "", empty());
    check("attribute a { 0 }/descendant::*", "", empty());
    check("attribute a { 0 }/self::*", "", empty());

    // namespaces
    check("(<a/>, comment{})/child::namespace-node()", "", empty());
    check("(<a/>, comment{})/descendant::namespace-node()", "", empty());
    check("(<a/>, comment{})/attribute::namespace-node()", "", empty());
    check("(<a/>, comment{})/self::namespace-node()", "", exists(IterStep.class));
    check("(<a/>, comment{})/descendant-or-self::namespace-node()", "", exists(IterStep.class));

    // the root expression is discarded as well
    check("document {}/..", "", empty(CDoc.class));
    check("function() { document {}/.. }()", "", empty(CDoc.class));
    check("declare function local:f() { document {}/.. }; local:f()", "", empty(CDoc.class));
  }

  /** Axis steps with node kind tests. */
  @Test public void axisNodeKinds() {
    check("document {}/parent::node()", "", root(Empty.class));

    check("attribute a {}/child::document-node()", "", root(Empty.class));
    check("attribute a {}/self::document-node()", "", root(Empty.class));
    check("attribute a {}/descendant-or-self::document-node()", "", root(Empty.class));
    check("attribute a {}/descendant-or-self::*", "", root(Empty.class));

    check("text { '' }/child::document-node()", "", root(Empty.class));
    check("text { '' }/self::document-node()", "", root(Empty.class));
    check("text { '' }/descendant-or-self::document-node()", "", root(Empty.class));
    check("text { '' }/descendant-or-self::*", "", root(Empty.class));

    check("document {}/ancestor-or-self::node()", "", root(CDoc.class));
    check("attribute a {}/descendant-or-self::attribute()", "a=\"\"", root(CAttr.class));
    check("text { '' }/descendant-or-self::text()", "", root(CTxt.class));

    check("text { '' }[self::node()]", "", root(CTxt.class));
    check("text { '' }[self::text()]", "", root(CTxt.class));
    check("text { '' }[descendant-or-self::text()]", "", root(CTxt.class));
    check("document {}[ancestor-or-self::node()]", "", root(CDoc.class));
    check("document {}[ancestor-or-self::document-node()]", "", root(CDoc.class));

    check("text { '' }[self::element()]", "", empty());
    check("document {}[ancestor-or-self::element()]", "", empty());
  }

  /** Sibling axes of database attributes. */
  @Test public void attributeSiblings() {
    final String atts = "parse-xml('<a b=\"\" c=\"\">t<d/></a>')//@* ! "
        + "(if(random:double() < 2) then . else 'x')";
    query("(" + atts + ")/following-sibling::node()", "");
    query("(" + atts + ")/preceding-sibling::node()", "");
    query("(" + atts + ")/following-sibling-or-self::node() ! name()", "b\nc");
  }

  /** Checks if iterative evaluation is used if no duplicates occur. */
  @Test public void iterativePath() {
    execute(new CreateDB(NAME, "<a id='0' x:id='' x='' xmlns:x='x'><b id='1'/><c id='2'/>"
        + "<d id='3'/><e id='4'/></a>"));
    check("(/a/*/../*) ! name()", "b\nc\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/../*) ! name()", "b\nc\nd\ne", exists(IterPath.class));
    check("(/a/*/following::*) ! name()", "c\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/following::*) ! name()", "c\nd\ne", exists(IterPath.class));
    check("(/a/*/following-sibling::*) ! name()", "c\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/following-sibling::*) ! name()", "c\nd\ne", exists(IterPath.class));
    check("(/*/@id/../*) ! name()", "b\nc\nd\ne", exists(IterPath.class));
    check("(exactly-one(/a)/@id/../*) ! name()", "b\nc\nd\ne", exists(IterPath.class));
  }

  /** Rewrite lists to unions. */
  @Test public void listToUnion() {
    check("<a/>[b, text()]", "", count(SingleIterPath.class, 1),
        type(IterStep.class, "(element(b)|text())*"));

    // union expression will be further rewritten to single path
    check("<a/>[b, c]", "", empty(List.class), count(SingleIterPath.class, 1));
    check("<a/>[(b | c) = '']", "", empty(List.class), count(SingleIterPath.class, 1));
    check("<a/>[(b | c) = (b | c)]", "", empty(List.class), count(SingleIterPath.class, 2));

    // list in the root of a path expression
    check("<a/>[(b, c)/d]", "", empty(List.class), count(IterPath.class, 1));

    // do not rewrite paths that yield no nodes
    check("(<a/>, <b/>)/name()", "a\nb", exists(List.class));
    check("let $_ := <_/> return ($_, $_)/0", "0\n0", exists(SingletonSeq.class));
  }

  /** Remove redundant paths and predicates. */
  @Test public void redundantPaths() {
    check("<a/>/*[*]/*", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a>X</a>/text()[..]/..", "<a>X</a>", empty(SingleIterPath.class));

    // no rewriting
    check("<a/>/*[*]/text()", "", count(IterPath.class, 1), exists(SingleIterPath.class));

    check("<a/>/*[a]/a", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a]/a/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a/b]/a/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a]/a[b]/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));

    // GH-2193: Bug on child node selection
    query("<X><a><b><c/></b><d/></a></X>/*[*/*]/*", "<b><c/></b>\n<d/>");
    check("<X/>/*[a/b]/a", "", count(IterPath.class, 2));
    check("<X/>/*[a/b]/a/c", "", count(IterPath.class, 2));
  }

  /** Check existence of paths in predicates. */
  @Test public void pathExistence() {
    execute(new CreateDB(NAME, "<a><b/></a>"));
    check("b", "", empty());
    check("a/a", "", empty());
    check("a/a[b]", "", empty());
    check("a[a]", "", empty());

    check("a[/b]", "", empty());
    check("a[/a/a]", "", empty());
    check("a[/a[/b]]", "", empty());
    check("a[/a/" + _UTIL_ROOT.args(" .") + "/b]", "", empty());
    check("a[" + _UTIL_ROOT.args(" .") + "/a/" + _UTIL_ROOT.args(" .") + "/b]", "", empty());

    check("a[/a]", "<a><b/></a>", root(IterPath.class), exists(DBNode.class));

    // GH-2121: nested database node paths
    execute(new CreateDB(NAME, "<x><x/></x>"));
    query("x[x/(text() | *)]", "");
  }

  /** Nested predicates, axis checks. */
  @Test public void nestedPredicates() {
    query("<doc><a/><b/><a/></doc>/a[following::*[1]/self::a]", "");
    query("<p><b/></p>/*[.[self::a union self::b/@x]]", "");
  }

  /** Steps with mixed operands. */
  @Test public void steps() {
    query("<a/>[./(@*)]", "");
    query("<_><x><x>A</x>B</x></_>//x/node()[last()] ! string()", "A\nB");
    query("<a><b/><b/></a>/b/last()", "2\n2");
    error("<local:a/>/self::local :*", QUERYEND_X);
  }

  /** Identical nodes in mixed paths. */
  @Test public void mixedPaths() {
    // the declared context item is only adopted if no database is opened
    execute(new Close());

    check("declare context item := <a/>; (.|.)/self::a", "<a/>", empty(Union.class));

    check("<a/> ! (., .)/<b/>", "<b/>\n<b/>", exists(REPLICATE));
    check(REPLICATE.args(" <a/>", 2) + "/<b/>", "<b/>\n<b/>", exists(REPLICATE));
    check("declare context item := <a/>; (., .)/<b/>", "<b/>\n<b/>", exists(SingletonSeq.class));

    // steps that yield nodes and atomic items
    error("(<a/>, <b/>)/(if(name() = 'a') then <a/> else 2)/.", PATHNODE_X_X_X);
  }

  /** Distinct document order. */
  @Test public void distinctDocumentOrder() {
    check("let $a := <a/> return" + DISTINCT_ORDERED_NODES.args(" ($a, $a)"),
        "<a/>", root(CElem.class));
    check("let $a := <a/> return ($a, $a)/.",
        "<a/>", root(CElem.class));
  }

  /** Removed self steps must not drop the document order of generalized nodes. */
  @Test public void gnodeDocumentOrder() {
    final String kinds = " ! (if(. instance of jnode()) then 'J' else 'X')";
    final String seq = "let $s := (jtree({ 'a': 1 }), <x/>) return string-join(";
    query(seq + "$s/self::node()" + kinds + ")", "XJ");
    query(seq + DISTINCT_ORDERED_NODES.args(" $s") + kinds + ")", "XJ");
    query(seq + "($s | $s)" + kinds + ")", "XJ");
    query(seq + "reverse($s)/self::node()" + kinds + ")", "XJ");
  }

  /** Filter expression, node order. */
  @Test public void nodeOrder() {
    query("let $f := fn { true() } "
        + "let $a := sort((<x>2</x>, <x>1</x>)) "
        + "let $b := $a[$f(.)] "
        + "return ($a, $b)",
        "<x>1</x>\n<x>2</x>\n<x>1</x>\n<x>2</x>");
    query("let $f := fn { true() } "
        + "return sort((<x>2</x>, <x>1</x>)) -> (., .[$f(.)])",
        "<x>1</x>\n<x>2</x>\n<x>1</x>\n<x>2</x>");
  }

  /** Rewrite simple map to path. */
  @Test public void simpleMapToPath() {
    execute(new CreateDB(NAME, "<xml><a/></xml>"));
    check("/xml ! a", "<a/>", root(IterPath.class));
    check("<a/> ! a ! b ! c ! d ! e", "", root(IterPath.class), empty(IterMap.class));
    check("<a/> ! a ! descendant::x", "", root(DualIterMap.class), exists(IterPath.class));
  }

  /** Optimize inlined path steps. */
  @Test public void inlinedSteps() {
    execute(new CreateDB(NAME, "<_>X</_>"));

    inline(true);
    check("function($db) { $db/UNKNOWN }(.)", "", empty());
    check("let $f := function($a) { $a/UNKNOWN } return ./$f(.)", "", empty());
    check("function($db) { $db/_[. = 'X'] }(.)", "<_>X</_>", root(ValueAccess.class));
  }

  /** Axis steps, better typing. */
  @Test public void stepTyping() {
    // fragments
    inline(true);
    check("function() as element(a)? { <a/>/self::a }()",
        "<a/>", root(CElem.class));
    check("function() as element(a)? { <a/>/self::Q{}a }()",
        "<a/>", root(CElem.class));
    check("function() as element(Q{}a)? { <a/>/self::Q{}a }()",
        "<a/>", root(CElem.class));

    // database nodes
    execute(new CreateDB(NAME, "<x>A</x>"));
    error("function() as element(x)* { x }()[text() = 'A']", NOCTX_X);
    error("function() as element(x)* { /x }()[text() = 'A']", NOCTX_X);
    check("function() as element(x)* {" + _DB_GET.args(NAME) + "/x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as element(x) {" + _DB_GET.args(NAME) + "/x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as document-node() {" + _DB_GET.args(NAME) + " }()/x[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));

    // no rewriting allowed
    check("function() as element(a)? { <a/>/self::*:a }()",
        "<a/>", root(CElem.class));
    check("function() as element(xml:a)? { <xml:a/>/self::xml:* }()",
        "<xml:a/>", root(CElem.class));
    error("function() as element(a)? { <xml:a/>/self::xml:a }()", INVTYPE_X);

    // axis followed by attribute step
    check("<x/>//@*", null, type("IterStep[@axis = 'descendant-or-self']", "element()*"));
    check("<x/>/../@*", null, type("IterStep[@axis = 'parent']", "element()?"));

    // steps with zero or one results
    check("<a/>/*[1]/*[1]", "", type(IterPath.class, "element()?"));
    check("(1 to 10000000) ! tail(<a/>/*[1])", "", empty());
  }

  /** Conditional steps with self, child and attribute branches. */
  @Test public void conditionalSteps() {
    final String iterative = "exists(//MixedPath[@iterative = 'true'])";
    final String cached = "empty(//MixedPath[@iterative = 'true'])";
    check("<a><b x=''><c/><d/></b><b/></a>/b/(if (@x) then * else .) ! name()", "c\nd\nb",
        iterative);
    check("<a><b x='1'/><b y='2'/></a>/b/(if (@x) then @x else @y) ! string()", "1\n2",
        iterative);
    check("jtree({ 'A': [ { 'B': 1 }, { 'B': 2 } ], 'C': { 'B': 3 } })/*"
        + "/(if (self::jnode(*, array(*))) then * else .)/B ! jvalue()", "1\n2\n3", iterative);
    check("head(<a>{ (1 to 100000) ! <b/> }</a>/(if (b) then b else .)) ! name()", "b",
        iterative);
    check("<a><b t='x'><c/></b><b t='y' z=''/><b/></a>/b/(switch (@t) case 'x' return * "
        + "case 'y' return @z default return .) ! name()", "c\nz\nb", iterative);
    check("<a><b><c/></b><d z=''/><e/></a>/*/(typeswitch (.) case element(b) return * "
        + "case element(d) return @z default return .) ! name()", "c\nz\ne", iterative);

    // results must be sorted: nested contexts, parent axis, multiple steps
    check("<x><a n='1'><a n='2'><b n='3'/></a><b n='4'/></a></x>//a/(if (*) then * else .)"
        + " ! string(@n)", "2\n3\n4", cached);
    check("<a><b/><b/></a>/b/(if (@x) then .. else .) ! name()", "b\nb", cached);
    check("<a><c/><b/></a>/(if (*) then (b, c) else .) ! name()", "c\nb", cached);
    check("<a><b x='1'/><b><c y='2'/></b></a>/b/(if (@x) then @x else c/@y) ! string()",
        "1\n2", cached);
    check("<a><b><c/></b><d/></a>/*/(typeswitch (.) case $b as element(b) return $b/* "
        + "case element(d) return @z default return .) ! name()", "c", cached);
    // positional access
    check("<a><b n='1'/><b n='2'/></a>/b/(if (position() = 1) then . else ()) ! string(@n)",
        "1", cached);
  }

  /** Name tests on all axes of persistent, main-memory and updated databases. */
  @Test public void dbAxes() {
    final StringBuilder xml = new StringBuilder("<x>");
    for(int i = 0; i < 10; i++) {
      xml.append("<a n='1'><b/>t<a><b m='2'/>u</a><!--c--><?p i?></a><b n='3'><a/></b>v");
    }
    xml.append("</x>");
    final String[] axes = { "ancestor", "ancestor-or-self", "attribute", "child", "descendant",
      "descendant-or-self", "following", "following-or-self", "following-sibling",
      "following-sibling-or-self", "parent", "preceding", "preceding-or-self", "preceding-sibling",
      "preceding-sibling-or-self", "self" };
    final String db = _DB_GET.args(NAME), nodes = "(" + db + "/descendant-or-self::node(), " + db
        + "//@*) ! (%s ! (count(ancestor::node()) || ':' || count(preceding::node())))";

    for(final boolean mainmem : new boolean[] { false, true }) {
      set(MainOptions.MAINMEM, mainmem);
      try {
        execute(new CreateDB(NAME, xml.toString()));
        execute(new Add("doc2.xml", xml.toString()));
        for(int u = 0; u < 2; u++) {
          for(final String axis : axes) {
            for(final String name : new String[] { "a", "b", "n", "m", "x" }) {
              query("deep-equal(" + String.format(nodes, axis + "::" + name) + ", "
                  + String.format(nodes, axis + "::*[name() = '" + name + "']") + ')', true);
            }
          }
          query("insert node <a n='4'><b/></a> after (" + db + "//b)[5]");
        }
      } finally {
        set(MainOptions.MAINMEM, false);
      }
    }
  }

  /** Name tests on all axes of fragments with embedded database nodes. */
  @Test public void fragmentAxes() {
    final String xml = "<x><a n='1'><b/>t<a><b m='2'/>u</a><!--c--><?p i?></a><b n='3'><a/></b>v"
        + "<a n='1'><b/>t<a><b m='2'/>u</a></a></x>";
    final String[] axes = { "ancestor", "ancestor-or-self", "attribute", "child", "descendant",
      "descendant-or-self", "following", "following-or-self", "following-sibling",
      "following-sibling-or-self", "parent", "preceding", "preceding-or-self", "preceding-sibling",
      "preceding-sibling-or-self", "self" };
    final String nodes = "let $f := document { parse-xml(\"" + xml + "\")/node() } "
        + "return ($f/descendant-or-self::node(), $f//@*) ! "
        + "(%s ! (count(ancestor::node()) || ':' || count(preceding::node())))";

    for(final String axis : axes) {
      for(final String name : new String[] { "a", "b", "n", "m", "x" }) {
        query("deep-equal(" + String.format(nodes, axis + "::" + name) + ", "
            + String.format(nodes, axis + "::*[name() = '" + name + "']") + ')', true);
      }
    }
  }

  /** Name tests in child and descendant steps of persistent, main-memory and updated databases. */
  @Test public void dbNameTests() {
    final StringBuilder xml = new StringBuilder("<x>");
    for(int i = 1; i <= 10000; i++) xml.append("<a>").append(i).append("</a><b/>t");
    final String x = _DB_GET.args(NAME) + "/x", check = "let $x := " + x + " return ("
        + "deep-equal($x/a, $x/node()[name() = 'a']), deep-equal($x//a, $x//node()[name() = 'a']))";

    for(final boolean mainmem : new boolean[] { false, true }) {
      set(MainOptions.MAINMEM, mainmem);
      try {
        execute(new CreateDB(NAME, xml + "</x>"));
        query("count(" + x + "/a)", 10000);
        query(check, "true\ntrue");
        query(x + "/a[5000] ! (insert node <a>0</a> before ., "
            + "insert node (1 to 5000) ! <b/> after .)");
        query("count(" + x + "/a)", 10001);
        query(check, "true\ntrue");
        query("delete node " + x + "/a[position() mod 3 = 0]");
        query("count(" + x + "/a)", 6668);
        query(check, "true\ntrue");
      } finally {
        set(MainOptions.MAINMEM, false);
      }
    }
  }

  /** Database nodes attached to a fragment parent. */
  @Test public void attachedDbNodes() {
    final String doc = "let $d := document { parse-xml('<x><a><b/></a><a><b/></a></x>')/node() } ";
    query(doc + "return $d//b/ancestor::a ! (root() is $d)", "true\ntrue");
    query(doc + "return $d//b/ancestor::a ! path()", "/Q{}x[1]/Q{}a[1]\n/Q{}x[1]/Q{}a[2]");
    query(doc + "let $s := siblings($d/x/a[1]) return ($s, $s) ! (root() is $d)",
        "true\ntrue\ntrue\ntrue");
  }

  /** Document order of fragments that share database instances. */
  @Test public void attachedDbNodesOrder() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<x><a/><b/></x>"));
    execute(new Add("b.xml", "<x><a/><b/></x>"));
    final String docs = "let $d := db:get('" + NAME + "') ! document { node() } ";
    query(docs + "return $d//node() ! name()", "x\na\nb\nx\na\nb");
    query(docs + "return ($d[2], $d[1]/x) ! (. is $d[1]/x)", "false\ntrue");
    query(docs + "let $n := $d/descendant-or-self::node() "
        + "return every $i in 2 to count($n) satisfies $n[$i - 1] << $n[$i]", true);
    query(docs + "return $d[1] << $d[1]/x", true);
    query(docs + "return $d[2] << $d[1]/x", false);

    final String elem = "let $e := <e a='1'>{ db:get('" + NAME + "')[1]/x/a }</e> ";
    query(elem + "return $e/@a << $e/a", true);
    query(elem + "return $e/a << $e/@a", false);
    query(elem + "return ($e/a | $e/@a) ! (. instance of attribute())", "true\nfalse");
  }

  /** Static result size of a step on a single map or array. */
  @Test public void structStepSize() {
    final String array = "array { (1 to 5) ! <x>{ . }</x> }";
    check("count(array { (1 to 1000000) ! <x>{ . }</x> }/*)", 1000000, empty(IterPath.class));
    check("count([ (), (1, 2), 3 ]/*)", 3, empty(IterPath.class));
    check("count({ 'a': 1, 'b': (2, 3) }/node())", 2, empty(IterPath.class));
    check("count([]/*)", 0, empty(IterPath.class));

    check("count(" + array + "/2)", 1, empty(IterPath.class));
    check("count(" + array + "/0)", 0, empty(IterPath.class));
    check("count(" + array + "/jnode(6))", 0, empty(IterPath.class));
    check("count(" + array + "/*[1])", 1, empty(IterPath.class));
    check("count(" + array + "/*[last()])", 1, empty(IterPath.class));
    check("count(" + array + "/*[position() < 3])", 2, empty(IterPath.class));
    check("count(" + array + "/*[position() > 3])", 2, empty(IterPath.class));
    check("count(" + array + "/*[position() = 2 to 9][1])", 1, empty(IterPath.class));
    check("count({ 'a': 1, 'b': 2 }/*[last()])", 1, empty(IterPath.class));
    check("count([]/*[1])", 0, empty(IterPath.class));
    check("count(" + array + "/..)", 0, empty(IterPath.class));
    check("count(" + array + "/following-sibling::*)", 0, empty(IterPath.class));
    check("count(" + array + "/preceding::*[1])", 0, empty(IterPath.class));
    check("count(" + array + "/descendant::node())", 5, empty(IterPath.class));
    check("count(" + array + "/descendant-or-self::node())", 6, empty(IterPath.class));
    check("count(" + array + "/descendant::*[last()])", 1, empty(IterPath.class));
    check("count(array { 1 to 5 }//*)", 5, empty(IterPath.class));
    check("count(" + array + "/ancestor-or-self::*)", 1, empty(IterPath.class));
    check("count(" + array + "/following-sibling-or-self::*)", 1, empty(IterPath.class));
    check("count(" + array + "/self::jnode(*, map(*)))", 0, empty(IterPath.class));

    // rewrites for unknown sizes
    final String rnd = "array { 1 to " + _RANDOM_INTEGER.args(1) + " + 2 }";
    check(rnd + "/..", "", empty(IterPath.class));
    check(rnd + "/preceding-sibling::*", "", empty(IterPath.class));
    check("count(jtree([ 1 ])/..)", 0, empty(IterPath.class));
    check("count(" + rnd + "/ancestor-or-self::*)", 1, "//IterStep/@axis = 'self'");
    check("count(" + rnd + "//*)", 2, "//IterStep/@axis = 'child'");

    // JNode leaves
    check("count(" + rnd + "/*/*)", 0, empty(IterPath.class));
    check("count(" + rnd + "/*/descendant-or-self::*)", 2, count(IterStep.class, 1));
    check("count([ (1, 2), [ 3 ] ]/*/descendant::*)", 3, exists(IterPath.class));

    // root nodes: documents and JNodes
    final String doc = "document { <a/> }";
    check(doc + "/following::node()", "", empty(IterPath.class));
    check("count(" + doc + "/following-or-self::node())", 1, empty(IterPath.class));
    check(doc + "/preceding-sibling-or-self::element()", "", empty(IterPath.class));
    query("count(" + doc + "/following-or-self::document-node())", 1);
    query("count((" + doc + ", " + doc + "/a)/following-or-self::document-node())", 1);
    check("count(" + rnd + "/preceding-sibling-or-self::*[1])", 1,
        "//IterStep/@axis = 'self'");

    // type errors are preserved
    error("[ 1 ]/attribute::node()", ATTRAXIS_X);
    error("[ 1 ]/attribute::node()[1]", ATTRAXIS_X);

    // JNodes with leaf values can be parents of item nodes
    final String item = "jtree([ 1 ])/*/item::*";
    query("count(" + item + "/parent::jnode(*, xs:integer))", 1);
    query("count(" + item + "/ancestor-or-self::jnode(*, xs:integer))", 2);
    query("count(jtree(1)/item::*/parent::jnode((), xs:integer))", 1);

    // -or-self axes on attributes
    query("count(<a b=''/>/@b/following-sibling-or-self::attribute())", 1);
    query("count(<a b=''/>/@b/preceding-or-self::attribute())", 1);

    check("count(" + array + "/*[. = '2'])", 1, exists(IterPath.class));
    check("count([ (), 1, (2, 3) ]/descendant::*)", 5, exists(IterPath.class));
    check("count({ 1: 1 }/1)", 1, exists(IterPath.class));
    query("[ (), (1, 2), 3 ]/* ! jkey(.)", "1\n2\n3");
    query("count([ [ 1, 2 ], [ 3 ] ]/*/*)", 3);
  }
}
