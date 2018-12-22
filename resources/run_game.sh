#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 64 --height 64 \
"java -jar ../target/MyBot.jar" \
"java -jar bot_versions/hungarian.jar" \
# "java -jar bot_versions/beta1.jar"
# "java -jar bot_versions/hungarian.jar"

# good seed 1544406607

MOST_RECENT_REPLAY=$(ls -d $PWD/replays/*.hlt -t | head -n1)
electron ../../fluorine/ -o $MOST_RECENT_REPLAY &
