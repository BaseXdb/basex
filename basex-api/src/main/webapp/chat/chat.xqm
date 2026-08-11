(:~
 : Simple WebSocket chat. RESTXQ functions.
 :
 : This module contains the web part of the chat: logging in and out, and
 : building the HTML pages. The live part (sending and receiving chat
 : messages) uses WebSockets and is found in chat-ws.xqm.
 :
 : How RESTXQ works: A function with a %rest:path annotation is tied to a
 : URL. When someone opens that URL, the function runs, and its result is
 : sent back to the browser.
 :
 : All links are relative to the page they appear in (/chat), so they stay
 : valid if the application is deployed below a context path.
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat = 'chat';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';

(:~
 : Login or main page. The session remembers who is logged in (each browser
 : gets its own session). If it contains a user name, the chat is shown;
 : otherwise, the login form appears.
 : @return HTML page
 :)
declare
  %rest:path('/chat')
  %output:method('html')
function chat:chat() as element() {
  if(session:get($chat-util:id)) then (
    chat:main()
  ) else (
    chat:login()
  )
};

(:~
 : Checks the user input, registers the user and reloads the chat.
 : The %rest:form-param annotations pass the values of the two
 : form fields into the function.
 : @param  $name  username
 : @param  $pass  password
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/chat/login-check')
  %rest:form-param('name', '{$name}')
  %rest:form-param('pass', '{$pass}')
function chat:login-check(
  $name  as xs:string,
  $pass  as xs:string
) as element(rest:response) {
  try {
    (: fails with an error if name or password is wrong :)
    user:check($name, $pass),
    (: name and password are correct: remember the user in the session :)
    session:set($chat-util:id, $name)
  } catch user:* {
    (: login fails: no session info is set :)
  },
  (: go back to the main page (relative to /chat/login-check): it shows the
   : chat if the login worked, and the login form again if it did not :)
  web:redirect('../chat')
};

(:~
 : Logs out the current user, tells all connected clients,
 : and returns to the login page.
 : @return redirection
 :)
declare
  %rest:path('/chat/logout')
function chat:logout() as element(rest:response) {
  (: close the chat connections of the user (the '!' makes sure
   : this only happens if someone is logged in at all) :)
  session:get($chat-util:id) ! chat-util:close(.),
  (: forget the user in the session :)
  session:delete($chat-util:id),
  web:redirect('../chat')
};

(:~
 : Returns a static file of the application.
 : @param  $file  file or unknown path
 : @return rest binary data
 :)
declare
  %rest:path('/chat/.static/{$file=.+}')
  %output:method('basex')
function chat:file(
  $file  as xs:string
) as item()+ {
  let $path := 'static/' || $file
  return if (contains($file, '..')) {
    web:error(400, 'Invalid path: ' || $file)
  } else {
    web:response-header(
      { 'media-type': web:content-type($path) },
      { 'Cache-Control': 'max-age=3600,public' }
    ),
    fetch:binary($path)
  }
};

(:~
 : Returns the HTML login page.
 : @return HTML page
 :)
declare %private function chat:login() as element(html) {
  (: the entered name and password are sent to login-check (see above) :)
  chat:wrap(
    <div class='panel'>
      <form action='chat/login-check' method='post'>{
        chat:field('Name:', <input type='text' name='name' id='user' autofocus=''/>),
        chat:field('Password:', (
          <input type='password' name='pass'/>,
          ' ',
          <button type='submit'>Login</button>
        ))
      }</form>
    </div>
  , '1fr', ())
};

(:~
 : Creates a labelled form field.
 : @param  $label    field label
 : @param  $control  form control
 : @return field
 :)
declare %private function chat:field(
  $label    as xs:string,
  $control  as item()*
) as element(div) {
  <div class='field'>{
    <span>{ $label }</span>,
    <div>{ $control }</div>
  }</div>
};

(:~
 : Returns the HTML main page: the room selector, a status line, a field for
 : typing messages, the list of users, and the chat messages. The included
 : script (chat.js) opens the WebSocket connection to the selected room and
 : fills in the 'status', 'users' and 'messages' parts.
 : @return HTML page
 :)
declare %private function chat:main() as element(html) {
  (: the input sends chat messages; the button asks the server for
   : statistics, which arrive asynchronously (see chat-ws:info / ws:eval) :)
  chat:wrap((
  <div class='panel full'>
    <p class='compose'>
      <input type='text' autofocus='true' placeholder='Message to the room…'
             id='input' onkeydown='keyDown(event)' autocomplete='off'/>
      { ' ' }
      <button type='button' id='send' onclick='sendInput()' disabled='disabled'>Send</button>
      { ' ' }
      <button type='button' id='cancel' onclick='resetPrivateMsg()'
              title='Leave private mode' hidden='hidden'>Cancel</button>
      { ' ' }
      <button type='button' onclick='serverInfo()'>Who’s here?</button>
    </p>
  </div>,
  <div class='panel'>
    <div id='users'/>
  </div>,
  <div class='panel'>
    <div class='note'><b>MESSAGES</b></div>
    <div id='messages'/>
  </div>
  ), '12rem 1fr', <script type='text/javascript' defer='' src='chat/.static/chat.js'/>)
};

(:~
 : Puts the supplied panels into a complete HTML page with header and title.
 : @param $panels   page panels
 : @param $columns  grid track sizes of the panels
 : @param $headers  extra header elements (scripts, etc.)
 : @return HTML page
 :)
declare %private function chat:wrap(
  $panels   as item()*,
  $columns  as xs:string,
  $headers  as element()*
) as element(html) {
  let $user := session:get($chat-util:id)
  return <html lang='en'>
    <head>
      <meta charset='utf-8'/>
      <meta http-equiv='Content-Security-Policy'
            content="default-src 'self'; script-src 'self' 'unsafe-inline';
                     style-src 'self' 'unsafe-inline'; img-src 'self' data:;
                     object-src 'none'; base-uri 'self'"/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>BaseX Chat Application</title>
      <meta name='description' content='WebSocket Chat'/>
      <meta name='author' content='BaseX Team, BSD License'/>
      <meta name='robots' content='noindex'/>
      <link rel='icon' href='chat/.static/basex.svg'/>
      <link rel='stylesheet' type='text/css' href='chat/.static/style.css'/>
      { $headers }
    </head>
    <body data-user='{ $user }'>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>
              <span class='title-full'>BaseX Chat Application</span>
              <span class='title-short'>BaseX Chat</span>
            </h1>
            {
              (: if someone is logged in, show the name and a logout link :)
              if($user) { <div><b>{ $user }</b> · <a href='chat/logout'>logout</a></div> }
            }
          </div>
          <nav class='ellipsis'>{
            (: logged in: room selector (active room bolded by chat.js) plus the
             : info element for server notices (welcome, join/leave, 'Who''s
             : here?'); logged out: the login prompt, placed here like the DBA :)
            if($user) {
              let $links := $chat-util:rooms !
                <a href='#' class='room' data-room='{ . }'>{ chat-util:name(.) }</a>
              return (
                head($links), tail($links) ! (' · ', .),
                (1 to 2) ! '&#x2000;', <b id='info' class='note'/>
              )
            } else {
              <div class='note'>Please enter your credentials:</div>
            }
          }</nav>
          <hr/>
        </div>
        <a href='./' class='header-logo'><img src='chat/.static/basex.svg' alt='BaseX'/></a>
      </header>
      <main>
        <div class='content' style='--columns: { $columns }'>{ $panels }</div>
      </main>
      <hr/>
      <footer class='right'><sup>BaseX Team, BSD License</sup></footer>
    </body>
  </html>
};
