' This example shows how external variables can be bound to XQuery expressions.
'
' Documentation: http://docs.basex.org/wiki/Clients
'
' (C) BaseX Team 2005-12, BSD License

Imports System
Imports System.Diagnostics
Imports System.IO

Module QueryExample
  Sub Main()
    Try
      ' initialize timer
      Dim watch As New Stopwatch()
      watch.Start()

      ' create session
      Dim session As New Session("localhost", 1984, "admin", "admin")

      Try
        ' create query instance
        Dim input As String = "declare variable $name external; for $i in 1 to 10 return element { $name } { $i }"
        Dim query As Query = session.Query(input)

        ' bind variable
        query.Bind("$name", "number")

        ' loop through all results
        Console.WriteLine(query.Execute())

        ' close query instance
        query.Close()

      Catch e As IOException
        ' print exception
        Console.WriteLine(e.Message)
      End Try

      ' close session
      session.Close()

      ' print time needed
      Console.WriteLine(vbLf & watch.ElapsedMilliseconds & " ms.")
      Console.ReadLine()

    Catch e As IOException
      ' print exception
      Console.WriteLine(e.Message)
    End Try
  End Sub
End Module

