module namespace chat = 'http://basex.org/modules/web-page';

import module namespace session = 'http://basex.org/modules/Session';
import module namespace sessions = 'http://basex.org/modules/Sessions';
import module namespace ws = 'http://basex.org/modules/ws';

(:~ Session chat id. :)
declare variable $chat:ID := 'chat';

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
  return if($type = 'message') then (
    chat:message($json?text)
  ) else if($type = 'users') then (
    chat:users()
  ) else error()
};

(:~ 
 : Closes a WebSocket.
 :)
declare
  %ws:close('/chat')
function chat:ws-close() as empty-sequence() {
  chat:users()
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
declare %private function chat:message(
  $text  as xs:string
) as empty-sequence() {
  ws:emit(json:serialize(map {
    'type': 'message',
    'text': serialize($text),
    'user': session:get($chat:ID),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]')
  }))
};
