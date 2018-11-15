#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 32 --height 32 --seed 1541971322 "java -jar ../target/MyBot.jar" "java -jar bot_versions/random_bot.jar"
MOST_RECENT_REPLAY=$(ls -d $PWD/replays/*.hlt -t | head -n1)
electron ../../fluorine/ -o $MOST_RECENT_REPLAY &
