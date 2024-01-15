# Trouble Shooting
프로젝트를 진행하면서 발생한 문제점들과 해결법 서술합니다.
  
## 만료된 Access token으로 계속해서 요청 가능한 문제
### 배경
유저가 로그인에 성공하면 `accessToken`과 `refreshToken`을 발급해 줍니다. 
`refreshToken`은 레디스에 보관하고 `accessToken`만 응답으로 보냅니다.

### 문제
문제가 발생한 토큰 재발급 과정은 이러합니다. 

1. 토큰 검증 시 `accessToken`이 만료되면 `accessToken`을 파싱하여 유저 정보를 꺼내온다. 
2. 해당 유저 정보로 레디스에서 `refreshToken`을 조회한다.
3. `refreshToken`에 대해 검증 한 뒤 `accessToken`을 재발급한다.
4. `refreshToken`이 없을 시(만료된 상태) 인증 에러를 응답한다. -> 재로그인 필요

위 과정에서 놓친 부분은 만료된 `accessToken`에서 유저 정보를 꺼내오기 때문에 `refreshToken`이 만료되기 전까지 
`accessToken`이 만료되었더라도 계속해서 재발급하여 요청을 처리할 수 있게 되는 로직이었습니다. 

### 해결
레디스의 접근을 한 번이라도 줄이기 위해서 `accessToken`을 파싱하여 유저 정보를 얻는 방법을 선택했었는데 이 생각이 잘못된 판단이었습니다.

1. 토큰 검증 시 현재 요청받은 `accessToken`으로 `refreshToken`을 조회합니다. 
2. `refreshToken`에 대해 검증 한 뒤 `accessToken`을 재발급하고, 재발급된 `accessToken`을 key로 `refreshToken`을 업데이트 합니다.
3. 이렇게 되면 `refreshToken`에 대한 key가 유일해지기 때문에 직전에 발급한 `accessToken`이 아니면 재발급 할 수 없게 됩니다.
4. 추가로 토큰 상태에 대한 예외처리도 분명해지는 개선이 있었습니다.

