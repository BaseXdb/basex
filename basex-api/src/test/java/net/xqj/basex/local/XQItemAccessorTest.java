package net.xqj.basex.local;

import static javax.xml.xquery.XQItemType.*;
import static org.junit.Assert.*;
import org.junit.Test;
import java.net.URI;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import org.w3c.dom.Attr;

/**
 * Testing Basic XQItemAccessor functionality.
 *
 * @author Charles Foster
 */
public final class XQItemAccessorTest extends XQJBaseTest {
  // --------------------------------------------------------------------------
  // Atomic Tests
  // --------------------------------------------------------------------------

  /**
   * Tests xs:anyURI.
   * @throws XQException XQuery exception
   */
  @Test
  public void testAnyURI() throws XQException {
    atomicTest("xs:anyURI('http://a/b/c')", "http://a/b/c", XQBASETYPE_ANYURI);
  }

  /**
   * Tests xs:base64Binary.
   * @throws XQException XQuery exception
   */
  @Test
  public void testBase64Binary() throws XQException {
    atomicTest("xs:base64Binary('AAAA')", "AAAA", XQBASETYPE_BASE64BINARY);
  }

  /**
   * Tests xs:boolean.
   * @throws XQException XQuery exception
   */
  @Test
  public void testBoolean() throws XQException {
    atomicTest("xs:boolean('true')", "true", XQBASETYPE_BOOLEAN);
  }

  /**
   * Tests xs:byte.
   * @throws XQException XQuery exception
   */
  @Test
  public void testByte() throws XQException {
    atomicTest("xs:byte('1')", "1", XQBASETYPE_BYTE);
  }

  /**
   * Tests xs:date.
   * @throws XQException XQuery exception
   */
  @Test
  public void testDate() throws XQException {
    atomicTest("xs:date('2000-12-31')", "2000-12-31", XQBASETYPE_DATE);
  }

  /**
   * Tests xs:dateTime.
   * @throws XQException XQuery exception
   */
  @Test
  public void testDateTime() throws XQException {
    atomicTest(
      "xs:dateTime('2000-12-31T12:00:00')", "2000-12-31T12:00:00",
      XQBASETYPE_DATETIME
    );
  }

  /**
   * Tests xs:decimal.
   * @throws XQException XQuery exception
   */
  @Test
  public void testDecimal() throws XQException {
    atomicTest("xs:decimal('1')", "1", XQBASETYPE_DECIMAL);
  }

  /**
   * Tests xs:double.
   * @throws XQException XQuery exception
   */
  @Test
  public void testDouble() throws XQException {
    atomicTest("xs:double('1')", "1", XQBASETYPE_DOUBLE);
  }

  /**
   * Tests xs:ENTITY.
   * @throws XQException XQuery exception
   */
  @Test
  public void testENTITY() throws XQException {
    atomicTest("xs:ENTITY('AAA')", "AAA", XQBASETYPE_ENTITY);
  }

  /**
   * Tests xs:float.
   * @throws XQException XQuery exception
   */
  @Test
  public void testFloat() throws XQException {
    atomicTest("xs:float('1')", "1", XQBASETYPE_FLOAT);
  }

  /**
   * Tests xs:gDay.
   * @throws XQException XQuery exception
   */
  @Test
  public void testGDay() throws XQException {
    atomicTest("xs:gDay('---11')", "---11", XQBASETYPE_GDAY);
  }

  /**
   * Tests xs:gMonth.
   * @throws XQException XQuery exception
   */
  @Test
  public void testGMonth() throws XQException {
    atomicTest("xs:gMonth('--11')", "--11", XQBASETYPE_GMONTH);
  }

  /**
   * Tests xs:gMonthDay.
   * @throws XQException XQuery exception
   */
  @Test
  public void testGMonthDay() throws XQException {
    atomicTest("xs:gMonthDay('--01-01')", "--01-01", XQBASETYPE_GMONTHDAY);
  }

