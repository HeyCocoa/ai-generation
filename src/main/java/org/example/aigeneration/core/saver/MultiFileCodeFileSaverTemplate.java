package org.example.aigeneration.core.saver;

import cn.hutool.core.util.StrUtil;
import org.example.aigeneration.ai.model.MultiFileCodeResult;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 *
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath){
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    /**
     * 获取代码类型
     *
     * @return
     */
    @Override
    public CodeGenTypeEnum getCodeType(){
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 验证输入参数
     *
     * @param result
     */
    @Override
    protected void validateInput(MultiFileCodeResult result){
        super.validateInput(result);
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if( StrUtil.isBlank(result.getHtmlCode()) ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
