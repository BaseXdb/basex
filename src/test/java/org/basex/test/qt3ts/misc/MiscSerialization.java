package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the Serialization.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscSerialization extends QT3TestSet {

  /**
   *  A standalone attribute node cannot be serialized. .
   */
  @org.junit.Test
  public void k2Serialization1() {
    final XQuery query = new XQuery(
      "attribute name {<anElement/>}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialError("SENR0001")
    );
  }

  /**
   *  Use characters in the range of #x7F through #x9F in an attribute node and ensure they are escaped properly. #x7F - #x9F .
   */
  @org.junit.Test
  public void k2Serialization10() {
    final XQuery query = new XQuery(
      "<a>{codepoints-to-string(127 to 159)}</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>&#127;&#128;&#129;&#130;&#131;&#132;&#133;&#134;&#135;&#136;&#137;&#138;&#139;&#140;&#141;&#142;&#143;&#144;&#145;&#146;&#147;&#148;&#149;&#150;&#151;&#152;&#153;&#154;&#155;&#156;&#157;&#158;&#159;</a>", false)
    );
  }

  /**
   *  Ensure a new-line character is properly escaped. .
   */
  @org.junit.Test
  public void k2Serialization11() {
    final XQuery query = new XQuery(
      "\"a&#xD;aa&#xD;&#xA;a&#xD;&#xA;\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("a&#xD;aa&#xD;\na&#xD;\n", false)
    );
  }

  /**
   *  Write out elements and attributes that have the same names except for their prefixes. .
   */
  @org.junit.Test
  public void k2Serialization12() {
    final XQuery query = new XQuery(
      "<e> <a:a xmlns:a=\"http://www.example.com/A\" a:a=\"value\"/> <b:a xmlns:b=\"http://www.example.com/A\" b:a=\"value\"/> </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e><a:a xmlns:a=\"http://www.example.com/A\" a:a=\"value\"/><b:a xmlns:b=\"http://www.example.com/A\" b:a=\"value\"/></e>", false)
    );
  }

  /**
   *  An attribute node appearing after an element cannot be serialized. .
   */
  @org.junit.Test
  public void k2Serialization2() {
    final XQuery query = new XQuery(
      "<e/>, attribute name {<anElement/>}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialError("SENR0001")
    );
  }

  /**
   *  An attribute node appearing before an element cannot be serialized. .
   */
  @org.junit.Test
  public void k2Serialization3() {
    final XQuery query = new XQuery(
      "attribute name {<anElement/>}, <e/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialError("SENR0001")
    );
  }

  /**
   *  An attribute node appearing between two element nodes cannot be serialized. .
   */
  @org.junit.Test
  public void k2Serialization4() {
    final XQuery query = new XQuery(
      "<e/>, attribute name {<anElement/>}, <e/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialError("SENR0001")
    );
  }

  /**
   *  Ensure that CR, NEL and LINE SEPARATOR in text nodes are escaped when serialized. .
   */
  @org.junit.Test
  public void k2Serialization5() {
    final XQuery query = new XQuery(
      "<a>&#xD;&#x85;&#x2028;</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>&#xD;&#x85;&#x2028;</a>", false)
    );
  }

  /**
   *  Ensure that CR, NL, TAB, NEL and LINE SEPARATOR in attributes are escaped when serialized. .
   */
  @org.junit.Test
  public void k2Serialization6() {
    final XQuery query = new XQuery(
      "<a attr=\"&#xD;&#xA;&#x9;&#x85;&#x2028;\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a attr=\"&#xD;&#xA;&#x9;&#x85;&#x2028;\"/>", false)
    );
  }

  /**
   *  Use characters in a text node that in XML 1.0 are invalid, and in XML 1.1 must be escaped. #x1 - #x1F .
   */
  @org.junit.Test
  public void k2Serialization7() {
    final XQuery query = new XQuery(
      "<a>{codepoints-to-string(1 to 31)}</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<a>&#x1;&#x2;&#x3;&#x4;&#x5;&#x6;&#x7;&#x8;&#x9;&#xA;&#xB;&#xC;&#xD;&#xE;&#xF;&#x10;&#x11;&#x12;&#x13;&#x14;&#x15;&#x16;&#x17;&#x18;&#x19;&#x1A;&#x1B;&#x1C;&#x1D;&#x1E;&#x1F;</a>", false)
      ||
        error("FOCH0001")
      )
    );
  }

  /**
   *  Use characters in an attribute node that in XML 1.0 are invalid, and in XML 1.1 must be escaped. #x1 - #x1F .
   */
  @org.junit.Test
  public void k2Serialization8() {
    final XQuery query = new XQuery(
      "<a attr=\"{codepoints-to-string(1 to 31)}\"></a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<a attr=\"&#x1;&#x2;&#x3;&#x4;&#x5;&#x6;&#x7;&#x8;&#x9;&#xA;&#xB;&#xC;&#xD;&#xE;&#xF;&#x10;&#x11;&#x12;&#x13;&#x14;&#x15;&#x16;&#x17;&#x18;&#x19;&#x1A;&#x1B;&#x1C;&#x1D;&#x1E;&#x1F;\"/>", false)
      ||
        error("FOCH0001")
      )
    );
  }

  /**
   *  Use characters in the range of #x7F through #x9F in an attribute node and ensure they are escaped properly. #x7F - #x9F .
   */
  @org.junit.Test
  public void k2Serialization9() {
    final XQuery query = new XQuery(
      "<a attr=\"{codepoints-to-string(127 to 159)}\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a attr=\"&#127;&#128;&#129;&#130;&#131;&#132;&#133;&#134;&#135;&#136;&#137;&#138;&#139;&#140;&#141;&#142;&#143;&#144;&#145;&#146;&#147;&#148;&#149;&#150;&#151;&#152;&#153;&#154;&#155;&#156;&#157;&#158;&#159;\"/>", false)
    );
  }
}
