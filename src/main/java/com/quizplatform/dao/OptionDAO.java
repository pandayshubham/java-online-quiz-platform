package com.quizplatform.dao;

import com.quizplatform.model.Option;
import java.util.List;

/**
 * Data Access Object interface for Question Option entities.
 */
public interface OptionDAO {

    int createOption(Option option);

    Option findById(int id);

    List<Option> findByQuestionId(int questionId);

    Option findCorrectOptionByQuestionId(int questionId);

    Option findCorrectOptionByQuestionId(java.sql.Connection conn, int questionId);

    boolean updateOption(Option option);

    boolean deleteOption(int id);

    boolean deleteByQuestionId(int questionId);
}

