package xu.yuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xu.yuan.model.domain.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.BlogAddRequest;
import xu.yuan.model.request.BlogUpdateRequest;
import xu.yuan.model.vo.BlogVO;

/**
* @author 肖广龙
* @description 针对表【blog】的数据库操作Service
* @createDate 2024-06-08 14:48:01
*/
public interface BlogService extends IService<Blog> {

    void addBlog(BlogAddRequest blogAddRequest, User loginUser);

    Page<BlogVO> getList(long currentPage, String searchText, long id);

    BlogVO getBlogById(Long id, long id1);


    void IsAuthriotyDeleteBlog(Long id, long id1, boolean admin);


    void IsAuthriotyUpdateBlog(Long id, User id1, boolean admin, BlogUpdateRequest blogUpdateRequest);
}
