/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2021                                            *
 *                                                                           *
 * Creation Date: 26-04-2021                                                 *
 *                                                                           *
 ****************************************************************************/
 
void execute() {


    def _setProxyVariables = {
    
        stage('Build - set proxy variables') {
	        if ( "${http_proxy}" != ''  && "${https_proxy}" != '') {
                URI httpUri = new URI("${http_proxy}") 
                URI httpsUri = new URI("${https_proxy}") 
                println (httpUri?.getHost())
                println (httpsUri?.getHost())
                env.ANT_OPTS="-Dhttp.proxyHost=\"httpUri?.getHost()\" -Dhttp.proxyPort=\"httpUri?.getPort()\" -Dhttps.proxyHost=\"httpsUri?.getHost()\" -Dhttps.proxyPort=\"httpUri?.getPort()\""
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost=\"httpUri?.getHost()\" -Dhttp.proxyPort=\"httpUri?.getPort()\" -Dhttps.proxyHost=\"httpsUri?.getHost()}\" -Dhttps.proxyPort=\"httpUri?.getPort()\""
            } else if ( "${http_proxy}" != '') {
                http = "${http_proxy}".replaceAll("http:\\", "").split(':');
                env.ANT_OPTS="-Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\""
            } else if ( "${https_proxy}" != '' ) {
                https = "${https_proxy}".replaceAll("https:\\", "").split(':');
                env.ANT_OPTS=" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
           } else {
                env.ANT_OPTS=""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
           }       
       }
   }  
   
    _setProxyVariables()
}
return this