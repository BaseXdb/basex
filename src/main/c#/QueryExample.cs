/*
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://basex.org/api
 *
 * (C) BaseX Team 2005-11, BSD License
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
      try
      {
        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");

        try
        {
          // create query instance
          string input = "for $i in 1 to 10 return <xml>Text { $i }</xml>";
          Query query = session.Query(input);

          // initialize query
          Console.WriteLine(query.Init());

          // loop through all results
          while (query.More()) 
          {
            Console.WriteLine(query.Next());
          }

          // close query
          Console.WriteLine(query.Close());
        }
        catch (IOException e)
        {
          // print exception
          Console.WriteLine(e.Message);
        }

        // close session
        session.Close();
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
}
