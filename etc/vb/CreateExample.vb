' This example shows how new databases can be created.
' Documentation: http://basex.org/api
'
' (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

Imports System
Imports System.IO

Module CreateExample
  Sub Main()
    Try

      ' create session
      Dim session As New Session("localhost", 1984, "admin", "admin")
      ' define InputStream
      Dim ms As New MemoryStream(System.Text.Encoding.UTF8.GetBytes("<xml>Hello World!</xml>"))
      ' create database
      session.Create("database", ms)
      Console.WriteLine(session.Info)

      ' run query on database
      Console.WriteLine(session.Execute("xquery /"))

      ' close session
      session.Close()
	Catch e As IOException
      ' print exception
      Console.WriteLine(e.Message)
    End Try
  End Sub
End Module