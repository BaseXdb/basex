(:~
 : Code for logging in and out.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/login';

import module namespace config = 'dba/lib/config' at 'lib/config.xqm';
import module namespace form = 'dba/lib/form' at 'lib/form.xqm';
import module namespace html = 'dba/lib/html' at 'lib/html.xqm';

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
  %perm:allow('public')
function dba:login(
  $name   as xs:string?,
  $error  as xs:string?,
  $page   as xs:string?
) as element() {
  (: user is already logged in: redirect to main page :)
  if (session:get($config:SESSION-KEY)) then (
    web:redirect('/dba')
  ) else (
    html:panel(
      <form method='post'>
        <input type='hidden' name='_page' value='{ $page }'/>
        {
          for key $key value $value in html:parameters()
          return <input type='hidden' name='{ $key }' value='{ $value }'/>,
          form:field('Name:', <input type='text' name='_name' value='{ $name }' autofocus=''/>),
          form:field('Password:', (
            <input type='password' name='_pass'/>,
            ' ',
            form:button('login', 'Log In')
          ))
        }
      </form>,
      { 'pane': false() })
    => html:wrap({ 'error': $error })
  )
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
    if (user:list-details($name)/@permission != 'admin') then (
      dba:reject($name, 'Admin credentials required', $page)
    ) else (
      dba:accept($name, $page)
    )
  } catch user:* {
    dba:reject($name, 'Please check your login data', $page),
    prof:sleep(500)
  }
};

(:~
 : Ends a session and redirects to the login page.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/logout')
function dba:logout(
) as element(rest:response) {
  let $user := session:get($config:SESSION-KEY)
  return (
    (: write log entry, redirect to login page :)
    admin:write-log('Logout: ' || $user, 'DBA'),
    web:redirect('/dba/login', { '_name': $user })
  ),
  (: closes the WebSockets of the session, deletes the session key :)
  ws:list-details()[@session = session:id()] ! ws:close(@id),
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

  (: redirect to supplied page or start page; the prefix keeps the target on the DBA :)
  web:redirect(`/dba/{ $page[.] otherwise head($config:VIEWS) }`, html:parameters())
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
