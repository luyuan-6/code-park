package com.lhd.codepark.judge.codesandbox;

import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeRequest;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
