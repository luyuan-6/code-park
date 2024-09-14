package com.lhd.codepark.model.dto.question;

import com.lhd.codepark.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {
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
     * 创建用户 id
     */
    private Long userId;

    /**
     * 题目答案
     */
    private String answer;


    private static final long serialVersionUID = 1L;
}
