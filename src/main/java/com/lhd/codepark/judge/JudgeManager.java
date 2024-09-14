package com.lhd.codepark.judge;

import com.lhd.codepark.judge.codesandbox.model.JudgeInfo;
import com.lhd.codepark.judge.codesandbox.strategy.DefaultJudgeStrategy;
import com.lhd.codepark.judge.codesandbox.strategy.JavaJudgeStrategy;
import com.lhd.codepark.judge.codesandbox.strategy.JudgeContext;
import com.lhd.codepark.judge.codesandbox.strategy.JudgeStrategy;
import com.lhd.codepark.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理 简化调用
 */
@Service
public class JudgeManager {
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 执行判题逻辑
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)){
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
