(:~
 : Code for logging in and out.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/login';

import module namespace config = 'dba/config' at 'lib/config.xqm';
import module namespace html = 'dba/html' at 'lib/html.xqm';

(:~
 : Permissions: checks the user credentials.
 : Redirects to the login page if a user is not logged in, or if the page is not public.
 : @param  $perm  permission data
 : @return redirection to login page if check was not successful
 :)
declare
  %perm:check('/dba', '{$perm}')
function dba:check(
  $perm  as map(*)
) as element(rest:response)? {
  let $path := $perm?path
  let $allow := $perm?allow
  return if ($allow = 'public') {
    (: public function, register id for better log entries :)
    request:set-attribute('id', $allow)
  } else if (session:get($config:SESSION-KEY)) {
    (: everything fine, user is logged in :)
  } else {
    (: normalize login path :)
    let $target := if (ends-with($path, '/dba')) then 'dba/login' else 'login'
    (: last visited page to redirect to (if there was one) :)
    let $page := replace($path, '^.*dba/?', '')[.]
    return web:redirect($target, html:parameters({ 'page': $page }))
  }
};

(:~
 : Login page.
 : @param  $name   username (optional)
 : @param  $error  error string (optional)
 : @param  $page   page to redirect to (optional)
 : @return login page or redirection to main page
 :)
declare
  %rest:GET
  %rest:path('/dba/login')
  %rest:query-param('_name',  '{$name}')
  %rest:query-param('_error', '{$error}')
  %rest:query-param('_page',  '{$page}')
  %output:method('html')
  %output:html-version('5')
  %perm:allow('public')
function dba:login(
  $name   as xs:string?,
  $error  as xs:string?,
  $page   as xs:string?
) as element() {
  (: user is already logged in: redirect to main page :)
  if (session:get($config:SESSION-KEY)) {
    web:redirect('/dba')
  } else {
    <tr>
      <td>
        <form method='post'>
          <input type='hidden' name='_page' value='{ $page }'/>
          {
            map:for-each(html:parameters(), fn($key, $value) {
              <input type='hidden' name='{ $key }' value='{ $value }'/>
            })
          }
          <div class='small'/>
          <table>
            <tr>
              <td><b>Name:</b></td>
              <td>
                <input type='text' name='_name' value='{ $name }' autofocus=''/>
              </td>
            </tr>
            <tr>
              <td><b>Password:</b></td>
              <td>{
                <input type='password' name='_pass'/>,
                ' ',
                html:button('login', 'Login')
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
    => html:wrap({ 'error': $error })
  }
};

(:~
 : Checks the user input and redirects to the main page or back to the login page.
 : @param  $name  username
 : @param  $pass  password
 : @param  $page  page to redirect to (optional)
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/login')
  %rest:form-param('_name', '{$name}')
  %rest:form-param('_pass', '{$pass}')
  %rest:form-param('_page', '{$page}')
  %perm:allow('public')
function dba:login-check(
  $name  as xs:string,
  $pass  as xs:string,
  $page  as xs:string?
) as element(rest:response) {
  try {
    user:check($name, $pass),
    if (user:list-details($name)/@permission != 'admin') {
      dba:reject($name, 'Admin credentials required', $page)
    } else {
      dba:accept($name, $page)
    }
  } catch user:* {
    dba:reject($name, 'Please check your login data', $page)
  }
};

(:~
 : Ends a session and redirects to the login page.
 : @return redirection
 :)
declare
  %rest:path('/dba/logout')
function dba:logout(
) as element(rest:response) {
  let $user := session:get($config:SESSION-KEY)
  return (
    (: write log entry, redirect to login page :)
    admin:write-log('Logout: ' || $user, 'DBA'),
    web:redirect('/dba/login', { '_name': $user })
  ),
  (: deletes the session key :)
  session:delete($config:SESSION-KEY)
};

(:~
 : Registers a user and redirects to the main page.
 : @param  $name  entered username
 : @param  $page  page to redirect to (optional)
 : @return redirection
 :)
declare %private function dba:accept(
  $name  as xs:string,
  $page  as xs:string?
) as element(rest:response) {
  (: register user, write log entry :)
  session:set($config:SESSION-KEY, $name),
  admin:write-log('Login: ' || $name, 'DBA'),

  (: redirect to supplied page or main page :)
  web:redirect($page[.] otherwise 'logs', html:parameters())
};

(:~
 : Rejects a user and redirects to the login page.
 : @param  $name   entered username
 : @param  $error  error message
 : @param  $page   page to redirect to (optional)
 : @return redirection
 :)
declare %private function dba:reject(
  $name   as xs:string,
  $error  as xs:string,
  $page   as xs:string?
) as element(rest:response) {
  (: write log entry, redirect to login page :)
  admin:write-log('Login denied: ' || $name, 'DBA'),
  web:redirect(
    'login',
    html:parameters({ 'name': $name, 'error': $error, 'page': $page })
  )
};
