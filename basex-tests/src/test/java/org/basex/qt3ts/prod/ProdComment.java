package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the Comment production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdComment extends QT3TestSet {

  /**
   *  A test whose essence is: `(3(: comment inbetween comment inbetween .
   */
  @org.junit.Test
  public void kXQueryComment1() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-1                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `(3(: comment inbetween :)- 1) eq 2`. :)\n" +
      "(:*******************************************************:)\n" +
      "(3(: comment inbetween :)- 1) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An empty comment at the very beginning of an expression. .
   */
  @org.junit.Test
  public void kXQueryComment10() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-10                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: An empty comment at the very beginning of an expression. :)\n" +
      "(:*******************************************************:)\n" +
      "(::) 1 eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An empty comment after a function's paranteses. .
   */
  @org.junit.Test
  public void kXQueryComment11() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-11                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: An empty comment after a function's paranteses. :)\n" +
      "(:*******************************************************:)\n" +
      "true()(::)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for loop with a comment inbetween. set up loop .
   */
  @org.junit.Test
  public void kXQueryComment12() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-12                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A for loop with a comment inbetween.         :)\n" +
      "(:*******************************************************:)\n" +
      "for (: set up loop :) $i in 3 return $i eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `if((: comment inbetween comment inbetween .
   */
  @org.junit.Test
  public void kXQueryComment13() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-13                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `if((: comment inbetween :)) then 1 else 1`. :)\n" +
      "(:*******************************************************:)\n" +
      "if((: comment inbetween :)) then 1 else 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A syntactically invalid comment that never ends. .
   */
  @org.junit.Test
  public void kXQueryComment14() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-14                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A syntactically invalid comment that never ends. :)\n" +
      "(:*******************************************************:)\n" +
      "1(: this comment does not end:",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A comment inside a comment that isn't terminated. content (: this comment does not end .
   */
  @org.junit.Test
  public void kXQueryComment15() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-15                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A comment inside a comment that isn't terminated. :)\n" +
      "(:*******************************************************:)\n" +
      "1(: content (: this comment does not end :)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A comment inside a comment that does not start properly. content this comment does not start properly .
   */
  @org.junit.Test
  public void kXQueryComment16() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-16                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A comment inside a comment that does not start properly. :)\n" +
      "(:*******************************************************:)\n" +
      "1(: content this comment does not start properly :) :)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Colons and paranteses appearing freely in comment content. ((( : )) ))ladl: :(): ()()(dahsi ()()( dad: ) .
   */
  @org.junit.Test
  public void kXQueryComment17() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-17                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: Colons and paranteses appearing freely in comment content. :)\n" +
      "(:*******************************************************:)\n" +
      "1(: ((( : )) ))ladl:  :(): ()()(dahsi ()()( dad: ) :) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Three comments appearing serially inside a comment. (:one comment another comment a third .
   */
  @org.junit.Test
  public void kXQueryComment18() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-18                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: Three comments appearing serially inside a comment. :)\n" +
      "(:*******************************************************:)\n" +
      "1(: (:one comment:) content (:another comment:) content (:a third:):)\n" +
      "\t\t\t   eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test stressing many nested comments. (:(:(:(:(:(:(: .
   */
  @org.junit.Test
  public void kXQueryComment19() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-19                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test stressing many nested comments.       :)\n" +
      "(:*******************************************************:)\n" +
      "1(:(:(:(:(:(:(:(::):):):):):):):) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `1 (: a (: nested a (: nested .
   */
  @org.junit.Test
  public void kXQueryComment2() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-2                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `1 (: a (: nested :) comment :) eq 1`. :)\n" +
      "(:*******************************************************:)\n" +
      "1 (: a (: nested :) comment :) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A syntactically invalid comment that doesn't properly start. .
   */
  @org.junit.Test
  public void kXQueryComment20() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-20                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A syntactically invalid comment that doesn't properly start. :)\n" +
      "(:*******************************************************:)\n" +
      ": :) 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A test whose essence is: `1 (: comment (: inside comment (: inside .
   */
  @org.junit.Test
  public void kXQueryComment3() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-3                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `1 (: comment (: inside :) comment :) eq 1`. :)\n" +
      "(:*******************************************************:)\n" +
      "1 (: comment (: inside :) comment :) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparison of two string literals, whose content reminds of comments. .
   */
  @org.junit.Test
  public void kXQueryComment4() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-4                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: Comparison of two string literals, whose content reminds of comments. :)\n" +
      "(:*******************************************************:)\n" +
      "\"reminds of a comment :)\" eq\n" +
      "\t\t    \"reminds of a comment :)\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `5 instance (: strange place for a comment strange place for a comment .
   */
  @org.junit.Test
  public void kXQueryComment5() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-5                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `5 instance (: strange place for a comment :) of item()`. :)\n" +
      "(:*******************************************************:)\n" +
      "5 instance (: strange place for a comment :) of item()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `1 (: simple comment simple comment .
   */
  @org.junit.Test
  public void kXQueryComment6() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-6                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `1 (: simple comment :) eq 1`. :)\n" +
      "(:*******************************************************:)\n" +
      "1 (: simple comment :) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `1 (: comment (: inside comment (: inside .
   */
  @org.junit.Test
  public void kXQueryComment7() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-7                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `1 (: comment (: inside :) NEW LINE comment :) eq 1`. :)\n" +
      "(:*******************************************************:)\n" +
      "1 (: comment (: inside :)\n" +
      "\t\t\t\tNEW LINE comment :) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(: "recursive comments must be balanced, this one is not "recursive comments must be balanced, this one is not .
   */
  @org.junit.Test
  public void kXQueryComment8() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-8                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: A test whose essence is: `(: \"recursive comments must be balanced, this one is not :)\" :)`. :)\n" +
      "(:*******************************************************:)\n" +
      "(: \"recursive comments must be \n" +
      "\tbalanced, this one is not :)\" :)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  An empty comment inbetween the 'eq' operator and a number literal. .
   */
  @org.junit.Test
  public void kXQueryComment9() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K-XQueryComment-9                               :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:22+01:00                       :)\n" +
      "(: Purpose: An empty comment inbetween the 'eq' operator and a number literal. :)\n" +
      "(:*******************************************************:)\n" +
      "1 eq (::)1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An invalid comment after a name test. .
   */
  @org.junit.Test
  public void k2XQueryComment1() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K2-XQueryComment-1                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:21+01:00                       :)\n" +
      "(: Purpose: An invalid comment after a name test.        :)\n" +
      "(:*******************************************************:)\n" +
      "let $i := <e>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                        </e>\n" +
      "                              return $i/b(:  ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  An invalid comment after a name test(#2). .
   */
  @org.junit.Test
  public void k2XQueryComment2() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K2-XQueryComment-2                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:21+01:00                       :)\n" +
      "(: Purpose: An invalid comment after a name test(#2).    :)\n" +
      "(:*******************************************************:)\n" +
      "let $i := <e>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                        </e>\n" +
      "                              return $i/b(: some : content (:some content  ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Have a computed comment constructor as a last step. some : content (:some content .
   */
  @org.junit.Test
  public void k2XQueryComment3() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K2-XQueryComment-3                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:21+01:00                       :)\n" +
      "(: Purpose: Have a computed comment constructor as a last step. :)\n" +
      "(:*******************************************************:)\n" +
      "let $i := <e>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                        </e>\n" +
      "                                        return $i/b/comment(: some : content (:some content:):){\"content\"}  ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--content--><!--content--><!--content-->", false)
    );
  }

  /**
   *  Have a direct comment constructor as a last step. some : content (:some content .
   */
  @org.junit.Test
  public void k2XQueryComment4() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K2-XQueryComment-4                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:21+01:00                       :)\n" +
      "(: Purpose: Have a direct comment constructor as a last step. :)\n" +
      "(:*******************************************************:)\n" +
      "let $i := <e>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                        </e>\n" +
      "                                        return $i/(: some : content (:some content:):)<!--content-->  ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--content-->", false)
    );
  }

  /**
   *  Have a direct comment constructor as a last step(#2). some : content (:some content .
   */
  @org.junit.Test
  public void k2XQueryComment5() {
    final XQuery query = new XQuery(
      "(:*******************************************************:)\n" +
      "(: Test: K2-XQueryComment-5                              :)\n" +
      "(: Written by: Frans Englich                             :)\n" +
      "(: Date: 2007-11-22T11:31:21+01:00                       :)\n" +
      "(: Purpose: Have a direct comment constructor as a last step(#2). :)\n" +
      "(:*******************************************************:)\n" +
      "let $i := <e>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                            <b/>\n" +
      "                                        </e>\n" +
      "                                        return $i/b/(: some : content (:some content:):)<!--content-->  ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--content--><!--content--><!--content-->", false)
    );
  }

  /**
   *  Simple use case for XQuery comments This is a comment .
   */
  @org.junit.Test
  public void xQueryComment001() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment001 :)\n" +
      "(: Description: Simple use case for XQuery comments :)\n" +
      "\n" +
      "\n" +
      "(: This is a comment :)\n" +
      "<result/>\n" +
      "\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Simple use case for XQuery comments This is a comment .
   */
  @org.junit.Test
  public void xQueryComment002() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment002 :)\n" +
      "(: Description: Simple use case for XQuery comments :)\n" +
      "\n" +
      "\n" +
      "\n" +
      "\n" +
      "(: This is a comment :)\n" +
      "(//fs:Folder)[1]/fs:File[1]/fs:FileName\n" +
      "",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      query.namespace("fs", "http://www.example.com/filesystem");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000000</fs:FileName>", false)
    );
  }

  /**
   *  Simple use case for XQuery comment containing '-' This is a comment- .
   */
  @org.junit.Test
  public void xQueryComment003() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment003 :)\n" +
      "(: Description: Simple use case for XQuery comment containing '-' :)\n" +
      "\n" +
      "\n" +
      "(:This is a comment-:)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Empty comment .
   */
  @org.junit.Test
  public void xQueryComment004() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment004 :)\n" +
      "(: Description: Empty comment :)\n" +
      "\n" +
      "\n" +
      "(::)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Comment containing only '-' - .
   */
  @org.junit.Test
  public void xQueryComment005() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment004 :)\n" +
      "(: Description: Comment containing only '-' :)\n" +
      "\n" +
      "\n" +
      "(:-:)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Comment containing ':' this is a comment : .
   */
  @org.junit.Test
  public void xQueryComment006() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment006 :)\n" +
      "(: Description: Comment containing ':' :)\n" +
      "\n" +
      "\n" +
      "(: this is a comment ::)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Comment containing ')' this is a comment ) .
   */
  @org.junit.Test
  public void xQueryComment007() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment007 :)\n" +
      "(: Description: Comment containing ')' :)\n" +
      "\n" +
      "\n" +
      "(: this is a comment ):)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Simple example of embedded comments this is a comment (: this is an embedded comment .
   */
  @org.junit.Test
  public void xQueryComment008() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment008 :)\n" +
      "(: Description: Simple example of embedded comments :)\n" +
      "\n" +
      "\n" +
      "(: this is a comment (: this is an embedded comment :):)\n" +
      "<result/>\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Comments inside a conditional expression test (: yada (: neato yada yada yada .
   */
  @org.junit.Test
  public void xQueryComment009() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment009 :)\n" +
      "(: Description: Comments inside a conditional expression :)\n" +
      "\n" +
      "\n" +
      "\n" +
      "if (:test (: yada (: neato :) :) :) (/fs:MyComputer) \n" +
      "\tthen (: yada :) \"true\"\n" +
      "\telse \"false\"\n" +
      "",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      query.namespace("fs", "http://www.example.com/filesystem");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   *  Comments inside a conditional expression comment this is the then case this is the else case .
   */
  @org.junit.Test
  public void xQueryComment010() {
    final XQuery query = new XQuery(
      "\n" +
      "(: Name: XQueryComment010 :)\n" +
      "(: Description: Comments inside a conditional expression :)\n" +
      "if (: comment :) \n" +
      "  ( //fs:Folder[1]/fs:FolderName/text() = \"Folder00000000000\" ) \n" +
      "then (: this is the then case :) ( true() )\n" +
      "else (: this is the else case :) ( false() )\n" +
      "\n" +
      "",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      query.namespace("fs", "http://www.example.com/filesystem");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comments inside a conditional expression test t2 t3 .
   */
  @org.junit.Test
  public void xQueryComment011() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment011 :)\n" +
      "(: Description: Comments inside a conditional expression :)\n" +
      "\n" +
      "\n" +
      "\n" +
      "if (:test:)(:t2:)(:t3:) (/fs:MyComputer) \n" +
      "\tthen \"true\"\n" +
      "\telse \"false\"\n" +
      "\n" +
      "",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      query.namespace("fs", "http://www.example.com/filesystem");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   *  Comments that looks like a function call test .
   */
  @org.junit.Test
  public void xQueryComment012() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment012 :)\n" +
      "(: Description: Comments that looks like a function call :)\n" +
      "\n" +
      "\n" +
      "/south(: test :)\n" +
      "\n" +
      "",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<south mark=\"s0\" />", false)
    );
  }

  /**
   *  Comments inside a sequence expression comment .
   */
  @org.junit.Test
  public void xQueryComment013() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment013 :)\n" +
      "(: Description: Comments inside a sequence expression :)\n" +
      "\n" +
      "\n" +
      "(1, 2, (: comment :) 3, 4)\n" +
      "\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  Comments inside a cast expression type comment .
   */
  @org.junit.Test
  public void xQueryComment014() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment014 :)\n" +
      "(: Description: Comments inside a cast expression :)\n" +
      "\n" +
      "\n" +
      "\"10\" cast as (: type comment :) xs:integer ?\n" +
      "\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   *  Incorrect comment syntax .
   */
  @org.junit.Test
  public void xQueryComment015() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment015 :)\n" +
      "(: Description: Incorrect comment syntax :)\n" +
      "\n" +
      "\n" +
      "(! Wrong syntax :)\n" +
      "<empty/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid comment .
   */
  @org.junit.Test
  public void xQueryComment016() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment016 :)\n" +
      "(: Description: Invalid comment :)\n" +
      "\n" +
      "\n" +
      "(:)\n" +
      "<empty/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid comment .
   */
  @org.junit.Test
  public void xQueryComment017() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment017 :)\n" +
      "(: Description: Invalid comment :)\n" +
      "\n" +
      "\n" +
      "(:: )\n" +
      "<empty/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid comment .
   */
  @org.junit.Test
  public void xQueryComment018() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment018 :)\n" +
      "(: Description: Invalid comment :)\n" +
      "\n" +
      "\n" +
      "-- Wrong comment format\n" +
      "<empty/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Old style comment syntax .
   */
  @org.junit.Test
  public void xQueryComment019() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment019 :)\n" +
      "(: Description: Old style comment syntax :)\n" +
      "\n" +
      "\n" +
      "{-- Wrong comment format --}\n" +
      "<empty/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Comment containing an enclosed expression { "comment" } .
   */
  @org.junit.Test
  public void xQueryComment020() {
    final XQuery query = new XQuery(
      "(: Name: XQueryComment020 :)\n" +
      "(: Description: Comment containing an enclosed expression :)\n" +
      "\n" +
      "\n" +
      "(: { \"comment\" } :)\n" +
      "<result/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }
}
