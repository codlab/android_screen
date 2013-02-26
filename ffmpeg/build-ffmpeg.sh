#!/bin/sh

WORKING_DIR=`pwd`
#you just get pw'n'd

PACKAGE="\/data\/data\/eu.codlab.screencast\/lib\/"

mkdir  $WORKING_DIR/final-builds

if [ "$NDK" = "" ]; then
	echo NDK required. example NDK=/home/user/android-ndk ./build-ffmpeg.sh
	exit
fi

# Download FFmpeg
curl http://sourceforge.net/p/servestream/code/1175/tree/ffmpeg/ffmpeg-0.11.1-android-2012-09-18.tar.gz?format=raw > ffmpeg-0.11.1-android-2012-09-18.tar.gz

# Unpackage the FFmpeg archive
tar -xvf ffmpeg-0.11.1-android-2012-09-18.tar.gz

# Prepare the FFmpeg archive for building
cd ffmpeg-0.11.1-android-2012-09-18

./extract.sh

#mods in it so replace the previous one TODO sed-ify it to prevent regression/bug
cp ../arm-build.sh ./

# Make the build scripts executable
chmod +x arm-build.sh

#sed -i "s/\/data\/data\/com.bambuser.broadcaster\/lib\//$PACKAGE/g" arm-build.sh

./arm-build.sh

cp -r build/ffmpeg/* $WORKING_DIR/final-builds

exit
