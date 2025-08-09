package org.example.aigeneration.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.example.aigeneration.model.dto.app.AppQueryRequest;
import org.example.aigeneration.model.entity.App;
import org.example.aigeneration.model.vo.AppVO;

import java.util.List;

/**
 * 服务层。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
public interface AppService extends IService<App>{
    
    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> records);
}
