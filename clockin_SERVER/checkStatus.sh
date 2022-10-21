#!/bin/zsh

pid=$(cat pid)
res=$(ps -el | grep "$pid")
if [[ -z "$res" ]]
then
    echo "程序不在运行"
else
    echo "$res"
fi