package org.example.aigeneration.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.example.aigeneration.model.dto.app.AppAddRequest;
import org.example.aigeneration.model.dto.app.AppQueryRequest;
import org.example.aigeneration.model.entity.App;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 服务层。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
public interface AppService extends IService<App>{

    /**
     * 根据App对象获取对应的AppVO对象
     *
     * @param app App对象，包含原始应用信息
     * @return AppVO对象，可能是视图对象或数据传输对象，用于展示或传输应用信息
     */
    AppVO getAppVO(App app);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 根据应用查询请求参数构建查询条件包装器
     *
     * @param appQueryRequest 应用查询请求参数，包含查询条件和过滤信息
     * @return 返回一个QueryWrapper对象，用于构建数据库查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象(AppVO)列表的方法
     *
     * @param records 包含应用实体对象(App)的列表
     * @return 返回包含应用视图对象(AppVO)的列表，用于前端展示
     */
    List<AppVO> getAppVOList(List<App> records);

    /**
     * 根据用户消息生成代码的响应流方法
     *
     * @param userMessage 用户输入的消息内容
     * @param appId       应用程序ID，用于标识具体的应用
     * @param loginUser   当前登录用户信息，用于权限验证等操作
     * @return 返回一个Flux<String>类型的响应流，包含生成的代码内容
     */
    Flux<String> chatToGenCode(String userMessage, Long appId, User loginUser);

    /**
     * 部署应用程序的方法
     *
     * @param appId     应用程序的唯一标识符
     * @param loginUser 当前登录用户的信息
     * @return 返回一个字符串，可能表示部署结果或相关信息
     */
    String deployApp(Long appId, User loginUser);

    void generateAppScreenshot(Long appId, String appUrl);
}
