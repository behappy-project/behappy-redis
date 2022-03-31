package org.xiaowu.behappy.redis.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.xiaowu.behappy.redis.metadata.Constant;

/**
 * @author xiaowu
 * Banner初始化
 */
public class BannerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (applicationContext.getParent() == null && applicationContext.getParent() != applicationContext) {
            String bannerShown = System.getProperty(Constant.BANNER_SHOWN, "true");
            if (!Boolean.valueOf(bannerShown)) {
                return;
            }
            String banner = " ____     ___  __ __   ____  ____  ____  __ __      ____     ___  ___    ____ _____\n" +
                    "|    \\   /  _]|  |  | /    ||    \\|    \\|  |  |    |    \\   /  _]|   \\  |    / ___/\n" +
                    "|  o  ) /  [_ |  |  ||  o  ||  o  )  o  )  |  |    |  D  ) /  [_ |    \\  |  (   \\_ \n" +
                    "|     ||    _]|  _  ||     ||   _/|   _/|  ~  |    |    / |    _]|  D  | |  |\\__  |\n" +
                    "|  O  ||   [_ |  |  ||  _  ||  |  |  |  |___, |    |    \\ |   [_ |     | |  |/  \\ |\n" +
                    "|     ||     ||  |  ||  |  ||  |  |  |  |     |    |  .  \\|     ||     | |  |\\    |\n" +
                    "|_____||_____||__|__||__|__||__|  |__|  |____/     |__|\\_||_____||_____||____|\\___|\n" +
                    "                                                                                   ";
            System.out.println(banner);
            StringBuilder version = new StringBuilder().append("Version:").append("1.0.0");
            StringBuilder github = new StringBuilder().append("Github:").append("https://github.com/wang-xiaowu");
            StringBuilder blog = new StringBuilder().append("Version:").append("https://wang-xiaowu.github.io/");
            System.out.println(version);
            System.out.println(github);
            System.out.println(blog);
        }
    }

}
