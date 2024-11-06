package main.model.repository;

import main.model.entity.Suggestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionsRepository extends JpaRepository<Suggestions, Long> {

    List<Suggestions> findAllByUserId(Long userId);

    List<Suggestions> findAllByUserIdAndGuildId(long userId, long guildId);
}