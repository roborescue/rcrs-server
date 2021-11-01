#!/bin/sh

ps -ef | grep `cd .. && pwd` | awk '{print "kill -9", $2}' | sh >/dev/null 2>&1