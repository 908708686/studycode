package cn.unis.ad;



import cn.unis.ad.util.SendMail;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@SpringBootTest(classes = AdApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Component
public class AdApplicationTest {
//    @Test
//    public void Test() {
//    }
    @Resource
    SendMail sendMail;
    @Test
    public void Test() {
    }
}
