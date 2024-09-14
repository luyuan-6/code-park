package com.lhd.codepark.judge.codesandbox.service;

import com.lhd.codepark.model.entity.QuestionSubmit;

public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
