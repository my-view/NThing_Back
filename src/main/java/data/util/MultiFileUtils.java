package data.util;

import data.config.UploadPathConfig;
import data.dto.FileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MultiFileUtils {

    private final UploadPathConfig uploadPathConfig;

    /**
     * 다중 파일 업로드
     * @param multipartFiles - 파일 객체 List
     * @return DB에 저장할 파일 정보 List
     */
    public List<FileDto.Request> uploadFiles(final List<MultipartFile> multipartFiles, String saveRoute) {
        List<FileDto.Request> files = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) {
                continue;
            }
            files.add(uploadFile(multipartFile, saveRoute));
        }
        return files;
    }

    /**
     * 단일 파일 업로드
     * @param multipartFile - 파일 객체
     * @return DB에 저장할 파일 정보
     */
    public FileDto.Request uploadFile(final MultipartFile multipartFile, String saveRoute) {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String saveName = generateSaveFilename(multipartFile.getOriginalFilename());
        String uploadPath = uploadPathConfig.getUploadPath() + saveRoute + File.separator + saveName;
        File uploadFile = new File(uploadPath);

        try {
            multipartFile.transferTo(uploadFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return FileDto.Request.builder()
                .original_name(multipartFile.getOriginalFilename())
                .save_name(saveRoute+"/"+saveName)
                .size((int) multipartFile.getSize())
                .build();
    }

    /**
     * 파일 로컬 경로 변환
     * @param fileList 파일 리스트
     * @return 파일명 수정된 파일 리스트
     */
    public List<FileDto.Response> generateFilePath(List<FileDto.Response> fileList) {
        List<FileDto.Response> generatedFileList = new ArrayList<>();

        for (FileDto.Response file : fileList) {
            String filePath = file.getSaveName();
            file.setSaveName(filePath != null
                    ? getDomain() + file.getSaveName()
                    : null
            );
            generatedFileList.add(file);
        }
        return generatedFileList;
    }

    /**
     * 저장 파일명 생성
     * @param filename 원본 파일명
     * @return 로컬에 저장할 파일명
     */
    private String generateSaveFilename(final String filename) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String extension = StringUtils.getFilenameExtension(filename);
        return uuid + "." + extension;
    }

    /**
     * 파일 삭제 (from Disk)
     * @param files - 삭제할 파일 정보 List
     */
    public void deleteFiles(final List<FileDto.Response> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        for (FileDto.Response file : files) {
            String filePath = file.getSaveName();
            deleteFile(filePath);
        }
    }

    /**
     * 파일 삭제 (from Disk)
     * @param filePath - 저장 경로
     */
    private void deleteFile(final String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            deleteFile(filePath);
        } else {
            System.out.println("File not found: " + filePath);
        }
    }

    public String getDomain() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            if (Objects.equals(serverName, "localhost")) {
                int serverPort = request.getServerPort();
                return scheme + "://" + serverName + ":" + serverPort + "/" + "file/";
            } else {
                return scheme + "s://" + serverName + "/" + "file/";
            }
        }
        return "";
    }

}