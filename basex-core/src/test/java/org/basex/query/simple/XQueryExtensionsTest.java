package org.basex.query.simple;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * XQuery extensions.
 *
 * @author BaseX Team 2005-24, BSD License
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
    query("try { error() } catch * { count($err:additional) }", 1);
    query("let $f := function () { error() } " +
        "return try { $f() } catch * { count($err:additional) }", 2);
  }

  /** Value map. */
  @Test public void valueMap() {
    query(wrap(1) + " ~ (., . to 6)", "1\n1\n2\n3\n4\n5\n6");
    query("count(" + wrap(1) + " ~ (., . to 6))", 7);

    check("2 ~ .", 2, root(Int.class));
    check("2 ~ .", 2, root(Int.class));

    check("void() ~ void() ~ 2", 2, root(CachedValueMap.class), count(VOID, 2));
    check("void() ~ 2", 2, root(CachedValueMap.class));
    check("void() ~ . ~ 2", 2, root(CachedValueMap.class), empty(ContextValue.class));
    check("(void() ~ void()) ~ 2", 2, count(CachedValueMap.class, 1));

    check("(1, 2) ~ (head(.) + tail(.))", 3, root(Int.class));
    check("(<a/>, <b/>) ~ (foot(.), head(.))", "<b/>\n<a/>", root(CachedValueMap.class));

    check("2 ~ (. * .)", 4, root(Int.class));
    check("<a>2</a> ~ (. * .)", 4, root(Dbl.class));
    check("<?_ 2?> ~ xs:integer() ~ (. * .)", 4, root(SingleValueMap.class));

    check("<?_ 2?> ! xs:integer() ! (. * .) ! (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ! xs:integer() ! (. * .) ~ (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ! xs:integer() ~ (. * .) ! (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ~ xs:integer() ! (. * .) ! (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ! xs:integer() ~ (. * .) ~ (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ~ xs:integer() ! (. * .) ~ (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ~ xs:integer() ~ (. * .) ! (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
    check("<?_ 2?> ~ xs:integer() ~ (. * .) ~ (. * .)", 16, empty(ItemMap.class),
        count(SingleValueMap.class, 1), root(SingleValueMap.class));
  }
}
