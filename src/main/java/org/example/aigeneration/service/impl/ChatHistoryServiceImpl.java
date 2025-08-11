package org.example.aigeneration.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aigeneration.constant.UserConstant;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.exception.ThrowUtils;
import org.example.aigeneration.mapper.ChatHistoryMapper;
import org.example.aigeneration.model.dto.chatHistory.ChatHistoryQueryRequest;
import org.example.aigeneration.model.entity.App;
import org.example.aigeneration.model.entity.ChatHistory;
import org.example.aigeneration.model.entity.User;
import org.example.aigeneration.model.enums.ChatHistoryMessageTypeEnum;
import org.example.aigeneration.service.AppService;
import org.example.aigeneration.service.ChatHistoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 服务层实现。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService{

    @Lazy
    @Resource
    private AppService appService;


    /**
     * 添加聊天历史记录
     * @param appId 应用ID
     * @param message 聊天消息内容
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return 是否添加成功
     */
    @Override
    public boolean addChatHistory(Long appId, String message, String messageType, Long userId){
        //校验数据
        ThrowUtils.throwIf(appId==null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(message==null, ErrorCode.PARAMS_ERROR, "message不能为空");
        ThrowUtils.throwIf(messageType==null, ErrorCode.PARAMS_ERROR, "messageType不能为空");
        ThrowUtils.throwIf(userId==null, ErrorCode.PARAMS_ERROR, "userId不能为空");
        //封装消息类型，并判断是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum==null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        //保存数据
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageTypeEnum.getValue())
                .build();
        // 保存聊天历史记录并返回保存结果
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId){
        ThrowUtils.throwIf(appId==null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser){
        //校验数据
        ThrowUtils.throwIf(appId==null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(loginUser==null, ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        ThrowUtils.throwIf(pageSize < 1 || pageSize > 50, ErrorCode.PARAMS_ERROR, "pageSize不能小于1或大于50");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //校验权限, 只有管理员和用户自己可以查看
        boolean isAdmin = app.getUserId().equals(loginUser.getId());
        boolean is99 = app.getPriority()==99L;
        ThrowUtils.throwIf(!isAdmin && !is99, ErrorCode.NO_AUTH_ERROR, "没有权限");
        //创建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        //分页查询
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if ( CollUtil.isEmpty(historyList)){
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            Collections.reverse(historyList);
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }


    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest request){
        QueryWrapper queryWrapper = QueryWrapper.create();
        if( request==null ){
            return queryWrapper;
        }
        queryWrapper.eq("appId", request.getAppId());
        if( request.getLastCreateTime()!=null ){
            queryWrapper.lt("createTime", request.getLastCreateTime());
        }
        if( StrUtil.isNotBlank(request.getSortField()) ){
            queryWrapper.orderBy(request.getSortField(), "descend".equals(request.getSortOrder()));
        }
        else{
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

}
