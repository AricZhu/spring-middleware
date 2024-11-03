import com.aric.middleware.rpc.annotation.EnableRpc;
import com.aric.middleware.rpcprovider.export.HelloService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration("classpath:spring-config.xml")
@EnableRpc
public class RpcTest {

    @Resource
    private HelloService helloService;

    @Test
    public void test_helloService() {
        String hi = helloService.hi();
        System.out.println(hi);
    }
}
