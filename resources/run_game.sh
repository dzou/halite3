#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 48 --height 48 \
"java -jar ../target/MyBot.jar" \
"java -jar bot_versions/alpha3.jar"
#"java -jar bot_versions/spawner.jar" \
#"java -jar bot_versions/hungarian.jar"

MOST_RECENT_REPLAY=$(ls -d $PWD/replays/*.hlt -t | head -n1)
electron ../../fluorine/ -o $MOST_RECENT_REPLAY &
