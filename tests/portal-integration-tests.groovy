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
        try {
           env.clientId = ${CLIENT_ID}
           env.clientSecret = ${CLIENT_SECRET}
        } catch (exc) {
            withCredentials([string(credentialsId: 'WS-TESTS-CLIENT-ID', variable: 'CLIENT_ID'), string(credentialsId: 'WS-TESTS-CLIENT-SECRET', variable: 'CLIENT_SECRET')]) {
              env.clientId = ${CLIENT_ID}
              env.clientSecret = ${CLIENT_SECRET}
            }
        }
                sh "cp ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties.template ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties"

                sh "sed -ri 's|oidc.authUrlScope=.*|oidc.authUrlScope=openid profile offline_access https://graph.microsoft.com/user.read.all https://graph.microsoft.com/group.readwrite.all https://graph.microsoft.com/directory.readwrite.all|g' ${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties"

                sh '''
            sed -i \
                -e "s|^\\(oidc.provider\\+=\\).*|\\1default|g" \
                -e "s|^\\(oidc.clientId\\+=\\).*|\\1${clientId}|g" \
                -e "s|^\\(oidc.clientSecret\\+=\\).*|\\1${clientSecret}|g" \
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

    def _stopUnusedContainers = {
        stage('Tests - stop unneeded services') {
            dir("${WORKSPACE}/docker") {
                sh "free"
                sh "docker stop oscm-mail oscm-birt"
                sh "docker rm oscm-mail oscm-birt"
                sh "sleep 5"
            }
        }
    }

    def _setupMaildevPorts = {
        stage('Tests - setup maildev ports') {
            dir("${WORKSPACE}/docker") {
                sh "docker stop oscm-maildev"
                sh "docker rm oscm-maildev"
                sh "docker-compose -f docker-compose-oscm.yml run -d -p 8082:1080 --name oscm-maildev oscm-maildev"
                sh "sleep 5"
                sh "free"
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
                        " ${RUN_PROXY_ARGS} " +
                        "-e MAVEN_OPTS=\"${MAVEN_OPTS} \" " +
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
    _updateTechnicalServicePath()
    _setupTenant()
    _stopUnusedContainers()
    _setupMaildevPorts()
    _installUITests()
    _cleanUp()
}

return this
