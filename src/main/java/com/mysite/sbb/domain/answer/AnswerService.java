package com.mysite.sbb.domain.answer;


import com.mysite.sbb.global.exception.DataNotFoundException;
import com.mysite.sbb.domain.question.Question;
import com.mysite.sbb.domain.user.SiteUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = Answer.builder()
                .content(content)
                .question(question)
                .author(author)
                .build();
        answerRepository.save(answer);
        return answer;
    }

    public void modify(Answer answer, String content){
        answer.updateContent(content);
        answerRepository.save(answer);
    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> _answer = answerRepository.findById(id);
        if(_answer.isPresent()){
            return _answer.get();
        }else{
            throw new DataNotFoundException("answer not found");
        }
    }

    public void delete(Answer answer) {
        answerRepository.delete(answer);
    }

    public void vote(Answer answer, SiteUser siteUser){
        answer.getVoter().add(siteUser);
        answerRepository.save(answer);
    }

    public Page<Answer> answerPage(Question question, int page){
        Pageable pageable = PageRequest.of(page,10);
        return answerRepository.findAllByQuestion(question,pageable);
    }

    public List<Answer> recentAnswer() {
        return answerRepository.findTop10ByOrderByCreateDate();
    }

    public long count() {
        return answerRepository.count();
    }
}
