(:~
 : Code for logging in and out.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/login';

import module namespace session = 'dba/session' at 'modules/session.xqm';
import module namespace html = 'dba/html' at 'modules/html.xqm';

(:~
 : Permissions: checks the user credentials.
 : @param  $perm  permission data
 : @return redirection to login page or empty sequence
 :)
declare
  %perm:check('/dba', '{$perm}')
function dba:check(
  $perm  as map(*)
) as element(rest:response)? {
  (: redirect to login page if user is not logged in, or if page is not public :)
  let $path := $perm?path
  where not($session:VALUE or $perm?allow = 'all')
  (: normalize login path :)
  let $target := if(ends-with($path, '/dba')) then 'dba/login' else 'login'
  (: last visited page to redirect to (if there was one) :)
  let $page := replace($path, '^.*dba/?', '')[.]
  return web:redirect($target, map { 'page': $page })
};

(:~
 : Login page.
 : @param  $name   user name (optional)
 : @param  $error  error string (optional)
 : @param  $page   page to redirect to (optional)
 : @return page
 :)
declare
  %rest:path("/dba/login")
  %rest:query-param("name" , "{$name}")
  %rest:query-param("error", "{$error}")
  %rest:query-param("page",  "{$page}")
  %output:method("html")
  %perm:allow("all")
function dba:welcome(
  $name   as xs:string?,
  $error  as xs:string?,
  $page   as xs:string?
) as element(html) {
  html:wrap(map { 'error': $error },
    <tr>
      <td>
        <form action="login-check" method="post">
          <input type="hidden" name="page" value="{ $page }"/>
          <div class='note'>
            Enter your admin credentials:
          </div>
          <div class='small'/>
          <table>
            <tr>
              <td><b>Name:</b></td>
              <td>
                <input size="30" name="name" value="{ $name }" id="user"/>
                { html:focus('user') }
              </td>
            </tr>
            <tr>
              <td><b>Password:</b></td>
              <td>{
                <input size="30" type="password" name="pass"/>,
                ' ',
                html:button('login', 'Login')
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Checks the user input and redirects to the main page, or back to the login page.
 : @param  $name  user name
 : @param  $pass  password
 : @param  $page  page to redirect to (optional)
 : @return redirection
 :)
declare
  %rest:path("/dba/login-check")
  %rest:query-param("name", "{$name}")
  %rest:query-param("pass", "{$pass}")
  %rest:query-param("page", "{$page}")
  %perm:allow("all")
function dba:login(
  $name  as xs:string,
  $pass  as xs:string,
  $page  as xs:string?
) as element(rest:response) {
  try {
    user:check($name, $pass),
    if(user:list-details($name)/@permission != 'admin') then (
      dba:reject($name, 'Admin credentials required', $page)
    ) else (
      dba:accept($name, $page)
    )
  } catch user:* {
    dba:reject($name, 'Please check your login data', $page)
  }
};

(:~
 : Ends a session and redirects to the login page.
 : @return redirection
 :)
declare
  %rest:path("/dba/logout")
function dba:logout(
) as element(rest:response) {
  (: write log entry, redirect to login page :)
  admin:write-log('DBA user was logged out: ' || $session:VALUE),
  web:redirect("/dba/login", map { 'name': $session:VALUE }),
  (: closes the DBA session :)
  session:close()
};

(:~
 : Registers a user and redirects to the main page.
 : @param  $name  entered user name
 : @param  $page  page to redirect to (optional)
 : @return redirection
 :)
declare %private function dba:accept(
  $name  as xs:string,
  $page  as xs:string?
) as element(rest:response) {
  (: register user, write log entry :)
  session:set($session:ID, $name),
  admin:write-log('DBA user was logged in: ' || $name),

  (: redirect to supplied page or main page :)
  web:redirect(if($page) then $page else 'logs')
};

(:~
 : Rejects a user and redirects to the login page.
 : @param  $name     entered user name
 : @param  $message  error message
 : @param  $page     path to redirect to (optional)
 : @return redirection
 :)
declare %private function dba:reject(
  $name     as xs:string,
  $message  as xs:string,
  $page     as xs:string?
) as element(rest:response) {
  (: write log entry, redirect to login page :)
  admin:write-log('DBA login was denied: ' || $name),
  web:redirect("login", map { 'name': $name, 'error': $message, 'page': $page })
};
