package com.pale.springbootinit.service;

import com.pale.springbootinit.model.entity.PostThumb;
import com.pale.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 帖子点赞服务
 *
 * @author <a href="https://github.com/PALE13">pale</a>
 * 
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
