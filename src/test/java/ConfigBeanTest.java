import com.txc.conf.ConfigBean;
import com.txc.conf.ConfigBeanSupport;
import com.txc.util.GradleGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by xiaocantan on 2016/8/20.
 */
public class ConfigBeanTest {
    @Test
    public void testConfigBean(){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("springContext.xml");
//        Map<String,String> configBean = (Map<String,String>) applicationContext.getBean("configBeanSupport");
//        System.out.println(configBean);
//        Assert.assertNotNull(configBean);
        GradleGenerator gradleGenerator = (GradleGenerator)applicationContext.getBean("gradleGenerator");
        String result = gradleGenerator.generatorGradleBuildFile("D:\\poi\\pom-file\\pom.xml");
        System.out.print(result);
    }
}
