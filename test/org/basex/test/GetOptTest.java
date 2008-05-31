package org.basex.test;

import java.io.IOException;
import org.basex.util.GetOpts;
import org.junit.Test;
import static org.junit.Assert.*;



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
   * 
   * @throws IOException - If Input fails.
   */
  @Test
  public void testWithoutOption() throws IOException {

    /*
     *   % testopt
     *   aflag = 0, bflag = 0, cvalue = (null)
     *   Non-option argument arg1
     */
    GetOpts g = new GetOpts("testopt", "ahR");
    assertEquals("No Options entered - return -1", -1, g.getopt());
    assertEquals("Path of fs testopt", null, g.getPath());
    /*
     *   % testopt /music/IckeUndEr
     *   aflag = 0, bflag = 0, cvalue = (null)
     *   Non-option argument /music/IckeUndEr
     */
    g = new GetOpts("testopt /music/IckeUndEr", "ahR");
    assertEquals("No Options entered - return -1", -1, g.getopt());
    assertEquals("Path of fs testopt /music/IckeUndEr", 
        "/music/IckeUndEr", g.getPath());
  }
  /**
   * Test of GetOpt.
   * 
   * Testcase:
   *  "fs ls -a";
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption1() throws IOException {
    String command = "ls -a";
    int[] enteredOptions =  {'a'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "ahRli");

    for(int i = 0; i < enteredOptions.length; i++) {     
      assertEquals(command, enteredOptions[i], getOptResult[i]);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   * 
   * Testcase:
   *  % fs testopt -a -b
   *  aflag = 1, bflag = 1, cvalue = (null)
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption2() throws IOException {
    String command = "testopt -a -b";
    int[] enteredOptions =  {'a', 'b'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {          
      assertEquals(command, enteredOptions[i], getOptResult[i]);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   * 
   * Testcase:
   *  % fs testopt -ab
   *  aflag = 1, bflag = 1, cvalue = (null)
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption3() throws IOException {
    String command = "testopt -ab";
    int[] enteredOptions =  {'a', 'b'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {          
      assertEquals(command, enteredOptions[i], getOptResult[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   * 
   * Testcase:
   *  % fs testopt -a -X
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption4() throws IOException {
    String command = "testopt -a -X";
    int[] enteredOptions =  {'a', '?'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "ahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {          
      assertEquals(command, enteredOptions[i], getOptResult[i]);
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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption5() throws IOException {
    String command = "testopt -ab -c";
    int[] enteredOptions =  {'a', 'b' , 'c'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "ahRblci");

    for(int i = 0; i < enteredOptions.length; i++) {          
      assertEquals(command, enteredOptions[i], getOptResult[i]);
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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOption6() throws IOException {
    String command = "testopt -YXb";
    int[] enteredOptions =  {'Y', '?', 'b'};    
    int[] getOptResult = optionHelp(command, enteredOptions.length, "YahRbli");

    for(int i = 0; i < enteredOptions.length; i++) {          
      assertEquals(command, enteredOptions[i], getOptResult[i]);
      assertEquals("Argument of " + command, null, returnedArgument);
    }
    assertEquals("Path of " + command, null, returnedPath);
  }

  /**
   * Test of GetOpt.
   * 
   * Testcase:
   *  % fs testopt -d ickeUndEr
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument1() throws IOException {
    String command = "testopt -d ickeUndEr -a";
    int[] enteredOptions =  {'d' , 'a'};    
    String args = "ahRbd:li";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument2() throws IOException {
    String command = "testopt -dickeUndEr -R";
    int[] enteredOptions =  {'d', 'R'};    
    String args = "ahRbd:li";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument3() throws IOException {
    String command = "testopt -d ickeUndEr -X";
    int[] enteredOptions =  {'d', '?'};    
    String args = "ahRbd:li";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument4() throws IOException {
    String command = "test -d icke -u";
    int[] enteredOptions =  {'d', 'u'};   
    String args = "ahRbd:lu";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument5() throws IOException {
    String command = "testopt -d ickeUndEr -iTest";
    int[] enteredOptions =  {'d', 'i'}; 
    String args = "ahRbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument6() throws IOException {
    String command = "testopt -d ickeUndEr -R -iTest -h";
    int[] enteredOptions =  {'d', 'R', 'i', 'h'}; 
    String args = "ahRbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument7() throws IOException {
    String command = "testopt -d ickeUndEr -R -iTest -h";
    int[] enteredOptions =  {'d', 'R', 'i', 'h'}; 
    String args = "ah:Rbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testOptionArgument8() throws IOException {
    String command = "testopt -d ickeUndEr -R -iTest -h";
    int[] enteredOptions =  {'d', 'R', 'h'}; 
    String args = "ahR:bd:li:";
    GetOpts g = new GetOpts(command, args);

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
          if(ch == 'R') {   
//            assertEquals("Argument of " + command, ":", g.getOptarg());
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
   *  % fs testopt -d ickeUndEr -R -iTest /Itunes/music/
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testAll1() throws IOException {
    String command = "testopt -d ickeUndEr -R -iTest /Itunes/music/";
    int[] enteredOptions =  {'d', 'R', 'i'};    
    String args = "ahRbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testAll2() throws IOException {
    String command = "testopt /Itunes/music/ -d ickeUndEr -R -iTest ";
    int[] enteredOptions =  {'d', 'R', 'i'};    
    String args = "ahRbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * 
   * @throws IOException - Input/Output failure.
   */
  @Test
  public void testAll3() throws IOException {
    String command = "testopt -d ickeUndEr /Itunes/music/ -R -iTest -X";
    int[] enteredOptions =  {'d', 'R', 'i', '?'}; 
    String args = "ahRbd:li:";
    GetOpts g = new GetOpts(command, args);

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
   * Performs a getopt command and returns the arguments returned by getopt.
   * 
   * @param length  expected results.
   * @param args  arguments.
   * @param command  passed by the "command line".
   * @return getopt result array.
   * @throws IOException  - Input/Output failure.
   */
  public int[] optionHelp(final String command, final int length,
      final String args) throws IOException {

    int[] getOptResult = new int[length]; 
    int index;
    GetOpts g = new GetOpts(command, args);
    int ch = g.getopt();
    index = 0;
    while (ch != -1) {      
      getOptResult[index] = ch;
      ch = g.getopt();
      ++index;
    }
    returnedArgument = g.getOptarg();
    returnedPath = g.getPath();
    return getOptResult;
  }
}
