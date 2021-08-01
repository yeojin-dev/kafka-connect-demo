import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.List;
import java.util.Map;

public class TestSourceTask extends SourceTask {

    // 태스크의 버전 리턴
    @Override
    public String version() {
        return null;
    }

    // 태스크 시작 로직 - 데이터 처리에 필요한 모든 리소스 초기화
    @Override
    public void start(Map<String, String> props) {

    }

    // 데이터 읽어오는 로직
    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return null;
    }

    // 종료 로직
    @Override
    public void stop() {

    }
}
