package main.model.repository;

import main.model.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query(value = "SELECT n FROM Notice n WHERE n.userId = :userId AND n.guildId.guildId = :guildId")
    List<Notice> findAllByUserIdAndGuildId(Long userId, Long guildId);

    @Query(value = "SELECT n FROM Notice n WHERE n.userId = :userId AND n.guildId.guildId = :guildId AND n.userTrackingId = :userTrack")
    Notice findTrackingUser(Long userId, Long guildId, Long userTrack);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Notice n WHERE n.userTrackingId = :userTrack")
    void deleteByUserTrackingId(Long userTrack);

}
