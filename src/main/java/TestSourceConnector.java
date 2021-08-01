import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.source.SourceTask;

import java.util.List;
import java.util.Map;

public class TestSourceConnector extends SourceConnector {

    // 사용자가 입력한 설정값을 초기화
    @Override
    public void start(Map<String, String> props) {
        // 만약 설정값에 문제가 있다면
        // throw new ConnectException("error message");
    }

    // 커넥터가 사용할 태스크 클래스 리턴
    @Override
    public Class<? extends Task> taskClass() {
        return null;
    }

    // 태스크가 여럿일 경우 각 태스크에 적용
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        return null;
    }

    // 종료 로직(e.g. JDBC 커넥션 종료)
    @Override
    public void stop() {

    }

    // 커넥터가 사용할 설정값에 대한 정보 리턴
    @Override
    public ConfigDef config() {
        return null;
    }

    // 커넥터의 버전을 리턴
    @Override
    public String version() {
        return null;
    }

}
