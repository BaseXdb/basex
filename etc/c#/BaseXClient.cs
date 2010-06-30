/*
 * -----------------------------------------------------------------------------
 * 
 * This module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the execute() method with the
 * database command as argument. The method returns the result or throws
 * an exception with the received error message.
 * For the execution of the iterative version of a query you need to call
 * the query() method. The results will then be returned via the more() and
 * the next() methods. If an error occurs an exception will be thrown.
 *
 * An even faster approach is to call execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
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
    private byte[] cache = new byte[4096];
    public NetworkStream stream;
    private TcpClient socket;
    private string info = "";
    private int bpos;
    private int bsize;

    /** see readme.txt */
    public Session(string host, int port, string username, string pw)
    {
      socket = new TcpClient(host, port);
      stream = socket.GetStream();
      string ts = receive();
      Send(username);
      Send(MD5(MD5(pw) + ts));
      if (stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }
    
    /** see readme.txt */
    public bool Execute(string com, Stream ms)
    {
      Send(com);
      Init();
      Receive(ms);
      info = Receive();
      return Ok();
    }
    
    /** see readme.txt */
    public String Execute(string com)
    {
      Send(com);
      Init();
      String result = Receive();
      info = Receive();
      if(!Ok()) 
      {
      	throw new IOException(info);
      }
      return result;
    }
    
    /** see readme.txt */
    public Query Query(string q) 
    {
      return new Query(this, q);
    }
    
    /** see readme.txt */
    public string Info
    {
      get
      {
        return info;
      }
    }
    
    /** see readme.txt */
    public void Close()
    {
      send("exit");
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
    private void Receive(Stream ms)
    {
      while (true)
      {
        byte b = Read();
        if (b == 0) break;
        ms.WriteByte(b);
      }
    }
    
    /** Receives a string from the socket. */
    public string Receive()
    {
      MemoryStream ms = new MemoryStream();
      Receive(ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }
    
    /** Sends strings to server. */
    public void Send(string message)
    {
      byte[] msg = System.Text.Encoding.UTF8.GetBytes(message);
      stream.Write(msg, 0, msg.Length);
      stream.WriteByte(0);
    }
    
    /** Returns success check. */
    public bool Ok()
    {
      return Read() == 0;
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
  
  class Query
  {
  	private Session session;
  	private string id;
  	private string nextItem;
  	
  	/** see readme.txt */
    public Query(Session s, string query)
    {
	  session = s;
	  session.stream.WriteByte(0);
	  session.Send(query);
	  id = session.Receive();
	  if(!session.Ok())
	  {
	  	throw new IOException(session.Receive());
	  }
    }
    
    /** see readme.txt */
    public bool More() 
    {
      session.stream.WriteByte(1);
      session.Send(id);
      nextItem = session.Receive();
      if(!session.Ok())
	  {
	  	throw new IOException(session.Receive());
	  }
      return nextItem.Length != 0;
    }
    
    /** see readme.txt */
    public string Next()
    {
      return nextItem;      
    }
    
    /** see readme.txt */
    public void Close()
    {
      session.stream.WriteByte(2);
      session.Send(id);
    }
  }
}