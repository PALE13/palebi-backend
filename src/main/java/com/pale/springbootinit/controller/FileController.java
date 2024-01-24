package com.pale.springbootinit.controller;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import com.pale.springbootinit.exception.ThrowUtils;
import com.pale.springbootinit.manager.AiManager;
import com.pale.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.pale.springbootinit.model.entity.Chart;
import com.pale.springbootinit.model.entity.User;
import com.pale.springbootinit.common.BaseResponse;
import com.pale.springbootinit.common.ErrorCode;
import com.pale.springbootinit.common.ResultUtils;
import com.pale.springbootinit.constant.FileConstant;
import com.pale.springbootinit.exception.BusinessException;
import com.pale.springbootinit.manager.CosManager;
import com.pale.springbootinit.model.dto.file.UploadFileRequest;
import com.pale.springbootinit.model.enums.FileUploadBizEnum;
import com.pale.springbootinit.model.vo.BiResponse;
import com.pale.springbootinit.service.ChartService;
import com.pale.springbootinit.service.UserService;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.pale.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/PALE13">pale</a>
 * 
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;
    
    @Resource
    private AiManager aiManager;



    /**
     * 校验文件
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
