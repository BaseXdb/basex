/*
 * Language Binding for BaseX.
 * Works with BaseX 6.3.1 and later
 * Documentation: http://basex.org/api
 *
 * (C) BaseX Team 2005-11, BSD License
 */
using System;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Collections.Generic;
using System.IO;
using System.Threading;

namespace BaseXClient
{
  class Session
  {
    private byte[] cache = new byte[4096];
    public NetworkStream stream;
    private TcpClient socket;
    private string info = "";
    private string shost;
    private int bpos;
    private int bsize;
	private TcpClient tsocket;
	private NetworkStream tstream;
	private Dictionary<string, TriggerNotification> tn;
	private int tport = 1985;

    /** see readme.txt */
    public Session(string host, int port, string username, string pw)
    {
      socket = new TcpClient(host, port);
      stream = socket.GetStream();
	  shost = host;
      string ts = Receive();
      Send(username);
      Send(MD5(MD5(pw) + ts));
      if (stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }

    /** see readme.txt */
    public void Execute(string com, Stream ms)
    {
      Send(com);
      Init();
      Receive(ms);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
    }

    /** see readme.txt */
    public String Execute(string com)
    {
      MemoryStream ms = new MemoryStream();
      Execute(com, ms);
      return System.Text.Encoding.UTF8.GetString(ms.ToArray());
    }

    /** see readme.txt */
    public Query Query(string q)
    {
      return new Query(this, q);
    }

    /** see readme.txt */
    public void Create(string name, Stream s)
    {
      stream.WriteByte(8);
      Send(name);
      while (true)
      {
      	int t = s.ReadByte();
      	if (t == -1) break;
      	stream.WriteByte(Convert.ToByte(t));
      }
      stream.WriteByte(0);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
    }
    
    /** see readme.txt */
    public void Add(string name, string target, Stream s)
    {
      stream.WriteByte(9);
      Send(name);
      Send(target);
      while (true)
      {
      	int t = s.ReadByte();
      	if (t == -1) break;
      	stream.WriteByte(Convert.ToByte(t));
      }
      stream.WriteByte(0);
      info = Receive();
      if(!Ok())
      {
        throw new IOException(info);
      }
    }
	
	/** see readme.txt */
  	public void AttachTrigger(string name, TriggerNotification notify)
	{
	  stream.WriteByte(10);
	  Send(name);
	  if(tsocket == null)
	  {	
		tn = new Dictionary<string, TriggerNotification>();
		tsocket = new TcpClient(shost, tport);
		tstream = tsocket.GetStream();
		string id = Receive();
		byte[] msg = System.Text.Encoding.UTF8.GetBytes(id);
      	tstream.Write(msg, 0, msg.Length);
      	tstream.WriteByte(0);
	    Thread t = new Thread(Listen);
		t.Start();
	  }
	  info = Receive();
	  if(!Ok())
      {
        throw new IOException(info);
      }
	  tn.Add(name, notify);
	}
	
	/** Listens to trigger socket */
	private void Listen() 
	{
	  while (true)
	    {
		  String name = readS();
		  String val = readS();
		  tn[name].Update(val);
		}
	}
	
	/** Returns trigger message */
	private string readS() 
	  {
		MemoryStream ms = new MemoryStream();
	  	while (true) 
		  {
			int b = tstream.ReadByte();
			if (b == 0) break;
			ms.WriteByte((byte) b);
		  }
		return System.Text.Encoding.UTF8.GetString(ms.ToArray()); 
	  }
	
	/** see readme.txt */
	public void Trigger(string query, string name, string msg, string mode)
	{
	  Execute("xquery db:trigger(" + query + ", " + name + ", " + notification + "," + mode")");
	}
	
	/** see readme.txt */
	public void DetachTrigger(string name)
	{
	  stream.WriteByte(11);
	  Send(name);
	  info = Receive();
	  if(!Ok())
      {
        throw new IOException(info);
      }
	  tn.Remove(name);
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
    private string next;

    /** see readme.txt */
    public Query(Session s, string query)
    {
      session = s;
      id = Exec(0, query);
    }

    /** see readme.txt */
    public string Init()
    {
      return Exec(4, id);
    }

    /** see readme.txt */
    public void Bind(string name, string value)
    {
      Exec(3, id + '\0' + name + '\0' + value + '\0');
    }

    /** see readme.txt */
    public bool More()
    {
      next = Exec(1, id);
      return next.Length != 0;
    }

    /** see readme.txt */
    public string Next()
    {
      return next;
    }

    /** see readme.txt */
    public string Execute()
    {
      return Exec(5, id);
    }

    /** see readme.txt */
    public string Info()
    {
      return Exec(6, id);
    }

    /** see readme.txt */
    public string Close()
    {
      return Exec(2, id);
    }

    /** see readme.txt */
    private string Exec(byte cmd, string arg)
    {
      session.stream.WriteByte(cmd);
      session.Send(arg);
      string s = session.Receive();
      if(!session.Ok())
      {
        throw new IOException(session.Receive());
      }
      return s;
    }
  }
	
  interface TriggerNotification 
	{
		void Update(string data);
	}
}