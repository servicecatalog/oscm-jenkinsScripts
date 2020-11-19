/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/

/**
 To import and use, add below code to jenkinsFile run by jenkins:
 `
 def clean = evaluate readTrusted('shared/cleanup.groovy')
 clean.execute()
 `

 Required environment variables:

 Name: DOCKER_TAG
 Type: String
 Default: latest
 Description: Docker tag name for created images
 ===
 Name: DOCKER_REGISTRY
 Type: String
 Default: artifactory.intern.est.fujitsu.com:5003
 Description: Registry host and port for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: DOCKER_ORGANIZATION
 Type: String
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: COMPLETE_CLEANUP
 Default: enabled
 Description: Delete the database and whole workspace. It should be done before builds.
 ===
 **/

void execute() {
    def _stopContainers = {
        stage('Cleanup - stop running containers') {
            sh "docker stop \$(docker ps -aq) || true"
            sh "docker rm \$(docker ps -aq) || true"
        }
    }

    def _cleanupContainers = {
        stage('Cleanup - docker compose') {
            sh '''
            if [ -f ${WORKSPACE}/docker/docker-compose-oscm.yml ]; then
                if [ $(docker-compose -f ${WORKSPACE}/docker/docker-compose-oscm.yml ps -q | wc -l) != "0" ]; then
                    docker-compose -f ${WORKSPACE}/docker/docker-compose-oscm.yml stop;
                    docker-compose -f ${WORKSPACE}/docker/docker-compose-oscm.yml rm -f;
                fi;
            fi
            '''
        }
    }

    def _pruneUnusedContainers = {
        stage('Cleanup - prune unused images') {
            sh 'docker image prune -a -f --filter "label!=DO_NOT_DELETE=true"'
        }
    }

    def _pruneVolumes = {
        stage('Cleanup - prune volumes') {
            sh 'docker volume prune -f'
        }
    }

    def _cleanupWorkspace = {
        stage('Cleanup - prepare workspace') {
            sh '''
            mkdir -p ${WORKSPACE}
            if [ ${COMPLETE_CLEANUP} == "true" ]; then
                docker run --rm -v ${WORKSPACE}/docker/data/oscm-db:/db busybox rm -rf /db/data || true;
                docker run --rm -v ${WORKSPACE}:/workspace centos:7 find /workspace -uid 0 -delete
                rm -rf ${WORKSPACE}/*
                rm -rf ${WORKSPACE}/{,.[!.],..?}*
                mkdir ${WORKSPACE}/docker
            else
                if [ ! -d ${WORKSPACE}/docker ]; then
                    mkdir ${WORKSPACE}/docker;
                else
                    docker run --rm -v ${WORKSPACE}/docker:/docker busybox  mv /docker/var.env "/docker/var.env.$(date -Iseconds)"     
                    docker run --rm -v ${WORKSPACE}/docker:/docker busybox mv /docker/.env "/docker/.env.$(date -Iseconds)"
                fi;
            fi;
            '''
        }
    }

    def _cleanupLocalImages = {
        stage('Cleanup - local images') {
            sh('docker rmi gc-ant || true')
            sh("docker rmi \$(docker images | grep -P '(?!.*sles)oscm-' | tr -s ' ' | awk '{ print \$1\":\"\$2}') || true")
        }
    }

    def _cleanupUntaggedImages = {
        stage('Cleanup - untagged images') {
            sh 'docker rmi $(docker images | grep "<none>" | awk "{print $3}") || true'
        }
    }

    _stopContainers()
    _cleanupContainers()
    _pruneUnusedContainers()
    _pruneVolumes()
    _cleanupWorkspace()
    _cleanupLocalImages()
    _cleanupUntaggedImages()
}

return this