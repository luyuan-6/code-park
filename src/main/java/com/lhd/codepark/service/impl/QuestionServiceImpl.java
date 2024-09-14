package com.lhd.codepark.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhd.codepark.common.ErrorCode;
import com.lhd.codepark.constant.CommonConstant;
import com.lhd.codepark.exception.BusinessException;
import com.lhd.codepark.exception.ThrowUtils;
import com.lhd.codepark.mapper.QuestionMapper;
import com.lhd.codepark.model.dto.question.CodeTemplate;
import com.lhd.codepark.model.dto.question.QuestionQueryRequest;
import com.lhd.codepark.model.entity.*;
import com.lhd.codepark.model.vo.QuestionVO;
import com.lhd.codepark.model.vo.UserVO;
import com.lhd.codepark.service.QuestionService;
import com.lhd.codepark.service.UserService;
import com.lhd.codepark.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author lhd666
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-05-31 21:21:01
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {
    @Resource
    private UserService userService;

    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }

    }


    /**
     * 根据QuestionQueryRequest对象生成QueryWrapper，用于查询Question实体。
     * 该方法通过对请求对象的属性进行判断，构造相应的查询条件。
     *
     * @param questionQueryRequest 查询条件请求对象，包含对问题的各种查询条件。
     * @return QueryWrapper<Question> 包含查询条件的QueryWrapper对象。
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        // 初始化QueryWrapper对象
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        // 如果请求对象为空，则直接返回空的QueryWrapper对象
        if (questionQueryRequest == null) {
            return queryWrapper;
        }

        // 从请求对象中获取各项查询条件
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tagList = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 根据标题、内容和答案生成模糊查询条件
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);

        // 如果标签列表不为空，生成标签的查询条件
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 根据ID和用户ID生成精确查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        // 设置删除标志为未删除的条件
        queryWrapper.eq("isDelete", false);

        // 根据排序字段和排序方式生成排序条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        // 返回构造好的QueryWrapper对象
        return queryWrapper;
    }



    /**
     * 根据问题对象和HTTP请求，获取问题的视图对象。
     * 此方法将问题对象转换为问题视图对象，并附加相关用户信息。
     *
     * @param question 问题对象，包含问题的具体信息。
     * @param request HTTP请求对象，用于获取请求中的额外信息（本方法中未使用）。
     * @return 返回包含问题信息和相关用户信息的问题视图对象。
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 将问题对象转换为问题视图对象
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 获取问题所属用户ID
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        // 根据用户ID查询用户对象
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        // 将用户对象转换为用户视图对象
        UserVO userVO = userService.getUserVO(user);
        // 将用户视图对象设置到问题视图对象中
        //添加不同的语言的代码模板
        String codeTemplateStr = ResourceUtil.readUtf8Str("CodeTemplate.json");
        CodeTemplate codeTemplate = JSONUtil.toBean(codeTemplateStr, CodeTemplate.class);
        questionVO.setCodeTemplate(codeTemplate);
        questionVO.setUserVO(userVO);

        return questionVO;
    }

    /**
     * 根据问题页面对象获取问题的视图对象页面。
     * 此方法主要用于将数据库中查询到的问题对象转换为包含更多相关信息的视图对象，以便在前端展示。
     * 这个过程包括了查询相关用户信息，以便在问题列表中显示问题提问者的相关信息。
     *
     * @param questionPage 问题分页对象，包含当前页的问题列表。
     * @param request      HttpServletRequest 对象，用于获取请求相关的信息（本方法中未使用）。
     * @return 返回转换后的视图对象页面。
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        // 获取当前页的问题列表
        List<Question> questionList = questionPage.getRecords();
        // 初始化问题的视图对象页面
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());

        // 如果问题列表为空，则直接返回空的视图对象页面
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }

        // 统一获取所有问题提问者的用户ID，以便后续批量查询用户信息
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        // 根据用户ID批量查询用户信息，并按用户ID进行分组
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 将问题对象转换为视图对象，并填充提问者的信息
        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            // 从用户ID映射中获取对应用户信息，若存在则设置到问题视图对象中
            // todo user 有问题
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userService.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());

        // 设置转换后的视图对象列表到视图对象页面
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }


}




