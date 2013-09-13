/*
 * This example shows how new databases can be created.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class CreateExample
  {
    public static void Main(string[] args)
    {
      try
      {
        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");

        // define InputStream
        MemoryStream ms = new MemoryStream(
          System.Text.Encoding.UTF8.GetBytes("<xml>Hello World!</xml>"));

        // create database
        session.Create("database", ms);
        Console.WriteLine(session.Info);

        // run query on database
        Console.WriteLine(session.Execute("xquery /"));
        
        // drop database
        session.Execute("drop db database");

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
