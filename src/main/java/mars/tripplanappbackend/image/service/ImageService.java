package mars.tripplanappbackend.image.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.global.enums.ErrorCode;
import mars.tripplanappbackend.global.enums.File;
import mars.tripplanappbackend.global.exception.BusinessException;
import mars.tripplanappbackend.image.response.PresignedUrlResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 이미지 업로드 및 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * 파일 업로드를 위한 Presigned URL을 생성하여 반환
     * 한 파일 당 최대 10mb 제한 있음
     * 10MB를 초과하는 파일 업로드 시 minio 서버에서 알아서 거절됨
     *
     * @param usersId  JWT 토큰에서 추출된 사용자
     * @param fileName 클라이언트가 전송한 원본 파일명
     * @param domain   파일이 저장될 도메인 카테고리
     * @return PresignedUrlResponseDto 업로드 URL, 저장 경로, 도메인 정보를 포함한 응답 객체
     * @throws BusinessException 파일명이 없거나 유효하지 않은 확장자인 경우 (INVALID_INPUT, INVALID_FILE_FORMAT)
     * @throws BusinessException MinIO 서버 통신 중 오류 발생 시 (INTERNAL_ERROR)
     */
    @Transactional
    public PresignedUrlResponseDto getUploadUrl(String usersId, String fileName, File domain) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (!isValidExtension(fileName)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // 파일명 생성 (UUID 8자리 + 현재시간 조합으로 중복 방지)
        try {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            String savedFileName = String.format("%s_%d.%s",
                    java.util.UUID.randomUUID().toString().substring(0, 8),
                    System.currentTimeMillis(),
                    extension);
            
            // 저장 경로 {도메인명}/{유저ID}/{저장파일명}
            String objectName = String.format("%s/%s/%s",
                    domain.getDirectory(), usersId, savedFileName);

            // Presigned URL 생성 (유효기간 5분)
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(5, TimeUnit.MINUTES)
                            .build()
            );

            return new PresignedUrlResponseDto(url, objectName, domain);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // 허용 확장자명
    private boolean isValidExtension(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".png") ||
                lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".webp");
    }
}
