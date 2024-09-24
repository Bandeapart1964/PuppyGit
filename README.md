<img src="screenshots/banner.png"/>


# PuppyGit Pro
PuppyGit Pro is A Git Client for Android, Open Source and No Ads and Free to use


## Download
<a href=https://github.com/Bandeapart1964/PuppyGitPro/releases>Github Release</a>


## Screenshots
<div>
<img src="screenshots/cl.png" width=150 />
<img src="screenshots/drawer.png"  width=150 />
<img src="screenshots/editor.png" width=150 />
<img src="screenshots/repos.png"  width=150  />
</div>


## Features
- fetch
- merge
- push
- files explorer
- simple file editor (only utf8 supported)
- commit history (git log)


## Untested features (already implemented, will release when test passed)
- shallow clone
- rebase
- cherry-pick
- patch
- reflog
- tags manage
- stash manage


## TODO (welcome pr)
- implement a settings page
- multi cherry-pick
- optimize Editor performance
- add colors and accept buttons for accept ours/theirs in Editor when merge mode on
- highlighting keyword when finding in Editor
- support more encoding for Editor(now only supported utf8)
- support submodules
- view commit history as a interactable tree


## About ssh
Idk how support ssh on Android, so PuppyGit Pro only support https for now, if you know how support ssh, please make a pr or give me some hints for that


## Build
import project to Android Studio, then build, thats all.


## Security
For reduce risk of password leak, Your passwords of git repos(e.g. github) are encrypted saved in PuppyGit's database on your device, the password of password encryptor is not open source for security reason, if you want build PuppyGit by yourself, you should update the password and version properly:
- update `EncryptorImpl.kt`, set your password encryptor, or simple use default encryptor
- set your encryptor version and password in `PassEncryptHelper.kt`, the password must enough to long and better don't make it be public

note: you should not change the passwords of old versions of password encryptors, if changed, when you upgrade app will can't decrypt the passwords encrypted by encryptor with old passwords, then all saved passwords on user device will be invalid, users must re-set passwords of credentials to right passwords and save them again.


## Help translate
now this app only support english, the english language file at src/main/res/values/strings.xml, you can translate it to your language, then send a pr


## Comments in codes
this project has many chinese comments, and some comments are out-of-date or nonsense, I have no plan clean them, but if you read the codes, and you wonder know some comments meaning, try translator or ask me is ok


## Donate
If this app has many users in future, maybe I'll make some ways to accept donations
