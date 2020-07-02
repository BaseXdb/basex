' This example shows how new documents can be added.
'
' Documentation: https://docs.basex.org/wiki/Clients
'
' (C) BaseX Team 2005-12, BSD License

Imports System
Imports System.IO

Module CreateExample
  Sub Main()
    Try
      ' create session
      Dim session As New Session("localhost", 1984, "admin", "admin")
      
      ' create empty database
      session.Execute("create db database")
      Console.WriteLine(session.Info)
      
      ' define InputStream
      Dim ms As New MemoryStream(System.Text.Encoding.UTF8.GetBytes("<xml>Hello World!</xml>"))
      
      ' add document
      session.Add("world/World.xml", ms)
      Console.WriteLine(session.Info)
      
      ' define InputStream
      Dim ms As New MemoryStream(System.Text.Encoding.UTF8.GetBytes("<xml>Hello Universe!</xml>"))
      
      ' add document
      session.Add("Universe.xml", ms)
      Console.WriteLine(session.Info)

      ' run query on database
      Console.WriteLine(session.Execute("xquery /"))
      
      ' drop database
      session.Execute("drop db database")

      ' close session
      session.Close()
	Catch e As IOException
      ' print exception
      Console.WriteLine(e.Message)
    End Try
  End Sub
End Module