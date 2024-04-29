package xu.yuan.Common;

import lombok.Data;

import java.io.Serializable;
@Data
public class PageRequest implements Serializable
{

    /**
     * 页面展示条数,并设置默认值
     */
    public int pageSize = 10;
    /**
     * 当前是第几页,并设置默认值
     */
    public int pageNum = 1;
}
