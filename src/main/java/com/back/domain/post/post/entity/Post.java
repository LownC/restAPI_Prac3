package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Post extends BaseEntity {
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 글이 저장/삭제될 때 댓글도 함께 저장/삭제되고, 컬렉션에서 빠진 댓글은 DB 에서도 삭제된다.
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content);
        comments.add(postComment);

        return postComment;
    }

    // 이 글에 달린 댓글 중에서만 찾는다. (다른 글의 댓글 id 면 empty)
    public Optional<PostComment> findCommentById(int id) {
        return comments.stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }
}
