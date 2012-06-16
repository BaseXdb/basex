package org.basex.test.qt3ts;

import static org.basex.core.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.Compare.Flag;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Base class for generated QT3TS test sets.
 *
 * @author Leo Woerteler
 */
public abstract class QT3TestSet {

  /** Database context. */
  public Context ctx;

  /** Expected results. */
  public final ArrayList<String> expected = new ArrayList<String>();

  /** QT3TS path, possibly {@code null}. */
  public static final String QT3TS = System.getProperty("QT3TS");

  /** The current query's result. */
  public QT3Result result;

  /**
   * Initializes the context.
   * @throws BaseXException exception
   */
  @Before
  public void buildUp() throws BaseXException {
    ctx = new Context();
    new Set(Prop.CHOP, false).execute(ctx);
    new Set(Prop.INTPARSE, false).execute(ctx);
    result = null;
  }

  /** Closes the context. */
  @After
  public void tearDown() {
    ctx.close();
    ctx = null;
    result = null;
  }

  /**
   * Tests assertion.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertQuery(final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(exp);

    final XQuery query = new XQuery(exp, ctx);
    try {
      return result(query.bind("result", value).value().getBoolean(), exp);
    } catch(final XQueryException ex) {
      // should not occur
      return fail(ex.getException().getMessage());
    } finally {
      query.close();
    }
  }

  /**
   * Tests count.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertCount(final long exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(exp + " items");
    final int res = value.size();
    return result(exp == res, Util.info("% items (% found)", exp, res));
  }

  /**
   * Tests equality.
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertEq(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Equal to: '%'", expect));

    final XQuery query = new XQuery(expect, ctx);
    try {
      final XdmItem exp = query.next();
      final XdmItem res = value instanceof XdmItem ? (XdmItem) value : null;
      return result(exp.equal(res), exp.toString());
    } catch(final XQueryException err) {
      return fail(err.getException().getMessage());
    } finally {
      query.close();
    }
  }

  /**
   * Tests deep equals.
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertDeepEq(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Deep-equal to: '%'", expect));
    final XQuery query = new XQuery(expect, ctx);
    try {
      final XdmValue exp = query.value();
      return result(exp.deepEqual(value), exp.toString());
    } finally {
      query.close();
    }
  }

  /**
   * Tests permutation.
   * [LW] wrong
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertPermutation(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Permutation of: '%'", expect));
    final XQuery query = new XQuery(expect, ctx);
    try {
      // cache expected results
      final HashSet<String> exp = new HashSet<String>();
      for(final XdmItem it : query) exp.add(it.getString());
      // cache actual results
      final HashSet<String> res = new HashSet<String>();
      for(final XdmItem it : value) res.add(it.getString());

      if(exp.size() != res.size())
        return fail(Util.info("% results (found: %)", exp.size(), res.size()));

      for(final String s : exp.toArray(new String[exp.size()])) {
        if(!res.contains(s)) return fail(Util.info("% (missing)", s));
      }
      for(final String s : res.toArray(new String[exp.size()])) {
        if(!exp.contains(s))
          return fail(Util.info("% (missing in expected result)", s));
      }
      return true;
    } finally {
      query.close();
    }
  }

  /**
   * Tests the serialized result.
   * [LW] wrong
   * @param expect expected result
   * @param ignorePref ignore namespaces
   * @return optional expected test suite result
   */
  protected boolean assertSerialization(final String expect, final boolean ignorePref) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Serializes to: '%'", expect));

    final String res = normNL(asString("serialize(.,map{'indent':='no'})", value));
    if(expect.equals(res)) return true;

