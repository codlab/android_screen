android_screen
==============
Help you to record your screen


1. specify the file where to save the result
2. click the go button (if you have the Nexus 7, only the specific Nexus 7 button will make a movie :) )
3. when it is finished, click on the notification to finish the video
4. the video is now in the file specified in hte main screen

Compiling ffmpeg
================

0. if you want to modify the precompiled binary version in the assets folder only!
1. on ubuntu run the build-ffmpeg.sh script
2. copy the ffmpeg standalone binary file to the assets


Compiling the app
=================

1. in eclipse import every projects
2. in the android properties of sliding-menu, verify that it has a reference on actionbar (right clic > properties)
3. in the android properties of the app itself, verify that it has a reference on the sliding-menu project
4. the same with project references in the list where you have android


Requirements
============

- /system/xbin/killall
- /system/bin/mount
- /system/bin/cp

well busybox :0)

LOTS of feature are going to be released soon :)


Known issues
============

Nexus 7... so I created the png frame mode
>> it records into the cache folder frames and then, uses ffmpeg to create the final file.

Please reports any other issue


Finally
=======

you like this app? feel free to make a little donation /o/
https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XXDATCYFK7SFJ
