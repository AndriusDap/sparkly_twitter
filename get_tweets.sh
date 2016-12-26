#!/bin/zsh
twurl -H stream.twitter.com /1.1/statuses/sample.json 2> /dev/null | head -n 100000 > tweets.json
