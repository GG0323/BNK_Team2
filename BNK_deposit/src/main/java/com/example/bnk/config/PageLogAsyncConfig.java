package com.example.bnk.config;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class PageLogAsyncConfig {
	// 서비스의 비동기 함수 설정 config
	// @Async("pageLogExecutor") 로 서비스에서 호출
    @Bean(name = "pageLogExecutor")
    public Executor pageLogExecutor() {
    	
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("page-log-"); // 스레드 이름
        executor.setCorePoolSize(2);               // 평상시 스레드 수 (로그 INSERT 는 가벼워서 2면 충분)
        executor.setMaxPoolSize(4);                // 큐가 가득 찼을 때 늘어날 수 있는 최대치
        executor.setQueueCapacity(500);            // 대기 큐 — DB가 잠깐 느려져도 여기 쌓였다가 처리됨

        // 큐까지 가득 찬 비상 상황: 로그는 유실을 감수하고 버린다.
        // (CallerRunsPolicy 를 쓰면 유실은 없지만 요청 스레드가 INSERT 를 떠안아
        //  "응답 지연 제거"라는 비동기 전환 목적이 깨진다 → 로그 용도에선 버리는 쪽이 맞음)
        RejectedExecutionHandler discardWithNotice = (r, e) ->
                System.out.println("[페이지로그] 큐 포화로 로그 1건 유실 (queue=" + e.getQueue().size() + ")");
        executor.setRejectedExecutionHandler(discardWithNotice);
    
        // 서버 종료 시 큐에 남은 로그를 최대 10초까지 마저 저장하고 종료
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        executor.initialize();
        return executor;
    }
    
    
    
    
    
}
