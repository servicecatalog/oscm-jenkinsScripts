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
                env.ANT_OPTS="-Dhttp.proxyHost=${httpUri?.getHost()} -Dhttp.proxyPort=${httpUri?.getPort()} -Dhttps.proxyHost=${httpsUri?.getHost()} -Dhttps.proxyPort=${httpsUri?.getPort()}"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost=${httpUri?.getHost()} -Dhttp.proxyPort=${httpUri?.getPort()} -Dhttps.proxyHost=${httpsUri?.getHost()} -Dhttps.proxyPort=${httspUri?.getPort()}"
            } else if ( "${http_proxy}" != '') {
                URI httpUri = new URI("${http_proxy}") 
                env.ANT_OPTS="-Dhttp.proxyHost=httpUri?.getHost() -Dhttp.proxyPort=httpUri?.getPort()"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost=httpUri?.getHost() -Dhttp.proxyPort=httpUri?.getPort()"
            } else if ( "${https_proxy}" != '' ) {
                URI httpsUri = new URI("${https_proxy}") 
                env.ANT_OPTS="-Dhttps.proxyHost=httpsUri?.getHost() -Dhttps.proxyPort=httspUri?.getPort()"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost= -Dhttps.proxyHost=httpsUri?.getHost() -Dhttps.proxyPort=httspUri?.getPort()"
           } else {
                env.ANT_OPTS=""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
           }       
       }
   }  
   
    _setProxyVariables()
}
return this