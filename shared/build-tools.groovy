
 def execute() {
 

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

    _buildOSCMCentosBasedImage()
    _buildAntImage()
    _buildMavenImage()

}

return this
 