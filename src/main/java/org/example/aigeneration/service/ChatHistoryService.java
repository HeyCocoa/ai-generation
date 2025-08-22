package org.example.aigeneration.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.example.aigeneration.model.dto.chatHistory.ChatHistoryQueryRequest;
import org.example.aigeneration.model.entity.ChatHistory;
import org.example.aigeneration.model.entity.User;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author <a href="https://gitee.com/kokoa123">kokoa123</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatHistory(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest request);
}
