/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 19-11-2020                                                 *
 *                                                                           *
 ****************************************************************************/
 
void execute() {
    def _prepareBuildTools = {
        stage('Build - pull build tools') {
             docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-gc-ant:${DOCKER_TAG}").pull()
             docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-centos-based:${DOCKER_TAG}").pull()
             docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-maven:${DOCKER_TAG}").pull()
             docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-gf:${DOCKER_TAG}").pull()
             sh(
                'docker tag ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-gc-ant:${DOCKER_TAG} gc-ant; ' +
                'docker tag ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-centos-based:${DOCKER_TAG} oscm-centos-based; ' +
                'docker tag ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-gf:${DOCKER_TAG} oscm-gf; ' +
                'docker tag ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-maven:${DOCKER_TAG} oscm-maven; ' 
            )
        }
    }
    
        def _downloadLibraries = {
        stage('Build - download external libraries') {
            sh "docker run " +
                    "--name gc-ant-ivy-${BUILD_ID} " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    "-e http_proxy=\"${http_proxy}\" " +
                    "-e https_proxy=\"${http_proxy}\" " +
                    "-e HTTP_PROXY=\"${http_proxy}\" " +
                    "-e HTTPS_PROXY=\"${http_proxy}\" " +
                    "-e ANT_OPTS=\"${ANT_OPTS} \" " +
                    "gc-ant -f /build/oscm-devruntime/javares/build-oscmaas.xml BUILD.LIB"
        }
    }
    
        def _compileCore = {
        stage('Build - compile oscm-core') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name gc-ant-core-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    "-e http_proxy=\"${http_proxy}\" " +
                    "-e https_proxy=\"${https_proxy}\" " +
                    "-e HTTP_PROXY=\"${http_proxy}\" " +
                    "-e HTTPS_PROXY=\"${https_proxy}\" " +
                    "-e ANT_OPTS=\"${ANT_OPTS} \" " +
                    "-e PATH=/usr/local/dart-sass:${env.PATH} " +
                    "gc-ant -f /build/oscm-devruntime/javares/build-oscmaas.xml BUILD.BES"
        }
    }
    _prepareBuildTools()
    _downloadLibraries()
    _compileCore()
}
return this