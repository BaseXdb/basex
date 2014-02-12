package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the BoundarySpaceDecl production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdBoundarySpaceDecl extends QT3TestSet {

  /**
   *  A simple 'declare boundary-space' declaration, specifying 'preserve'. .
   */
  @org.junit.Test
  public void kBoundarySpaceProlog1() {
    final XQuery query = new XQuery(
      "(::)declare(::)boundary-space(::)strip(::); 1 eq 1",
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
   *  A simple 'declare boundary-space' declaration, specifying 'strip'. .
   */
  @org.junit.Test
  public void kBoundarySpaceProlog2() {
    final XQuery query = new XQuery(
      "(::)declare(::)boundary-space(::)strip(::); 1 eq 1",
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
   *  Two 'declare boundary-space' declarations are invalid. .
   */
  @org.junit.Test
  public void kBoundarySpaceProlog3() {
    final XQuery query = new XQuery(
      "(::)declare(::)boundary-space(::)strip(::); (::)declare(::)boundary-space(::)preserve(::); 1 eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0068")
    );
  }

  /**
   *  The expression 'declare boundary space' is invalid. .
   */
  @org.junit.Test
  public void kBoundarySpaceProlog4() {
    final XQuery query = new XQuery(
      "(::)declare(::)boundary space(::)strip(::); 1 eq 1",
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
   *  Ensure the 'boundary-space' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2BoundarySpaceProlog1() {
    final XQuery query = new XQuery(
      "boundary-space ne boundary-space",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace001() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <a> {\"abc\"} </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>abc</a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace002() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <a> {\"abc\"} </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc </a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace003() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <a> z {\"abc\"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> z abc</a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace004() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <a> z {\"abc\"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> z abc</a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace005() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <a>&#x20;{\"abc\"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc</a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace006() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <a>&#x20;{\"abc\"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc</a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace007() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <a>&#x20;{\"abc\"}{' '}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc </a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace008() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <a>&#x20;{\"abc\"}{' '}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc </a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace009() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; <a>&#x20;{\"abc\"}{\" \"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc </a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace010() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; <a>&#x20;{\"abc\"}{\" \"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> abc </a>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace011() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space strip;\n" +
      "<res>\n" +
      "a\n" +
      "b\n" +
      "c\n" +
      "</res>\n" +
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
      assertSerialization("<res>\na\nb\nc\n</res>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace012() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space preserve;\n" +
      "<res>\n" +
      "a\n" +
      "b\n" +
      "c\n" +
      "</res>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<res>\na\nb\nc\n</res>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace013() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space strip;\n" +
      "<res>\n" +
      "  a\n" +
      "  b\n" +
      "  c\n" +
      "</res>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<res>\n  a\n  b\n  c\n</res>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace014() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space preserve;\n" +
      "<res>\n" +
      "  a\n" +
      "  b\n" +
      "  c\n" +
      "</res>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<res>\n  a\n  b\n  c\n</res>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace015() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space strip;\n" +
      "<A>  A   {\"B\"}   C   {\"D\"}  </A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A>  A   B   C   D</A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace016() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space preserve;\n" +
      "<A>  A   {\"B\"}   C   {\"D\"}  </A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A>  A   B   C   D  </A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace017() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space strip;\n" +
      "<A>  A   {\"B\"}   C   {\"D  \"}</A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A>  A   B   C   D  </A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace018() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space preserve;\n" +
      "<A>  A   {\"B\"}   C   {\"D  \"}</A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A>  A   B   C   D  </A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace019() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space strip;\n" +
      "<A> (a), (b), (c) </A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A> (a), (b), (c) </A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace020() {
    final XQuery query = new XQuery(
      "\n" +
      "declare boundary-space preserve;\n" +
      "<A> (a), (b), (c) </A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A> (a), (b), (c) </A>", false)
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace021() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; (\" \",10, 20, 30, 40,\" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  10 20 30 40  ")
    );
  }

  /**
   *  Demonstrates stripping/preserving of boundary spaces by element constructors during processing of the query .
   */
  @org.junit.Test
  public void boundarySpace022() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; (\" \",10, 20, 30, 40,\" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  10 20 30 40  ")
    );
  }

  /**
   *  Evaluation of the of a query prolog with two boundary space declarations. .
   */
  @org.junit.Test
  public void boundaryspacedeclerr1() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; declare boundary-space strip; \"abc\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0068")
    );
  }
}
