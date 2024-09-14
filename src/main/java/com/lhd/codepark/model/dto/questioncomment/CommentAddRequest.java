package com.lhd.codepark.model.dto.questioncomment;


import com.lhd.codepark.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 15712
 */
@Data
public class CommentAddRequest implements Serializable {
    /**
     * 父级评论
     */
    private QuestionComment parentComment;

    /**
     * 当前评论
     */
    private QuestionComment currentComment;
}
