import com.txc.conf.ConfigBean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

/**
 * Created by xiaocantan on 2016/8/20.
 */
public class ConfigBeanTest {
    @Test
    public void testConfigBean(){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("springContext.xml");
        Properties configBean = (Properties) applicationContext.getBean("configBean");
        System.out.println(configBean.get("dependencyManagement.dependencies"));
        Assert.assertNotNull(configBean);
    }
}
