package net.xqj.basex.local;

import org.junit.Test;
import org.w3c.dom.Attr;

import static org.junit.Assert.*;
import static javax.xml.xquery.XQItemType.*;
import static org.junit.Assert.assertEquals;

import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import java.net.URI;

/**
 * Basic XQItemAccessor test.
 * @author cfoster
 */

public class XQItemAccessorTest extends XQJBaseTest
{
  // --------------------------------------------------------------------------
  // Atomic Tests
  // --------------------------------------------------------------------------

  @Test
  public void testAnyURI() throws Exception {
    atomicTest("xs:anyURI('http://a/b/c')", "http://a/b/c", XQBASETYPE_ANYURI);
  }

  @Test
  public void testBase64Binary() throws Exception {
    atomicTest("xs:base64Binary('AAAA')", "AAAA", XQBASETYPE_BASE64BINARY);
  }

  @Test
  public void testBoolean() throws Exception {
    atomicTest("xs:boolean('true')", "true", XQBASETYPE_BOOLEAN);
  }

  @Test
  public void testByte() throws Exception {
    atomicTest("xs:byte('1')", "1", XQBASETYPE_BYTE);
  }

  @Test
  public void testDate() throws Exception {
    atomicTest("xs:date('2000-12-31')", "2000-12-31", XQBASETYPE_DATE);
  }

  @Test
  public void testDateTime() throws Exception {
    atomicTest(
      "xs:dateTime('2000-12-31T12:00:00')", "2000-12-31T12:00:00",
      XQBASETYPE_DATETIME
    );
  }

  @Test
  public void testDecimal() throws Exception {
    atomicTest("xs:decimal('1')", "1", XQBASETYPE_DECIMAL);
  }

  @Test
  public void testDouble() throws Exception {
    atomicTest("xs:double('1')", "1", XQBASETYPE_DOUBLE);
  }

  @Test
  public void testEntity() throws Exception {
    atomicTest("xs:ENTITY('AAA')", "AAA", XQBASETYPE_ENTITY);
  }

  @Test
  public void testFloat() throws Exception {
    atomicTest("xs:float('1')", "1", XQBASETYPE_FLOAT);
  }

  @Test
  public void testGDay() throws Exception {
    atomicTest("xs:gDay('---11')", "---11", XQBASETYPE_GDAY);
  }

  @Test
  public void testGMonth() throws Exception {
    atomicTest("xs:gMonth('--11')", "--11", XQBASETYPE_GMONTH);
  }

  @Test
  public void testGMonthDay() throws Exception {
    atomicTest("xs:gMonthDay('--01-01')", "--01-01", XQBASETYPE_GMONTHDAY);
  }

  @Test
  public void testGYear() throws Exception {
    atomicTest("xs:gYear('2000')", "2000", XQBASETYPE_GYEAR);
  }

  @Test
  public void testGYearMonth() throws Exception {
    atomicTest("xs:gYearMonth('2000-01')", "2000-01", XQBASETYPE_GYEARMONTH);
  }

  @Test
  public void testHexBinary() throws Exception {
    atomicTest("xs:hexBinary('AA')", "AA", XQBASETYPE_HEXBINARY);
  }

  @Test
  public void testID() throws Exception {
    atomicTest("xs:ID('AA')", "AA", XQBASETYPE_ID);
  }

  @Test
  public void testIDREF() throws Exception {
    atomicTest("xs:IDREF('AA')", "AA", XQBASETYPE_IDREF);
  }

  @Test
  public void testInt() throws Exception {
    atomicTest("xs:int('1')", "1", XQBASETYPE_INT);
  }

  @Test
  public void testInteger() throws Exception {
    atomicTest("xs:integer('1')", "1", XQBASETYPE_INTEGER);
  }

  @Test
  public void testLanguage() throws Exception {
    atomicTest("xs:language('en-GB')", "en-GB", XQBASETYPE_LANGUAGE);
  }

  @Test
  public void testLong() throws Exception {
    atomicTest("xs:long('1')", "1", XQBASETYPE_LONG);
  }

  @Test
  public void testName() throws Exception {
    atomicTest("xs:Name('AAA')", "AAA", XQBASETYPE_NAME);
  }

  @Test
  public void testNCName() throws Exception {
    atomicTest("xs:NCName('AAA')", "AAA", XQBASETYPE_NCNAME);
  }

  @Test
  public void testNegativeInteger() throws Exception {
    atomicTest("xs:negativeInteger('-1')", "-1", XQBASETYPE_NEGATIVE_INTEGER);
  }

  @Test
  public void testNMTOKEN() throws Exception {
    atomicTest("xs:NMTOKEN('AAA')", "AAA", XQBASETYPE_NMTOKEN);
  }

  @Test
  public void testNonNegativeInteger() throws Exception {
    atomicTest("xs:nonNegativeInteger('1')", "1",
      XQBASETYPE_NONNEGATIVE_INTEGER);
  }

  @Test
  public void testNonPositiveInteger() throws Exception {
    atomicTest("xs:nonPositiveInteger('-1')", "-1",
      XQBASETYPE_NONPOSITIVE_INTEGER);
  }

  @Test
  public void testNormalizedString() throws Exception {
    atomicTest("xs:normalizedString('AAA')","AAA",XQBASETYPE_NORMALIZED_STRING);
  }

  @Test
  public void testPositiveInteger() throws Exception {
    atomicTest("xs:positiveInteger('1')", "1", XQBASETYPE_POSITIVE_INTEGER);
  }

