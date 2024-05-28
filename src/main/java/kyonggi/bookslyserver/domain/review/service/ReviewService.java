package kyonggi.bookslyserver.domain.review.service;

import kyonggi.bookslyserver.domain.reservation.entity.Reservation;
import kyonggi.bookslyserver.domain.reservation.entity.ReservationSchedule;
import kyonggi.bookslyserver.domain.reservation.repository.ReservationRepository;
import kyonggi.bookslyserver.domain.review.dto.request.CreateReviewRequestDto;
import kyonggi.bookslyserver.domain.review.dto.response.CreateReviewResponseDto;
import kyonggi.bookslyserver.domain.review.dto.response.GetUserReviewResponseDto;
import kyonggi.bookslyserver.domain.review.entity.Review;
import kyonggi.bookslyserver.domain.review.entity.ReviewImage;
import kyonggi.bookslyserver.domain.review.repository.ReviewImageRepository;
import kyonggi.bookslyserver.domain.review.repository.ReviewRepository;
import kyonggi.bookslyserver.domain.user.service.UserQueryService;
import kyonggi.bookslyserver.global.aws.s3.AmazonS3Manager;
import kyonggi.bookslyserver.global.common.Uuid;
import kyonggi.bookslyserver.global.common.UuidRepository;
import kyonggi.bookslyserver.global.error.ErrorCode;
import kyonggi.bookslyserver.global.error.exception.ConflictException;
import kyonggi.bookslyserver.global.error.exception.EntityNotFoundException;
import kyonggi.bookslyserver.global.error.exception.ForbiddenException;
import kyonggi.bookslyserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static kyonggi.bookslyserver.global.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_PICTURE_COUNT = 4;

    private final UserQueryService userQueryService;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final AmazonS3Manager amazonS3Manager;
    private final UuidRepository uuidRepository;
    private final ReviewImageRepository reviewImageRepository;

    private void validateReviewPictures(List<MultipartFile> reviewPictures) {
        if (reviewPictures == null) {
            return;
        }

        if (reviewPictures.size() > MAX_PICTURE_COUNT) {
            throw new InvalidValueException(MAX_PICTURE_SIZE_OVER);
        }

        Set<String> pictureNames = new HashSet<>();
        for (MultipartFile picture : reviewPictures) {
            if (picture.getSize() > MAX_FILE_SIZE) {
                throw new InvalidValueException(MAX_FILE_SIZE_OVER);
            }

            if (!pictureNames.add(picture.getOriginalFilename())) {
                throw new InvalidValueException(FILE_NAME_DUPLICATE);
            }
        }
    }


    private Uuid createUuid() {
        String uuid = UUID.randomUUID().toString();
        Uuid savedUuid = uuidRepository.save(Uuid.builder()
                .uuid(uuid).build());
        return savedUuid;
    }

    public CreateReviewResponseDto createReview(Long userId, CreateReviewRequestDto createReviewRequestDto) {
        validateReviewPictures(createReviewRequestDto.getReviewPictures());

        Reservation reservation = reservationRepository.findById(createReviewRequestDto.getReservationId()).orElseThrow(() -> new EntityNotFoundException(RESERVATION_NOT_FOUND));
        if (reservation.getReview() != null) throw new ConflictException(ErrorCode.REVIEW_ALREADY_EXISTS);

        ReservationSchedule reservationSchedule = reservation.getReservationSchedule();
        Review review = Review.builder()
                .shop(reservationSchedule.getShop())
                .employee(reservationSchedule.getEmployee())
                .user(userQueryService.findUser(userId))
                .content(createReviewRequestDto.getContent())
                .rating(createReviewRequestDto.getRating()).build();

        Review savedReview = reviewRepository.save(review);
        reservation.addReview(savedReview);

        Uuid savedUuid = createUuid();

        createReviewRequestDto.getReviewPictures().forEach(picture-> {
            String pictureUrl = amazonS3Manager.uploadFile(
                    amazonS3Manager.generateReviewKeyName(savedUuid, picture.getOriginalFilename()), picture);

            ReviewImage reviewImage = ReviewImage.builder()
                    .review(savedReview)
                    .reviewImgUrl(pictureUrl)
                    .build();
            reviewImageRepository.save(reviewImage);
        });
        return CreateReviewResponseDto.of(savedReview);
    }

    public GetUserReviewResponseDto getUserReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new EntityNotFoundException(REVIEW_NOT_FOUND));
        log.info("로그인 유저 아이디: "+userId);
        if (review.getUser().getId() != userId) {
            throw new ForbiddenException();
        }
        return GetUserReviewResponseDto.of(review);
    }
}
