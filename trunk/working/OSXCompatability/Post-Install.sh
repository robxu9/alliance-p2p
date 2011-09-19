#! /bin/sh

# Checks if Volume then Applications (OSX Lion)
if [ -d "/Applications/data/" ]; then

# Delete data folder in App incase it already exists   
rm -r /Applications/Alliance.app/Contents/Resources/Javadata/

# Make data folder inside Alliance App.
mkdir -p /Applications/Alliance.app/Contents/Resources/Javadata/

# Move Data files to newest release
mv /Applications/data/settings.xml* /Applications/Alliance.app/Contents/Resources/Javadata/

# Delete Alliance 1.0.6 data
rm -rf /Applications/cache/
rm -rf /Applications/data/
rm -rf /Applications/logs/
rm -rf /Applications/downloads/

fi

# Checks if Home directory then Applications (Previous to OSX Lion)
if [ -d "/Users/$USER/Applications/data/" ]; then

# Delete data folder in App incase it already exists    
rm -r /Users/$USER/Applications/Alliance.app/Contents/Resources/Javadata/

# Make data folder inside Alliance App.
mkdir -p /Users/$USER/Applications/Alliance.app/Contents/Resources/Javadata/

# Move Data files to newest release
mv /Users/$USER/Applications/data/settings.xml* /Users/$USER/Applications/Alliance.app/Contents/Resources/Javadata/

# Delete Alliance 1.0.6 data
rm -rf /Users/$USER/Applications/cache/
rm -rf /Users/$USER/Applications/data/
rm -rf /Users/$USER/Applications/logs/
rm -rf /Users/$USER/Applications/downloads/

fi

exit
