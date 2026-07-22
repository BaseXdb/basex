package org.basex.query.simple;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * XQuery extensions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryExtensionsTest extends SandboxTest {
  /** If without else. */
  @Test public void ifWithoutElse() {
    query("if(1) then 2", 2);
    query("if(1) then if(2) then 3", 3);
    query("if(1) then 2 else if(3) then 4", 2);
    query("if(()) then 2", "");
    query("if(()) then if(2) then 3", "");
    query("if(()) then 2 else if(3) then 4", 4);
  }

  /** $err:additional. */
  @Test public void errAdditional() {
    query("try { error() } catch * { count($err:stack-trace) }", 1);
    query("let $f := function () { error() } " +
        "return try { $f() } catch * { count($err:stack-trace) }", 1);

    // every entry names the function it occurred in; the last one is the main module
    query("declare %basex:inline(0) function local:f() { local:g() }; " +
        "declare %basex:inline(0) function local:g() { error() }; " +
        "try { local:f() } catch * { " +
        "let $lines := tokenize($err:stack-trace, '\n')[normalize-space()] return (" +
        "count($lines), " +
        "starts-with($lines[1], 'local:g#0 ('), " +
        "starts-with($lines[2], 'local:f#0 ('), " +
        "matches($lines[3], '^[^#]+, \\d+/\\d+$')) }", "3\ntrue\ntrue\ntrue");
    // global variables are reported as well
    query("declare %basex:inline(0) function local:f() { error() }; " +
        "declare variable $config := local:f(); " +
        "try { $config } catch * { let $lines := tokenize($err:stack-trace, '\n') return (" +
        "starts-with($lines[1], 'local:f#0 ('), " +
        "starts-with($lines[2], '$config (')) }", "true\ntrue");
    query("declare variable $config := error(); " +
        "try { $config } catch * { starts-with($err:stack-trace, '$config (') }", true);
    // errors outside a declaration are reported without a name
    query("let $f := function() { error() } " +
        "return try { $f() } catch * { contains($err:stack-trace, '(') }", false);
    query("try { error() } catch * { contains($err:stack-trace, '(') }", false);

    // the enclosing function is recorded at parse time, and hence survives inlining
    query("declare %basex:inline function local:f() { error() }; " +
        "try { local:f() } catch * { starts-with($err:stack-trace, 'local:f#0 (') }", true);
    query("declare %basex:inline(0) function local:f() { error() }; " +
        "try { local:f() } catch * { starts-with($err:stack-trace, 'local:f#0 (') }", true);
  }

  /** Pipeline operator. */
  @Test public void pipeline() {
    query(wrap(1) + " -> (., . to 6)", "1\n1\n2\n3\n4\n5\n6");
    query("count(" + wrap(1) + " -> (., . to 6))", 7);

    query("<x/>/parent::* -> (self::y or count(*) eq 1)", false);

    check("2 -> .", 2, root(Itr.class));
    check("2 -> .", 2, root(Itr.class));

    check("void(()) -> void(()) -> 2", 2, root(Pipeline.class), count(VOID, 2));
    check("void(()) -> 2", 2, root(Pipeline.class));
    check("void(()) -> . -> 2", 2, root(Pipeline.class), empty(ContextValue.class));
    check("(void(()) -> void(())) -> 2", 2, count(Pipeline.class, 1));

    check("(1, 2) -> (head(.) + tail(.))", 3, root(Itr.class));
    check("(<a/>, <b/>) -> (foot(.), head(.))", "<b/>\n<a/>", root(Pipeline.class));

    check("2 -> (. * .)", 4, root(Itr.class));
    check("<a>2</a> -> (. * .)", 4, root(Dbl.class));
    check("<?_ 2?> -> xs:integer() -> (. * .)", 4, root(Pipeline.class));

    check("<?_ 2?> ! xs:integer() ! (. * .) ! (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> ! xs:integer() ! (. * .) -> (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> ! xs:integer() -> (. * .) ! (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> -> xs:integer() ! (. * .) ! (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> ! xs:integer() -> (. * .) -> (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> -> xs:integer() ! (. * .) -> (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> -> xs:integer() -> (. * .) ! (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
    check("<?_ 2?> -> xs:integer() -> (. * .) -> (. * .)", 16,
        count(Pipeline.class, 1), root(Pipeline.class));
  }
}
