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

    //chapGPT 활용 포트폴리오 자동생성
    @PostMapping(path="/auth/create-by-ai", headers = "X-Authorization-Id")
    public ApiResponse<PortfolioSaveRequestDto> createByAi(@RequestHeader("X-Authorization-Id") String memberId, @RequestParam String careerField, @RequestParam String title, @RequestBody ParticipatedNoticeDto participatedNoticeDto) throws JsonProcessingException {
            PortfolioSaveRequestDto result = new PortfolioSaveRequestDto(title, gptService.getAssistantMsg(title, careerField, participatedNoticeDto));
            Long resultId = portfolioService.save(memberId, result);
            return ApiResponse.createSuccess(result);
    }


    //회원의 포트폴리오 생성
    @PostMapping(path="/new")
    public ApiResponse<Long> savePortfolio(@RequestParam String memberId, @RequestBody PortfolioSaveRequestDto requestDto) {

        Bucket bucket = rateLimiterService.resolveBucket(memberId);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long saveToken = probe.getRemainingTokens();

        if(probe.isConsumed()) {
            log.info("API Call Success");
            log.info("Available Token : {}", saveToken);

            Long result = portfolioService.save(memberId, requestDto);
            if(result == -1L) {
                return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
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
    @PostMapping(path="/auth/new",  headers = "X-Authorization-Id")
    public ApiResponse<Long> save(@RequestHeader("X-Authorization-Id") String memberId, @RequestBody PortfolioSaveRequestDto requestDto) {

        //Member member = userService.getLoginUserByLoginId(auth.getName());

        Long result = portfolioService.save(memberId, requestDto);
        if(result == -1L) {
            return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }
        return ApiResponse.createSuccess(result);
    }

    //회원의 포트폴리오 업데이트
    @PostMapping("/update/id")
    public ApiResponse<Long> update(@RequestParam Long id, @RequestBody PortfolioUpdateRequestDto requestDto) {
        Long result = portfolioService.update(id, requestDto);
        if(result == -1L) {
            return (ApiResponse<Long>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }
        return ApiResponse.createSuccess(result);
    }

    //회원의 포트폴리오 포트폴리오id(PK) 로 조회
    @GetMapping("/id")
    public ApiResponse<PortfolioResponseDto> findById(@RequestParam Long id) {
        PortfolioResponseDto result = portfolioService.findById(id);
        if(result == null) {
            return (ApiResponse<PortfolioResponseDto>) ApiResponse.createError("포트폴리오 업데이트에 실패했습니다.");
        }

        return ApiResponse.createSuccess(result);
    }

    //로그인한 회원의 포트폴리오 조회
    @GetMapping(path = "/auth/portfoliolist", headers = "X-Authorization-Id")
    public ApiResponse<List<PortfolioListResponseDto>> findMemberPortfolioList(@RequestHeader("X-Authorization-Id") String memberId){

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
