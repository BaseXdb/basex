package org.basex.api.xmldb;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.ConsoleOutput;
import java.io.IOException;

/**
 * Implementation of the ResourceIterator Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResourceIterator implements ResourceIterator {
  
  /** Result */
  Result result;
  /** Start value for iterator */
  int start = 0;
  
  /**
   * Standard constructor with result.
   * @param result Result
   */
  public BXResourceIterator(Result result) {
    this.result = result;
  }

  /**
   * @see org.xmldb.api.base.ResourceIterator#hasMoreResources()
   */
  public boolean hasMoreResources() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.xmldb.api.base.ResourceIterator#nextResource()
   */
  public Resource nextResource() {
 // Prints the output to an output stream
    ConsoleOutput console = new ConsoleOutput(System.out);
    try{
      result.serialize(new XMLSerializer(console));
      console.flush();
      return null;
    } catch (IOException io) {
      System.out.println(io);
    }
    return null;
  }

}
