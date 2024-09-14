package com.lhd.codepark.judge.codesandbox.service.impl;

import cn.hutool.json.JSONUtil;
import com.lhd.codepark.common.ErrorCode;
import com.lhd.codepark.exception.BusinessException;
import com.lhd.codepark.judge.JudgeManager;
import com.lhd.codepark.judge.codesandbox.CodeSandbox;
import com.lhd.codepark.judge.codesandbox.CodeSandboxFactory;
import com.lhd.codepark.judge.codesandbox.CodeSandboxProxy;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeRequest;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeResponse;
import com.lhd.codepark.judge.codesandbox.model.JudgeInfo;
import com.lhd.codepark.judge.codesandbox.service.JudgeService;
import com.lhd.codepark.judge.codesandbox.strategy.JudgeContext;
import com.lhd.codepark.model.dto.question.JudgeCase;
import com.lhd.codepark.model.entity.Question;
import com.lhd.codepark.model.entity.QuestionSubmit;
import com.lhd.codepark.model.enums.QuestionSubmitStatusEnum;
import com.lhd.codepark.service.QuestionService;
import com.lhd.codepark.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        //  2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目判题状态更新失败");
        }
        // 4）调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();

        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 执行代码的请求信息
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        // 获取代码执行后的信息
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        //  5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setOutputList(outputList);
        judgeContext.setInputList(inputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        // 根据代码类型 使用策略模式进行判题
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目更新错误");
        }
        // 获取提交题目的信息
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);

        return questionSubmitResult;
    }
}
