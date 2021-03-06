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
 def update = evaluate readTrusted('shared/oscm-core.groovy')
 update.execute()
 `

 Required environment variables:

 Name: REPO_TAG_OSCM
 Type: String
 Default: master
 Description: Branch or tag in the oscm git repository: https://github.com/servicecatalog/oscm
 ===
 Name: REPO_TAG_DOCKERBUILD
 Type: String
 Default: master
 Description: Branch or tag in the oscm-dockerbuild git repository: https://github.com/servicecatalog/oscm-dockerbuild
 ===
 Name: ANT_URL
 Type: String
 Default: https://archive.apache.org/dist/ant/binaries/apache-ant-1.9.4-bin.tar.gz
 Description: URL to download ant from.
 **/

def execute() {

    def _cleanupWorkspace = {
        stage('Cleanup - prepare workspace') {
            sh "rm -rf ${WORKSPACE}/*"
        }
    }

    def _cloneOSCMRepository = {
        stage('Update - clone OSCM repository') {
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

    def _prepareDockerbuildRepository = {
        stage('Update - clone dockerbuild repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-dockerbuild"
            dir("${WORKSPACE}/oscm-dockerbuild") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_DOCKERBUILD}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-dockerbuild.git']]
                ]
            }
        }
    }

    def _setupAnt = {
        stage('Update - download and setup ant') {
            sh "curl ${ANT_URL} -x ${https_proxy}:${https_port} -o ${WORKSPACE}/apache-ant.tar.gz"
            sh "mkdir ${WORKSPACE}/apache-ant && tar -xf ${WORKSPACE}/apache-ant.tar.gz -C ${WORKSPACE}/apache-ant --strip-components 1"
            sh "wget https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.4.0/ivy-2.4.0.jar -O ${WORKSPACE}/apache-ant/lib/ivy.jar"
            sh "wget https://github.com/sass/dart-sass/releases/download/1.26.11/dart-sass-1.26.11-linux-x64.tar.gz -O ${WORKSPACE}/dart-sass-1.26.11-linux-x64.tar.gz"
            sh "mkdir ${WORKSPACE}/sass && tar -xf ${WORKSPACE}/dart-sass-1.26.11-linux-x64.tar.gz -C ${WORKSPACE}/sass --strip-components 1"

            ANT_BIN = sh(
                    script: "echo ${WORKSPACE}/apache-ant/bin/ant",
                    returnStdout: true
            ).trim()
            ANT_HOME = sh(
                    script: "echo ${WORKSPACE}/apache-ant",
                    returnStdout: true
            ).trim()
            ANT_OPTS = sh(
                    script: 'echo "${ANT_OPTS}"',
                    returnStdout: true
            ).trim()
            SASS_EXEC = sh (
                    script: "echo ${WORKSPACE}/sass",
                    returnStdout: true
            )
        }
    }

    def _setupJavaHome = {
        stage('Update - setup java home') {
            JAVA_HOME = sh(
                    script: "echo \$(dirname \$(dirname \$(readlink -f \$(which javac))))",
                    returnStdout: true
            ).trim()
        }
    }

    def _downloadLibraries = {
        stage('Update - download external libraries') {
            sh "export ANT_OPTS=\"${ANT_OPTS}\" && " +
                    "${ANT_BIN} -f ${WORKSPACE}/oscm-devruntime/javares/build-oscmaas.xml BUILD.LIB"
        }
    }

    def _compileCore = {
        withEnv(["PATH=/usr/local/dart-sass:${env.PATH}"]) {
            stage('Update - compile oscm-core') {
                sh "export ANT_OPTS=\"${ANT_OPTS}\" && " +
                        "export JAVA_HOME=\"${JAVA_HOME}\" && " +
                        "export PATH=\"${SASS_EXEC}\":${PATH} >> ~/.bashrc  && " +
                        "${ANT_BIN} -f ${WORKSPACE}/oscm-devruntime/javares/build-oscmaas.xml BUILD.BES"
            }
        }
    }

    def _updateCore = {
        stage('Update - update oscm-core') {
            sh "docker exec oscm-core rm -f /opt/apache-tomee/webapps/oscm-portal.war && " +
                    "docker exec oscm-core rm -rf /opt/apache-tomee/webapps/oscm-portal && " +
                    "docker exec oscm-core rm -f /opt/apache-tomee/apps/oscm.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-portal/oscm-portal.war oscm-core:/opt/apache-tomee/webapps/oscm-portal.war && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-ear/oscm.ear oscm-core:/opt/apache-tomee/apps/oscm.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-security/oscm-security.jar oscm-core:/opt/apache-tomee/lib/oscm-security.jar && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-common/oscm-common.jar oscm-core:/opt/apache-tomee/lib/oscm-common.jar && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-rest-api-common/oscm-rest-api-common.jar oscm-core:/opt/apache-tomee/lib/oscm-rest-api-common.jar && " +
                    "docker restart oscm-core"
        }
    }

    _cleanupWorkspace()
    _cloneOSCMRepository()
    _prepareDockerbuildRepository()
    _setupAnt()
    _setupJavaHome()
    _downloadLibraries()
    _compileCore()
    _updateCore()
}

return this
