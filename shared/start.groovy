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
 def start = evaluate readTrusted('shared/start.groovy')
 start.execute()
 `

 Required environment variables:

 Name: DOCKER_REGISTRY
 Description: Registry host and port for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG
 ===
 Name: DOCKER_ORGANIZATION
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG
 ===
 Name: DOCKER_TAG
 Default: v17.4.0
 Description: Tag for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG
 ===
 Name: TOMEE_DEBUG
 Default: disabled
 Description: Value ignored when glassfish chosen.
 ===
 Name: AUTH_MODE
 Type: Option list (INTERNAL, OIDC)
 Default: INTERNAL
 Description: Auth mode used to communicate with OSCM web services.
 ===
 Name: SAMPLE_DATA
 Type: boolean
 Default: false
 Description: Load sample data?

 **/

void execute(String FQDN = env.NODE_NAME, PROXY = true) {

    def _createEnvTemplates = {
        stage('Start - create env templates') {
            // If the docker dir doesn't exists at this point, it is created with root owner by volume mapping with the docker run command
            // But we need to change it's owner to jenkins here, in order that env files can be replaced with sed later on
            // (this is because sed needs permission to create a temp file in this directory!)
            script {
                env.FQDN_NODE = FQDN
                sh '''
                docker run --rm -v ${WORKSPACE}/docker:/docker busybox chown $(id -u jenkins):$(id -g jenkins) /docker
                mkdir -p ${WORKSPACE}/docker/config/oscm-identity/tenants/
                docker run \
                    --name deployer1 \
                    --rm \
                    -v ${WORKSPACE}/docker:/target \
                    -e SAMPLE_DATA=${SAMPLE_DATA} \
                    -e HOST_FQDN=${FQDN_NODE} \
                    ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-deployer:${DOCKER_TAG}
                '''
            }
        }
    }

    def _setupEnv = {
        stage('Start - fill .env template') {
            sh '''
            sed -i \
                -e "s|^\\(IMAGE_[A-Z]\\+=\\).*/\\(.*:\\).*|\\1${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/\\2${DOCKER_TAG}|g" \
                -e "s|^DOCKER_PATH=.*|DOCKER_PATH=${WORKSPACE}/docker|g" \
                ${WORKSPACE}/docker/.env;
            '''
        }
    }

    def _setupVarEnv = {
        stage('Start - fill var.env template') {
            script {
            env.FQDN_NODE = FQDN
            sh '''
            sed -i \
                -e "s|^\\(DB_PORT_.*\\+=\\).*|\\15432|g" \
                -e "s|^\\(DB_PWD_.*\\+=\\).*|\\1secret|g" \
                -e "s|^\\(DB_SUPERPWD=\\).*|\\1secret|g" \
                -e "s|^\\(SMTP_HOST=\\).*|\\1oscm-maildev|g" \
                -e "s|^\\(SMTP_PORT=\\).*|\\125|g" \
                -e "s|^\\(SMTP_FROM=\\).*|\\1oscm@${NODE_NAME}.${DOMAIN}|g" \
                -e "s|^\\(SMTP_USER=\\).*|\\1none|g" \
                -e "s|^\\(SMTP_PWD=\\).*|\\1none|g" \
                -e "s|^\\(SMTP_AUTH=\\).*|\\1false|g" \
                -e "s|^\\(SMTP_TLS=\\).*|\\1false|g" \
                -e "s|^\\(CONTAINER_CALLBACK_THREADS=\\).*|\\150|g" \
                -e "s|^\\(PROXY_ENABLED=\\).*|\\1true|g" \
                -e "s|^\\(PROXY_HTTP_HOST=\\).*|\\1${http_proxy}|g" \
                -e "s|^\\(PROXY_HTTP_PORT=\\).*|\\1${http_port}|g" \
                -e "s|^\\(PROXY_HTTPS_HOST=\\).*|\\1${https_proxy}|g" \
                -e "s|^\\(PROXY_HTTPS_PORT=\\).*|\\1${https_port}|g" \
                -e "s|^\\(PROXY_NOPROXY=\\).*|\\1oscm-core\\|10.140.18.120\\|estvcsadev.${DOMAIN}\\|10.140.16.69|g" \
                -e "s|^\\(CONTAINER_MAX_SIZE=\\).*|\\150|g" \
                -e "s|^\\(LOG_LEVEL=\\).*|\\1ERROR|g" \
                -e "s|^\\(APP_ADMIN_MAIL_ADDRESS=\\).*|\\1oscm@${NODE_NAME}.${DOMAIN}|g" \
                -e "s|^\\(CONTROLLER_ORG_ID=\\).*|\\1PLATFORM_OPERATOR|g" \
                -e "s|^\\(CONTROLLER_USER_KEY=\\).*|\\1${CONTROLLER_USER_KEY}|g" \
                -e "s|^\\(CONTROLLER_USER_NAME=\\).*|\\1${CONTROLLER_USER_ID}|g" \
                -e "s|^\\(CONTROLLER_USER_PASS=\\).*|\\1${CONTROLLER_USER_PASS}|g" \
                -e "s|^\\\\(DB_USER_VMWARE=\\\\).*|\\\\1vmwareuser|g" \
                -e "s|^\\\\(DB_PWD_VMWARE=\\\\).*|\\\\1secret|g" \
                -e "s|^\\\\(VCENTER_NAME=\\\\).*|\\\\1estvcsadev.${DOMAIN}|g" \
                -e "s|^\\\\(DATACENTER_NAME=\\\\).*|\\\\1EST|g" \
                -e "s|^\\\\(CLUSTER_NAME=\\\\).*|\\\\1esscluster|g" \
                -e "s|^\\\\(LOAD_BALANCER_NAME=\\\\).*|\\\\1VM Network|g" \
                -e "s|^\\(KEY_SECRET=\\).*|\\1secretsecret1234|g" \
                -e "s|^\\(HOST_FQDN=\\).*|\\1${FQDN_NODE}|g" \
                -e "s|^\\(REPORT_ENGINEURL=https://\\).*\\(:8681.*\\)|\\1${FQDN_NODE}\\2|g" \
                -e "s|^\\(TOMEE_DEBUG=\\).*|\\1${TOMEE_DEBUG}|g" \
                -e "s|^\\\\(AUTH_MODE=\\\\).*|\\\\1${AUTH_MODE}|g" \
                -e "s|^\\\\(SSO_IDP_TRUSTSTORE=\\\\).*|\\\\1/opt/apache-tomee/conf/ssl.p12|g" \
                -e "s|^\\\\(SSO_IDP_TRUSTSTORE_PASSWORD=\\\\).*|\\\\1changeit|g" \
                -e "s|^\\\\(SSO_SIGNING_ALGORITHM=\\\\).*|\\\\1SHA1|g" \
                -e "s|^\\\\(SSO_SIGNING_KEY_ALIAS=\\\\).*|\\\\11|g" \
                -e "s|^\\\\(SSO_SIGNING_KEYSTORE=\\\\).*|\\\\1/opt/apache-tomee/conf/ssl.p12|g" \
                -e "s|^\\\\(SSO_SIGNING_KEYSTORE_PASS=\\\\).*|\\\\1changeit|g" \
                -e "s|^\\\\(ADMIN_USER_ID=\\\\).*|\\\\1${ADMIN_USER_ID}|g" \
                -e "s|^\\\\(ADMIN_USER_PWD=\\\\).*|\\\\1${ADMIN_USER_PWD}|g" \
                -e "s|^\\\\(APP_USER_NAME=\\\\).*|\\\\1${ADMIN_USER_ID}|g" \
                -e "s|^\\\\(APP_USER_PWD=\\\\).*|\\\\1${ADMIN_USER_PWD}|g" \
                -e "s|^\\\\(SUPPLIER_USER_ID=\\\\).*|\\\\1${SUPPLIER_USER_ID}|g" \
                -e "s|^\\\\(SUPPLIER_USER_PWD=\\\\).*|\\\\1${SUPPLIER_USER_PWD}|g" \
                -e "s|^\\\\(CUSTOMER_USER_ID=\\\\).*|\\\\1${CUSTOMER_USER_ID}|g" \
                -e "s|^\\\\(RESELLER_USER_ID=\\\\).*|\\\\1${RESELLER_USER_ID}|g" \
                -e "s|^\\\\(CONTROLLER_USER_NAME=\\\\).*|\\\\1${CONTROLLER_USER_NAME}|g" \
                -e "s|^\\\\(CONTROLLER_USER_PASS=\\\\).*|\\\\1${CONTROLLER_USER_PASS}|g" \
				${WORKSPACE}/docker/var.env;
            '''
            }
        }
    }

    def _start = {
        stage('Start - start OSCM') {
        script {
            env.PROXY = PROXY
            sh '''
            docker run \
                --name deployer2 \
                --rm \
                -v ${WORKSPACE}/docker:/target \
                -v /var/run/docker.sock:/var/run/docker.sock \
                -e INITDB=true \
                -e STARTUP=true \
                -e PROXY=${PROXY} \
                -e SAMPLE_DATA=${SAMPLE_DATA} \
                ${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-deployer:${DOCKER_TAG}
            '''
        }
        }
    }

    _createEnvTemplates()
    _setupEnv()
    _setupVarEnv()
    _start()
}

return this