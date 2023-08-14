package com.usc.spark.util;


import com.usc.spark.conf.ConfigurationManager;
import com.usc.spark.constant.Constants;
import net.minidev.json.JSONObject;


/**
 *
 * @author Administrator
 *
 */
public class ParamUtils {

	/**
	 * 从命令行参数中提取任务id
	 * @param args 命令行参数
	 * @param taskType 参数类型(任务id对应的值是Long类型才可以)，对应my.properties中的key
	 * @return 任务id
	 */
	public static Long getTaskIdFromArgs(String[] args, String taskType) {
		boolean local = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL);

		if(local) {
			return ConfigurationManager.getLong(taskType);
		} else {
			try {
				if(args != null && args.length > 0) {
					return Long.valueOf(args[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0L;
	}

	/**
	 * 从json对象中获取参数
	 * @param jsonObject
	 * @param field
	 * @return
	 */
	public  static String getParam(JSONObject jsonObject, String field){
		return jsonObject.get(field).toString();
	}



	
}
