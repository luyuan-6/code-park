package com.lhd.codepark.judge.codesandbox.strategy;

import com.lhd.codepark.judge.codesandbox.model.JudgeInfo;
import com.lhd.codepark.model.dto.question.JudgeCase;
import com.lhd.codepark.model.entity.Question;
import com.lhd.codepark.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文类
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
