package com.jiangli.maven.parse.cmd.init

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 19:07
 */
class InitJSONConfig {
    var project_path:String? = null
    var configs:List<InitConfig> = mutableListOf()

    override fun toString(): String {
        return "InitJSONConfig(project_path=$project_path, configs=$configs)"
    }
}

class InitConfig {
    var profile_id:String? = null
    var groupId:String? = null
    var artifaceId:String? = null

    override fun toString(): String {
        return "InitConfig(profile_id=$profile_id, groupId=$groupId, artifaceId=$artifaceId)"
    }


}