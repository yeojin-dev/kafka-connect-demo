import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import java.util.Collection;
import java.util.Map;

public class TestSinkTask extends SinkTask {

    // 태스크의 버전을 리턴
    @Override
    public String version() {
        return null;
    }

    // 태스크 시작 로직 - 데이터 처리에 필요한 모든 리소스 초기화
    @Override
    public void start(Map<String, String> props) {

    }

    // 저장할 데이터를 토픽에서 주기적으로 읽어오는 함수
    @Override
    public void put(Collection<SinkRecord> records) {

    }

    // put() 메서드에서 가져온 데이터를 주기적으로 저장할 때 사용
    // put() 메서드 내부에서 저장할 수도 있기 때문에 이 메소드는 반드시 구현하지 않아도 됨
    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {

    }

    // 종료 로직
    @Override
    public void stop() {

    }
}