  @Test
  public void testQName() throws Exception {
    atomicTest("xs:QName('AAA')", "AAA", XQBASETYPE_QNAME);
  }

  @Test
  public void testShort() throws Exception {
    atomicTest("xs:short('1')", "1", XQBASETYPE_SHORT);
  }

  @Test
  public void testString() throws Exception {
    atomicTest("'Hello World'", "Hello World", XQBASETYPE_STRING);
  }

  @Test
  public void testTime() throws Exception {
    atomicTest("xs:time('12:12:12')", "12:12:12", XQBASETYPE_TIME);
  }

  @Test
  public void testToken() throws Exception {
    atomicTest("xs:token('AAA')", "AAA", XQBASETYPE_TOKEN);
  }

  @Test
  public void testUnsignedByte() throws Exception {
    atomicTest("xs:unsignedByte('1')", "1", XQBASETYPE_UNSIGNED_BYTE);
  }

  @Test
  public void testUnsignedInt() throws Exception {
    atomicTest("xs:unsignedInt('1')", "1", XQBASETYPE_UNSIGNED_INT);
  }

  @Test
  public void testUnsignedLong() throws Exception {
    atomicTest("xs:unsignedLong('1')", "1", XQBASETYPE_UNSIGNED_LONG);
  }

  @Test
  public void testUnsignedShort() throws Exception {
    atomicTest("xs:unsignedShort('1')", "1", XQBASETYPE_UNSIGNED_SHORT);
  }

  @Test
  public void testDayTimeDuration() throws Exception {
    atomicTest("xs:dayTimeDuration('PT5H')","PT5H", XQBASETYPE_DAYTIMEDURATION);
  }

  @Test
  public void testUntypedAtomic() throws Exception {
    atomicTest("xs:untypedAtomic('AAA')", "AAA", XQBASETYPE_UNTYPEDATOMIC);
  }

  @Test
  public void testYearMonthDuration() throws Exception {
    atomicTest("xs:yearMonthDuration('P1M')", "P1M",
      XQBASETYPE_YEARMONTHDURATION);
  }

  // --------------------------------------------------------------------------
  // Node tests
  // --------------------------------------------------------------------------

  @Test
  public void testComment() throws Exception {
    nodeTest("<!-- comment -->", "<!-- comment -->", XQITEMKIND_COMMENT);
  }

  @Test
  public void testElement() throws Exception {
    nodeTest("<e>AAA</e>", "<e>AAA</e>", XQITEMKIND_ELEMENT);
  }

  @Test
  public void testDocumentElement() throws Exception {
    nodeTest("document{<e>AAA</e>}", "<e>AAA</e>", XQITEMKIND_DOCUMENT_ELEMENT);
  }

  @Test
  public void testProcessingInstruction() throws Exception {
    nodeTest("processing-instruction {'a'} {'b'}", "<?a b?>",
      XQITEMKIND_PI);
  }

  @Test
  public void testText() throws Exception {
    nodeTest("text { 'AAA' }", "AAA", XQITEMKIND_TEXT);
  }

  @Test
  public void testDocumentNode() throws Exception {
    nodeTest("document { text { 'AAA' } }", "AAA", XQITEMKIND_DOCUMENT);
  }

  @Test
  public void testAttribute() throws Exception {
    XQResultSequence rs = xqc.createExpression().executeQuery(
      "attribute a { 'AAA' }"
    );
    assertTrue(rs.next());
    XQItemType type = rs.getItemType();
    assertEquals(XQITEMKIND_ATTRIBUTE, type.getItemKind());
    assertEquals("a", type.getNodeName().getLocalPart());
    assertEquals("AAA", ((Attr)rs.getItem().getNode()).getValue());
  }

  // --------------------------------------------------------------------------
  // Node URI tests
  // --------------------------------------------------------------------------

  @Test
  public void testQNameURI() throws Exception {
    uriTest("fn:QName('http://a/b/c','a:b')", "http://a/b/c");
  }

  @Test
  public void testAttributeURI() throws Exception {
    XQResultSequence rs = xqc.createExpression().executeQuery(
      "attribute { fn:QName('http://a/b/c', 'a:b') } { 'AAA' }"
    );

    rs.next();
    XQItemType type = rs.getItemType();
    assertEquals("http://a/b/c", rs.getNodeUri().toString());
    assertEquals("a", type.getNodeName().getPrefix());
    assertEquals("b", type.getNodeName().getLocalPart());
  }

  // --------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------

  public void atomicTest(
    String query,
    String expectedValue,
    int expectedBaseType) throws Exception
  {
    XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());
    XQItemType type = rs.getItemType();
    assertEquals(XQITEMKIND_ATOMIC, type.getItemKind());
    assertEquals(expectedBaseType, type.getBaseType());
    assertEquals(expectedValue, rs.getItemAsString(null));
  }

  public void nodeTest(
    String query,
    String expectedValue,
    int expectedItemKind) throws Exception
  {
    XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());
    XQItemType type = rs.getItemType();
    assertEquals(expectedItemKind, type.getItemKind());
    assertEquals(expectedValue, rs.getItemAsString(null));
  }

  public void uriTest(String query, String expectedURI) throws Exception
  {
    XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());

    URI uri = rs.getNodeUri();
    assertNotNull(uri);
    assertEquals(expectedURI, uri.toString());
  }

  // --------------------------------------------------------------------------

}
