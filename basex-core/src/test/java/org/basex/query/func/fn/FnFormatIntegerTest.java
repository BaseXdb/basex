package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for fn:format-integer.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatIntegerTest extends SandboxTest {
  /** fn:format-integer. */
  @Test public void formatInteger() {
    query("format-integer((), 'w')", "");
    query("format-integer(123, '1')", "123");
    query("format-integer(123, '001')", "123");
    query("format-integer(123, '00001')", "00123");
    query("format-integer(-123, '9')", "-123");
    query("format-integer(-123, '999')", "-123");
    query("format-integer(-123, '99999')", "-00123");
    query("format-integer(0, '0')", "0");
    query("format-integer(0, '000')", "000");
    query("format-integer(0, '00000')", "00000");

    query("format-integer(1, '#,0')", "1");
    query("format-integer(1, '#.0')", "1");
    query("format-integer(1, '#,#,0')", "1");
    query("format-integer(1, '#0')", "1");
    query("format-integer(12, '#,#1')", "12");

    query("format-integer(1, '0.0')", "0.1");
    query("format-integer(1, '0,0')", "0,1");
    query("format-integer(11, '#,0')", "1,1");
    query("format-integer(111, '##0.0')", "1.1.1");

    query("string-join(for $i in 1 to 4 return format-integer($i, 'a'))", "abcd");
    query("string-join(for $i in 1 to 4 return format-integer($i, 'A'))", "ABCD");
    query("string-join(for $i in 1 to 20 return format-integer($i, 'i'), '|')",
        "i|ii|iii|iv|v|vi|vii|viii|ix|x|xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx");
    query("string-join(for $i in 1 to 20 return format-integer($i, 'I'), '|')",
        "I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX");
    query("string-join(for $i in 1 to 20 return format-integer($i, '١'), '|')",
        "١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|"
        + "١٢|١٣|١٤|١٥|١٦|١٧|"
        + "١٨|١٩|٢٠");
    query("string-join(for $i in 1 to 20 return format-integer($i, '٩'), '|')",
        "١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|"
        + "١٢|١٣|١٤|١٥|١٦|١٧|"
        + "١٨|١٩|٢٠");
    query("string-join(for $i in 1 to 10 return format-integer($i, 'Ww'), '|')",
        "One|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten");
    query("string-join(for $i in 1 to 10 return format-integer($i, 'W'), '|')",
        "ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN");
    query("string-join(for $i in 1 to 10 return format-integer($i, 'w'), '|')",
        "one|two|three|four|five|six|seven|eight|nine|ten");
    query("format-integer(1500000, '0,000')", "1,500,000");
    query("format-integer(1500000, '#,###,000')", "1,500,000");
    query("format-integer(1500000, '###𐄀0,00')", "1500𐄀0,00");
    query("format-integer(602347826, '#(000)000-000')", "602)347-826");
    query("format-integer(2, 'W;o')", "SECOND");
    query("format-integer(1, '1;o(-en)')", "1st");
    query("format-integer(1, 'Ww;t')", "One");
    query("format-integer((), 'Ww')", "");
    query("format-integer(1234, '#,𐒠𐒠𐒠')",
        "𐒡,𐒢𐒣𐒤");
    query("format-integer(1, '𐒠')", "𐒡");
    query("format-integer(-85, '1;o')", "-85th");
    query("format-integer(-5, 'Ww;o')", "-Fifth");
    query("format-integer(0, 'Ww')", "Zero");
    query("string-join(for $i in 1 to 5 return format-integer($i, '①'))",
        "①②③④⑤");
    query("string-join(for $i in 1 to 5 return format-integer($i, '⑴'))",
        "⑴⑵⑶⑷⑸");
    query("string-join(for $i in 1 to 5 return format-integer($i, '⒈'))",
        "⒈⒉⒊⒋⒌");
    query("string-join(for $i in 1 to 5 return format-integer($i, 'Α'))",
        "ΑΒΓΔΕ");
    query("string-join(for $i in 1 to 5 return format-integer($i, 'α'))",
        "αβγδε");
    query("string-join(for $i in 1 to 5 return format-integer($i, 'א'))",
        "אבגדה");
    query("format-integer(12345678901,'# 000')", "12 345 678 901");
    query("string-join(for $i in (1 to 23, 151, 302, 469, 2025) "
        + "return concat($i, '=', format-integer($i, '一'), '|'))",
        "1=一|2=二|3=三|4=四|5=五|6=六|7=七|8=八|9=九|"
        + "10=十|11=十一|12=十二|13=十三|14=十四|"
        + "15=十五|16=十六|17=十七|18=十八|19=十九|"
        + "20=二十|21=二十一|22=二十二|23=二十三|"
        + "151=百五十一|302=三百二|469=四百六十九|"
        + "2025=二千二十五|");
  }

  /** fn:format-integer, error cases. */
  @Test public void formatIntegerErrors() {
    error("format-integer(123, '0١')", DIFFMAND_X);
    error("format-integer(123, '١0')", DIFFMAND_X);
    error("format-integer(123, '0١0')", DIFFMAND_X);
    error("format-integer(1234, '٣0٣')", DIFFMAND_X);
    error("format-integer(1, '')", PICEMPTY);
    error("format-integer(1234, 'Ww;o())')", INVMODIFIER_X);
    error("format-integer(1500000, '0,000,')", INVGROUP_X);
    error("format-integer(1500000, '11#0,000')", OPTAFTER_X);
    error("format-integer(1500000, '0,00,,000')", INVGROUP_X);
    error("format-integer(1, '#--0')", INVGROUP_X);
  }
}
