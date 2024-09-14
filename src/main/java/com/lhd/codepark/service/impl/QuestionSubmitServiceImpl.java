package com.lhd.codepark.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhd.codepark.common.ErrorCode;
import com.lhd.codepark.constant.CommonConstant;
import com.lhd.codepark.exception.BusinessException;
import com.lhd.codepark.judge.codesandbox.service.JudgeService;
import com.lhd.codepark.mapper.QuestionSubmitMapper;
import com.lhd.codepark.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.lhd.codepark.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.lhd.codepark.model.entity.Question;
import com.lhd.codepark.model.entity.QuestionSubmit;
import com.lhd.codepark.model.entity.User;
import com.lhd.codepark.model.enums.QuestionSubmitLanguageEnum;
import com.lhd.codepark.model.enums.QuestionSubmitStatusEnum;
import com.lhd.codepark.model.vo.QuestionSubmitVO;
import com.lhd.codepark.rabbitmq.MessageProducer;
import com.lhd.codepark.service.QuestionService;
import com.lhd.codepark.service.QuestionSubmitService;

import javax.annotation.Resource;

import com.lhd.codepark.service.UserService;
import com.lhd.codepark.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * 帖子点赞服务实现
 *
 * @author lhd
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;
    @Resource
    private MessageProducer messageProducer;


    /**
     * 处理题目的提交操作。
     *
     * @param questionSubmitAddRequest 提交请求的封装类，包含题目ID、代码和编程语言等信息。
     * @param loginUser                当前操作的用户信息。
     * @return 返回提交的题目ID。
     * @throws BusinessException 如果编程语言不合法、题目不存在或保存失败时，抛出业务异常。
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        // 根据题目ID获取题目信息
        Long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 获取当前登录用户的ID
        long userId = loginUser.getId();
        // 创建题目的提交记录对象
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 初始化判题信息和状态
        questionSubmit.setJudgeInfo("{}");
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        // 保存题目的提交记录
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交数据插入失败");
        }
        // 设置问题的提交数
        Integer submitNum = question.getSubmitNum();
        Question updateQuestion = new Question();
        synchronized (question.getSubmitNum()) {
            submitNum = submitNum + 1;
            updateQuestion.setId(questionId);
            updateQuestion.setSubmitNum(submitNum);
            save = questionService.updateById(updateQuestion);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
            }
        }

        Long questionSubmitId = questionSubmit.getId();

        // 执行判题服务
        // 同步
        judgeService.doJudge(questionSubmitId);
        // 线程池异步
//        CompletableFuture.runAsync(() -> {
//            judgeService.doJudge(questionSubmitId);
//        });

        // 发送消息 RabbitMQ
        messageProducer.sendMessage("code_exchange", "my_routingKey", String.valueOf(questionSubmitId));

        // 返回提交记录的ID
        return questionSubmitId;
    }


    /**
     * 根据请求参数构建查询条件封装类。
     * 该方法用于根据前端传递的查询请求对象，生成相应的MyBatis查询包装器，以实现动态拼接查询条件的功能。
     * 主要用于筛选问题提交记录，支持多条件查询和排序。
     *
     * @param questionSubmitQueryRequest 查询请求对象，包含各种查询条件。
     * @return 返回构建好的QueryWrapper对象，用于MyBatis的查询操作。
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        // 初始化查询包装器
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        // 如果请求对象为空，则直接返回空的查询包装器
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        // 从请求对象中获取各项查询条件
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 根据条件动态拼接查询语句
        // 如果语言不为空，则添加语言查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        // 如果用户ID不为空，则添加用户ID查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 如果问题ID不为空，则添加问题ID查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 如果状态不为空且有效，则添加状态查询条件
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        // 添加是否删除的查询条件，只查询未删除的记录
        queryWrapper.eq("isDelete", false);
        // 根据排序字段和排序方式添加排序条件，支持升序和降序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        // 返回构建好的查询包装器
        return queryWrapper;
    }


    /**
     * 根据QuestionSubmit对象获取对应的QuestionSubmitVO对象。
     * 此方法用于在前端展示问题提交的信息，但为了保护用户隐私，如果当前登录用户不是提交问题的用户且不是管理员，
     * 则不返回问题的代码内容。
     *
     * @param questionSubmit 问题提交实体类，包含问题的详细信息和代码。
     * @param loginUser      当前登录的用户信息。
     * @return 返回QuestionSubmitVO对象，包含问题的展示信息。
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        // 将QuestionSubmit实体类转换为VO对象，用于前端展示。
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 获取当前登录用户的ID。
        long userId = loginUser.getId();
        // 如果当前登录用户不是问题提交者且不是管理员，则隐藏代码内容。
        // 脱敏: 仅本人和管理员能看到自己(提交 userId 和 登录用户id不同) 提交的代码
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 根据题目提交的分页信息，获取对应的题目提交详情的分页对象。
     * 此方法用于将数据库查询出的题目提交实体（QuestionSubmit）转换为前端展示所需的形式（QuestionSubmitVO）。
     *
     * @param questionSubmitPage 题目提交的分页实体，包含当前页的题目提交列表。
     * @param loginUser          当前登录的用户信息，用于权限判断或个性化处理。
     * @return 返回转换后的题目提交详情的分页对象。
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        // 获取当前页的题目提交列表
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();

        // 初始化题目提交详情的分页对象
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());

        // 如果当前页的题目提交列表为空，则直接返回空的分页对象
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }

        // 将题目提交实体列表转换为题目提交详情VO列表
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());

        // 将转换后的详情VO列表设置到分页对象中
        questionSubmitVOPage.setRecords(questionSubmitVOList);

        // 返回转换后的分页对象
        return questionSubmitVOPage;
    }

}