  /**
   * Tests xs:gYear.
   * @throws XQException XQuery exception
   */
  @Test
  public void testGYear() throws XQException {
    atomicTest("xs:gYear('2000')", "2000", XQBASETYPE_GYEAR);
  }

  /**
   * Tests xs:gYearMonth.
   * @throws XQException XQuery exception
   */
  @Test
  public void testGYearMonth() throws XQException {
    atomicTest("xs:gYearMonth('2000-01')", "2000-01", XQBASETYPE_GYEARMONTH);
  }

  /**
   * Tests xs:hexBinary.
   * @throws XQException XQuery exception
   */
  @Test
  public void testHexBinary() throws XQException {
    atomicTest("xs:hexBinary('AA')", "AA", XQBASETYPE_HEXBINARY);
  }

  /**
   * Tests xs:ID.
   * @throws XQException XQuery exception
   */
  @Test
  public void testID() throws XQException {
    atomicTest("xs:ID('AA')", "AA", XQBASETYPE_ID);
  }

  /**
   * Tests xs:IDREF.
   * @throws XQException XQuery exception
   */
  @Test
  public void testIDREF() throws XQException {
    atomicTest("xs:IDREF('AA')", "AA", XQBASETYPE_IDREF);
  }

  /**
   * Tests xs:int.
   * @throws XQException XQuery exception
   */
  @Test
  public void testInt() throws XQException {
    atomicTest("xs:int('1')", "1", XQBASETYPE_INT);
  }

  /**
   * Tests xs:integer.
   * @throws XQException XQuery exception
   */
  @Test
  public void testInteger() throws XQException {
    atomicTest("xs:integer('1')", "1", XQBASETYPE_INTEGER);
  }

  /**
   * Tests xs:language.
   * @throws XQException XQuery exception
   */
  @Test
  public void testLanguage() throws XQException {
    atomicTest("xs:language('en-GB')", "en-GB", XQBASETYPE_LANGUAGE);
  }

  /**
   * Tests xs:long.
   * @throws XQException XQuery exception
   */
  @Test
  public void testLong() throws XQException {
    atomicTest("xs:long('1')", "1", XQBASETYPE_LONG);
  }

  /**
   * Tests xs:Name.
   * @throws XQException XQuery exception
   */
  @Test
  public void testName() throws XQException {
    atomicTest("xs:Name('AAA')", "AAA", XQBASETYPE_NAME);
  }

  /**
   * Tests xs:NCName.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNCName() throws XQException {
    atomicTest("xs:NCName('AAA')", "AAA", XQBASETYPE_NCNAME);
  }

  /**
   * Tests xs:negativeInteger.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNegativeInteger() throws XQException {
    atomicTest("xs:negativeInteger('-1')", "-1", XQBASETYPE_NEGATIVE_INTEGER);
  }

  /**
   * Tests xs:NMTOKEN.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNMTOKEN() throws XQException {
    atomicTest("xs:NMTOKEN('AAA')", "AAA", XQBASETYPE_NMTOKEN);
  }

  /**
   * Tests xs:nonNegativeInteger.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNonNegativeInteger() throws XQException {
    atomicTest("xs:nonNegativeInteger('1')", "1",
      XQBASETYPE_NONNEGATIVE_INTEGER);
  }

  /**
   * Tests xs:nonPositiveInteger.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNonPositiveInteger() throws XQException {
    atomicTest("xs:nonPositiveInteger('-1')", "-1",
      XQBASETYPE_NONPOSITIVE_INTEGER);
  }

  /**
   * Tests xs:normalizedString.
   * @throws XQException XQuery exception
   */
  @Test
  public void testNormalizedString() throws XQException {
    atomicTest("xs:normalizedString('AAA')", "AAA", XQBASETYPE_NORMALIZED_STRING);
  }

  /**
   * Tests xs:positiveInteger.
   * @throws XQException XQuery exception
   */
  @Test
  public void testPositiveInteger() throws XQException {
    atomicTest("xs:positiveInteger('1')", "1", XQBASETYPE_POSITIVE_INTEGER);
  }

