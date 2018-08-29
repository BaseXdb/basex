module namespace chat = 'http://basex.org/modules/web-page';

import module namespace session = 'http://basex.org/modules/Session';
import module namespace sessions = 'http://basex.org/modules/Sessions';
import module namespace ws = 'http://basex.org/modules/ws';

(:~ Session chat id. :)
declare variable $chat:ID		  := 'chat';
(:~ WebSocket ID of the WebSocket instance :)
declare variable $chat:ws-ID   := 'ws-id';

(:~ 
 : Processes a WebSocket message.
 : @param  $message  message
 :)
declare
  %ws:message('/chat', '{$message}')
function chat:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $type := $json?type
  return if($type = 'message-global') then (
    chat:message-global($json?text)
  ) else if($type = 'message-private') then(
    chat:message-private($json?text, $json?receiver)
  ) else if($type = 'users') then (
    chat:users()
  ) else error()
};

(:~ 
 : Opens a WebSocket.
 :)
declare
  %ws:connect('/chat')
  function chat:ws-connect() as empty-sequence() {
  let $ws-ids := session:get($chat:ws-ID)
  let $set-ws-sess := session:set($chat:ws-ID, ($ws-ids , ws:id()))
  return ()
};

(:~ 
 : Closes a WebSocket.
 :)
declare
  %ws:close('/chat')
function chat:ws-close() as empty-sequence() {
  let $ws-ids := session:get($chat:ws-ID)
  let $del-ws-id := session:set($chat:ws-ID, $ws-ids[. != ws:id()])
  return chat:users()
};
  
(:~
 : Sends a user list to all clients.
 :)
declare %private function chat:users() as empty-sequence() {
  ws:emit(json:serialize(map {
    'type': 'users',
    'users': array { distinct-values(
      sessions:ids() ! sessions:get(., $chat:ID)
    )}
  }))
};

(:~
 : Sends a message to all clients.
 :)
declare %private function chat:message-global(
  $text  as xs:string
) as empty-sequence() {
  ws:emit(json:serialize(map {
    'type': 'message-global',
    'text': serialize($text),
    'user': session:get($chat:ID),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]')
  }))
};

(:~ 
 : Sends a message to a specific user.
 :)
declare %private function chat:message-private(
  $text as xs:string,
  $name as xs:string
) as empty-sequence(){
  let $ws-sessions := (
     for $session-id in sessions:ids()
     where sessions:get($session-id,$chat:ID) = $name
     return sessions:get($session-id,$chat:ws-ID)
   )
   return ws:send(json:serialize(map {
    'type': 'message-private',
    'text': serialize($text),
    'user': session:get($chat:ID),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]')
  }), $ws-sessions)
};