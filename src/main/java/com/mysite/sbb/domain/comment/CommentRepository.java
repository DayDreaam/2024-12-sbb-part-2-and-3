package com.mysite.sbb.domain.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Integer> {

    List<Comment> findTop10ByOrderByCreateDateDesc();
}
