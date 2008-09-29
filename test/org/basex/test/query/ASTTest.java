package org.basex.test.query;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Num;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit Test class.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class ASTTest {
  /** Database Context. */
  private static final Context CONTEXT = new Context();
  /** Name of test file. */
  private static final String TEST_FILE = "etc/xml/test.xml";

  /**
   * Initialize XML Storage for tests.
   * @throws Exception exception
   */
  @BeforeClass
  public static void initData() throws Exception {
    Prop.chop = true;
    new CreateDB(TEST_FILE).execute(CONTEXT, null);
  }

  /**
   * Evaluates an expression, starting from the root node.
   * @param str expression string
   * @return context set
   * @throws QueryException query exception
   */
  private NodeSet evalNodes(final String str) throws QueryException {
    final Result result = eval(str, new Nodes(0, CONTEXT.data()));
    if(result instanceof NodeSet) return (NodeSet) result;
    fail("No nodes returned!");
    return null;
  }

  /**
   * Evaluates an expression.
   * @param str expression string
   * @param cont initial context set
   * @return context set
   * @throws QueryException query exception
   */
  private NodeSet evalNodes(final String str, final Nodes cont)
      throws QueryException {

    final Result result = eval(str, cont);
    if(result instanceof NodeSet) return (NodeSet) result;
    fail("No nodes returned!");
    return null;
  }

  /**
   * Evaluates an expression.
   * @param str expression string
   * @param n initial context set
   * @return context set
   * @throws QueryException query exception
   */
  private Result eval(final String str, final Nodes n) throws QueryException {
    return build(str).query(n);
  }

  /**
   * Build the expression.
   * @param str String to build
   * @return built expression the built Expression
   * @throws QueryException query exception
   */
  private XPathProcessor build(final String str) throws QueryException {
    final XPathProcessor proc = new XPathProcessor(str);
    proc.compile(CONTEXT.current());
    return proc;
  }

  /**
   * Asserts that the nodes are equal.
   * @param cs context set
   * @param nodes node array
   */
  private void assertNodeSetEquals(final NodeSet cs, final int[] nodes) {
    if(!cs.same(new NodeSet(nodes, CONTEXT.data()))) fail();
  }

  /**
   * Asserts that the node array is contained in the context set.
   * @param cs context set
   * @param nodes node array
   */
  private void assertNodeSetContains(final NodeSet cs, final int[] nodes) {
    for(final int result : nodes) {
      int j = 0;
      if(j == cs.size) fail();
      while(cs.nodes[j] != result) {
        if(++j == cs.size) fail();
      }
    }
  }

  /**
   * Asserts that the specified node is not contained in the context set.
   * @param cs context set
   * @param node node array
   */
  private void assertNodeSetNotContains(final NodeSet cs, final int node) {
    for(final int result : cs.nodes) {
      if(result == node) fail();
    }
  }

  /**
   * Test for doc node returned on '/' query.
   * @throws QueryException query exception
   */
  @Test
  public void testBasic() throws QueryException {
    final Result res = eval("3", new Nodes(0, CONTEXT.data()));
    assertTrue(new Num(3).same(res));
  }

  /**
   * Test for doc node returned on '/' query.
   * @throws QueryException query exception
   */
  @Test
  public void docNodeReturned1() throws QueryException {
    assertNodeSetEquals(evalNodes("/"), new int[] { 0 });
  }

  /**
   * Test for doc node returned on '/.' query.
   * @throws QueryException query exception
   */
  @Test
  public void docNodeReturned2() throws QueryException {
    assertNodeSetEquals(evalNodes("/."), new int[] { 0 });
  }

  /**
   * Test for doc node contained in ancestor axis.
   * @throws QueryException query exception
   */
  @Test
  public void docNodeReturned3() throws QueryException {
    assertNodeSetContains(evalNodes("ancestor::node()",
        new Nodes(5, CONTEXT.data())), new int[] { 0 });
  }

  /**
   * Test for doc node not returned on child step from doc node.
   * @throws QueryException query exception
   */
  @Test
  public void docNodeNotReturned() throws QueryException {
    assertNodeSetNotContains(evalNodes("/*"), 0);
  }

  /**
   * Test for basic name resolving.
   * @throws QueryException query exception
   */
  @Test
  public void nameResolvingBug() throws QueryException {
    assertNodeSetEquals(evalNodes("//a"), new int[] { 1 });
  }

  /**
   * Test for wildcard matching.
   * @throws QueryException query exception
   */
  @Test
  public void allNamesBug() throws QueryException {
    final NodeSet res = evalNodes("//*");
    // checking only some of the wanted results
    assertNodeSetContains(res, new int[] { 1, 2, 13 });
    // and an unwanted one
    assertNodeSetNotContains(res, 0);
  }

  /**
   * Test for attribute axis.
   * @throws QueryException query exception
   */
  @Test
  public void attributesBug() throws QueryException {
    assertNodeSetEquals(evalNodes("//*/attribute::node()"),
        new int[] { 7, 8 });
  }

  /**
   * Test for descendant-or-self axis.
   * @throws QueryException query exception
   */
  @Test
  public void descOrSelfBug() throws QueryException {
    assertNodeSetNotContains(evalNodes("//node()"), 0);
  }

  /**
   * Test for '///' not recognized as valid.
   * @throws QueryException query exception
   */
  @Test(expected = QueryException.class)
  public void multipleSlashesBug1() throws QueryException {
    evalNodes("///");
  }

  /**
   * Test for '////' not recognized as valid.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void multipleSlashesBug2() throws QueryException {
    evalNodes("////");
  }

  /**
   * Test for '//' not recognized as valid.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void doubleSlashBug2() throws QueryException {
    evalNodes("a[//]");
  }

  /**
   * Test for 'count(//)' not recognized as valid.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void doubleSlashBug3() throws QueryException {
    evalNodes("//*[count(//)]");
  }

  /**
   * Test for '//' not recognized as valid.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void doubleSlashBug1() throws QueryException {
    evalNodes("//");
  }

  /**
   * Test for './.' bug.
   * @throws QueryException exception
   */
  @Test
  public void dotSlashBug1() throws QueryException {
    assertNodeSetEquals(evalNodes("./."), new int[] { 0 });
  }

  /**
   * Test for './/.' bug.
   * @throws QueryException exception
   */
  @Test
  public void dotSlashBug2() throws QueryException {
    assertNodeSetContains(evalNodes(".//."), new int[] { 1, 2 });
  }

  /**
   * Test for './/a' bug.
   * @throws QueryException exception
   */
  @Test
  public void dotSlashBug3() throws QueryException {
    assertNodeSetEquals(evalNodes(".//a"), new int[] { 1 });
  }

  /**
   * Test for './' not allowed.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void dotSlashBug4() throws QueryException {
    evalNodes("./");
  }

  /**
   * Test for 'a/' not allowed.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void dotSlashBug5() throws QueryException {
    evalNodes("a/");
  }

  /**
   * Test for 'a[b]' bug.
   * @throws QueryException exception
   */
  @Test
  public void nameOnlyBug() throws QueryException {
    assertNodeSetEquals(evalNodes("a[b]"), new int[] { 1 });
  }

  /**
   * Test for 'a[b' throwing ParsingException, not Errors.
   * @throws QueryException exception
   */
  @Test(expected = QueryException.class)
  public void unfinishedPredicateBug() throws QueryException {
    evalNodes("a[b");
  }

  /**
   * Test for comparing text nodes with numbers.
   * @throws QueryException exception
   */
  @Test
  public void comparisonBug() throws QueryException {
    assertNodeSetEquals(evalNodes("//text()[. > 1]"),
        new int[] { 12, 16, 18, 20 });
  }

  /**
   * Test a function.
   * @throws QueryException exception
   */
  @Test
  public void functionCallTest() throws QueryException {
    assertNodeSetEquals(evalNodes("/descendant-or-self::b[last()]/text()"),
        new int[] { 18 });
  }

  /**
   * Test for comparison.
   * @throws QueryException exception
   */
  @Test
  public void comparisonTest() throws QueryException {
    assertNodeSetEquals(evalNodes(
      "/descendant-or-self::b[. > 3.2]/text()"), new int[] { 16 });
  }

  /**
   * Delete the test-database.
   */
  @AfterClass
  public static void tearDown() {
    new DropDB("test").execute(CONTEXT);
  }
}
