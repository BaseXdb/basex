(:~
 : Code for logging in and out.
 :
 : @author Christian Grün, BaseX Team 2005-19, BSD License
 :)
module namespace dba = 'dba/login';

import module namespace session = 'dba/session' at 'modules/session.xqm';
import module namespace html = 'dba/html' at 'modules/html.xqm';
import module namespace Request = 'http://exquery.org/ns/request';

(:~
 : Permissions: checks the user credentials.
 : @param  $perm  permission data
 : @return redirection to login page or empty sequence
 :)
declare
  %perm:check("/dba", "{$perm}")
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
  return web:redirect($target, dba:params(map { '_page': $page }))
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
  %rest:query-param("_name",  "{$name}")
  %rest:query-param("_error", "{$error}")
  %rest:query-param("_page",  "{$page}")
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
          <input name="_page" value="{ $page }" type="hidden"/>
          {
            for $param in Request:parameter-names()[not(starts-with(., '_'))]
            return <input name="{ $param }" value="{ Request:parameter($param) }" type="hidden"/>
          }
          <div class='small'/>
          <table>
            <tr>
              <td><b>Name:</b></td>
              <td>
                <input name="_name" value="{ $name }" id="user" size="30"/>
                { html:focus('user') }
              </td>
            </tr>
            <tr>
              <td><b>Password:</b></td>
              <td>{
                <input name="_pass" type="password" size="30"/>,
                ' ',
                <input type="submit" value="Login"/>
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
  %rest:POST
  %rest:path("/dba/login-check")
  %rest:query-param("_name", "{$name}")
  %rest:query-param("_pass", "{$pass}")
  %rest:query-param("_page", "{$page}")
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
  web:redirect("/dba/login", map { '_name': $session:VALUE }),
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
  web:redirect(if($page) then $page else 'logs', dba:params(map { }))
};

(:~
 : Rejects a user and redirects to the login page.
 : @param  $name     entered user name
 : @param  $message  error message
 : @param  $page     page to redirect to (optional)
 : @return redirection
 :)
declare %private function dba:reject(
  $name     as xs:string,
  $message  as xs:string,
  $page     as xs:string?
) as element(rest:response) {
  (: write log entry, redirect to login page :)
  admin:write-log('DBA login was denied: ' || $name),
  web:redirect(
    "login",
    dba:params(map { '_name': $name, '_error': $message, '_page': $page })
  )
};

(:~
 : Crerates a map with all current query parameters.
 : @param  $map  additional parameters
 : @return map
 :)
declare %private function dba:params(
  $map  as map(*)
) as map(*) {
  map:merge((
    $map,
    for $param in Request:parameter-names()[not(starts-with(., '_'))]
    return map { $param: Request:parameter($param) }
  ))
};
