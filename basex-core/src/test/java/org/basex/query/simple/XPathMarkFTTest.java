package org.basex.query.simple;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * XPathMark functional tests. Node results are checked via their {@code pre} values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XPathMarkFTTest extends SandboxTest {
  /** Test document. */
  private static final String DOC = """
    <?xml version='1.0' encoding='UTF-8'?>
    <A id='n1' pre='1' post='26' xml:lang='en'>
    <B id='n2' pre='2' post='3'>
    <C id='n3' pre='3' post='1'>clergywoman</C>
    <D id='n4' pre='4' post='2'>decadent</D></B>
    <E id='n5' pre='5' post='22'>
    <F id='n6' pre='6' post='6'>
    <G id='n7' pre='7' post='4'>gentility</G>
    <H id='n8' pre='8' post='5' idrefs='n17 n26'>happy-go-lucky man</H></F>
    <I id='n9' pre='9' post='9'>
    <J id='n10' pre='10' post='7'>jigsaw</J>
    <K id='n11' pre='11' post='8'>kerchief</K></I>
    <L id='n12' pre='12' post='15'>
    <!--L is the twelve-th letter of the English alphabet-->
    The letter L is followed by the letter:
    <M id='n13' pre='13' post='10'/>which is followed by the letter:<N id='n14' pre='14' post='13'>
    <O id='n15' pre='15' post='11'>ovenware</O>
    <P id='n16' pre='16' post='12'>plentiful</P></N>
    <?myPI value='XPath is nice'?>
    <Q id='n17' pre='17' post='14' idrefs='n8 n26'> quarrelsome</Q></L>
    <R id='n18' pre='18' post='18'>
    <S id='n19' pre='19' post='16'>sage</S>
    <T id='n20' pre='20' post='17'>tattered</T></R>
    <U id='n21' pre='21' post='21'>
    <V id='n22' pre='22' post='19'>voluptuary</V>
    <W id='n23' pre='23' post='20'>wriggle</W></U></E>
    <X id='n24' pre='24' post='25'>
    <Y id='n25' pre='25' post='23'>yawn</Y>
    <Z id='n26' pre='26' post='24' idrefs='n8 n17' xml:lang='it'>zuzzurellone</Z></X></A>""";

  /** Creates the test database. */
  @BeforeAll public static void beforeClass() {
    set(MainOptions.STRIPWS, true);
    execute(new CreateDB(NAME, DOC));
  }

  /** Drops the test database. */
  @AfterAll public static void afterClass() {
    execute(new DropDB(NAME));
    set(MainOptions.STRIPWS, false);
  }

  /** Axes. */
  @Test public void axes() {
    pre("//L/*", 59, 64, 79);
    pre("//L/parent::*", 20);
    pre("//L/descendant::*", 59, 64, 68, 73, 79);
    pre("//L/descendant-or-self::*", 53, 59, 64, 68, 73, 79);
    pre("//L/ancestor::*", 1, 20);
    pre("//L/ancestor-or-self::*", 1, 20, 53);
    pre("//L/following-sibling::*", 85, 99);
    pre("//L/preceding-sibling::*", 24, 39);
    pre("//L/following::*", 85, 89, 94, 99, 103, 108, 113, 117, 122);
    pre("//L/preceding::*", 6, 10, 15, 24, 28, 33, 39, 43, 48);
    pre("//L/self::*", 53);
    query("//L/@id/string()", "n12");
  }

  /** Functions. */
  @Test public void functions() {
    pre("//*[contains(., 'plentiful')]", 1, 20, 53, 64, 73);
    pre("//*[starts-with(., 'plentiful')]", 73);
    pre("//*[substring(., 1, 9) = 'plentiful']", 73);
    pre("//*[substring-after(., 'oven') = 'ware']", 68);
    pre("//*[substring-before(., 'ful') = 'plenti']", 73);
    pre("//*[string-length(translate(normalize-space(.), ' ', '')) > 100]", 1, 20);
    pre("//*[concat(., ..) = ..]", 59);
    pre("//*[ceiling(@pre div @post) = 1]", 1, 6, 20, 24, 39, 53, 85, 99, 113);
    pre("//*[floor(@pre div @post) = 0]", 1, 6, 20, 53, 113);
    pre("//*[round(@pre div @post) = 0]", 1, 20);
    pre("//*[name(.) = 'X']", 113);
    pre("//*[lang('it')]", 122);
    pre("//L/child::*[last()]", 79);
    pre("//L/descendant::*[4]", 73);
    pre("//L/ancestor::*[2]", 1);
    pre("//L/following-sibling::*[1]", 85);
    pre("//L/preceding-sibling::*[1]", 39);
    pre("//L/following::*[7]", 113);
    pre("//L/preceding::*[7]", 15);
    pre("//*[count(ancestor::*) > 3]", 68, 73);
    pre("//*[sum(ancestor::*/@pre) < sum(descendant::*/@pre)]",
        1, 6, 20, 24, 39, 53, 64, 85, 99, 113);
    pre("id('n1 n26')", 1, 122);
    pre("id(id(//*[.='happy-go-lucky man']/@idrefs)/@idrefs)", 33, 79, 122);
    pre("//*[number(@pre) < number(@post)]", 1, 6, 20, 53, 113);
    pre("//*[string(@pre - 1) = '0']", 1);
    pre("//*[boolean(@id) = true() and boolean(@idrefs) = false()]",
        1, 6, 10, 15, 20, 24, 28, 39, 43, 48, 53, 59, 64, 68, 73, 85, 89, 94, 99, 103,
        108, 113, 117);
  }

  /** Operators. */
  @Test public void operators() {
    pre("//*[child::* and preceding::Q]", 85, 99, 113);
    pre("//*[not(child::*) and preceding::Q]", 89, 94, 103, 108, 117, 122);
    pre("//*[preceding::L or following::L]",
        6, 10, 15, 24, 28, 33, 39, 43, 48, 85, 89, 94, 99, 103, 108, 113, 117, 122);
    pre("//L/ancestor::* | //L/descendant::*", 1, 20, 59, 64, 68, 73, 79);
    pre("//*[.='happy-go-lucky man']", 33);
    pre("//*[@pre > 12 and @post < 15]", 59, 64, 68, 73, 79);
    pre("//*[@pre != @post]",
        1, 6, 10, 15, 20, 28, 33, 43, 48, 53, 59, 64, 68, 73, 79, 89, 94, 103, 108, 113,
        117, 122);
    pre("//*[((@post * @post + @pre * @pre) div (@post + @pre)) > "
        + "((@post - @pre) * (@post - @pre))]",
        6, 24, 39, 48, 53, 59, 64, 79, 85, 89, 94, 99, 103, 108, 113, 117, 122);
    pre("//*[@pre mod 2 = 0]", 6, 15, 24, 33, 43, 53, 64, 73, 85, 94, 103, 113, 122);
  }

  /** Predicates. */
  @Test public void predicates() {
    pre("//*[L]", 20);
    pre("//*[parent::L]", 59, 64, 79);
    pre("//*[descendant::L]", 1, 20);
    pre("//*[descendant-or-self::L]", 1, 20, 53);
    pre("//*[ancestor::L]", 59, 64, 68, 73, 79);
    pre("//*[ancestor-or-self::L]", 53, 59, 64, 68, 73, 79);
    pre("//*[following-sibling::L]", 24, 39);
    pre("//*[preceding-sibling::L]", 85, 99);
    pre("//*[following::L]", 6, 10, 15, 24, 28, 33, 39, 43, 48);
    pre("//*[preceding::L]", 85, 89, 94, 99, 103, 108, 113, 117, 122);
    pre("//*[self::L]", 53);
    pre("//*[@id]", 1, 6, 10, 15, 20, 24, 28, 33, 39, 43, 48, 53, 59, 64, 68, 73, 79, 85, 89, 94,
        99, 103, 108, 113, 117, 122);
  }

  /** Node tests. */
  @Test public void nodeTests() {
    pre("//L/text()", 58, 63);
    pre("//L/comment()", 57);
    pre("//L/processing-instruction()", 78);
    pre("//L/processing-instruction('myPI')", 78);
    pre("//L/node()", 57, 58, 59, 63, 64, 78, 79);
    pre("//L/N", 64);
    pre("//L/*", 59, 64, 79);
  }
}