  /**
   * Tests xs:QName.
   * @throws XQException XQuery exception
   */
  @Test
  public void testQName() throws XQException {
    atomicTest("xs:QName('AAA')", "AAA", XQBASETYPE_QNAME);
  }

  /**
   * Tests xs:short.
   * @throws XQException XQuery exception
   */
  @Test
  public void testShort() throws XQException {
    atomicTest("xs:short('1')", "1", XQBASETYPE_SHORT);
  }

  /**
   * Tests xs:string.
   * @throws XQException XQuery exception
   */
  @Test
  public void testString() throws XQException {
    atomicTest("'Hello World'", "Hello World", XQBASETYPE_STRING);
  }

  /**
   * Tests xs:time.
   * @throws XQException XQuery exception
   */
  @Test
  public void testTime() throws XQException {
    atomicTest("xs:time('12:12:12')", "12:12:12", XQBASETYPE_TIME);
  }

  /**
   * Tests xs:token.
   * @throws XQException XQuery exception
   */
  @Test
  public void testToken() throws XQException {
    atomicTest("xs:token('AAA')", "AAA", XQBASETYPE_TOKEN);
  }

  /**
   * Tests xs:unsignedByte.
   * @throws XQException XQuery exception
   */
  @Test
  public void testUnsignedByte() throws XQException {
    atomicTest("xs:unsignedByte('1')", "1", XQBASETYPE_UNSIGNED_BYTE);
  }

  /**
   * Tests xs:unsignedInt.
   * @throws XQException XQuery exception
   */
  @Test
  public void testUnsignedInt() throws XQException {
    atomicTest("xs:unsignedInt('1')", "1", XQBASETYPE_UNSIGNED_INT);
  }

  /**
   * Tests xs:unsignedLong.
   * @throws XQException XQuery exception
   */
  @Test
  public void testUnsignedLong() throws XQException {
    atomicTest("xs:unsignedLong('1')", "1", XQBASETYPE_UNSIGNED_LONG);
  }

  /**
   * Tests xs:unsignedShort.
   * @throws XQException XQuery exception
   */
  @Test
  public void testUnsignedShort() throws XQException {
    atomicTest("xs:unsignedShort('1')", "1", XQBASETYPE_UNSIGNED_SHORT);
  }

  /**
   * Tests xs:dayTimeDuration.
   * @throws XQException XQuery exception
   */
  @Test
  public void testDayTimeDuration() throws XQException {
    atomicTest("xs:dayTimeDuration('PT5H')", "PT5H", XQBASETYPE_DAYTIMEDURATION);
  }

  /**
   * Tests xs:untypedAtomic.
   * @throws XQException XQuery exception
   */
  @Test
  public void testUntypedAtomic() throws XQException {
    atomicTest("xs:untypedAtomic('AAA')", "AAA", XQBASETYPE_UNTYPEDATOMIC);
  }

  /**
   * Tests xs:yearMonthDuration.
   * @throws XQException XQuery exception
   */
  @Test
  public void testYearMonthDuration() throws XQException {
    atomicTest("xs:yearMonthDuration('P1M')", "P1M", XQBASETYPE_YEARMONTHDURATION);
  }

  // --------------------------------------------------------------------------
  // Node tests
  // --------------------------------------------------------------------------

  /**
   * Tests comment().
   * @throws XQException XQuery exception
   */
  @Test
  public void testComment() throws XQException {
    nodeTest("<!-- comment -->", "<!-- comment -->", XQITEMKIND_COMMENT);
  }

  /**
   * Tests element().
   * @throws XQException XQuery exception
   */
  @Test
  public void testElement() throws XQException {
    nodeTest("<e>AAA</e>", "<e>AAA</e>", XQITEMKIND_ELEMENT);
  }

