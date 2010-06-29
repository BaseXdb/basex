/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how results from a query can be received in an iterative
 * mode.
 * The execution time will be printed along with the result of the command.
 *
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class QueryIteratorExample
  {
    public static void Main(string[] args)
    {
      // initialize timer
      Stopwatch watch = new Stopwatch();
      watch.Start();
      
      // command to be performed
      string cmd = "1 to 10";
      
      try
      {
        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");
		
        try
        {
          // run query iterator
          Query query = session.query(cmd);
          while (query.more()) 
          {
          	Console.WriteLine(query.next());
          }
          query.close();
        }
        catch (IOException e)
        {
          // print exception
          Console.WriteLine(e.Message);
        }

        // close session
        session.Close();
        
        // print time needed
        Console.WriteLine("\n" + watch.ElapsedMilliseconds + " ms.");
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
}
