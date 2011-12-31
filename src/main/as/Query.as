/**
 * Language Binding for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) Manfred Knobloch, BSD License
 * Institut für Wissensmedien IWM - Knowledge Media Research Center (KMRC)
 **/
package basexapi
{
  import flash.utils.ByteArray;

  public class Query
  {
    public const NO_ID:int = 0;
    public const CLOSED:int = -1;
    public const PENDING_ID_REQUEST:int = 10;
    public const ID_RECEIVED:int = 20;
    public const PENDING_BIND_REQUEST:int = 30;
    public const BIND_RESPONSE:int = 40;		
    public const EXECUTE_REQUEST:int = 50;
    
    protected var _session:Session;		
    protected var _query:String;
    protected var _name:String;
    protected var _value:String;
    
    [Bindable]
    public var state:int=0;
    public var id:String = "";
    protected var socketBuffer:ByteArray;
    
    public function Query(session:Session, query:String)
    { 
      _session = session;
      _query = query;
      _name = "";
      _value = "";
      state = PENDING_ID_REQUEST;
      socketBuffer = new ByteArray();
      socketBuffer.writeByte(0);
      socketBuffer.writeUTFBytes(_query);
      socketBuffer.writeByte(0);
      _session.sendBuffer(socketBuffer);
    }
    
    /**
     * detect bind indicator 'declare' in query
     * string
     **/
    public function needBind():Boolean{
      var _lowerq:String = _query.toLowerCase();
      if ( _lowerq.indexOf('declare') > -1){
        return true;
      }
      return false;					
    }
    
    public function setBindParams(name:String, value:String):void{
      _name = name;
      _value = value;
    }
    
    /**
     * send 2 \0 bytes at end, otherwise bind will fail for
     * the type parameter seems to be neccessary
     * ('\x03', '0\x00$name\x00number\x00\x00')
     **/ 
    public function bind():void{
      if (state > PENDING_ID_REQUEST){
        state = PENDING_BIND_REQUEST;
        socketBuffer = new ByteArray();				
        socketBuffer.writeByte(3);
        socketBuffer.writeUTFBytes(id);
        socketBuffer.writeByte(0);
        socketBuffer.writeUTFBytes(_name);
        socketBuffer.writeByte(0);
        socketBuffer.writeUTFBytes(_value);
        socketBuffer.writeByte(0); // termination for value
        socketBuffer.writeByte(0); // termination for unused type
        
        socketBuffer.position = 0;
        _session.sendBuffer(socketBuffer);				
      }
    }
    
    public function sendQuery(code:int, arg:String):void{	
      socketBuffer = new ByteArray();
      socketBuffer.writeByte(code);
      socketBuffer.writeUTFBytes(this.id);
      socketBuffer.writeByte(0);
      _session.sendBuffer(socketBuffer);
    }
    
    public function execute():void{
      sendQuery(0x05, id);
    }
    
    public function info():void{
      exc(6, id);
    }
    
    public function options():void{
      exc(7, id);
    }
    
    public function close():void{
      exc(2, id);
    }
    
    public function exc(cmd:int, arg:String):void{
      sendQuery(cmd, arg);
    }
    
  }
}