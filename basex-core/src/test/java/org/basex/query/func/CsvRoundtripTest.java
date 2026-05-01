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
    roundtrip(func, "1,2,3,4|11,12,13,14",
      "'select-columns': (1, 4, 17), 'row-delimiter': '|'",
      "<csv><record><entry>1</entry><entry>4</entry><entry/></record>" +
      "<record><entry>11</entry><entry>14</entry><entry/></record></csv>");
    roundtrip(func, "a,b,c,d|1,2,3,4|11,12,13,14",
      "'format': 'attributes', 'header': true(), 'select-columns': (1, 4, 2), 'row-delimiter': '|'",
      "<csv><record><entry name=\"a\">1</entry><entry name=\"d\">4</entry>" +
      "<entry name=\"b\">2</entry></record><record><entry name=\"a\">11</entry>" +
      "<entry name=\"d\">14</entry><entry name=\"b\">12</entry></record></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14",
      "'format': 'xquery', 'select-columns': (1, 4, 17), 'row-delimiter': '|'",
      "{\"records\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"\"])}");
  }

  /** Test method. */
  @Test public void csvToArrays() {
    final Function func = CSV_TO_ARRAYS;
    roundtrip(func, "", "",
      "");
    roundtrip(func, "one", "",
      "[\"one\"]");
    roundtrip(func, "one,two", "",
      "[\"one\",\"two\"]");
    roundtrip(func, "one,two&#xA;three,four", "",
      "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "",
      "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two&#xA;three,four,five", "",
      "[\"one\",\"two\"]\n[\"three\",\"four\",\"five\"]");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "",
      "[\"one\",\"two\"]\n[]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two&#xA;\"three,four\",five", "",
      "[\"one\",\"two\"]\n[\"three,four\",\"five\"]");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "",
      "[\"one\",\"two\"]\n[\"three,\"\"four\"\"\",\"five\"]");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "",
      "[\"one\",\"\"]\n[\"\",\"four\"]\n[\"\",\"\"]\n[\"\",\"\",\"\",\"\"]");
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "",
      "[\"one\",\"\"]\n[\"\",\"four\"]");
    roundtrip(func, "one,\"[&#xA;]\"&#xA;\"\",\"four\"", "",
      "[\"one\",\"[&#xA;]\"]\n[\"\",\"four\"]");
    roundtrip(func, "one;two&#xA;three;four",
      "'field-delimiter': ';'",
      "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|'",
      "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one.two|three.four",
      "'row-delimiter': '|', 'field-delimiter': '.'",
      "[\"one\",\"two\"]\n[\"three\",\"four\"]");
    roundtrip(func, "one,'two,2'|three,'four,4'",
      "'row-delimiter': '|', 'quote-character': ''''",
      "[\"one\",\"two,2\"]\n[\"three\",\"four,4\"]");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''",
      "'row-delimiter': '|', 'quote-character': ''''",
      "[\"one\",\"two,'2'\"]\n[\"three\",\"four,'4'\"]");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|'",
      "[\"one \",\"two \"]\n[\" three\",\" four\"]");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|', 'trim-whitespace': false()",
      "[\"one \",\"two \"]\n[\" three\",\" four\"]");
    roundtrip(func, "one ,two | three, twenty  four ",
      "'row-delimiter': '|', 'trim-whitespace': true()",
      "[\"one\",\"two\"]\n[\"three\",\"twenty  four\"]");
    roundtrip(func, "&#xA;", "",
      "[]");
    roundtrip(func, "&#xA; ", "",
      "[]\n[\" \"]");
    roundtrip(func, "&#xA; ",
      "'trim-whitespace': true()",
      "[]");
    roundtrip(func, "&#xA;&#xA;",
      "'trim-whitespace': true()",
      "[]\n[]");
    roundtrip(func, "&#xA;&#xA;&#xA;",
      "'trim-whitespace': true()",
      "[]\n[]\n[]");
    roundtrip(func, "one,two,\"z\"", "",
      "[\"one\",\"two\",\"z\"]");
    roundtrip(func, "one,two,\"z\"&#xA;", "",
      "[\"one\",\"two\",\"z\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c|p,q,r",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\"]\n[\"p\",\"q\",\"r\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|'",
      "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]\n[\"p\",\"q\",\"r\",\"s\",\"t\",\"u\"]");
  }

  /** Test method. */
  @Test public void parseCsv() {
    final Function func = PARSE_CSV;
    roundtrip(func, "", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":()}");
    roundtrip(func, "one", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":[\"one\"]}");
    roundtrip(func, "one,two", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":[\"one\",\"two\"]}");
    roundtrip(func, "one,two&#xA;three,four", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two&#xA;three,four,five", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\",\"five\"])}");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[],[\"three\",\"four\"])}");
    roundtrip(func, "one,two&#xA;\"three,four\",five", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three,four\",\"five\"])}");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three,\"\"four\"\"\",\"five\"])}");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"\"],[\"\",\"four\"],[\"\",\"\"],[\"\",\"\",\"\",\"\"])}");
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"one\",\"\"],[\"\",\"four\"])}");
    roundtrip(func, "one,\"[&#xA;]\"&#xA;\"\",\"four\"", "",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"[&#xA;]\"],[\"\",\"four\"])}");
    roundtrip(func, "one;two&#xA;three;four", "'field-delimiter': ';'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one.two|three.four",
      "'row-delimiter': '|', 'field-delimiter': '.'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,'two,2'|three,'four,4'",
      "'row-delimiter': '|', 'quote-character': ''''",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two,2\"],[\"three\",\"four,4\"])}");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''",
      "'row-delimiter': '|', 'quote-character': ''''",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two,'2'\"],[\"three\",\"four,'4'\"])}");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one \",\"two \"],[\" three\",\" four\"])}");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|', 'trim-whitespace': false()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one \",\"two \"],[\" three\",\" four\"])}");
    roundtrip(func, "one ,two | three, twenty  four ",
      "'row-delimiter': '|', 'trim-whitespace': true()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"twenty  four\"])}");
    roundtrip(func, "&#xA;", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":[]}");
    roundtrip(func, "&#xA; ", "",
      "{\"columns\":(),\"column-index\":{},\"rows\":([],[\" \"])}");
    roundtrip(func, "&#xA; ",
      "'trim-whitespace': true()",
      "{\"columns\":(),\"column-index\":{},\"rows\":[]}");
    roundtrip(func, "&#xA;&#xA;",
      "'trim-whitespace': true()",
      "{\"columns\":(),\"column-index\":{},\"rows\":([],[])}");
    roundtrip(func, "&#xA;&#xA;&#xA;",
      "'trim-whitespace': true()",
      "{\"columns\":(),\"column-index\":{},\"rows\":([],[],[])}");
    roundtrip(func, "left,right|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': ('left', 'right')",
      "{\"columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': false()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': 'left'",
      "{\"columns\":\"left\",\"column-index\":{\"left\":1}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': ('', 'right')",
      "{\"columns\":(\"\",\"right\"),\"column-index\":{\"right\":2}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "left,left|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"left\",\"left\"),\"column-index\":{\"left\":1}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, ",right|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"\",\"right\"),\"column-index\":{\"right\":2}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, ",|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"\",\"\"),\"column-index\":{}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "left,right",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2},\"rows\":()}");
    roundtrip(func, "1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'select-columns': (1 to 4)",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"])}");
    roundtrip(func, "a,b,c,d,e,f,g,h,i|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'select-columns': (1 to 4), 'header': true()",
      "{\"columns\":(\"a\",\"b\",\"c\",\"d\")," +
      "\"column-index\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'trim-rows': true()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"])}");
    roundtrip(func, "a,b,c,d|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'trim-rows': true(), 'header': true()",
      "{\"columns\":(\"a\",\"b\",\"c\",\"d\")," +
      "\"column-index\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16",
      "'row-delimiter': '|', 'trim-rows': false(), 'header': false()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"11\",\"12\",\"13\",\"14\",\"15\",\"16\"])}");
    roundtrip(func, "1,2,3,4,5,6|14,15,16",
      "'row-delimiter': '|', 'select-columns': (1 to 4)",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\"],[\"14\",\"15\",\"16\",\"\"])}");
    roundtrip(func, "1,2,3,4,5,6|14,15,16",
      "'row-delimiter': '|', 'trim-rows': true()",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"4\",\"5\",\"6\"],[\"14\",\"15\",\"16\",\"\",\"\",\"\"])}");
    roundtrip(func, "a,b,c,d,e|1,2,3|14,15,16",
      "'row-delimiter': '|', 'trim-rows': true(), 'header': true()",
      "{\"columns\":(\"a\",\"b\",\"c\",\"d\",\"e\")," +
      "\"column-index\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5}," +
      "\"rows\":([\"1\",\"2\",\"3\",\"\",\"\"],[\"14\",\"15\",\"16\",\"\",\"\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (4, 3, 2, 1), 'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"4\",\"3\",\"2\",\"1\"],[\"14\",\"13\",\"12\",\"11\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4), 'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"1\",\"4\"],[\"11\",\"14\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 17), 'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 17, 4), 'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"\",\"4\"],[\"11\",\"\",\"14\"])}");
    roundtrip(func, "1,2,3,4|11,12,13,14,15", "'select-columns': (1, 4, 5), 'row-delimiter': '|'",
      "{\"columns\":(),\"column-index\":{}," +
      "\"rows\":([\"1\",\"4\",\"\"],[\"11\",\"14\",\"15\"])}");
    roundtrip(func, "first,second,third,fourth|1,2,3,4|11,12,13,14",
      "'select-columns': (1, 4, 3), 'header': true(), 'row-delimiter': '|'",
      "{\"columns\":(\"first\",\"fourth\",\"third\")," +
      "\"column-index\":{\"first\":1,\"fourth\":2,\"third\":3}," +
      "\"rows\":([\"1\",\"4\",\"3\"],[\"11\",\"14\",\"13\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (1 to 3)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"a\",\"b\",\"c\"],[\"p\",\"q\",\"r\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (2 to 4)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"b\",\"c\",\"d\"],[\"q\",\"r\",\"s\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 2)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"b\"],[\"s\",\"r\",\"q\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"])}");
    roundtrip(func, "left,right|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "{\"columns\":(\"left\",\"right\"),\"column-index\":{\"left\":1,\"right\":2}," +
      "\"rows\":([\"one\",\"two\"],[\"three\",\"four\"])}");
    roundtrip(func, "a,b,c,d,e,f|p,q,r,s,t,u",
      "'row-delimiter': '|', 'select-columns': (4, 3, 1)",
      "{\"columns\":(),\"column-index\":{},\"rows\":([\"d\",\"c\",\"a\"],[\"s\",\"r\",\"p\"])}");
  }

  /** Test method. */
  @Test public void csvToXml() {
    final Function func = CSV_TO_XML;
    roundtrip(func, "", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows/></csv>");
    roundtrip(func, "one", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<rows><row><field>one</field></row></rows></csv>");
    roundtrip(func, "one,two", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<rows><row><field>one</field><field>two</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four&#xA;", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;three,four,five", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;&#xA;three,four", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row><row/>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;\"three,four\",five", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three,four</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,two&#xA;\"three,\"\"four\"\"\",five", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three,\"four\"</field><field>five</field></row></rows></csv>");
    roundtrip(func, "one,&#xA;,four&#xA;,&#xA;,,,", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field/></row>" +
      "<row><field/><field>four</field></row>" +
      "<row><field/><field/></row>" +
      "<row><field/><field/><field/><field/></row></rows></csv>");
    roundtrip(func, "one,\"\"&#xA;\"\",\"four\"", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field/></row>" +
      "<row><field/><field>four</field></row></rows></csv>");
    roundtrip(func, "one;two&#xA;three;four", "'field-delimiter': ';'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one.two|three.four",
      "'row-delimiter': '|', 'field-delimiter': '.'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,'two,2'|three,'four,4'",
      "'row-delimiter': '|', 'quote-character': ''''",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two,2</field></row>" +
      "<row><field>three</field><field>four,4</field></row></rows></csv>");
    roundtrip(func, "one,'two,''2'''|three,'four,''4'''",
      "'row-delimiter': '|', 'quote-character': ''''",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two,'2'</field></row>" +
      "<row><field>three</field><field>four,'4'</field></row></rows></csv>");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one </field><field>two </field></row>" +
      "<row><field> three</field><field> four</field></row></rows></csv>");
    roundtrip(func, "one ,two | three, four",
      "'row-delimiter': '|', 'trim-whitespace': false()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one </field><field>two </field></row>" +
      "<row><field> three</field><field> four</field></row></rows></csv>");
    roundtrip(func, "one ,two | three, twenty  four ",
      "'row-delimiter': '|', 'trim-whitespace': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>twenty  four</field></row></rows></csv>");
    roundtrip(func, "&#xA;", "",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows><row/></rows></csv>");
    roundtrip(func, "left,right|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>left</column><column>right</column></columns><rows>" +
      "<row><field column=\"left\">one</field><field column=\"right\">two</field></row>" +
      "<row><field column=\"left\">three</field><field column=\"right\">four</field></row>" +
      "</rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': ('left', 'right')",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>left</column><column>right</column></columns><rows>" +
      "<row><field column=\"left\">one</field><field column=\"right\">two</field></row>" +
      "<row><field column=\"left\">three</field><field column=\"right\">four</field></row>" +
      "</rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': false()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': 'left'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>left</column></columns><rows>" +
      "<row><field column=\"left\">one</field><field>two</field></row>" +
      "<row><field column=\"left\">three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': ('', 'right')",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column/><column>right</column></columns><rows>" +
      "<row><field>one</field><field column=\"right\">two</field></row>" +
      "<row><field>three</field><field column=\"right\">four</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|', 'header': ()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "left,left|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>left</column><column>left</column></columns><rows>" +
      "<row><field column=\"left\">one</field><field column=\"left\">two</field></row>" +
      "<row><field column=\"left\">three</field><field column=\"left\">four</field></row>" +
      "</rows></csv>");
    roundtrip(func, ",right|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column/><column>right</column></columns><rows>" +
      "<row><field>one</field><field column=\"right\">two</field></row>" +
      "<row><field>three</field><field column=\"right\">four</field></row></rows></csv>");
    roundtrip(func, ",|one,two|three,four",
      "'row-delimiter': '|', 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column/><column/></columns><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'select-columns': (1 to 4)",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>2</field><field>3</field><field>4</field></row>" +
      "<row><field>11</field><field>12</field><field>13</field><field>14</field></row>" +
      "</rows></csv>");
    roundtrip(func, "a,b,c,d,e,f,g,h,i|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'select-columns': (1 to 4), 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>a</column><column>b</column><column>c</column><column>d</column>" +
      "</columns><rows>" +
      "<row><field column=\"a\">1</field><field column=\"b\">2</field>" +
      "<field column=\"c\">3</field><field column=\"d\">4</field></row>" +
      "<row><field column=\"a\">11</field><field column=\"b\">12</field>" +
      "<field column=\"c\">13</field><field column=\"d\">14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'trim-rows': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>2</field><field>3</field><field>4</field></row>" +
      "<row><field>11</field><field>12</field><field>13</field><field>14</field></row>" +
      "</rows></csv>");
    roundtrip(func, "a,b,c,d|1,2,3,4,5,6,7,8,9,10|11,12,13,14,15,16,17,18,19,20",
      "'row-delimiter': '|', 'trim-rows': true(), 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>a</column><column>b</column><column>c</column><column>d</column>" +
      "</columns><rows>" +
      "<row><field column=\"a\">1</field><field column=\"b\">2</field>" +
      "<field column=\"c\">3</field><field column=\"d\">4</field></row>" +
      "<row><field column=\"a\">11</field><field column=\"b\">12</field>" +
      "<field column=\"c\">13</field><field column=\"d\">14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14,15,16",
      "'row-delimiter': '|', 'trim-rows': false(), 'header': false()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>2</field><field>3</field><field>4</field></row>" +
      "<row><field>11</field><field>12</field><field>13</field><field>14</field>" +
      "<field>15</field><field>16</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4,5,6|14,15,16",
      "'row-delimiter': '|', 'select-columns': (1 to 4)",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>2</field><field>3</field><field>4</field></row>" +
      "<row><field>14</field><field>15</field><field>16</field><field/></row></rows></csv>");
    roundtrip(func, "1,2,3,4,5,6|14,15,16",
      "'row-delimiter': '|', 'trim-rows': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>2</field><field>3</field>" +
      "<field>4</field><field>5</field><field>6</field></row>" +
      "<row><field>14</field><field>15</field><field>16</field>" +
      "<field/><field/><field/></row></rows></csv>");
    roundtrip(func, "a,b,c,d,e|1,2,3,4,5|14,15,16",
      "'row-delimiter': '|', 'trim-rows': true(), 'header': true()",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>a</column><column>b</column><column>c</column>" +
      "<column>d</column><column>e</column></columns><rows>" +
      "<row><field column=\"a\">1</field><field column=\"b\">2</field>" +
      "<field column=\"c\">3</field><field column=\"d\">4</field>" +
      "<field column=\"e\">5</field></row>" +
      "<row><field column=\"a\">14</field><field column=\"b\">15</field>" +
      "<field column=\"c\">16</field><field column=\"d\"/><field column=\"e\"/></row>" +
      "</rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (4, 3, 2, 1), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>4</field><field>3</field><field>2</field><field>1</field></row>" +
      "<row><field>14</field><field>13</field><field>12</field><field>11</field></row>" +
      "</rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>4</field></row>" +
      "<row><field>11</field><field>14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 4, 17), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>4</field><field/></row>" +
      "<row><field>11</field><field>14</field><field/></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14", "'select-columns': (1, 17, 4), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field/><field>4</field></row>" +
      "<row><field>11</field><field/><field>14</field></row></rows></csv>");
    roundtrip(func, "1,2,3,4|11,12,13,14,15", "'select-columns': (1, 4, 5), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>1</field><field>4</field><field/></row>" +
      "<row><field>11</field><field>14</field><field>15</field></row></rows></csv>");
    roundtrip(func, "first,second,third,fourth|1,2,3,4|11,12,13,14",
      "'select-columns': (1, 4, 3), 'header': true(), 'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
      "<columns><column>first</column><column>fourth</column><column>third</column>" +
      "</columns><rows>" +
      "<row><field column=\"first\">1</field><field column=\"fourth\">4</field>" +
      "<field column=\"third\">3</field></row>" +
      "<row><field column=\"first\">11</field><field column=\"fourth\">14</field>" +
      "<field column=\"third\">13</field></row></rows></csv>");
    roundtrip(func, "one,two|three,four",
      "'row-delimiter': '|'",
      "<csv xmlns=\"http://www.w3.org/2005/xpath-functions\"><rows>" +
      "<row><field>one</field><field>two</field></row>" +
      "<row><field>three</field><field>four</field></row></rows></csv>");
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
  private static void roundtrip(final Function function, final String input, final String options,
      final String expected) {

    // parsing
    final String adaptive = "declare option output:method 'adaptive'; ";
    final String parseQuery = adaptive + function.args(input, " { " + options + " }");
    final String parseResult = query(parseQuery).replaceAll(",\"get\":.*?\\}", "");
    compare(parseQuery, parseResult, expected, null);

    // serialization: add target format to options
    final StringBuilder format = new StringBuilder();
    if(!options.contains("'format'")) {
      if(!options.isEmpty()) format.append(", ");
      format.append("'format': '").append(function == _CSV_PARSE ? CsvFormat.DIRECT :
        function == CSV_TO_ARRAYS ? CsvFormat.W3_ARRAYS :
        function == CSV_TO_XML ? CsvFormat.W3_XML : CsvFormat.W3).append("'");
    }
    final String serializeQuery = _CSV_SERIALIZE.args(
      parseResult.startsWith("<") ? ' ' + parseResult :
      parseResult.startsWith("[") ? " (" + parseResult.replace('\n', ',') + ")" :
      parseResult.isEmpty() ? " ()" : " " + parseResult, " { " + options + format + " }");
    final String serialize = query(serializeQuery);
    final String roundtripQuery = adaptive +
        function.args(" \"" + serialize.replace("\"", "\"\"") + "\"", " { " + options + " }");
    final String roundtripResult = query(roundtripQuery).replaceAll(",\"get\":.*?\\}", "");
    compare(roundtripQuery, roundtripResult, expected, null);
  }
}
