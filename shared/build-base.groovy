
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

    def _prepareDocumentationRepository = {
        stage('Build - prepare documentation repository') {
            sh "mkdir -p ${WORKSPACE}/documentation"
            dir("${WORKSPACE}/documentation") {
                checkout scm: [
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${REPO_TAG_DOCUMENTATION}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class : 'CloneOption',
                                                             noTags : false, reference: '',
                                                             shallow: true]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/documentation.git']]
                ]
            }
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
        
    def _copyUserDocumentation = {
        stage('Build - copy user documentation') {
            sh "cp -r ${WORKSPACE}/documentation/Development/oscm-doc-user/resources/ ${WORKSPACE}/oscm-doc-user/";
        }
    }

    def _buildOSCMCentosBasedImage = {
        stage('Build - CENTOS base image oscm-centos-based') {
            docker.build(
                    "oscm-centos-based", 
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
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
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
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
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-maven"
            )
            sh(
                'docker tag oscm-maven oscm-maven:${DOCKER_TAG}; ' 
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
                    "-e ANT_OPTS=\"-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080\" " +
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


    def _compileIdentity = {
        stage('Build - compile oscm-identity') {
            user = sh(returnStdout: true, script: 'id -u').trim()
            group = sh(returnStdout: true, script: 'id -g').trim()
            sh "docker run " +
                    "--name maven-identity-${BUILD_ID} " +
                    "--user $user:$group " +
                    "--rm " +
                    "-v ${WORKSPACE}:/build " +
                    "-e http_proxy=\"${http_proxy}\" " +
                    "-e https_proxy=\"${https_proxy}\" " +
                    "-e HTTP_PROXY=\"${http_proxy}\" " +
                    "-e HTTPS_PROXY=\"${https_proxy}\" " +
                    "-e MAVEN_OPTS=\"-Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080\" " +
                    "oscm-maven clean package -f /build/oscm-identity/pom.xml"
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
                    "-e http_proxy=\"${http_proxy}\" " +
                    "-e https_proxy=\"${https_proxy}\" " +
                    "-e HTTP_PROXY=\"${http_proxy}\" " +
                    "-e HTTPS_PROXY=\"${https_proxy}\" " +
                    "-e MAVEN_OPTS=\"-Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080\" " +
                    "oscm-maven clean install -f /build/oscm-app-maven/pom.xml"
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

    def _buildServerImage = {
        stage('Build - server image oscm-gf') {
            docker.build(
                    "oscm-gf:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-gf"
            )
        }
    }

    def _buildCoreImage = {
        stage('Build - core image oscm-core') {
            docker.build(
                    "oscm-core:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-core"
            )
        }
    }


    def _buildIdentityImage = {
        stage('Build - identity image oscm-identity') {
            docker.build(
                    "oscm-identity:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-identity"
            )
        }
    }


    def _buildDBImage = {
        stage('Build - db image oscm-db') {
            docker.build(
                    "oscm-db:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-db"
            )
        }
    }

    def _buildNginxImage = {
        stage('Build - base nginx image') {
            docker.build(
                    "oscm-nginx",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-nginx"
            )
        }
    }

    def _buildBrandingImage = {
        stage('Build - branding image oscm-branding') {
            docker.build(
                    "oscm-branding:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-branding"
            )
        }
    }

    def _buildWebserverImage = {
        stage('Build - webserver image oscm-help') {
            docker.build(
                    "oscm-help:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-help"
            )
        }
    }

    def _buildBirtImage = {
        stage('Build - birt image oscm-birt') {
            docker.build(
                    "oscm-birt:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-birt"
            )
        }
    }

    def _buildInitDBImage = {
        stage('Build - init db image oscm-initdb') {
            docker.build(
                    "oscm-initdb:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-initdb"
            )
        }
    }

    def _buildProxy = {
        stage('Build - proxy image oscm-proxy') {
            docker.build(
                    "oscm-proxy:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-proxy"
            )
        }
    }

    def _buildMailDev = {
        stage('Build - maildev image oscm-maildev') {
            docker.build(
                    "oscm-maildev:${DOCKER_TAG}",
                    "--build-arg http_proxy=\"${http_proxy}\" " +
                            "--build-arg https_proxy=\"${https_proxy}\" " +
                            "--build-arg HTTP_PROXY=\"${http_proxy}\" " +
                            "--build-arg HTTPS_PROXY=\"${https_proxy}\" " +
                            "${WORKSPACE}/oscm-dockerbuild/oscm-maildev"
            )
        }
    }

	_cloneOSCMRepository()
	_cloneOSCMAppRepository()
    _prepareDockerbuildRepository()
    _prepareDocumentationRepository()
    _prepareIndentityRepository()
    _prepareApprovalAdapterRepository()

    _copyUserDocumentation()

    _buildOSCMCentosBasedImage()
    _buildAntImage()
    _buildMavenImage()

    _downloadLibraries()
    _copyTenantConfig()
    
	_compileCore()
	_compileApp()
    _compileIdentity()
    _copyArtifacts()

	
    _buildServerImage()
    _buildDBImage()
    _buildIdentityImage()
    _buildProxy()
    _buildNginxImage()
    _buildBrandingImage()
    _buildWebserverImage()
    _buildBirtImage()
    _buildInitDBImage()
    _buildMailDev()
}

return this
 