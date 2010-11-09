package org.basex.test.query;

/**
 * XQuery functions tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNFormatIntegerTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<x/>";

    queries = new Object[][] {
      { "format-integer-001", str("123"), "format-integer(123, '1')" },
      { "format-integer-002", str("123"), "format-integer(123, '001')" },
      { "format-integer-003", str("00123"), "format-integer(123, '00001')" },
      { "format-integer-004", str("-123"), "format-integer(-123, '9')" },
      { "format-integer-005", str("-123"), "format-integer(-123, '999')" },
      { "format-integer-006", str("-00123"), "format-integer(-123, '99999')" },
      { "format-integer-007", str("0"), "format-integer(0, '0')" },
      { "format-integer-008", str("000"), "format-integer(0, '000')" },
      { "format-integer-009", str("00000"), "format-integer(0, '00000')" },
      { "format-integer-010", str("abcd"),
        "string-join(for $i in 1 to 4 return format-integer($i, 'a'))" },
      { "format-integer-011", str("ABCD"),
        "string-join(for $i in 1 to 4 return format-integer($i, 'A'))" },
      { "format-integer-012", str("i|ii|iii|iv|v|vi|vii|viii|ix|x|" +
        "xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx"),
        "string-join(for $i in 1 to 20 return format-integer($i, 'i'), '|')" },
      { "format-integer-013", str("I|II|III|IV|V|VI|VII|VIII|IX|X|" +
        "XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX"),
        "string-join(for $i in 1 to 20 return format-integer($i, 'I'), '|')" },
      { "format-integer-014",
        str("\u0661|\u0662|\u0663|\u0664|\u0665|\u0666|\u0667|\u0668|\u0669|" +
            "\u0661\u0660|\u0661\u0661|\u0661\u0662|\u0661\u0663|\u0661" +
            "\u0664|\u0661\u0665|\u0661\u0666|\u0661\u0667|\u0661\u0668|" +
            "\u0661\u0669|\u0662\u0660"),
        "string-join(for $i in 1 to 20 " +
        "return format-integer($i, '\u0661'), '|')" },
      { "format-integer-015",
        str("\u0661|\u0662|\u0663|\u0664|\u0665|\u0666|\u0667|\u0668|\u0669|" +
            "\u0661\u0660|\u0661\u0661|\u0661\u0662|\u0661\u0663|\u0661" +
            "\u0664|\u0661\u0665|\u0661\u0666|\u0661\u0667|\u0661\u0668|" +
            "\u0661\u0669|\u0662\u0660"),
        "string-join(for $i in 1 to 20 " +
        "return format-integer($i, '\u0669'), '|')" },
      { "format-integer-016",
        str("One|Two|Three|Four|Five|Six|Seven|Eight|Nine|Ten"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'Ww'), '|')" },
      { "format-integer-017",
        str("ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'W'), '|')" },
      { "format-integer-018",
        str("one|two|three|four|five|six|seven|eight|nine|ten"),
        "string-join(for $i in 1 to 10 return format-integer($i, 'w'), '|')" },
      { "format-integer-019", "format-integer(a, 'w')" }, // XPDY0002
      { "format-integer-020", "format-integer(1, '')" }, // XTDE0030
      /* { "format-integer-021", str("1,500,000"),
        "format-integer(1500000, '0,000')" },
      { "format-integer-022", str("1,500,000"),
        "format-integer(1500000, '#,###,000')" },
      { "format-integer-023", "format-integer(1500000, '0,000,')" },
      { "format-integer-024", "format-integer(1500000, '11#0,000')" },
      { "format-integer-025", "format-integer(1500000, '#')" },
      { "format-integer-026", "format-integer(1500000, '#a')" },
      { "format-integer-027", "format-integer(1500000, ',123')" },
      { "format-integer-028", "format-integer(1500000, '0,00,,000')" },
      { "format-integer-029", str("1500\ud800\udd0001000,00"),
        "format-integer(1500000, '###\ud800\udd000,00')" },
      { "format-integer-030", str("(602)347-826"),
        "format-integer(602347826, '#(000)000-000')" }, */
      { "format-integer-031", str("SECOND"), "format-integer(2, 'Wo')" },
      /* { "format-integer-032", str("Erster"),
        "format-integer(1, 'Wwo(-er)', 'de' cast as xs:language)" }, */
      { "format-integer-033", str("1st"), "format-integer(1, '1o(-en)')" },
      { "format-integer-034", "format-integer(1, '1o(-er)a')" },
      { "format-integer-035", str("One"), "format-integer(1, 'Wwt')" },
      { "format-integer-036", str(""), "format-integer((), 'Ww')" },
      { "format-integer-037", "format-integer(1234, 'Wwo())')" },
      { "format-integer-038", "format-integer(1234, '()Wwo')" },
      { "format-integer-039", "format-integer(1234, '\n')" },
      /* { "format-integer-040", "format-integer(1234, '123١')" },
      { "format-integer-041",
        str("\uD801\uDCA1,\uD801\uDCA2\uD801\uDCA3\uD801\uDCA4"),
        "format-integer(1234, '#,\uD801\uDCA0\uD801\uDCA0\uD801\uDCA0')" },*/
      /*{ "format-integer-042", str("1234"), "format-integer(1234, 'ﯴ')" },*/
      { "format-integer-043", str("-85th"), "format-integer(-85, '1o')" },
      { "format-integer-044", str("-Fifth"), "format-integer(-5, 'Wwo')" },
      { "format-integer-045", str("Zero"), "format-integer(0, 'Ww')" },
      { "format-integer-046", str("\u2460|\u2461|\u2462|\u2463|\u2464"),
        "string-join(for $i in 1 to 5 " +
        "return format-integer($i, '\u2460'), '|')" },
      { "format-integer-047", str("\u2474|\u2475|\u2476|\u2477|\u2478"),
        "string-join(for $i in 1 to 5 " +
        "return format-integer($i, '\u2474'), '|')" },
      { "format-integer-048", str("\u2488|\u2489|\u248A|\u248B|\u248C"),
        "string-join(for $i in 1 to 5 " +
        "return format-integer($i, '\u2488'), '|')" },
      { "format-integer-049", str("\u0391|\u0392|\u0393|\u0394|\u0395"),
        "string-join(for $i in 1 to 5 " +
        "return format-integer($i, '\u0391'), '|')" },
      { "format-integer-050", str("\u03b1|\u03b2|\u03b3|\u03b4|\u03b5"),
        "string-join(for $i in 1 to 5 return format-integer($i,'α'), '|')" },
      /*{ "format-integer-051", str("12 345 678 901"),
        "format-integer(12345678901,'# 000')" },
      { "format-integer-052", str("1=\u4E00|2=\u4E8C|3=\u4E09|4=\u56DB|" +
        "5=\u4E94|6=\u516D|7=\u4E03|8=\u516B|9=\u4E5D|10=\u5341|11=\u5341" +
        "\u4E00|12=\u5341\u4E8C|13=\u5341\u4E09|14=\u5341\u56DB|15=\u5341" +
        "\u4E94|16=\u5341\u516D|17=\u5341\u4E03|18=\u5341\u516B|19=\u5341" +
        "\u4E5D|20=\u4E8C\u5341|21=\u4E8C\u5341\u4E00|22=\u4E8C\u5341\u4E8C|" +
        "23=\u4E8C\u5341\u4E09|151=\u767E\u4E94\u5341\u4E00|302=\u4E09\u767E" +
        "\u4E8C|469=\u56DB\u767E\u516D\u5341\u4E5D|2025=\u4E8C\u5343" +
        "\u4E8C\u5341\u4E94"),
        "string-join(for $i in (1 to 23, 151, 302, 469, 2025) " +
        "return concat($i, '=',  format-integer($i, '\u4e00'), '|'))" },*/
    };
  }
}
