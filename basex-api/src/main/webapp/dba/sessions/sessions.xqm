(:~
 : Sessions page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/sessions';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace session = 'dba/session' at '../modules/session.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'sessions';

(:~
 : Sessions page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/sessions")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:sessions(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Web Sessions</h2>
        {
          let $headers := (
            <id type='id'>ID</id>,
            <name>Name</name>,
            <value>Value</value>,
            <access type='dateTime' order='desc'>Last Access</access>,
            <you>You</you>
          )
          let $rows :=
            for $id in Sessions:ids()
            let $access := Sessions:accessed($id)
            let $you := if(Session:id() = $id) then '✓' else '–'
            (: supported session ids (application-specific, can be extended :)
            for $name in Sessions:names($id)[. = ($session:ID, 'id')]
            let $value := try {
              Sessions:get($id, $name)
            } catch Sessions:get {
              (: non-XQuery session value :)
            }
            let $string := util:chop(serialize($value, map { 'method': 'basex' }), 20)
            order by $access descending
            return <row id='{ $id || '|' || $name }' name='{ $name }' value='{ $string }'
                        access='{ $access }' you='{ $you }'/>
          let $buttons := (
            html:button('session-kill', 'Kill', true())
          )
          return html:table($headers, $rows, $buttons, map { }, map { 'sort': $sort })
        }
        </form>
      </td>
      <td class='vertical'/>
      <td>
        <h2>Database Sessions</h2>
        {
          let $headers := (
            <address>Address</address>,
            <user>User</user>
          )
          let $rows :=
            for $session in admin:sessions()
            return <row address='{ $session/@address }' user='{ $session/@user }'/>
          return html:table($headers, $rows, (), map { }, map { })
        }
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of users
 : @param  $ids     ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/sessions")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %rest:query-param("id",     "{$ids}")
function dba:users-redirect(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'id': $ids })
};
