package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the DirectConstructor production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirectConstructor extends QT3TestSet {

  /**
   *  comment constructor - regular .
   */
  @org.junit.Test
  public void constrComment1() {
    final XQuery query = new XQuery(
      "<!--comment-->",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--comment-->", false)
    );
  }

  /**
   *  comment constructor - regular content .
   */
  @org.junit.Test
  public void constrComment2() {
    final XQuery query = new XQuery(
      "fn:data(<!--comment-->) = \"comment\"",
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
   *  comment constructor - empty .
   */
  @org.junit.Test
  public void constrComment3() {
    final XQuery query = new XQuery(
      "<!---->",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!---->", false)
    );
  }

  /**
   *  comment constructor - empty content .
   */
  @org.junit.Test
  public void constrComment4() {
    final XQuery query = new XQuery(
      "fn:data(<!---->) = \"\"",
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
   *  comment constructor - special characters .
   */
  @org.junit.Test
  public void constrComment5() {
    final XQuery query = new XQuery(
      "<!--<?&-&lt;&#x20;><![CDATA[x]]>-->",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!--<?&-&lt;&#x20;><![CDATA[x]]>-->", false)
    );
  }

  /**
   *  comment constructor - single dash .
   */
  @org.junit.Test
  public void constrComment6() {
    final XQuery query = new XQuery(
      "<!----->",
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
   *  comment constructor - trailing dash .
   */
  @org.junit.Test
  public void constrComment7() {
    final XQuery query = new XQuery(
      "<!--comment--->",
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
   *  comment constructor - double dash .
   */
  @org.junit.Test
  public void constrComment8() {
    final XQuery query = new XQuery(
      "<!--com--ment-->",
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
   *  PI constructor - regular .
   */
  @org.junit.Test
  public void constrPiContent1() {
    final XQuery query = new XQuery(
      "<?pi content?>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?pi content?>", false)
    );
  }

  /**
   *  PI constructor - regular content .
   */
  @org.junit.Test
  public void constrPiContent2() {
    final XQuery query = new XQuery(
      "fn:data(<?pi content?>) = \"content\"",
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
   *  PI constructor - empty .
   */
  @org.junit.Test
  public void constrPiContent3() {
    final XQuery query = new XQuery(
      "<?pi ?>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?pi ?>", false)
    );
  }

  /**
   *  PI constructor - empty content .
   */
  @org.junit.Test
  public void constrPiContent4() {
    final XQuery query = new XQuery(
      "fn:data(<?pi ?>) = \"\"",
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
   *  PI constructor - special characters .
   */
  @org.junit.Test
  public void constrPiContent5() {
    final XQuery query = new XQuery(
      "<?pi <?&--&lt;&#x20;><![CDATA[x]]> ?>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?pi <?&--&lt;&#x20;><![CDATA[x]]> ?>", false)
    );
  }

  /**
   *  PI constructor - leading whitespace .
   */
  @org.junit.Test
  public void constrPiContent6() {
    final XQuery query = new XQuery(
      "<pi>{string-to-codepoints(<?pi x?>)}</pi>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<pi>120</pi>", false)
    );
  }

  /**
   *  PI constructor - trailing whitespace .
   */
  @org.junit.Test
  public void constrPiContent7() {
    final XQuery query = new XQuery(
      "<pi>{string-to-codepoints(<?pi x ?>)}</pi>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<pi>120 32</pi>", false)
    );
  }

  /**
   *  PI constructor - syntax error .
   */
  @org.junit.Test
  public void constrPiContent8() {
    final XQuery query = new XQuery(
      "<?pi ?>?>",
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
   *  PI constructor - target name xml .
   */
  @org.junit.Test
  public void constrPiTarget1() {
    final XQuery query = new XQuery(
      "<?XmL?>",
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
   *  PI constructor - target name xml .
   */
  @org.junit.Test
  public void constrPiTarget2() {
    final XQuery query = new XQuery(
      "<?XML?>",
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
   *  PI constructor - target name xml .
   */
  @org.junit.Test
  public void constrPiTarget3() {
    final XQuery query = new XQuery(
      "<?xml?>",
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
   *  PI constructor - target name xml .
   */
  @org.junit.Test
  public void constrPiTarget4() {
    final XQuery query = new XQuery(
      "<?xMl?>",
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
   *  Test that processing-instruction data does not receive any special treatment. .
   */
  @org.junit.Test
  public void k2DirectConOther1() {
    final XQuery query = new XQuery(
      "string(<?target \"\"''content&amp;amp;ss&amp;#00; &amp;#x2014;?>) eq \"\"\"\"\"''content&amp;amp;amp;ss&amp;amp;#00; &amp;amp;#x2014;\"",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther10() {
    final XQuery query = new XQuery(
      "<!",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther11() {
    final XQuery query = new XQuery(
      "<foo><!--",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther12() {
    final XQuery query = new XQuery(
      "<foo><!-",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther13() {
    final XQuery query = new XQuery(
      "<foo><!",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther14() {
    final XQuery query = new XQuery(
      "<!--",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther15() {
    final XQuery query = new XQuery(
      "<!-- content",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther16() {
    final XQuery query = new XQuery(
      "<!-- content -",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther17() {
    final XQuery query = new XQuery(
      "<!---",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther18() {
    final XQuery query = new XQuery(
      "<!----",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther19() {
    final XQuery query = new XQuery(
      "<!----",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther2() {
    final XQuery query = new XQuery(
      "<!- oo -->",
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
   *  Space is not allowed between '<?' and the target name. .
   */
  @org.junit.Test
  public void k2DirectConOther20() {
    final XQuery query = new XQuery(
      "<? spaceIsNotAllowedBefore ?>",
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
   *  '|' is not allowed in PI target names. .
   */
  @org.junit.Test
  public void k2DirectConOther21() {
    final XQuery query = new XQuery(
      "<?invalid|char ?>",
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
   *  ':' is not allowed in PI target names. .
   */
  @org.junit.Test
  public void k2DirectConOther22() {
    final XQuery query = new XQuery(
      "<?invalid:char ?>",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther23() {
    final XQuery query = new XQuery(
      "<?xml:char ?>",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther24() {
    final XQuery query = new XQuery(
      "<?",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther25() {
    final XQuery query = new XQuery(
      "<?",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther26() {
    final XQuery query = new XQuery(
      "<?xml ?>",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther27() {
    final XQuery query = new XQuery(
      "<?XML ?>",
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
   *  Syntax error in processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther28() {
    final XQuery query = new XQuery(
      "<?XmL ?>",
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
   *  Extract the name from a PI. .
   */
  @org.junit.Test
  public void k2DirectConOther29() {
    final XQuery query = new XQuery(
      "local-name(<?xmlSUFFIX content?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xmlSUFFIX")
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther3() {
    final XQuery query = new XQuery(
      "<! oo -->",
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
   *  Extract the name from a PI. .
   */
  @org.junit.Test
  public void k2DirectConOther30() {
    final XQuery query = new XQuery(
      "local-name(<?PREFIXxml content?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PREFIXxml")
    );
  }

  /**
   *  Extract the name from a PI. .
   */
  @org.junit.Test
  public void k2DirectConOther31() {
    final XQuery query = new XQuery(
      "local-name(<?PREFIXxmlSUFFIX content?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PREFIXxmlSUFFIX")
    );
  }

  /**
   *  Extract the data from a directly constructed PI. .
   */
  @org.junit.Test
  public void k2DirectConOther32() {
    final XQuery query = new XQuery(
      "<?validchar ???<<???? <? >?hm???> eq \"???<<???? <? >?hm??\"",
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
   *  Extract the data from a directly constructed PI. .
   */
  @org.junit.Test
  public void k2DirectConOther33() {
    final XQuery query = new XQuery(
      "<?validchar content ?> eq \"content \"",
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
   *  Extract the data from a directly constructed PI. .
   */
  @org.junit.Test
  public void k2DirectConOther34() {
    final XQuery query = new XQuery(
      "<?validchar content a b c asdada dad ?> eq \"content a b c asdada dad \"",
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
   *  Extract the content from a comment. .
   */
  @org.junit.Test
  public void k2DirectConOther35() {
    final XQuery query = new XQuery(
      "<!-- some - - - - content - - - >>>>> << >>><>& ;& --> eq \" some - - - - content - - - >>>>> << >>><>&amp; ;&amp; \"",
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
   *  Extract the content from a comment. .
   */
  @org.junit.Test
  public void k2DirectConOther36() {
    final XQuery query = new XQuery(
      "<!-- - - - - - - - - - - - - - - - - --> eq \" - - - - - - - - - - - - - - - - \"",
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
   *  Enclosed expressions aren't recognized inside processing instructions. .
   */
  @org.junit.Test
  public void k2DirectConOther37() {
    final XQuery query = new XQuery(
      "<?target {1 + 1}?>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?target {1 + 1}?>", false)
    );
  }

  /**
   *  Space after PI data isn't stripped. .
   */
  @org.junit.Test
  public void k2DirectConOther38() {
    final XQuery query = new XQuery(
      "string(<?target content ?>) eq \"content \"",
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
   *  Mixed content involving two text nodes and one comment node. .
   */
  @org.junit.Test
  public void k2DirectConOther39() {
    final XQuery query = new XQuery(
      "<e>a<!--data tar-->b</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>a<!--data tar-->b</e>", false)
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther4() {
    final XQuery query = new XQuery(
      "<!-- oo ->",
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
   *  Mixed content involving two text nodes and one comment node. .
   */
  @org.junit.Test
  public void k2DirectConOther40() {
    final XQuery query = new XQuery(
      "string(<e>a<!--data tar-->b</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ab")
    );
  }

  /**
   *  Ensure comment content doesn't receive special interpretation. .
   */
  @org.junit.Test
  public void k2DirectConOther41() {
    final XQuery query = new XQuery(
      "string(<!-- content&amp;amp;ss&amp;amp;#00; &amp;#x2014;-->) eq \" content&amp;amp;amp;ss&amp;amp;amp;#00; &amp;amp;#x2014;\"",
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
   *  A PI target must be specified. .
   */
  @org.junit.Test
  public void k2DirectConOther42() {
    final XQuery query = new XQuery(
      "<??>",
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
   *  Two subsequent processing instructions as a primary expression is a syntax error. .
   */
  @org.junit.Test
  public void k2DirectConOther43() {
    final XQuery query = new XQuery(
      "<?a?><?b ?>",
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
   *  Test the atomized type of a PI. .
   */
  @org.junit.Test
  public void k2DirectConOther44() {
    final XQuery query = new XQuery(
      "data(<?target data?>) instance of xs:string",
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
   *  Test the atomized type of a comment. .
   */
  @org.junit.Test
  public void k2DirectConOther45() {
    final XQuery query = new XQuery(
      "data(<!-- a comment -->) instance of xs:string",
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
   *  Test serialization of tricky comment content. .
   */
  @org.junit.Test
  public void k2DirectConOther46() {
    final XQuery query = new XQuery(
      "<!-- <<<>><&%(/?=(=)&entity;-]]> -->",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- <<<>><&%(/?=(=)&entity;-]]> -->", false)
    );
  }

  /**
   *  Use an ending ']]>' string inside a processing instruction. .
   */
  @org.junit.Test
  public void k2DirectConOther47() {
    final XQuery query = new XQuery(
      "<?target ]]>?>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?target ]]>?>", false)
    );
  }

  /**
   *  Ensure xml:id is properly normalized, when using a direct constructor. The value is also an invalid xs:ID value, hence the allowed error code. .
   */
  @org.junit.Test
  public void k2DirectConOther48() {
    final XQuery query = new XQuery(
      "string(exactly-one(<e xml:id=\" ab c d \"/>/@*))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "ab c d")
      ||
        error("XQDY0091")
      )
    );
  }

  /**
   *  Check that an attribute value's value is properly read and serialized. Since the whitespace 
   *         is expressed with character references they are preserved and hence aren't subject to for 
   *         instance end-of-line handling. Subsequently, the serialization process must escape such characters 
   *         in order to not have the parser normalize the values when being read back in. .
   */
  @org.junit.Test
  public void k2DirectConOther49() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-49.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\" &#xD;&#xA;&#x9;&#xD;&#xD;&#xD;&#xD;      &#xD; &#xD;     &#xD;&#xA; &#xD;&#xA; &#xD;&#xA;\"/>", false)
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther5() {
    final XQuery query = new XQuery(
      "<!--- oo ->",
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
   *  Ensure that EOL-normalization also takes place in CDATA sections. .
   */
  @org.junit.Test
  public void k2DirectConOther50() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-50.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("true", false)
    );
  }

  /**
   *  Ensure that EOL-normalization also takes place in CDATA sections(#2). .
   */
  @org.junit.Test
  public void k2DirectConOther51() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-51.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("true", false)
    );
  }

  /**
   *  Ensure that EOLs are normalized in text nodes. .
   */
  @org.junit.Test
  public void k2DirectConOther52() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-52.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>a \n\n\n \nstring literal \n</e>", false)
    );
  }

  /**
   *  Ensure that EOLs are normalized in comment nodes. .
   */
  @org.junit.Test
  public void k2DirectConOther53() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-53.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><!--a \n\n\n \nstring literal \n--></e>", false)
    );
  }

  /**
   *  Ensure that EOLs are normalized in processing instructions. .
   */
  @org.junit.Test
  public void k2DirectConOther54() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirectConstructor/K2-DirectConOther-54.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><?target a \n\n\n \nstring literal \n?></e>", false)
    );
  }

  /**
   *  A single '>'. .
   */
  @org.junit.Test
  public void k2DirectConOther55() {
    final XQuery query = new XQuery(
      ">",
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
   *  A single '<'. .
   */
  @org.junit.Test
  public void k2DirectConOther56() {
    final XQuery query = new XQuery(
      "<",
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
   *  '/>' is a syntax error. .
   */
  @org.junit.Test
  public void k2DirectConOther57() {
    final XQuery query = new XQuery(
      "/>",
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
   *  Ensure whitespace at the beginning of attributes, isn't stripped. .
   */
  @org.junit.Test
  public void k2DirectConOther58() {
    final XQuery query = new XQuery(
      "<e attr=\"   a\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"   a\"/>", false)
    );
  }

  /**
   *  Check attribute normalization, with apostrophes. .
   */
  @org.junit.Test
  public void k2DirectConOther59() {
    final XQuery query = new XQuery(
      "<elem attr='\n" +
      "                            \n" +
      "                    \n" +
      "                    \n" +
      "                    \n" +
      "\n" +
      "                 '/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"                                                                                                               \"/>", false)
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther6() {
    final XQuery query = new XQuery(
      "<!-->",
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
   *  Check normalization of attributes. .
   */
  @org.junit.Test
  public void k2DirectConOther60() {
    final XQuery query = new XQuery(
      "<e attr=\"    \"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"    \"/>", false)
    );
  }

  /**
   *  An unmatched curly in an attribute value template(quotes). .
   */
  @org.junit.Test
  public void k2DirectConOther61() {
    final XQuery query = new XQuery(
      "<element attributeName=\"}\"/>",
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
   *  An unmatched curly in an attribute value template(apostrophes). .
   */
  @org.junit.Test
  public void k2DirectConOther62() {
    final XQuery query = new XQuery(
      "<element attributeName='}'/>",
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
   *  '1' is an invalid value for xml:id. .
   */
  @org.junit.Test
  public void k2DirectConOther63() {
    final XQuery query = new XQuery(
      "<a xml:id=\"1\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<a xml:id=\"1\"/>", false)
      ||
        error("XQDY0091")
      )
    );
  }

  /**
   *  'PRESERVE' is an invalid value for xml:space. .
   */
  @org.junit.Test
  public void k2DirectConOther64() {
    final XQuery query = new XQuery(
      "<a xml:space=\"PRESERVE\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<a xml:space=\"PRESERVE\"/>", false)
      ||
        error("XQDY0092")
      )
    );
  }

  /**
   *  ' preserve' is an invalid value for xml:space. .
   */
  @org.junit.Test
  public void k2DirectConOther65() {
    final XQuery query = new XQuery(
      "<a xml:space=\"   preserve\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<a xml:space=\"   preserve\"/>", false)
      ||
        error("XQDY0092")
      )
    );
  }

  /**
   *  Ensure direct element constructors makes namespaces in scope for other node constructors. .
   */
  @org.junit.Test
  public void k2DirectConOther66() {
    final XQuery query = new XQuery(
      "declare namespace a=\"http://example.com/NotThis\"; declare namespace g=\"http://example.com/NotThis2\"; <a xmlns:a=\"http://example.com/NotThis\" xmlns:b=\"http://example.com\"> <b xmlns:a=\"http://example.com/\" xmlns:c=\"http://example.com/c\"> <c xmlns:d=\"http://example.com/d\"/> { for $i in in-scope-prefixes(<e/>) order by $i return $i, \"|\", for $i in in-scope-prefixes(element e {()}) order by $i return $i } <d xmlns:e=\"http://example.com/d\"/> </b> </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns:b=\"http://example.com\" xmlns:a=\"http://example.com/NotThis\"><b xmlns:c=\"http://example.com/c\" xmlns:a=\"http://example.com/\"><c xmlns:d=\"http://example.com/d\"/>a b c xml | a b c xml<d xmlns:e=\"http://example.com/d\"/></b></a>", false)
    );
  }

  /**
   *  Ensure the default namespace is picked up by independent child constructors. .
   */
  @org.junit.Test
  public void k2DirectConOther67() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://example.com/3\">{namespace-uri-from-QName(node-name(<e/>)), namespace-uri-from-QName(node-name(element e2 {()}))}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://example.com/3\">http://example.com/3 http://example.com/3</e>", false)
    );
  }

  /**
   *  Ensure that EOLs are normalized in attributes surrounded with quotes. .
   */
  @org.junit.Test
  public void k2DirectConOther68() {
    final XQuery query = new XQuery(
      "<c b=\"a \n" +
      "\n" +
      "\n" +
      " \n" +
      "string literal \n" +
      "\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<c b=\"a      string literal  \"/>", false)
    );
  }

  /**
   *  Ensure that EOLs are normalized in attributes surrounded with apostrophes. .
   */
  @org.junit.Test
  public void k2DirectConOther69() {
    final XQuery query = new XQuery(
      "<c b=\"a \n" +
      "\n" +
      "\n" +
      " \n" +
      "string literal \n" +
      "\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<c b=\"a      string literal  \"/>", false)
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther7() {
    final XQuery query = new XQuery(
      "<!-- -- -->",
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
   *  Ensure that EOLs are normalized in text nodes, but not when specified with character references. .
   */
  @org.junit.Test
  public void k2DirectConOther70() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<a>\n" +
      "\n" +
      "\n" +
      " \n" +
      "|&#xD; &#xD;&#xA;</a>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 10 10 32 10 124 13 32 13 10")
    );
  }

  /**
   *  A small fragment using namespaces. .
   */
  @org.junit.Test
  public void k2DirectConOther71() {
    final XQuery query = new XQuery(
      "<a xmlns:p=\"urn:abbrev:NS\"><b p:c=\"\" p:d=\"\"/></a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns:p=\"urn:abbrev:NS\"><b p:c=\"\" p:d=\"\"/></a>", false)
    );
  }

  /**
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther8() {
    final XQuery query = new XQuery(
      "<!--",
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
   *  Syntax error in comment. .
   */
  @org.junit.Test
  public void k2DirectConOther9() {
    final XQuery query = new XQuery(
      "<!-",
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
}
