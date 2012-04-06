package org.basex.test.query;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.lang.annotation.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.basex.test.query.simple.*;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runners.model.*;

/**
 * Test if index and non-index full-text queries behave the same way.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
@InputData("<x>A x B</x>")
public class FTIndexQueryTest extends SandboxTest {
  /** Name of database with full-text index. */
  private static final String NAME_IX = NAME + "ix";
  /** Context of database with full-text index. */
  private static final Context CTX_IX = new Context();

  /** Rule to create database without full-text index. */
  @Rule
  public final CreateDBRule createdb = new CreateDBRule(NAME, context);

  /** Rule to create database with full-text index. */
  @Rule
  public final CreateDBRule createdbix = new CreateDBRule(NAME_IX, CTX_IX);

  /** Static initialization. */
  @BeforeClass
  public static void setUpClass() {
    context.prop.set(Prop.FTINDEX, false);
    CTX_IX.prop.set(Prop.FTINDEX, true);
    CTX_IX.mprop.set(MainProp.DBPATH, sandbox().path());
  }

  /** Static clean-up. */
  @AfterClass
  public static void cleanUpClass() {
    CTX_IX.close();
  }

  /** Run all tests from {@link FTTest}. */
  @Test
  @InputData(FTTest.DOC)
  public void testFTTest() {
    for(final Object[] q : FTTest.QUERIES)
      if(q.length == 3) assertQuery((String) q[2]);
  }

  /** Run all tests from {@link XPathMarkFTTest}. */
  @Test
  @InputData(XPathMarkFTTest.DOC)
  public void testXPathMarkFTTest() {
    for(final Object[] q : XPathMarkFTTest.QUERIES) assertQuery((String) q[2]);
  }

  /** Word distance test. */
  @Test
  @Ignore("GH-359")
  public void testWordsDistance() {
    assertQuery(
        "//*[text() contains text 'A B' all words distance exactly 0 words]");
  }

  /** {@code ft:mark()} test with ft option {@code all words}. */
  @Test
  @Ignore("GH-337")
  public void testFTMarkAllWords() {
    assertQuery(
        _FT_MARK.args(" //*[text() contains text {'A B'} all words], 'b'"));
  }

  /** {@code ft:mark()} test with {@code ftand}. */
  @Test
  public void testFTMarkFTAnd() {
    assertQuery(
        _FT_MARK.args(" //*[text() contains text 'A' ftand 'B'], 'b'"));
  }

  /**
   * Assert that a query returns the same result with and without ft index.
   * @param q query
   */
  private static void assertQuery(final String q) {
    try {
      assertEquals("Query failed:\n" + q + '\n',
          new XQuery(q).execute(context), new XQuery(q).execute(CTX_IX));
      // [DP]: assert that index was really used
    } catch (final BaseXException e) {
      fail("Query failed:\n" + q + "\nMessage: " + e.getMessage());
    }
  }
}

/** Annotation to provide input data for a test. */
@Retention(RetentionPolicy.RUNTIME)
@interface InputData {
  /**
   * Input data.
   * @return string
   */
  String value();
}

/**
 * Test rule, creating a database before executing each test method, which is
 * annotated with {@link InputData}.
 */
class CreateDBRule implements MethodRule {
  /** Database context. */
  final Context ctx;
  /** Database name. */
  final String db;

  /**
   * Constructor.
   * @param d database
   * @param c database context
   */
  public CreateDBRule(final String d, final Context c) {
    db = d;
    ctx = c;
  }

  @Override
  public Statement apply(final Statement base, final FrameworkMethod method,
      final Object target) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        InputData input = method.getAnnotation(InputData.class);
        if(input == null) {
          input = method.getMethod().getDeclaringClass().getAnnotation(
              InputData.class);
        }
        if(input != null) new CreateDB(db, input.value()).execute(ctx);
        base.evaluate();
        if(input != null) new DropDB(db).execute(ctx);
      }
    };
  }
}
