#! /bin/sh

#Check if Alliance.app exists
if [ -d "/Applications/Alliance.app/" ]; then

#if so, delete it
rm -r /Applications/Alliance.app/

fi

#Check if Alliance.app exists in 2nd location
if [ -d "/Users/$USER/Applications/Alliance.app/" ]; then

#if so, delete it
rm -r /Users/$USER/Applications/Alliance.app/

fi
