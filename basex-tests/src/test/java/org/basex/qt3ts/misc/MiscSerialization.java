package org.basex.qt3ts.misc;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the Serialization.
 *
 * @author BaseX Team 2005-14, BSD License
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialError("SENR0001")
    );
  }

  /**
   *  Use characters in the range of #x7F through #x9F in a text node and ensure they are escaped properly. #x7F - #x9F .
   */
  @org.junit.Test
  public void k2Serialization10() {
    final XQuery query = new XQuery(
      "<a>{codepoints-to-string(127 to 159)}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        serializationMatches("&#x7f;&#x80;&#x81;&#x82;&#x83;&#x84;&#x85;&#x86;&#x87;&#x88;&#x89;&#x8a;&#x8b;&#x8c;&#x8d;&#x8e;&#x8f;&#x90;&#x91;&#x92;&#x93;&#x94;&#x95;&#x96;&#x97;&#x98;&#x99;&#x9a;&#x9b;&#x9c;&#x9d;&#x9e;&#x9f;", "i")
      ||
        serializationMatches("&#127;&#128;&#129;&#130;&#131;&#132;&#133;&#134;&#135;&#136;&#137;&#138;&#139;&#140;&#141;&#142;&#143;&#144;&#145;&#146;&#147;&#148;&#149;&#150;&#151;&#152;&#153;&#154;&#155;&#156;&#157;&#158;&#159;", "")
      )
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        (
          serializationMatches("a&#xD;aa&#xD;", "i")
        ||
          serializationMatches("a&#13;aa&#13;", "")
        )
      &&
        serializationMatches("\\r?\\n.*\\r?\\n", "")
      )
    );
  }

  /**
   *  Write out elements and attributes that have the same names except for their prefixes.
   *                     (Note by MHK: this is not really a serialization test.)
   *       .
   */
  @org.junit.Test
  public void k2Serialization12() {
    final XQuery query = new XQuery(
      "<e> <a:a xmlns:a=\"http://www.example.com/A\" a:a=\"value\"/> <b:a xmlns:b=\"http://www.example.com/A\" b:a=\"value\"/> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        serializationMatches("&#xD;&#x85;&#x2028;", "i")
      ||
        serializationMatches("&#13;&#133;&#8232;", "")
      )
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        serializationMatches("&#xD;&#xA;&#x9;&#x85;&#x2028;", "i")
      ||
        serializationMatches("&#13;&#10;&#9;&#133;&#8232;", "")
      )
    );
  }

  /**
   *  Use characters in a text node that in XML 1.0 are invalid, and in XML 1.1 must be escaped. #x1 - #x1F .
   */
  @org.junit.Test
  public void k2Serialization7a() {
    final XQuery query = new XQuery(
      "<a>{codepoints-to-string(1 to 31)}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("FOCH0001")
      )
    );
  }

  /**
   *  Use characters in an attribute node that in XML 1.0 are invalid, and in XML 1.1 must be escaped. #x1 - #x1F .
   */
  @org.junit.Test
  public void k2Serialization8a() {
    final XQuery query = new XQuery(
      "<a attr=\"{codepoints-to-string(1 to 31)}\"></a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        serializationMatches("&#x7f;&#x80;&#x81;&#x82;&#x83;&#x84;&#x85;&#x86;&#x87;&#x88;&#x89;&#x8a;&#x8b;&#x8c;&#x8d;&#x8e;&#x8f;&#x90;&#x91;&#x92;&#x93;&#x94;&#x95;&#x96;&#x97;&#x98;&#x99;&#x9a;&#x9b;&#x9c;&#x9d;&#x9e;&#x9f;", "i")
      ||
        serializationMatches("&#127;&#128;&#129;&#130;&#131;&#132;&#133;&#134;&#135;&#136;&#137;&#138;&#139;&#140;&#141;&#142;&#143;&#144;&#145;&#146;&#147;&#148;&#149;&#150;&#151;&#152;&#153;&#154;&#155;&#156;&#157;&#158;&#159;", "")
      )
    );
  }
}
