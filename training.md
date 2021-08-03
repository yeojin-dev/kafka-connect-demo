# 실습

## 카프카 실행

```shell
wget https://archive.apache.org/dist/kafka/2.5.0/kafka_2.12-2.5.0.tgz
tar xvf kafka_2.12-2.5.0.tgz
cd kafka_2.12-2.5.0
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
bin/kafka-server-start.sh -daemon config/server.properties
# 커넥트는 끄기가 귀찮아서 그냥 실행(이후 실습은 다른 셸 사용)
bin/connect-distributed.sh config/connect-distributed.properties
```

## connect rest api 실행

* 포스트맨 셋업
    * [포스트맨 링크](https://www.getpostman.com/collections/62e32d708a559beb46ed)

* 실행해보기
    1. 커넥터 플러그인 리스트
    2. 커넥터 리스트

## 실습용 커넥터 실행

### 기본

* main 패키지 jar 빌드
    * 빌드가 어려우면 미리 빌드해놓은 jar 사용
    
* 카프카 커넥트 사용 중지하고 커넥트 설정 수정

```shell
vim config/connect-distributed.properties
```

```
# jar 위치한 폴더 경로 입력(아래는 예시)
plugin.path=/Users/demo1/out/artifacts/demo1_main_jar
```

* 커넥트 재실행

* 커넥터 플러그인 리스트에 새로 커넥터 추가한 것 확인

* 소스, 싱크용 파일 미리 생성

```shell
touch /tmp/sink.txt
touch /tmp/source.txt
```

* SingleFileSourceConnector, SingleFileSinkConnector 생성
    * 포스트맨의 커넥터 추가 사용
    * body 데이터를 커넥터 소스 코드와 비교하기
    * 소스에는 topic 설정, 싱크에는 topics 설정인 것에 주의

* 커넥터 리스트, 커넥터 개별 조회, 커넥터 상태 조회로 커넥터 생성 완료했는지 확인

* 소스용 파일 업데이트

```shell
echo "test" >> /tmp/source.txt
```

* 싱크용 파일 업데이트 확인
    * 약간 시간차가 있음

```shell
cat /tmp/sink.txt
```

### 토픽 확인

* 커넥트용 토픽과 직접 추가한 커넥터용 토픽이 추가된 것 확인

```shell
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
# __consumer_offsets
# connect-configs
# connect-offsets
# connect-status
# test.topic
```

### 싱크 커넥터 2개

* 싱크용 추가 파일 미리 생성

```shell
touch /tmp/sink2.txt
```

* SingleFileSinkConnector 생성
    * file 값 수정해 생성
    
* 소스용 파일 업데이트

```shell
echo "hahaha" >> /tmp/source.txt
```

* 싱크용 파일 업데이트 확인
    * 약간 시간차가 있음

```shell
cat /tmp/sink.txt
cat /tmp/sink2.txt
```

## 카프카 종료

* 커넥터 모두 제거

* 커넥트는 데몬이 아니라 그냥 종료

* 카프카 서버, 주키퍼 종료

```shell
bin/kafka-server-stop.sh
bin/zookeeper-server-stop.sh 
```