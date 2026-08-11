(:~
 : Users: the registered users and what each of them may do.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace panels = 'dba/lib/user-panels' at 'panels.xqm';
import module namespace user-info = 'dba/lib/user-info' at 'user-info.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'users';

(:~
 : Users: the users to choose from, and the selected one. The selection is part of the address,
 : so a link reproduces what the panels show.
 : @param  $name     selected user
 : @param  $newname  name that was entered but could not be assigned
 : @param  $perm     permission that was entered but could not be assigned
 : @param  $sort     table sort key
 : @param  $info     info string
 : @param  $error    error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/users')
  %rest:query-param('name',    '{$name}', '')
  %rest:query-param('newname', '{$newname}')
  %rest:query-param('perm',    '{$perm}')
  %rest:query-param('sort',    '{$sort}', '')
  %rest:query-param('info',    '{$info}')
  %rest:query-param('error',   '{$error}')
  %output:method('html')
function dba:users(
  $name     as xs:string,
  $newname  as xs:string?,
  $perm     as xs:string?,
  $sort     as xs:string,
  $info     as xs:string?,
  $error    as xs:string?
) as element(html) {
  let $user := panels:user($name, $newname, $perm)
  let $permissions := panels:local-permissions($name)
  (: the panels follow the selection, and are not remembered: what is attached to no user in
     particular steps back once one of them is being looked at :)
  let $fold := ' collapsed'[$user]
  return (
    <div class='panel' data-label='Users'>
      <div class='pane'>{ panels:users($sort, $name) }</div>
    </div>,
    (: the form is the pane: its editor takes the height that the fields leave :)
    <div class='panel{ ' hidden'[empty($user)] }' data-label='User'>{ $user }</div>,
    <div class='panel{ ' hidden'[empty($permissions)] }' data-label='Permissions'>
      <div class='pane'>{ $permissions }</div>
    </div>,
    <div class='panel{ $fold }' data-label='Information'>{ panels:information() }</div>
  ) => html:wrap({
    'header' : $dba:CAT,
    (: the information panel is only open while no user is shown, where it is one of two
       panels: its share is what the two of them then split :)
    'columns': ('25fr', '35fr', '25fr', '25fr'),
    'rows'   : '1fr',
    'panels' : 'auto',
    'scripts': ('cm6', 'editor'),
    (: both information fields are edited as XML; the selected user's is the one of record :)
    'init'   : 'loadCodeMirror("xml", [ "editor", "user-info" ]);',
    'info'   : $info,
    'error'  : $error
  })
};

(:~
 : Runs a user action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/users/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($action, {
    'create': fn($args) { {
      'page'  : $dba:CAT,
      'params': { 'name': $args?name },
      'info'  : `User "{ $args?name }" was created.`,
      'run'   : %updating fn() {
        if (user:exists($args?name)) {
          error((), 'User already exists.')
        } else {
          user:create($args?name, $args?pw, $args?perm)
        }
      }
    } },
    'drop': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'user', 'dropped'),
      'run' : %updating fn() { $args?name ! user:drop(.) }
    } },
    'update': fn($args) {
      let $name := string($args?name)
      let $newname := string($args?newname)
      (: a name that is taken is the one error that can be foreseen; it leaves the user where
         it was, so the panel keeps showing the one that was being edited :)
      let $taken := $newname != $name and user:exists($newname)
      return {
        'page'  : $dba:CAT,
        (: the password is deliberately not carried back: it would end up in the address bar,
           in the browser history and in the log :)
        'params': {
          'name': if ($taken) then $name else $newname,
          'newname': $newname,
          'perm': $args?perm
        },
        'info'  : 'User was updated.',
        'run'   : %updating fn() {
          if ($taken) {
            error((), 'User already exists.')
          } else if ($name != $newname) {
            user:alter($name, $newname)
          },
          (: an empty field leaves the password as it is :)
          if ($args?pw) { user:password($name, $args?pw) },
          if ($args?perm != user:list-details($name)/@permission) {
            user:grant($name, $args?perm)
          },
          let $xml := user-info:parse($args?info)
          where not(deep-equal(user:info($name), $xml))
          return user:update-info($xml, $name)
        }
      }
    },
    'info': fn($args) { {
      'page': $dba:CAT,
      'info': 'User information was updated.',
      'run' : %updating fn() {
        let $xml := user-info:parse($args?info)
        where not(deep-equal(user:info(), $xml))
        return user:update-info($xml)
      }
    } },
    'pattern-add': fn($args) { {
      'page'  : $dba:CAT,
      'params': { 'name': $args?name },
      'info'  : `Pattern "{ $args?pattern }" was created.`,
      'run'   : %updating fn() {
        user:grant($args?name, $args?perm, $args?pattern)
      }
    } },
    'pattern-drop': fn($args) { {
      'page'  : $dba:CAT,
      'params': { 'name': $args?name },
      'info'  : utils:info($args?pattern, 'pattern', 'dropped'),
      'run'   : %updating fn() { $args?pattern ! user:drop($args?name, .) }
    } }
  })
};
