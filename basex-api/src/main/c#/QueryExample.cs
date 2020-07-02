﻿/*
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
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

          // loop through all results
          while (query.More()) 
          {
            Console.WriteLine(query.Next());
          }

          // close query
          query.Close();
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
