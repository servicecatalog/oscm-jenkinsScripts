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
 def build = evaluate readTrusted('shared/build.groovy')
 build.execute()
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
 Name: REPO_TAG_OSCM_APP
 Type: String
 Default: master
 Description: Branch or tag in the oscm-app git repository: https://github.com/servicecatalog/oscm-app
 ===
 Name: REPO_TAG_APP_SHELL
 Type: String
 Default: master
 Description: Branch or tag in the oscm-app-shell git repository: https://github.com/servicecatalog/oscm-app-shell.git
 ===
 Name: REPO_TAG_APPROVAL
 Type: String
 Default: master
 Description: Branch or tag in the oscm-approval git repository: https://github.com/servicecatalog/oscm-approval
 ===
 Name: REPO_TAG_IDENTITY
 Type: String
 Default: master
 Description: Branch or tag in the oscm-identity git repository: https://github.com/servicecatalog/oscm-idenity.git
 ===
 Name: REPO_TAG_DOCUMENTATION
 Type: String
 Default: master
 Description: Branch or tag in the documentation git repository" https://github.com/servicecatalog/documentation
 ===
 Name: DOCKER_TAG
 Type: String
 Default: latest
 Description: Docker tag name for created images

 **/

def execute() {

    def _cloneOSCMRepository = {
        stage('Build - clone OSCM repository') {
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

    def _cloneOSCMAppRepository = {
        stage('Build - clone OSCM repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-app-maven"
            dir("${WORKSPACE}/oscm-app-maven") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_OSCM_APP}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-app']]
                ]
            }
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

     def _prepareIndentityRepository = {
        stage('Build - clone oscm-identity repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-identity"
            dir("${WORKSPACE}/oscm-identity") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_IDENTITY}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-identity']]
                ]
            }
        }
    }

    def _prepareDockerbuildRepository = {
        stage('Build - clone dockerbuild repository') {
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


    def _prepareShellAdapterRepository = {
        stage('Build - clone oscm-app-shell repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-app-shell"
            dir("${WORKSPACE}/oscm-app-shell") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_APP_SHELL}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-app-shell']]
                ]
            }
        }
    }

    def _prepareApprovalAdapterRepository = {
        stage('Build - clone oscm-approval repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-approval"
            dir("${WORKSPACE}/oscm-approval") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_APPROVAL}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-approval']]
                ]
            }
        }
    }


    def _prepareRestAPIRepository = {
        stage('Build - clone oscm-rest-api repository') {
            sh "mkdir -p ${WORKSPACE}/oscm-rest-api"
            dir("${WORKSPACE}/oscm-rest-api") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_REST_API}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-rest-api.git']]
                ]
            }
        }
    }

    def _prepareMailRepository = {
       stage('Build - clone oscm-mail repository') {
           sh "mkdir -p ${WORKSPACE}/oscm-mail"
           dir("${WORKSPACE}/oscm-mail") {
               checkout scm: [
                       $class                           : 'GitSCM',
                       branches                         : [[name: "master"]],
                       doGenerateSubmoduleConfigurations: false,
                       extensions                       : [[$class : 'CloneOption',
                                                            noTags : false, reference: '',
                                                            shallow: true]],
                       submoduleCfg                     : [],
                       userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm-mail']]
               ]
           }
       }
   }

    def _downloadLibraries = {
        stage('Build - download external libraries') {
            sh "docker run " +
                    "--name gc-ant-ivy-${BUILD_ID} " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e ANT_OPTS=\" ${ANT_OPTS}\" " +
                    "gc-ant -f /build/oscm-devruntime/javares/build-oscmaas.xml BUILD.LIB"
        }
    }

     def _copyTenantConfig = {
        stage('Build - before oscm-core compiling') {
            sh "mkdir -p ${WORKSPACE}/oscm-portal/WebContent/oidc"
            dir("${WORKSPACE}/oscm-portal/WebContent/oidc") {
                sh "cp ${WORKSPACE}/oscm-identity/config/tenants/tenant-default.properties ."
            }
        }
    }

      def _copyArtifacts = {
        stage('Build - copy artifacts') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name copy-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    "oscm-centos-based /bin/bash /build/oscm-dockerbuild/prepare.sh /build || true"
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
                    " ${RUN_PROXY_ARGS} " +
                    "-e ANT_OPTS=\" ${ANT_OPTS}\" " +
                    "-e PATH=/usr/local/dart-sass:${env.PATH} " +
                    "gc-ant -f /build/oscm-devruntime/javares/build-oscmaas.xml BUILD.BES"
        }
    }

    def _compileApp = {
        stage('Build - compile oscm-app') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-app-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e MAVEN_OPTS=\"${MAVEN_OPTS} \" " +
                    "oscm-maven clean install -f /build/oscm-app-maven/pom.xml"
        }
    }

    def _compileShell = {
        stage('Build - compile oscm-shell') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-shell-adapter-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e MAVEN_OPTS=\"${MAVEN_OPTS} \" " +
                    "oscm-maven clean package -f /build/oscm-app-shell/pom.xml"
        }
    }

    def _compileApproval = {
        stage('Build - compile oscm-approval') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-approval-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e MAVEN_OPTS=\"${MAVEN_OPTS}\" " +
                    "oscm-maven clean package -f /build/oscm-approval/pom.xml"
        }
    }

    def _compileRestAPI = {
        stage('Build - compile oscm-rest-api') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-app-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e MAVEN_OPTS=\"${MAVEN_OPTS}\" " +
                    "oscm-maven clean install -f /build/oscm-rest-api/pom.xml"
        }
    }

      def _compileIdentity = {
        stage('Build - compile oscm-identity') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-identity-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    " ${RUN_PROXY_ARGS} " +
                    "-e MAVEN_OPTS=\"${MAVEN_OPTS}\" " +
                    "oscm-maven clean package -f /build/oscm-identity/pom.xml"
        }
    }

    def _compileMail = {
      stage('Build - compile oscm-mail') {
          user = sh(returnStdout: true, script: 'id -u').trim()
          group = sh(returnStdout: true, script: 'id -g').trim()
          sh "docker run " +
                  "--name maven-mail-${BUILD_ID} " +
                  "--user $user:$group " +
                  "--rm " +
                  "-v ${WORKSPACE}:/build " +
                  "-e MAVEN_OPTS=\"${MAVEN_OPTS}\" " +
                  "oscm-maven clean package -f /build/oscm-mail/pom.xml"
      }
  }

        def _buildIdentityImage = {
        stage('Build - identity image oscm-identity') {
            docker.build(
                    "oscm-identity:${DOCKER_TAG}",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-identity"
            )
        }
    }

    def _buildMailImage = {
        stage('Build - mail image oscm-mail') {
            docker.build(
                    "oscm-mail:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-mail"
            )
        }
    }

    def _buildCoreImage = {
        stage('Build - core image oscm-core') {
            docker.build(
                    "oscm-core:${DOCKER_TAG}",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-core"
            )
        }
    }

    def _buildAppImage = {
        stage('Build - app image oscm-app') {
            docker.build(
                    "oscm-app:${DOCKER_TAG}",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-app"
            )
        }
    }

    def _buildInitDBImage = {
        stage('Build - init db image oscm-initdb') {
            docker.build(
                    "oscm-initdb:${DOCKER_TAG}",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-initdb"
            )
        }
    }

    def _buildDeployerImage = {
        stage('Build - deployer image') {
            docker.build("oscm-deployer:${DOCKER_TAG}",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-deployer"
            )
        }
    }



    _cloneOSCMRepository()
    _cloneOSCMAppRepository()
    _prepareBuildTools()
    _prepareIndentityRepository()
    _prepareDockerbuildRepository()
    _prepareShellAdapterRepository()
    _prepareRestAPIRepository()
    _prepareApprovalAdapterRepository()
    _prepareMailRepository()


    _downloadLibraries()
    _copyTenantConfig()

    _compileCore()
    _compileApp()
    _compileShell()
    _compileApproval()
    _compileRestAPI()
    _compileIdentity()
    _compileMail()
    _copyArtifacts()

    _buildIdentityImage()
    _buildMailImage()
    _buildCoreImage()
    _buildAppImage()
    _buildInitDBImage()
    _buildDeployerImage()
}

return this
