package com.usc.spark.util;


import com.usc.spark.conf.ConfigurationManager;
import com.usc.spark.constant.Constants;


import net.minidev.json.JSONObject;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import java.text.DecimalFormat;


/**
 *
 * @author Administrator
 *
 */
public class SparkUtils {
	
	/**
	 * 根据当前是否本地测试的配置，决定 如何设置SparkSession的master
	 */
	public static void setMaster(SparkSession.Builder spark) {
		boolean local = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL);
		if(local) {
			spark.master("local");
		}
	}

	
	/**
	 * 获取数据
	 * 如果spark.local配置设置为true，则获取本地数据；否则获取hive中的数据
	 */
	public static void localData(SparkSession.Builder spark) {
		boolean local = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL);
		/**
		 * 如何local为true  说明在本地测试  应该生产模拟数据    RDD-》DataFrame-->注册成临时表0
		 * false    HiveContext  直接可以操作hive表
		 */
		if(local) {

		}
	}

	/**
	 * 获取指定范围数据
	 * @param spark
	 * @param taskParamsJsonObject
	 * @return
	 */
	public static Dataset<Row> getPrimitiveItudeByDateRange(SparkSession spark, JSONObject taskParamsJsonObject) {

		String startDate = ParamUtils.getParam(taskParamsJsonObject, Constants.PARAM_START_DATE);
 		String endDate = ParamUtils.getParam(taskParamsJsonObject, Constants.PARAM_END_DATE);
		System.out.println(startDate+""+endDate);

 		/*String sql="select * "
				+ "from primitive_itude "
				+ "where time<='" + "20181003000000 "+ "' "
				+ "and time>='" +"20181003000000"+ "'";*/
		String sql="select * from primitive_itude";
		Dataset<Row> primitiveDataSet = spark.sql(sql);
		return primitiveDataSet;

	}
	public static double  getDistance(double aLon,double alat,double zLon,double zlat) {
		DecimalFormat format = new DecimalFormat("0.0000");
		Long length=null;
		int Ri = 6371;
		Double d = ( Ri * Math.acos(Math.sin(alat*(Math.PI/180))*Math.sin(zlat*(Math.PI/180)) +
				Math.cos(alat*(Math.PI/180))*Math.cos(zlat*(Math.PI/180))*
						Math.cos((aLon - zLon)*(Math.PI/180))));
		length = Math.round(d*1000);
		String s = format.format(length);
		Double v = Double.valueOf(s);
		return v;
	}
	public static Dataset<Row> geTripMode(SparkSession spark) {
		String sql="select * " +
				"from trip_mode ";
		Dataset<Row> tripModeDataSet = spark.sql(sql);

		return tripModeDataSet;
	}

}
