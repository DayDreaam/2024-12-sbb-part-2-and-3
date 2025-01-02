package com.mysite.sbb.global.util;

import com.mysite.sbb.domain.answer.AnswerService;
import com.mysite.sbb.domain.category.CategoryService;
import com.mysite.sbb.domain.question.QuestionService;
import com.mysite.sbb.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;

    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.initCategory();
            self.initUser();
            self.initQuestion();
            self.initAnswer();
        };
    }

    @Transactional
    public void initCategory() {
        if(categoryService.count()>0){
            return;
        }
        categoryService.make("질문답변");
        categoryService.make("강좌");
        categoryService.make("자유게시판");
    }

    @Transactional
    public void initUser() {
        if(userService.count()>0){
            return;
        }
        userService.create("지식인123","jisik@asdfsdf.com","비밀번호123");
        userService.create("검은수염","kurohige@asdasdf.com","크로우즈");
        userService.create("한석원","hansukwon@asdfsadf.com","수학의신");
        userService.create("emmoogg","emmoogg@gmail.com","password");
    }

    @Transactional
    public void initQuestion() {
        if(questionService.count()>0){
            return;
        }
        for(int i=0;i<100;i++){
            questionService.create(String.format("한석원과 수학 데이트 %d",i+1),"대충 강의내용",userService.getUser("한석원"),categoryService.findById(2));
        }
        questionService.create("스프링부트 질문","계속 연습해보고는 있지만 아직 어렵네요.",userService.getUser("지식인123"),categoryService.findById(1));
        questionService.create("제하하하하하","이 공격은 대체 뭐냐 에이스",userService.getUser("검은수염"),categoryService.findById(3));
    }

    @Transactional
    public void initAnswer() {
        if(answerService.count()>0){
            return;
        }
        answerService.create(questionService.getQuestion(101),"어려울만함",userService.getUser("검은수염"));
        answerService.create(questionService.getQuestion(101),"공부가 어렵다면 내 강의를 봐라",userService.getUser("한석원"));
    }

}

