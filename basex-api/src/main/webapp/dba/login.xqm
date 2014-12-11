(:~
 : Code for logging in and out.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/login';

import module namespace Session = 'http://basex.org/modules/session';
import module namespace G = 'dba/global' at 'modules/global.xqm';
import module namespace html = 'dba/html' at 'modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at 'modules/tmpl.xqm';
import module namespace web = 'dba/web' at 'modules/web.xqm';

(:~
 : Login page.
 : @param  $name   user name
 : @param  $url    url
 : @param  $error  error string 
 : @return page
 :)
declare
  %rest:path("dba/login")
  %rest:query-param("name" , "{$name}")
  %rest:query-param("url",   "{$url}")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function _:login(
  $name   as xs:string?,
  $url    as xs:string?,
  $error  as xs:string?
) as element(html) {
  tmpl:wrap(map { 'error': $error },
    <tr>
      <td>
        <form action="login-check" method="post">
          <div class='note'>
            Please enter your credentials:
          </div>
          <div class='small'/>
          <table>
            <tr>
              <td><b>Name:</b></td>
              <td>
                <input size="30" name="name" value="{ $name }" id="user"/>
                { html:focus('user') }
                { html:button('login', 'Login') }
              </td>
            </tr>
            <tr>
              <td><b>Password:</b></td>
              <td>
                <input size="30" type="password" name="pass"/>
              </td>
            </tr>
            <tr>
              <td colspan='2'>
              <div class='small'/>
              <div class='note'>
                Enter a <code>host:port</code> combination if you want to connect<br/>
                to a remote BaseX server instance:
              </div>
              <div class='small'/>
              </td>
            </tr>
            <tr>
              <td><b>Address:</b></td>
              <td>
                <input size="30" name="url" value="{ $url }"/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Checks the user input and redirects to the start or login page.
 : @param  $name  user name
 : @param  $pass  password
 : @param  $url   url
 :)
declare
  %updating
  %rest:path("dba/login-check")
  %rest:query-param("name", "{$name}")
  %rest:query-param("pass", "{$pass}")
  %rest:query-param("url",  "{$url}")
function _:check(
  $name  as xs:string,
  $pass  as xs:string,
  $url   as xs:string
) {
  if($url) then (
    if(matches($url, '^.+:\d+/?$')) then (
      let $host := replace($url, ':.*$', '')
      let $port := replace($url, '^.*:|/$', '')
      return try {
        client:close(client:connect($host, xs:integer($port), $name, $pass)),
        Session:set($G:SESSION-KEY, $name),
        Session:set($G:SESSION-REMOTE,
          <remote>
            <host>{ $host }</host>
            <port>{ $port }</port>
            <name>{ $name }</name>
            <pass>{ $pass }</pass>
          </remote>
        ),
        web:redirect('databases')
      } catch * {
        web:redirect('login', map { 'name': $name, 'url': $url, 'error': $err:description })
      }
    ) else (
      web:redirect('login', map {
        'name': $name,
        'url': $url,
        'error': 'Please check the syntax of your URL.'
      })
    )
  ) else (
    let $server-pw := user:list-details()[@name = $name]/password[@algorithm = 'salted-sha256']
    let $server-salt := $server-pw/salt
    let $server-hash := $server-pw/hash
    let $user-hash := lower-case(xs:string(xs:hexBinary(hash:sha256($server-salt || $pass))))
    return if($server-hash = $user-hash) then (
      Session:set($G:SESSION-KEY, $name),
      admin:write-log('User was logged in: ' || $name),
      web:redirect('databases')
    ) else (
      web:redirect('login', map {
        'name': $name,
        'error': 'Please check your login data.'
      })
    )
  )
};

(:~
 : Ends a session and redirects to the login page.
 :)
declare
  %updating
  %rest:path("dba/logout")
function _:logout(
) {
  admin:write-log('User was logged out: ' || $G:SESSION),
  Session:close(),
  web:redirect('login')
};
