package org.basex.test.query;

import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Context;
import static org.basex.core.Prop.NL;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.BuilderSerializer;
import org.basex.query.QueryProcessor;
import org.basex.util.Token;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for rewritings of FLWOR-expressions.
 * @author Leo Woerteler
 */
public final class FlworOptimizeTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /**
   * Tests the relocation of a static let clause.
   * @throws Exception exception
   */
  @Test public void moveTopTest() throws Exception {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $m := $seq[(count($seq) + 1) idiv 2] " +
        "return concat($i, $j, $m)",

        "12b 13b 23b",
        "every $for in //For satisfies //Let[@var eq '$m'] << $for"
    );
  }

  /**
   * Tests the relocation of a let clause.
   * @throws Exception exception
   */
  @Test public void moveMidTest() throws Exception {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $a := $seq[$i] " +
        "return concat($i, $j, $a)",

        "12a 13a 23b",
        "let $a := //Let[@var = '$a'] return " +
          "//For[@var eq '$i'] << $a and $a << //For[@var eq '$j']"
    );
  }

  /**
   * Tests the relocation of a let clause.
   * @throws Exception exception
   */
  @Test public void dontMoveTest() throws Exception {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $b := $seq[$j] " +
        "return concat($i, $j, $b)",

        "12b 13c 23c",
        "every $for in //For satisfies $for << //Let[@var eq '$b']"
    );
  }

  /**
   * Tests the relocation of a static let clause.
   * @throws Exception exception
   */
  @Test public void moveForTest() throws Exception {
    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b in $x " +
        "return $b",

        "<x/>" + NL + "<x/>",
        "//For[@var eq '$b'] << //For[@var eq '$a']",
        "every $for in //For satisfies exactly-one(//Let) << $for"
    );
  }

  /**
   * Checks the query plan and the result.
   * @param qu query
   * @param res result
   * @param props queries on the query plan
   * @throws Exception exception
   */
  private void check(final String qu, final String res, final String... props)
      throws Exception {
    final QueryProcessor qp = new QueryProcessor(qu, CTX);
    try {
      // compare results
      assertEquals("Query result:", res, qp.execute().toString());

      // parse query plan
      CTX.openDB(CreateDB.xml(new Parser("") {
        @Override
        public void parse(final Builder build) throws IOException {
          build.startDoc(Token.token("QueryPlan"));
          qp.plan(new BuilderSerializer(build));
          build.endDoc();
        }
      }, CTX));

      // check query plan
      for(final String p : props) {
        if(!new XQuery(p).execute(CTX).equals("true"))
          fail(p + ":" + NL + qp.ctx.root);
      }
    } finally {
      qp.close();
    }
  }
}
