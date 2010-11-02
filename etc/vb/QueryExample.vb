' This example shows how queries can be executed in an iterative manner.
' Documentation: http://basex.org/api
'
' (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License

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
        Dim input As String = "for $i in 1 to 10 return <xml>Text { $i }</xml>"
        Dim query As Query = session.Query(input)
        
        ' initialize query
		Console.WriteLine(query.Init())
		
        ' loop through all results
        While query.More()
          Console.WriteLine(query.Nexty())
        End While

        ' close query instance
        Console.WriteLine(query.Close())

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

