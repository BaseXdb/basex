package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.basex.BaseXServer;
import org.basex.core.Text;
import org.basex.io.in.DecodingInput;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.basex.util.Util;
import org.basex.util.list.ByteList;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the correctness of the item types of the client API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ItemTypeTest {
  /** Test database name. */
  protected static final String DB = Util.name(SessionTest.class);
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private static TestSession session;

  /**
   * Starts a session.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    server = new BaseXServer("-z", "-p9999", "-e9998");
    session = new TestSession(
        Text.LOCALHOST, 9999, Text.ADMIN, Text.ADMIN);
  }

  /**
   * Stops a session.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    session.close();
    server.stop();
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws IOException I/O exception
   */
  @Test
  public void test() throws IOException {
    for(final String[] t : TYPES) {
      if(t.length < 2) continue;
      final TestQuery cq = session.query(t[1]);
      assertTrue(cq.more());
      assertEquals(t[0], cq.type());
      cq.close();
    }
  }

  /**
   * Extends the client session.
   */
  static class TestSession extends ClientSession {
    /**
     * Constructor.
     * @param host host name
     * @param port port
     * @param user user name
     * @param pass password
     * @throws IOException I/O exception
     */
    public TestSession(final String host, final int port, final String user,
        final String pass) throws IOException {
      super(host, port, user, pass);
    }

    @Override
    public TestQuery query(final String query) throws IOException {
      return new TestQuery(query, this, out);
    }
  }

  /**
   * Extends the client session.
   */
  static class TestQuery extends ClientQuery {
    /** List of data types. */
    IntList types;

    /**
     * Constructor.
     * @param q query string
     * @param s session reference
     * @param os output stream
     * @throws IOException I/O exception
     */
    public TestQuery(final String q, final ClientSession s,
        final OutputStream os) throws IOException {
      super(q, s, os);
    }

    @Override
    protected void cache(final InputStream is) throws IOException {
      cache = new TokenList();
      types = new IntList();
      final ByteList bl = new ByteList();
      while(true) {
        final int t = is.read();
        if(t <= 0) break;
        types.add(t);
        final DecodingInput di = new DecodingInput(is);
        for(int b; (b = di.read()) != -1;) bl.add(b);
        cache.add(bl.toArray());
        bl.reset();
      }
    }

    /**
     * Returns the current data type. Must be called before {@link #next()}.
     * @return data type
     */
    public String type() {
      return TYPES[types.get(pos)][0];
    }
  }

  /** Examples for available data types. */
  static final String[][] TYPES = {
    { }, { }, { }, { }, { }, { }, { },
    // code: 7
    { "function item" },
    // code: 8 and higher
    { "node()" },
    { "text()", "text { 'a' }" },
    { "processing-instruction()", "processing-instruction { 'a' } { 'b' }" },
    { "element()", "<a/>" },
    { "document-node()", "document { 'a' }" },
    { "document-node(element())" },
    { "attribute()", "attribute a { 'b' }" },
    { "comment()", "comment { 'a' } " },
    { }, { }, { }, { }, { }, { }, { }, { },
    { }, { }, { }, { }, { }, { }, { }, { },
    // code: 32 and higher
    { "item()" },
    { "xs:untyped" },
    { "xs:anyType" },
    { "xs:anySimpleType" },
    { "xs:anyAtomicType" },
    { "xs:untypedAtomic", "data(<a>a</a>)" },
    { "xs:string", "'a'" },
    { "xs:normalizedString", "xs:normalizedString('a')" },
    { "xs:token", "xs:token('a')" },
    { "xs:language", "xs:language('a')" },
    { "xs:NMTOKEN", "xs:NMTOKEN('a')" },
    { "xs:Name", "xs:Name('a')" },
    { "xs:NCName", "xs:NCName('a')" },
    { "xs:ID", "xs:ID('a')" },
    { "xs:IDREF", "xs:IDREF('a')" },
    { "xs:ENTITY", "xs:ENTITY('a')" },
    { "xs:float", "xs:float(1)" },
    { "xs:double", "1.1e1" },
    { "xs:decimal", "1.1" },
    { "xs:precisionDecimal" },
    { "xs:integer", "1" },
    { "xs:nonPositiveInteger", "xs:nonPositiveInteger(-1)" },
    { "xs:negativeInteger", "xs:negativeInteger(-1)" },
    { "xs:long", "xs:long(1)" },
    { "xs:int", "xs:int(1)" },
    { "xs:short", "xs:short(1)" },
    { "xs:byte", "xs:byte(1)" },
    { "xs:nonNegativeInteger", "xs:nonNegativeInteger(1)" },
    { "xs:unsignedLong", "xs:unsignedLong(1)" },
    { "xs:unsignedInt", "xs:unsignedInt(1)" },
    { "xs:unsignedShort", "xs:unsignedShort(1)" },
    { "xs:unsignedByte", "xs:unsignedByte(1)" },
    { "xs:positiveInteger", "xs:positiveInteger(1)" },
    { "xs:duration", "xs:duration('P1Y')" },
    { "xs:yearMonthDuration", "xs:yearMonthDuration('P1Y')" },
    { "xs:dayTimeDuration", "xs:dayTimeDuration('PT1M')" },
    { "xs:dateTime", "xs:dateTime('2001-01-01T23:59:59')" },
    { "xs:dateTimeStamp" },
    { "xs:date", "xs:date('2001-01-01')" },
    { "xs:time", "xs:time('01:01:01')" },
    { "xs:gYearMonth", "xs:gYearMonth('2001-01')" },
    { "xs:gYear", "xs:gYear('2001')" },
    { "xs:gMonthDay", "xs:gMonthDay('--01-01')" },
    { "xs:gDay", "xs:gDay('---01')" },
    { "xs:gMonth", "xs:gMonth('--01')" },
    { "xs:boolean", "true()" },
    { "basex:binary" },
    { "xs:base64Binary", "xs:base64Binary('aaaa')" },
    { "xs:hexBinary", "xs:hexBinary('aa')" },
    { "basex:raw" },
    { "xs:anyURI", "xs:anyURI('a')" },
    { "xs:QName", "xs:QName('a')" },
    { "xs:NOTATION" }
  };
}
