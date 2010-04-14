/*
 * -----------------------------------------------------------------------------
 * 
 * This C# module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the Execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result is stored in the Result property,
 * and the Info property returns additional processing information or error
 * output.
 *
 * An even faster approach is to call Execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
 * 
 * -----------------------------------------------------------------------------
 * Example:
 * 
 * using System;
 * 
 * namespace BaseXClient
 *	{
 *	public class Example
 *	{
 *		public static void Main(string[] args)
 *		{
 *			try
 *      {
 *				Session session = new Session("localhost", 1984, "admin", "admin");
 * 
 *        // Version 1: perform command and show result or error output
 *				if (session.Execute("xquery 1 to 10"))
 *        {
 *					Console.WriteLine(session.Result);
 * 				}
 *        else
 *        {
 *					Console.WriteLine(session.Info);
 *				}
 *
 *        // Version 2 (faster): send result to the specified output stream
 *        Stream stream = Console.OpenStandardOutput();
 *        if (!session.Execute("xquery 1 to 10", out))
 *        {
 *          Console.WriteLine(session.Info);
 *        }
 * 
 *				session.Close();
 *			} catch (Exception e) {
 *				Console.WriteLine(e.Message);
 *			}
 *		}
 *		}
 *	}
 * 
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Collections.Generic;
using System.IO;

namespace BaseXClient
{
  class Session
  {
    private MemoryStream result = new MemoryStream();
    private byte[] cache = new byte[4096];
    private NetworkStream stream;
    private TcpClient socket;
    private string info = "";
    private int bpos;
    private int bsize;

    /** Constructor, creating a new socket connection. */
    public Session(string host, int port, string username, string pw)
    {
      socket = new TcpClient(host, port);
      stream = socket.GetStream();
      string ts = ReadString();
      Send(username);
      Send(MD5(MD5(pw) + ts));
      if (stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }
    
    /** Executes the specified command. */
    public bool Execute(string com, Stream ms)
    {
      Send(com);
      Init();
      ReadString(ms);
      info = ReadString();
      return Read() == 0;
    }
    
    /** Executes the specified command. */
    public bool Execute(string com)
    {
      result = new MemoryStream();
      return Execute(com, result);
    }
    
    /** Returns the result. */
    public string Result
    {
      get
      {
        return System.Text.Encoding.UTF8.GetString(result.ToArray());
      }
    }
    
    /** Returns the processing information. */
    public string Info
    {
      get
      {
        return info;
      }
    }
    
    /** Closes the connection. */
    public void Close()
    {
      Send("exit");
      socket.Close();
    }
    
    /** Initializes the byte transfer. */
    private void Init()
    {
      bpos = 0;
      bsize = 0;
    }
    
    /** Returns a single byte from the socket. */
    private byte Read()
    {
      if (bpos == bsize)
      {
        bsize = stream.Read(cache, 0, 4096);
        bpos = 0;
      }
      return cache[bpos++];
    }
    
    /** Receives a string from the socket. */
    private void ReadString(Stream ms)
    {
      while (true)
      {
        byte b = Read();
        if (b == 0) break;
        ms.WriteByte(b);
      }
    }
    
    /** Receives a string from the socket. */
    private string ReadString()
    {
      MemoryStream ms = new MemoryStream();
      ReadString(ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }
    
    /** Sends strings to server. */
    private void Send(string message)
    {
      byte[] msg = System.Text.Encoding.UTF8.GetBytes(message);
      stream.Write(msg, 0, msg.Length);
      stream.WriteByte(0);
    }
    
    /** Returns the md5 hash of a string. */
    private string MD5(string input)
    {
      MD5CryptoServiceProvider MD5 = new MD5CryptoServiceProvider();
      byte[] hash = MD5.ComputeHash(Encoding.UTF8.GetBytes(input));

      StringBuilder sb = new StringBuilder();
      foreach (byte h in hash)
      {
        sb.Append(h.ToString("x2"));
      }
      return sb.ToString();
    }
  }
}
