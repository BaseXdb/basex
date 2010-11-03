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
        str("١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|١٢|١٣|١٤|١٥|١٦|١٧|١٨|١٩|٢٠"),
        "string-join(for $i in 1 to 20 return format-integer($i, '١'), '|')" },
      { "format-integer-015",
        str("١|٢|٣|٤|٥|٦|٧|٨|٩|١٠|١١|١٢|١٣|١٤|١٥|١٦|١٧|١٨|١٩|٢٠"),
        "string-join(for $i in 1 to 20 return format-integer($i, '٩'), '|')" },
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
      { "format-integer-029", str("1500𐄀0,00"),
        "format-integer(1500000, '###𐄀0,00')" },
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
      { "format-integer-041", str("𐒡,𐒢𐒣𐒤"),
        "format-integer(1234, '#,𐒠𐒠𐒠')" },*/
      /*{ "format-integer-042", str("1234"), "format-integer(1234, 'ﯴ')" },*/
      { "format-integer-043", str("-85th"), "format-integer(-85, '1o')" },
      { "format-integer-044", str("-Fifth"), "format-integer(-5, 'Wwo')" },
      { "format-integer-045", str("Zero"), "format-integer(0, 'Ww')" },
      { "format-integer-046", str("①|②|③|④|⑤"),
        "string-join(for $i in 1 to 5 return format-integer($i, '①'), '|')" },
      { "format-integer-047", str("⑴|⑵|⑶|⑷|⑸"),
        "string-join(for $i in 1 to 5 return format-integer($i, '⑴'), '|')" },
      { "format-integer-048", str("⒈|⒉|⒊|⒋|⒌"),
        "string-join(for $i in 1 to 5 return format-integer($i, '⒈'), '|')" },
      { "format-integer-049", str("Α|Β|Γ|Δ|Ε"),
        "string-join(for $i in 1 to 5 return format-integer($i, 'Α'), '|')" },
      { "format-integer-050", str("α|β|γ|δ|ε"),
        "string-join(for $i in 1 to 5 return format-integer($i,'α'), '|')" },
      /* { "format-integer-051", str("12 345 678 901"),
        "format-integer(12345678901,'# 000')" },
      { "format-integer-052", str("1=一|2=二|3=三|4=四|5=五|6=六|7=七|8=八|" +
        "9=九|10=十|11=十一|12=十二|13=十三|14=十四|15=十五|16=十六|17=十七|" +
        "18=十八|19=十九|20=二十|21=二十一|22=二十二|23=二十三|" +
        "151=百五十一|302=三百二|469=四百六十九|2025=二千二十五"),
        "string-join(for $i in (1 to 23, 151, 302, 469, 2025) " +
        "return concat($i, '=',  format-integer($i, '一'), '|')" },
      */
    };
  }
}
