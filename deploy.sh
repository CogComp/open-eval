#!/bin/bash

echo "A deploy is being lunched now on port 9000 of the following ip:  " | /shared/shelley/khashab2/slacktee/slacktee.sh

ifconfig eth0 | grep inet | awk '{print $2}' | cut -d':' -f2  | /shared/shelley/khashab2/slacktee/slacktee.sh

git pull | /shared/shelley/khashab2/slacktee/slacktee.sh

sbt -java-home /shared/shelley/khashab2/java/jdk1.8.0_31  'project core' run | /shared/shelley/khashab2/slacktee/slacktee.sh  
