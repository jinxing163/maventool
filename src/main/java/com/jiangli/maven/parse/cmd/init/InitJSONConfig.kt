package com.jiangli.maven.parse.cmd.init

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 19:07
 */
class InitJSONConfig {
    var project_path:String? = null
    var deploy_url:String? = null
    var deploy_repositoryId:String? = null
    var configs:List<InitConfig> = mutableListOf()

    override fun toString(): String {
        return "InitJSONConfig(project_path=$project_path, deploy_url=$deploy_url, deploy_repositoryId=$deploy_repositoryId, configs=$configs)"
    }


}

class InitConfig {
    var profile_id:String? = null
    var mvn_cmd:String? = null
    var jarName:String? = null
    var groupId:String? = null
    var artifaceId:String? = null

    override fun toString(): String {
        return "InitConfig(profile_id=$profile_id, mvn_cmd=$mvn_cmd, jarName=$jarName, groupId=$groupId, artifaceId=$artifaceId)"
    }


}