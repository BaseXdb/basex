package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:format-integer function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFormatInteger extends QT3TestSet {

  /**
   * format-integer with picture="1".
   */
  @org.junit.Test
  public void formatInteger001() {
    final XQuery query = new XQuery(
      "format-integer(123, '1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'123'")
    );
  }

  /**
   * format-integer with picture="001".
   */
  @org.junit.Test
  public void formatInteger002() {
    final XQuery query = new XQuery(
      "format-integer(123, '001')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'123'")
    );
  }

  /**
   * format-integer with picture="00001".
   */
  @org.junit.Test
  public void formatInteger003() {
    final XQuery query = new XQuery(
      "format-integer(123, '00001')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'00123'")
    );
  }

  /**
   * format-integer negative integer with picture="9".
   */
  @org.junit.Test
  public void formatInteger004() {
    final XQuery query = new XQuery(
      "format-integer(-123, '9')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'-123'")
    );
  }

  /**
   * format-integer negative integer with picture="999".
   */
  @org.junit.Test
  public void formatInteger005() {
    final XQuery query = new XQuery(
      "format-integer(-123, '999')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'-123'")
    );
  }

  /**
   * format-integer negative integer with picture="99999".
   */
  @org.junit.Test
  public void formatInteger006() {
    final XQuery query = new XQuery(
      "format-integer(-123, '99999')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'-00123'")
    );
  }

  /**
   * format-integer zero with picture="0".
   */
  @org.junit.Test
  public void formatInteger007() {
    final XQuery query = new XQuery(
      "format-integer(0, '0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'0'")
    );
  }

  /**
   * format-integer zero with picture="000".
   */
  @org.junit.Test
  public void formatInteger008() {
    final XQuery query = new XQuery(
      "format-integer(0, '000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'000'")
    );
  }

  /**
   * format-integer zero with picture="00000".
   */
  @org.junit.Test
  public void formatInteger009() {
    final XQuery query = new XQuery(
      "format-integer(0, '00000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'00000'")
    );
  }

  /**
   * format-integer with picture="a".
   */
  @org.junit.Test
  public void formatInteger010() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 4 return format-integer($i, 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'abcd'")
    );
  }

  /**
   * format-integer with picture="A".
   */
  @org.junit.Test
  public void formatInteger011() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 4 return format-integer($i, 'A'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'ABCD'")
    );
  }

  /**
   * format-integer with picture="i" (lower-case roman numerals).
   */
  @org.junit.Test
  public void formatInteger012() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 20 return format-integer($i, 'i'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'i|ii|iii|iv|v|vi|vii|viii|ix|x|xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx'")
    );
  }

  /**
   * format-integer with picture="I" (upper-case roman numerals).
   */
  @org.junit.Test
  public void formatInteger013() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 20 return format-integer($i, 'I'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX'")
    );
  }

  /**
   * format-integer using Arabic digits.
   */
  @org.junit.Test
  public void formatInteger014() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 20 return format-integer($i, '١'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|١٢|١٣|١٤|١٥|١٦|١٧|١٨|١٩|٢٠'")
    );
  }

  /**
   * format-integer using Arabic digits.
   */
  @org.junit.Test
  public void formatInteger015() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 20 return format-integer($i, '٩'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|١٢|١٣|١٤|١٥|١٦|١٧|١٨|١٩|٢٠'")
    );
  }

  /**
   * format-integer with title case words.
   */
  @org.junit.Test
  public void formatInteger016() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 10 return format-integer($i, 'Ww'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'One|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten'")
    );
  }

  /**
   * format-integer with upper-case words.
   */
  @org.junit.Test
  public void formatInteger017() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 10 return format-integer($i, 'W'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN'")
    );
  }

  /**
   * format-integer with lower-case words.
   */
  @org.junit.Test
  public void formatInteger018() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 10 return format-integer($i, 'w'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'one|two|three|four|five|six|seven|eight|nine|ten'")
    );
  }

  /**
   * format-integer - Error case.
   */
  @org.junit.Test
  public void formatInteger019() {
    final XQuery query = new XQuery(
      "format-integer(a, 'w')",
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
   * format-integer - Error case with empty picture.
   */
  @org.junit.Test
  public void formatInteger020() {
    final XQuery query = new XQuery(
      "format-integer(1, '')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - picture grouping.
   */
  @org.junit.Test
  public void formatInteger021() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '0,000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1,500,000'")
    );
  }

  /**
   * format-integer - picture grouping, with hash symbols at the end.
   */
  @org.junit.Test
  public void formatInteger022() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '#,###,000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1,500,000'")
    );
  }

  /**
   * format-integer - picture grouping.
   */
  @org.junit.Test
  public void formatInteger023() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '0,000,')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - picture grouping error.
   */
  @org.junit.Test
  public void formatInteger024() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '11#0,000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - picture grouping error.
   */
  @org.junit.Test
  public void formatInteger025() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '#')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1500000'")
    );
  }

  /**
   * format-integer - picture grouping error.
   */
  @org.junit.Test
  public void formatInteger026() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '#a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1500000'")
    );
  }

  /**
   * format-integer - picture grouping error.
   */
  @org.junit.Test
  public void formatInteger027() {
    final XQuery query = new XQuery(
      "format-integer(1500000, ',123')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - picture grouping error.
   */
  @org.junit.Test
  public void formatInteger028() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '0,00,,000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - picture grouping, with hash symbols at the end; non-BMP grouping separator.
   */
  @org.junit.Test
  public void formatInteger029() {
    final XQuery query = new XQuery(
      "format-integer(1500000, '###\ud800\udd000,00')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1500\ud800\udd000,00'")
    );
  }

  /**
   * format-integer - picture with different separators, which are irregular.
   */
  @org.junit.Test
  public void formatInteger030() {
    final XQuery query = new XQuery(
      "format-integer(602347826, '#(000)000-000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'(602)347-826'")
    );
  }

  /**
   * format-integer - Ordinal number output request.
   */
  @org.junit.Test
  public void formatInteger031() {
    final XQuery query = new XQuery(
      "format-integer(2, 'W;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'SECOND'")
    );
  }

  /**
   * format-integer - Ordinal number output request.
   */
  @org.junit.Test
  public void formatInteger033() {
    final XQuery query = new XQuery(
      "format-integer(1, '1;o(-en)')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1st'")
    );
  }

  /**
   * format-integer - ordinal number request format modifier, with invalid picture.
   */
  @org.junit.Test
  public void formatInteger034() {
    final XQuery query = new XQuery(
      "format-integer(1, '1;o(-er)z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - format modifier t.
   */
  @org.junit.Test
  public void formatInteger035() {
    final XQuery query = new XQuery(
      "format-integer(1, 'Ww;t')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'One'")
    );
  }

  /**
   * format-integer - empty sequence.
   */
  @org.junit.Test
  public void formatInteger036() {
    final XQuery query = new XQuery(
      "format-integer((), 'Ww')",
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
        assertEq("''")
      &&
        assertType("xs:string")
      &&
        assertQuery("count($result) = 1")
      )
    );
  }

  /**
   * format-integer - mismatched parens in picture.
   */
  @org.junit.Test
  public void formatInteger037() {
    final XQuery query = new XQuery(
      "format-integer(1234, 'Ww;o()(')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - misplaced parens in picture.
   */
  @org.junit.Test
  public void formatInteger038() {
    final XQuery query = new XQuery(
      "format-integer(1234, '()Ww;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1234th")
    );
  }

  /**
   * format-integer - disallowed characters in picture.
   */
  @org.junit.Test
  public void formatInteger039() {
    final XQuery query = new XQuery(
      "format-integer(1234, '\n" +
      "')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1234")
    );
  }

  /**
   * format-integer - mixed digits in picture.
   */
  @org.junit.Test
  public void formatInteger040() {
    final XQuery query = new XQuery(
      "format-integer(1234, '123١')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - non-BMP digits (Osmanya digits).
   */
  @org.junit.Test
  public void formatInteger041() {
    final XQuery query = new XQuery(
      "format-integer(1234, '#,𐒠𐒠𐒠')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'𐒡,𐒢𐒣𐒤'")
    );
  }

  /**
   * format-integer - unrecognized numbering sequence - fallback.
   */
  @org.junit.Test
  public void formatInteger042() {
    final XQuery query = new XQuery(
      "format-integer(1234, 'ﯴ')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1234'")
    );
  }

  /**
   * format-integer - negative ordinal.
   */
  @org.junit.Test
  public void formatInteger043() {
    final XQuery query = new XQuery(
      "format-integer(-85, '1;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'-85th'")
    );
  }

  /**
   * format-integer - negative ordinal in words.
   */
  @org.junit.Test
  public void formatInteger044() {
    final XQuery query = new XQuery(
      "format-integer(-5, 'Ww;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'-Fifth'")
    );
  }

  /**
   * format-integer - zero in words.
   */
  @org.junit.Test
  public void formatInteger045() {
    final XQuery query = new XQuery(
      "format-integer(0, 'Ww')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Zero'")
    );
  }

  /**
   * format-integer with circled digits.
   */
  @org.junit.Test
  public void formatInteger046() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 5 return format-integer($i, '①'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'①|②|③|④|⑤'")
    );
  }

  /**
   * format-integer with parenthesized digits.
   */
  @org.junit.Test
  public void formatInteger047() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 5 return format-integer($i, '⑴'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'⑴|⑵|⑶|⑷|⑸'")
    );
  }

  /**
   * format-integer with full-stopped digits.
   */
  @org.junit.Test
  public void formatInteger048() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 5 return format-integer($i, '⒈'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'⒈|⒉|⒊|⒋|⒌'")
    );
  }

  /**
   * format-integer with greek uppercase.
   */
  @org.junit.Test
  public void formatInteger049() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 5 return format-integer($i, 'Α'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Α|Β|Γ|Δ|Ε'")
    );
  }

  /**
   * format-integer with greek lowercase.
   */
  @org.junit.Test
  public void formatInteger050() {
    final XQuery query = new XQuery(
      "string-join(for $i in 1 to 5 return format-integer($i,'α'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'α|β|γ|δ|ε'")
    );
  }

  /**
   * format-integer with space as separator.
   */
  @org.junit.Test
  public void formatInteger051() {
    final XQuery query = new XQuery(
      "format-integer(12345678901,'# 000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'12 345 678 901'")
    );
  }

  /**
   * format-integer using Kanji sequence (see http://en.wikipedia.org/wiki/Japanese_numerals).
   */
  @org.junit.Test
  public void formatInteger052() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "                for $i in (1 to 23, 151, 302, 469, 2025) \n" +
      "                return concat($i, '=',  format-integer($i, '一')), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1=一|2=二|3=三|4=四|5=五|6=六|7=七|8=八|9=九|10=十|11=十一|12=十二|13=十三|14=十四|15=十五|16=十六|17=十七|18=十八|19=十九|20=二十|21=二十一|22=二十二|23=二十三|151=百五十一|302=三百二|469=四百六十九|2025=二千二十五'")
    );
  }

  /**
   * format-integer with leading optional-digit-sign.
   */
  @org.junit.Test
  public void formatInteger053() {
    final XQuery query = new XQuery(
      "format-integer(123,'#0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'123'")
    );
  }

  /**
   * format-integer - trailing optional-digit-sign error.
   */
  @org.junit.Test
  public void formatInteger054() {
    final XQuery query = new XQuery(
      "format-integer(123,'0#')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * format-integer - test case relates to Bug #12280 in bugzilla.
   */
  @org.junit.Test
  public void formatInteger055() {
    final XQuery query = new XQuery(
      "format-integer(1,'b;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * format-integer - test case relates to Bug #12280 in bugzilla.
   */
  @org.junit.Test
  public void formatInteger056() {
    final XQuery query = new XQuery(
      "format-integer(1,'o;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * format-integer - test case relates to Bug #12280 in bugzilla.
   */
  @org.junit.Test
  public void formatInteger057() {
    final XQuery query = new XQuery(
      "format-integer(1,'bo;o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * format-integer - test case relates to Bug #12280 in bugzilla.
   */
  @org.junit.Test
  public void formatInteger058() {
    final XQuery query = new XQuery(
      "format-integer(1,'bb')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * format-integer - semicolon at end is ignored.
   */
  @org.junit.Test
  public void formatInteger059() {
    final XQuery query = new XQuery(
      "format-integer(1,'001;')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'001'")
    );
  }

  /**
   * format-integer - semicolon as grouping separator.
   */
  @org.junit.Test
  public void formatInteger060() {
    final XQuery query = new XQuery(
      "format-integer(1234,'#;##1;')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'1;234'")
    );
  }

  /**
   * format-integer - Lone semicolon 
   *                      (unrecognized format token, defaults to "1").
   */
  @org.junit.Test
  public void formatInteger061() {
    final XQuery query = new XQuery(
      "format-integer(1, ';')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * Third argument as a string in the form of a language code.
   */
  @org.junit.Test
  public void formatInteger062() {
    final XQuery query = new XQuery(
      "format-integer(11, 'Ww', 'en')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Eleven'")
    );
  }

  /**
   * Third argument as a string not in the form of a language code.
   */
  @org.junit.Test
  public void formatInteger063() {
    final XQuery query = new XQuery(
      "format-integer(11, 'Ww', '@*!+%')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Eleven'")
    );
  }

  /**
   * Error case with missing format modifier separator.
   */
  @org.junit.Test
  public void formatInteger064() {
    final XQuery query = new XQuery(
      "format-integer(-1, '1o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }
}
