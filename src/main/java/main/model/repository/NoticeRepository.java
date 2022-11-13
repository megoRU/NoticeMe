package main.model.repository;

import main.model.entity.Subs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Subs, Long> {

    @Query(value = "SELECT s FROM Subs s WHERE s.userId = :userId AND s.server.guildIdLong = :guildId")
    List<Subs> findAllByUserIdAndGuildId(Long userId, Long guildId);

    @Query(value = "SELECT s FROM Subs s WHERE s.userId = :userId AND s.server.guildIdLong = :guildId AND s.userTrackingId = :userTrack")
    Subs findTrackingUser(Long userId, Long guildId, Long userTrack);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Subs s WHERE s.userTrackingId = :userTrack")
    void deleteAllByUserTrackingId(Long userTrack);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Subs s WHERE s.userTrackingId = :userTrack AND s.userId = :userId")
    void deleteByUserTrackingId(Long userTrack, Long userId);

}
