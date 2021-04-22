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
 Name: MAVEN_URL
 Type: String
 Default: https://apache.osuosl.org/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz
 Description: URL to download maven from.
 **/

def execute() {

    def _cleanupWorkspace = {
        stage('Cleanup - prepare workspace') {
            sh "rm -rf ${WORKSPACE}/*"
        }
    }

    def _cloneOSCMAppShellRepository = {
        stage('Update - clone OSCM App Shell repository') {
            checkout scm: [
                    $class                           : 'GitSCM',
                    branches                         : [[name: "${REPO_TAG_OSCM_APP_SHELL}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions                       : [[$class : 'CloneOption',
                                                         noTags : false, reference: '',
                                                         shallow: true]],
                    submoduleCfg                     : [],
                    userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-app-shell.git']]
            ]
        }
    }

    def _setupMaven = {
        stage('Update - download and setup maven') {
            sh "curl ${MAVEN_URL} -x ${https_proxy}:${https_port} -o ${WORKSPACE}/apache-maven.tar.gz"
            sh "mkdir ${WORKSPACE}/apache-maven && tar -xf ${WORKSPACE}/apache-maven.tar.gz -C ${WORKSPACE}/apache-maven --strip-components 1"
            MAVEN_BIN = sh(
                    script: "echo ${WORKSPACE}/apache-maven/bin/mvn",
                    returnStdout: true
            ).trim()
            MAVEN_HOME = sh(
                    script: "echo ${WORKSPACE}/apache-maven",
                    returnStdout: true
            ).trim()
            MAVEN_OPTS = sh(
                    script: 'echo "-Dhttp.proxyHost=${http_proxy} -Dhttp.proxyPort=8080 -Dhttps.proxyHost=${https_proxy} -Dhttps.proxyPort=${https_port}"',
                    returnStdout: true
            ).trim()
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

    def _compileShell = {
        stage('Update - compile oscm-app-shell') {
            sh "export MAVEN_OPTS=\"${MAVEN_OPTS}\" && " +
                    "export JAVA_HOME=\"${JAVA_HOME}\" && " +
                    "${MAVEN_BIN} package"
        }
    }

    def _updateShell = {
        stage('Update - update oscm-app-shell') {
            sh "docker exec oscm-app rm -f /opt/apache-tomee/webapps/oscm-app-shell.war && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/webapps/oscm-app-shell && " +
                    "docker cp ${WORKSPACE}/target/oscm-app-shell.war oscm-app:/opt/apache-tomee/webapps/oscm-app-shell.war && " +
                    "docker restart oscm-app"
        }
    }

    _cleanupWorkspace()
    _cloneOSCMAppShellRepository()
    _setupMaven()
    _setupJavaHome()
    _compileShell()
    _updateShell()
}

return this
