(:~
 : WebSocket chat with rooms. Utility functions.
 :
 : These functions are used by both the web pages (chat.xqm) and the
 : WebSocket functions (chat-ws.xqm). They build on the ws module:
 :
 : - ws:ids() lists all open connections,
 : - ws:get() and ws:set() store extra information with a connection,
 : - ws:path() returns the path a connection was opened on (used here to
 :   find out which room a connection belongs to), and
 : - ws:send(), ws:emit() and ws:broadcast() push data to clients:
 :   ws:send() targets selected connections, ws:emit() reaches everyone,
 :   and ws:broadcast() reaches everyone except the current connection.
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat-util = 'chat/util';

import module namespace ws = 'http://basex.org/modules/ws';

(:~ Key under which the username is stored with each connection. :)
declare variable $chat-util:id := 'id';
(:~ WebSocket path prefix; the room name is appended (see chat-ws.xqm). :)
declare variable $chat-util:prefix := '/chat/';
(:~ Rooms offered on the page. The server accepts any room whose name
 : matches the pattern in the %ws annotations, so this list only drives
 : the buttons in the user interface (see chat.xqm). :)
declare variable $chat-util:rooms := ('lobby', 'tech', 'random');

(:~
 : Returns the room a connection belongs to. It is derived from the path
 : the connection was opened on: ws:path('websocket3') = '/chat/lobby'.
 : @param  $id  connection id
 : @return room name
 :)
declare function chat-util:room(
  $id  as xs:string
) as xs:string {
  substring-after(ws:path($id), $chat-util:prefix)
};

(:~
 : Returns a room name with a capital first letter, for display. The room id
 : itself stays lower case: the path templates in chat-ws.xqm constrain room
 : names to [a-z0-9-]+, so every room has a single canonical (lower-case) id.
 : @param  $room  room name (lower case)
 : @return capitalised name
 :)
declare function chat-util:name(
  $room  as xs:string
) as xs:string {
  upper-case(substring($room, 1, 1)) || substring($room, 2)
};

(:~
 : Sends the list of users to all clients: all registered users, and the
 : rooms each online user is currently connected to. The map is turned into
 : JSON and read again by the browser (chat.js).
 :)
declare function chat-util:users() as empty-sequence() {
  (: ws:emit sends the data to everyone who is connected :)
  ws:emit({
    'type': 'users',
    (: all users known to the server :)
    'users': array { sort(user:list()) },
    (: users that are connected right now, with their rooms: group the open
     : connections by user name (a user may have one tab open per room) :)
    'active': array {
      for $id in ws:ids()
      let $name := ws:get($id, $chat-util:id)
      where $name
      group by $name
      order by $name
      return {
        'name': $name,
        'rooms': array { sort(distinct-values($id ! chat-util:room(.))) }
      }
    }
  })
};

(:~
 : Sends a chat message: to everyone in the given room, or privately to all
 : connections of a single user (in any room).
 : @param  $text  text to be sent
 : @param  $to    receiver of a private message (empty: public message)
 : @param  $room  room the message was sent from
 :)
declare function chat-util:message(
  $text  as xs:string,
  $to    as xs:string?,
  $room  as xs:string
) as empty-sequence() {
  (: pick the target connections: the receiver's connections for a private
   : message, otherwise everyone who is in the same room :)
  let $ids := if($to)
    then ws:ids()[ws:get(., $chat-util:id) = $to]
    else ws:ids()[chat-util:room(.) = $room]
  return ws:send({
    'type': 'message',
    (: serialize() replaces the characters <, > and &, so the text
     : cannot smuggle HTML into the page (see chat.js) :)
    'text': serialize($text),
    (: ws:id() is the connection the message came from; the name
     : stored with it (see chat-ws:connect) is the sender :)
    'from': ws:get(ws:id(), $chat-util:id),
    'room': $room,
    'date': format-time(current-time(), '[H02]:[m02]:[s02]'),
    'private': boolean($to)
  }, $ids)
};

(:~
 : Sends a system line (a server notice, not a chat message) to the
 : specified connections.
 : @param  $text  notice text
 : @param  $ids   target connections
 :)
declare function chat-util:system(
  $text  as xs:string,
  $ids   as xs:string*
) as empty-sequence() {
  ws:send(chat-util:notice($text), $ids)
};

(:~
 : Sends a system line to every client except the current connection.
 : Used for join and leave notices: the one who triggers the event does
 : not need to be told about it (ws:broadcast leaves it out).
 : @param  $text  notice text
 :)
declare function chat-util:announce(
  $text  as xs:string
) as empty-sequence() {
  ws:broadcast(chat-util:notice($text))
};

(:~
 : Builds a system-line message.
 : @param  $text  notice text
 : @return message map
 :)
declare %private function chat-util:notice(
  $text  as xs:string
) as map(*) {
  {
    'type': 'system',
    'text': serialize($text),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]')
  }
};

(:~
 : Closes all WebSocket connections of the specified user.
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
