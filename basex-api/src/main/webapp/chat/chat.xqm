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
  (: go back to the main page: it shows the chat if the login
   : worked, and the login form again if it did not :)
  web:redirect('/chat')
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
  web:redirect('/chat')
};

(:~
 : Returns the HTML login page.
 : @return HTML page
 :)
declare %private function chat:login() as element(html) {
  chat:wrap((
    <div class='warning'>Please enter your credentials:</div>,
    (: the entered name and password are sent to login-check (see above) :)
    <form action='/chat/login-check' method='post'>
      <div class='small'/>
      <table>
        <tr>
          <td><b>Name:</b></td>
          <td>
            <input size='30' name='name' id='user' autofocus=''/>
          </td>
        </tr>
        <tr>
          <td><b>Password:</b></td>
          <td>{
            <input size='30' type='password' name='pass'/>,
            <button type='submit'>Login</button>
          }</td>
        </tr>
      </table>
    </form>
  ), ())
};

(:~
 : Returns the HTML main page: a field for typing messages, the list of
 : users, and the chat messages. The included script (chat.js) opens the
 : WebSocket connection and fills in the 'users' and 'messages' parts.
 : @return HTML page
 :)
declare %private function chat:main() as element(html) {
  chat:wrap((
    <p>
      <input type='text' size='60' autofocus='true' placeholder='Message to all users…'
             id='input' onkeydown='keyDown(event)' autocomplete='off'/>
    </p>,
    <table width='100%'>
      <tr>
        <td width='100'>
          <div class='note'>USERS (<b>online</b>)</div>
          <div id='users'/>
        </td>
        <td class='vertical'/>
        <td>
          <div class='note'>CHAT MESSAGES</div>
          <div id='messages'/>
        </td>
      </tr>
    </table>
  ), <script type='text/javascript' src='/static/chat.js'/>)
};

(:~
 : Puts the supplied contents into a complete HTML page with header and title.
 : @param $contents  page contents
 : @param $headers   extra header elements (scripts, etc.)
 : @return HTML page
 :)
declare %private function chat:wrap(
  $contents  as item()*,
  $headers   as element()*
) as element(html) {
  <html>
    <head>
      <meta charset='utf-8'/>
      <title>BaseX WebSocket Chat</title>
      <meta name='author' content='BaseX Team, BSD License'/>
      <link rel='stylesheet' type='text/css' href='/static/style.css'/>
      { $headers }
    </head>
    <body>
      <span class='right'>
        {
          (: if someone is logged in, show the name and a logout link :)
          for $id in session:get($chat-util:id)
          return <span><b>{ $id }</b> (<a href='/chat/logout'>logout</a>)</span>
        }
        &#xa0; <a href='/'><img src='static/basex.svg' class='img'/></a>
      </span>
      <h1>BaseX WebSocket Chat</h1>
      { $contents }
    </body>
  </html>
};
