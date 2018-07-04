package com.jiangli.maven.parse.cmd.init

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 19:07
 */
class InitJSONConfig {
    var targetDirName:String? = "jar_deployment_scripts"
    var project_path:String? = "\${jarPath}"
    var deploy_url:String? = "http://maven.i.zhihuishu.com:8081/nexus/content/repositories/thirdparty/"
    var deploy_repositoryId:String? = "thirdparty"
    var configs:List<InitConfig> = mutableListOf()

    var maven_username:String? = "deployment"
    var maven_pwd:String? = "deployment123"
    var maven_search_url:String? = "http://maven.i.zhihuishu.com:8081/nexus/#view-repositories;"
    var maven_version_urlprefix:String? = "http://maven.i.zhihuishu.com:8081/nexus/service/local/repositories/thirdparty/content"

    override fun toString(): String {
        return "InitJSONConfig(targetDirName=$targetDirName, project_path=$project_path, deploy_url=$deploy_url, deploy_repositoryId=$deploy_repositoryId, configs=$configs, maven_search_url=$maven_search_url, maven_version_urlprefix=$maven_version_urlprefix)"
    }


}

class InitConfig {
    var profile_id:String? = null
    var mvn_cmd:String? = null
    var jarName:String? = null
    var groupId:String? = null
    var artifactId:String? = null

    override fun toString(): String {
        return "InitConfig(profile_id=$profile_id, mvn_cmd=$mvn_cmd, jarName=$jarName, groupId=$groupId, artifactId=$artifactId)"
    }


}