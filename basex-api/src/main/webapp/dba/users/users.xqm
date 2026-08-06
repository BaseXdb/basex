(:~
 : Users.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Users.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/users')
  %rest:query-param('sort',  '{$sort}', 'name')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:users(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  (
    <div class='panel'>
      <form method='post' autocomplete='off'>
        <h2>Users</h2>
        {
          let $headers := (
            { 'key': 'name', 'label': 'Name' },
            { 'key': 'permission', 'label': 'Permission' },
            { 'key': 'you', 'label': 'You' }
          )
          let $entries := (
            let $current := session:get($config:SESSION-KEY)
            for $user in user:list-details()
            let $name := string($user/@name)
            return {
              'name': $name,
              'permission': $user/@permission,
              'you': if ($current = $name) then '✓' else '–'
            }
          )
          let $buttons := (
            html:button('user-create', 'Create…'),
            html:button('user-drop', 'Drop', ('CHECK', 'CONFIRM'))
          )
          let $options := { 'link': 'user', 'sort': $sort }
          return html:table($headers, $entries, $buttons, {}, $options)
        }
      </form>
      <div>&#xa0;</div>
    </div>,
    <div class='panel'>
      <form method='post' autocomplete='off'>{
        <h2>User Information</h2>,
        <div class='buttons'>{ html:button('users-info', 'Update') }</div>,
        <textarea name='info' id='editor' spellcheck='false'>{
          serialize(user:info(), { 'indent': true() } )
        }</textarea>,
        html:js('loadCodeMirror("xml", true);')
      }</form>
    </div>
  ) => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
};
