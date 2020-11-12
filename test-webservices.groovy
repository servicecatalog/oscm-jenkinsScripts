/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/

// How to use:
// 1. Create a job of type Pipeline
// 2. As the pipeline script paste this script
// 3. Create the following string parameters (adjust defaults if you wish):
// --
// Name: DOCKER_REGISTRY
// Default: artifactory.intern.est.fujitsu.com:5002
// Description: Registry host and port for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: DOCKER_ORGANIZATION
// Default: oscmdocker
// Description: Organization name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: TAG_REPO_JENKINS_SCRIPTS
// Default: master
// Description: http://estscm1.intern.est.fujitsu.com/fujitsu-bss/jenkinsScripts
// --
// Name: TAG_REPO_DEVELOPMENT
// Default: master
// Description: Branch or tag in the git repository development
// --
// Name: TAG_REPO_DOCKERBUILD
// Default: master
// Description: Branch or tag in the git repository oscmaas-dockerbuild
// --
// Name: TAG_REPO_DOCUMENTATION
// Default: master
// Description: Branch or tag in the git repository documentation https://github.com/servicecatalog/documentation
// --
// Name: DOCKER_ORGANIZATION
// Default: oscmdocker
// Description: Project name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: TAG_DOCKER
// Default: ws-tests
// Description: Docker tag name for the built images
// --
// Name: ANT_URL
// Default: https://archive.apache.org/dist/ant/binaries/apache-ant-1.9.4-bin.tar.gz
// Description: URL to download ant from.
// --
// Name: IVY_URL
// Default: http://www-eu.apache.org/dist//ant/ivy/2.4.0/apache-ivy-2.4.0-bin.tar.gz
// Description: URL to download ivy from.
// --
// Name: OSCM_GIT_REPO
// Default: https://github.com/servicecatalog/oscm.git
// Description: Repository URL to download the oscm from.
// --
// Name: JAVA_HOME_DIR
// Default: /etc/alternatives/java_sdk_1.8.0
// Description: Path to the JDK directory.
//
// Create the following Node parameter:
// --
// Name: NODE_NAME
// Select the default nodes and possible nodes for your environment
// Enable "Disallow multi node selection"
// Node eligibility: All nodes
// Description: Where to set up the OSCM?
//
// Create the following Boolean parameters:
// --
// Name: PURGE_DATABASE
// Default: disabled
// Description: Delete the database and start with a fresh one?
// --
// Name: TOMEE_DEBUG
// Default: disabled

// --
// Name: SAMPLE_DATA
// Default: disabled
// Description: Load sample data?

node("${NODE_NAME}") {

    def _cloneOSCMRepository = {
        stage('Build - checkout tests') {
            checkout scm: [
                    $class                           : 'GitSCM',
                    branches                         : [[name: "${REPO_TAG_OSCM}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions                       : [[$class : 'CloneOption',
                                                         noTags : false, reference: '',
                                                         shallow: true]],
                    submoduleCfg                     : [],
                    userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm.git']]
            ]
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
                    "-e ANT_OPTS=\"-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080\" " +
                    "-e PATH=/usr/local/dart-sass:${env.PATH} " +
                    "gc-ant -f /build/oscm-devruntime/javares/build-oscmaas.xml BUILD.BES"
        }
    }
    
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
    
    def clean = evaluate readTrusted('shared/cleanup.groovy')
    def pull = evaluate readTrusted('shared/pull.groovy')
    def start = evaluate readTrusted('shared/start.groovy')
    def test = evaluate readTrusted('shared/test-webservices.groovy')

    clean.execute()
    pull.execute()
    _prepareBuildTools()
    _cloneOSCMRepository()
    _compileCore()
    start.execute()
    test.execute()
    clean.execute()
}
