package com.lhd.codepark.judge.codesandbox.strategy;

import com.lhd.codepark.judge.codesandbox.model.JudgeInfo;

public interface JudgeStrategy {

    /**
     * 执行判题方法
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
