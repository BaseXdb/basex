package org.basex.query.func;

import org.basex.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNFormatIntTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<dummy/>";

    queries = new Object[][] {
      { "010", str("123"), "format-integer(123, '1')" },
      { "020", str("123"), "format-integer(123, '001')" },
      { "030", str("00123"), "format-integer(123, '00001')" },
      { "040", str("-123"), "format-integer(-123, '9')" },
      { "050", str("-123"), "format-integer(-123, '999')" },
      { "060", str("-00123"), "format-integer(-123, '99999')" },
      { "070", str("0"), "format-integer(0, '0')" },
      { "080", str("000"), "format-integer(0, '000')" },
      { "090", str("00000"), "format-integer(0, '00000')" },

      { "100", str(",1"), "format-integer(1, '#,0')" },
      { "102", str(".1"), "format-integer(1, '#.0')" },
      { "104", str(",1"), "format-integer(1, '#,#,0')" },
      { "106", str("1"), "format-integer(1, '#0')" },

      { "110", str("0.1"), "format-integer(1, '0.0')" },
      { "112", str("0,1"), "format-integer(1, '0,0')" },
      { "114", str("1,1"), "format-integer(11, '#,0')" },
      { "116", str("1.1.1"), "format-integer(111, '##0.0')" },

      { "120", str("abcd"),
        "string-join(for $i in 1 to 4 return format-integer($i, 'a'))" },
      { "122", str("ABCD"),
        "string-join(for $i in 1 to 4 return format-integer($i, 'A'))" },
      { "124", str("i|ii|iii|iv|v|vi|vii|viii|ix|x|" +
        "xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx"),
        "string-join(for $i in 1 to 20 return format-integer($i, 'i'), '|')" },
      { "126", str("I|II|III|IV|V|VI|VII|VIII|IX|X|" +
        "XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX"),
        "string-join(for $i in 1 to 20 return format-integer($i, 'I'), '|')" },
      { "140", str("\u0661|\u0662|\u0663|\u0664|\u0665|\u0666|\u0667|\u0668|" +
          "\u0669|\u0661\u0660|\u0661\u0661|\u0661\u0662|\u0661\u0663|\u0661" +
          "\u0664|\u0661\u0665|\u0661\u0666|\u0661\u0667|\u0661\u0668|\u0661" +
          "\u0669|\u0662\u0660"),
        "string-join(for $i in 1 to 20 " +
        "return format-integer($i, '\u0661'), '|')" },
      { "150", str("\u0661|\u0662|\u0663|\u0664|\u0665|\u0666|\u0667|\u0668|" +
          "\u0669|\u0661\u0660|\u0661\u0661|\u0661\u0662|\u0661\u0663|\u0661" +
          "\u0664|\u0661\u0665|\u0661\u0666|\u0661\u0667|\u0661\u0668|\u0661" +
          "\u0669|\u0662\u0660"),
        "string-join(for $i in 1 to 20 " +
        "return format-integer($i, '\u0669'), '|')" },
      { "160", str("One|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'Ww'), '|')" },
      { "170", str("ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'W'), '|')" },
      { "180", str("one|two|three|four|five|six|seven|eight|nine|ten"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'w'), '|')" },
      { "210", str("1,500,000"),
        "format-integer(1500000, '0,000')" },
      { "220", str("1,500,000"),
        "format-integer(1500000, '#,###,000')" },
      { "290", str("1500\ud800\udd000,00"),
        "format-integer(1500000, '###\ud800\udd000,00')" },
      { "300", str("(602)347-826"),
        "format-integer(602347826, '#(000)000-000')" },
      { "310", str("SECOND"), "format-integer(2, 'W;o')" },
      { "330", str("1st"), "format-integer(1, '1;o(-en)')" },
      { "350", str("One"), "format-integer(1, 'Ww;t')" },
      { "360", str(""), "format-integer((), 'Ww')" },
      { "41", str("\uD801\uDCA1,\uD801\uDCA2\uD801\uDCA3\uD801\uDCA4"),
        "format-integer(1234, '#,\uD801\uDCA0\uD801\uDCA0\uD801\uDCA0')" },
      { "420", str("\uD801\uDCA1"), "format-integer(1, '\uD801\uDCA0')" },
      { "430", str("-85th"), "format-integer(-85, '1;o')" },
      { "440", str("-Fifth"), "format-integer(-5, 'Ww;o')" },
      { "450", str("Zero"), "format-integer(0, 'Ww')" },
      { "460", str("\u2460\u2461\u2462\u2463\u2464"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u2460'))" },
      { "470", str("\u2474\u2475\u2476\u2477\u2478"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u2474'))" },
      { "480", str("\u2488\u2489\u248A\u248B\u248C"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u2488'))" },
      { "490", str("\u0391\u0392\u0393\u0394\u0395"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u0391'))" },
      { "500", str("\u03b1\u03b2\u03b3\u03b4\u03b5"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u03b1'))" },
      { "505", str("\u03b1\u03b2\u03b3\u03b4\u03b5"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u03b1'))" },
      { "507", str("\u05d0\u05d1\u05d2\u05d3\u05d4"),
        "string-join(for $i in 1 to 5 return format-integer($i, '\u05d0'))" },
      { "510", str("12 345 678 901"),
        "format-integer(12345678901,'# 000')" },
      { "520", str("1=\u4E00|2=\u4E8C|3=\u4E09|4=\u56DB|" +
        "5=\u4E94|6=\u516D|7=\u4E03|8=\u516B|9=\u4E5D|10=\u5341|11=\u5341" +
        "\u4E00|12=\u5341\u4E8C|13=\u5341\u4E09|14=\u5341\u56DB|15=\u5341" +
        "\u4E94|16=\u5341\u516D|17=\u5341\u4E03|18=\u5341\u516B|19=\u5341" +
        "\u4E5D|20=\u4E8C\u5341|21=\u4E8C\u5341\u4E00|22=\u4E8C\u5341\u4E8C|" +
        "23=\u4E8C\u5341\u4E09|151=\u767E\u4E94\u5341\u4E00|302=\u4E09\u767E" +
        "\u4E8C|469=\u56DB\u767E\u516D\u5341\u4E5D|2025=\u4E8C\u5343" +
        "\u4E8C\u5341\u4E94|"),
        "string-join(for $i in (1 to 23, 151, 302, 469, 2025) " +
        "return concat($i, '=',  format-integer($i, '\u4e00'), '|'))" },

      // errors
      { "err010", "format-integer(123, '0\u0661')" },
      { "err020", "format-integer(123, '\u06610')" },
      { "err030", "format-integer(123, '0\u06610')" },
      { "err035", "format-integer(1234, '\u06630\u0663')" },
      { "err040", "format-integer(a, 'w')" }, // XPDY0002
      { "err050", "format-integer(1, '')" }, // XTDE0030
      { "err070", "format-integer(1234, 'Ww;o())')" },
      { "err100", "format-integer(1500000, '0,000,')" },
      { "err110", "format-integer(1500000, '11#0,000')" },
      // check: { "err140", "format-integer(1500000, ',123')" },
      { "err150", "format-integer(1500000, '0,00,,000')" },
      { "err160", "format-integer(1, '#--0')" }
    };
  }
}
