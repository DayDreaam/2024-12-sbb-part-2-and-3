package com.mysite.sbb.domain.answer;

import com.mysite.sbb.domain.question.Question;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query("""
            SELECT a
            FROM Answer a
            LEFT JOIN a.voter v
            WHERE a.question = :question
            GROUP BY a
            ORDER BY COUNT(v) DESC,
            a.createDate DESC
            """)
    Page<Answer> findAllByQuestion(@Param("question") Question question, Pageable pageable);


    List<Answer> findTop10ByOrderByCreateDate();
}
