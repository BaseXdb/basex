module namespace chat = 'http://basex.org/modules/web-page';

import module namespace session = 'http://basex.org/modules/Session';
import module namespace sessions = 'http://basex.org/modules/Sessions';

(:~ Session chat id. :)
declare variable $chat:ID := 'chat';

(:~
 : Login page.
 : @return page
 :)
declare
  %rest:path('/chat/login')
  %output:method('html')
function chat:login() as element(html) {
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
 : Main page.
 : @return HTML page or redirection
 :)  
declare
  %rest:path('/chat')
  %output:method('html')
function chat:main(
) as item() {
  let $id := session:get($chat:ID)
  return if(empty($id)) then web:redirect('/chat/login') else
  
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
  ), <script type="text/javascript" src="/static/chat.js"/>)
};

(:~
 : Checks the user input and redirects to the main page, or back to the login page.
 : @param  $name  user name
 : @param  $pass  password
 : @return redirection
 :)  
declare
  %rest:POST
  %rest:path('/chat/login-check')
  %rest:query-param('name', '{$name}')
  %rest:query-param('pass', '{$pass}')
function chat:check(
  $name  as xs:string,
  $pass  as xs:string
) as element(rest:response) {
  try {
    user:check($name, $pass),
    session:set($chat:ID, $name),
    web:redirect('/chat')
  } catch user:* {
    web:redirect('/chat/login')
  }
};

(:~
 : Returns an HTML page.
 : @param $contents  page contents
 : @param $login     login page
 : @return HTML
 :)
declare %private function chat:wrap(
  $contents  as item()*,
  $headers   as element()*
) as element(html) {
  <html>
    <head>
      <meta charset='utf-8'/>
      <title>BaseX WebSocket Chat</title>
      <meta name='author' content='BaseX Team, 2014-18'/>
      <link rel='stylesheet' type='text/css' href='/static/style.css'/>
      { $headers }
    </head>
    <body>
      <span class='right'>
        {
          for $id in session:get($chat:ID)
          return <span>
            <b>{ $id }</b> (<a href='/chat/logout'>logout</a>)
          </span>
        }
        &#xa0; <img src='/static/basex.svg' class='img'/>
      </span>
      <h1>BaseX WebSocket Chat</h1>
      { $contents }
    </body>
  </html>
};
