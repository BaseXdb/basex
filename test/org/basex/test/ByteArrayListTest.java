package org.basex.test;

import static org.junit.Assert.*;

import org.basex.util.ByteArrayList;
import org.basex.util.Token;
import org.junit.Test;


/**
 * ByteArrayList Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz
 */
public class ByteArrayListTest {

  /**
   * Test of Add & Remove. 
   */
  @Test
  public void testAddRemove() {

    byte[][] list = new byte[4][];
    list[0] = Token.token("a");
    list[1] = Token.token("b");
    list[2] = Token.token("c");
    list[3] = Token.token("d");

    ByteArrayList l = new ByteArrayList();

    for(byte[] element : list)
      l.add(element);

    for(byte[] element : list)
      assertEquals("Test Elements", element, l.remove(0));    
  }

  /**
   * Test of Add & Remove. 
   */
  @Test
  public void testAddFinish() {

    byte[][] list = new byte[4][];
    list[0] = Token.token("a");
    list[1] = Token.token("b");
    list[2] = Token.token("c");
    list[3] = Token.token("d");

    ByteArrayList l = new ByteArrayList();

    for(byte[] element : list)
      l.add(element);

    byte[][] lFinish = l.finish();

    for(int i = 0; i < list.length; ++i)
      assertEquals("Test Elements", list[i], lFinish[i]);    
  }

  /**
   * Test of Sort. 
   */
  @Test
  public void testSortDesc() {

    byte[][] sort = new byte[6][];
    sort[0] = Token.token("a");
    sort[1] = Token.token("aa");
    sort[2] = Token.token("bffff");
    sort[3] = Token.token("ca");
    sort[4] = Token.token("d");
    sort[5] = Token.token("daaa");

    byte[][] unsort = new byte[6][];
    unsort[0] = Token.token("daaa");
    unsort[1] = Token.token("aa");
    unsort[2] = Token.token("bffff");
    unsort[3] = Token.token("d");
    unsort[4] = Token.token("a");
    unsort[5] = Token.token("ca");

    ByteArrayList l = new ByteArrayList();

    for(byte[] element : unsort)
      l.add(element);

    l.sort(false, false);
    byte[][] lFinish = l.finish();

    for(int i = 0; i < sort.length; ++i)       
      assertEquals("Test Elements", Token.string(sort[i]),
          Token.string(lFinish[i]));    
  }

  /**
   * Test of Sort . 
   */
  @Test
  public void testSortAnyNumElem() {

    int numEl = 100;
    boolean ins = false;
    byte[][] sort = new byte[numEl][];
    byte[][] unsort = new byte[numEl][];

    for(int i = 0; i < sort.length; i++) { 
      String s = "" + (char) (97 + (i % 26));

      for(int j = 0; j < i; ++j) {
        if(sort[j] != null) { 
          if(Token.string(sort[j]).compareTo(s) >= 0) {            
            System.arraycopy(sort, j, sort, j + 1, sort.length - j - 1);
            sort[j] = Token.token(s);
            ins = true;
            break;
          } 
        }
      }
      if(!ins) {    
        sort[i] = Token.token(s);
        ins = false;
      }
      unsort[unsort.length - 1 - i] = Token.token(s);      
    }

    ByteArrayList l = new ByteArrayList();

    for(byte[] element : unsort)
      l.add(element);

    l.sort(false, false);
    byte[][] lFinish = l.finish();

    for(int i = 0; i < sort.length; ++i) {        
      assertEquals("Test Elements", Token.string(sort[i]),
          Token.string(lFinish[i]));    

    }
  }
}
