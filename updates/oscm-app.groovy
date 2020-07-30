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
 def update = evaluate readTrusted('shared/oscm-app.groovy')
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
            sh "curl ${ANT_URL} -x http://proxy.intern.est.fujitsu.com:8080 -o ${WORKSPACE}/apache-ant.tar.gz"
            sh "mkdir ${WORKSPACE}/apache-ant && tar -xf ${WORKSPACE}/apache-ant.tar.gz -C ${WORKSPACE}/apache-ant --strip-components 1"
            sh "wget https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.4.0/ivy-2.4.0.jar -O ${WORKSPACE}/apache-ant/lib/ivy.jar"
            ANT_BIN = sh(
                    script: "echo ${WORKSPACE}/apache-ant/bin/ant",
                    returnStdout: true
            ).trim()
            ANT_HOME = sh(
                    script: "echo ${WORKSPACE}/apache-ant",
                    returnStdout: true
            ).trim()
            ANT_OPTS = sh(
                    script: 'echo "-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"',
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

    def _downloadLibraries = {
        stage('Update - download external libraries') {
            sh "export ANT_OPTS=\"${ANT_OPTS}\" && " +
                    "${ANT_BIN} -f ${WORKSPACE}/oscm-devruntime/javares/build-oscmaas.xml BUILD.LIB"
        }
    }

    def _compileApp = {
        stage('Update - compile oscm-app') {
            sh "export ANT_OPTS=\"${ANT_OPTS}\" && " +
                    "export JAVA_HOME=\"${JAVA_HOME}\" && " +
                    "${ANT_BIN} -f ${WORKSPACE}/oscm-devruntime/javares/build-oscmaas.xml BUILD.APP"

        }
    }

    def _updateApp = {
        stage('Update - update oscm-app') {
            sh "docker exec oscm-app rm -f /opt/apache-tomee/apps/oscm-app.ear && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/apps/oscm-app && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/apps/oscm-app-startup.war && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/apps/oscm-app-startup && " +
                    "docker exec oscm-app rm -f /opt/apache-tomee/controllers/oscm-app-aws.ear && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/controllers/oscm-app-aws && " +
                    "docker exec oscm-app rm -f /opt/apache-tomee/controllers/oscm-app-azure.ear && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/controllers/oscm-app-azure && " +
                    "docker exec oscm-app rm -f /opt/apache-tomee/controllers/oscm-app-openstack.ear && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/controllers/oscm-app-openstack && " +
                    "docker exec oscm-app rm -f /opt/apache-tomee/controllers/oscm-app-vmware.ear && " +
                    "docker exec oscm-app rm -rf /opt/apache-tomee/controllers/oscm-app-vmware && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-ear/oscm-app.ear oscm-app:/opt/apache-tomee/apps/oscm-app.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-startup/oscm-app-startup.war oscm-app:/opt/apache-tomee/apps/oscm-app-startup.war && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-aws/oscm-app-aws.ear oscm-app:/opt/apache-tomee/controllers/oscm-app-aws.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-azure/oscm-app-azure.ear oscm-app:/opt/apache-tomee/controllers/oscm-app-azure.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-openstack/oscm-app-openstack.ear oscm-app:/opt/apache-tomee/controllers/oscm-app-openstack.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-vmware-ear/oscm-app-vmware.ear oscm-app:/opt/apache-tomee/controllers/oscm-app-vmware.ear && " +
                    "docker cp ${WORKSPACE}/oscm-build/result/package/oscm-app-extsvc-2-0/oscm-app-extsvc-2-0.jar oscm-app:/opt/apache-tomee/lib/oscm-app-extsvc-2-0.jar && " +
                    "docker restart oscm-app"
        }
    }

    _cleanupWorkspace()
    _cloneOSCMRepository()
    _prepareDockerbuildRepository()
    _setupAnt()
    _setupJavaHome()
    _downloadLibraries()
    _compileApp()
    _updateApp()
}

return this
