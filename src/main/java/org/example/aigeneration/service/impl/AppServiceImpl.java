package org.example.aigeneration.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aigeneration.ai.AiCodeGenTypeRoutingService;
import org.example.aigeneration.ai.AiCodeGenTypeRoutingServiceFactory;
import org.example.aigeneration.constant.AppConstant;
import org.example.aigeneration.core.AiCodeGeneratorFacade;
import org.example.aigeneration.core.builder.VueProjectBuilder;
import org.example.aigeneration.core.handler.StreamHandlerExecutor;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.exception.ThrowUtils;
import org.example.aigeneration.mapper.AppMapper;
import org.example.aigeneration.model.dto.app.AppAddRequest;
import org.example.aigeneration.model.dto.app.AppQueryRequest;
import org.example.aigeneration.model.entity.App;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.model.enums.ChatHistoryMessageTypeEnum;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;
import org.example.aigeneration.model.vo.AppVO;
import org.example.aigeneration.model.vo.UserVO;
import org.example.aigeneration.service.AppService;
import org.example.aigeneration.service.ChatHistoryService;
import org.example.aigeneration.service.ScreenshotService;
import org.example.aigeneration.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务层实现。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;
    @Value ("${code.deploy-host:http://localhost}")
    private String deployHost;

    /**
     * 将App实体对象转换为AppVO视图对象
     *
     * @param app App实体对象
     * @return 转换后的AppVO视图对象，如果输入为null则返回null
     */
    public AppVO getAppVO(App app){
        // 参数校验，如果app为null则直接返回null
        if( app==null ){
            return null;
        }
        // 创建AppVO实例并复制app的属性
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        // 如果userId不为空，则查询用户信息并设置到appVO中
        if( userId!=null ){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser){
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用工厂类每次生成新的 AI 智能选择代码生成
        AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = routingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest){
        if( appQueryRequest==null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList){
        // 检查输入的appList是否为空，如果为空则返回一个新的空ArrayList
        if( CollUtil.isEmpty(appList) ){
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList
                .stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app->{
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser){
        //校验信息
        ThrowUtils.throwIf(appId==null || appId < 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(userMessage==null, ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        //获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //校验权限
        if( !app.getUserId().equals(loginUser.getId()) ){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        //获取生成文件类型
        CodeGenTypeEnum fileType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(fileType==null, ErrorCode.SYSTEM_ERROR, "不支持的文件生成类型");
        //插入用户信息
        chatHistoryService.addChatHistory(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        //生成代码
        Flux<String> stream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, fileType, appId);
        //处理代码文件
        return streamHandlerExecutor.doExecute(stream, chatHistoryService, appId, loginUser, fileType);
    }

    @Override
    public String deployApp(Long appId, User loginUser){
        //校验信息
        ThrowUtils.throwIf(appId==null || appId < 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        //获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //校验权限
        if( !app.getUserId().equals(loginUser.getId()) ){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        //生成部署ID，如果有就不生成
        String deployKey = app.getDeployKey();
        if( deployKey==null ){
            deployKey = RandomUtil.randomString(6);
        }
        //获取生成文件类型以及文件保存位置
        String fileType = app.getCodeGenType();
        String sourcePath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/" + fileType + "_" + appId;
        //检查文件保存位置是否存在
        File sourceFile = new File(sourcePath);
        if( !sourceFile.exists() || !sourceFile.isDirectory() ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存位置不存在");
        }
        // Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(fileType);
        if( codeGenTypeEnum==CodeGenTypeEnum.VUE_PROJECT ){
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourcePath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File dist = new File(sourcePath, "dist");
            ThrowUtils.throwIf(!dist.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceFile = dist;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", dist.getAbsolutePath());
        }
        //执行部署操作
        String deployPath = AppConstant.CODE_DEPLOY_ROOT_DIR + "/" + deployKey;
        try {
            FileUtil.copyContent(sourceFile, new File(deployPath), true);
        } catch( Exception e ) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败");
        }
        //更新应用信息
        App appUpdate = App.builder()
                .id(appId)
                .deployKey(deployKey)
                .deployedTime(LocalDateTime.now())
                .build();
        boolean update = this.updateById(appUpdate);
        if( !update ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新应用信息失败");
        }
        // 10. 构建应用访问 URL
        String s = String.format("%s/%s/", deployHost, deployKey);
        // 11. 异步生成应用截图并更新封面
        generateAppScreenshotAsync(appId, s);
        //返回可访问的URL
        return s;
    }

    @Override
    public boolean removeById(Serializable id){
        if( id==null ){
            return false;
        }
        // 转换为 Long 类型
        long appId = Long.parseLong(id.toString());
        if( appId <= 0 ){
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch( Exception e ) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl){
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(()->{
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }
}
