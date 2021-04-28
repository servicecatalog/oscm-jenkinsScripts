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
	        script {
	        	String[] http;
	        	String[] https;
	        if ( "${http_proxy}" != ''  && "${https_proxy}" != '') {
      			http = http_proxy.split(':');
      			https = https_proxy.split(':');
               env.ANT_OPTS="-Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
            } else if ( "${http_proxy}" != '' && "${http_port}" != '' ) {
               env.ANT_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
            } else if ( "${https_proxy}" != '' &&  "${https_port}" != '' ) {
               env.ANT_OPTS=" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
	        } else {
	           env.ANT_OPTS=""
	           env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
	        }       
	       }
       }
   }  
   
    _setProxyVariables()
}
return this