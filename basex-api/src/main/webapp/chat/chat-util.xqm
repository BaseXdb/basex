(:~
 : Simple WebSocket chat. Utility functions.
 :
 : These functions are used by both the web pages (chat.xqm) and the
 : WebSocket functions (chat-ws.xqm). They build on the ws module:
 : ws:ids() lists all open connections, ws:get() and ws:set() store
 : extra information with a connection, and ws:send() and ws:emit()
 : push data to clients.
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat-util = 'chat/util';

import module namespace session = 'http://basex.org/modules/session';
import module namespace ws = 'http://basex.org/modules/ws';

(:~ Key under which the username is stored: in the session,
 : and with each WebSocket connection. :)
declare variable $chat-util:id := 'id';

(:~
 : Sends the list of users (all, and currently online) to all clients.
 : The map is turned into JSON and read again by the browser (chat.js).
 :)
declare function chat-util:users() as empty-sequence() {
  (: ws:emit sends the data to everyone who is connected :)
  ws:emit({
    'type': 'users',
    (: all users known to the server :)
    'users': array { sort(user:list()) },
    (: users that are connected right now; duplicates are removed, as
     : a user may have the chat open in more than one browser tab :)
    'active': array { distinct-values(
      for $id in ws:ids()
      return ws:get($id, $chat-util:id)
    )}
  })
};

(:~
 : Sends a message to all clients, or to the clients of a specific user.
 : @param  $text  text to be sent
 : @param  $to    receiver of a private message (optional)
 :)
declare function chat-util:message(
  $text  as xs:string,
  $to    as xs:string?
) as empty-sequence() {
  (: pick the target connections: all of them, or only those
   : of the receiver if the message is private :)
  let $ws-ids := ws:ids()[not($to) or ws:get(., $chat-util:id) = $to]
  return ws:send({
    'type': 'message',
    (: serialize() replaces the characters <, > and &, so the text
     : cannot smuggle HTML into the page (see chat.js) :)
    'text': serialize($text),
    (: ws:id() is the connection the message came from; the name
     : stored with it (see chat-ws:connect) is the sender :)
    'from': ws:get(ws:id(), $chat-util:id),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]'),
    'private': boolean($to)
  }, $ws-ids)
};

(:~
 : Closes all WebSocket connections from the specified user.
 : @param  $name  username
 :)
declare function chat-util:close(
  $name  as  xs:string
) as empty-sequence() {
  for $id in ws:ids()
  where ws:get($id, $chat-util:id) = $name
  (: 1000 means: closed normally, nothing went wrong :)
  return ws:close($id, 1000, 'logout')
};
