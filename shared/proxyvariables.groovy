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
        ANT_OPTS
        MAVEN_OPTS
        if ( "${http_proxy}" != ''  && -n ${https_proxy} != '' && ${http_port} ] &&  ${https_port} != '' )
        
               env.ANT_OPTS="-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
        else 
               env.ANT_OPTS=""
               env.MAVEN_OPTS=""
               
       }
       }
   }             
     def _echo = {
          stage('Print Varliabe'){
            steps{
                echo ${ANT_OPTS}
                echo ${MAVEN_OPTS}
            }
        }
    }
    
    _setProxyVariables()
    _echo
}
return this