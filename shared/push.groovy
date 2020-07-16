/**

 *****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 *****************************************************************************


 To import and use, add below code to jenkinsFile run by jenkins:
 `
 def push = evaluate readTrusted('shared/push.groovy')
 push.execute()
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

 Required environment variables if "loginRequired" parameter is set to true:
 ===
 Name: USERNAME
 Type: String
 Default: -
 Description: Username for the target <DOCKER_REGISTRY>
 ===
 Name: PASSWORD
 Type: String
 Default: -
 Description: Password for the target <DOCKER_REGISTRY>

 **/

def execute(boolean loginRequired = false, publish = false) {
    def srcRegistry = sh (
            script: 'if [ -n "$DOCKER_SRC_REGISTRY" ]; then echo $DOCKER_SRC_REGISTRY; else echo $DOCKER_REGISTRY; fi',
            returnStdout: true
    ).trim()

    def srcOrg = sh (
            script: 'if [ -n "$DOCKER_SRC_ORGANIZATION" ]; then echo $DOCKER_SRC_ORGANIZATION; else echo $DOCKER_ORGANIZATION; fi',
            returnStdout: true
    ).trim()

    def srcTag = sh (
            script: 'if [ -n "$DOCKER_SRC_TAG" ]; then echo $DOCKER_SRC_TAG; else echo $DOCKER_TAG; fi',
            returnStdout: true
    ).trim()
    
    def dstReg = sh (
            script: 'if [ -n "$DOCKER_DST_REGISTRY" ]; then echo $DOCKER_DST_REGISTRY; else echo $DOCKER_REGISTRY; fi',
            returnStdout: true
    ).trim()


    def dstOrg = sh (
            script: 'if [ -n "$DOCKER_DST_ORGANIZATION" ]; then echo $DOCKER_DST_ORGANIZATION; else echo $DOCKER_ORGANIZATION; fi',
            returnStdout: true
    ).trim()

    def dstTag = sh (
            script: 'if [ -n "$DOCKER_DST_TAG" ]; then echo $DOCKER_DST_TAG; else echo $DOCKER_TAG; fi',
            returnStdout: true
    ).trim()

    def _tagImages = {
        if(!publish) {
            stage('Push - tag local images') {
                sh('IMAGES="db initdb core app identity birt branding help deployer maildev proxy"; ' +
                        'for IMAGE in ${IMAGES}; do ' +
                        'docker tag oscm-${IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-${IMAGE}:${DOCKER_TAG}; ' +
                        'done'
                )
            }
        } else {
            stage('Push - retag images') {
                sh('IMAGES="db initdb core app identity birt branding help deployer maildev proxy"; ' +
                        'for IMAGE in ${IMAGES}; do ' +
                        'docker pull ' + "${srcRegistry}/${srcOrg}" + '/oscm-${IMAGE}:' + "${srcTag}; " +
                        'docker tag ' + "${srcRegistry}/${srcOrg}" + '/oscm-${IMAGE}:' + "${srcTag} ${dstReg}/${dstOrg}" + '/oscm-${IMAGE}:' + "${dstTag}; " +
                        'done'
                )
            }
        }
    }

    def _loginToDockerHub = {
        stage('Push - login to DockerHub') {
            sh 'docker login -u ${USERNAME} -p ${PASSWORD}'
        }
    }

    def _pushImages = {
        stage('Push - images to registry') {
            sh('IMAGES="db initdb core app identity birt branding help deployer maildev proxy"; ' +
                    'for IMAGE in ${IMAGES}; do ' +
                    "docker push " + (publish ? "" : '${DOCKER_REGISTRY}/') + "${dstOrg}/oscm-" + '${IMAGE}' + ":${dstTag}; " +
                    'done'
            )
        }
    }
    
    def _logoutFromDockerHub = {
        stage('Push - logout from DockerHub') {
            sh 'docker logout'
        }
    }

    _tagImages()

    if(loginRequired) {
        _loginToDockerHub()
    }

    _pushImages()
    
    if(loginRequired) {
        _logoutFromDockerHub()
    }
}

return this