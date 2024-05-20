package com.hisujung.microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hisujung.microservice.ApiResponse;
import com.hisujung.microservice.dto.*;
import com.hisujung.microservice.service.GptServiceImpl;
import com.hisujung.microservice.service.PortfolioService;
import com.hisujung.microservice.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/portfolio")
public class PortfolioApiController {

    private final PortfolioService portfolioService;
    private final GptServiceImpl gptService;
    private final RateLimiterService rateLimiterService;


    public String fetchNoticeCheckedList() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://IP-address:8080/notice/checked-list", String.class);
        return response.getBody();
    }

    // 처리율 제한 장치 적용하려는 API
    @PostMapping("/create-by-ai")
    public ApiResponse<Long> createByAi(@RequestBody ActivitiesDto dto, Authentication auth) throws JsonProcessingException {

        String memberId = auth.getName();

        Bucket bucket = rateLimiterService.resolveBucket(memberId);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long saveToken = probe.getRemainingTokens();

        if(probe.isConsumed()) {
            log.info("API Call Success");
            log.info("Available Token : {}", saveToken);

            // 변경된 부분 시작
            String noticeCheckedList = fetchNoticeCheckedList();
            Long result = portfolioService.save(memberId, dto.getPortfolioTitle(), gptService.getAssistantMsg(noticeCheckedList, dto.getCareerField()));
            // 변경된 부분 끝

            if(result == -1L) {
                return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 생성에 실패했습니다.");
            }
            return ApiResponse.createSuccess(result);
        }

        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

        log.info("TOO MANY REQUEST");
        log.info("Available Token : {}", saveToken);
        log.info("Wait Time {} Second ", waitForRefill);

        return (ApiResponse<Long>) ApiResponse.createError("HttpStatus.TOO_MANY_REQUESTS");
    }




    //회원의 포트폴리오 생성
    @PostMapping("/new")
    public ApiResponse<Long> save(@RequestBody PortfolioSaveRequestDto requestDto) {

        //Member member = userService.getLoginUserByLoginId(auth.getName());

        Long result = portfolioService.save(requestDto);
        if(result == -1L) {
            return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }
        return ApiResponse.createSuccess(result);
    }

    //회원의 포트폴리오 업데이트
    @PostMapping("update/id")
    public ApiResponse<Long> update(@RequestParam Long id, @RequestBody PortfolioUpdateRequestDto requestDto) {
        Long result = portfolioService.update(id, requestDto);
        if(result == -1L) {
            return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }
        return ApiResponse.createSuccess(result);
    }

    //회원의 포트폴리오 포트폴리오id(PK) 로 조회
    @GetMapping("id")
    public ApiResponse<PortfolioResponseDto> findById(@RequestParam Long id) {
        PortfolioResponseDto result = portfolioService.findById(id);
        if(result == null) {
            return (ApiResponse<PortfolioResponseDto>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }

        return ApiResponse.createSuccess(result);
    }

    //로그인한 회원의 포트폴리오 조회
    @GetMapping("/portfoliolist")
    public ApiResponse<List<PortfolioListResponseDto>> findMemberPortfolioList(@RequestParam String memberId){

        List<PortfolioListResponseDto> resultList = portfolioService.findAllDescByMember(memberId);
        if(resultList == null) {
            return (ApiResponse<List<PortfolioListResponseDto>>)ApiResponse.createError("포트폴리오가 존재하지 않습니다.");
        }
        return ApiResponse.createSuccess(resultList);
    }


    //포트폴리오 삭제
    @DeleteMapping("/portfolio/id")
    public ApiResponse<Long> delete(@RequestParam Long id) {
        portfolioService.delete(id);
        return ApiResponse.createSuccess(id);
    }
}
