/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/

// How to use:
// 1. Create a job of type Pipeline
// 2. As the pipeline script paste this script
// 3. Create the following string parameters (adjust defaults if you wish):
// --
// Name: DOCKER_REGISTRY
// Default: artifactory.intern.est.fujitsu.com:5002
// Description: Registry host and port for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: DOCKER_ORGANIZATION
// Default: oscmdocker
// Description: Organization name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: TAG_REPO_JENKINS_SCRIPTS
// Default: master
// Description: http://estscm1.intern.est.fujitsu.com/fujitsu-bss/jenkinsScripts
// --
// Name: TAG_REPO_DEVELOPMENT
// Default: master
// Description: Branch or tag in the git repository development
// --
// Name: TAG_REPO_DOCKERBUILD
// Default: master
// Description: Branch or tag in the git repository oscmaas-dockerbuild
// --
// Name: TAG_REPO_DOCUMENTATION
// Default: master
// Description: Branch or tag in the git repository documentation https://github.com/servicecatalog/documentation
// --
// Name: DOCKER_ORGANIZATION
// Default: oscmdocker
// Description: Project name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:TAG_DOCKER
// --
// Name: TAG_DOCKER
// Default: ws-tests
// Description: Docker tag name for the built images
// --
// Name: ANT_URL
// Default: https://archive.apache.org/dist/ant/binaries/apache-ant-1.9.4-bin.tar.gz
// Description: URL to download ant from.
// --
// Name: IVY_URL
// Default: http://www-eu.apache.org/dist//ant/ivy/2.4.0/apache-ivy-2.4.0-bin.tar.gz
// Description: URL to download ivy from.
// --
// Name: OSCM_GIT_REPO
// Default: https://github.com/servicecatalog/oscm.git
// Description: Repository URL to download the oscm from.
// --
// Name: JAVA_HOME_DIR
// Default: /etc/alternatives/java_sdk_1.8.0
// Description: Path to the JDK directory.
//
// Create the following Node parameter:
// --
// Name: NODE_NAME
// Select the default nodes and possible nodes for your environment
// Enable "Disallow multi node selection"
// Node eligibility: All nodes
// Description: Where to set up the OSCM?
//
// Create the following Boolean parameters:
// --
// Name: PURGE_DATABASE
// Default: disabled
// Description: Delete the database and start with a fresh one?
// --
// Name: TOMEE_DEBUG
// Default: disabled

// --
// Name: SAMPLE_DATA
// Default: disabled
// Description: Load sample data?

node("${NODE_NAME}") {
    
    
    def clean = evaluate readTrusted('shared/cleanup.groovy')
    def pull = evaluate readTrusted('shared/pull.groovy')
    def checkoutTests = evaluate readTrusted('shared/checkout-tests.groovy')
    def prepareWS = evaluate readTrusted('shared/prepare-ws.groovy')
    def start = evaluate readTrusted('shared/start.groovy')
    def test = evaluate readTrusted('shared/test-webservices.groovy')

    clean.execute()
    pull.execute()
    checkoutTests.execute()
    prepareWS.execute()
    start.execute()
    test.execute()
    clean.execute()
}
