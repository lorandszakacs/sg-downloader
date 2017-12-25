#!/bin/bash

# usage:
# ./docker-psql.sh               — is equivalent to the version with the start parameter
# ./docker-psql.sh start         — this has "createOrRun semantics". If the image does not exist then it created it. Otherwise it tries to start it. If it is already running, then it does nothing
# ./docker-psql.sh restart       — restarts already running container
# ./docker-psql.sh stop          — stops the container

# all important info is in this file here

# Official mongo base image:
DOCKER_BASE_IMAGE='mongo:latest'
RESULTING_CONTAINER_NAME='sgs-mongo'

EXPOSED_PORT=27016 #this is the port on the host machine; most likely you want to change this one.
INTERNAL_PORT=27017 #this is the default port on which postgresql starts on within the container.

LOCAL_DB_DATA_FOLDER=~/sgs/db

CMD_START='start'
CMD_RESTART='restart'
CMD_STOP='stop'

isRunning=$(docker inspect -f {{.State.Running}} $RESULTING_CONTAINER_NAME)

echo_commands() {
  echo "possible commands — all apply to docker image '$RESULTING_CONTAINER_NAME':"
  echo ""
  echo "  $CMD_START        -> starts a docker image, attemps creates it if it doesn't exist"
  echo "  $CMD_RESTART      -> restarts the existing docker image"
  echo "  $CMD_STOP         -> stops the image"
}

dockerStart() {
  docker container run --name $RESULTING_CONTAINER_NAME -v $LOCAL_DB_DATA_FOLDER:/data/db -d -p $EXPOSED_PORT:$INTERNAL_PORT $DOCKER_BASE_IMAGE
}

dockerRestart() {
  docker container restart $RESULTING_CONTAINER_NAME
}

dockerStop() {
  docker container stop $RESULTING_CONTAINER_NAME
}

dockerRemove() {
  docker rm $RESULTING_CONTAINER_NAME
}

if (( $# == 0 ));
then
  echo ""
  echo "no command line arguments specified. Defaulting to command: $CMD_START"
  echo ""
  echo_commands
  user_cmd="start"
else
  user_cmd="$1"
fi

if [ "$user_cmd" == "$CMD_START" ]
then
  if [ "$isRunning" == "true" ]
  then
    echo ""
    echo "container already started. doing nothing"
    echo ""
  elif [ "$isRunning" == "false" ]
  then
    echo ""
    echo "container exists; but is not running, starting up."
    echo ""
    dockerRestart
  else
    echo ""
    echo "container does not exist. creating"
    echo ""
    dockerStart
  fi #end "start"
elif [ "$user_cmd" == "$CMD_RESTART" ]
then
  echo "restarting container"
  dockerRestart
elif [ "$user_cmd" == "$CMD_STOP" ]
then
  echo "stopping docker container"
  dockerStop
else
  echo "unknown command: $user_cmd"
  echo_commands
fi
