package com.machpay.api.quiz;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.dto.QuizPlayResponse;
import com.machpay.api.quiz.repository.QuizAnswerRepository;
import com.machpay.api.quiz.repository.QuizPlayRepository;
import com.machpay.api.quiz.repository.QuizQuestionRepository;
import com.machpay.api.quiz.repository.QuizResultRepository;
import com.machpay.api.quiz.repository.QuizSeasonRepository;
import com.machpay.api.user.UserService;
import com.machpay.api.user.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizService {
    @Autowired
    private QuizMapper quizMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private QuizPlayRepository quizPlayRepository;

    @Autowired
    private QuizSeasonRepository quizSeasonRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    public QuizAnswer findAnswerByQuestionAndId(QuizQuestion question, Long id) {
        return quizAnswerRepository.findByQuestionAndId(question, id).orElseThrow(() -> new ResourceNotFoundException("Answer", "id", id));
    }

    public QuizQuestion findQuestionById(UUID id) {
        return quizQuestionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", "id",
                id));
    }

    @Transactional
    public void createQuestion(QuestionRequest questionRequest, Long userId) {
        validateAnswers(questionRequest.getAnswers());
        User user = userService.findById(userId);
        QuizQuestion quizQuestion = quizMapper.toQuizQuestion(questionRequest);
        quizQuestion.setUser(user);

        quizQuestionRepository.save(quizQuestion);
        createAnswers(quizQuestion, questionRequest.getAnswers());
    }

    private void validateAnswers(List<QuestionRequest.Answer> answers) {
        int correctAnswerCount = 0;

        for (QuestionRequest.Answer answer : answers) {
            if (answer.isCorrect())
                correctAnswerCount++;
        }

        if (correctAnswerCount > 1)
            throw new BadRequestException("Multiple options cannot be correct");
    }

    @Transactional
    public void createAnswers(QuizQuestion question, List<QuestionRequest.Answer> answers) {
        List<QuizAnswer> quizAnswers = quizMapper.toQuizAnswerList(answers);
        quizAnswers = quizAnswers.stream().map(answer -> setQuestion(question, answer)).collect(Collectors.toList());

        quizAnswerRepository.saveAll(quizAnswers);
    }

    private QuizAnswer setQuestion(QuizQuestion question, QuizAnswer answer) {
        answer.setQuestion(question);

        return answer;
    }

    public QuestionResponse getLatestQuestion() {
        QuizQuestion quizQuestion = quizQuestionRepository.findFirstByOrderByCreatedAtDesc();
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAllByQuestion(quizQuestion);
        QuestionResponse questionResponse = quizMapper.toQuestionResponse(quizQuestion);
        questionResponse.setAnswers(quizMapper.toAnswerResponseList(quizAnswers));

        return questionResponse;
    }

    @Transactional
    public boolean checkAnswer(AnswerRequest answerRequest, Long userId) {
        User user = userService.findById(userId);
        QuizQuestion quizQuestion = findQuestionById(answerRequest.getQuestion());
        QuizAnswer quizAnswer = findAnswerByQuestionAndId(quizQuestion, answerRequest.getAnswer());

        if (quizAnswer.isCorrect())
            updateQuizPlay(user, quizQuestion.getPoint());

        return quizAnswer.isCorrect();
    }

    public QuizSeason getActiveSeason() {
        return quizSeasonRepository.findFirstByActiveTrue().orElseThrow(() -> new ResourceNotFoundException("Quiz " +
                "Season", "active", true));
    }

    @Transactional
    public QuizPlay updateQuizPlay(User user, Long point) {
        QuizSeason quizSeason = getActiveSeason();
        Optional<QuizPlay> existingPoints = quizPlayRepository.findByUserAndSeason(user, quizSeason);

        if (existingPoints.isPresent()) {
            QuizPlay existingQuizPlay = existingPoints.get();
            existingQuizPlay.setPoint(existingQuizPlay.getPoint() + point);
            existingQuizPlay.setGamePlayed(existingQuizPlay.getGamePlayed() + 1);

            return quizPlayRepository.save(existingQuizPlay);
        }

        QuizPlay quizPlay = new QuizPlay();
        quizPlay.setUser(user);
        quizPlay.setPoint(point);
        quizPlay.setSeason(quizSeason);
        quizPlay.setGamePlayed(Long.valueOf(1));

        return quizPlayRepository.save(quizPlay);
    }

    public List<QuizPlayResponse> getLeaderBoard() {
        List<QuizPlay> quizPlays = quizPlayRepository.findAllByOrderByPointDesc();

        return getQuizPlayResponse(quizPlays);
    }

    private List<QuizPlayResponse> getQuizPlayResponse(List<QuizPlay> quizPlays) {
        return quizPlays.stream().map(quizPlay -> {
            QuizPlayResponse quizPlayResponse = new QuizPlayResponse();
            quizPlayResponse.setPoint(quizPlay.getPoint());
            quizPlayResponse.setGamePlayed(quizPlay.getGamePlayed());
            quizPlayResponse.setPlayer(getPlayer(quizPlay.getUser()));

            return quizPlayResponse;
        }).collect(Collectors.toList());
    }

    private QuizPlayResponse.Player getPlayer(User user) {
        QuizPlayResponse.Player player = new QuizPlayResponse.Player();
        Member member = memberService.findById(user.getId());
        player.setName(member.getFullName());
        player.setPhoto(member.getImageUrl());

        return player;
    }
}
