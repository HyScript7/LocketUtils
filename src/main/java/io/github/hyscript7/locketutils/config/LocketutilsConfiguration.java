package io.github.hyscript7.locketutils.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "locketutils")
@PropertySource("file:./locketutils.properties")
public class LocketutilsConfiguration {
    private final Bot bot = new Bot();
    private final Database database = new Database();

    @Getter
    @Setter
    public static class Bot {
        private String token;
        private String prefix;
        private long guildid;
        private long logchannelid;
        private long adminroleid;
        private long operatorroleid;
        private long whitelistedrole;
    }

    @Getter
    @Setter
    public static class Database {
        private String hostname;
        private String port;
        private String username;
        private String password;
        private String database;
    }
}
