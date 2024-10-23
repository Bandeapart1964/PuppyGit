## TODO (welcome pr)
- multi cherry-pick
- optimize Editor performance
- Editor merge mode: add colors for ours/theirs blocks
- Editor merge mode: add accept ours/theirs buttons at conflict block (expect like vscode style)
- highlighting keyword when finding in Editor
- support more encoding for Editor(now only supported utf8)
- view commit history as a interactable tree
- view history of file(plan: add menu item in Files page single item menu, and Editor page top bar menu. if file is not in a git repo, show a toast)
- Files: support more order methods(order by name, order by date, desc, asc, etc), and each path can have difference order method(path and order method should save to a single config file like file opend history for avoid settings file oversized)
- Files: support override or rename for copy/move when target and src have same name
- ChangeList: support group by folder(try use different status options, if good, no need group by myself)(should expend when clicked a folder, if it is not a submodule or git repo. and can view diff content when clicked a file under a folder. and should make sure it can't stage a folder when it is a git repo under other repo but isn't a submodule)
- ChangeList: support show/hide submodules(try different status opts, if good, no need implement by myself)
- ChangeList: support show folder first(order method)
- add go to bottom for every go to top fab(column layout, upside icon, clicked go to top, bottom icon, clicked go to bottom)
- signed commits (by gpg key)
- support ssh
- git blame
- optional: can disable auto loading when go to ChangeList (for avoid this case: go to ChangeList, but the repo is not you want, and page loading, you can't switch repo...stucked... but actually, have workaround, go to Files or Repos page can direct show ChangeList of selected repo)

