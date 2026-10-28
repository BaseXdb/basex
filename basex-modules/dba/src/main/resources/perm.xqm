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
  (: redirects to the login page if the page is not public and no user is logged in :)
  switch () {
    (: public function, register id for better log entries :)
    case $perm?allow = 'public' return request:set-attribute('id', $perm?allow)
    case exists(session:get($config:SESSION-KEY)) return ()
    default return (
      (: last visited page to redirect to (if there was one), current query parameters :)
      let $page := replace($perm?path, '^.*dba/?', '')[.]
      let $params := {
        map:filter($perm?parameters, fn($key) { not(starts-with($key, '_')) }),
        { '_page': $page }
      }
      return web:redirect('/dba/login', $params)
    )
  }
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
