package com.example;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFileSourceConnector extends SourceConnector {

    private Map<String, String> configProperties;

    // 사용자가 입력한 설정값을 초기화
    @Override
    public void start(Map<String, String> props) {
        this.configProperties = props;
        try {
            new SingleFileSourceConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    // 커넥터가 사용할 태스크 클래스 리턴
    @Override
    public Class<? extends Task> taskClass() {
        return SingleFileSourceTask.class;
    }

    // 태스크가 여럿일 경우 각 태스크에 적용할 설정 리스트
    // 예제에서는 모든 태스크가 같은 설정을 사용함
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        Map<String, String> taskProps = new HashMap<>();
        taskProps.putAll(configProperties);
        for (int i = 0;  i < maxTasks; i++) {
            taskConfigs.add(taskProps);
        }
        return taskConfigs;
    }

    // 종료 로직(e.g. JDBC 커넥션 종료)
    @Override
    public void stop() {
    }

    // 커넥터가 사용할 설정값에 대한 정보 리턴
    @Override
    public ConfigDef config() {
        return SingleFileSourceConnectorConfig.CONFIG;
    }

    // 커넥터의 버전을 리턴
    @Override
    public String version() {
        return "1.0";
    }

}
