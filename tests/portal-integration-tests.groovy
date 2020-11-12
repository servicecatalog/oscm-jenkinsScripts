/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/

/**
 For required environment variables please see the following file: tests/run-integration-tests.groovy
 **/

def execute() {

    def authMode = sh(
            script: 'echo $AUTH_MODE;',
            returnStdout: true
    ).trim()
    
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
    
    def _prepareBuildTools = {
        stage('Build - pull build tools') {
             docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-maven:${DOCKER_TAG}").pull()
             sh(
                'docker tag ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-maven:${DOCKER_TAG} oscm-maven; ' 
            )
        }
    }

    def _updateTechnicalServicePath = {
        stage('Tests - Update technical service path') {
            sh "sed -ri 's|technicalservice\\.xml\\.path=.*|technicalservice.xml.path=$TECH_SERVICE_PATH|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            sh "sed -ri 's|auth\\.mode=.*|auth.mode=$AUTH_MODE|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            sh "sed -ri 's|bes\\.user\\.id=.*|bes.user.id=$ADMIN_USER_ID|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            sh "sed -ri 's|bes\\.user\\.password=.*|bes.user.password=$ADMIN_USER_PWD|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            sh "sed -ri 's|app\\.user\\.id=.*|app.user.id=$ADMIN_USER_ID|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            sh "sed -ri 's|app\\.user\\.password=.*|app.user.password=$ADMIN_USER_PWD|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            if (authMode == 'OIDC') {
                sh "sed -ri 's|oidc\\.supplier\\.id=.*|oidc.supplier.id=$SUPPLIER_USER_ID|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
                sh "sed -ri 's|oidc\\.supplier\\.password=.*|oidc.supplier.password=$SUPPLIER_USER_PWD|g' ${WEBTEST_PROPERTIES_LOCALIZATION}"
            }
        }
    }

    def _setupTenant = {
        stage('Test webservices - setup tenant') {
            if (authMode == 'OIDC') {
                sh "cp ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties.template ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties"

                sh "sed -ri 's|oidc.authUrlScope=.*|oidc.authUrlScope=openid profile offline_access https://graph.microsoft.com/user.read.all https://graph.microsoft.com/group.readwrite.all https://graph.microsoft.com/directory.readwrite.all|g' ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties"

                sh '''
            sed -i \
                -e "s|^\\(oidc.provider\\+=\\).*|\\1default|g" \
                -e "s|^\\(oidc.clientId\\+=\\).*|\\152d193b3-0b31-4084-88a6-ea1e065b6bec|g" \
                -e "s|^\\(oidc.clientSecret\\+=\\).*|\\17F=peZ64RzCeUZRUi3BmgAB.wMMjmo_:|g" \
                -e "s|^\\(oidc.authUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/authorize|g" \
                -e "s|^\\(oidc.logoutUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/logout|g" \
                -e "s|^\\(oidc.tokenUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/token|g" \
                -e "s|^\\(oidc.redirectUrl\\+=\\).*|\\1https://localhost:9091/oscm-identity/callback|g" \
                -e "s|^\\(oidc.configurationUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/v2.0/.well-known/openid-configuration|g" \
                -e "s|^\\(oidc.usersEndpoint\\+=\\).*|\\1https://graph.microsoft.com/v1.0/users|g" \
                -e "s|^\\(oidc.groupsEndpoint\\+=\\).*|\\1https://graph.microsoft.com/v1.0/groups|g" \
                -e "s|^\\(oidc.idpApiUri\\+=\\).*|\\1https://graph.microsoft.com|g" \
				${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties;
            '''
            }
        }
    }

    def _installUITests = {
        stage('Tests - install ui tests') {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                user = sh(returnStdout: true, script: 'id -u').trim()
                group = sh(returnStdout: true, script: 'id -g').trim()
                sh "sleep 120"
                sh "docker run " +
                        "--name maven-ui-tests-${BUILD_ID} " +
                        "--user $user:$group " +
                        "--rm " +
                        "--network host " +
                        "--add-host oscm-identity:127.0.0.1 " +
                        "-v ${WORKSPACE}:/build " +
                        "-e http_proxy=\"${http_proxy}\" " +
                        "-e https_proxy=\"${https_proxy}\" " +
                        "-e HTTP_PROXY=\"${http_proxy}\" " +
                        "-e HTTPS_PROXY=\"${https_proxy}\" " +
                        "-e MAVEN_OPTS=\"-Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080\" " +
                        "oscm-maven clean install -e -f /build/oscm-ui-tests/pom.xml"
            }
        }
    }

    def _cleanUp = {
        stage('Tests - clean up') {
            sh "if [ \$(docker volume ls -qf dangling=true | wc -l) != '0' ]; then docker volume ls -qf dangling=true | xargs -r docker volume rm > /dev/null; fi"
        }
    }

    
    _prepareBuildTools()
    _cloneOSCMRepository()
    _updateTechnicalServicePath()
    _setupTenant()
    _installUITests()
    _cleanUp()
}

return this
