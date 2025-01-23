package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.build.csv.CsvOptions.*;
import org.junit.jupiter.api.*;

/**
 * This class roundtrips CSV data through parsing and serialization.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvRoundtripTest extends SandboxTest {
  /** Test method. */
  @Test public void csvParse() {
    final Function func = _CSV_PARSE;
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 17), 'row-"
        + "delimiter': '|'", "<csv><record><entry>1</entry><entry>4</entry><entry/></record><record"
            + "><entry>11</entry><entry>14</entry><entry/></record></csv>");
    roundtrip(func, "a,b,c,d|1,2,3,4|11,12,13,14", "'format': 'attributes', 'header': true(), 'sele"
        + "ct-columns': (1, 4, 2), 'row-delimiter': '|'", "<csv><record><entry name=\"a\">1</entry>"
        + "<entry name=\"d\">4</entry><entry name=\"b\">2</entry></record><record><entry name=\"a\""
        + ">11</entry><entry name=\"d\">14</entry><entry name=\"b\">12</entry></record></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'format': 'xquery', 'select-columns': (1, 4, 17), 'row-"
        + "delimiter': '|'", "{\"records\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"\"])}");
  }

  /** Test method. */
  @Test public void csvToArrays() {
    final Function func = CSV_TO_ARRAYS;
    roundtrip(func, "", "", "");
    roundtrip(func, "one", "", "[\"one\"]");
    roundtrip(func, "one,two", "", "[\"one\",\"two\"]");
    roundtrip(func, "one,two&#xA;three,four", "", "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "", "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two&#xA;three,four,five", "", "[\"one\",\"two\"]\n[\"three\",\"four\",\"fi"
        + "ve\"]");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "", "[\"one\",\"two\"]\n[]\n[\"three\",\"four\"]"
        );
    roundtrip(func, "one,two&#xA;\"three,four\",five", "", "[\"one\",\"two\"]\n[\"three,four\",\"fi"
        + "ve\"]");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "", "[\"one\",\"two\"]\n[\"three,\""
        + "\"four\"\"\",\"five\"]");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "", "[\"one\",\"\"]\n[\"\",\"four\"]\n[\"\",\""
        + "\"]\n[\"\",\"\",\"\",\"\"]");
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "", "[\"one\",\"\"]\n[\"\",\"four\"]");
    roundtrip(func, "one,\"[&#xA;]\"&#xA;\"\",\"four\"", "", "[\"one\",\"[&#xA;]\"]\n[\"\",\"four\""
        + "]");
    roundtrip(func, "one;two&#xA;three;four", "'field-delimiter': ';'", "[\"one\",\"two\"]\n[\"thre"
        + "e\",\"four\"]");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|'", "[\"one\",\"two\"]\n[\"three\",\""
        + "four\"]");
    roundtrip(func, "one.two|three.four", "'row-delimiter': '|', 'field-delimiter': '.'", "[\"one\""
        + ",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,'two,2'|three,'four,4'", "'row-delimiter': '|', 'quote-character': ''''",
        "[\"one\",\"two,2\"]\n[\"three\",\"four,4\"]");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''", "'row-delimiter': '|', 'quote-character':"
        + " ''''", "[\"one\",\"two,'2'\"]\n[\"three\",\"four,'4'\"]");
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|'", "[\"one \",\"two \"]\n[\" thr"
        + "ee\",\" four\"]");
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|', 'trim-whitespace': false()",
        "[\"one \",\"two \"]\n[\" three\",\" four\"]");
    roundtrip(func, "one ,two | three, twenty  four ", "'row-delimiter': '|', 'trim-whitespace': tr"
        + "ue()", "[\"one\",\"two\"]\n[\"three\",\"twenty  four\"]");
    roundtrip(func, "&#xA;", "", "[]");
    roundtrip(func, "&#xA; ", "", "[]\n[\" \"]");
    roundtrip(func, "&#xA; ", "'trim-whitespace': true()", "[]");
    roundtrip(func, "&#xA;&#xA;", "'trim-whitespace': true()", "[]\n[]");
    roundtrip(func, "&#xA;&#xA;&#xA;", "'trim-whitespace': true()", "[]\n[]\n[]");
    roundtrip(func, "one,two,\"z\"", "", "[\"one\",\"two\",\"z\"]");
    roundtrip(func, "one,two,\"z\"&#xA;", "", "[\"one\",\"two\",\"z\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c|p,q,r", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\"]\n[\"p\",\"q\",\"r\""
        + "]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|'", "[\"a\",\"b\",\"c\",\"d\",\""
        + "e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
  }

  /** Test method. */
  @Test public void parseCsv() {
    final Function func = PARSE_CSV;
    roundtrip(func, " ()", "", "{\"columns\":(),\"column-index\":{},\"rows\":(),\"get\":(anonymous-"
        + "function)#2}");
    roundtrip(func, "", "", "{\"columns\":(),\"column-index\":{},\"rows\":(),\"get\":(anonymous-fun"
        + "ction)#2}");
    roundtrip(func, "one", "", "{\"columns\":(),\"column-index\":{},\"rows\":[\"one\"],\"get\":(ano"
        + "nymous-function)#2}");
    roundtrip(func, "one,two", "", "{\"columns\":(),\"column-index\":{},\"rows\":[\"one\",\"two\"],"
        + "\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;three,four", "", "{\"columns\":(),\"column-index\":{},\"rows\":(["
        + "\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "", "{\"columns\":(),\"column-index\":{},\"rows"
        + "\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;three,four,five", "", "{\"columns\":(),\"column-index\":{},\"rows"
        + "\":([\"one\",\"two\"],[\"three\",\"four\",\"five\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "", "{\"columns\":(),\"column-index\":{},\"rows"
        + "\":([\"one\",\"two\"],[],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;\"three,four\",five", "", "{\"columns\":(),\"column-index\":{},\"r"
        + "ows\":([\"one\",\"two\"],[\"three,four\",\"five\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "", "{\"columns\":(),\"column-index"
        + "\":{},\"rows\":([\"one\",\"two\"],[\"three,\"\"four\"\"\",\"five\"]),\"get\":(anonymous"
        + "-function)#2}");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "", "{\"columns\":(),\"column-index\":{},\"rows"
        + "\":([\"one\",\"\"],[\"\",\"four\"],[\"\",\"\"],[\"\",\"\",\"\",\"\"]),\"get\":(anonymous"
        + "-function)#2}");
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "", "{\"columns\":(),\"column-index\":{},\"rows\""
        + ":([\"one\",\"\"],[\"\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,\"[&#xA;]\"&#xA;\"\",\"four\"", "", "{\"columns\":(),\"column-index\":{},"
        + "\"rows\":([\"one\",\"[&#xA;]\"],[\"\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one;two&#xA;three;four", "'field-delimiter': ';'", "{\"columns\":(),\"column-i"
        + "ndex\":{},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)"
        + "#2}");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|'", "{\"columns\":(),\"column-index\""
        + ":{},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one.two|three.four", "'row-delimiter': '|', 'field-delimiter': '.'", "{\"colum"
        + "ns\":(),\"column-index\":{},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(a"
        + "nonymous-function)#2}");
    roundtrip(func, "one,'two,2'|three,'four,4'", "'row-delimiter': '|', 'quote-character': ''''",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"one\",\"two,2\"],[\"three\",\"four,4\"]),"
        + "\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''", "'row-delimiter': '|', 'quote-character':"
        + " ''''", "{\"columns\":(),\"column-index\":{},\"rows\":([\"one\",\"two,'2'\"],[\"three\","
        + "\"four,'4'\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|'", "{\"columns\":(),\"column-ind"
        + "ex\":{},\"rows\":([\"one \",\"two \"],[\" three\",\" four\"]),\"get\":(anonymous-functio"
        + "n)#2}");
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|', 'trim-whitespace': false()", ""
        + "{\"columns\":(),\"column-index\":{},\"rows\":([\"one \",\"two \"],[\" three\",\" four\"]"
        + "),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one ,two | three, twenty  four ", "'row-delimiter': '|', 'trim-whitespace': tr"
        + "ue()", "{\"columns\":(),\"column-index\":{},\"rows\":([\"one\",\"two\"],[\"three\",\"twe"
        + "nty  four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "&#xA;", "", "{\"columns\":(),\"column-index\":{},\"rows\":[],\"get\":(anonymou"
        + "s-function)#2}");
    roundtrip(func, "&#xA; ", "", "{\"columns\":(),\"column-index\":{},\"rows\":([],[\" \"]),\"get"
        + "\":(anonymous-function)#2}");
    roundtrip(func, "&#xA; ", "'trim-whitespace': true()", "{\"columns\":(),\"column-index\":{},\"r"
        + "ows\":[],\"get\":(anonymous-function)#2}");
    roundtrip(func, "&#xA;&#xA;", "'trim-whitespace': true()", "{\"columns\":(),\"column-index\":{}"
        + ",\"rows\":([],[]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "&#xA;&#xA;&#xA;", "'trim-whitespace': true()", "{\"columns\":(),\"column-index"
        + "\":{},\"rows\":([],[],[]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "left,right|one,two|three,four", "'row-delimiter': '|', 'header': true()", "{\""
        + "columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2},\"rows\":([\"on"
        + "e\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': ('left', 'right')", "{\""
        + "columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2},\"rows\":([\"on"
        + "e\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': false()", "{\"columns\":"
        + "(),\"column-index\":{},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get\":(anonym"
        + "ous-function)#2}");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': 'left'", "{\"columns\":"
        + "\"left\",\"column-index\":{\"left\":1},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])"
        + ",\"get\":(anonymous-function)#2}");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': ('', 'right')", "{\"colu"
        + "mns\":(\"\",\"right\"),\"column-index\":{\"right\":2},\"rows\":([\"one\",\"two\"],[\"thr"
        + "ee\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "left,left|one,two|three,four", "'row-delimiter': '|', 'header': true()", "{\"c"
        + "olumns\":(\"left\",\"left\"),\"column-index\":{\"left\":1},\"rows\":([\"one\",\"two\"],["
        + "\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, ",right|one,two|three,four", "'row-delimiter': '|', 'header': true()", "{\"colu"
        + "mns\":(\"\",\"right\"),\"column-index\":{\"right\":2},\"rows\":([\"one\",\"two\"],[\"thr"
        + "ee\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, ",|one,two|three,four", "'row-delimiter': '|', 'header': true()", "{\"columns\""
        + ":(\"\",\"\"),\"column-index\":{},\"rows\":([\"one\",\"two\"],[\"three\",\"four\"]),\"get"
        + "\":(anonymous-function)#2}");
    roundtrip(func, "left,right", "'row-delimiter': '|', 'header': true()", "{\"columns\":(\"left\""
        + ",\"right\"),\"column-index\":{\"left\":1,\"right\":2},\"rows\":(),\"get\":(anonymous-fun"
        + "ction)#2}");
    roundtrip(func, "1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-delimiter': '|', 's"
        + "elect-columns': (1 to 4)", "{\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"2\","
            + "\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f,g,h,i|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-d"
        + "elimiter': '|', 'select-columns': (1 to 4), 'header': true()", "{\"columns\":(\"a\",\"b"
        + "\",\"c\",\"d\"),\"column-index\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4},\"rows\":([\"1\",\"2"
        + "\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16,17,18,19,20", "'row-delimiter': '|', 'trim-rows': tr"
        + "ue()", "{\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\","
            + "\"12\",\"13\",\"14\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-delimiter':"
        + " '|', 'trim-rows': true(), 'header': true()", "{\"columns\":(\"a\",\"b\",\"c\",\"d\"),\""
        + "column-index\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4},\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\""
        + "11\",\"12\",\"13\",\"14\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16", "'row-delimiter': '|', 'trim-rows': false(), 'head"
        + "er': false()", "{\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"2\",\"3\",\"4\"],"
        + "[\"11\",\"12\",\"13\",\"14\",\"15\",\"16\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4,5,6|14,15,16", "'row-delimiter': '|', 'select-columns': (1 to 4)", "{"
        + "\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"14\",\"15\",\""
        + "16\",\"\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4,5,6|14,15,16", "'row-delimiter': '|', 'trim-rows': true()", "{\"column"
        + "s\":(),\"column-index\":{},\"rows\":([\"1\",\"2\",\"3\",\"4\",\"5\",\"6\"],[\"14\",\"15"
        + "\",\"16\",\"\",\"\",\"\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e|1,2,3|14,15,16", "'row-delimiter': '|', 'trim-rows': true(), 'header"
        + "': true()", "{\"columns\":(\"a\",\"b\",\"c\",\"d\",\"e\"),\"column-index\":{\"a\":1,\"b"
        + "\":2,\"c\":3,\"d\":4,\"e\":5},\"rows\":([\"1\",\"2\",\"3\",\"\",\"\"],[\"14\",\"15\",\"1"
        + "6\",\"\",\"\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (4, 3, 2, 1), 'row-delimiter': '|'",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"4\",\"3\",\"2\",\"1\"],[\"14\",\"13\",\"1"
        + "2\",\"11\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4), 'row-delimiter': '|'", "{\"co"
        + "lumns\":(),\"column-index\":{},\"rows\":([\"1\",\"4\"],[\"11\",\"14\"]),\"get\":(anonymo"
        + "us-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 17), 'row-delimiter': '|'", "{"
        + "\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"\"]),\""
        + "get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 17, 4), 'row-delimiter': '|'", "{"
        + "\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"\",\"4\"],[\"11\",\"\",\"14\"]),\""
        + "get\":(anonymous-function)#2}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15", "'select-columns': (1, 4, 5), 'row-delimiter': '|'",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"15\"]),"
        + "\"get\":(anonymous-function)#2}");
    roundtrip(func, "first,second,third,fourth|1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 3), "
        + "'header': true(), 'row-delimiter': '|'", "{\"columns\":(\"first\",\"fourth\",\"third\"),"
        + "\"column-index\":{\"first\":1,\"fourth\":2,\"third\":3},\"rows\":([\"1\",\"4\",\"3\"],["
        + "\"11\",\"14\",\"13\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (1 to 3)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"a\",\"b\",\"c\"],[\"p\",\"q\",\"r\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (2 to 4)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"b\",\"c\",\"d\"],[\"q\",\"r\",\"s\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 2)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"b\"],[\"s\",\"r\",\"q\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"]),\"g"
        + "et\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"]),\""
        + "get\":(anonymous-function)#2}");
    roundtrip(func, "left,right|one,two|three,four", "'row-delimiter': '|', 'header': true()", "{\""
        + "columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2},\"rows\":([\"on"
        + "e\",\"two\"],[\"three\",\"four\"]),\"get\":(anonymous-function)#2}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u", "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
        "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"]),\"g"
        + "et\":(anonymous-function)#2}");
  }

  /** Test method. */
  @Test public void csvToXml() {
    final Function func = CSV_TO_XML;
    roundtrip(func, "", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows/></csv>");
    roundtrip(func, "one", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><f"
        + "ield>one</field></row></rows></csv>");
    roundtrip(func, "one,two", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><ro"
        + "w><field>one</field><field>two</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-functi"
        + "ons\"><rows><row><field>one</field><field>two</field></row><row><field>three</field><fie"
        + "ld>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-f"
        + "unctions\"><rows><row><field>one</field><field>two</field></row><row><field>three</field"
        + "><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four,five", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-f"
        + "unctions\"><rows><row><field>one</field><field>two</field></row><row><field>three</field"
        + "><field>four</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-f"
        + "unctions\"><rows><row><field>one</field><field>two</field></row><row/><row><field>three<"
        + "/field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;\"three,four\",five", "", "<csv xmlns=\"http://www.w3.org/2005/xpa"
        + "th-functions\"><rows><row><field>one</field><field>two</field></row><row><field>three,fo"
        + "ur</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "", "<csv xmlns=\"http://www.w3.org/"
        + "2005/xpath-functions\"><rows><row><field>one</field><field>two</field></row><row><field>"
        + "three,\"four\"</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-"
        + "functions\"><rows><row><field>one</field><field/></row><row><field/><field>four</field><"
        + "/row><row><field/><field/></row><row><field/><field/><field/><field/></row></rows></csv>"
        );
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-fu"
        + "nctions\"><rows><row><field>one</field><field/></row><row><field/><field>four</field></r"
        + "ow></rows></csv>");
    roundtrip(func, "one;two&#xA;three;four", "'field-delimiter': ';'", "<csv xmlns=\"http://www.w3"
        + ".org/2005/xpath-functions\"><rows><row><field>one</field><field>two</field></row><row><f"
        + "ield>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|'", "<csv xmlns=\"http://www.w3.org/2"
        + "005/xpath-functions\"><rows><row><field>one</field><field>two</field></row><row><field>t"
        + "hree</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one.two|three.four", "'row-delimiter': '|', 'field-delimiter': '.'", "<csv xml"
        + "ns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>one</field><field>two</f"
        + "ield></row><row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,'two,2'|three,'four,4'", "'row-delimiter': '|', 'quote-character': ''''",
        "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>one</field><field>"
        + "two,2</field></row><row><field>three</field><field>four,4</field></row></rows></csv>");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''", "'row-delimiter': '|', 'quote-character':"
        + " ''''",
        "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>one</field><field>"
        + "two,'2'</field></row><row><field>three</field><field>four,'4'</field></row></rows></csv>"
        );
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|'", "<csv xmlns=\"http://www.w3.o"
        + "rg/2005/xpath-functions\"><rows><row><field>one </field><field>two </field></row><row><f"
        + "ield> three</field><field> four</field></row></rows></csv>");
    roundtrip(func, "one ,two | three, four", "'row-delimiter': '|', 'trim-whitespace': false()",
        "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>one </field><field"
        + ">two </field></row><row><field> three</field><field> four</field></row></rows></csv>");
    roundtrip(func, "one ,two | three, twenty  four ", "'row-delimiter': '|', 'trim-whitespace': tr"
        + "ue()", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>one</fie"
        + "ld><field>two</field></row><row><field>three</field><field>twenty  four</field></row></r"
        + "ows></csv>");
    roundtrip(func, "&#xA;", "", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row/"
        + "></rows></csv>");
    roundtrip(func, "left,right|one,two|three,four", "'row-delimiter': '|', 'header': true()", "<cs"
        + "v xmlns=\"http://www.w3.org/2005/xpath-functions\"><columns><column>left</column><column"
        + ">right</column></columns><rows><row><field column=\"left\">one</field><field column=\"ri"
        + "ght\">two</field></row><row><field column=\"left\">three</field><field column=\"right\">"
        + "four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': ('left', 'right')", "<cs"
        + "v xmlns=\"http://www.w3.org/2005/xpath-functions\"><columns><column>left</column><column"
        + ">right</column></columns><rows><row><field column=\"left\">one</field><field column=\"ri"
        + "ght\">two</field></row><row><field column=\"left\">three</field><field column=\"right\">"
        + "four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': false()", "<csv xmlns=\""
        + "http://www.w3.org/2005/xpath-functions\"><rows><row><field>one</field><field>two</field>"
        + "</row><row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': 'left'", "<csv xmlns=\"h"
        + "ttp://www.w3.org/2005/xpath-functions\"><columns><column>left</column></columns><rows><r"
        + "ow><field column=\"left\">one</field><field>two</field></row><row><field column=\"left\""
        + ">three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': ('', 'right')", "<csv xm"
        + "lns=\"http://www.w3.org/2005/xpath-functions\"><columns><column/><column>right</column><"
        + "/columns><rows><row><field>one</field><field column=\"right\">two</field></row><row><fie"
        + "ld>three</field><field column=\"right\">four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|', 'header': ()", "<csv xmlns=\"http:"
        + "//www.w3.org/2005/xpath-functions\"><rows><row><field>one</field><field>two</field></row"
        + "><row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "left,left|one,two|three,four", "'row-delimiter': '|', 'header': true()", "<csv"
        + " xmlns=\"http://www.w3.org/2005/xpath-functions\"><columns><column>left</column><column>"
        + "left</column></columns><rows><row><field column=\"left\">one</field><field column=\"left"
        + "\">two</field></row><row><field column=\"left\">three</field><field column=\"left\">four"
        + "</field></row></rows></csv>");
    roundtrip(func, ",right|one,two|three,four", "'row-delimiter': '|', 'header': true()", "<csv xm"
        + "lns=\"http://www.w3.org/2005/xpath-functions\"><columns><column/><column>right</column><"
        + "/columns><rows><row><field>one</field><field column=\"right\">two</field></row><row><fie"
        + "ld>three</field><field column=\"right\">four</field></row></rows></csv>");
    roundtrip(func, ",|one,two|three,four", "'row-delimiter': '|', 'header': true()", "<csv xmlns="
        + "\"http://www.w3.org/2005/xpath-functions\"><columns><column/><column/></columns><rows><r"
        + "ow><field>one</field><field>two</field></row><row><field>three</field><field>four</field"
        + "></row></rows></csv>");
    roundtrip(func, "1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-delimiter': '|', 's"
        + "elect-columns': (1 to 4)", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>"
        + "<row><field>1</field><field>2</field><field>3</field><field>4</field></row><row><field>1"
        + "1</field><field>12</field><field>13</field><field>14</field></row></rows></csv>");
    roundtrip(func, "a,b,c,d,e,f,g,h,i|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-d"
        + "elimiter': '|', 'select-columns': (1 to 4), 'header': true()", "<csv xmlns=\"http://www."
        + "w3.org/2005/xpath-functions\"><columns><column>a</column><column>b</column><column>c</co"
        + "lumn><column>d</column></columns><rows><row><field column=\"a\">1</field><field column="
        + "\"b\">2</field><field column=\"c\">3</field><field column=\"d\">4</field></row><row><fie"
        + "ld column=\"a\">11</field><field column=\"b\">12</field><field column=\"c\">13</field><f"
        + "ield column=\"d\">14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16,17,18,19,20", "'row-delimiter': '|', 'trim-rows': tr"
        + "ue()", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field"
        + "><field>2</field><field>3</field><field>4</field></row><row><field>11</field><field>12</"
        + "field><field>13</field><field>14</field></row></rows></csv>");
    roundtrip(func, "a,b,c,d|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20", "'row-delimiter':"
        + " '|', 'trim-rows': true(), 'header': true()", "<csv xmlns=\"http://www.w3.org/2005/xpath"
        + "-functions\"><columns><column>a</column><column>b</column><column>c</column><column>d</c"
        + "olumn></columns><rows><row><field column=\"a\">1</field><field column=\"b\">2</field><fi"
        + "eld column=\"c\">3</field><field column=\"d\">4</field></row><row><field column=\"a\">11"
        + "</field><field column=\"b\">12</field><field column=\"c\">13</field><field column=\"d\">"
        + "14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16", "'row-delimiter': '|', 'trim-rows': false(), 'head"
        + "er': false()", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>"
        + "1</field><field>2</field><field>3</field><field>4</field></row><row><field>11</field><fi"
        + "eld>12</field><field>13</field><field>14</field><field>15</field><field>16</field></row>"
        + "</rows></csv>");
    roundtrip(func, "1,2,3,4,5,6|14,15,16", "'row-delimiter': '|', 'select-columns': (1 to 4)", "<c"
        + "sv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field>2<"
        + "/field><field>3</field><field>4</field></row><row><field>14</field><field>15</field><fie"
        + "ld>16</field><field/></row></rows></csv>");
    roundtrip(func, "1,2,3,4,5,6|14,15,16", "'row-delimiter': '|', 'trim-rows': true()", "<csv xmln"
        + "s=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field>2</field>"
        + "<field>3</field><field>4</field><field>5</field><field>6</field></row><row><field>14</fi"
        + "eld><field>15</field><field>16</field><field/><field/><field/></row></rows></csv>");
    roundtrip(func, "a,b,c,d,e|1,2,3,4,5|14,15,16", "'row-delimiter': '|', 'trim-rows': true(), 'he"
        + "ader': true()", "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><columns><column>"
        + "a</column><column>b</column><column>c</column><column>d</column><column>e</column></colu"
        + "mns><rows><row><field column=\"a\">1</field><field column=\"b\">2</field><field column="
        + "\"c\">3</field><field column=\"d\">4</field><field column=\"e\">5</field></row><row><fie"
        + "ld column=\"a\">14</field><field column=\"b\">15</field><field column=\"c\">16</field><f"
        + "ield column=\"d\"/><field column=\"e\"/></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (4, 3, 2, 1), 'row-delimiter': '|'",
        "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>4</field><field>3<"
        + "/field><field>2</field><field>1</field></row><row><field>14</field><field>13</field><fie"
        + "ld>12</field><field>11</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4), 'row-delimiter': '|'", "<csv "
        + "xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field>4</fi"
        + "eld></row><row><field>11</field><field>14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 17), 'row-delimiter': '|'", "<"
        + "csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field>4"
        + "</field><field/></row><row><field>11</field><field>14</field><field/></row></rows></csv>"
        );
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 17, 4), 'row-delimiter': '|'", "<"
        + "csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field/>"
        + "<field>4</field></row><row><field>11</field><field/><field>14</field></row></rows></csv>"
        );
    roundtrip(func, "1,2,3,4|11,12,13,14,15", "'select-columns': (1, 4, 5), 'row-delimiter': '|'",
        "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row><field>1</field><field>4<"
        + "/field><field/></row><row><field>11</field><field>14</field><field>15</field></row></row"
        + "s></csv>");
    roundtrip(func, "first,second,third,fourth|1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 3),"
        + " 'header': true(), 'row-delimiter': '|'", "<csv xmlns=\"http://www.w3.org/2005/xpath-fu"
        + "nctions\"><columns><column>first</column><column>fourth</column><column>third</column><"
        + "/columns><rows><row><field column=\"first\">1</field><field column=\"fourth\">4</field>"
        + "<field column=\"third\">3</field></row><row><field column=\"first\">11</field><field co"
        + "lumn=\"fourth\">14</field><field column=\"third\">13</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four", "'row-delimiter': '|'", "<csv xmlns=\"http://www.w3.org/"
        + "2005/xpath-functions\"><rows><row><field>one</field><field>two</field></row><row><field"
        + ">three</field><field>four</field></row></rows></csv>");
  }

  /**
   * Parses CSV with the given function and verifies that the result is as expected. Then
   * serializes the result, parses the serialization, and verifies that this also returns
   * the expected result.
   * @param function function
   * @param input CSV input
   * @param options options
   * @param expected expected result
   */
  private void roundtrip(final Function function, final String input, final String options,
      final String expected) {

    // parsing
    final String parseQuery = function.args(input, " { " + options + " }");
    final String result = query(parseQuery);
    compare(parseQuery, result, expected, null);

    // serialization: add target format to options
    final StringBuilder format = new StringBuilder();
    if(!options.contains("'format'")) {
      if(!options.isEmpty()) format.append(", ");
      format.append("'format': '" + (function == _CSV_PARSE ? CsvFormat.DIRECT :
        function == CSV_TO_ARRAYS ? CsvFormat.W3_ARRAYS :
        function == CSV_TO_XML ? CsvFormat.W3_XML : CsvFormat.W3_MAP)).append("'");
    }
    final String serializeQuery = _CSV_SERIALIZE.args(
      result.startsWith("<") ? ' ' + result :
      result.startsWith("[") ? " (" + result.replace('\n', ',') + ")" :
      result.startsWith("{") ? " " + result.replaceAll(",\"get\":\\(anonymous-function\\)#2", "") :
      result.isEmpty() ? " ()" : " " + result, " { " + options + format + " }");
    final String serialization = query(serializeQuery);
    final String roundtripQuery = function.args(" \"" + serialization.replace("\"", "\"\"") + "\"",
        " { " + options + " }");
    compare(roundtripQuery, query(roundtripQuery), expected, null);
  }
}
