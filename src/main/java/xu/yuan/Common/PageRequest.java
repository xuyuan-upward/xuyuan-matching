package xu.yuan.Common;

import lombok.Data;

import java.io.Serializable;
@Data
public class PageRequest implements Serializable
{

    private static final long serialVersionUID = -1851146430521931277L;
    /**
     * 页面展示条数,并设置默认值
     */
    public int pageSize = 10;
    /**
     * 当前是第几页,并设置默认值
     */
    public int pageNum = 1;
}
