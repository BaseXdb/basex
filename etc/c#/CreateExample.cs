/*
 * This example shows how new databases can be created.
 * Documentation: http://basex.org/api
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
        MemoryStream ms = new MemoryStream(System.Text.Encoding.UTF8.GetBytes("<xml>Hello World!</xml>"));

        // create database
        session.Create("database", ms);
        Console.WriteLine(session.Info);
        
        // run query on database
        Console.WriteLine(session.Execute("xquery /"));

        // close session
        session.Close();
        Console.ReadLine();
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
}
