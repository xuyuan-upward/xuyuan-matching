package xu.yuan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.Blog;
import xu.yuan.model.domain.Follow;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.BlogAddRequest;
import xu.yuan.model.vo.BlogVO;
import xu.yuan.model.vo.UserVO;
import xu.yuan.service.BlogService;
import xu.yuan.mapper.BlogMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.FollowService;
import xu.yuan.service.UserService;
import xu.yuan.utils.AliOSSUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static xu.yuan.Common.SystemCommon.PAGE_SIZE;

/**
* @author 肖广龙
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2024-06-08 14:48:01
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{
    /**
     * oos功能
     */
    @Resource
private AliOSSUtils aliOSSUtils;

    @Resource
    private UserService userService;
    @Resource
    private FollowService followService;
    /**
     * 添加博客
     * @param blogAddRequest 添加博客内容
     * @param loginUser 当前登录用户
     */
    @Override
    public void addBlog(BlogAddRequest blogAddRequest, User loginUser) {
        // 获取博客内容
        MultipartFile[] images = blogAddRequest.getImages();
        // 存储每一张照片
        ArrayList<String> imageList = new ArrayList<>();
        // 上传照片
        String upload = null;
        for (MultipartFile image : images) {
            try {
                upload = aliOSSUtils.upload(image);
                imageList.add(upload);
            } catch (IOException e) {
                throw new BusinessEception(ErrorCode.SYSTEM, "系统出现异常");
            }
        }
        String content = blogAddRequest.getContent();
        String title = blogAddRequest.getTitle();
        String join = StringUtils.join(imageList, ",");
        //添加
        Blog blog = new Blog();
        blog.setContent(content);
        blog.setTitle(title);
        blog.setUserId(loginUser.getId());
        blog.setImages(join);
        this.save(blog);
    }

    /**
     * 获取信息列表
     * @param currentPage
     * @param searchText 关键字
     * @param loginId
     */
    @Override
    public Page<BlogVO> getList(long currentPage, String searchText, long loginId) {
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.like(StringUtils.isNotBlank(searchText), Blog::getTitle, searchText);
        blogLambdaQueryWrapper.orderBy(true, false, Blog::getCreateTime);
        Page<Blog> blogPage = this.page(new Page<>(currentPage, PAGE_SIZE), blogLambdaQueryWrapper);
        Page<BlogVO> blogVoPage = new Page<>();
        // 拷贝只是拷贝了分页信息（如总记录数、当前页码、页面大小等），
        // 但是它并没有转换 Page 对象内部的记录列表 (List<Blog> 类型的 records)
        // 的具体内容。records 仍然保持着 Blog 类型的对象。
        BeanUtils.copyProperties(blogPage, blogVoPage);
        List<Blog> blogList = blogPage.getRecords();
//        // 获取作者id
//        List<Long> authorIds = blogList.stream().map(Blog::getUserId).collect(Collectors.toList());
//        LambdaQueryWrapper<User> userwrapper = new LambdaQueryWrapper<>();
//        userwrapper.in(User::getId,authorIds);
//        // 获取作者
//        List<UserVO> authorList = userService.list(userwrapper).stream().map(user -> {
//            UserVO userVO = new UserVO();
//            BeanUtils.copyProperties(user, userVO);
//            return userVO;
//        }).collect(Collectors.toList());
        List<BlogVO> blogVOList = blogList.stream().map((blog) -> {
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            return blogVO;
        }).collect(Collectors.toList());
//        List<BlogVO> blogVOList = blogPage.getRecords().stream().map((blog) -> {
//            BlogVO blogVO = new BlogVO();
//            BeanUtils.copyProperties(blog, blogVO);
//            if (userId != null) {
//                LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                blogLikeLambdaQueryWrapper.eq(BlogLike::getBlogId, blog.getId()).eq(BlogLike::getUserId, userId);
//                long count = blogLikeService.count(blogLikeLambdaQueryWrapper);
//                blogVO.setIsLike(count > 0);
//            }
//            return blogVO;
//        }).collect(Collectors.toList());
        // 展示图片 默认展示第一张图片
        List<BlogVO> blogWithCoverImg = getFirstCoverImg(blogVOList);
        // 获取作者的信息 效率会很低
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogWithCoverImg.stream().forEach(blogVO -> {
            userLambdaQueryWrapper.eq(User::getId, blogVO.getUserId()).select(User::getUsername);
            User one = userService.getOne(userLambdaQueryWrapper);
            userLambdaQueryWrapper.clear();
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(one,userVO);
            blogVO.setAuthor(userVO);
        });
        blogVoPage.setRecords(blogWithCoverImg);
        return blogVoPage;
    }

    /**
     * 通过id回显博文信息
     * @param id 博文id
     * @param loginId
     * @return
     */
    @Override
    public BlogVO getBlogById(Long id, long loginId) {
     // 根据id 查找博文
        Blog blog = this.getById(id);
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog,blogVO);
        // 当前发表博主的id 图片 覆盖
        Long userId = blog.getUserId();
        User Bloguser = userService.getById(userId);
        UserVO userVO = new UserVO();
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, loginId).eq(Follow::getFollowUserId, userId);
        userVO.setIsFollow(followService.count(wrapper)> 0);
        BeanUtils.copyProperties(Bloguser,userVO);
        blogVO.setAuthor(userVO);
        blogVO.setCoverImage(blogVO.getImages().split(",")[0]);
        // TODO 赞人数显示先不写 还有就是是否显示自己已经赞功能也不写 => 先默认
        blogVO.setIsLike(false);
        blogVO.setLikedNum(1);
        blogVO.setCommentsNum(0);
        return blogVO;
    }

    /**
     *
     * @param id 文章
     * @param loginId
     * @param admin
     */
    @Override
    public void IsAuthriotyDeleteBlog(Long id, long loginId, boolean admin) {
        Blog blog = this.getById(id);
        // 删除文章判断是否有权限
        if (!admin && loginId != blog.getUserId()) {
            throw new BusinessEception(ErrorCode.NO_AUTH, "不是作者，没有权限删除");
        }
        this.removeById(id);
    }

    /**
     * 获取博客的第一张图片
     */
    private List<BlogVO>  getFirstCoverImg(List<BlogVO> blogVOList){
        if (CollectionUtil.isEmpty(blogVOList)) {
            return new ArrayList<>();
        }
        for (BlogVO blogVO : blogVOList) {
            String images = blogVO.getImages();
            String[] split = images.split(",");
            if (split.length == 0) {
                blogVO.setCoverImage("");
            }
            blogVO.setCoverImage(split[0]);
        }
        return  blogVOList;
    }
}




