package com.server.domain.push.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import com.server.domain.member.entity.Member;
import com.server.domain.push.entity.Push;
import com.server.domain.push.repository.PushRepository;
import com.server.domain.push.template.PushTemplate;
import com.server.domain.push.template.PushTemplateConstructor;
import com.server.domain.schedule.entity.Schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {
    private final PushRepository pushRepository;
    private final PushTemplateConstructor pushTemplateConstructor;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public Push savePush(Push push) {
        String token = push.getPushToken();
        Member member = push.getMember();
        Push foundPush = member.getPush();

        if (foundPush != null) {
            foundPush.setPushToken(token);
            return pushRepository.save(foundPush);
        }
        pushRepository.save(push);
        sendWelcomeMessage(token, member);

        return push;
    }

    @Async
    public void sendPush(PushTemplate pushTemplate) {
        Message requestMessage = Message.builder()
                .setToken(pushTemplate.getToken())
                .setNotification(Notification.builder()
                        .setTitle(pushTemplate.getTitle())
                        .setBody(pushTemplate.getBody())
                        .setImage(pushTemplate.getImageUrl())
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.withLink(pushTemplate.getUrl()))
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(requestMessage);
        } catch (FirebaseMessagingException e) {
            log.error("### 푸시 에러: ", e.getMessage());
            // throw new CustomException(ExceptionCode.PUSH_FAILD);
        }

    }

    @Async
    public void sendWelcomeMessage(String token, Member member) {
        String nickname = member.getNickname();
        PushTemplate pushTemplate = pushTemplateConstructor
                .getWelcomeTemplate(token, nickname);

        sendPush(pushTemplate);
    }

    @Async
    public void sendPostScheduleMessage(Schedule schedule) {
        // Member
        Member member = schedule.getMember();
        Push push = member.getPush();

        if (push == null) {
            return;
        }

        PushTemplate pushTemplate = pushTemplateConstructor
                .getPostScheduleTemplate(schedule, member, push);

        sendPush(pushTemplate);
    }

    @Async
    public void sendScheduledMessage(Schedule schedule, Member member, int hour) {
        Push push = member.getPush();

        if (push == null) {
            return;
        }

        PushTemplate pushTemplate = pushTemplateConstructor
                .getScheduledTemplate(schedule, member, push, hour);

        sendPush(pushTemplate);
    }

    // 이벤트용
    @Async
    public void sendEventMessage(Member member, Push push, long giftId) {
        String token = push.getPushToken();
        String nickname = member.getNickname();
        PushTemplate pushTemplate = pushTemplateConstructor
                .getEventTemplate(token, nickname, giftId);

        sendPush(pushTemplate);
    }

    // 이벤트용
    @Async
    public void sendNoticeMessage(String title, String message) {
        List<Push> pushs = pushRepository.findAll();

        for (Push push : pushs) {
            Member member = push.getMember();
            String nickname = member.getNickname();
            String token = push.getPushToken();
            PushTemplate pushTemplate = pushTemplateConstructor
                    .getNoticeTemplate(token, nickname, title, message);
            sendPush(pushTemplate);
        }

    }
}
