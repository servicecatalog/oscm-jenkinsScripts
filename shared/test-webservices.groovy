void execute() {
    def ANT_HOME = ''
    def ANT_BIN = ''
    def TEST_DIR = '${WORKSPACE}/testdir'

    def _prepareEnv = {
        stage('Test webservices - prepare environment') {
            sh "rm -rf ${TEST_DIR}"
            sh "mkdir -p ${TEST_DIR}"
        }
    }

    def _setupAnt = {
        stage('Test webservices - download and setup ant') {
            sh "curl ${ANT_URL} -x http://proxy.intern.est.fujitsu.com:8080 -o ${TEST_DIR}/apache-ant.tar.gz"
            sh "mkdir -p ${TEST_DIR}/apache-ant"
            sh "tar -xf ${TEST_DIR}/apache-ant.tar.gz -C ${TEST_DIR}/apache-ant --strip-components=1"
            ANT_BIN = sh(
                    script: "echo ${TEST_DIR}/apache-ant/bin/ant",
                    returnStdout: true
            ).trim()
            ANT_HOME = sh(
                    script: "echo ${TEST_DIR}/apache-ant",
                    returnStdout: true
            ).trim()
        }
    }

    def _setupIvy = {
        stage('Test webservices - download and setup ivy') {
            sh "curl ${IVY_URL} -x http://proxy.intern.est.fujitsu.com:8080 -o ${TEST_DIR}/apache-ivy.tar.gz"
            sh "mkdir -p ${TEST_DIR}/apache-ivy"
            sh "tar -xf ${TEST_DIR}/apache-ivy.tar.gz -C ${TEST_DIR}/apache-ivy --strip-components=1"
            sh "mkdir -p $HOME/.ant/lib"
            sh "cp ${TEST_DIR}/apache-ivy/ivy-2.4.0.jar $ANT_HOME/lib/ivy.jar"
        }
    }

    def _cloneRepo = {
        stage('Test webservices - clone repository') {
            sh "git clone --single-branch --branch ${REPO_TAG_OSCM} https://github.com/servicecatalog/oscm.git ${TEST_DIR}/oscm"
        }
    }

    def _enableRemoteEjb = {
        stage('Test webservices - enable remote ejb in tomee') {
            sh "docker cp oscm-core:/opt/apache-tomee/conf/system.properties ${TEST_DIR}/system.properties"

            sh "echo '\r\ntomee.remote.support = true\r\n' >> ${TEST_DIR}/system.properties"
            sh "cat ${TEST_DIR}/system.properties"

            sh "docker cp ${TEST_DIR}/system.properties oscm-core:/opt/apache-tomee/conf/system.properties"
            sh "docker restart oscm-core"
        }
    }

    def _setupCerts = {
        stage('Test webservices - setup certificates') {
            sh "rm -rf /tmp/certs"
            sh "mkdir -p /tmp/certs"
            sh "docker cp oscm-core:/opt/apache-tomee/conf/ssl.p12 /tmp/certs"
        }
    }

    def _setupTenant = {
        stage('Test webservices - setup tenant') {
            sh "cp ${WORKSPACE}/oscm-portal/WebContent/oidc/tenant-default.properties ${WORKSPACE}/docker/config/oscm-identity/tenants/"

            sh '''
            sed -i \
                -e "s|^\\(oidc.provider\\+=\\).*|\\1default|g" \
                -e "s|^\\(oidc.clientId\\+=\\).*|\\1ef29aa22-369b-424f-9c72-6800fb24239d|g" \
                -e "s|^\\(oidc.clientSecret\\+=\\).*|\\1ARNRWfZsWiA6A]=tmA-GyRPlhX9=9VQ7|g" \
                -e "s|^\\(oidc.authUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/authorize|g" \
                -e "s|^\\(oidc.authUrlScope\\+=\\).*|\\1openid profile offline_access https://graph.microsoft.com/user.read.all https://graph.microsoft.com/group.readwrite.all |g" \
                -e "s|^\\(oidc.logoutUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/logout|g" \
                -e "s|^\\(oidc.tokenUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/oauth2/v2.0/token|g" \
                -e "s|^\\(oidc.redirectUrl\\+=\\).*|\\1http://localhost:9090/oscm-identity/callback|g" \
                -e "s|^\\(oidc.configurationUrl\\+=\\).*|\\1https://login.microsoftonline.com/ctmgsso.onmicrosoft.com/v2.0/.well-known/openid-configuration|g" \
                -e "s|^\\(oidc.usersEndpoint\\+=\\).*|\\1https://graph.microsoft.com/v1.0/users|g" \
                -e "s|^\\(oidc.groupsEndpoint\\+=\\).*|\\1https://graph.microsoft.com/v1.0/groups|g" \
                -e "s|^\\(oidc.idpApiUri\\+=\\).*|\\1https://graph.microsoft.com |g" \
				${WORKSPACE}/docker/config/oscm-identity/tenants/tenant-default.properties;
            '''
        }
    }

    def _setupSupplier = {
        stage('Test webservices - setup supplier') {
            sh "echo \"SUPPLIER_USER_PWD=${SUPPLIER_USER_PWD}\" >> ${WORKSPACE}/docker/var.env"
        }
    }

    def _test = {
        stage('Test webservices - run webservice tests') {
            withEnv([
                    "JAVA_HOME=${JAVA_HOME_DIR}",
                    "ANT_HOME=${ANT_HOME}",
                    "ANT_OPTS=-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080 -Xmx4096m -Xms32m",
            ]) {
                try {
                    sh "${ANT_BIN} -buildfile ${TEST_DIR}/oscm/oscm-build/cruisecontrol.xml _runWebserviceTests"
                } finally {
                    archiveArtifacts "testdir/oscm/oscm-build/result/reports/test-ws/**/*"
                }
            }
        }
    }

    _prepareEnv()
    _setupAnt()
    _setupIvy()
    _cloneRepo()
    _enableRemoteEjb()
    _setupCerts()
    _setupTenant()
    _setupSupplier()
    _test()
}

return this