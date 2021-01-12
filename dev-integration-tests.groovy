/**
 How to use:
 1. Create a job of type Pipeline
 2. As the pipeline script paste this script
 3. Create the following parameters (adjust defaults as you wish):

 Required environment variables:
 ===
 Name: NODE_NAME
 Type: Option list
 Default: -
 Description: Nodes used to run the pipeline.
 ===
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
 Description: Branch or tag in the oscm-app-shell git repository: https://github.com/servicecatalog/oscm-app-shell
 ===
 Name: REPO_TAG_APPROVAL
 Type: String
 Default: master
 Description: Branch or tag in the oscm-approval git repository: https://github.com/servicecatalog/oscm-approval
 ===
 Name: REPO_TAG_IDENTITY
 Type: String
 Default: master
 Description: Branch or tag in the oscm-identity git repository: https://github.com/servicecatalog/oscm-identity
 ===
 Name: REPO_TAG_REST_API
 Type: String
 Default: master
 Description: Branch or tag in the oscm-identity git repository: https://github.com/servicecatalog/oscm-rest-api
 ===
 Name: REPO_TAG_DOCUMENTATION
 Type: String
 Default: master
 Description: Branch or tag in the documentation git repository" https://github.com/servicecatalog/documentation
 ===
 Name: ACTIVATION_CODE_URL
 Type: String
 Default: http://10.140.16.80:8080/userContent/sles-reg-key
 Description: URL with activation code for SLES base image registration
 ===
 Name: EMAIL_ADDRESS
 Type: String
 Default: frank.shimizu@est.fujitsu.com
 Description: Email address used for SLES base image registration
 ===
 Name: TRUSTED_CERTS_PATH
 Type: String
 Default: /opt/trusted_certs
 Description: Path to the directory with certificates that will be imported as trusted by oscm
 ===
 Name: SAMPLE_DATA
 Type: boolean
 Default: false
 Description: Load sample data?
 ===
 Name: TOMEE_DEBUG
 Type: boolean
 Default: false
 Description: Value ignored when glassfish chosen.
 ===
 Name: AUTH_MODE
 Type: Option list (INTERNAL, OIDC)
 Default: INTERNAL
 Description: Auth mode used to communicate with OSCM web services.
 ===
 Name: COMPLETE_CLEANUP
 Default: enabled
 Description: Delete the database and whole workspace
 ===
 Name: TECH_SERVICE_PATH
 Type: String
 Default: /build/oscm-ui-tests/src/test/resources/TechnicalService.xml
 Description: Localization of Technical Service definition stored in .xml file
 ===
 Name: WEBTEST_PROPERTIES_LOCALIZATION
 Type: String
 Default: $WORKSPACE/oscm-ui-tests/src/test/resources/webtest.properties
 Description: Localization of webtest.properties file from oscm-ui-tests oscm module
 ===
 **/

node("${NODE_NAME}") {
    // Pull and start
    def clean = evaluate readTrusted('shared/cleanup.groovy')
    def pull = evaluate readTrusted('shared/pull.groovy')
    def start = evaluate readTrusted('shared/start.groovy')
    
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
        }
    }
    
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

    // Run integration tests
    def tests = evaluate readTrusted('tests/portal-integration-tests.groovy')

    clean.execute()
    _cloneOSCMRepository()
    _prepareDockerbuildRepository()
    _buildOSCMCentosBasedImage()
    _buildMavenImage()
    pull.execute()
    start.execute(env.NODE_NAME, false)

    tests.execute()
}
