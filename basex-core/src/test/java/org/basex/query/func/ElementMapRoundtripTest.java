package org.basex.query.func;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class roundtrips XML through fn:element-to-map and fn:map-to-element.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ElementMapRoundtripTest extends SandboxTest {
  /** Namespace declarations. */
  private static final String NS = "declare namespace p = 'http://p'; ";

  /** Test method. */
  @Test public void simpleContent() {
    roundtrip("<a/>");
    roundtrip("<a>x</a>");
    roundtrip("<a> x </a>");
    roundtrip("<a>a&#10;b</a>");
    roundtrip("<a>&lt;&amp;</a>");
    roundtrip("<a>{ codepoints-to-string(119070) }</a>");
    // inferred data types
    roundtrip("<a>10</a>");
    roundtrip("<a>-10</a>");
    roundtrip("<a>10.5</a>");
    roundtrip("<a>true</a>");
    roundtrip("<a>false</a>");
    roundtrip("<a>TRUE</a>");
    roundtrip("<a>007</a>");
    roundtrip("<a>INF</a>");
    roundtrip("<a>NaN</a>");
    roundtrip("<a>123456789012345678901234567890</a>");
    // attributes
    roundtrip("<a x=''/>");
    roundtrip("<a x='1'/>");
    roundtrip("<a x='1' y='2'>t</a>");
    roundtrip("<a x='&#9;'/>");
    roundtrip("<a x='&#10;'/>");
    roundtrip("<a x='&lt;&amp;\"'>&lt;&amp;</a>");
    // element and attribute with identical names
    roundtrip("<a b='1'><b>2</b></a>");
    // element names with special characters
    roundtrip("<a.b-c_d/>");
    roundtrip("<a>{ element { codepoints-to-string(119070) } { 'x' } }</a>");
  }

  /** Test method. */
  @Test public void childElements() {
    // record layout
    roundtrip("<a><b>1</b><c>2</c></a>");
    roundtrip("<a><b><c>1</c></b><d x='2'/></a>");
    // sequence layout
    roundtrip("<a><b>1</b><c>2</c><b>3</b></a>");
    roundtrip("<a><b x='1'>1</b><c>2</c><b>3</b></a>");
    // list layout (child name is recoverable from the sibling attributes)
    roundtrip("<a x='1'><b>2</b><b>3</b></a>");
    roundtrip("<a x='1'><b/><b/></a>");
    // mixed layout
    roundtrip("<p id='x'>a<i>b</i>c</p>");
    roundtrip("<a><b>1</b>t</a>");
    roundtrip("<a>t<b>1</b></a>");
    roundtrip("<a>t<b x='1'>1</b></a>");
    roundtrip("<a>t<b>u<c>1</c></b></a>");
    // deeply nested and wide elements
    query("let $e := fold-left(1 to 50, <l>x</l>, fn($x, $i) { <l>{ $x }</l> }) "
        + "return deep-equal($e, " + convert("$e", null) + ")", true);
    query("let $e := <a x='1'>{ for $i in 1 to 500 return <b>{ $i }</b> }</a> "
        + "return deep-equal($e, " + convert("$e", null) + ")", true);
  }

  /** Test method. */
  @Test public void namespaces() {
    roundtrip("<x xmlns='http://e'><y>1</y></x>");
    roundtrip("<p:x xmlns:p='http://p'><p:y>1</p:y></p:x>");
    roundtrip("<p:x xmlns:p='http://p'><y>1</y></p:x>");
    roundtrip("<x xmlns='http://e'><p:y xmlns:p='http://p'>1</p:y></x>");
    roundtrip("<x xmlns='http://e'><y xmlns=''>1</y></x>");
    // identical local names in different namespaces
    roundtrip("<a xmlns:p='http://p'><p:b>1</p:b><b>2</b></a>");
    roundtrip("<a xmlns:p='http://p' xmlns:q='http://q' p:x='1' q:x='2'/>");
    // attributes
    roundtrip("<a xmlns:p='http://p' p:x='1'/>");
    roundtrip("<a xml:id='v'/>");
    roundtrip("<a xml:lang='en'/>");
    roundtrip("<a xmlns='http://e' x='1'/>");
    // attribute prefixes are not preserved: a new prefix is generated
    serialized("<a xmlns:p='http://p' p:x='1'/>", null, "<a xmlns:ns=\"http://p\" ns:x=\"1\"/>");
  }

  /** Test method. */
  @Test public void nameFormats() {
    roundtrip("<a x='1'><b>2</b></a>", " { 'name-format': 'lexical' }");
    roundtrip("<a x='1'><b>2</b></a>", " { 'name-format': 'local' }");
    roundtrip("<a x='1'><b>2</b></a>", " { 'name-format': 'eqname' }");
    roundtrip("<p:x xmlns:p='http://p'><p:y>1</p:y></p:x>", " { 'name-format': 'eqname' }");
    roundtrip("<a xmlns:p='http://p' p:x='1'/>", " { 'name-format': 'eqname' }");
    // lexical names are resolved against the static context of the query
    query(NS + "deep-equal(<p:x><p:y>1</p:y></p:x>, "
        + convert("<p:x><p:y>1</p:y></p:x>", " { 'name-format': 'lexical' }") + ")", true);
    // local names discard all namespaces
    serialized("<x xmlns='http://e'><y>1</y></x>", " { 'name-format': 'local' }",
        "<x><y>1</y></x>");
    serialized("<a xmlns:p='http://p' p:x='1'/>", " { 'name-format': 'local' }", "<a x=\"1\"/>");
    // lexical names retain prefixes, but a default namespace has none
    query(NS + "deep-equal(<a p:x='1'/>, "
        + convert("<a p:x='1'/>", " { 'name-format': 'lexical' }") + ")", true);
    serialized("<x xmlns='http://e'>1</x>", " { 'name-format': 'lexical' }", "<x>1</x>");
  }

  /** Test method. */
  @Test public void markerAndContentKey() {
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': ':' }");
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': '#' }");
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': 'attr:' }");
    roundtrip("<a x='1'>t</a>", " { 'content-key': 'value' }");
    roundtrip("<a x='1'>t</a>", " { 'content-key': '@v' }");
    roundtrip("<a x='1'>t</a>", " { 'content-key': '#' }");
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': ':', 'content-key': ':v' }");

    // an empty marker cannot distinguish attributes from child elements
    error(convert("<a><b>1</b></a>", " { 'attribute-marker': '' }"), MAP_TO_ELEMENT_X);
    // a child element name that starts with the marker is restored as an attribute
    serialized("<a><bc>1</bc></a>", " { 'attribute-marker': 'b' }", "<a c=\"1\"/>");
    // a child element name that equals the marker leaves an empty name
    error(convert("<a><at>1</at></a>", " { 'attribute-marker': 'at' }"), MAP_TO_ELEMENT_X);
    // reserved keys take precedence over the marker
    error(convert("<a comment='1'>t</a>", " { 'attribute-marker': '#' }"), MAP_TO_ELEMENT_X);
    // a content key that equals a child element name yields simple content
    serialized("<a><value>1</value></a>", " { 'content-key': 'value' }", "<a>1</a>");

    // reserved keys keep their meaning if they are chosen as content key
    roundtrip("<a x='1'>t</a>", " { 'content-key': '#comment' }");
    roundtrip("<a x='1'>t</a>", " { 'content-key': '#processing-instruction' }");
    roundtrip("<a x='1'>t</a>", " { 'content-key': '#data' }");
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': '#content' }");
    roundtrip("<a x='1'>t</a>", " { 'attribute-marker': 'v', 'content-key': 'v' }");

    // if attribute and element names conflict, the default marker is used instead
    query("element-to-map(<a x='1'><x>2</x></a>, { 'attribute-marker': '' })"
        + "?a => map:keys() => sort()", "@x\nx");
    query("element-to-map(<a x='1'><vx>2</vx></a>, { 'attribute-marker': 'v' })"
        + "?a => map:keys() => sort()", "@x\nvx");
    // without a conflict, the requested marker is used
    query("element-to-map(<a x='1'><y>2</y></a>, { 'attribute-marker': '' })"
        + "?a => map:keys() => sort()", "x\ny");
    // skipped attributes cannot conflict
    query("element-to-map(<a x='1'><x>2</x></a>, { 'attribute-marker': '', "
        + "'plan': { '@x': { 'type': 'skip' } } })?a => map:keys()", "x");
    // the conflict is decided per element
    query("element-to-map(<a x='1'><x>2</x><b x='3'><y>4</y></b></a>, { 'attribute-marker': '' })"
        + "?a?b => map:keys() => sort()", "x\ny");
    query("element-to-map(<a x='1' y='2'><x>3</x></a>, { 'attribute-marker': '' })"
        + "?a => map:keys() => sort()", "@x\n@y\nx");
    query("element-to-map(<a xmlns:p='u' p:x='1'><p:x>2</p:x></a>, { 'attribute-marker': '' })"
        + "?a => map:keys() => sort()", "@Q{u}x\nQ{u}x");
    // an attribute entry must not be applied to an element of the same name, and vice versa
    query("element-to-map(<a x='1'><x>2</x></a>, { 'plan': { '@x': { 'type': 'integer' } } })"
        + "?a?('@x') instance of xs:integer", true);
    query("element-to-map(<a x='1'><x>2</x></a>, { 'plan': { '@x': { 'type': 'integer' } } })"
        + "?a?x instance of xs:integer", true);
    query("element-to-map(<a x='1'/>, { 'plan': { 'x': { 'layout': 'simple', "
        + "'type': 'integer' } } })?a?('@x') instance of xs:string", true);
  }

  /** Test method. */
  @Test public void plans() {
    roundtrip("<a>1</a>", " { 'plan': { 'a': { 'layout': 'simple', 'type': 'string' } } }");
    roundtrip("<a x='1'/>", " { 'plan': { 'a': { 'layout': 'empty-plus' } } }");
    roundtrip("<a x='1'>t</a>", " { 'plan': { 'a': { 'layout': 'simple-plus' } } }");
    roundtrip("<a><b>1</b></a>", " { 'plan': { 'a': { 'layout': 'record' } } }");
    roundtrip("<a><b>1</b><b>2</b></a>", " { 'plan': { 'a': { 'layout': 'sequence' } } }");
    roundtrip("<a>x<b>y</b></a>", " { 'plan': { '*': { 'layout': 'mixed' } } }");
    roundtrip("<a><b>1</b><b>2</b></a>",
        " { 'plan': { 'a': { 'layout': 'list', 'child': 'b' } } }");
    roundtrip("<a x='1'><b>1</b><b>2</b></a>",
        " { 'plan': { 'a': { 'layout': 'list-plus', 'child': 'b' } } }");
    // a list layout is also applied to child elements
    roundtrip("<a x='1'><b><c>1</c><c>2</c></b></a>",
        " { 'plan': { 'b': { 'layout': 'list', 'child': 'c' } } }");
    // xml layout
    roundtrip("<a><b x='1'>t</b></a>", " { 'plan': { 'a': { 'layout': 'xml' } } }");
    roundtrip("<a><p:b xmlns:p='http://p' x='1'>t</p:b></a>",
        " { 'plan': { 'a': { 'layout': 'xml' } } }");
    roundtrip("<a/>", " { 'plan': { 'a': { 'layout': 'xml' } } }");
    // attribute types
    roundtrip("<a x='1'/>", " { 'plan': { '@x': { 'type': 'integer' } } }");
    roundtrip("<a x='1'>t</a>",
        " { 'attribute-marker': ':', 'plan': { '@x': { 'type': 'integer' } } }");
    roundtrip("<a xmlns:p='u' p:x='1'/>", " { 'plan': { '@Q{u}x': { 'type': 'integer' } } }");

    // attributes with the type 'skip' are omitted
    serialized("<a x='1' y='2'>t</a>", " { 'plan': { '@x': { 'type': 'skip' } } }",
        "<a y=\"2\">t</a>");
    serialized("<a>t<b x='1'>u</b></a>", " { 'plan': { '@x': { 'type': 'skip' } } }",
        "<a>t<b>u</b></a>");
    // a root element with the layout 'deep-skip' yields an empty sequence
    query("count(element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'deep-skip' } } }))", 0);
    query("count(" + convert("<a/>", " { 'plan': { 'a': { 'layout': 'deep-skip' } } }") + ")", 0);
    // the layout 'error' always fails
    error("element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'error' } } })", PLAN_X_X);
    error("element-to-map(<a><b/></a>, { 'plan': { 'a': { 'layout': 'record' }, "
        + "'*': { 'layout': 'error' } } })", PLAN_X_X);
    // list layouts require identically named children
    error("element-to-map(<a x='1'><b>1</b><c>2</c></a>, "
        + "{ 'plan': { 'a': { 'layout': 'list-plus', 'child': 'b' } } })", PLAN_X_X);
    // single children are skipped, siblings are kept
    serialized("<a><b>1</b><c>2</c></a>", " { 'plan': { 'c': { 'layout': 'deep-skip' } } }",
        "<a><b>1</b></a>");
    // xml layout for a child element
    roundtrip("<a><b><c x='1'>t</c></b></a>", " { 'plan': { 'b': { 'layout': 'xml' } } }");
    // layouts for elements without children
    roundtrip("<a x='1'/>", " { 'plan': { 'a': { 'layout': 'mixed' } } }");
    roundtrip("<a x='1'/>", " { 'plan': { 'a': { 'layout': 'sequence' } } }");
    // plan keys are independent of the name format
    query("element-to-map(<x xmlns='u'>1</x>, { 'name-format': 'local', "
        + "'plan': { 'Q{u}x': { 'layout': 'simple', 'type': 'string' } } })?x "
        + "instance of xs:untypedAtomic", true);
    // content that cannot be cast to the requested type is kept as a string
    roundtrip("<a>x</a>", " { 'plan': { 'a': { 'layout': 'simple', 'type': 'integer' } } }");
    roundtrip("<a>123456789012345678901234567890</a>",
        " { 'plan': { 'a': { 'layout': 'simple', 'type': 'integer' } } }");
    // a plan may be applied to other documents
    query("let $o := { 'plan': element-to-map-plan(<a><b>1</b><b>2</b></a>) } "
        + "return serialize(" + convert("<a><b>1</b></a>", " $o") + ")", "<a><b>1</b></a>");

    // skipped children are lost
    serialized("<a><b>1</b></a>", " { 'plan': { 'b': { 'layout': 'deep-skip' } } }", "<a/>");
    // layouts that do not match the input
    error("element-to-map(<a><b>1</b></a>, { 'plan': { 'a': { 'layout': 'empty' } } })", PLAN_X_X);
    error("element-to-map(<a><b>1</b><b>2</b></a>, "
        + "{ 'plan': { 'a': { 'layout': 'list', 'child': 'z' } } })", PLAN_X_X);

    // the wildcard entry is the fallback for a layout that cannot be applied
    roundtrip("<a>t</a>", " { 'plan': { 'a': { 'layout': 'empty' }, "
        + "'*': { 'layout': 'simple' } } }");
    roundtrip("<a>t<b>1</b></a>", " { 'plan': { 'a': { 'layout': 'record' }, "
        + "'*': { 'layout': 'mixed' } } }");
    // the fallback must be applicable as well
    error("element-to-map(<a><b>1</b><c>2</c></a>, { 'plan': { '*': { 'layout': 'record' } } })",
        PLAN_X_X);
    error("element-to-map(<a>t</a>, { 'plan': { 'a': { 'layout': 'empty' }, "
        + "'*': { 'layout': 'empty' } } })", PLAN_X_X);
    error("element-to-map(<a>t</a>, { 'plan': { 'a': { 'layout': 'empty' } } })", PLAN_X_X);
  }

  /** Test method. */
  @Test public void generatedPlans() {
    generated("<a>1</a>");
    generated("<a x='1'>t</a>");
    generated("<a><b>1</b><c>2</c></a>");
    generated("<a><b>1</b><b>2</b></a>");
    generated("<a x='1'><b>1</b><b>2</b></a>");
    // a generated plan records the child names of nested lists
    generated("<a><b><c>1</c><c>2</c></b></a>");
    generated("<a x='1'><b><c>1</c><c>2</c></b></a>");
    generated("<a>t<b x='1'>u</b>v</a>");
    generated("<p:a xmlns:p='http://p'><p:b>1</p:b></p:a>");
    // a plan generated from several elements applies to all of them
    query("let $e1 := <a><b>1</b></a> let $e2 := <a><b>1</b><b>2</b></a> "
        + "let $o := { 'plan': element-to-map-plan(($e1, $e2)) } "
        + "return every $e in ($e1, $e2) satisfies "
        + "deep-equal($e, map-to-element(element-to-map($e, $o), $o))", true);
  }

  /** Test method. */
  @Test public void scale() {
    query("count(map-to-element({ 'a': { 'b': array { 1 to 10000 } } })/b)", 10000);
    query("count(map-to-element({ 'a': map:merge((1 to 1000) ! { '@a' || . : . }) })/@*)", 1000);
    query("string-length(map-to-element({ 'a': string-join((1 to 10000) ! 'x') }))", 10000);
    query("let $e := <a x='1'>{ for $i in 1 to 1000 return <b>{ $i }</b> }</a> "
        + "return deep-equal($e, " + convert("$e", null) + ")", true);
  }

  /** Test method. */
  @Test public void parsedInput() {
    // parsed input exercises database nodes instead of constructed nodes
    parsed("<a x=\"1\">t</a>");
    parsed("<a><b>1</b><c>2</c></a>");
    parsed("<a>t<b>1</b>u</a>");
    parsed("<p:a xmlns:p=\"u\"><p:b>1</p:b></p:a>");
    parsed("<a><![CDATA[<x>]]></a>");
    parsed("<a> t </a>");
    parsed("<a x=\"1&#10;2\"/>");
    // whitespace text nodes are dropped by the record layout
    query("serialize(" + convert("parse-xml('<a> <b>1</b> </a>')/*", null) + ")",
        "<a><b>1</b></a>");
    // comments and processing instructions survive the mixed layout
    query("serialize(" + convert("parse-xml('<a>x<!--c--><b/><?p d?>y</a>')/*", null) + ")",
        "<a>x<!--c--><b/><?p d?>y</a>");
    // documents are represented by their element child
    query("serialize(" + convert("parse-xml('<!--c--><a>x</a>')", null) + ")", "<a>x</a>");
  }

  /** Test method. */
  @Test public void invalidPlans() {
    // entries with keys that are no valid names are ignored
    query("element-to-map(<a>1</a>, { 'plan': { 'x y': { 'layout': 'empty' } } })?a", 1);
    query("element-to-map(<a>1</a>, { 'plan': { '': { 'layout': 'empty' } } })?a", 1);
    query("element-to-map(<a>1</a>, { 'plan': { 1: { 'layout': 'empty' } } })?a", 1);
    query("element-to-map(<a>1</a>, { 'plan': { 'z:a': { 'layout': 'empty' } } })?a", 1);
    query("element-to-map(<a x='1'/>, { 'plan': { '@x y': { 'type': 'skip' } } })?a?('@x')", 1);
    // layout, type and child values must be valid
    error("element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'x' } } })", INVALIDOPTION_X);
    error("element-to-map(<a>1</a>, { 'plan': { 'a': { 'layout': 'simple', 'type': 'x' } } })",
        INVALIDOPTION_X);
    error("element-to-map(<a><b/><b/></a>, "
        + "{ 'plan': { 'a': { 'layout': 'list', 'child': 'x y' } } })", INVALIDOPTION_X);
    // missing and unexpected keys
    error("element-to-map(<a/>, { 'plan': { 'a': {} } })", INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'list' } } })", INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'plan': { '@x': { 'layout': 'simple' } } })", INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'plan': { '@x': {} } })", INVALIDOPTION_X);
    error("element-to-map(<a>1</a>, { 'plan': { 'a': { 'layout': 'simple', 'type': 'skip' } } })",
        INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'empty', 'child': 'b' } } })",
        INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'plan': { 'a': { 'layout': 'empty', 'type': 'integer' } } })",
        INVALIDOPTION_X);
    // the same validation applies to the reverse direction
    error("map-to-element({ 'a': 'x' }, { 'plan': { 'a': { 'layout': 'x' } } })", INVALIDOPTION_X);
    query("serialize(map-to-element({ 'a': 'x' }, "
        + "{ 'plan': { 'x y': { 'layout': 'empty' } } }))", "<a>x</a>");
    // invalid options
    error("element-to-map(<a/>, { 'unknown': 1 })", INVALIDOPTION_X);
    error("element-to-map(<a/>, { 'name-format': 'x' })", INVALIDOPTIONVALUE_X);
    error("map-to-element({ 'a': 'x' }, { 'unknown': 1 })", INVALIDOPTION_X);
  }

  /** Test method. */
  @Test public void invalidMaps() {
    // structural requirements
    error("map-to-element({})", MAP_TO_ELEMENT_X);
    error("map-to-element({ 1: 'x' })", MAP_TO_ELEMENT_X);
    error("map-to-element({ 'a': true#0 })", MAP_TO_ELEMENT_X);
    error("map-to-element({ 'a': [ { '@x': '1' }, { '@x': '2' } ] })", MAP_TO_ELEMENT_X);
    // reserved keys are recognized in arrays only
    error("map-to-element({ 'a': { '#comment': 'c' } })", MAP_TO_ELEMENT_X);
    // names
    error("map-to-element({ 'Q{u}': 'x' })", MAP_TO_ELEMENT_X);
    error("map-to-element({ 'xmlns:a': 'x' })", MAP_TO_ELEMENT_X);
    query("serialize(map-to-element({ 'Q{}a': 'x' }))", "<a>x</a>");
    query("serialize(map-to-element({ xs:QName('a'): 'x' }))", "<a>x</a>");
    // empty values
    query("serialize(map-to-element({ 'a': [] }))", "<a/>");
    query("serialize(map-to-element({ 'a': () }))", "<a/>");
    query("serialize(map-to-element({ 'a': { 'b': {} } }))", "<a><b/></a>");
    // nilled elements
    query("serialize(map-to-element({ 'a': xs:QName('fn:null') }))",
        "<a xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>");
    // characters that are not permitted in XML
    error("serialize(map-to-element({ 'a': codepoints-to-string(1) }))", INVCODE_X);
    // xml layout
    error("map-to-element({ 'a': '<b>' }, { 'plan': { 'a': { 'layout': 'xml' } } })",
        MAP_TO_ELEMENT_X);
    error("map-to-element({ 'a': '<!--c-->' }, { 'plan': { 'a': { 'layout': 'xml' } } })",
        MAP_TO_ELEMENT_X);
    error("map-to-element({ 'a': '<!DOCTYPE b><b/>' }, { 'plan': { 'a': { 'layout': 'xml' } } })",
        MAP_TO_ELEMENT_X);
    // the xml layout ignores the map key: the serialized name wins
    query("serialize(map-to-element({ 'a': '<b x=\"1\">t</b>' }, "
        + "{ 'plan': { 'a': { 'layout': 'xml' } } }))", "<b x=\"1\">t</b>");
    query("serialize(map-to-element({ 'a': '<b><![CDATA[<x>]]></b>' }, "
        + "{ 'plan': { 'a': { 'layout': 'xml' } } }))", "<b>&lt;x&gt;</b>");
  }

  /** Test method. */
  @Test public void commentsAndProcessingInstructions() {
    // preserved by the mixed and xml layouts
    serialized("<a>x<!--c--><i>y</i><?p d?>z</a>", null, "<a>x<!--c--><i>y</i><?p d?>z</a>");
    serialized("<a><!--c--><b>1</b></a>", " { 'plan': { 'a': { 'layout': 'mixed' } } }",
        "<a><!--c--><b>1</b></a>");
    serialized("<a><!--c--><b>1</b></a>", " { 'plan': { 'a': { 'layout': 'xml' } } }",
        "<a><!--c--><b>1</b></a>");
    // preserved by the sequence layout
    serialized("<a><!--c--><b>1</b><c>2</c><b>3</b></a>", null,
        "<a><!--c--><b>1</b><c>2</c><b>3</b></a>");
    serialized("<a><?p d?><b>1</b><c>2</c><b>3</b></a>", null,
        "<a><?p d?><b>1</b><c>2</c><b>3</b></a>");
    serialized("<a><!--c--><b>1</b></a>", " { 'plan': { 'a': { 'layout': 'sequence' } } }",
        "<a><!--c--><b>1</b></a>");
    // dropped by all other layouts
    serialized("<a>x<!--c-->y</a>", null, "<a>xy</a>");
    serialized("<a><!--c--></a>", null, "<a/>");
    serialized("<a><?p d?></a>", null, "<a/>");
    serialized("<a><b><!--c--></b></a>", null, "<a><b/></a>");
    serialized("<a><!--c--><b>1</b><?p d?><c>2</c></a>", null, "<a><b>1</b><c>2</c></a>");
    // processing instruction without data
    serialized("<a>x{ processing-instruction p {} }<i>y</i></a>", null, "<a>x<?p?><i>y</i></a>");
  }

  /** Test method. */
  @Test public void input() {
    // a document node is represented by its element child
    serialized("document { <a>x</a> }", null, "<a>x</a>");
    serialized("document { <!--c-->, <?p d?>, <a>x</a> }", null, "<a>x</a>");
    // empty input
    query("count(" + convert("()", null) + ")", 0);
    query("count(" + convert("()", " { 'name-format': 'local' }") + ")", 0);
    // documents without a single element child are rejected
    error("element-to-map(document { })", INVTYPE_X);
    error("element-to-map(document { 'x' })", INVTYPE_X);
    error("element-to-map(document { <a/>, <b/> })", INVTYPE_X);
  }

  /** Test method. */
  @Test public void informationLoss() {
    // inferred data types are restored in canonical form
    serialized("<a>1e3</a>", null, "<a>1000</a>");
    serialized("<a>10e0</a>", null, "<a>10</a>");
    serialized("<a>1.0</a>", null, "<a>1</a>");
    serialized("<a>1.</a>", null, "<a>1</a>");
    serialized("<a>.5</a>", null, "<a>0.5</a>");
    serialized("<a>+1</a>", null, "<a>1</a>");
    serialized("<a> 1 </a>", null, "<a>1</a>");
    serialized("<a> true </a>", null, "<a>true</a>");
    // typed attribute values are restored in canonical form as well
    roundtrip("<a x=' 1 '/>");
    serialized("<a x=' 1 '/>", " { 'plan': { '@x': { 'type': 'integer' } } }", "<a x=\"1\"/>");
    // attributes in the xsi namespace are not represented in the map
    serialized("<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:nil='true'/>", null,
        "<a/>");
    serialized("<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='x'>t</a>", null,
        "<a>t</a>");
    // without attributes, repeated children yield an array without a child name
    error(convert("<a><b>1</b><b>2</b></a>", null), MAP_TO_ELEMENT_X);
    error(convert("<a><b/><b/></a>", null), MAP_TO_ELEMENT_X);
    // without a plan, a nested list is indistinguishable from repeated children
    serialized("<a x='1'><b><c>1</c><c>2</c></b></a>", null, "<a x=\"1\"><b>1</b><b>2</b></a>");
  }

  /** Test method. */
  @Test public void constructedNodes() {
    // every call constructs a new element
    query("map-to-element({ 'a': 'x' }) is map-to-element({ 'a': 'x' })", false);
    query("count(map-to-element({ 'a': 'x' }) | map-to-element({ 'a': 'x' }))", 2);
    // the result is a parentless element that can be used as a node
    query("empty(map-to-element({ 'a': 'x' })/..)", true);
    query("map-to-element({ 'a': { 'b': '1', 'c': '2' } })/c/string()", 2);
    query("serialize(<w>{ map-to-element({ 'a': 'x' }) }</w>)", "<w><a>x</a></w>");
    query("serialize(copy $c := map-to-element({ 'a': 'x' }) "
        + "modify insert node <b/> into $c return $c)", "<a>x<b/></a>");
    // static types
    query("map-to-element({ 'a': 'x' }) instance of element()", true);
    query("map-to-element(()) instance of element()?", true);
    query("element-to-map(<a/>) instance of map(xs:string, item()?)", true);
    query("element-to-map(<a>x</a>)?a instance of xs:untypedAtomic", true);
  }

  /**
   * Checks that an element is restored by a round trip through a map.
   * @param element element constructor
   */
  private static void roundtrip(final String element) {
    roundtrip(element, null);
  }

  /**
   * Checks that an element is restored by a round trip through a map.
   * @param element element constructor
   * @param options conversion options (can be {@code null})
   */
  private static void roundtrip(final String element, final String options) {
    query("deep-equal(" + element + ", " + convert(element, options) + ")", true);
  }

  /**
   * Checks that an element is restored by a round trip with a generated conversion plan.
   * @param element element constructor
   */
  private static void generated(final String element) {
    query("let $o := { 'plan': element-to-map-plan(" + element + ") } "
        + "return deep-equal(" + element + ", "
        + "map-to-element(element-to-map(" + element + ", $o), $o))", true);
  }

  /**
   * Checks that a parsed element is restored by a round trip through a map.
   * @param xml XML string
   */
  private static void parsed(final String xml) {
    roundtrip("parse-xml('" + xml + "')/*");
  }

  /**
   * Checks the serialized result of a round trip through a map.
   * @param element element constructor
   * @param options conversion options (can be {@code null})
   * @param expected expected serialization
   */
  private static void serialized(final String element, final String options,
      final String expected) {
    query("serialize(" + convert(element, options) + ")", expected);
  }

  /**
   * Returns a query that converts an element to a map and back.
   * @param element element constructor
   * @param options conversion options (can be {@code null})
   * @return query
   */
  private static String convert(final String element, final String options) {
    final String opts = options == null ? "" : "," + options;
    return "map-to-element(element-to-map(" + element + opts + ")" + opts + ")";
  }
}
