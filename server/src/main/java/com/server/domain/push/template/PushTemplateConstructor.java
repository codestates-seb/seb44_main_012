package com.server.domain.push.template;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.server.domain.member.entity.Member;
import com.server.domain.push.entity.Push;
import com.server.domain.region.entity.Region;
import com.server.domain.schedule.entity.Schedule;
import com.server.domain.schedule.service.ScheduleService;
import com.server.global.utils.CustomRandom;

@Component
public class PushTemplateConstructor {
    @Lazy
    @Autowired
    private ScheduleService scheduleService;

    @Value("${share.key}")
    private String shareSecretKey;

    @Value("${url.server}")
    private String serverUrl;

    public PushTemplate getWelcomeTemplate(String token, String nickname) {
        PushTemplate pushTemplate = PushTemplate.builder()
                .token(token)
                .title(String.format("%s님의 가입을 환영합니다!", nickname))
                .body("PliP과 함께 여행 일정을 작성하러 가볼까요?")
                .build();

        return pushTemplate;
    }

    public PushTemplate getPostScheduleTemplate(Schedule schedule,
            Member member,
            Push push) {
        // Member member
        String nickname = member.getNickname();

        // Schedule
        long scheduleId = schedule.getScheduleId();
        Region region = schedule.getRegion();
        String engName = region.getEngName();
        String korName = region.getKorName();
        LocalDate startDate = schedule.getStartDate();
        LocalDate endDate = schedule.getEndDate();
        int period = schedule.getPeriod();
        String term = period == 1 ? "당일치기" : String.format("%d박 %d일", period - 1, period);

        // PushTemplate
        String shareUrl = scheduleService.createShareUrl(scheduleId, member);
        PushTemplate pushTemplate = PushTemplate.builder()
                .token(push.getPushToken())
                .title(String.format("%s님의 %s 여행 일정입니다.", nickname, korName))
                .body(String.format("기간: %s ~ %s (%s)", startDate, endDate, term))
                .imageUrl(CustomRandom.getCustomRegionUrl(engName))
                .url(shareUrl)
                .build();

        return pushTemplate;
    }

    public PushTemplate getScheduledTemplate(Schedule schedule,
            Member member,
            Push push,
            int hour) {
        // Member
        String nickname = member.getNickname();

        // Schedule
        long scheduleId = schedule.getScheduleId();

        // Region
        Region region = schedule.getRegion();
        String engName = region.getEngName();
        String korName = region.getKorName();

        String title;
        String body;
        String shareUrl = null;

        if (hour == 22) {
            title = String.format("%s님! %s 여행은 즐거우셨나요?",
                nickname,
                korName);
            body = "클릭하여 일지를 작성하러 가볼까요?";
        } else {
            String prefix = hour == 7 ? "오늘" : "내일";
            title = String.format("%s님! %s은 설레는 %s 여행날이에요!",
                nickname,
                prefix,
                korName);
            body = "클릭 시 일정 상세 정보로 이동합니다.";
            shareUrl = scheduleService.createShareUrl(scheduleId, member);
        }

        PushTemplate pushTemplate = PushTemplate.builder()
                .token(push.getPushToken())
                .title(title)
                .body(body)
                .imageUrl(CustomRandom.getCustomRegionUrl(engName))
                .url(shareUrl)
                .build();

        return pushTemplate;
    }

    public PushTemplate getDeleteScheduleTemplate(Schedule schedule) {
        return null;
    }

    // 이벤트용
    public PushTemplate getEventTemplate(String token, String nickname, long giftId) {
        PushTemplate pushTemplate = PushTemplate.builder()
                .token(token)
                .title(String.format("%s님 사탕이 도착했어요~", nickname))
                .body(String.format("선착순 이벤트에 %d등으로 참여하셨습니다.", giftId))
                .imageUrl(String.format("%s/files/images/gifts?id=%d", serverUrl, giftId))
                .build();

        return pushTemplate;
    }

    // 이벤트용
    public PushTemplate getNoticeTemplate(String token, String nickname, String title, String message) {
        PushTemplate pushTemplate = PushTemplate.builder()
                .token(token)
                .title(title)
                .body(String.format("%s님 %s", nickname, message))
                .imageUrl(serverUrl + "/files/images/gifts?id=999")
                .url(serverUrl + "/events")
                .build();

        return pushTemplate;
    }
}
