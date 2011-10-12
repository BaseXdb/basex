/*
 * This example shows how new documents can be added.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-11, BSD License
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class AddExample
  {
    public static void Main(string[] args)
    {
      try
      {
        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");

        // create empty database
        session.Execute("create db database");
        Console.WriteLine(session.Info);
        
        // define InputStream
        MemoryStream ms = new MemoryStream(
          System.Text.Encoding.UTF8.GetBytes("<xml>Hello World!</xml>"));
          
        // add document
        session.Add("world/world.xml", ms);
        Console.WriteLine(session.Info);
        
        // define InputStream
        MemoryStream ms = new MemoryStream(
          System.Text.Encoding.UTF8.GetBytes("<xml>Hello Universe!</xml>"));
          
        // add document
        session.Add("Universe.xml", ms);
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
