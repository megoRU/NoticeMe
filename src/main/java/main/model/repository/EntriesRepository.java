package main.model.repository;

import main.model.entity.Entries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntriesRepository extends JpaRepository<Entries, Long> {

    @Query(value = "SELECT e FROM Entries e " +
            "LEFT JOIN Subs s ON s.userId = e.userId " +
            "WHERE e.guildId = :guildId " +
            "AND e.userId = :userId " +
            "AND e.usersInChannel IS NOT NULL")
    List<Entries> getAllEntriesForSuggestion(@Param("userId") Long userId, @Param("guildId") Long guildId);
}
