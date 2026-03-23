package org.example.aigeneration.core.saver;

import cn.hutool.core.util.StrUtil;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath){
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    /**
     * 获取代码类型
     *
     * @return
     */
    @Override
    protected CodeGenTypeEnum getCodeType(){
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 验证输入参数
     *
     * @param result
     */
    @Override
    protected void validateInput(HtmlCodeResult result){
        super.validateInput(result);
        // HTML 代码不能为空
        if( StrUtil.isBlank(result.getHtmlCode()) ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
