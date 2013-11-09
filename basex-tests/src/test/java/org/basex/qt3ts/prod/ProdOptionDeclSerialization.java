package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for serialization parameters defined in the OptionDecl production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdOptionDeclSerialization extends QT3TestSet {

  /**
   *  Test default values for serialization parameters .
   */
  @org.junit.Test
  public void serialization001() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:cdata-section-elements \"\";\n" +
      "         declare option output:doctype-public \"none\";\n" +
      "         declare option output:doctype-system \"none\";\n" +
      "         declare option output:indent \"no\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:suppress-indentation \"\";\n" +
      "         declare option output:undeclare-prefixes \"no\";\n" +
      "         declare option output:use-character-maps \"\";\n" +
      "         <result>ok</result>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  Indentation may add whitespace characters .
   */
  @org.junit.Test
  public void serialization002() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:indent \"yes\";\n" +
      "         <result>ok</result>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  it is a static error [err:XQST0108] if an output declaration appears in a library module .
   */
  @org.junit.Test
  public void serialization003() {
    final XQuery query = new XQuery(
      "\n" +
      "         import module namespace test=\"http://www.w3.org/TestModules/test\";\n" +
      "         <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/test", file("prod/Serialization/serialization1-lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0108")
    );
  }

  /**
   *  It is a static error [err:XQST0110] if the same serialization parameter is declared more than once. .
   */
  @org.junit.Test
  public void serialization004() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:indent \"yes\";\n" +
      "         declare option output:indent \"yes\";\n" +
      "         <result>ok</result>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0110")
    );
  }

  /**
   *  It is a static error [err:XQST0109] if the local name of an output declaration in the http://www.w3.org/2010/xslt-xquery-serialization namespace is not one of the serialization parameter names .
   */
  @org.junit.Test
  public void serialization005() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:doesnotexist \"yes\";\n" +
      "         <result>ok</result>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0109")
    );
  }

  /**
   *  If a processor is performing serialization, it is a static error [err:XQST0119] if the implementation is not able to process the value of the output:parameter-document declaration to produce an XDM instance.  .
   */
  @org.junit.Test
  public void serialization006() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:parameter-document \"Serialization/serialization-parameters.xml\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        error("XQST0119")
      )
    );
  }

  /**
   *  It's ok to override serialization parameters from the parameter-document doc .
   */
  @org.junit.Test
  public void serialization007() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:parameter-document \"Serialization/serialization-parameters.xml\";\n" +
      "         declare option output:indent \"yes\";\n" +
      "         <result>ok</result>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for byte-order-mark.
   *       .
   */
  @org.junit.Test
  public void serialization008() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:byte-order-mark \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for doctype-public.
   *       .
   */
  @org.junit.Test
  public void serialization009() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:doctype-public \"&#xc381;\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for cdata-section-elements.
   *       .
   */
  @org.junit.Test
  public void serialization010() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:cdata-section-elements \"::INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for doctype-system.
   *       .
   */
  @org.junit.Test
  public void serialization011() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:doctype-system \"mustnotincludebothanapostrophe&#x27;andquotationmark&#x22;\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for encoding.
   *       .
   */
  @org.junit.Test
  public void serialization012() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:encoding \"onlyasciiallowedlessthan&#x7f;\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      ||
        assertSerialError("SESU0007")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for escape-uri-attributes.
   *       .
   */
  @org.junit.Test
  public void serialization013() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:escape-uri-attributes \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for include-content-type.
   *       .
   */
  @org.junit.Test
  public void serialization014() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:include-content-type \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for indent.
   *       .
   */
  @org.junit.Test
  public void serialization015() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:indent \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for media-type.
   *       .
   */
  @org.junit.Test
  public void serialization016() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         (: the charset parameter of the media type MUST NOT be specified explicitly in the value of the media-type parameter. :)\n" +
      "         declare option output:media-type \"text/html; charset=ISO-8859-4\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for method.
   *       .
   */
  @org.junit.Test
  public void serialization017() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         (: An expanded QName with a null namespace URI, and the local part of the name equal to one of xml, xhtml, html or text, or having a non-null namespace URI :)\n" +
      "         declare option output:method \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *        A serialization error [err:SESU0011] results if the value of the normalization-form parameter 
   *        specifies a normalization form that is not supported by the serializer; the serializer MUST 
   *        signal the error.
   *       .
   */
  @org.junit.Test
  public void serialization018() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:normalization-form \"__NOT_SUPPORTED__\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SESU0011")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for omit-xml-declaration.
   *       .
   */
  @org.junit.Test
  public void serialization019() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:omit-xml-declaration \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for standalone.
   *       .
   */
  @org.junit.Test
  public void serialization020() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:standalone \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for suppress-indentation.
   *       .
   */
  @org.junit.Test
  public void serialization021() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:suppress-indentation \"::INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for undeclare-prefixes.
   *       .
   */
  @org.junit.Test
  public void serialization022() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:undeclare-prefixes \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *          It is a serialization error [err:SEPM0016] if a parameter value is invalid for the given parameter. 
   *          Wrong serialization parameter value for use-character-maps.
   *       .
   */
  @org.junit.Test
  public void serialization023() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:use-character-maps \"INVALID_VALUE\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SEPM0016")
      )
    );
  }

  /**
   *  
   *         The value of the version parameter MUST match the VersionNum production of the XML Recommendation 
   *         [XML10] or [XML11]. A serialization error [err:SESU0013] results if the value of the version 
   *         parameter specifies a version of XML that is not supported by the serializer; the serializer 
   *         MUST signal the error.
   *       .
   */
  @org.junit.Test
  public void serialization024() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         (: An unsupported xml version which matches the VersionNum of XML Recommendation XML10 :)\n" +
      "         declare option output:version \"1.14159265\";\n" +
      "         <result>ok</result>\n" +
      "      ",
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
        assertSerialization("<result>ok</result>", false)
      ||
        assertSerialError("SESU0013")
      )
    );
  }

  /**
   *  
   *          It is an error to use the HTML output method if characters which are permitted in XML but not in HTML appear in the instance of the data model.   
   *       .
   */
  @org.junit.Test
  public void serialization025() {
    final XQuery query = new XQuery(
      "declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"html\";\n" +
      "         (: control characters not allowed in html :)\n" +
      "         <result>ok&#x7f;</result>\n" +
      "      ",
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
        assertSerialization("<result>ok\u007f</result>", false)
      ||
        assertSerialError("SEPM0014")
      )
    );
  }

  /**
   *  
   *          XML method: it's an error to use standalone=yes if the document is not well-formed   
   *       .
   */
  @org.junit.Test
  public void serialization026() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:standalone \"yes\";\n" +
      "\t\t (<a/>,<b/>)\n" +
      "      ",
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
        serializationMatches("<\\?xml[^t]+\\?><a/><b/>", "")
      ||
        assertSerialError("SEPM0004")
      )
    );
  }

  /**
   *  
   *          XML method: it's an error to use standalone=no if the document is not well-formed   
   *       .
   */
  @org.junit.Test
  public void serialization027() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:standalone \"yes\";\n" +
      "\t\t \"banana\"\n" +
      "      ",
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
        serializationMatches("banana", "")
      ||
        assertSerialError("SEPM0004")
      )
    );
  }

  /**
   *  
   *          XML method: it's an error to use doctype-system if the document is not well-formed   
   *       .
   */
  @org.junit.Test
  public void serialization028() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:doctype-system \"http://www.example.com/\";\n" +
      "\t\t (<a/>,<b/>)\n" +
      "      ",
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
        serializationMatches("<\\?xml.*\\?><a/><b/>", "")
      ||
        assertSerialError("SEPM0004")
      )
    );
  }

  /**
   *  
   *          XML method: it's an error to use doctype-system if the document is not well-formed   
   *       .
   */
  @org.junit.Test
  public void serialization029() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:doctype-system \"http://www.example.com/\";\n" +
      "\t\t \"potato\"\n" +
      "      ",
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
        serializationMatches("potato", "")
      ||
        assertSerialError("SEPM0004")
      )
    );
  }

  /**
   *  
   *          XML method: it's NOT an error to use standalone=omit if the document is not well-formed   
   *       .
   */
  @org.junit.Test
  public void serialization030() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:standalone \"omit\";\n" +
      "\t\t \"banana\"\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      serializationMatches("banana", "")
    );
  }

  /**
   *  
   *          XML method: it's an error to use standalone together with omit-xml-declaration   
   *       .
   */
  @org.junit.Test
  public void serialization031() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:standalone \"yes\";\n" +
      "         declare option output:omit-xml-declaration \"yes\";\n" +
      "\t\t <a/>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialError("SEPM0009")
    );
  }

  /**
   *  
   *          XML method: it's an error to use standalone together with omit-xml-declaration   
   *       .
   */
  @org.junit.Test
  public void serialization032() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace output = \"http://www.w3.org/2010/xslt-xquery-serialization\";\n" +
      "         declare option output:method \"xml\";\n" +
      "         declare option output:standalone \"no\";\n" +
      "         declare option output:omit-xml-declaration \"yes\";\n" +
      "\t\t <a/>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialError("SEPM0009")
    );
  }
}
