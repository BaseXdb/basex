(:~
 : Simple WebSocket chat. RESTXQ functions.
 : @author BaseX Team 2005-23, BSD License
 :)
module namespace chat = 'chat';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';

(:~
 : Login or main page.
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
 : @param  $name  username
 : @param  $pass  password
 : @return redirection
 :)  
declare
  %rest:POST
  %rest:path('/chat/login-check')
  %rest:query-param('name', '{$name}')
  %rest:query-param('pass', '{$pass}')
function chat:login-check(
  $name  as xs:string,
  $pass  as xs:string
) as element(rest:response) {
  try {
    user:check($name, $pass),
    session:set($chat-util:id, $name)
  } catch user:* {
    (: login fails: no session info is set :)
  },
  web:redirect('/chat')
};

(:~
 : Logs out the current user, notifies all WebSocket clients, and redirects to the login page.
 : @return redirection
 :)
declare
  %rest:path('/chat/logout')
function chat:logout() as element(rest:response) {
  session:get($chat-util:id) ! chat-util:close(.),
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
 : Returns the HTML main page.
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
 : Returns an HTML page.
 : @param $contents  page contents
 : @param $login     login page
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
      <meta name='author' content='BaseX Team 2005-23, BSD License'/>
      <link rel='stylesheet' type='text/css' href='/static/style.css'/>
      { $headers }
    </head>
    <body>
      <span class='right'>
        {
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
