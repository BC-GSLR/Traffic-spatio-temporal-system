import com.usc.spark.conf.ConfigurationManager;
import com.usc.spark.constant.Constants;
import com.usc.spark.util.SparkUtils;

public class TrafficTest {
    public static void main(String[] args) {
       /* boolean local = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL);
        System.out.println(local);*/
       Double distance=SparkUtils.getDistance(123.3417969,41.66085815,123.3346634,41.66479874);
        System.out.println(distance);
    }
}
