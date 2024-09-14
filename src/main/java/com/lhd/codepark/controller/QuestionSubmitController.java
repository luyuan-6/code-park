package com.lhd.codepark.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhd.codepark.common.BaseResponse;
import com.lhd.codepark.common.ErrorCode;
import com.lhd.codepark.common.ResultUtils;
import com.lhd.codepark.exception.BusinessException;
import com.lhd.codepark.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lhd.codepark.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.lhd.codepark.model.entity.QuestionSubmit;
import com.lhd.codepark.model.entity.User;
import com.lhd.codepark.model.vo.QuestionSubmitVO;
import com.lhd.codepark.service.QuestionSubmitService;
import com.lhd.codepark.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author lhd 
 */
@RestController
//@RequestMapping("/questionSubmit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;
//
//    /**
//     * 处理问题提交的请求。
//     *
//     * @param questionSubmitAddRequest 包含问题提交相关信息的请求体。
//     * @param request HTTP请求对象，用于获取登录用户信息。
//     * @return 返回处理结果，包含提交的问题ID。
//     * @throws BusinessException 如果请求参数无效，则抛出业务异常。
//     */
//    @PostMapping("/")
//    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
//                                             HttpServletRequest request) {
//        // 校验请求参数的合法性
//        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//
//        // 获取登录用户信息
//        final User loginUser = userService.getLoginUser(request);
//
//        // 执行问题提交操作
//        long questionId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
//
//        // 返回成功的响应，包含问题ID
//        return ResultUtils.success(questionId);
//    }
//
//
//    /**
//     * 分页获取题目提交列表。
//     * 该接口用于根据请求中的条件，分页获取题目的提交记录。除了管理员外，普通用户只能看到非答案、提交代码等公开信息。
//     *
//     * @param questionSubmitQueryRequest 包含查询条件和分页信息的请求对象。
//     * @param request                    HTTP请求对象，用于获取当前登录的用户信息。
//     * @return 返回包含题目提交信息的分页响应对象。
//     */
//    @PostMapping("/list/page")
//    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
//                                                                         HttpServletRequest request) {
//        // 解析请求中的当前页和每页大小
//        long current = questionSubmitQueryRequest.getCurrent();
//        long size = questionSubmitQueryRequest.getPageSize();
//
//        // 根据查询条件和分页信息，从数据库中查询题目提交的分页数据
//        // 从数据库中查询原始的题目提交分页信息
//        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
//                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
//
//        // 获取当前登录的用户信息
//        final User loginUser = userService.getLoginUser(request);
//
//        // 将查询到的题目提交分页数据转换为前端可显示的VO对象，并返回
//        // 返回脱敏信息
//        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
//    }
}
