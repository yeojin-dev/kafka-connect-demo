package com.example;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class SingleFileSourceConnectorConfig extends AbstractConfig {

    // 읽을 파일 위치 설정
    public static final String DIR_FILE_NAME = "file";
    private static final String DIR_FILE_NAME_DEFAULT_VALUE = "/tmp/kafka.txt";
    private static final String DIR_FILE_NAME_DOC = "읽을 파일 경로와 이름";

    // 데이터를 보낼 토픽 설정
    public static final String TOPIC_NAME = "topic";
    private static final String TOPIC_DEFAULT_VALUE = "test";
    private static final String TOPIC_DOC = "보낼 토픽 이름";

    // 커넥터에 사용할 옵션값 정의
    // Importance enum 클래스는 사용자가 명시적으로 중요도를 알려주기 위한 용도
    public static ConfigDef CONFIG = new ConfigDef()
            .define(DIR_FILE_NAME, ConfigDef.Type.STRING,
                    DIR_FILE_NAME_DEFAULT_VALUE, ConfigDef.Importance.HIGH,
                    DIR_FILE_NAME_DOC)
            .define(TOPIC_NAME, ConfigDef.Type.STRING,
                    TOPIC_DEFAULT_VALUE, ConfigDef.Importance.HIGH,
                    TOPIC_DOC);

    public SingleFileSourceConnectorConfig(Map<String, String> props) {
        super(CONFIG, props);
    }

}