참고 PR([#3](https://github.com/do5do/funding/pull/3#issue-2057987361))

## 펀딩 상품 목록 조회 시 N+1 문제
### 배경
펀딩 상품은 M개의 리워드와 R개의 이미지를 `@OneToMany` 관계로 가지고 있습니다. 
펀딩 상품 목록을 조회할 때 연관된 컬렉션까지 모두 조회하여 페이징 처리해야 합니다.

### 문제
최적화 이전 펀딩 상품 목록 조회 시 발생하는 쿼리는 이러합니다.

- 상품 목록인 루트 조회 1회
- 각 상품의 컬렉션인 리워드 조회 1회 + 이미지 조회 1회로 합 2회
- 최종적으로 상품 목록 api 호출 시 상품의 개수(N)만큼 1 + N * 2회의 쿼리가 발생

만약 상품 목록이 1000개라면 총 2001번의 쿼리가 나가게 됩니다. 

### 해결
먼저 상품 목록 조회 시 동적 쿼리를 생성하고, 휴먼 에러를 줄이기 위해 Querydsl을 사용하고 있었습니다.

- 1차 시도
  - 상품 목록 조회 쿼리에서 컬렉션까지 fetch join으로 모두 조회
  - 하지만 컬렉션이 하나 이상이어서 fetch join 불가
    - MultipleBagFetchException 발생 -> 둘 이상의 컬렉션(Bag)을 fetch join하는 경우 결과가 카다시안 곱이기 때문에 유효한 값을 판단할 수 없어서 발생하는 에러
    - (+) 이후에 알게되었는데, 컬렉션을 List가 아닌 Set으로 받으면 처리가 가능하다고 한다.
- 2차 시도
  - hibernate.default_batch_fetch_size로 컬렉션 조회 최적화
  - 컬렉션은 지연 로딩으로 프록시가 초기화 되며 조회해온다.
  - 이때, 위 설정으로 컬렉션을 지정한 사이즈만큼 in 절로 한번에 조회하게 된다. 
  - 결과적으로 루트 조회 1회 + 컬렉션 조회 1회로 총 2회의 쿼리 발생

참고 PR([#4](https://github.com/do5do/funding/pull/4#issue-2060121406))

## 리워드 재고 수량 동시성 제어 시 트랜잭션 커밋 시점 이슈
### 배경
펀딩 상품 리워드의 경우 수량이 정해져있으며 모두 선착순입니다. 
유저가 동시에 접근하는 경우를 고려하여 Redisson을 이용한 분산 락을 적용하였고, 사용 편의를 위해 AOP(`@Aspect`)로 처리하였습니다.

### 문제
재고 수량 감소를 위해 lock을 획득하고, 해제하는 과정은 올바르게 진행 되었지만 재고 수량의 정합성이 맞지 않았습니다. 
이유는 이러합니다. <br>

재고 수량: `100개` <br>
동시 요청 유저: `A`, `B`

- A 유저 lock 획득 -> 재고 수량 감소(99) -> lock 해제 -> 트랜잭션 커밋 (재고 수량 업데이트)
- A 유저가 lock을 해제한 뒤 바로 B 유저 lock 획득 -> A 유저의 트랜잭션이 커밋전이라 재고 수량은 100이 조회 된다. (데이터 정합성 오류)

### 해결
lock을 획득/해제하는 과정과 재고 수량을 감소하는 과정이 하나의 트랜잭션으로 처리 되었기 때문에 
모든 로직이 완료되고 나서 커밋이 발생하는 것이 문제였습니다.

- 1차 시도
  - 기존 펀딩하기 메소드에서 열던 트랜잭션을 새로운 트랜잭션으로 열 수 있도록 수정 `@Transactional(propagation = Propagation.REQUIRES_NEW)`
  - 이미 AOP 프록시 객체인 상태라 하나의 트랜잭션이라서 설정 값이 적용되지 않음
- 2차 시도
  - Aspect 클래스 내에서 `joinPoint.proceed()`로 실제 객체의 메소드를 호출할 때 새로운 트랜잭션을 열 수 있도록 트랜잭션을 여는 중간 객체를 하나 더 두는 방식
    ```java
    // lock 처리를 하는 Aspect 클래스
    @RequiredArgsConstructor
    @Component
    @Aspect
    public class DistributedLockAspect {
    
        private final RedissonLockService redissonLockService;
        private final LockCallNewTransaction lockCallNewTransaction;
    
        @Around("@annotation(com.zerobase.funding.api.lock.annotation.DistributedLock)")
        public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            DistributedLock annotation = method.getAnnotation(DistributedLock.class);
            String key = getLockKey(joinPoint, annotation);
            redissonLockService.lock(key, annotation.waitTime(), annotation.leasTime());
    
            try {
                return lockCallNewTransaction.proceed(joinPoint); // 중간 클래스를 통해 실제 메서드 호출
            } finally {
                redissonLockService.unlock(key);
            }
        }
    }
    
    // 중간 클래스
    @Component
    public class LockCallNewTransaction {
    
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
            return joinPoint.proceed();
        }
    }
    ```
  - 테스트 코드를 작성하여 100개의 스레드로 테스트해본 결과 90%의 스레드들이 주어진 시간 내에 lock을 획득하지 못해서 실패
  - (wait time을 5초보다 더 준다면 수행 속도가 현저히 떨어졌다.)
- 3차 시도
  - 트랜잭션을 새로 열지 않고 기존 트랜잭션 내에서 `saveAndFlush()`로 저장하는 동시에 커밋을 시켰다.
  - 테스트 시 현저하게 빠른 속도록 성공

### 피드백
피드백을 받고 알게되었는데, 위에서 해결한 3차 시도의 방법은 **다중 서버인 환경에서는 기존과 동일하게 레이스 컨디션이 발생**하게 됩니다. 
(다중 서버에서 레이서 컨디션이 발생하는 이유 : 기존의 A, B 스레드를 A, B 서버라고 생각하면 된다.) <br>
결론적으로는 2차 시도 방법을 적용하였고, 테스트 시 실패하는 원인에 대해 다시 확인하였습니다.

- 트랜잭션을 새로 열면서 기존보다 커넥션이 하나 더 필요하게 되었고, 이로인해 커넥션이 말라버려서 최종 실패로 이어지게 되는 것을 로그를 통해 확인
- hikari의 기본 풀 사이즈는 10개 -> 풀 사이즈를 늘려서 테스트 시도 -> 성공

참고 PR([#5](https://github.com/do5do/funding/pull/5#issue-2062334669))

## 스프링 배치 ItemWriter에서 List로 받기
### 배경
펀딩 종료 시점에 처리해야하는 이벤트가 있어서 스프링 배치를 활용하였습니다.

### 문제
펀딩 성공/실패 여부를 알기위해 `ItemProcessor`에서 펀딩 상품의 각 리워드가 가진 모든 펀딩을 조회한 뒤 
상태 변경 및 이벤트 처리 후 `ItemWriter`에게 List로 return해야 했습니다.  

- 처음엔 인자 값을 받는데 문제가 없길래 `List<List<T>>`로 write를 시도했지만 ArrayList라서 정상 처리가 되지 않았습니다. (에러 로그를 통해 확인) 

### 해결
`ItemWriter`는 리스트 형태의 결과 값을 리턴 받도록 되어 있습니다. -> `write(Chunk<? extends T> items)` <br>
하지만 받아야하는 결과 값은 `List<List<T>>`로 즉, `Chunk<Chunk<T>>`의 형태로 넘겨주어야 했습니다. 

**접근 방법**
- `ItemWriter`의 `write()` 메서드를 재정의 하여 입력받은 `Chunk<Chunk<T>>` 타입을 `Chunk<T>`로 재가공합니다.
- `Chunk<T>`를 인자로 받는 `ItemWriter`를 생성하여 해당 writer가 재가공한 chunk 리스트를 write하도록 하여 정상 처리되도록 합니다.
    ```java
    public class FundingJobConfig {
        // ...
        
        @Bean(name = BeanPrefix.FUNDING_ENDED + "Writer")
        public JpaItemWriter<List<Funding>> jpaItemWriter() {
            JpaItemWriter<List<Funding>> writer = new JpaItemWriter<>() {
                @Override
                public void write(Chunk<? extends List<Funding>> chunk) {
                    // 재가공
                    Chunk<Funding> total = new Chunk<>();
    
                    for (List<Funding> fundings : chunk.getItems()) {
                        for (Funding funding : fundings) {
                            total.add(funding);
                        }
                    }
    
                    // 재가공한 데이터 write
                    JpaItemWriter<Funding> delegator = new JpaItemWriter<>();
                    delegator.setEntityManagerFactory(entityManagerFactory);
                    delegator.write(total);
                }
            };
    
            writer.setEntityManagerFactory(entityManagerFactory);
            return writer;
        }
    }
    ```
[참고 자료](https://jojoldu.tistory.com/140) <br>
배치 관련 PR([#6](https://github.com/do5do/funding/pull/6#issue-2068716047))

## SSE 알림 처리 시 access deny 에러
### 배경
실시간 알림 발송을 위해 Server Sent Event(SSE)를 적용하여 구현하였습니다.

### 문제
SSE 연결이 만료될 때 해당 thread가 timeout이 되면서 Access Deny 에러가 발생했습니다. <br>
`SseEmitter`에서는 쓰레드를 `Runable`로 생성하여 `run()`을 하는데, 
이때 쓰레드 간 인증 객체(`SecurityContext`)를 공유하지 못해서 인증 실패로 빠지게 된다고 판단되었습니다. 

### 해결
SSE 구독 API는 로그인 직후 클라이언트에서 요청하는 api입니다. 
SSE 커넥션 이후 만료 시 마다 계속해서 인증 검증을 할 필요는 없다고 생각됐습니다.

**대안**
- SSE 구독 API의 permission을 허용합니다.
- 클라이언트는 구독 요청 시 이전과 같이 헤더에 인증 토큰을 실어 보냅니다.
- permission을 허용하는 것은 인증 처리가 되지 않아도 허용하겠다는 것이며, 토큰이 있는 상태라면 동일하게 `TokenAuthenticationFilter`의 로직을 타고 인증 객체를 생성할 것 입니다.
- 결론적으로 전과 같이 DB 조회없이 인증 객체에서 유저 정보를 얻어 SSE 구독 처리를 하면서, timeout 되는 쓰레드에서 예외가 발생하지 않도록 변경하였습니다.

참고 PR([#9](https://github.com/do5do/funding/pull/9#issue-2081190606))

## nginx not found 에러
### 배경
서버 아키텍처를 구성하면서 nginx를 프록시로 두었습니다.

### 문제
nginx로 요청이 오면 서비스 애플리케이션으로 포워딩할 수 있도록 기본 설정을 하고, 
로컬에서 docker-compose로 띄워 포워딩이 올바르게 잘 되는지 테스트를 하였습니다. 
그런데 루트 경로를 제외한 다른 모든 경로를 찾지 못하는 404, not found 에러를 겪었습니다.

**설정 방법**
- nginx의 기본 설정 파일인 `/etc/nginx/nginx.conf`를 토대로 포워딩 부분만 추가하여 docker continer가 뜰 때 마운트 해줬습니다.
    ```nginx configuration
    # nginx.conf를 바탕으로 아래 설정 추가
    upstream funding-server { # backend upstream
      server funding-app:8080;
    }

    server {
      listen 80;
      
      location / { # 해당 경로의 모든 요청을 backend로 forward
        proxy_pass http://funding-server;
        proxy_redirect off; # 리다이렉트는 설정된 그대로 전달
        proxy_set_header Host $host; # 요청 헤더에서 넘어온 host를 전달, 없다면 server_name으로 전달
        proxy_set_header X-Real-IP $remote_addr; # 클라이언트의 IP 주소를 전달
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; # IP 주소를 ,로 구분하여 추가
        proxy_set_header X-Forwarded-Host $server_name; # 클라이언트의 호스트 이름 식별을 위함
      }

      location /notification/subscribe { # sse connection 설정
        proxy_pass http://funding-server/notification/subscribe;
        proxy_set_header Connection ''; # 지속 연결을 위함
        proxy_http_version 1.1; # http 버전
        proxy_buffering off; # 응답을 버퍼에 저장해두지 않는다. (응답 실시간 성을 위함)
      }
    }
    ```

### 해결
에러 로그를 확인해보니 proxy_pass로 설정한 경로가 아닌 다른 경로로 포워딩 되고 있었습니다.

- 설정 파일을 다시 확인 -> 하단에서 include하는 default 설정 확인 -> `include /etc/nginx/conf.d/*.conf;`
- 해당 경로의 `/etc/nginx/conf.d/default.conf`를 확인 -> '/' 로 오는 모든 요청을 에러에서 확인한 경로(`/usr/share/nginx/html`)로 포워딩하는 설정 발견
- 새로 작성하였던 config 파일에서 include를 제외시켜서 해결
- (+) 이후 알게 되었는데 기본 config는 건드리지 않고, 위에서 제외 시킨 include되는 경로에서 서버 관련 설정을 하여 포함시킨다고 한다.

**회고**<br>
좀 더 학습하여 적용했어야 했는데, 배포 과정에서는 시간이 촉박하여 바로 진행했던게 이런 기초적인 실수를 불러 일으킨 것 같다.

참고 PR([#8](https://github.com/do5do/funding/pull/8#issue-2080356392))