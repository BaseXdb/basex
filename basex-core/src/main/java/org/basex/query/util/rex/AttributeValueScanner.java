// This file was generated on Wed Mar 25, 2026 16:33 (UTC+01) by REx v6.1 which is Copyright (c) 1979-2025 by Gunther Rademacher <grd@gmx.net>
// REx command line: -java -a basex AttributeValueScanner.ebnf

                                                            // line 2 "AttributeValueScanner.ebnf"
                                                            package org.basex.query.util.rex;

                                                            /**
                                                             * Scanner for attribute values in direct element constructors.
                                                             *
                                                             * @author BaseX Team, BSD License
                                                             * @author Gunther Rademacher
                                                             */
                                                            @SuppressWarnings("all")
                                                            public class AttributeValueScanner
                                                            {
                                                              /**
                                                               * Creates a new parser for the given input.
                                                               * @param input the input to be parsed
                                                               */
                                                              public AttributeValueScanner(final int[] input) {
                                                                initialize(new CharSequence(input));
                                                              }

                                                              /**
                                                                * Returns the length of the attribute value starting at the given
                                                                * position, or -1 if there is no valid attribute value at that
                                                                * position.
                                                                * @param pos the position to check for an attribute value
                                                                */
                                                              public int length(final int pos) {
                                                                reset(0, pos, pos);
                                                                try {
                                                                  parse_DirAttributeValue();
                                                                  return e0 - pos;
                                                                } catch (final ParseException e) {
                                                                  return -1;
                                                                }
                                                              }
                                                            // line 40 "AttributeValueScanner.java"

  public static class ParseException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;
    private int begin, end, offending, expected, state;

    public ParseException(int b, int e, int s, int o, int x)
    {
      begin = b;
      end = e;
      state = s;
      offending = o;
      expected = x;
    }

    @Override
    public String getMessage()
    {
      return offending < 0
           ? "lexical analysis failed"
           : "syntax error";
    }

    public int getBegin() {return begin;}
    public int getEnd() {return end;}
    public int getState() {return state;}
    public int getOffending() {return offending;}
    public int getExpected() {return expected;}
    public boolean isAmbiguousInput() {return false;}
  }

  public void initialize(CharSequence source)
  {
    input = source;
    size = source.length();
    reset(0, 0, 0);
  }

  public CharSequence getInput()
  {
    return input;
  }

  public int getTokenOffset()
  {
    return b0;
  }

  public int getTokenEnd()
  {
    return e0;
  }

  public final void reset(int l, int b, int e)
  {
            b0 = b; e0 = b;
    l1 = l; b1 = b; e1 = e;
    end = e;
  }

  public void reset()
  {
    reset(0, 0, 0);
  }

  public static String getOffendingToken(ParseException e)
  {
    return e.getOffending() < 0 ? null : TOKEN[e.getOffending()];
  }

  public static String[] getExpectedTokenSet(ParseException e)
  {
    String[] expected;
    if (e.getExpected() >= 0)
    {
      expected = new String[]{TOKEN[e.getExpected()]};
    }
    else
    {
      expected = getTokenSet(- e.getState());
    }
    return expected;
  }

  public String getErrorMessage(ParseException e)
  {
    String message = e.getMessage();
    String[] tokenSet = getExpectedTokenSet(e);
    String found = getOffendingToken(e);
    int size = e.getEnd() - e.getBegin();
    message += (found == null ? "" : ", found " + found)
            + "\nwhile expecting "
            + (tokenSet.length == 1 ? tokenSet[0] : java.util.Arrays.toString(tokenSet))
            + "\n"
            + (size == 0 || found != null ? "" : "after successfully scanning " + size + " characters beginning ");
    String prefix = input.subSequence(0, e.getBegin()).toString();
    int line = prefix.replaceAll("[^\n]", "").length() + 1;
    int column = prefix.length() - prefix.lastIndexOf('\n');
    return message
         + "at line " + line + ", column " + column + ":\n..."
         + input.subSequence(e.getBegin(), Math.min(input.length(), e.getBegin() + 64))
         + "...";
  }

  public void parse_DirAttributeValue()
  {
    lookahead1(16);                 // '"' | "'"
    switch (l1)
    {
    case 16:                        // '"'
      consume(16);                  // '"'
      for (;;)
      {
        lookahead1(23);             // QuotAttrContentChar | '"' | '""' | '{' | '{{' | '}}'
        if (l1 == 16)               // '"'
        {
          break;
        }
        switch (l1)
        {
        case 17:                    // '""'
          parse_EscapeQuot();
          break;
        default:
          parse_QuotAttrValueContent();
        }
      }
      consume(16);                  // '"'
      break;
    default:
      consume(19);                  // "'"
      for (;;)
      {
        lookahead1(24);             // AposAttrContentChar | "'" | "''" | '{' | '{{' | '}}'
        if (l1 == 19)               // "'"
        {
          break;
        }
        switch (l1)
        {
        case 20:                    // "''"
          parse_EscapeApos();
          break;
        default:
          parse_AposAttrValueContent();
        }
      }
      consume(19);                  // "'"
    }
  }

  private void parse_EscapeQuot()
  {
    consume(17);                    // '""'
  }

  private void parse_EscapeApos()
  {
    consume(20);                    // "''"
  }

  private void parse_QuotAttrValueContent()
  {
    switch (l1)
    {
    case 7:                         // QuotAttrContentChar
      consume(7);                   // QuotAttrContentChar
      break;
    default:
      parse_CommonContent();
    }
  }

  private void parse_CommonContent()
  {
    switch (l1)
    {
    case 40:                        // '{{'
      consume(40);                  // '{{'
      break;
    case 42:                        // '}}'
      consume(42);                  // '}}'
      break;
    default:
      parse_EnclosedExpr();
    }
  }

  private void parse_AposAttrValueContent()
  {
    switch (l1)
    {
    case 8:                         // AposAttrContentChar
      consume(8);                   // AposAttrContentChar
      break;
    default:
      parse_CommonContent();
    }
  }

  private void parse_DirElemContent()
  {
    switch (l1)
    {
    case 27:                        // '<'
    case 28:                        // '<!--'
    case 31:                        // '<?'
      parse_DirectConstructor();
      break;
    case 29:                        // '<![CDATA['
      parse_CDataSection();
      break;
    case 9:                         // ElementContentChar
      consume(9);                   // ElementContentChar
      break;
    default:
      parse_CommonContent();
    }
  }

  private void parse_EnclosedExpr()
  {
    lookahead1(13);                 // '{'
    consume(39);                    // '{'
    for (;;)
    {
      lookahead1(26);               // AposStringLiteral | QuotStringLiteral | OtherEnclosedExprContent | '(#' | '(:' |
                                    // '<' | '<' | '<!--' | '<?' | '`' | '``[' | '{' | '}'
      if (l1 == 41)                 // '}'
      {
        break;
      }
      parse_EnclosedExprContent();
    }
    consume(41);                    // '}'
  }

  private void parse_EnclosedExprContent()
  {
    switch (l1)
    {
    case 22:                        // '(:'
      parse_Comment();
      break;
    case 2:                         // AposStringLiteral
    case 3:                         // QuotStringLiteral
      parse_StringLiteral();
      break;
    case 38:                        // '``['
      parse_StringConstructor();
      break;
    case 37:                        // '`'
      parse_StringTemplate();
      break;
    case 21:                        // '(#'
      parse_Pragma();
      break;
    case 39:                        // '{'
      parse_EnclosedExpr();
      break;
    case 26:                        // '<'
      consume(26);                  // '<'
      break;
    case 15:                        // OtherEnclosedExprContent
      consume(15);                  // OtherEnclosedExprContent
      break;
    default:
      parse_DirectConstructor();
    }
  }

  private void parse_Comment()
  {
    consume(22);                    // '(:'
    for (;;)
    {
      lookahead1(18);               // CommentContents | '(:' | ':)'
      if (l1 == 25)                 // ':)'
      {
        break;
      }
      switch (l1)
      {
      case 1:                       // CommentContents
        consume(1);                 // CommentContents
        break;
      default:
        parse_Comment();
      }
    }
    consume(25);                    // ':)'
  }

  private void parse_StringLiteral()
  {
    switch (l1)
    {
    case 2:                         // AposStringLiteral
      consume(2);                   // AposStringLiteral
      break;
    default:
      consume(3);                   // QuotStringLiteral
    }
  }

  private void parse_StringConstructor()
  {
    consume(38);                    // '``['
    parse_StringConstructorContent();
    consume(36);                    // ']``'
  }

  private void parse_StringConstructorContent()
  {
    lookahead1(0);                  // StringConstructorChars
    consume(5);                     // StringConstructorChars
    for (;;)
    {
      lookahead1(17);               // ']``' | '`'
      if (l1 != 37)                 // '`'
      {
        break;
      }
      parse_StringInterpolation();
      lookahead1(0);                // StringConstructorChars
      consume(5);                   // StringConstructorChars
    }
  }

  private void parse_StringInterpolation()
  {
    consume(37);                    // '`'
    parse_EnclosedExpr();
    lookahead1(12);                 // '`'
    consume(37);                    // '`'
  }

  private void parse_StringTemplate()
  {
    consume(37);                    // '`'
    for (;;)
    {
      lookahead1(19);               // StringTemplateFixedPart | '`' | '{'
      if (l1 == 37)                 // '`'
      {
        break;
      }
      switch (l1)
      {
      case 4:                       // StringTemplateFixedPart
        consume(4);                 // StringTemplateFixedPart
        break;
      default:
        parse_StringTemplateVariablePart();
      }
    }
    consume(37);                    // '`'
  }

  private void parse_StringTemplateVariablePart()
  {
    parse_EnclosedExpr();
  }

  private void parse_Pragma()
  {
    consume(21);                    // '(#'
    lookahead1(1);                  // PragmaContents
    consume(6);                     // PragmaContents
    lookahead1(6);                  // '#)'
    consume(18);                    // '#)'
  }

  private void parse_DirectConstructor()
  {
    switch (l1)
    {
    case 27:                        // '<'
      parse_DirElemConstructor();
      break;
    case 28:                        // '<!--'
      parse_DirCommentConstructor();
      break;
    default:
      parse_DirPIConstructor();
    }
  }

  private void parse_DirElemConstructor()
  {
    consume(27);                    // '<'
    lookahead1(5);                  // QName
    consume(13);                    // QName
    parse_DirAttributeList();
    switch (l1)
    {
    case 24:                        // '/>'
      consume(24);                  // '/>'
      break;
    default:
      consume(33);                  // '>'
      for (;;)
      {
        lookahead1(25);             // ElementContentChar | '<' | '<!--' | '<![CDATA[' | '</' | '<?' | '{' | '{{' | '}}'
        if (l1 == 30)               // '</'
        {
          break;
        }
        parse_DirElemContent();
      }
      consume(30);                  // '</'
      lookahead1(5);                // QName
      consume(13);                  // QName
      lookahead1(15);               // S | '>'
      if (l1 == 14)                 // S
      {
        consume(14);                // S
      }
      lookahead1(9);                // '>'
      consume(33);                  // '>'
    }
  }

  private void parse_DirAttributeList()
  {
    for (;;)
    {
      lookahead1(21);               // S | '/>' | '>'
      if (l1 != 14)                 // S
      {
        break;
      }
      consume(14);                  // S
      lookahead1(22);               // QName | S | '/>' | '>'
      if (l1 == 13)                 // QName
      {
        consume(13);                // QName
        lookahead1(14);             // S | '='
        if (l1 == 14)               // S
        {
          consume(14);              // S
        }
        lookahead1(8);              // '='
        consume(32);                // '='
        lookahead1(20);             // S | '"' | "'"
        if (l1 == 14)               // S
        {
          consume(14);              // S
        }
        parse_DirAttributeValue();
      }
    }
  }

  private void parse_CDataSection()
  {
    consume(29);                    // '<![CDATA['
    lookahead1(2);                  // CDataSectionContents
    consume(10);                    // CDataSectionContents
    lookahead1(11);                 // ']]>'
    consume(35);                    // ']]>'
  }

  private void parse_DirCommentConstructor()
  {
    consume(28);                    // '<!--'
    lookahead1(3);                  // DirCommentContents
    consume(11);                    // DirCommentContents
    lookahead1(7);                  // '-->'
    consume(23);                    // '-->'
  }

  private void parse_DirPIConstructor()
  {
    consume(31);                    // '<?'
    lookahead1(4);                  // DirPIContents
    consume(12);                    // DirPIContents
    lookahead1(10);                 // '?>'
    consume(34);                    // '?>'
  }

  private void consume(int t)
  {
    if (l1 == t)
    {
      b0 = b1; e0 = e1; l1 = 0;
    }
    else
    {
      error(b1, e1, 0, l1, t);
    }
  }

  private void lookahead1(int tokenSetId)
  {
    if (l1 == 0)
    {
      l1 = match(tokenSetId);
      b1 = begin;
      e1 = end;
    }
  }

  private int error(int b, int e, int s, int l, int t)
  {
    throw new ParseException(b, e, s, l, t);
  }

  private int     b0, e0;
  private int l1, b1, e1;
  private CharSequence input = null;
  private int size = 0;
  private int begin = 0;
  private int end = 0;

  private int match(int tokenSetId)
  {
    boolean nonbmp = false;
    begin = end;
    int current = end;
    int result = INITIAL[tokenSetId];
    int state = 0;

    for (int code = result & 127; code != 0; )
    {
      int charclass;
      int c0 = current < size ? input.charAt(current) : 0;
      ++current;
      if (c0 < 0x80)
      {
        charclass = MAP0[c0];
      }
      else if (c0 < 0xd800)
      {
        int c1 = c0 >> 4;
        charclass = MAP1[(c0 & 15) + MAP1[(c1 & 31) + MAP1[c1 >> 5]]];
      }
      else
      {
        if (c0 < 0xdc00)
        {
          int c1 = current < size ? input.charAt(current) : 0;
          if (c1 >= 0xdc00 && c1 < 0xe000)
          {
            nonbmp = true;
            ++current;
            c0 = ((c0 & 0x3ff) << 10) + (c1 & 0x3ff) + 0x10000;
          }
        }

        int lo = 0, hi = 5;
        for (int m = 3; ; m = (hi + lo) >> 1)
        {
          if (MAP2[m] > c0) {hi = m - 1;}
          else if (MAP2[6 + m] < c0) {lo = m + 1;}
          else {charclass = MAP2[12 + m]; break;}
          if (lo > hi) {charclass = 0; break;}
        }
      }

      state = code;
      int i0 = (charclass << 7) + code - 1;
      code = TRANSITION[(i0 & 7) + TRANSITION[i0 >> 3]];

      if (code > 127)
      {
        result = code;
        code &= 127;
        end = current;
      }
    }

    result >>= 7;
    if (result == 0)
    {
      end = current - 1;
      int c1 = end < size ? input.charAt(end) : 0;
      if (c1 >= 0xdc00 && c1 < 0xe000)
      {
        --end;
      }
      return error(begin, end, state, -1, -1);
    }
    else if ((result & 64) != 0)
    {
      end = begin;
      if (nonbmp)
      {
        for (int i = result >> 7; i > 0; --i)
        {
          int c1 = end < size ? input.charAt(end) : 0;
          ++end;
          if (c1 >= 0xd800 && c1 < 0xdc000)
          {
            ++end;
          }
        }
      }
      else
      {
        end += (result >> 7);
      }
    }
    else if (nonbmp)
    {
      for (int i = result >> 7; i > 0; --i)
      {
        --end;
        int c1 = end < size ? input.charAt(end) : 0;
        if (c1 >= 0xdc00 && c1 < 0xe000)
        {
          --end;
        }
      }
    }
    else
    {
      end -= result >> 7;
    }

    if (end > size) end = size;
    return (result & 63) - 1;
  }

  private static String[] getTokenSet(int tokenSetId)
  {
    java.util.ArrayList<String> expected = new java.util.ArrayList<>();
    int s = tokenSetId < 0 ? - tokenSetId : INITIAL[tokenSetId] & 127;
    for (int i = 0; i < 43; i += 32)
    {
      int j = i;
      int i0 = (i >> 5) * 93 + s - 1;
      int f = EXPECTED[(i0 & 3) + EXPECTED[i0 >> 2]];
      for ( ; f != 0; f >>>= 1, ++j)
      {
        if ((f & 1) != 0)
        {
          expected.add(TOKEN[j]);
        }
      }
    }
    return expected.toArray(new String[]{});
  }

  private static final int[] MAP0 =
  {
    /*   0 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 26, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 1,
    /*  34 */ 2, 3, 4, 4, 4, 5, 6, 7, 4, 4, 4, 8, 9, 10, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 11, 4, 12, 13, 14, 15, 4, 16, 17,
    /*  67 */ 18, 19, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 20, 17, 17, 17, 17, 17, 17, 21, 4, 22,
    /*  94 */ 4, 17, 23, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
    /* 121 */ 17, 17, 24, 4, 25, 4, 4
  };

  private static final int[] MAP1 =
  {
    /*   0 */ 108, 124, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 156, 181, 181, 181, 181,
    /*  21 */ 181, 214, 215, 213, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  42 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  63 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  84 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /* 105 */ 214, 214, 214, 254, 247, 270, 370, 286, 307, 347, 291, 408, 408, 408, 400, 348, 322, 348, 322, 348, 348,
    /* 126 */ 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 364, 364, 364, 364, 364, 364, 364,
    /* 147 */ 331, 348, 348, 348, 348, 348, 348, 348, 348, 386, 408, 408, 409, 407, 408, 408, 348, 348, 348, 348, 348,
    /* 168 */ 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 408, 408, 408, 408, 408, 408, 408, 408,
    /* 189 */ 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408, 408,
    /* 210 */ 408, 408, 408, 329, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348,
    /* 231 */ 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 348, 408, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /* 256 */ 0, 0, 0, 0, 0, 0, 0, 26, 26, 0, 0, 26, 0, 0, 26, 1, 2, 3, 4, 4, 4, 5, 6, 7, 4, 4, 4, 8, 9, 10, 4, 16, 17,
    /* 289 */ 18, 19, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 24, 4, 25, 4, 4, 17, 17, 17, 17, 20, 17, 17, 17, 17,
    /* 316 */ 17, 17, 21, 4, 22, 4, 17, 17, 17, 17, 17, 17, 17, 4, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
    /* 343 */ 17, 17, 4, 17, 23, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 9, 9, 9, 9, 9, 9, 9, 9,
    /* 372 */ 9, 9, 9, 9, 9, 9, 9, 9, 11, 4, 12, 13, 14, 15, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 17, 17, 4, 4, 4, 4, 4,
    /* 405 */ 4, 4, 9, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 9
  };

  private static final int[] MAP2 =
  {
    /*  0 */ 57344, 63744, 64976, 65008, 65536, 983040, 63743, 64975, 65007, 65533, 983039, 1114111, 4, 17, 4, 17, 17,
    /* 17 */ 4
  };

  private static final int[] INITIAL =
  {
    /*  0 */ 1, 2, 3, 1540, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27
  };

  private static final int[] TRANSITION =
  {
    /*    0 */ 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 441, 436, 441, 483, 432, 440,
    /*   21 */ 449, 636, 491, 1152, 509, 441, 441, 441, 441, 441, 441, 436, 441, 520, 528, 440, 449, 547, 555, 676, 569,
    /*   42 */ 441, 441, 441, 441, 441, 441, 584, 441, 483, 580, 440, 449, 572, 592, 1152, 606, 441, 441, 441, 441, 441,
    /*   63 */ 441, 436, 441, 483, 432, 440, 449, 701, 592, 1152, 509, 441, 441, 441, 441, 441, 441, 436, 441, 616, 624,
    /*   84 */ 440, 449, 1162, 555, 1152, 644, 441, 441, 441, 441, 441, 441, 436, 441, 561, 655, 440, 663, 720, 684,
    /*  104 */ 826, 698, 441, 441, 441, 441, 441, 441, 436, 441, 483, 709, 717, 728, 701, 592, 1134, 509, 441, 441, 441,
    /*  125 */ 441, 441, 441, 746, 441, 483, 754, 773, 449, 701, 592, 470, 785, 777, 735, 441, 441, 441, 441, 436, 441,
    /*  146 */ 483, 432, 536, 449, 701, 592, 1199, 793, 777, 735, 441, 441, 441, 441, 436, 441, 1008, 432, 440, 449,
    /*  166 */ 501, 592, 463, 509, 846, 441, 441, 441, 441, 441, 436, 441, 598, 432, 808, 819, 512, 592, 831, 606, 738,
    /*  187 */ 441, 441, 441, 441, 441, 436, 441, 909, 940, 440, 449, 647, 555, 1152, 569, 441, 441, 441, 441, 441, 441,
    /*  208 */ 436, 839, 483, 432, 440, 449, 701, 592, 1152, 509, 811, 848, 441, 441, 441, 441, 436, 856, 1022, 432,
    /*  228 */ 765, 864, 701, 872, 880, 509, 846, 441, 441, 441, 441, 441, 628, 497, 483, 895, 632, 449, 925, 903, 1152,
    /*  249 */ 921, 441, 441, 441, 441, 441, 441, 532, 441, 974, 432, 536, 449, 1138, 933, 1199, 793, 1112, 761, 441,
    /*  269 */ 441, 441, 441, 532, 441, 974, 432, 536, 449, 1138, 933, 1199, 793, 1061, 735, 441, 441, 441, 441, 532,
    /*  289 */ 441, 974, 432, 536, 449, 1138, 933, 1199, 793, 948, 735, 441, 441, 441, 441, 532, 441, 974, 432, 536,
    /*  309 */ 449, 1138, 933, 1199, 793, 1185, 735, 441, 441, 441, 441, 532, 441, 974, 432, 536, 449, 1138, 933, 1199,
    /*  329 */ 793, 1061, 956, 441, 441, 441, 441, 436, 441, 483, 432, 440, 449, 701, 592, 477, 968, 441, 913, 441, 441,
    /*  350 */ 441, 441, 982, 1157, 887, 994, 1002, 449, 701, 1016, 1152, 509, 441, 441, 441, 441, 441, 441, 1042, 960,
    /*  370 */ 1030, 1038, 986, 1054, 647, 1069, 1083, 569, 441, 441, 441, 441, 441, 441, 436, 539, 1089, 1097, 440,
    /*  389 */ 1105, 1120, 1128, 1152, 1146, 441, 441, 441, 441, 441, 441, 436, 441, 690, 1170, 440, 1178, 800, 555,
    /*  408 */ 1152, 569, 441, 441, 441, 441, 441, 441, 436, 608, 1075, 432, 1046, 449, 701, 592, 456, 509, 670, 1193,
    /*  428 */ 441, 441, 441, 441, 1152, 1280, 60, 1, 1, 2, 3, 1540, 5, 0, 0, 0, 0, 0, 0, 0, 0, 43, 43, 43, 0, 0, 0,
    /*  455 */ 687, 0, 0, 43, 19328, 43, 0, 83, 0, 0, 43, 19328, 43, 0, 28160, 0, 0, 43, 19328, 43, 80, 71, 0, 0, 43,
    /*  480 */ 19328, 43, 81, 0, 0, 43, 687, 0, 0, 0, 1024, 19916, 60, 0, 18492, 1, 3, 0, 0, 37, 0, 0, 0, 0, 0, 3968,
    /*  506 */ 54, 55, 18492, 0, 19200, 18492, 0, 0, 0, 0, 0, 54, 55, 68, 2176, 0, 43, 687, 2176, 0, 0, 2225, 1152,
    /*  529 */ 1280, 54, 1, 1, 2, 3, 1540, 5, 1826, 0, 0, 0, 0, 0, 0, 5120, 0, 0, 2304, 0, 0, 0, 0, 584, 55, 18432,
    /*  555 */ 19840, 0, 0, 18432, 1, 3, 0, 0, 41, 687, 0, 0, 0, 1024, 0, 19200, 18432, 0, 0, 0, 0, 0, 54, 55, 74, 1152,
    /*  581 */ 1280, 60, 1, 1, 17310, 3, 1540, 5, 0, 35, 0, 19840, 60, 0, 18492, 1, 3, 0, 0, 42, 687, 0, 0, 0, 1024, 0,
    /*  607 */ 19200, 0, 0, 0, 0, 0, 0, 1959, 1959, 2560, 0, 43, 687, 2560, 0, 0, 1024, 2612, 1280, 55, 1, 1, 2, 3,
    /*  631 */ 1540, 18081, 0, 0, 0, 0, 0, 0, 0, 70, 54, 55, 18492, 55, 19200, 18432, 0, 0, 0, 0, 0, 54, 55, 18432,
    /*  655 */ 1152, 1280, 56, 1, 1, 2, 3, 1540, 16707, 16707, 16707, 0, 0, 0, 687, 0, 0, 83, 0, 83, 89, 0, 0, 43,
    /*  679 */ 19328, 43, 0, 0, 54, 19840, 75, 0, 18507, 1, 3, 0, 0, 43, 46, 0, 0, 0, 51, 0, 19200, 18507, 0, 0, 0, 0,
    /*  705 */ 0, 54, 55, 18492, 1152, 1280, 60, 1, 1, 0, 3, 1540, 5, 0, 2432, 0, 0, 0, 0, 0, 54, 55, 18507, 43, 3328,
    /*  730 */ 43, 0, 0, 0, 687, 0, 0, 91, 0, 0, 0, 0, 0, 88, 0, 0, 1, 2, 3, 32, 5, 0, 0, 36, 1152, 1280, 60, 1, 1, 2,
    /*  760 */ 3, 0, 0, 91, 93, 0, 0, 0, 0, 4480, 0, 0, 0, 5, 1826, 0, 64, 0, 0, 0, 0, 85, 86, 0, 0, 0, 19200, 18492,
    /*  788 */ 80, 0, 0, 1871, 3712, 0, 19200, 18492, 0, 0, 0, 1871, 0, 0, 5504, 0, 0, 54, 55, 18432, 5, 63, 0, 0, 0, 0,
    /*  814 */ 0, 0, 28160, 0, 0, 68, 16709, 16709, 0, 0, 0, 687, 0, 0, 16707, 19328, 16707, 0, 0, 0, 19328, 16709, 0,
    /*  837 */ 82, 0, 4224, 0, 0, 0, 0, 0, 4224, 0, 0, 28160, 0, 28160, 0, 0, 0, 0, 0, 0, 4352, 0, 0, 0, 0, 0, 4352, 43,
    /*  865 */ 43, 43, 0, 0, 0, 687, 3200, 19840, 60, 0, 18492, 1, 0, 0, 3072, 4608, 0, 43, 19328, 43, 0, 28160, 0, 40,
    /*  889 */ 43, 687, 0, 0, 0, 1024, 1152, 1280, 58, 1, 1, 2, 3, 1540, 4096, 58, 0, 18490, 1, 3, 0, 0, 43, 687, 0, 0,
    /*  915 */ 0, 0, 3840, 0, 0, 0, 0, 19200, 18490, 0, 0, 0, 0, 0, 4096, 54, 55, 18490, 19911, 60, 0, 18492, 1, 3,
    /*  939 */ 1871, 0, 53, 57, 1, 1, 2, 3, 1540, 84, 85, 86, 0, 85, 86, 0, 91, 0, 92, 91, 0, 0, 0, 0, 0, 4864, 0, 0, 0,
    /*  968 */ 0, 19200, 18492, 0, 4992, 0, 0, 0, 43, 687, 0, 0, 1826, 1024, 28, 2, 31, 1540, 5, 0, 0, 0, 0, 0, 0, 66,
    /*  994 */ 1152, 1280, 60, 28, 28, 2, 34238, 1540, 5, 0, 0, 0, 0, 65, 0, 0, 43, 687, 0, 48, 48, 1024, 19840, 60, 0,
    /* 1019 */ 18492, 28, 34238, 0, 0, 43, 687, 0, 4352, 4352, 1024, 0, 4864, 43, 4908, 0, 0, 0, 1024, 1152, 1280, 4923,
    /* 1041 */ 33597, 29, 2, 3, 1540, 5, 0, 0, 0, 0, 0, 1959, 0, 43, 43, 43, 687, 0, 0, 44, 0, 85, 86, 0, 85, 86, 0, 91,
    /* 1069 */ 19840, 0, 77, 18432, 78, 3, 0, 0, 43, 687, 1959, 1959, 1959, 1024, 0, 4736, 43, 19328, 43, 0, 0, 0, 43,
    /* 1092 */ 5165, 0, 0, 0, 5170, 5170, 5170, 5120, 1, 33536, 2, 3, 1540, 43, 43, 43, 0, 687, 0, 45, 0, 85, 86, 0, 85,
    /* 1117 */ 86, 90, 91, 0, 5248, 0, 0, 0, 54, 55, 18432, 19840, 0, 0, 18432, 33536, 3, 0, 0, 43, 19328, 0, 0, 0, 0,
    /* 1142 */ 71, 54, 55, 18492, 0, 19200, 18432, 0, 0, 33536, 0, 0, 43, 19328, 43, 0, 0, 0, 38, 0, 0, 0, 0, 2688, 0,
    /* 1167 */ 54, 457, 18432, 51, 51, 5376, 1, 1, 2, 3, 1540, 43, 43, 43, 0, 0, 687, 46, 0, 85, 86, 87, 85, 86, 0, 91,
    /* 1193 */ 89, 0, 89, 0, 0, 0, 0, 0, 43, 19328, 43, 0, 71, 0
  };

  private static final int[] EXPECTED =
  {
    /*   0 */ 65, 47, 107, 121, 51, 58, 62, 65, 47, 118, 69, 54, 73, 77, 81, 85, 143, 89, 135, 96, 100, 101, 102, 106,
    /*  24 */ 107, 132, 92, 115, 112, 125, 107, 107, 129, 141, 107, 149, 107, 108, 107, 139, 107, 107, 147, 107, 107,
    /*  45 */ 107, 107, 4096, 8192, 262144, 8388608, 589824, 0, 37748738, 16, 16, 16, 16777216, 606208, 16793600,
    /*  60 */ 16801792, 196736, 1573120, -134217216, -1671397364, 32, 64, 1024, 2048, 4194306, 33554434, 2, 16, 131072,
    /*  74 */ 0, 0, 1048576, -134217728, 8, 4, 6324224, -1677721600, 32768, 0, 32768, 32, 1024, 8192, 8388608, 2,
    /*  90 */ 805306368, 134217728, 8, 32, 128, 1, 0, 32, 8192, 268435456, 536870912, 134217728, 134217728, 536870912,
    /* 104 */ 134217728, 536870912, 536870912, 0, 0, 0, 0, 64, 160, 0, 2, 2, 0, 48, 0, 0, 16384, 0, 0, 16384, 16384,
    /* 125 */ 1408, 1408, 1408, 736, 0, 4, 8, 0, 1, 2, 4, 2097152, 32768, 268435456, 0, 8, 16, 0, 0, 0, 2, 4194304, 0,
    /* 148 */ 64, 0, 0, 256, 1024
  };

  private static final String[] TOKEN =
  {
    "%ERROR",
    "CommentContents",
    "AposStringLiteral",
    "QuotStringLiteral",
    "StringTemplateFixedPart",
    "StringConstructorChars",
    "PragmaContents",
    "QuotAttrContentChar",
    "AposAttrContentChar",
    "ElementContentChar",
    "CDataSectionContents",
    "DirCommentContents",
    "DirPIContents",
    "QName",
    "S",
    "OtherEnclosedExprContent",
    "'\"'",
    "'\"\"'",
    "'#)'",
    "''''",
    "''''''",
    "'(#'",
    "'(:'",
    "'-->'",
    "'/>'",
    "':)'",
    "'<'",
    "'<'",
    "'<!--'",
    "'<![CDATA['",
    "'</'",
    "'<?'",
    "'='",
    "'>'",
    "'?>'",
    "']]>'",
    "']``'",
    "'`'",
    "'``['",
    "'{'",
    "'{{'",
    "'}'",
    "'}}'"
  };

                                                            // line 189 "AttributeValueScanner.ebnf"
                                                            private record CharSequence(int[] input) {
                                                                public int length() {
                                                                  return input.length;
                                                                }

                                                                public int charAt(int index) {
                                                                  return input[index];
                                                                }

                                                                public CharSequence subSequence(int start, int end) {
                                                                  throw new UnsupportedOperationException();
                                                                }
                                                              }

                                                              /* Testing code
                                                              public static void main(String[] args) {
                                                                final String input = args[0];
                                                                System.out.println(input);
                                                                final AttributeValueScanner parser
                                                                  = new AttributeValueScanner(input.codePoints().toArray());
                                                                for(int i = 0; i < input.length(); i++) {
                                                                  final int size = parser.size(i);
                                                                  if (size > 0) {
                                                                    System.out.println(i + " to " + (i + size) + ": " + input.substring(i, i + size));
                                                                  }
                                                                }
                                                              }
                                                              */
                                                            }
                                                            // line 873 "AttributeValueScanner.java"
// End
