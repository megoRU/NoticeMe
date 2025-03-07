package main.model.repository;

import jakarta.transaction.Transactional;
import main.model.entity.Subs;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Subs, Long> {

    @Query(value = "SELECT s FROM Subs s WHERE s.userId = :userId AND s.server.guildIdLong = :guildId")
    List<Subs> findAllByUserIdAndGuildId(@Param("userId") Long userId, @Param("guildId") Long guildId);

    @Query(value = "SELECT s FROM Subs s WHERE s.userId = :userId AND s.server.guildIdLong = :guildId AND s.userTrackingId = :userTrack")
    Subs findTrackingUser(@Param("userId") Long userId, @Param("guildId") Long guildId, @Param("userTrack") String userTrack);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Subs s WHERE s.userTrackingId = :userTrack")
    void deleteAllByUserTrackingId(@Param("userTrack") String userTrack);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Subs s WHERE s.userTrackingId = :userTrack AND s.userId = :userId")
    void deleteByUserTrackingId(@Param("userTrack") String userTrack, @Param("userId") Long userId);

    @Override
    @NotNull
    @EntityGraph(attributePaths = {"server"})
    List<Subs> findAll();
}