package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the code completion of path expressions, as used by the GUI.
 *
 * <p>Both parsers are tested with the queries of {@code CASES}. The queries of
 * {@code CASES_MANUAL} and {@code CASES_REX} are the same, with the different suggestions of the
 * hand-written and the generated parser.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class QuerySuggestTest extends SandboxTest {
  /** Document. */
  private static final String XML =
      "<r><a id='1'><b><c/><d x='1'/></b><b2/></a><ab><b/></ab><abc k='v'/></r>";

  /** Document with namespaces, and names with supplementary characters. */
  private static final String XML_NAMES =
      "<r xmlns:p='urn:p' xmlns:q='urn:q'><p:a id='1'><p:b/><q:b/></p:a><a/>"
      + "<\uD835\uDCB3 \uD835\uDCB4='1'><\uD835\uDCB5/></\uD835\uDCB3><ab>x</ab><\u00FC/></r>";

  /** Queries, with the suggestions (comma-separated) and the valid input of both parsers. */
  private static final String[][] CASES = {
    { "", "", "" },
    { "/", "r", "/" },
    { "/r", "", "/" },
    { "/r/", "a,ab,abc", "/r/" },
    { "/r/a", "a,ab,abc", "/r/" },
    { "/r/a/", "@id,b,b2", "/r/a/" },
    { "/r/a/b", "b,b2", "/r/a/" },
    { "/r/a/b/", "c,d", "/r/a/b/" },
    { "/r/a/b/c", "", "/r/a/b/" },
    { "/r/a/b/c/", "", "/r/a/b/c/" },
    { "/r/a/b/c/d/", "", "/r/a/b/c/d/" },
    { "//", "@id,@k,@x,a,ab,abc,b,b2,c,d,r", "//" },
    { "//b", "b,b2", "//" },
    { "//b/", "c,d", "//b/" },
    { "/r//", "@id,@k,@x,a,ab,abc,b,b2,c,d", "/r//" },
    { "/r//c", "", "/r//" },
    { "/r/a/@id", "", "/r/a/" },
    { "/r/a/@id/", "", "/r/a/@id/" },
    { "/r/a/b/d/@", "@x", "/r/a/b/d/" },
    { "/r/a[", "@id,b,b2", "/r/a[" },
    { "/r/a[b/", "c,d", "/r/a[b/" },
    { "/r/a[b/c]/", "@id,b,b2", "/r/a[b/c]/" },
    { "/r/a[b]/b/", "c,d", "/r/a[b]/b/" },
    { "/r/a[@id='1']/", "@id,b,b2", "/r/a[@id='1']/" },
    { "/r/a[@id = '1' and b/", "c,d", "/r/a[@id = '1' and b/" },
    { "/r/*", "", "/r/" },
    { "/r/*/", "", "/r/*/" },
    { "/r/a/*/", "", "/r/a/*/" },
    { "/r/child::", "a,ab,abc", "/r/child::" },
    { "/r/child::a/", "@id,b,b2", "/r/child::a/" },
    { "/r/child::a/child::", "@id,b,b2", "/r/child::a/child::" },
    { "/r/descendant::", "a,ab,abc", "/r/descendant::" },
    { "/r/descendant-or-self::", "a,ab,abc", "/r/descendant-or-self::" },
    { "/r/parent::", "a,ab,abc", "/r/parent::" },
    { "/r/a/../", "", "/r/a/../" },
    { "/r/a/b2/", "", "/r/a/b2/" },
    { "/r/x", "", "/r/" },
    { "/r/x/", "", "/r/x/" },
    { "/r/a/b | /r/", "a,ab,abc", "/r/a/b | /r/" },
    { "/r/a, /r/ab/", "b", "/r/a, /r/ab/" },
    { "(/r/a/", "@id,b,b2", "(/r/a/" },
    { "(/r/a)/", "@id,b,b2", "(/r/a)/" },
    { "for $x in /r/a return $x/", "r", "for $x in /r/a return $x/" },
    { "for $x in /r/a return /r/ab/", "b", "for $x in /r/a return /r/ab/" },
    { "count(/r/a/", "@id,b,b2", "count(/r/a/" },
    { "count(/r/a/b/", "c,d", "count(/r/a/b/" },
    { "/r/a[count(b/", "c,d", "/r/a[count(b/" },
    { "/r/a/text()/", "", "/r/a/text()/" },
    { "/r/a/node()/", "", "/r/a/node()/" },
    { "/r/(a|ab)/", "", "/r/(a|ab)/" },
    { "/r/(a|", "", "/r/(a|" },
    { "/r/a/(b|", "", "/r/a/(b|" },
    { "r/", "", "r/" },
    { "r/a/", "", "r/a/" },
    { "a/b", "", "a/" },
    { "'string/", "", "'string/" },
    { "\"/r/a/", "", "\"/r/a/" },
    { "/r/a/b[c = 'x/", "", "/r/a/b[c = 'x/" },
    { "/r[a/b]/ab/", "b", "/r[a/b]/ab/" },
    { "/r/a/b[1]/", "c,d", "/r/a/b[1]/" },
    { "/r/a/b[1", "c,d", "/r/a/b[1" },
    { "1 + ", "", "1 + " },
    { "/r/a/b/c/ ", "", "/r/a/b/c/ " },
    { "/ r / a / ", "@id,b,b2", "/ r / a / " },
    { "/r/a/b/*:c", "", "/r/a/b/" },
    { "(: comment :) /r/", "a,ab,abc", "(: comment :) /r/" },
    { "/r/a/b/c/@", "", "/r/a/b/c/" },
    { "/r/ab/b/", "", "/r/ab/b/" },
    { "/r/ab", "ab,abc", "/r/" },
    { "/r/abc/@", "@k", "/r/abc/" },
    { "/r/abc/@k", "", "/r/abc/" },
    { "/r/a/c", "", "/r/a/" },
    { "/r/a/b/c[", "", "/r/a/b/c[" },
    { "/r/a[b/c[", "", "/r/a[b/c[" },
    { "/r/a[.", "@id,b,b2", "/r/a[." },
    { "/r/a[./", "c,d", "/r/a[./" },
    { "/r/a[b = /r/", "", "/r/a[b = /r/" },
    { "/r/a[b = /r/a/", "", "/r/a[b = /r/a/" },
    { "/r/a/b/(", "", "/r/a/b/(" },
    { "/r/a/b[c ", "", "/r/a/b[c " },
    { "/r/a/b[Q{u}c ", "", "/r/a/b[Q{u}c " },
    { "/r/a/b/0x1/", "@x", "/r/a/b/0x1/" },
    { "/r/a/b/1/", "@x", "/r/a/b/1/" },
    { "/r/a/b/\"x\"/", "@x", "/r/a/b/\"x\"/" },
    { "/r/a/b/x", "", "/r/a/b/" },
    { "//a/b/", "c,d", "//a/b/" },
    { "//a//", "@id,@x,b,b2,c,d", "//a//" },
    { "//a//c", "", "//a//" },
    { "/r/a/b/c/../", "", "/r/a/b/c/../" },
    { "/r/a/b/c/../../", "", "/r/a/b/c/../../" },
    { "/r/element(a)/", "@id,b,b2", "/r/element(a)/" },
    { "/r/a/child::element(b)/", "c,d", "/r/a/child::element(b)/" },
    { "for $x in /r/a/", "@id,b,b2", "for $x in /r/a/" },
    { "let $a := /r/a/", "@id,b,b2", "let $a := /r/a/" },
    { "if(/r/a/", "@id,b,b2", "if(/r/a/" },
    { "<x>{ /r/a/", "@id,b,b2", "<x>{ /r/a/" },
    { "<x>{ /r/a/b[", "c,d", "<x>{ /r/a/b[" },
    { "for $x in /r/a where $x/b/", "", "for $x in /r/a where $x/b/" },
    { "/r/a/b and na", "", "/r/a/b and " },
    { "/r/a/attribute::id", "", "/r/a/" },
    { "<a>/r/a/", "", "<a>/r/a/" },
    { "<a><!-- /r/a/", "", "<a><!-- /r/a/" },
    { "<a>don't /r/a/", "", "<a>don't /r/a/" },
    { "<a>x</a>/r/a/", "@id,b,b2", "<a>x</a>/r/a/" },
    { "for $e in <a>x</a> return /r/a/", "@id,b,b2", "for $e in <a>x</a> return /r/a/" },
    { "for $e in <a>{ /r/a/", "@id,b,b2", "for $e in <a>{ /r/a/" },
    { "<a><![CDATA[ /r/a/", "", "<a><![CDATA[ /r/a/" },
    { "<a>{ <b>x</b>, /r/a/", "@id,b,b2", "<a>{ <b>x</b>, /r/a/" },
    { "<a><b/>/r/a/", "", "<a><b/>/r/a/" },
    { "1 <a and /r/a/", "@id,b,b2", "1 <a and /r/a/" },
    { "<a>{{ /r/", "", "<a>{{ /r/" },
    { "<\u3007>/r/a/", "", "<\u3007>/r/a/" },
    { "<\u200C>/r/a/", "", "<\u200C>/r/a/" },
    { "<\u3007>{ /r/a/", "@id,b,b2", "<\u3007>{ /r/a/" },
    { "for $x in if(1<2) then /r/a/", "@id,b,b2", "for $x in if(1<2) then /r/a/" },
    { "for $x in if(1 < 2) then /r/a/", "@id,b,b2", "for $x in if(1 < 2) then /r/a/" },
    { "let $x := if(1<2) then /r/a/", "@id,b,b2", "let $x := if(1<2) then /r/a/" },
    { "let $x := if(1<2) then /r/a/b[", "c,d", "let $x := if(1<2) then /r/a/b[" },
    { "<for>/r/a/", "", "<for>/r/a/" },
    { "<a b=\"v\">/r/a/", "", "<a b=\"v\">/r/a/" },
    { "for $x in /r/a where 1<2 return /r/", "a,ab,abc", "for $x in /r/a where 1<2 return /r/" },
    { "<NaN x='a'>/r/a/", "", "<NaN x='a'>/r/a/" },
    { "<NaN>/r/a/", "", "<NaN>/r/a/" },
  };

  /** Queries with other suggestions of the hand-written parser. */
  private static final String[][] CASES_MANUAL = {
    { "//@", "@id,@k,@x,a,ab,abc,b,b2,c,d,r", "//" },
    { "/r/a/@", "@id,b,b2", "/r/a/" },
    { "/r/a/@i", "", "/r/a/" },
    { "/r/a[b", "b,b2", "/r/a[b" },
    { "/r/a[b/c", "", "/r/a[b/c" },
    { "/r/a[b/c]", "", "/r/a[b/" },
    { "/r/a[@id", "", "/r/a[@id" },
    { "/r/a[@id = '1' and ", "", "/r/a[@id = '1' and " },
    { "/r/a[b][", "", "/r/a[b][" },
    { "/r/a[b][b/", "", "/r/a[b][b/" },
    { "/r/descendant::c", "", "/r/" },
    { "/r/a/..", "", "/r/a/" },
    { "/r/a/b/.", "c,d", "/r/a/b/" },
    { "string(/r/a/@", "@id,b,b2", "string(/r/a/" },
    { "/r/a/text()", "", "/r/a/" },
    { "declare variable $x := /r/a/; ", "@id,b,b2", "declare variable $x := /r/a/" },
    { "/r/a/child::element(b", "@id,b,b2", "/r/a/child::element(b" },
    { "(/)", "r", "(/" },
    { "count(/r/a)", "a", "count(/r/" },
    { "/r/a/b/c/..", "", "/r/a/b/c/" },
    { "/r/a[@i", "", "/r/a[@i" },
    { "string(/r/a/@i", "", "string(/r/a/@i" },
    { "/r/element(a", "a,ab,abc", "/r/element(a" },
    { "/r/element(", "a,ab,abc", "/r/element(" },
    { "/r/child::a", "a,ab,abc", "/r/" },
    { "for $x in /r/a/b", "b,b2", "for $x in /r/a/b" },
    { "count(//element(*", "@id,@k,@x,a,ab,abc,b,b2,c,d,r", "count(//element(*" },
    { "/r/a/attribute::i", "", "/r/a/" },
    { "/r/a/attribute::", "@id,b,b2", "/r/a/attribute::" },
    { "<a b=\"{ /r/", "", "<a b=\"{ /r/" },
    { "<p:a>/r/a/", "", "<" },
  };

  /** The queries of {@link #CASES_MANUAL}, with the suggestions of the generated parser. */
  private static final String[][] CASES_REX = {
    { "//@", "@id,@k,@x", "//" },
    { "/r/a/@", "@id", "/r/a/" },
    { "/r/a/@i", "@id", "/r/a/" },
    { "/r/a[b", "b,b2", "/r/a[" },
    { "/r/a[b/c", "", "/r/a[b/" },
    { "/r/a[b/c]", "", "/r/a[b/c]" },
    { "/r/a[@id", "", "/r/a[" },
    { "/r/a[@id = '1' and ", "@id,b,b2", "/r/a[@id = '1' and " },
    { "/r/a[b][", "@id,b,b2", "/r/a[b][" },
    { "/r/a[b][b/", "c,d", "/r/a[b][b/" },
    { "/r/descendant::c", "", "/r/descendant::" },
    { "/r/a/..", "", "/r/a/.." },
    { "/r/a/b/.", "c,d", "/r/a/b/." },
    { "string(/r/a/@", "@id", "string(/r/a/" },
    { "/r/a/text()", "", "/r/a/text()" },
    { "declare variable $x := /r/a/; ", "", "declare variable $x := /r/a/; " },
    { "/r/a/child::element(b", "b,b2", "/r/a/child::element(" },
    { "(/)", "", "(/)" },
    { "count(/r/a)", "", "count(/r/a)" },
    { "/r/a/b/c/..", "", "/r/a/b/c/.." },
    { "/r/a[@i", "@id", "/r/a[" },
    { "string(/r/a/@i", "@id", "string(/r/a/" },
    { "/r/element(a", "a,ab,abc", "/r/element(" },
    { "/r/element(", "", "/r/" },
    { "/r/child::a", "a,ab,abc", "/r/child::" },
    { "for $x in /r/a/b", "b,b2", "for $x in /r/a/" },
    { "count(//element(*", "", "count(//element(" },
    { "/r/a/attribute::i", "@id", "/r/a/" },
    { "/r/a/attribute::", "@id", "/r/a/" },
    { "<a b=\"{ /r/", "a,ab,abc", "<a b=\"{ /r/" },
    { "<p:a>/r/a/", "", "<p:a>/r/a/" },
  };

  /** Queries for the document with names, with the suggestions of both parsers. */
  private static final String[][] CASES_NAMES = {
    { "/r/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/*:a", "a,ab", "/r/" },
    { "/r/*:a/", "", "/r/*:a/" },
    { "//*:b", "", "//" },
    { "//*:b/", "", "//*:b/" },
    { "/r/a/", "", "/r/a/" },
    { "/r/Q{urn:p}a/", "", "/r/Q{urn:p}a/" },
    { "/r/\uD835\uDCB3", "", "/r/" },
    { "/r/\uD835\uDCB3/", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5", "", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/", "", "/r/\uD835\uDCB3/\uD835\uDCB5/" },
    { "/r/\uD835\uDCB3/@\uD835\uDCB4", "", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/@\uD835\uDCB4/", "", "/r/\uD835\uDCB3/@\uD835\uDCB4/" },
    { "/r/\uD835\uDCB3[", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3[" },
    { "/r/\uD835\uDCB3[\uD835\uDCB5/", "", "/r/\uD835\uDCB3[\uD835\uDCB5/" },
    { "//\uD835\uDCB5", "", "//" },
    { "//\uD835\uDCB5/", "", "//\uD835\uDCB5/" },
    { "/r/\u00FC", "", "/r/" },
    { "/r/\u00FC/", "", "/r/\u00FC/" },
    { "/r/ab/\u00FC", "", "/r/ab/" },
    { "(: \uD835\uDCB3 :) /r/\uD835\uDCB3/",
      "@\uD835\uDCB4,\uD835\uDCB5", "(: \uD835\uDCB3 :) /r/\uD835\uDCB3/" },
    { "\"\uD835\uDCB3\" || /r/\uD835\uDCB3/",
      "@\uD835\uDCB4,\uD835\uDCB5", "\"\uD835\uDCB3\" || /r/\uD835\uDCB3/" },
    { "\"\uD835\uDCB3\"[/r/\uD835\uDCB3/",
      "@\uD835\uDCB4,\uD835\uDCB5", "\"\uD835\uDCB3\"[/r/\uD835\uDCB3/" },
    { "\"\uD835\uDCB3\" and /r/\uD835\uDCB3", "", "\"\uD835\uDCB3\" and /r/" },
    { "for $\uD835\uDCB3 in /r/\uD835\uDCB3 return /r/\uD835\uDCB3/",
      "@\uD835\uDCB4,\uD835\uDCB5", "for $\uD835\uDCB3 in /r/\uD835\uDCB3 return /r/\uD835\uDCB3/"
      },
    { "for $\uD835\uDCB3 in /r/\uD835\uDCB3/",
      "@\uD835\uDCB4,\uD835\uDCB5", "for $\uD835\uDCB3 in /r/\uD835\uDCB3/" },
    { "\uD835\uDCB3 + /r/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "\uD835\uDCB3 + /r/" },
    { "\uD835\uDCB3/", "", "\uD835\uDCB3/" },
    { "/\uD835\uDCB3", "", "/" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/\uD835\uDCB5/",
      "", "/r/\uD835\uDCB3/\uD835\uDCB5/\uD835\uDCB5/" },
    { "declare namespace p = \"urn:p\"; /r/",
      "a,ab,p:a,\u00FC,\uD835\uDCB3", "declare namespace p = \"urn:p\"; /r/" },
    { "declare namespace p = \"urn:p\"; /r/p:a", "", "declare namespace p = \"urn:p\"; /r/" },
    { "declare namespace p = \"urn:p\"; /r/p:a/",
      "@id,p:b,q:b", "declare namespace p = \"urn:p\"; /r/p:a/" },
    { "declare namespace p = \"urn:p\"; /r/p:a/p:b/",
      "", "declare namespace p = \"urn:p\"; /r/p:a/p:b/" },
    { "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/q:b/",
      "", "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/q:b/" },
    { "declare namespace p = \"urn:p\"; /r/p:*/", "", "declare namespace p = \"urn:p\"; /r/p:*/" },
    { "declare namespace p = \"urn:p\"; //p:b/", "", "declare namespace p = \"urn:p\"; //p:b/" },
    { "declare namespace p = \"urn:p\"; /r/p:a[p:b/",
      "", "declare namespace p = \"urn:p\"; /r/p:a[p:b/" },
    { "declare default element namespace \"urn:p\"; /r/a/",
      "", "declare default element namespace \"urn:p\"; /r/a/" },
    { "declare namespace p = \"urn:p\"; /r/Q{urn:p}a/",
      "", "declare namespace p = \"urn:p\"; /r/Q{urn:p}a/" },
    { "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/",
      "", "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/" },
    { "declare namespace p = \"urn:p\"; (: \uD835\uDCB3 :) /r/p:a/",
      "@id,p:b,q:b", "declare namespace p = \"urn:p\"; (: \uD835\uDCB3 :) /r/p:a/" },
    { "declare namespace p = \"urn:p\"; for $\uD835\uDCB3 in /r/p:a return $\uD835\uDCB3/",
      "r", "declare namespace p = \"urn:p\"; for $\uD835\uDCB3 in /r/p:a return $\uD835\uDCB3/" },
    { "for $\uD835\uDCB3 in /r/\uD835\uDCB3/\uD835\uDCB5/",
      "", "for $\uD835\uDCB3 in /r/\uD835\uDCB3/\uD835\uDCB5/" },
    { "/r/\uD835\uDCB3/element(\uD835\uDCB5)/", "", "/r/\uD835\uDCB3/element(\uD835\uDCB5)/" },
  };

  /** Queries for the document with names, with other suggestions of the hand-written parser. */
  private static final String[][] CASES_NAMES_MANUAL = {
    { "/r/p:", "", "/r/p:" },
    { "/r/p:a", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/p:", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/p:b", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/q:", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/*:", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/*:b", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/*:b/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:*", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:*/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "//p:b",
      "@id,@\uD835\uDCB4,a,ab,p:a,p:b,q:b,r,text(),\u00FC,\uD835\uDCB3,\uD835\uDCB5", "//" },
    { "/r//p:", "", "/r//p:" },
    { "/r/p:a/@", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a/@i", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a[", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a[p:", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a[p:b", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/p:a[q:b/", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/Q{urn:p}", "", "/r/Q{" },
    { "/r/p:a/p:b/..", "a,ab,p:a,\u00FC,\uD835\uDCB3", "/r/" },
    { "/r/\uD835\uDCB3/@", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3[\uD835\uDCB5", "", "/r/\uD835\uDCB3[\uD835\uDCB5" },
    { "/r/\uD835\uDCB3[@", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3[" },
    { "//\uD835\uDCB3/@", "@\uD835\uDCB4,\uD835\uDCB5", "//\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/ )", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/ " },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/)", "", "/r/\uD835\uDCB3/\uD835\uDCB5/" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/ ]", "", "/r/\uD835\uDCB3/\uD835\uDCB5/ " },
    { "/r/\uD835\uDCB3/!", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/ 1", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/p:", "", "/r/\uD835\uDCB3/p:" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/p:", "", "/r/\uD835\uDCB3/\uD835\uDCB5/p:" },
    { "declare namespace p = \"urn:p\"; /r/p:", "", "declare namespace p = \"urn:p\"; /r/p:" },
    { "declare namespace p = \"urn:p\"; /r/p:a/p:",
      "", "declare namespace p = \"urn:p\"; /r/p:a/p:" },
    { "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/q:",
      "", "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/q:" },
    { "declare namespace p = \"urn:p\"; /r/p:a[p:",
      "", "declare namespace p = \"urn:p\"; /r/p:a[p" },
    { "declare namespace p = \"urn:p\"; /r/p:a/@",
      "@id,p:b,q:b", "declare namespace p = \"urn:p\"; /r/p:a/" },
    { "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/\uD835\uDCAB:",
      "", "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/\uD835\uDCAB:" },
  };

  /** The queries of {@link #CASES_NAMES_MANUAL}, with the suggestions of the generated parser. */
  private static final String[][] CASES_NAMES_REX = {
    { "/r/p:", "p:a", "/r/" },
    { "/r/p:a", "", "/r/" },
    { "/r/p:a/", "@id,p:b,q:b", "/r/p:a/" },
    { "/r/p:a/p:", "p:b", "/r/p:a/" },
    { "/r/p:a/p:b", "", "/r/p:a/" },
    { "/r/p:a/q:", "q:b", "/r/p:a/" },
    { "/r/p:a/*:", "@id,p:b,q:b", "/r/p:a/" },
    { "/r/p:a/*:b", "", "/r/p:a/" },
    { "/r/p:a/*:b/", "", "/r/p:a/*:b/" },
    { "/r/p:*", "", "/r/" },
    { "/r/p:*/", "", "/r/p:*/" },
    { "//p:b", "", "//" },
    { "/r//p:", "p:a,p:b", "/r//" },
    { "/r/p:a/@", "@id", "/r/p:a/" },
    { "/r/p:a/@i", "@id", "/r/p:a/" },
    { "/r/p:a[", "@id,p:b,q:b", "/r/p:a[" },
    { "/r/p:a[p:", "p:b", "/r/p:a[" },
    { "/r/p:a[p:b", "", "/r/p:a[" },
    { "/r/p:a[q:b/", "", "/r/p:a[q:b/" },
    { "/r/Q{urn:p}", "", "/r/Q{urn:p}" },
    { "/r/p:a/p:b/..", "", "/r/p:a/p:b/.." },
    { "/r/\uD835\uDCB3/@", "@\uD835\uDCB4", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3[\uD835\uDCB5", "", "/r/\uD835\uDCB3[" },
    { "/r/\uD835\uDCB3[@", "@\uD835\uDCB4", "/r/\uD835\uDCB3[" },
    { "//\uD835\uDCB3/@", "@\uD835\uDCB4", "//\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/ )", "", "/r/\uD835\uDCB3/ )" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/)", "", "/r/\uD835\uDCB3/\uD835\uDCB5/)" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/ ]", "", "/r/\uD835\uDCB3/\uD835\uDCB5/ ]" },
    { "/r/\uD835\uDCB3/!", "", "/r/\uD835\uDCB3/!" },
    { "/r/\uD835\uDCB3/ 1", "@\uD835\uDCB4,\uD835\uDCB5", "/r/\uD835\uDCB3/ 1" },
    { "/r/\uD835\uDCB3/p:", "", "/r/\uD835\uDCB3/" },
    { "/r/\uD835\uDCB3/\uD835\uDCB5/p:", "", "/r/\uD835\uDCB3/\uD835\uDCB5/" },
    { "declare namespace p = \"urn:p\"; /r/p:", "p:a", "declare namespace p = \"urn:p\"; /r/" },
    { "declare namespace p = \"urn:p\"; /r/p:a/p:",
      "p:b", "declare namespace p = \"urn:p\"; /r/p:a/" },
    { "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/q:",
      "q:b", "declare namespace p = \"urn:p\"; declare namespace q = \"urn:q\"; /r/p:a/" },
    { "declare namespace p = \"urn:p\"; /r/p:a[p:",
      "p:b", "declare namespace p = \"urn:p\"; /r/p:a[" },
    { "declare namespace p = \"urn:p\"; /r/p:a/@",
      "@id", "declare namespace p = \"urn:p\"; /r/p:a/" },
    { "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/\uD835\uDCAB:",
      "", "declare namespace \uD835\uDCAB = \"urn:p\"; /r/\uD835\uDCAB:a/" },
  };

  /** Tests the suggestions of the generated parser. */
  @Test public void rex() {
    check(XML, MainOptions.QueryParserType.REX, CASES, CASES_REX);
    check(XML_NAMES, MainOptions.QueryParserType.REX, CASES_NAMES, CASES_NAMES_REX);
  }

  /** Tests the suggestions of the hand-written parser. */
  @Test public void manual() {
    check(XML, MainOptions.QueryParserType.MANUAL, CASES, CASES_MANUAL);
    check(XML_NAMES, MainOptions.QueryParserType.MANUAL, CASES_NAMES, CASES_NAMES_MANUAL);
  }

  /**
   * Checks the suggestions for queries.
   * @param xml document
   * @param parser query parser: only the suggestions of the generated parser are checked for
   *   repeated text
   * @param common queries with the same suggestions for both parsers
   * @param specific queries with the suggestions of the parser
   */
  private static void check(final String xml, final MainOptions.QueryParserType parser,
      final String[][] common, final String[][] specific) {
    final String[][] cases = Stream.concat(Stream.of(common), Stream.of(specific)).
        toArray(String[][]::new);
    final MainOptions.QueryParserType previous = context.options.get(MainOptions.QUERYPARSER);
    set(MainOptions.QUERYPARSER, parser);
    // the internal parser supports all name characters
    set(MainOptions.INTPARSE, true);
    try {
      execute(new CreateDB(NAME, xml));
      final StringBuilder errors = new StringBuilder();
      for(final String[] test : cases) {
        final String query = test[0];
        try(QueryContext qc = new QueryContext(context)) {
          final var list = qc.suggest(query, context.data());
          final String valid = list.pop();
          final String suggestions = String.join(",", list.toArray());
          if(!suggestions.equals(test[1]) || !valid.equals(test[2])) {
            errors.append("\n  ").append(query).append("\n    expected: [").append(test[1]).
              append("] ").append(test[2]).append("\n    found:    [").append(suggestions).
              append("] ").append(valid);
          }
          // the text that follows the valid input is the start of each suggestion: no repetition
          final String rest = query.substring(valid.length());
          if(parser == MainOptions.QueryParserType.REX && !rest.isEmpty() &&
              !rest.contains("::") && !rest.startsWith("*:")) {
            for(final String suggestion : list) {
              if(!suggestion.startsWith(rest)) {
                errors.append("\n  ").append(query).append("\n    insertion: ").append(valid).
                  append(suggestion);
              }
            }
          }
        }
      }
      if(!errors.isEmpty()) fail(errors.toString());
    } finally {
      set(MainOptions.INTPARSE, false);
      set(MainOptions.QUERYPARSER, previous);
      execute(new DropDB(NAME));
    }
  }
}
