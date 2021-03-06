/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/
 def execute() {
 
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

    def _buildOSCMCentosBasedImage = {
        stage('Build - CENTOS base image oscm-centos-based') {
            docker.build(
                    "oscm-centos-based", 
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-centos-based"
            )
            sh(
                'docker tag oscm-centos-based oscm-centos-based:${DOCKER_TAG}; ' 
            )
        }
    }
    

    def _buildAntImage = {
        stage('Build - ant image gc-ant') {
            docker.build(
                    "gc-ant",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/gc-ant"
            )
            sh(
                'docker tag gc-ant oscm-gc-ant:${DOCKER_TAG}; ' 
            )
        }
    }

    def _buildMavenImage = {
        stage('Build - maven image oscm-maven') {
            docker.build(
                    "oscm-maven",
                            "${BUILD_PROXY_ARGS}" +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-maven"
            )
            sh(
                'docker tag oscm-maven oscm-maven:${DOCKER_TAG}; ' 
            )
        }
    }
    
    _prepareDockerbuildRepository()
    _buildOSCMCentosBasedImage()
    _buildAntImage()
    _buildMavenImage()

}

return this
 