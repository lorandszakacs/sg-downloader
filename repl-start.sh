#!/bin/bash

MEMORY='-Xmx8G -Xms4G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xss64M'

#the root folder where all the exports, and where the AWS folder lives
ROOT=~/sgs

#the ROOT location of the source code
REPO_LOCATION=~/workspace/lsz/sg-downloader

#the docker script used to start our mongodb instance, which is
#required by the $JAR_NAME
DOCKER_SCRIPT=$REPO_LOCATION/docker-mongo.sh

#the name of the executable jar that is run to do the harvesting
JAR_NAME=$REPO_LOCATION/sg-harvester/target/scala-2.13/sg-harvester.jar

#we copy from the local folder, to the destination folder
SRC_FOLDER=$ROOT/local/models/

#the current folder, we return to it at the end of the program
TEMP_CURR=${pwd}

phase() {
  echo ""
  echo "-----------------------------------------------------------------------"
  echo "-- starting phase #$1 ---- $2"
  echo "-----------------------------------------------------------------------"
  echo ""
}

failedTo() {
  echo ""
  echo "*********"
  echo "failed to $1 — exiting";
  echo "*********"
  echo ""
  exit 1;
}

warning() {
  echo ""
  echo "*********"
  echo "-- warning: $1";
  echo "*********"
  echo ""
}

info() {
  echo ""
  echo "-- $1"
  echo ""
}
###############################################################################
###############################################################################
phase "0" "docker daemon"
###############################################################################
###############################################################################

[[ $(uname) == 'Darwin' ]] || { echo "This function only runs on macOS." >&2; exit 2; }

info "Starting Docker.app, if necessary..."

open -g -a Docker.app || exit

# Wait for the server to start up, if applicable.
i=0
while ! docker system info &>/dev/null; do
  (( i++ == 0 )) && printf %s '-- Waiting for Docker to finish starting up...' || printf '.'
  sleep 1
done
(( i )) && printf '\n'

info "-- Docker is ready.";

###############################################################################
###############################################################################
phase "1" "docker image via script: '$DOCKER_SCRIPT start'";
###############################################################################
###############################################################################

sh $DOCKER_SCRIPT start;

###############################################################################
###############################################################################
phase "2" "running the SG harverster: 'java -jar $JAR_NAME'";
###############################################################################
###############################################################################

if [ ! -f $JAR_NAME ];
then
  warning "jar: $JAR_NAME does not exist, attempting to create —— 'cd $REPO_LOCATION; sbt mkJar'";

  cd $REPO_LOCATION;
  sbt mkJar;

  if [ $? -eq 0 ]
  then
    echo "";
  else
    info "cleaning up"
    sh $DOCKER_SCRIPT stop;
    echo ""
    failedTo "do 'sbt mkJar' — nothing to execute";
  fi
fi


info "executing: 'java $MEMORY -jar $JAR_NAME'";

java -jar $MEMORY $JAR_NAME;

if [ $? -eq 0 ]
then
  echo ""
else
  info "cleaning up"
  sh $DOCKER_SCRIPT stop;
  echo ""
  failedTo "execute delta harvest"
fi

###############################################################################
###############################################################################
phase "3" "exiting"
###############################################################################
###############################################################################

cd $TEMP_CURR;

exit 0;