  /**
   * Tests document(element()).
   * @throws XQException XQuery exception
   */
  @Test
  public void testDocumentElement() throws XQException {
    nodeTest("document{<e>AAA</e>}", "<e>AAA</e>", XQITEMKIND_DOCUMENT_ELEMENT);
  }

  /**
   * Tests processing-instruction().
   * @throws XQException XQuery exception
   */
  @Test
  public void testProcessingInstruction() throws XQException {
    nodeTest("processing-instruction {'a'} {'b'}", "<?a b?>",
      XQITEMKIND_PI);
  }

  /**
   * Tests text().
   * @throws XQException XQuery exception
   */
  @Test
  public void testText() throws XQException {
    nodeTest("text { 'AAA' }", "AAA", XQITEMKIND_TEXT);
  }

  /**
   * Tests document().
   * @throws XQException XQuery exception
   */
  @Test
  public void testDocumentNode() throws XQException {
    nodeTest("document { text { 'AAA' } }", "AAA", XQITEMKIND_DOCUMENT);
  }

  /**
   * Tests attribute().
   * @throws XQException XQuery exception
   */
  @Test
  public void testAttribute() throws XQException {
    final XQResultSequence rs = xqc.createExpression().executeQuery(
      "attribute a { 'AAA' }"
    );
    assertTrue(rs.next());
    final XQItemType type = rs.getItemType();
    assertEquals(XQITEMKIND_ATTRIBUTE, type.getItemKind());
    assertEquals("a", type.getNodeName().getLocalPart());
    assertEquals("AAA", ((Attr) rs.getItem().getNode()).getValue());
  }

  // --------------------------------------------------------------------------
  // Node URI tests
  // --------------------------------------------------------------------------

  /**
   * Tests the URI of a QName.
   * @throws XQException XQuery exception
   */
  @Test
  public void testQNameURI() throws XQException {
    uriTest("fn:QName('http://a/b/c','a:b')", "http://a/b/c");
  }

  /**
   * Tests the URI of an attribute.
   * @throws XQException XQuery exception
   */
  @Test
  public void testAttributeURI() throws XQException {
    final XQResultSequence rs = xqc.createExpression().executeQuery(
      "attribute { fn:QName('http://a/b/c', 'a:b') } { 'AAA' }"
    );

    rs.next();
    final XQItemType type = rs.getItemType();
    assertEquals("http://a/b/c", rs.getNodeUri().toString());
    assertEquals("a", type.getNodeName().getPrefix());
    assertEquals("b", type.getNodeName().getLocalPart());
  }

  // --------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------

  /**
   * Runs an atomic test.
   * @param query query string
   * @param expectedValue expected value
   * @param expectedBaseType expected base type
   * @throws XQException XQuery exception
   */
  private void atomicTest(final String query, final String expectedValue,
      final int expectedBaseType) throws XQException {

    final XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());
    final XQItemType type = rs.getItemType();
    assertEquals(XQITEMKIND_ATOMIC, type.getItemKind());
    assertEquals(expectedBaseType, type.getBaseType());
    assertEquals(expectedValue, rs.getItemAsString(null));
  }

  /**
   * Runs a node test.
   * @param query query string
   * @param expectedValue expected value
   * @param expectedItemKind expected item kind
   * @throws XQException XQuery exception
   */
  private void nodeTest(final String query, final String expectedValue,
    final int expectedItemKind) throws XQException {

    final XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());
    final XQItemType type = rs.getItemType();
    assertEquals(expectedItemKind, type.getItemKind());
    assertEquals(expectedValue, rs.getItemAsString(null));
  }

  /**
   * Runs a URI test.
   * @param query query string
   * @param expectedURI expected URI
   * @throws XQException XQuery exception
   */
  private void uriTest(final String query, final String expectedURI) throws XQException {
    final XQResultSequence rs = xqc.createExpression().executeQuery(query);
    assertTrue(rs.next());

    final URI uri = rs.getNodeUri();
    assertNotNull(uri);
    assertEquals(expectedURI, uri.toString());
  }

  // --------------------------------------------------------------------------

}
