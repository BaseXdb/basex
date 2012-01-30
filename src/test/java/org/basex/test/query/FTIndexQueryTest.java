package org.basex.test.query;

import static org.basex.util.Util.name;
import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Test if index and non-index full-text queries behave the same way.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class FTIndexQueryTest {
  /** Context of database without full-text index. */
  private static final Context CTX = new Context();
  /** Context of database with full-text index. */
  private static final Context CTX_IX = new Context();
  /** Name of database without full-text index. */
  private static final String DBNAME = name(FTIndexQueryTest.class);
  /** Name of database with full-text index. */
  private static final String DBNAME_IX = name(FTIndexQueryTest.class) + "ix";

  /** Rule to create database without full-text index. */
  @Rule
  public final CreateDBRule createdb = new CreateDBRule(DBNAME, CTX);

  /** Rule to create database with full-text index. */
  @Rule
  public final CreateDBRule createdbix = new CreateDBRule(DBNAME_IX, CTX_IX);

  /**
   * Static initialization.
   * @throws BaseXException if initialization fails
   */
  @BeforeClass
  public static void setUpClass() throws BaseXException {
    new Set(Prop.FTINDEX, false).execute(CTX);
    new Set(Prop.FTINDEX, true).execute(CTX_IX);
  }

  /** Static clean-up. */
  @AfterClass
  public static void cleanUpClass() {
    CTX.close();
    CTX_IX.close();
  }

  /**
   * Word distance test.
   * @throws BaseXException if query execution fails
   */
  @Test
  @Ignore("GH-359")
  @InputData("<x>A x B</x>")
  public void testWordsDistance() throws BaseXException {
    final String q =
        "//*[text() contains text 'A B' all words distance exactly 0 words]";
    assertEquals(new XQuery(q).execute(CTX), new XQuery(q).execute(CTX_IX));
  }
}

/** Annotation to provide input data for a test. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface InputData {
  /** Input data. */
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
        if(input != null) new CreateDB(db, input.value()).execute(ctx);
        base.evaluate();
      }
    };
  }
}
