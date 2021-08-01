# 3. 카프카 기본 설명

## 로컬 설정

```
# 다운로드와 실행
wget https://archive.apache.org/dist/kafka/2.5.0/kafka_2.12-2.5.0.tgz
tar xvf kafka_2.12-2.5.0.tgz
cd kafka_2.12-2.5.0
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
bin/kafka-server-start.sh -daemon config/server.properties

# 토픽 생성
bin/kafka-topics.sh --create --bootstrap-server 127.0.0.1:9092 --topic hello.kafka
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --list
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --describe --topic hello.kafka
```

## 3.6 카프카 커넥트

* 데이터 파이프라인 생성 시 반복 작업을 줄이고 효율적인 전송을 이루기 위한 애플리케이션

* 커넥터의 종류
    1. 소스 커넥터 : 프로듀서 역할
    2. 싱크 커넥터 : 컨슈머 역할

![커넥트 구조](https://www.programmersought.com/images/708/5fcfe281db2eb505da1b5bfdd6024aec.png)

* 사용자가 커넥트에 커넥터 생성 명령을 내리면 커넥트는 내부에 커넥터와 태스크를 생성
    * 커넥터 : 태스크 관리
        * 컨버터 : 데이터 스키마 변경(json, string 등)
        * 트랜스폼 : 데이터 변환(key 제거 등)
    * 태스크 : 데이터 처리

#### 커넥트를 실행하는 방법

* 단일 모드, 분산 모드
    * 단일 모드 : 1개 프로세스만 실행, 고가용성 없음
    * 분산 모드 : 2대 이상의 서버에서 클러스터로 운영

* REST API 호출해서 현재 실행 중인 커넥트의 조회 가능
  https://docs.confluent.io/platform/current/connect/references/restapi.html

#### 단일 모드 커넥트

* 설정 파일 - config/connect-standalone.properties

```
# 카프카 클러스터 연동 정보
bootstrap.servers=localhost:9092

# 데이터 변환 설정
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true

# 커넥터의 데이터 처리 시점을 저장하는 오프셋 파일 위치(로컬에 저장)
offset.storage.file.filename=/tmp/connect.offsets

# 태스크가 처리한 오프셋 커밋 주기
offset.flush.interval.ms=10000

# 추가 플러그인 위치
# Examples: 
# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
#plugin.path=
```

* (예시)파일 소스 커넥터 설정 - config/connect-file-source.properties

```
# 커넥터 이름
name=local-file-source
# 사용할 커넥터 클래스
connector.class=FileStreamSource
# 커넥터로 실행할 태스크 개수(병렬 처리 설정용)
tasks.max=1
# 읽을 파일 위치
file=test.txt
# 읽은 파일의 데이터를 저장할 토픽 이름
topic=connect-test
```

#### 분산 모드 커넥트

* 설정 파일 - config/connect-distributed.properties

```
bootstrap.servers=localhost:9092

# 다수의 커넥트 프로세스를 묶을 그룹 이름
group.id=connect-cluster

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true

# 분산 모드에서는 오프셋 정보를 카프카 내부 토픽에 저장
offset.storage.topic=connect-offsets
offset.storage.replication.factor=1
config.storage.topic=connect-configs
config.storage.replication.factor=1
status.storage.topic=connect-status
status.storage.replication.factor=1

offset.flush.interval.ms=10000

# Examples: 
# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
```

* REST API 이용해 커넥트 관리

### 소스 커넥터

* 소스 애플리케이션 또는 소스 파일로부터 데이터를 가져와 토픽으로 넣어주는 역할

#### 직접 만들어보기

* 개발한 클래스와 더불어 참조하는 라이브러리도 함께 빌드하여 jar 생성 필요함

##### 의존성 추가

```
dependencies {
    compile('org.apache.kafka:connect-api:2.5.0')
}
```

##### 기본 커넥터 클래스 구조

```java
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

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

    // 태스크가 여럿일 경우 각 태스크에 적용할 설정 리스트
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
```

##### 기본 태스크 클래스 구조

```java
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
```

##### 커넥터 설정값 정의

```java
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
```

##### 커넥터 클래스 정의

```java
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
```

##### 커넥터 태스크 정의

```java
package com.example;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SingleFileSourceTask extends SourceTask {

    private Logger logger = LoggerFactory.getLogger(SingleFileSourceTask.class);

    public final String FILENAME_FIELD = "filename";
    public final String POSITION_FIELD = "position";

    private Map<String, String> fileNamePartition;
    private Map<String, Object> offset;
    private String topic;
    private String file;
    private long position = -1;


    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        try {
            // 설정 가져오기
            SingleFileSourceConnectorConfig config = new SingleFileSourceConnectorConfig(props);
            topic = config.getString(SingleFileSourceConnectorConfig.TOPIC_NAME);
            file = config.getString(SingleFileSourceConnectorConfig.DIR_FILE_NAME);
            fileNamePartition = Collections.singletonMap(FILENAME_FIELD, file);

            // 마지막으로 읽은 위치 가져오기
            offset = context.offsetStorageReader().offset(fileNamePartition);
            if (offset != null) {
                Object lastReadFileOffset = offset.get(POSITION_FIELD);
                if (lastReadFileOffset != null) {
                    position = (Long) lastReadFileOffset;
                }
            } else {
                position = 0;
            }
        } catch (Exception e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    // readLine 이후의 데이터를 읽어서 각 줄을 문자열로 변환 후 리스트에 적재
    private List<String> getLines(long readLine) throws Exception {
        BufferedReader reader = Files.newBufferedReader(Paths.get(file));
        return reader.lines().skip(readLine).collect(Collectors.toList());
    }

    // 태스크 시작 이후 데이터를 읽어오기 위해 반복적으로 호출
    // 데이터를 읽고 토픽으로 발송
    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> results = new ArrayList<>();
        try {
            Thread.sleep(1000);
            List<String> lines = getLines(position);
            if (lines.size() > 0) {
                lines.forEach(line -> {
                    Map<String, Long> sourceOffset = Collections.singletonMap(POSITION_FIELD, ++position);
                    // 토픽으로 보낼 데이터를 담는 객체
                    SourceRecord sourceRecord = new SourceRecord(
                            fileNamePartition,
                            sourceOffset,
                            topic,
                            Schema.STRING_SCHEMA,
                            line);
                    results.add(sourceRecord);
                });
            }
            return results;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ConnectException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {

    }
}
```

### 싱크 커넥터

* 토픽의 데이터를 타깃 애플리케이션 또는 파일로 저장

#### 직접 만들어보기

##### 의존성 추가

```
dependencies {
    compile('org.apache.kafka:connect-api:2.5.0')
}
```

##### 기본 커넥터 클래스 구조

* 소스 커넥터와 같은 구조

```java
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.List;
import java.util.Map;

public class TestSinkConnector extends SinkConnector {

    // 사용자가 입력한 설정값을 초기화
    @Override
    public void start(Map<String, String> props) {

    }

    // 커넥터가 사용할 태스크 클래스 리턴
    @Override
    public Class<? extends Task> taskClass() {
        return null;
    }

    // 태스크가 여럿일 경우 각 태스크에 적용할 설정 리스트
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
```

##### 기본 태스크 클래스 구조

```java
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
```

##### 커넥터 설정값 정의

```java
package com.example;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import java.util.Map;

public class SingleFileSinkConnectorConfig extends AbstractConfig {

    // 저장할 파일 위치 설정
    public static final String DIR_FILE_NAME = "file";
    private static final String DIR_FILE_NAME_DEFAULT_VALUE = "/tmp/kafka.txt";
    private static final String DIR_FILE_NAME_DOC = "저장할 디렉토리와 파일 이름";

    // 커넥터에 사용할 옵션값 정의
    // Importance enum 클래스는 사용자가 명시적으로 중요도를 알려주기 위한 용도
    public static ConfigDef CONFIG = new ConfigDef()
            .define(DIR_FILE_NAME, Type.STRING,
                    DIR_FILE_NAME_DEFAULT_VALUE, Importance.HIGH,
                    DIR_FILE_NAME_DOC);

    public SingleFileSinkConnectorConfig(Map<String, String> props) {
        super(CONFIG, props);
    }
}
```

##### 커넥터 클래스 정의

```java
package com.example;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFileSinkConnector extends SinkConnector {

    private Map<String, String> configProperties;

    @Override
    public String version() {
        return "1.0";
    }

    // 설정값 초기화
    @Override
    public void start(Map<String, String> props) {
        this.configProperties = props;
        try {
            new SingleFileSinkConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    // 사용할 태스크 클래스 정의
    @Override
    public Class<? extends Task> taskClass() {
        return SingleFileSinkTask.class;
    }

    // 태스크가 여럿일 경우 각 태스크에 적용할 설정 리스트
    // 예제에서는 모든 태스크가 같은 설정을 사용함
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        Map<String, String> taskProps = new HashMap<>();
        taskProps.putAll(configProperties);
        for (int i = 0; i < maxTasks; i++) {
            taskConfigs.add(taskProps);
        }
        return taskConfigs;
    }

    // 커넥터에서 사용할 설정값 저장
    @Override
    public ConfigDef config() {
        return SingleFileSinkConnectorConfig.CONFIG;
    }

    @Override
    public void stop() {
    }
}
```

##### 커넥터 태스크 정의

```java
package com.example;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class SingleFileSinkTask extends SinkTask {
    private SingleFileSinkConnectorConfig config;
    private File file;
    private FileWriter fileWriter;

    @Override
    public String version() {
        return "1.0";
    }

    // 리소스 초기화
    @Override
    public void start(Map<String, String> props) {
        try {
            config = new SingleFileSinkConnectorConfig(props);
            file = new File(config.getString(SingleFileSinkConnectorConfig.DIR_FILE_NAME));
            fileWriter = new FileWriter(file, true);
        } catch (Exception e) {
            throw new ConnectException(e.getMessage(), e);
        }

    }

    // 읽어온 데이터를 버퍼에 저장
    @Override
    public void put(Collection<SinkRecord> records) {
        try {
            for (SinkRecord record : records) {
                fileWriter.write(record.value().toString() + "\n");
            }
        } catch (IOException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    // 실제 파일 시스템에 저장
    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
        try {
            fileWriter.flush();
        } catch (IOException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }
}
```
