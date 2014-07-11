package org.basex.api.client;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.server.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the correctness of the item types of the client API.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XdmInfoTest extends SandboxTest {
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
    server = createServer();
    session = new TestSession(Text.S_LOCALHOST, 9999, Text.S_ADMIN, Text.S_ADMIN);
  }

  /**
   * Stops a session.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    session.close();
    stopServer(server);
  }

  /**
   * Tests the returned item types.
   * @throws IOException I/O exception
   */
  @Test
  public void testIter() throws IOException {
    for(final Object[] t : TYPES) {
      if(t.length < 2) continue;
      final TestQuery tq = session.query(t[1].toString());
      final TestItem ti = tq.iter();
      assertSame("Types are different.\nExpected: " + t[0] +
          "\nFound: " + TYPES[ti.type][0], t, TYPES[ti.type]);
      assertEquals(Token.string(ti.result), TYPES[ti.type][2]);
      tq.close();
    }
  }

  /**
   * Tests the returned XDM information.
   * @throws IOException I/O exception
   */
  @Test
  public void testFull() throws IOException {
    for(final Object[] t : TYPES) {
      if(t.length < 2) continue;
      final TestQuery tq = session.query(t[1].toString());
      final TestItem ti = tq.full();
      assertSame("Types are different.\nExpected: " + t[0] +
          "\nFound: " + TYPES[ti.type][0], t, TYPES[ti.type]);
      assertEquals(Token.string(ti.result), TYPES[ti.type][2]);
      if(t.length > 3) assertEquals(Token.string(ti.uri), TYPES[ti.type][3]);
      tq.close();
    }
  }

  /**
   * Extends the client session.
   */
  private static class TestSession extends ClientSession {
    /**
     * Constructor.
     * @param host host name
     * @param port port
     * @param user user name
     * @param pass password
     * @throws IOException I/O exception
     */
    TestSession(final String host, final int port, final String user, final String pass)
        throws IOException {
      super(host, port, user, pass);
    }

    @Override
    public TestQuery query(final String query) throws IOException {
      return new TestQuery(query, this);
    }

    /**
     * Executes a command and sends the result to the specified output stream.
     * @param cmd server command
     * @param arg argument
     * @return string
     * @throws IOException I/O exception
     */
    byte[] exec(final ServerCmd cmd, final String arg) throws IOException {
      final ArrayOutput o = new ArrayOutput();
      sout.write(cmd.code);
      send(arg);
      sout.flush();
      final BufferInput bi = new BufferInput(sin);
      ClientSession.receive(bi, o);
      if(!ClientSession.ok(bi)) throw new BaseXException(bi.readString());
      return o.toArray();
    }
  }

  /**
   * Extends the client session.
   */
  private static class TestQuery extends ClientQuery {
    /**
     * Constructor.
     * @param q query string
     * @param s session reference
     * @throws IOException I/O exception
     */
    TestQuery(final String q, final TestSession s) throws IOException {
      super(q, s, null);
    }

    /**
     * Returns a full result item.
     * @return full string
     * @throws IOException I/O exception
     */
    TestItem full() throws IOException {
      final byte[] f = ((TestSession) cs).exec(ServerCmd.FULL, id);
      final TestItem ti = new TestItem();
      ti.type = f[0];
      if(TYPES[ti.type].length > 3) {
        for(int b = 1; b < f.length; b++) {
          if(f[b] == 0) {
            ti.uri = Arrays.copyOfRange(f, 1, b);
            ti.result = Arrays.copyOfRange(f, b + 1, f.length);
            break;
          }
        }
        assertNotNull("No extended info: " + TYPES[ti.type][0], ti.uri);
      } else {
        ti.result = Arrays.copyOfRange(f, 1, f.length);
      }
      return ti;
    }

    /**
     * Returns a typed result.
     * @return full string
     * @throws IOException I/O exception
     */
    TestItem iter() throws IOException {
      final byte[] f = ((TestSession) cs).exec(ServerCmd.RESULTS, id);
      final TestItem ti = new TestItem();
      ti.type = f[0];
      ti.result = Arrays.copyOfRange(f, 1, f.length);
      return ti;
    }
  }

  /**
   * Resulting item.
   */
  static class TestItem {
    /** Item kind/type. */
    int type;
    /** Optional URI. */
    byte[] uri;
    /** Result. */
    byte[] result;
  }

  /**
   * <p>Examples for available data types.</p>
   * <ul>
   * <li>Array position: type id</li>
   * <li>Entry 1: node type</li>
   * <li>Entry 2: example</li>
   * <li>Entry 3: textual result</li>
   * </ul>
   */
  public static final Object[][] TYPES = {
    { }, { }, { }, { }, { }, { }, { },
    // code: 7
    { "function item" },
    // code: 8 and higher
    { "node()" },
    { "text()", "text { 'a' }", "a" },
    { "processing-instruction()", "processing-instruction { 'a' } { 'b' }", "<?a b?>" },
    { "element()", "<a/>", "<a/>" },
    { "document-node()", "document { 'a' }", "a", "" },
    { "document-node(element())", "document { <a/> }", "<a/>", "" },
    { "attribute()" },
    { "comment()", "comment { 'a' } ", "<!--a-->" },
    { }, { }, { }, { }, { }, { }, { }, { },
    { }, { }, { }, { }, { }, { }, { }, { },
    // code: 32 and higher
    { "item()" },
    { "xs:untyped" },
    { "xs:anyType" },
    { "xs:anySimpleType" },
    { "xs:anyAtomicType" },
    { "xs:untypedAtomic", "data(<a>a</a>)", "a" },
    { "xs:string", "'a'", "a" },
    { "xs:normalizedString", "xs:normalizedString('a')", "a" },
    { "xs:token", "xs:token('a')", "a" },
    { "xs:language", "xs:language('a')", "a" },
    { "xs:NMTOKEN", "xs:NMTOKEN('a')", "a" },
    { "xs:Name", "xs:Name('a')", "a" },
    { "xs:NCName", "xs:NCName('a')", "a" },
    { "xs:ID", "xs:ID('a')", "a" },
    { "xs:IDREF", "xs:IDREF('a')", "a" },
    { "xs:ENTITY", "xs:ENTITY('a')", "a" },
    { "xs:float", "xs:float(1)", "1" },
    { "xs:double", "1.1e0", "1.1" },
    { "xs:decimal", "1.1", "1.1" },
    { "xs:precisionDecimal" },
    { "xs:integer", "1", "1" },
    { "xs:nonPositiveInteger", "xs:nonPositiveInteger(-1)", "-1" },
    { "xs:negativeInteger", "xs:negativeInteger(-1)", "-1" },
    { "xs:long", "xs:long(1)", "1" },
    { "xs:int", "xs:int(1)", "1" },
    { "xs:short", "xs:short(1)", "1" },
    { "xs:byte", "xs:byte(1)", "1" },
    { "xs:nonNegativeInteger", "xs:nonNegativeInteger(1)", "1" },
    { "xs:unsignedLong", "xs:unsignedLong(1)", "1" },
    { "xs:unsignedInt", "xs:unsignedInt(1)", "1" },
    { "xs:unsignedShort", "xs:unsignedShort(1)", "1" },
    { "xs:unsignedByte", "xs:unsignedByte(1)", "1" },
    { "xs:positiveInteger", "xs:positiveInteger(1)", "1" },
    { "xs:duration", "xs:duration('P1Y')", "P1Y" },
    { "xs:yearMonthDuration", "xs:yearMonthDuration('P1Y')", "P1Y" },
    { "xs:dayTimeDuration", "xs:dayTimeDuration('PT1M')", "PT1M" },
    { "xs:dateTime", "xs:dateTime('2001-01-01T23:59:59')", "2001-01-01T23:59:59" },
    { "xs:dateTimeStamp" },
    { "xs:date", "xs:date('2001-01-01')", "2001-01-01" },
    { "xs:time", "xs:time('01:01:01')", "01:01:01" },
    { "xs:gYearMonth", "xs:gYearMonth('2001-01')", "2001-01" },
    { "xs:gYear", "xs:gYear('2001')", "2001" },
    { "xs:gMonthDay", "xs:gMonthDay(' --01-01 ')", "--01-01" },
    { "xs:gDay", "xs:gDay('---01')", "---01" },
    { "xs:gMonth", "xs:gMonth('--01')", "--01" },
    { "xs:boolean", "true()", "true" },
    { "basex:binary" },
    { "xs:base64Binary", "xs:base64Binary('aaaa')", "aaaa" },
    { "xs:hexBinary", "xs:hexBinary('aa')", "AA" },
    { "xs:anyURI", "xs:anyURI('a')", "a" },
    { "xs:QName", "xs:QName('xml:a')", "xml:a", "http://www.w3.org/XML/1998/namespace" },
    { "xs:NOTATION" }
  };
}
