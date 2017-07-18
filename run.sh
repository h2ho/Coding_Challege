#!/bin/sh


unset JAVA_TOOL_OPTIONS
export JAVA_HOME=/usr/lib/jvm/java-1.8.0
JAVA_CC=$JAVA_HOME/bin/javac

export CLASSPATH=".:src/"

echo --- Cleaning
rm -f *.class
rm -f src/*.class


echo --- Compiling Java
$JAVA_CC -version
$JAVA_CC src/*.java

$JAVA_HOME/bin/java Tweet tweet_input/tweets.txt tweet_output/ft1.txt
$JAVA_HOME/bin/java AverageDeg tweet_output/ft1.txt tweet_output/ft2.txt
