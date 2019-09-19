package hello.redis;

import hello.model.SomeObject;
import org.redisson.api.RTopic;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;


@Component("myRedisson")
public class MyRedisson {

    public MyRedisson() {
        redSub();
    }

    private RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.1.70:6379");
        RedissonClient client = Redisson.create(config);
        return client;
    }


    public void redSub(){
        RTopic topic = getRedissonClient().getTopic("anyTopic");

        topic.addListener(SomeObject.class, new MessageListener<SomeObject>() {
            @Override
            public void onMessage(CharSequence charSequence, SomeObject someObject) {
                System.out.println("#############################");
                System.out.println("get instance 1");
                System.out.println(someObject);
                System.out.println("end");
                System.out.println("#############################");
            }
        });
    }

    public void redPub(String name){
        RTopic topic = getRedissonClient().getTopic("anyTopic");
        long clientsReceivedMessage = topic.publish(new SomeObject(name));
    }
}
