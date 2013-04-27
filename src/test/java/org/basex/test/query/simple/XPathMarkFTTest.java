package org.basex.test.query.simple;

import org.basex.test.query.*;

/**
 * XPathMark functional tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XPathMarkFTTest extends QueryTest {
  /** Test document. */
  public static final String DOC =
      "<?xml version='1.0' encoding='UTF-8'?>" +
      //"<!DOCTYPE A SYSTEM 'alphabet.dtd'>" +
      "<A id='n1' pre='1' post='26' xml:lang='en'>" +
      "<B id='n2' pre='2' post='3'>" +
      "<C id='n3' pre='3' post='1'>clergywoman</C>" +
      "<D id='n4' pre='4' post='2'>decadent</D></B>" +
      "<E id='n5' pre='5' post='22'>" +
      "<F id='n6' pre='6' post='6'>" +
      "<G id='n7' pre='7' post='4'>gentility</G>" +
      "<H id='n8' pre='8' post='5' idrefs='n17 n26'>" +
      "happy-go-lucky man</H></F>" +
      "<I id='n9' pre='9' post='9'>" +
      "<J id='n10' pre='10' post='7'>jigsaw</J>" +
      "<K id='n11' pre='11' post='8'>kerchief</K></I>" +
      "<L id='n12' pre='12' post='15'>" +
      "<!--L is the twelve-th letter of the English alphabet-->" +
      "The letter L is followed by the letter:" +
      "<M id='n13' pre='13' post='10'/>" +
      "which is followed by the letter:<N id='n14' pre='14' post='13'>" +
      "<O id='n15' pre='15' post='11'>ovenware</O>" +
      "<P id='n16' pre='16' post='12'>plentiful</P></N>" +
      "<?myPI value='XPath is nice'?>" +
      "<Q id='n17' pre='17' post='14' idrefs='n8 n26'>" +
      "quarrelsome</Q></L>" +
      "<R id='n18' pre='18' post='18'>" +
      "<S id='n19' pre='19' post='16'>sage</S>" +
      "<T id='n20' pre='20' post='17'>tattered</T></R>" +
      "<U id='n21' pre='21' post='21'>" +
      "<V id='n22' pre='22' post='19'>voluptuary</V>" +
      "<W id='n23' pre='23' post='20'>wriggle</W></U></E>" +
      "<X id='n24' pre='24' post='25'>" +
      "<Y id='n25' pre='25' post='23'>yawn</Y>" +
      "<Z id='n26' pre='26' post='24' idrefs='n8 n17'" +
      " xml:lang='it'>zuzzurellone</Z></X></A>";

  /** Test queries. */
  public static final Object[][] QUERIES = new Object[][] {
      { "A01", node(59, 64, 79), "//L/*" },
      { "A02", node(20), "//L/parent::*" },
      { "A03", node(59, 64, 68, 73, 79), "//L/descendant::*" },
      { "A04", node(53, 59, 64, 68, 73, 79), "//L/descendant-or-self::*" },
      { "A05", node(1, 20), "//L/ancestor::*" },
      { "A06", node(1, 20, 53), "//L/ancestor-or-self::*" },
      { "A07", node(85, 99), "//L/following-sibling::*" },
      { "A08", node(24, 39), "//L/preceding-sibling::*" },
      { "A09", node(85, 89, 94, 99, 103, 108, 113, 117, 122),
        "//L/following::*" },
      { "A10", node(6, 10, 15, 24, 28, 33, 39, 43, 48), "//L/preceding::*" },
      { "A11", node(53), "//L/self::*" },
      { "A12", str("n12"), "//L/@id/string()" },
      { "F01", node(1, 20, 53, 64, 73), "//*[contains(., 'plentiful')]" },
      { "F02", node(73), "//*[starts-with(., 'plentiful')]" },
      { "F03", node(73), "//*[substring(., 1, 9) = 'plentiful']" },
      { "F04", node(68), "//*[substring-after(., 'oven') = 'ware']" },
      { "F05", node(73), "//*[substring-before(., 'ful') = 'plenti']" },
      { "F06", node(1, 20), "//*[string-length(translate(" +
        "normalize-space(.), ' ', '')) > 100]" },
      { "F07", node(59), "//*[concat(., ..) = ..]" },
      { "F08", node(1, 6, 20, 24, 39, 53, 85, 99, 113),
        "//*[ceiling(@pre div @post) = 1]" },
      { "F09", node(1, 6, 20, 53, 113), "//*[floor(@pre div @post) = 0]" },
      { "F10", node(1, 20), "//*[round(@pre div @post) = 0]" },
      { "F11", node(113), "//*[name(.) = 'X']" },
      { "F12", node(122), "//*[lang('it')]" },
      { "F13", node(79), "//L/child::*[last()]" },
      { "F14", node(73), "//L/descendant::*[4]" },
      { "F15", node(1), "//L/ancestor::*[2]" },
      { "F16", node(85), "//L/following-sibling::*[1]" },
      { "F17", node(39), "//L/preceding-sibling::*[1]" },
      { "F18", node(113), "//L/following::*[7]" },
      { "F19", node(15), "//L/preceding::*[7]" },
      { "F20", node(68, 73), "//*[count(ancestor::*) > 3]" },
      { "F21", node(1, 6, 20, 24, 39, 53, 64, 85, 99, 113),
        "//*[sum(ancestor::*/@pre) < sum(descendant::*/@pre)]" },
      { "F22", node(1, 122), "id('n1 n26')" },
      { "F23", node(33, 79, 122),
        "id(id(//*[.='happy-go-lucky man']/@idrefs)/@idrefs)" },
      { "F24", node(1, 6, 20, 53, 113), "//*[number(@pre) < number(@post)]" },
      { "F25", node(1), "//*[string(@pre - 1) = '0']" },
      { "F26", node(1, 6, 10, 15, 20, 24, 28, 39, 43, 48, 53, 59, 64, 68,
          73, 85, 89, 94, 99, 103, 108, 113, 117),
          "//*[boolean(@id) = true() and boolean(@idrefs) = false()]" },
      { "O01", node(85, 99, 113), "//*[child::* and preceding::Q]" },
      { "O02", node(89, 94, 103, 108, 117, 122),
          "//*[not(child::*) and preceding::Q]" },
      { "O03", node(6, 10, 15, 24, 28, 33, 39, 43, 48, 85, 89, 94, 99,
          103, 108, 113, 117, 122), "//*[preceding::L or following::L]" },
      { "O04", node(1, 20, 59, 64, 68, 73, 79),
          "//L/ancestor::* | //L/descendant::*" },
      { "O05", node(33), "//*[.='happy-go-lucky man']" },
      { "O06", node(59, 64, 68, 73, 79), "//*[@pre > 12 and @post < 15]" },
      { "O07", node(1, 6, 10, 15, 20, 28, 33, 43, 48, 53, 59, 64, 68, 73,
          79, 89, 94, 103, 108, 113, 117, 122),
          "//*[@pre != @post]" },
      { "O08", node(6, 24, 39, 48, 53, 59, 64, 79, 85, 89, 94, 99, 103,
          108, 113, 117, 122), "//*[((@post * @post + @pre * @pre) div " +
          "(@post + @pre)) > ((@post - @pre) * (@post - @pre))]" },
      { "O09", node(6, 15, 24, 33, 43, 53, 64, 73, 85, 94, 103, 113, 122),
          "//*[@pre mod 2 = 0]" },
      { "P01", node(20), "//*[L]" },
      { "P02", node(59, 64, 79), "//*[parent::L]" },
      { "P03", node(1, 20), "//*[descendant::L]" },
      { "P04", node(1, 20, 53), "//*[descendant-or-self::L]" },
      { "P05", node(59, 64, 68, 73, 79), "//*[ancestor::L]" },
      { "P06", node(53, 59, 64, 68, 73, 79), "//*[ancestor-or-self::L]" },
      { "P07", node(24, 39), "//*[following-sibling::L]" },
      { "P08", node(85, 99), "//*[preceding-sibling::L]" },
      { "P09", node(6, 10, 15, 24, 28, 33, 39, 43, 48), "//*[following::L]" },
      { "P10", node(85, 89, 94, 99, 103, 108, 113, 117, 122),
        "//*[preceding::L]" },
      { "P11", node(53), "//*[self::L]" },
      { "P12", node(1, 6, 10, 15, 20, 24, 28, 33, 39, 43, 48, 53, 59, 64,
          68, 73, 79, 85, 89, 94, 99, 103, 108, 113, 117, 122),
          "//*[@id]" },
      { "T01", node(58, 63), "//L/text()" },
      { "T02", node(57), "//L/comment()" },
      { "T03", node(78), "//L/processing-instruction()" },
      { "T04", node(78), "//L/processing-instruction('myPI')" },
      { "T05", node(57, 58, 59, 63, 64, 78, 79), "//L/node()" },
      { "T06", node(64), "//L/N" },
      { "T07", node(59, 64, 79), "//L/*" }
    };

  /** Constructor. */
  static {
    doc = DOC;
    queries = QUERIES;
  }

  /* TABLE REPRESENTATION
  POS  PAR  TYPE  CONTENT
    0   -1  DOC   test.xml
    1    0  ELEM  A
    2    1  ATTR  id="n1"
    3    1  ATTR  pre="1"
    4    1  ATTR  post="26"
    5    1  ATTR  xml:lang="en"
    6    1  ELEM  B
    7    6  ATTR  id="n2"
    8    6  ATTR  pre="2"
    9    6  ATTR  post="3"
   10    6  ELEM  C
   11   10  ATTR  id="n3"
   12   10  ATTR  pre="3"
   13   10  ATTR  post="1"
   14   10  TEXT  clergywoman
   15    6  ELEM  D
   16   15  ATTR  id="n4"
   17   15  ATTR  pre="4"
   18   15  ATTR  post="2"
   19   15  TEXT  decadent
   20    1  ELEM  E
   21   20  ATTR  id="n5"
   22   20  ATTR  pre="5"
   23   20  ATTR  post="22"
   24   20  ELEM  F
   25   24  ATTR  id="n6"
   26   24  ATTR  pre="6"
   27   24  ATTR  post="6"
   28   24  ELEM  G
   29   28  ATTR  id="n7"
   30   28  ATTR  pre="7"
   31   28  ATTR  post="4"
   32   28  TEXT  gentility
   33   24  ELEM  H
   34   33  ATTR  id="n8"
   35   33  ATTR  pre="8"
   36   33  ATTR  post="5"
   37   33  ATTR  idrefs="n17 n26"
   38   33  TEXT  happy-go-lucky man
   39   20  ELEM  I
   40   39  ATTR  id="n9"
   41   39  ATTR  pre="9"
   42   39  ATTR  post="9"
   43   39  ELEM  J
   44   43  ATTR  id="n10"
   45   43  ATTR  pre="10"
   46   43  ATTR  post="7"
   47   43  TEXT  jigsaw
   48   39  ELEM  K
   49   48  ATTR  id="n11"
   50   48  ATTR  pre="11"
   51   48  ATTR  post="8"
   52   48  TEXT  kerchief
   53   20  ELEM  L
   54   53  ATTR  id="n12"
   55   53  ATTR  pre="12"
   56   53  ATTR  post="15"
   57   53  COMM  L is the twelve-th letter of the English alphabet
   58   53  TEXT  The letter L is followed by the letter:
   59   53  ELEM  M
   60   59  ATTR  id="n13"
   61   59  ATTR  pre="13"
   62   59  ATTR  post="10"
   63   53  TEXT  which is followed by the letter:
   64   53  ELEM  N
   65   64  ATTR  id="n14"
   66   64  ATTR  pre="14"
   67   64  ATTR  post="13"
   68   64  ELEM  O
   69   68  ATTR  id="n15"
   70   68  ATTR  pre="15"
   71   68  ATTR  post="11"
   72   68  TEXT  ovenware
   73   64  ELEM  P
   74   73  ATTR  id="n16"
   75   73  ATTR  pre="16"
   76   73  ATTR  post="12"
   77   73  TEXT  plentiful
   78   53  PI    myPI value="XPath is nice"
   79   53  ELEM  Q
   80   79  ATTR  id="n17"
   81   79  ATTR  pre="17"
   82   79  ATTR  post="14"
   83   79  ATTR  idrefs="n8 n26"
   84   79  TEXT  quarrelsome
   85   20  ELEM  R
   86   85  ATTR  id="n18"
   87   85  ATTR  pre="18"
   88   85  ATTR  post="18"
   89   85  ELEM  S
   90   89  ATTR  id="n19"
   91   89  ATTR  pre="19"
   92   89  ATTR  post="16"
   93   89  TEXT  sage
   94   85  ELEM  T
   95   94  ATTR  id="n20"
   96   94  ATTR  pre="20"
   97   94  ATTR  post="17"
   98   94  TEXT  tattered
   99   20  ELEM  U
  100   99  ATTR  id="n21"
  101   99  ATTR  pre="21"
  102   99  ATTR  post="21"
  103   99  ELEM  V
  104  103  ATTR  id="n22"
  105  103  ATTR  pre="22"
  106  103  ATTR  post="19"
  107  103  TEXT  voluptuary
  108   99  ELEM  W
  109  108  ATTR  id="n23"
  110  108  ATTR  pre="23"
  111  108  ATTR  post="20"
  112  108  TEXT  wriggle
  113    1  ELEM  X
  114  113  ATTR  id="n24"
  115  113  ATTR  pre="24"
  116  113  ATTR  post="25"
  117  113  ELEM  Y
  118  117  ATTR  id="n25"
  119  117  ATTR  pre="25"
  120  117  ATTR  post="23"
  121  117  TEXT  yawn
  122  113  ELEM  Z
  123  122  ATTR  id="n26"
  124  122  ATTR  pre="26"
  125  122  ATTR  post="24"
  126  122  ATTR  idrefs="n8 n17"
  127  122  ATTR  xml:lang="it"
  128  122  TEXT  zuzzurellone
  */
}
