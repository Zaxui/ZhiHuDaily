package com.example.lenovo.jiazhihu.model;

import java.util.List;

/**
 * Created by lenovo on 2017/8/4.
 */

public class Comments {
    private List<Comment> comments;

    public List<Comment> getComments(){
        return comments;
    }
    public void Comments( List<Comment> comments){
        this.comments = comments;
    }
}
