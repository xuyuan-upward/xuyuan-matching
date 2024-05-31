package xu.yuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xu.yuan.model.domain.Message;
import xu.yuan.service.MessageService;
import xu.yuan.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 肖广龙
* @description 针对表【message】的数据库操作Service实现
* @createDate 2024-05-30 21:08:07
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

}




