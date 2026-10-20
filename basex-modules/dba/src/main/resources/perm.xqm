(:~
 : Permission checks.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/perm';

(: this module is parsed for every request, so it only imports the configuration :)
import module namespace config = 'dba/lib/config' at 'lib/config.xqm';

(:~
 : Permissions: checks the user credentials.
 : @param  $perm  permission data
 : @return redirection to login page if check was not successful
 :)
declare
  %perm:check('/dba', '{$perm}')
function dba:check(
  $perm  as map(*)
) as element(rest:response)? {
  (: redirects to the login page if a user is not logged in, or if the page is not public :)
  let $path := $perm?path
  let $allow := $perm?allow
  return if ($allow = 'public') then (
    (: public function, register id for better log entries :)
    request:set-attribute('id', $allow)
  ) else if (session:get($config:SESSION-KEY)) then (
    (: everything fine, user is logged in :)
  ) else (
    (: last visited page to redirect to (if there was one), current query parameters :)
    let $page := replace($path, '^.*dba/?', '')[.]
    let $params := {
      for key $key value $value in request:parameter-map()
      where not(starts-with($key, '_'))
      return { $key: $value },
      { '_page': $page }
    }
    return web:redirect('/dba/login', $params)
  )
};

(:~
 : Permissions: checks the user credentials of a WebSocket handshake.
 :)
declare
  %perm:check('/ws/dba')
function dba:ws-check() as empty-sequence() {
  (: webSocket URLs have their own address space, so they are not covered by the check above :)
  if (empty(session:get($config:SESSION-KEY))) {
    web:error(403, 'Please log in.')
  }
};
