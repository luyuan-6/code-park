package com.lhd.codepark.model.vo;


import cn.hutool.json.JSONUtil;
import com.lhd.codepark.model.dto.question.CodeTemplate;
import com.lhd.codepark.model.dto.question.JudgeConfig;
import com.lhd.codepark.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 */
@Data
public class QuestionVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;
    /**
     * 判题用例（json 数组）
     */
    private String judgeCase;

    /**
     * 点赞数
     */
    private Integer thumbNum;


    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     */
    private UserVO userVO;

    /**
     * 代码模板
     */
    private CodeTemplate codeTemplate;


    /**
     * 将问题的包装类转换为实际业务对象
     *
     * @param questionVO 问题的视图对象，包含展示用的信息
     * @return 转换后的实际业务问题对象，如果输入为null，则返回null
     */
    public static Question voToObj(QuestionVO questionVO) {
        // 检查输入的视图对象是否为null
        if (questionVO == null) {
            return null;
        }
        // 创建一个新的问题对象
        Question question = new Question();
        // 使用BeanUtils工具类复制属性，从视图对象到实际对象
        BeanUtils.copyProperties(questionVO, question);
        // 处理标签列表，将其转换为JSON字符串格式保存
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        // 处理判断配置，将其转换为JSON字符串格式保存
        JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
        if (voJudgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
        }
        return question;
    }


    /**
     * 将Question对象转换为QuestionVO对象
     *
     * @param question 需要转换的Question对象
     * @return 转换后的QuestionVO对象，如果输入的question为null，则返回null
     */
    public static QuestionVO objToVo(Question question) {
        // 检查输入的question对象是否为null
        if (question == null) {
            return null;
        }
        // 创建一个新的QuestionVO对象
        QuestionVO questionVO = new QuestionVO();
        // 使用BeanUtils复制question对象的属性到questionVO对象
        BeanUtils.copyProperties(question, questionVO);
        // 将question对象的tags属性转换为字符串列表
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTags(tagList);
        // 将question对象的judgeConfig属性从字符串转换为JudgeConfig对象
        String judgeConfigStr = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionVO;
    }


    private static final long serialVersionUID = 1L;
}
