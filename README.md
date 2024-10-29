<img src="screenshots/banner.png"/>


# PuppyGit
PuppyGit is A Git Client for Android, Open Source and No Ads and Free to use


## Author
PuppyGit made by Bandeapart1964 of catpuppyapp

### PuppyGit is a free app though, but if it helpful to you, please consider star this project and/or <a href=https://github.com/Bandeapart1964/PuppyGit/blob/main/donate.md>Donate</a>, it will help this project long live.



## Demo Video:
<a href=https://www.patreon.com/posts/puppygit-is-git-114679516>Clone A Repo</a> <br>
<a href=https://www.patreon.com/posts/obisidian-114681158>Obisdian + PuppyGit</a> <br>
<a href=https://www.patreon.com/posts/markor-puppygit-114681068>Markor + PuppyGit</a> <br>
<a href=https://www.patreon.com/posts/puppygit-now-and-114680923>Switch DarkMode and Chinese</a><br>


## Download
<a href=https://github.com/Bandeapart1964/PuppyGitPro/releases>Github Release</a>


## The Difference Of PuppyGit Pro and Non-Pro
TLDR; just old name and new name, no difference of features, all features are free to use for everyone<br><br>
Long:<br>
The "PuppyGit Pro" was an old name, but the icon's "Pro" text, I have no plan to remove it for now, but maybe I will update the icon in future though.<br><br>
More details if you want to know all about the story: in end of 2023, I was plan to create 2 apps, Pro for paid, another for free, but google play just broken my plan, the pro app, goole play staffs let me update the privacy policy in 1month, I done it with few days, then submit, but censoring was over 1month, then my app got banned, reason is no update the privacy policy(I updated, but the google censor too slow, so...), after few days, google staff tell me "Congratulation! your app privacy file now passed, but due to your app banned, so can't make your app passed the censoring", how stupid..., then I send appeal email to google, but google reject my unban request. and, my free version app got banned too, just because I used the Git icon, google play staffs tell me I have no right use it and I am mislead people think my app is create by git official, then banned my free app too, ok, well, then even I removed the Git logo from PuppyGit icon, and re-submit a new version, still no one response till now, after 2 apps banned, then my google developer account got banned by too much apps banned, so google think I am a bad developer(wtf), then I appeal, but google play keep it's decision, finally, I give up talk to google play, I make the app free and open source, but I accept donations.


## PuppyGit + Notes App = A Free Cross-platform Sync Way!
Not only sync codes, You can use PuppyGit sync your notes repository create by note-taking apps like: <a href="https://github.com/obsidianmd/obsidian-releases">Obsidian</a> / <a href="https://github.com/gsantner/markor">Markor</a> or Other!

## 2fa
If your github/gitlab or other platforms account enabled 2fa, you may need create a personal access token instead your password

see:<br>
<a href=https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token>github create personal access token</a><br>
<a href=https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html#create-a-personal-access-token>gitlab create personal access token</a>


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
- shallow clone
- rebase
- cherry-pick
- patch
- reflog
- tags manage
- stash manage
- submodules manage


## About ssh
Idk how support ssh on Android, so PuppyGit only support https for now, if you know how support ssh, please make a pr or give me some hints for that


## Build
import project to Android Studio, then build, that's all. (android NDK is required)
<br><br>
### if you want to build libs by your self:<br>
require:<br>
- jdk 1.8 or later (oracle or openjdk or adoptjdk, should all worked as expect)
- <a href=https://github.com/android/ndk/wiki/Changelog-r26#r26b>android ndk r26b</a>, and target abi is 'android21'
<br>
Then you can build every lib from source code, the source code of the libs, see [Libs](#Libs)


## Security
For reduce risk of password leak, Your passwords of git repos(e.g. github) are encrypted saved in PuppyGit's database on your device, the password of password encryptor is not in this open source repo for security reason, if you want build PuppyGit by yourself, you better update the password and version to yours and dont share it for more safe (just better, not enforce and necessary):
- update `EncryptorImpl.kt`, set your password encryptor, or simple use default encryptor
- set your encryptor version and password in `PassEncryptHelper.kt`, the password must enough to long and better don't make it be public

note: you should not change the passwords of old versions of password encryptors, if changed, when you upgrade app will can't decrypt the passwords encrypted by encryptor with old passwords, then all saved passwords on user device will be invalid, users must delete all credentials, then restart app, then re create credentials again.


## Help translate
1. Download <a href="https://github.com/Bandeapart1964/PuppyGit/blob/main/app/src/main/res/values/strings.xml">strings.xml</a>
2. Translate the file's values to your language, e.g.```<help>help translate</help>```to```<help>帮助翻译</help>```
3. Create a issue attaching the file you was translated, the issue should tell which language you traslated to

Then I'll add your language into PuppyGit in furthur version

*NOTE: the string in strings.xml like "ph_a3f241dc_NUMBER" are place holders, the last NUMBER is order, e.g. a string resource ```<str1>name: ph_a3f241dc_1, age: ph_a3f241dc_2</str1>```, will replaced when running, it maybe will show as: ```"name: abc, age: 123"```, if you have mistake with the order number, e.g.```<str1>name: ph_a3f241dc_2, age: ph_a3f241dc_1</str1>```, it may cause app show wrong text like ```"name: 123, age: abc"```


## Comments in code
this project has many chinese comments, and some comments are out-of-date or nonsense, I have no plan clean them, but if you read the codes, and you wonder know some comments meaning, try translator or ask me is ok


## Credit
### Logo
The PuppyGit logo designed by Bandeapart1964(myself)<br>
The Git Logo in PuppyGit logo is created by Jason Long, is licensed under the Creative Commons Attribution 3.0 Unported License. (<a href=https://git-scm.com/downloads/logos>The Git Logo</a>)<br>

### Libs
The libgit2.so built from <a href=https://github.com/libgit2/libgit2/releases/tag/v1.7.2>libgit2 1.7.2</a>(<a href=https://raw.githubusercontent.com/libgit2/libgit2/main/COPYING>LICENSE</a>)<br>
The libgit24j.so and git24j-1.0.2.20241022.jar  built from a fork of <a href=https://github.com/git24j/git24j>Git24j</a>, the fork link is <a href=https://github.com/Frank997/git24j/tree/1.0.3.20241022-ready-pr>Here</a>(<a href=https://raw.githubusercontent.com/git24j/git24j/master/LICENSE>LICENSE</a>)<br>
The libcrypto.so and libssl.so built from <a href=https://github.com/openssl/openssl/releases/tag/openssl-3.3.0>openssl 3.3.0</a>(<a href=https://raw.githubusercontent.com/openssl/openssl/master/LICENSE.txt>LICENSE</a>)<br>
The Editor of PuppyGit is modified from kaleidot725's <a href=https://github.com/kaleidot725/text-editor-compose>text-editor-compose</a>(<a href=https://raw.githubusercontent.com/kaleidot725/text-editor-compose/main/LICENSE>LICENSE</a>)
