/**

 *****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 *****************************************************************************


 To import and use, add below code to jenkinsFile run by jenkins:
 `
 def pull = evaluate readTrusted('shared/pull.groovy') `
 pull.execute()
 `

 Required environment variables:

 Name: DOCKER_REGISTRY
 Default: artifactory.intern.est.fujitsu.com:5003
 Description: Registry host and port for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG
 ===
 Name: DOCKER_ORGANIZATION
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG
 ===
 Name: DOCKER_TAG
 Default: v17.4.0
 Description: Tag for the final Docker image. Example: DOCKER_REGISTRY/DOCKER_ORGANIZATION/oscm-core:DOCKER_TAG

 **/

void execute() {
    def _pullDeployerImage = {
        stage('Pull - pull deployer image') {
            docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-deployer:${DOCKER_TAG}").pull()
        }
    }
    
     def _pullProxyImage = {
        stage('Pull - pull proxy image') {
            docker.image("${DOCKER_REGISTRY}/${DOCKER_ORGANIZATION}/oscm-proxy:${DOCKER_TAG}").pull()
        }
    }

    _pullDeployerImage()
    _pullProxyImage()
}

return this