    // include check for comments, processing instructions and namespaces
    String flags = "'" + Flag.ALLNODES + '\'';
    if(!ignorePref) flags += ",'" + Flag.NAMESPACES + '\'';
    final String query = Function.DEEP_EQUAL_OPT.args("<X>" + expect + "</X>",
        "<X>" + res + "</X>" , "(" + flags + ")");
    return result(asBoolean(query, null), expect);
  }

  /**
   * Tests a serialization error.
   * @param code expected error code
   * @return optional expected test suite result
   */
  protected boolean assertSerialError(final String code) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Serialization error: '%'", code));
    try {
      value.toString();
      return fail(code);
    } catch(final RuntimeException qe) {
      final String res = qe.getMessage().replaceAll("\\[|\\].*\r?\n?.*", "");
      return result(code.equals("*") || code.equals(res),
          Util.info("% (found: %)", code, res));
    }
  }

  /**
   * Tests a serialization error.
   * @param code expected error code
   * @return optional expected test suite result
   */
  protected boolean error(final String code) {
    if(result.value != null) return fail(Util.info("Error: '%'", code));
    if(code.equals("*")) return true;

    final QNm err = new QNm(code, QueryText.ERRORURI);

    if(result.exc != null) {
      final QueryException qe = result.exc.getException();
      final QNm name = qe.err() != null ? qe.err().qname() : qe.qname();
      if(name != null) return result(err.eq(name), Util.info("% (found: %)", err, name));
      return fail(Util.info("% (found: %)", err, "?"));
    }

    final String res = result.error.getMessage().replaceAll("\\[|\\].*\r?\n?.*", "");
    return result(code.equals(res), Util.info("% (found: %)", code, res));
  }

  /**
   * Tests string value.
   * @param norm normalize space
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertStringValue(final boolean norm, final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("String value: '%'", exp));

    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final XdmItem it : value) {
      if(c != 0) tb.add(' ');
      tb.add(it.getString());
      c++;
    }
    return result(exp.equals(norm ? string(norm(tb.finish())) : tb.toString()), exp);
  }

  /**
   * Tests boolean.
   * @param exp expected
   * @return optional expected test suite result
   */
  protected boolean assertBoolean(final boolean exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Boolean result: %", exp));
    return result(value.getType().eq(SeqType.BLN) && value.getBoolean() == exp,
        Util.info(exp));
  }

  /**
   * Tests empty sequence.
   * @return optional expected test suite result
   */
  protected boolean assertEmpty() {
    return result(result.value == XdmEmpty.EMPTY, "()");
  }

  /**
   * Tests type.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertType(final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Has type: '%'", exp));
    try {
      final XQuery query = new XQuery("$result instance of " + exp, ctx);
      return result(query.bind("result", value).value().getBoolean(),
        Util.info("type '%' (found: '%')", exp, value.getType().toString()));
    } catch(final XQueryException ex) {
      // should not occur
      return fail(ex.getException().getMessage());
    }
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  private String asString(final String query, final XdmValue value) {
    return XQuery.string(query, value, ctx);
  }

  /**
   * Returns the boolean representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  private boolean asBoolean(final String query, final XdmValue value) {
    final XQuery qp = new XQuery(query, ctx).context(value);
    try {
      final XdmItem it = qp.next();
      return it != null && it.getBoolean();
    } finally {
      qp.close();
    }
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private static String normNL(final String in) {
    return in.replaceAll("\r\n|\r|\n", NL);
  }

  /**
   * Adds the string to the expected results if {@code success} if {@code false}
   * and returns {@code success}.
   * @param success success flag
   * @param expect expected expression
   * @return {@code success}
   */
  private boolean result(final boolean success, final String expect) {
    return success || fail(expect);
  }

  /**
   * Adds the string to the expected results.
   * @param expect expected string
   * @return {@code false}
   */
  private boolean fail(final String expect) {
    return !expected.add(expect);
  }

  /**
   * Executes the query and returns the result.
   * @param query query to be executed
   * @return result
   */
  protected QT3Result result(final XQuery query) {
    final QT3Result res = new QT3Result();
    try {
      res.value = query.value();
    } catch(final XQueryException xqe) {
      res.exc = xqe;
    } catch(final Throwable trw) {
      res.error = trw;
    } finally {
      query.close();
    }
    return res;
  }

  /**
   * Throws an {@link AssertionError} if the test failed.
   * @param success test success
   */
  protected void test(final boolean success) {
    if(!success)
      Assert.fail(Util.info("Expected one of %, found: '%'", expected, result));
  }

  /**
   * Creates an absolute file path for the given resource path.
   * @param path resource path
   * @return absolute file path
   */
  protected String file(final String path) {
    final File f = new File("src/test/resources/qt3ts",
        path).getAbsoluteFile();
    if(f.exists()) return f.getPath();

    if(QT3TS == null) return null;

    final File dir = f.getParentFile();
    if(!dir.exists()) dir.mkdirs();

    final File qt3 = new File(QT3TS, path);
    if(!qt3.canRead()) return null;
    try {
      f.createNewFile();
      final BufferedInputStream in = new BufferedInputStream(new FileInputStream(qt3));
      final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
      final byte[] buffer = new byte[1 << 13];
      for(int len; (len = in.read(buffer)) >= 0;) {
        out.write(buffer, 0, len);
      }
      in.close();
      out.close();
      return f.getPath();
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a document node from the given file path.
   * @param file file path
   * @return document node
   */
  protected XdmItem node(final String file) {
    if(file == null) return null;
    final XQuery xq = new XQuery("doc('" + file + "')", ctx);
    try {
      return xq.next();
    } finally {
      xq.close();
    }
  }

  /**
   * Structure for storing XQuery results.
   */
  public static class QT3Result {
    /** Query result. */
    XdmValue value;
    /** Query exception. */
    XQueryException exc;
    /** Query error. */
    Throwable error;

    @Override
    public String toString() {
      return value != null ? value.toString() : exc != null
          ? '[' + exc.getCode() + "] " + exc.toString()
          : error.toString();
    }
  }
}
