package kyonggi.bookslyserver.domain.user.service;

import kyonggi.bookslyserver.domain.user.dto.request.VerifyCodeRequestDto;
import kyonggi.bookslyserver.domain.user.dto.request.SendSMSRequestDto;
import kyonggi.bookslyserver.domain.user.dto.response.VerifyCodeResponseDto;
import kyonggi.bookslyserver.domain.user.dto.response.SendSMSResponseDto;
import kyonggi.bookslyserver.domain.user.entity.User;
import kyonggi.bookslyserver.domain.user.repository.UserRepository;
import kyonggi.bookslyserver.global.util.AuthUtil;
import kyonggi.bookslyserver.global.error.ErrorCode;
import kyonggi.bookslyserver.global.error.exception.CustomNurigoException;
import kyonggi.bookslyserver.global.error.exception.EntityNotFoundException;
import kyonggi.bookslyserver.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoBadRequestException;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthService {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final RedisUtil redisUtil;

    public SendSMSResponseDto sendMessage(SendSMSRequestDto sendSMSRequestDto) {

        String receivingNumber = sendSMSRequestDto.receivingNumber();
        int authCode = authUtil.createAuthCode();

        SingleMessageSentResponse messageSentResponse = null;

        try {
            messageSentResponse = authUtil.sendOneSMS(receivingNumber, authCode);
        } catch (NurigoBadRequestException e) {
            throw new CustomNurigoException();
        }

        //인증 시간 5분 설정
        redisUtil.setDataExpire(String.valueOf(authCode), receivingNumber,  60 * 5L);

        return SendSMSResponseDto.builder()
                .statusCode(messageSentResponse.getStatusCode())
                .sendTo(messageSentResponse.getTo())
                .sendFrom(messageSentResponse.getFrom())
                .statusMessage(messageSentResponse.getStatusMessage()).build();
    }

    public SendSMSResponseDto sendMessageToUser(Long userId, SendSMSRequestDto sendSMSRequestDto) {

        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));

        return sendMessage(sendSMSRequestDto);
    }

    public VerifyCodeResponseDto ownerVerifyByCode(VerifyCodeRequestDto verifyCodeRequestDto) {

        String code = verifyCodeRequestDto.code();
        String receivingNumber = redisUtil.getValues(code);

        if (receivingNumber != null && receivingNumber.equals(verifyCodeRequestDto.receivingNumber())) {
            return VerifyCodeResponseDto.builder()
                    .isVerify(true).build();}

        return VerifyCodeResponseDto.builder()
                .isVerify(false).build();
    }

    public VerifyCodeResponseDto userVerifyByCode(Long userId, VerifyCodeRequestDto verifyCodeRequestDto) {

        String code = verifyCodeRequestDto.code();
        String receivingNumber = redisUtil.getValues(code);

        if (receivingNumber != null && receivingNumber.equals(verifyCodeRequestDto.receivingNumber())) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));

            //본인인증 회원으로 전환
            user.updateVerifiedInfo(receivingNumber);
            return VerifyCodeResponseDto.builder()
                    .isVerify(true).build();}

        return VerifyCodeResponseDto.builder()
                .isVerify(false).build();
    }
}
