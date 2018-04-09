#!/bin/bash

JVM_ARGS='-Xmx8G -Xms4G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xss64M -XX:+UseStringDeduplication -XX:+PrintStringDeduplicationStatistics'

#the path to the AWS S3 instance where to sync the files that are in $AWS_ROOT
AWS_CLOUD_LOC=$1

#the root folder where all the exports, and where the AWS folder lives
ROOT=~/sgs

#the ROOT location of the source code
REPO_LOCATION=~/workspace/lsz/sg-downloader

#the docker script used to start our mongodb instance, which is
#required by the $JAR_NAME
DOCKER_SCRIPT=$REPO_LOCATION/docker-mongo.sh

#the name of the executable jar that is run to do the harvesting
JAR_NAME=$REPO_LOCATION/sg-harvester/target/scala-2.12/sg-harvester.jar

#the folder which is the mirror of what's in AWS
AWS_ROOT=$ROOT/amazon-aws-s3

#we copy from the local folder, to the destination folder
SRC_FOLDER=$ROOT/local/models/
DEST_FOLDER=$AWS_ROOT/models/

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
phase "2" "running the SG harverster: 'java -jar $JAR_NAME delta'";
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


info "executing: 'java $JVM_ARGS -jar $JAR_NAME delta'";

java -jar $JVM_ARGS $JAR_NAME export-html;

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
phase "3" "closing docker image"
###############################################################################
###############################################################################

sh $DOCKER_SCRIPT stop;

###############################################################################
###############################################################################
phase "4" "running 'rsync -avhu $SRC_FOLDER $DEST_FOLDER'"
###############################################################################
###############################################################################

# http://unix.stackexchange.com/questions/149965/how-to-copy-merge-two-directories
# syncs everything from Source to Destination. The merged folder resides in Destination.
#
# -a means "archive" and copies everything recursively from source to destination preserving nearly everything.
#
# -v gives more output ("verbose").
#
# -h for human readable.
#
# --progress to show how much work is done.
#
# If you want only update the destination folder with newer files from source folder:
# rsync -avhu --progress source destination

#rsync -avhu $SRC_FOLDER $DEST_FOLDER;
rm -rf $DEST_FOLDER/*
mv $SRC_FOLDER/* $DEST_FOLDER/

###############################################################################
###############################################################################
phase "5" "uploading to AWS S3: cd $AWS_ROOT; aws s3 sync . $AWS_CLOUD_LOC"
###############################################################################
###############################################################################

cd $AWS_ROOT;
aws s3 sync . $AWS_CLOUD_LOC;

###############################################################################
###############################################################################
phase "6" "exiting"
###############################################################################
###############################################################################

cd $TEMP_CURR;

exit 0;
