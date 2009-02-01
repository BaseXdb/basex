package org.basex.test;

import static org.junit.Assert.*;

import org.basex.fs.FSParser;
import org.basex.util.StringList;
import org.junit.Test;

/**
 * GetOpt Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz
 */
public final class GetOptTest {
  /** OutputStream.*/
  private String returnedPath;
  /** OutputStream.*/
  private String returnedArgument;

  /**
   * Constructor.
   */
  public GetOptTest() { }

  /**
   * Test of GetOpt.
   *
   * Testcases:
   *    "fs ls"
   *    "fs ls /music/IckeUndEr"
   *    "fs ls media/../media"
   */
  @Test
  public void testWithoutOption() {
    /*
     *   % testopt
     *   aflag = 0, bflag = 0, cvalue = (null)
     *   Non-option argument arg1
     */
    FSParser g = new FSParser("", "ahR");
    assertEquals("No Options entered - return -1", -1, g.getopt());
    assertEquals("Path of fs testopt", null, g.getPath());
    /*
     *   % testopt /music/IckeUndEr
     *   aflag = 0, bflag = 0, cvalue = (null)
     *   Non-option argument /music/IckeUndEr
     */
    g = new FSParser("/music/IckeUndEr", "ahR");
    assertEquals("No Options entered - return -1", -1, g.getopt());
    assertEquals("Path of fs testopt /music/IckeUndEr",
        "/music/IckeUndEr", g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  "fs ls -a";
   */
  @Test
  public void testOption1() {
    final String command = "-a";
    final int[] enteredOptions =  {'a'};
    final int[] res = optionHelp(command, enteredOptions.length, "ahRli");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -a -b
   *  aflag = 1, bflag = 1, cvalue = (null)
   */
  @Test
  public void testOption2() {
    final String command = "-a -b";
    final int[] enteredOptions =  {'a', 'b'};
    final int[] res = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -ab
   *  aflag = 1, bflag = 1, cvalue = (null)
   */
  @Test
  public void testOption3() {
    final String command = "-ab";
    final int[] enteredOptions =  {'a', 'b'};
    final int[] res = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -a -X
   */
  @Test
  public void testOption4() {
    final String command = "-a -X";
    final int[] enteredOptions =  {'a', 0 };
    final int[] res = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }


  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -ab
   *  aflag = 1, bflag = 1, cvalue = (null)
   */
  @Test
  public void testOption5() {
    final String command = "-ab -c";
    final int[] enteredOptions =  {'a', 'b' , 'c'};
    final int[] res = optionHelp(command, enteredOptions.length, "ahRblci");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -ab
   *  aflag = 1, bflag = 1, cvalue = (null)
   */
  @Test
  public void testOption6() {
    final String command = "-YXb";
    final int[] enteredOptions =  {'Y', 0, 'b'};
    final int[] res = optionHelp(command, enteredOptions.length, "YahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {
      assertEquals(command, enteredOptions[i], res[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr
   */
  @Test
  public void testOptionArgument1() {
    final String command = "-d ickeUndEr -a";
    final int[] enteredOptions =  {'d' , 'a'};
    final String args = "ahRbd:li";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals("Command " + command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        assertEquals("Argument of " + command, null, g.getOptarg());
      }
      ch = g.getopt();
      ++i;
    }
    assertEquals("Path of " + command, null, g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -dickeUndEr -R
   */
  @Test
  public void testOptionArgument2() {
    final String command = "-dickeUndEr -R";
    final int[] enteredOptions =  {'d', 'R'};
    final String args = "ahRbd:li";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals("Command " + command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        assertEquals("Argument of " + command, null, g.getOptarg());
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());
  }



  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr
   */
  @Test
  public void testOptionArgument3() {
    final String command = "-d ickeUndEr -X";
    final int[] enteredOptions =  {'d', 0};
    final String args = "ahRbd:li";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        assertEquals("Argument of " + command, null, g.getOptarg());
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());

  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr
   */
  @Test
  public void testOptionArgument4() {
    final String command = "-d icke -u";
    final int[] enteredOptions =  {'d', 'u'};
    final String args = "ahRbd:lu";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "icke", g.getOptarg());
      } else {
        assertEquals("Argument of " + command, null, g.getOptarg());
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());

  }



  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr -iTest
   */
  @Test
  public void testOptionArgument5() {
    final String command = "-d ickeUndEr -iTest";
    final int[] enteredOptions =  {'d', 'i'};
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          assertEquals("Argument of " + command, null, g.getOptarg());
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());

  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr -R -iTest
   */
  @Test
  public void testOptionArgument6() {
    final String command = "-d ickeUndEr -R -iTest -h";
    final int[] enteredOptions =  {'d', 'R', 'i', 'h'};
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          assertEquals("Argument of " + command, null, g.getOptarg());
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr -R -iTest
   */
  @Test
  public void testOptionArgument7() {
    final String command = "-d ickeUndEr -R -iTest -h";
    final int[] enteredOptions =  {'d', 'R', 'i', 'h'};
    final String args = "ah:Rbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          if(ch == 'h') {
            assertEquals("Argument of " + command, ":", g.getOptarg());
          } else {
            assertEquals("Argument of " + command, null, g.getOptarg());
          }
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % testopt -d ickeUndEr -R -iTest -h
   */
  @Test
  public void testOptionArgument8() {
    final String command = "-d ickeUndEr -R -iTest -h";
    final int[] enteredOptions =  {'d', 'R', 'h'};
    final String args = "ahR:bd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          if(ch != 'R') {
            assertEquals("Argument of " + command, null, g.getOptarg());
          }
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, null, g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt -d ickeUndEr -R -iTest /Itunes/music/
   */
  @Test
  public void testAll1() {
    final String command = "-d ickeUndEr -R -iTest /Itunes/music/";
    final int[] enteredOptions =  {'d', 'R', 'i'};
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          assertEquals("Argument of " + command, null, g.getOptarg());
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, "/Itunes/music/", g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt /Itunes/music/ -d ickeUndEr -R -iTest
   *  X is a nonvalid option
   */
  @Test
  public void testAll2() {
    final String command = "/Itunes/music/ -d ickeUndEr -R -iTest ";
    final int[] enteredOptions =  {'d', 'R', 'i'};
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          assertEquals("Argument of " + command, null, g.getOptarg());
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, "/Itunes/music/", g.getPath());
  }


  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt /Itunes/music/ -d ickeUndEr -R -iTest
   *  X is a nonvalid option
   */
  @Test
  public void testAll3() {
    final String command = "-d ickeUndEr /Itunes/music/ -R -iTest -X";
    final int[] enteredOptions =  {'d', 'R', 'i', 0};
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);

    int ch = g.getopt();
    int i = 0;

    while (ch != -1) {
      assertEquals(command, enteredOptions[i], ch);
      if(ch == 'd') {
        assertEquals("Argument of " + command, "ickeUndEr", g.getOptarg());
      } else {
        if(ch == 'i') {
          assertEquals("Argument of " + command, "Test", g.getOptarg());
        } else {
          assertEquals("Argument of " + command, null, g.getOptarg());
        }
      }
      ++i;
      ch = g.getopt();
    }
    assertEquals("Path of " + command, "/Itunes/music/", g.getPath());
  }

  /**
   * Test of GetOpt.
   *
   * Testcase:
   *  % fs testopt /Itunes/music/ -d ickeUndEr -R -iTest
   *  X is a nonvalid option
   */
  @Test
  public void testSourceAndTarget() {
    final String command = "ickeUndEr.txt icke.txt";
    final String args = "ahRbd:li:";
    final FSParser g = new FSParser(command, args);
    g.getopt();
    final StringList argsOfGetopt = g.getFoundArgs();
    if(argsOfGetopt.size == 2) {
      assertEquals("Path of " + command, "ickeUndEr.txt", argsOfGetopt.list[0]);
      assertEquals("Path of " + command, "icke.txt", argsOfGetopt.list[1]);
    }
  }

  /**
   * Performs a getopt command and returns the arguments returned by getopt.
   *
   * @param length  expected results.
   * @param args  arguments.
   * @param command  passed by the "command line".
   * @return getopt result array.
   */
  public int[] optionHelp(final String command, final int length,
      final String args) {

    final int[] res = new int[length];
    int index;
    final FSParser g = new FSParser(command, args);
    int ch = g.getopt();
    index = 0;
    while (ch != -1) {
      res[index] = ch;
      ch = g.getopt();
      ++index;
    }
    returnedArgument = g.getOptarg();
    returnedPath = g.getPath();
    return res;
  }
}
